/*
 * MobudsInternetConnection.java
 *
 * Created on 18 January 2007, 19:38
 *
 * Internet connection for loading/saving your profile from the 'net
 */

package mobuds;

import javax.microedition.io.*;
import javax.bluetooth.LocalDevice;
import java.io.*;
import java.util.*;

/**
 * Handles communication with the talkeasi.com website
 * @author rob smith
 */
public class MobudsInternetConnection {    
    public final static int HTTP_OK           = 0;     // Operation successful
    public final static int HTTP_ERROR        = 1;     // Connectivity Error
    public final static int HTTP_UNKNOWNUSER  = 2;     // Unknown user
    public final static int HTTP_UNKNOWNERROR = 3;     // Server error
    
    public boolean bUpgradeIsRequired;                 // After a connection, is an upgrade of the software required?
    
    private final static int MAX_CONNECTION_STEPS = 25;
    HttpConnection httpConnection;
    private MobudsSystem moSystem;
    DataInputStream     dis;
    DataOutputStream    dos;
    int iFieldCount;
    Hashtable tb;
    frmBusy busyWindow;
            
    // Creates a new instance of MobudsInternetConnection class
    public MobudsInternetConnection(MobudsSystem mosystem) {
        moSystem = mosystem;
        busyWindow = new frmBusy(mosystem);
    }
    
    // Set display progress
    private void setPosition(int nPos) {
        busyWindow.setProgress(nPos,MAX_CONNECTION_STEPS+10);
    }    
    
    
    // Get a response string for a field
    private String getResponseString(String s) {
        Object f = tb.get(s);        
        if (f==null) return ""; else {
            byte b[] = (byte[])f;
            return new String(b,0,b.length);
        }
    }
    
    // Send a field 
    private boolean sendField(String sName,String sValue) {
        boolean bRes;
        byte b[];
        String s = sName+"="+baseconf.urlEncode(sValue);
        if (iFieldCount>0) s="&"+s;
        iFieldCount++;        
        b = s.getBytes();
        
        try {
            dos.write(b);
            bRes = true;
        } catch (Exception e) {bRes = false;};
        return bRes;
    }
    
    // Connect to server
    public int connectToServer(String sUsername, String sPassword, boolean bSaveProfileMode) {
        int iResult;
        setPosition(0);
        dis = null;
        dos = null;
        iFieldCount = 0;           
        int i,a;
        String s;
        boolean bAdValid = false;
        
        busyWindow.show();
        moSystem.pause(100);
        
        bUpgradeIsRequired = false;
        
        // Ensure previous instance is shut
        if (httpConnection!=null) {
            try {httpConnection.close();} catch (Exception e) {};
            httpConnection=null;
        }
        
        
        try {
            // Now create a new one
            httpConnection = (HttpConnection)Connector.open(baseconf.TALKEASI_URL,Connector.READ_WRITE);

            //we're posting
            httpConnection.setRequestMethod(HttpConnection.POST);
            
            // One request only
            httpConnection.setRequestProperty("Connection","close"); 
                
            // Set a property that has the version in
            String ver = moSystem.getVersion();
            
            httpConnection.setRequestProperty("User-Agent","Mobuds/"+ver);
            
            // Lets try to look like a form submission
            httpConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            // Send the width and height to the server
            httpConnection.setRequestProperty("ScreenWidth",busyWindow.getWidth()+"");
            httpConnection.setRequestProperty("ScreenHeight",(busyWindow.getImageSpace())+"");

            // Send Username and Password in the header
            httpConnection.setRequestProperty("MoBudsUser",baseconf.convertToHexish(sUsername)+'-'+baseconf.convertToHexish(sPassword)+"-"+System.currentTimeMillis());            
            
            // Which mode?
            if (bSaveProfileMode) httpConnection.setRequestProperty("MobudsMode","SET");
                             else httpConnection.setRequestProperty("MobudsMode","GET");
            
            setPosition(1);
            
            dos = httpConnection.openDataOutputStream();
            
            setPosition(2);
            
            // Send a blank bluetooth address if not available
            try {
                s = LocalDevice.getLocalDevice().getBluetoothAddress();
            } catch (Exception e) {
                s="";
            }
            sendField("bluetooth",s);

            if (bSaveProfileMode) {
                sendField("profile",moSystem.persData.myProfile.saveToString(false));
                sendField("alert1",moSystem.persData.sAlertProfile[0]);
                sendField("alert2",moSystem.persData.sAlertProfile[1]);
                sendField("alert3",moSystem.persData.sAlertProfile[2]);
                sendField("picfile",moSystem.persData.strPicFile);            
                sendField("prefs",moSystem.persData.getSettingsAsString());
                sendField("thetime",moSystem.persData.getTheTime()+"");
                setPosition(3);
                // Friends list
                Enumeration eNum = moSystem.persData.myFriends.elements();
                sendField("friends",moSystem.persData.myFriends.size()+"");
                i = 0;
                while (eNum.hasMoreElements()) {
                    StoredUser user = (StoredUser)eNum.nextElement();
                    sendField("friend"+i,moSystem.persData.stringFromUser(user));
                    i++;
                }
                setPosition(4);
                eNum = moSystem.persData.myBlocks.elements();
                sendField("blocks",moSystem.persData.myBlocks.size()+"");
                i = 0;
                while (eNum.hasMoreElements()) {
                    StoredUser user = (StoredUser)eNum.nextElement();
                    sendField("block"+i,moSystem.persData.stringFromUser(user));
                    i++;
                }
                setPosition(5);

                try {
                    int recs[] = moSystem.persData.getAllRecordIds();

                    moSystem.persData.beginDumpingToServer();
                    sendField("adds",recs.length+"");

                    for (int aa=recs.length-1; aa>=0; aa--)
                        sendField("add"+aa,moSystem.persData.getRecordAsBase64(recs[aa]));
                    
                } finally {
                    moSystem.persData.endDumpingToServer();
                }

                setPosition(6);
            }
            setPosition(8);
            
            dos.close();
            dos = null;
                         
            dis = new DataInputStream(httpConnection.openInputStream());
                        
            if (!httpConnection.getHeaderField("UserStatus").equals("OK")) iResult = HTTP_UNKNOWNUSER; else
            if (!httpConnection.getHeaderField("Response").equals("OK")) iResult = HTTP_UNKNOWNERROR; else {                
                // retrieve the response from server
                String responseMessage = "";
                int total = httpConnection.getHeaderFieldInt("Content-length",0);
                int ch;
                setPosition(9);
                
                // faster http read if content-length is set
                if (total==0) {
                    boolean b0 = false;
                    while(( ch = dis.read()) != -1 ) {                        
                        responseMessage+=(char)ch;
                        if (!b0) {
                            setPosition(15);
                            b0 = true;
                        }
                    }
                } else {
                    int iRead = 0;
                    while (iRead<total) {
                        int t2 = total/20;
                        int t3;
                        if (t2<1) t2=1;
                        byte bb[] = new byte[t2];
                        t3 = dis.read(bb);
                        if (t3<0) iRead = total; else {
                            iRead+=t3;
                            responseMessage+=new String(bb,0,t3);
                        }
                        setPosition(10+((iRead*10)/total));
                    }
                }

                // haha, much faster
                setPosition(20);                
                tb = baseconf.getDecodeFields(responseMessage);
                
                // Grab the advert                
                bAdValid = busyWindow.changeAdvertGraphic(baseconf.decode(getResponseString(baseconf.ADVERT_FIELD)));
                setPosition(21);
                
                bUpgradeIsRequired = Integer.parseInt(getResponseString("upgrade"))!=0;
                
                // Should we be reading data back from the stream?
                if (!bSaveProfileMode) {
                    String sPrefs = getResponseString("prefs");
                    moSystem.persData.myProfile.loadFromString(getResponseString("profile"));
                    moSystem.persData.sAlertProfile[0]=getResponseString("alert1");
                    moSystem.persData.sAlertProfile[1]=getResponseString("alert2");
                    moSystem.persData.sAlertProfile[2]=getResponseString("alert3");
                    moSystem.persData.strPicFile=getResponseString("picfile"); 
                    moSystem.persData.getSettingsFromString(sPrefs);
                    setPosition(22);
                    
                    // Friends list
                    Hashtable hashes = new Hashtable();
                    i = Integer.parseInt(getResponseString("friends"));
                    for (a=0; a<i; a++) {
                        try {
                            StoredUser u = moSystem.persData.userFromString(getResponseString("friend"+a));
                            hashes.put(u.sBluetoothAddress,u);
                        } catch (Exception moo1) {};
                    }
                    moSystem.persData.myFriends = hashes;
                    setPosition(23);
                    
                    hashes = new Hashtable();
                    i = Integer.parseInt(getResponseString("blocks"));
                    for (a=0; a<i; a++) {
                        try {
                            StoredUser u = moSystem.persData.userFromString(getResponseString("block"+a));
                            hashes.put(u.sBluetoothAddress,u);
                        } catch (Exception moo2) {};
                    }
                    moSystem.persData.myBlocks = hashes;
                    hashes=null;
                    setPosition(24);

                } else {
                    moSystem.persData.flushDataDump();
                    moSystem.bluetoothAccess.signalRefindAllPeople();
                }
                dis.close();
                dis = null;           
                iResult = HTTP_OK;
            } 

            setPosition(25);
            
        } catch (Exception e) {iResult=HTTP_ERROR;};
        tb = null;
                
        try {dis.close();} catch (Exception e) {}; 
        dis = null;
        try {dos.close();} catch (Exception e) {}; 
        dos = null;
        try {httpConnection.close();} catch (Exception e) {}; 
        httpConnection = null;
        
        Random randGen = new Random();
            
        // Simple delay used when doing an update
        if (bAdValid)
           for (i=0; i<10; i++) {
               setPosition(MAX_CONNECTION_STEPS+i);
                    
                try {
                    synchronized (randGen) {
                        randGen.wait((randGen.nextInt()%300) + 400);
                    }
                } catch (Exception e) {}
           }
        
        return iResult;        
    }    
}
