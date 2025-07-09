/*
 * PersistantData.java
 *
 * Created on 14 December 2006, 20:07
 *
 * Class for managing loading and saving from and to "disk"
 */

package mobuds;

import javax.microedition.rms.*;
import java.util.*;

/**
 *
 * @author rob smith
 */

// All Midp2.0 apps are allowed at least 8k of storage space.
public class PersistantData {
    public final static int MAXIMUM_LICENCE_KEY = 1;   // Where bLastLicenceShown should be at    
    
    public final static int IDEAL_MEMORY_AVAILABLE = 8192;        // how much storage space we actually get
    public final static int SETTING_RECORDSTORE_OVERHEAD = 50;    // this is wasted by each record store, only included on dtatstore size
    
    public final static int SETTINGS_DATASTORE_SIZE = 740;        // estimate of byets used by the settings
    public final static int SETTINGS_MESSAGE_SIZE = 280;          // Estimate of maximum size of storage space for each message
    public final static int SETTING_FRIENDSLIST_SIZE = 90;       // Each friend uses this space    
    public final static int SETTING_MAX_SAVED_MESSAGES = 10;      // Maximum number of messages saved to disk, 
    public final static int SETTINGS_MAX_BUDFOE_COUNT = 20;
    public final static int MIN_SIZE_AVAILABLE = 7000;           // leave ~7k free
    public final static int MAX_LOGGING_RECORDS = 200;          // just over a days worth  (about 51k)  
    
    public final static int RECORDBLOCK_SIZE = 256;
    public final static byte MOBUDSHEADER_ENDOFRECORD = 0;
    public final static byte MOBUDSHEADER_LOCATION_MOHUBID = 1;
    public final static byte MOBUDSHEADER_LOCATION_LATLONG = 2;
    public final static byte MOBUDSHEADER_LOCATION_USERINRANGE = 3;
    public final static byte MOBUDSHEADER_LOCATION_USEROUTRANGE = 4;
    public final static byte MOBUDSHEADER_STARTUP = 5;    // we have just started
    public final static byte MOBUDSHEADER_SHUTDOWN = 6;   // we have just closed (might be missing)
    
    public static int SAVE_PROFILE = 1;
    public static int SAVE_FRIENDS = 2;
    public static int SAVE_BLOCKS  = 4;
          
    public static long SYSTEM_TIME_REMOVED = 1230768000;   // seconds since unix epoc to jan 09
    private static String STORE_PROFILE = "MyProfile";
    private static String STORE_FRIENDS = "MoFriends";
    private static String STORE_BLOCKS =  "MoBlocks";
    private static String STORE_MESSAGES =  "MoMessages";
    private static String STORE_PEOPLELIST = "MyDiscovery";
    
    private static final int MOVEMENT = 20;
    
    public MobudsProfile myProfile;
    public Hashtable myFriends;
    public Hashtable myBlocks;
    public boolean bAllowMessages;
    public boolean bAllowMessagesFromFriends;
    public boolean bAllowPhoneNumber;
    public boolean bAllowPhoto;    
    public boolean bVibrateOnMessage;
    public boolean bFlashOnMessage;
    public boolean bMakeNoiseOnMessage;
    public boolean bDisplayMessage;
    public String strPicFile;   // TODO!   
    public int bLastLicenceShown;             // Current licence number displayed, 0 to 63
    private RecordEnumeration rsRecordList;
    
    // Alerts system - what to be alerted on
    public boolean bAlertFriends;
    public boolean bAlertBlocks;
    public boolean bAlertVibrateOnMessage;
    public boolean bAlertFlashOnMessage;
    public boolean bAlertMakeNoiseOnMessage; 
    public boolean bAlertShowMessage;
    public boolean bEnableAlert[] = new boolean[3];
    public String sAlertProfile[] = new String[3];
    
    // Should recent bluetooth stuff be monitored?
    public boolean bMonitorBluetooth;    
    public boolean bMonitorGPS;
    
    // Local alerts
    public boolean blocAdverts;
    public boolean blocPromos;
    public boolean blocVenNews;
    public boolean blocGenNews;

    public boolean bLiteMode;
    
    public String sUsername;
    public String sPassword;
    
    public boolean bDebugMode = false;
    private boolean badLocation;
    
    private long lastlng,lastlat,lasttime,lastacc;
    
    public int debugModeEnableStep;
    public int currentRecordPos;
    public int currentRecordId;
    private byte[] currentRecord = new byte[RECORDBLOCK_SIZE];
    private byte[] lastLocation = null;  // last location discovered
    
    RecordStore rsDataLogging = null;
    public boolean bIgnoreLoggingForAMinute;
    
    // hidden sequence to show the debug menu
    public void advanceStep(int step) {
        if (step==debugModeEnableStep) debugModeEnableStep++; else debugModeEnableStep=0;
        if (debugModeEnableStep>=8) bDebugMode=true;
    }
    
    
    // Return TRUE if the wizard should run on startup
    public boolean shouldWizardRun() {
        return ((myProfile.strNickName.length()<1)||(myProfile.iYear>=1994)||(myProfile.iYear<1900));
    }
    
    // loads the next record (cycled)
    private void loadNextRecord() {
        // we have two choices, make a new record, or wrap around to an earlier one.
        try {
            if ((rsDataLogging.getSizeAvailable()>MIN_SIZE_AVAILABLE) && (rsRecordList.numRecords()<MAX_LOGGING_RECORDS)) {
                for (int a=0; a<currentRecord.length; a++) currentRecord[a] = MOBUDSHEADER_ENDOFRECORD;
                currentRecordId = rsDataLogging.addRecord(currentRecord, 0, currentRecord.length);
                currentRecordPos=0;
            } else {
                // wrap around to an old one
                int lastPos=-1;
                int nextPos;
                boolean grabNext = false;
                boolean exitNow = false;
                rsRecordList.reset();
                while ((rsRecordList.hasNextElement())&&(!exitNow)) {
                    nextPos = rsRecordList.nextRecordId();
                    if ((lastPos<0)||(grabNext)) lastPos=nextPos;
                    exitNow = grabNext;                    
                    if (nextPos==currentRecordPos) grabNext=true;                    
                }
                currentRecordId = lastPos;
                rsDataLogging.getRecord(currentRecordId, currentRecord, 0);
                currentRecordPos = 0;
            }
        } catch (Exception e) {}
    }
    
    // returns the highest stamp on record
    private void clearRecordSpace(int size) {
        int pos = currentRecordPos;
        int siz = 0;
        
        while (pos<RECORDBLOCK_SIZE) {
                if (currentRecord[pos]==MOBUDSHEADER_ENDOFRECORD) return; // already ok
                switch (currentRecord[pos]) {
                    case MOBUDSHEADER_LOCATION_MOHUBID:siz=9; break; 
                    case MOBUDSHEADER_LOCATION_LATLONG:siz=17; break; 
                    case MOBUDSHEADER_LOCATION_USERINRANGE:siz=13; break; 
                    case MOBUDSHEADER_LOCATION_USEROUTRANGE:siz=13; break;
                    case MOBUDSHEADER_STARTUP:siz=5;break;
                    case MOBUDSHEADER_SHUTDOWN:siz=5;break;
                }
                size-=siz;
                while (siz-->0) currentRecord[pos++]=MOBUDSHEADER_ENDOFRECORD;
                if (size<0) return;  // enough space has been freed
        }    
    }
    
    // we want to save all this crap to mobuds server
    public void beginDumpingToServer() {
        bIgnoreLoggingForAMinute = true;
        logShutdown();
        flushCurrentRecord();
    }
    
    // We dont care anymore
    public void endDumpingToServer() {
        bIgnoreLoggingForAMinute = false;
        try {
            rsDataLogging.getRecord(currentRecordId, currentRecord, 0);
        } catch (Exception e) {};
    }

    // Return one of the records as base64
    public String getRecordAsBase64(int recordId) {
        try {
            rsDataLogging.getRecord(recordId, currentRecord, 0);
        } catch (Exception s) {return "";};
        return baseconf.encode(currentRecord);
    }

    // return the total number of records
    public int[] getAllRecordIds() {
        if (rsRecordList.numRecords()<1) return null;
        int[] ret = new int[rsRecordList.numRecords()];
        int a=0;
        try {
            rsRecordList.reset();
            while (rsRecordList.hasNextElement()) {
                ret[a++]=rsRecordList.nextRecordId();
            }
        } catch (Exception e) {};
        return ret;
    }

    // flush the data dump, all records can be blanked as the server has them
    public void flushDataDump() {
        try {

            int[] ids = getAllRecordIds();
            
            for (int a=0; a<currentRecord.length; a++) currentRecord[a]=MOBUDSHEADER_ENDOFRECORD;

            for (int a=ids.length-1; a>=0; a--) // wipe it
                rsDataLogging.setRecord(ids[a], currentRecord, 0, currentRecord.length);

            currentRecordPos = 0;
            if (rsRecordList.hasNextElement()) currentRecordId = rsRecordList.nextRecordId();  else currentRecordId=-1;

            currentRecordPos=0;
            logStartup();
            logLastLocation();


        } catch (Exception e) {}
    }
    
    // simple, get the time, in seconds
    public static int getTheTime() {
        long i = System.currentTimeMillis() / (long)1000;
        i -= SYSTEM_TIME_REMOVED;
        return (int)i;
    }
    
    // data log some data 
    private void submitNewRecord(byte[] newRecord) {
        if (rsDataLogging==null) return;
        if (rsRecordList==null) return;
        if (bIgnoreLoggingForAMinute) return;

        synchronized(currentRecord) {

            int spaceLeft = RECORDBLOCK_SIZE-(currentRecordPos+1);
            if (spaceLeft<newRecord.length) {
                // pad out the remaining space and flush it
                while (currentRecordPos<RECORDBLOCK_SIZE) currentRecord[currentRecordPos++]=MOBUDSHEADER_ENDOFRECORD;
                flushCurrentRecord();
                loadNextRecord();
                currentRecordPos=0;
            }
            // clear out records until there is room for this one
            clearRecordSpace(newRecord.length);
            // then save it
            for (int a=0; a<newRecord.length; a++) currentRecord[currentRecordPos+a]=newRecord[a];
            currentRecordPos+=newRecord.length;
        }
    }
    
    // flushes the current record to disk
    private void flushCurrentRecord() {
        try {
            if (currentRecordId<0) {
                currentRecordId=rsDataLogging.addRecord(currentRecord, 0, currentRecord.length);
            } else  rsDataLogging.setRecord(currentRecordId, currentRecord, 0, currentRecord.length);
        } catch (Exception e) {};
    }    
    
    // returns the highest stamp on record
    private int findHighestStamp() {
        currentRecordPos=0;
        int lastTime = 0;
        int t;
        
        while (currentRecordPos<RECORDBLOCK_SIZE) {
                if (currentRecord[currentRecordPos]==MOBUDSHEADER_ENDOFRECORD) return lastTime;                
                t =    (currentRecord[currentRecordPos+1]&0xFF) |  ((currentRecord[currentRecordPos+2]&0xFF)<<8)
                            |  ((currentRecord[currentRecordPos+3]&0xFF)<<16)  |  ((currentRecord[currentRecordPos+4]&0xFF)<<24);
                if (t>lastTime) lastTime=t; else return lastTime;   // return if we have gone backwards... unlikely but could happen                
                switch (currentRecord[currentRecordPos]) {
                    case MOBUDSHEADER_LOCATION_MOHUBID:currentRecordPos+=9; break; 
                    case MOBUDSHEADER_LOCATION_LATLONG:currentRecordPos+=17; break; 
                    case MOBUDSHEADER_LOCATION_USERINRANGE:currentRecordPos+=13; break; 
                    case MOBUDSHEADER_LOCATION_USEROUTRANGE:currentRecordPos+=13; break;
                    case MOBUDSHEADER_STARTUP:currentRecordPos+=5; break;
                    case MOBUDSHEADER_SHUTDOWN:currentRecordPos+=5; break;
                }
        }    
        return lastTime;
    }

    // Add an entry to signal startup
    public void logStartup() {
            byte[] block = new byte[5];
            block[0] = MOBUDSHEADER_STARTUP;
            int timeStamp = getTheTime();
            block[1] = (byte)(timeStamp&0xFF);
            block[2] = (byte)((timeStamp>>8)&0xFF);
            block[3] = (byte)((timeStamp>>16)&0xFF);
            block[4] = (byte)((timeStamp>>24)&0xFF);
            submitNewRecord(block);
    }


    // Add an entry to signal shutdown
    public void logShutdown() {
            byte[] block = new byte[5];
            block[0] = MOBUDSHEADER_SHUTDOWN;
            int timeStamp = getTheTime();
            block[1] = (byte)(timeStamp&0xFF);
            block[2] = (byte)((timeStamp>>8)&0xFF);
            block[3] = (byte)((timeStamp>>16)&0xFF);
            block[4] = (byte)((timeStamp>>24)&0xFF);
            submitNewRecord(block);
    }
    
    // trys to reserve as much space as possible for data logging
    private void initDataLogging() {
        try {
            if (rsDataLogging.getNumRecords()<1) {
                int dataSize=0;
                for (int a=0; a<currentRecord.length; a++) currentRecord[a]=MOBUDSHEADER_ENDOFRECORD;

                // keep adding blank records until the disk space is less than what we think we miniminly need
                // or until we have stored way too much! (6k is our minimum other limit)
                try {
                    while ((rsDataLogging.getSizeAvailable()>MIN_SIZE_AVAILABLE)&&(dataSize<6114)) {
                        rsDataLogging.addRecord(currentRecord,0,currentRecord.length);
                        dataSize+=currentRecord.length;
                    }
                } catch (Exception e) {};        
            }
            
            // Now we need to enumerate all records so see where we are/where we left off
            rsRecordList = rsDataLogging.enumerateRecords(null,null,true);
            rsRecordList.reset();
            int highestindex,highestStamp,lastHighest,highestIndexPos;
            lastHighest=-1;
            highestindex=-1;
            highestIndexPos=0;

            
            // Search for record which has the highest timestamp
            while (rsRecordList.hasNextElement()) {
                currentRecordId = rsRecordList.nextRecordId();
                rsDataLogging.getRecord(currentRecordId, currentRecord, 0);
                currentRecordPos=0;
                highestStamp = findHighestStamp();
                if (highestStamp>lastHighest) {
                    highestindex = currentRecordId;
                    lastHighest = highestStamp;
                    highestIndexPos=currentRecordPos;
                }                
            }
            
            // now we know the record with the highest index, and, the position to write to next
            currentRecordPos = highestIndexPos;
            currentRecordId = highestindex;            
            rsDataLogging.getRecord(currentRecordId, currentRecord, 0);
            logStartup();
           
        } catch (Exception ee) {}
    }
   
    // notify a new bluetooth address has been seen
    public void notifyNewBluetoothAddress(String sAddress, boolean bInRange) {
        if (!bMonitorBluetooth) return;
        byte[] block;
        int timeStamp = getTheTime();

        block = new byte[13];
        block[0] = (bInRange?MOBUDSHEADER_LOCATION_USERINRANGE:MOBUDSHEADER_LOCATION_USEROUTRANGE);
        block[1] = (byte)(timeStamp&0xFF);
        block[2] = (byte)((timeStamp>>8)&0xFF);
        block[3] = (byte)((timeStamp>>16)&0xFF);
        block[4] = (byte)((timeStamp>>24)&0xFF);

        while (sAddress.length()<16) sAddress="0"+sAddress;

        block[5]=baseconf.strHexToByte(sAddress.substring(0,2));
        block[6]=baseconf.strHexToByte(sAddress.substring(2,4));
        block[7]=baseconf.strHexToByte(sAddress.substring(4,6));
        block[8]=baseconf.strHexToByte(sAddress.substring(6,8));
        block[9]=baseconf.strHexToByte(sAddress.substring(8,10));
        block[10]=baseconf.strHexToByte(sAddress.substring(10,12));
        block[11]=baseconf.strHexToByte(sAddress.substring(12,14));
        block[12]=baseconf.strHexToByte(sAddress.substring(14,16));

        submitNewRecord(block);
    }
    
    
    // signal a new location
    public void notifyNewLocation(String newLocation, int mohubId) {
        if (!bMonitorGPS) return;

        byte[] block;
        int timeStamp = getTheTime();

        if (newLocation.length()<1) {
            if (!badLocation) {
                 badLocation = true;
                 // room for 68 years
                 block = new byte[9];
                 block[0] = MOBUDSHEADER_LOCATION_MOHUBID;
                 block[1] = (byte)(timeStamp&0xFF);
                 block[2] = (byte)((timeStamp>>8)&0xFF);
                 block[3] = (byte)((timeStamp>>16)&0xFF);
                 block[4] = (byte)((timeStamp>>24)&0xFF);
                 block[5] = 0;
                 block[6] = 0;
                 block[7] = 0;
                 block[8] = 0;
                 lastLocation = block;
                 submitNewRecord(block);
            }
            return;
        }

        badLocation = false;

        clsMohubServer svr = new clsMohubServer();
        svr.publishOtherLocation(newLocation);
        
        if ((lastacc==0)||(((Math.abs(lastlng-svr.lng)>MOVEMENT)||(Math.abs(lastlat-svr.lat)>MOVEMENT))&&(Math.abs(lasttime-svr.lastUse)>=60))) {
            lastlng = svr.lng;
            lastlat = svr.lat;
            lastacc = svr.accuracy;
            lasttime = svr.lastUse;


            if (mohubId>0) {
                 // room for 68 years
                 block = new byte[9];
                 block[0] = MOBUDSHEADER_LOCATION_MOHUBID;
                 block[1] = (byte)(timeStamp&0xFF);
                 block[2] = (byte)((timeStamp>>8)&0xFF);
                 block[3] = (byte)((timeStamp>>16)&0xFF);
                 block[4] = (byte)((timeStamp>>24)&0xFF);
                 block[5] = (byte)(mohubId&0xFF);
                 block[6] = (byte)((mohubId>>8)&0xFF);
                 block[7] = (byte)((mohubId>>16)&0xFF);
                 block[8] = (byte)((mohubId>>24)&0xFF);
            } else {
                 block = new byte[17];
                 block[0] = MOBUDSHEADER_LOCATION_LATLONG;
                 block[1] = (byte)(timeStamp&0xFF);
                 block[2] = (byte)((timeStamp>>8)&0xFF);
                 block[3] = (byte)((timeStamp>>16)&0xFF);
                 block[4] = (byte)((timeStamp>>24)&0xFF);

                 // latitude/longitude goes from -90/90 and -180/180
                 block[5] = (byte)(lastlat&0xFF);
                 block[6] = (byte)((lastlat>>8)&0xFF);
                 block[7] = (byte)((lastlat>>16)&0xFF);
                 block[8] = (byte)((lastlat>>24)&0xFF);

                 block[9] = (byte)(lastlng&0xFF);
                 block[10] = (byte)((lastlng>>8)&0xFF);
                 block[11] = (byte)((lastlng>>16)&0xFF);
                 block[12] = (byte)((lastlng>>24)&0xFF);

                 block[13] = (byte)(lastacc&0xFF);
                 block[14] = (byte)((lastacc>>8)&0xFF);
                 block[15] = (byte)((lastacc>>16)&0xFF);
                 block[16] = (byte)((lastacc>>24)&0xFF);                     
            }

            lastLocation = block;
            submitNewRecord(block);
        }
    }

    // re-dubmit the last location, but with the date stamp now
    public void logLastLocation() {
        if (lastLocation!=null) {
               int timeStamp = getTheTime();
               lastLocation[1] = (byte)(timeStamp&0xFF);
               lastLocation[2] = (byte)((timeStamp>>8)&0xFF);
               lastLocation[3] = (byte)((timeStamp>>16)&0xFF);
               lastLocation[4] = (byte)((timeStamp>>24)&0xFF);
               submitNewRecord(lastLocation);
        }
    }
        
    // Load messages from disk
    public Vector loadMessagesFromDisk() {
        RecordStore rs = null; 
        Vector res = new Vector();
        
        synchronized(this) {
            try {   
               rs = RecordStore.openRecordStore(STORE_MESSAGES,true);

                for (int i=1; i<=rs.getNumRecords(); i++) {
                    byte b[] = rs.getRecord(i);  // get the first record
                    PersonMessage pm = new PersonMessage();
                    pm.fromSaving(new String(b,0,b.length));
                    pm.sSourceProfile = unstipProfileUp(pm.sSourceProfile);
                    pm.bMessageType = baseconf.COMMUNICATION_SENDMESSAGE;
                    pm.sBluetoothTo = ""; // us
                    res.addElement(pm);
               } 
            } catch (Exception e) {}      
        }
        
        try { rs.closeRecordStore(); } catch (Exception e) {};
               
        return res;
    }
    
    // Save messages back out
    public void saveMessagesToDisk(Vector messages) {
        RecordStore rs = null; 
      
        synchronized(this) {
            try {RecordStore.deleteRecordStore(STORE_MESSAGES);} catch (Exception e) {};
            try {   
               rs = RecordStore.openRecordStore(STORE_MESSAGES,true,RecordStore.AUTHMODE_ANY,true);
               String s;
               String backup;
            
               for (int i=0; i<messages.size(); i++) {
                   PersonMessage pm = (PersonMessage)messages.elementAt(i);
                   backup = pm.sSourceProfile;
                   pm.sSourceProfile = stipProfileDown(pm.sSourceProfile);
                   s = pm.forSaving();
                   pm.sSourceProfile = backup;
                   rs.addRecord(s.getBytes(),0,s.length());
               }
               
            } catch (Exception e) {}   
            
            try { rs.closeRecordStore(); } catch (Exception e) {};

        }
    }
    
    // update the friends and blocks list
    public void updateFriendsBlocks(Hashtable userList) {
           StoredUser u;
           DiscoveredUsers du;
           Enumeration eNum;
           Date d = new Date();
           
           synchronized (myProfile) {        
               // Friends list
               eNum = myFriends.elements();
               while (eNum.hasMoreElements()) {
                     u = (StoredUser)eNum.nextElement();
                     du = (DiscoveredUsers)userList.get(u.sBluetoothAddress);
                     if (du!=null) u.sProfile = du.userProfile;
               }
           
               // Blocks list
               eNum = myBlocks.elements();
               while (eNum.hasMoreElements()) {
                     u = (StoredUser)eNum.nextElement();
                     du = (DiscoveredUsers)userList.get(u.sBluetoothAddress);
                     if (du!=null) u.sProfile = du.userProfile;
               }
           }
    }
        
    // Load in a user from a string
    public StoredUser userFromString(String s) {
        StoredUser sUser = new StoredUser();
        int iPos = s.indexOf(",");
        
        sUser.sBluetoothAddress = s.substring(0,iPos);
        sUser.sProfile = s.substring(iPos+1);
        
        return sUser;
    }
    
    // Save a user into a string
    public String stringFromUser(StoredUser u) {
        return u.sBluetoothAddress+","+u.sProfile;
    }
    
    // Remove user from friends list
    public void delUserFromFriends(String bluetoothAddress) {
            synchronized (myProfile) {
                myFriends.remove(bluetoothAddress);
            }
            bSaveUserData(SAVE_FRIENDS);
    }
    
    // Remove user from blocked list
    public void delUserFromBlocks(String bluetoothAddress) {
            synchronized (myProfile) {
                myBlocks.remove(bluetoothAddress);
            }
            bSaveUserData(SAVE_BLOCKS);
    }
    
    // Reverses some of the damage from below
    public String unstipProfileUp(String sProfile) {
        MobudsProfile mp = new MobudsProfile();;
        mp.loadFromString(sProfile,true);
        return mp.saveToString(true);    
    }
    
    // Strip down what is actually stored from the profile
    public String stipProfileDown(String sProfile) {
        MobudsProfile mp = new MobudsProfile();;
        mp.loadFromString(sProfile);
        mp.strURL="";
        mp.strDescription="";
        mp.lVisibility&=~(mp.VISIBLE_URL|mp.VISIBLE_MESSAGE);
        return mp.saveToString(true,true);
    }
    
    // Add a user to the friends list
    public boolean addUserToFriends(String bluetoothAddress, String sProfile) {
        if (!isFriend(bluetoothAddress)) {
            int iMask = SAVE_FRIENDS;
            
            
            Date d = new Date();
            StoredUser u = new StoredUser();
            u.sBluetoothAddress = bluetoothAddress;
            u.sProfile = sProfile;
            
            synchronized (myProfile) {
                if (myBlocks.remove(bluetoothAddress)!=null) iMask|=SAVE_BLOCKS;

                if (myFriends.size()+myBlocks.size()>=SETTINGS_MAX_BUDFOE_COUNT) return false;
                
                myFriends.put(bluetoothAddress,u); 
            }
            bSaveUserData(iMask);           
            
            return true;
        } else return true;
    }

    // Add a user to the block list
    public boolean addUserToBlockList(String bluetoothAddress, String sProfile) {
        if (!isBlocked(bluetoothAddress)) {
            int iMask = SAVE_BLOCKS;
            Date d = new Date();
            StoredUser u = new StoredUser();
            u.sBluetoothAddress = bluetoothAddress;
            u.sProfile = sProfile;
            
            synchronized (myProfile) {
                if (myFriends.remove(bluetoothAddress)!=null) iMask|=SAVE_FRIENDS;

                if (myFriends.size()+myBlocks.size()>=SETTINGS_MAX_BUDFOE_COUNT) return false;

                myBlocks.put(bluetoothAddress,u); 
                
            }
            bSaveUserData(iMask);           
            
            return true;
        } else return true;
    }
        
    // Is the bluetooth address a friend?
    public boolean isFriend(String bluetoothAddress) {
        return myFriends.containsKey(bluetoothAddress);
    }
    
    // Is the bluetooth user blocked?
    public boolean isBlocked(String bluetoothAddress) {
        return myBlocks.containsKey(bluetoothAddress);
    }
    
    // Creates a new instance of PersistantData
    public PersistantData() {
        myProfile = new MobudsProfile();
        myFriends = new Hashtable();
        myBlocks = new Hashtable();
        
        try {
            rsRecordList = null;
            rsDataLogging = RecordStore.openRecordStore(STORE_PEOPLELIST,true,RecordStore.AUTHMODE_ANY,true);
            initDataLogging();
        } catch (Exception e1) {
            rsDataLogging = null;
        }
    }
    
    public void closeStorage() {
        try {
            logShutdown();
            flushCurrentRecord();
            rsRecordList = null;
            if (rsDataLogging!=null) rsDataLogging.closeRecordStore();
        } catch (Exception f1) {};
    }
    
    // Save the data at the top from persistant storage
    public boolean bSaveUserData(int maskToSave)  {
        RecordStore rs = null; 
        String sProfile;
        byte b[] = new byte[5];
        String sTemp;
        
        synchronized (myProfile) {            
            // Save OUR profile
            if ((maskToSave&SAVE_PROFILE)!=0) {
                // Inctrease the version counter=0x3FFFF;

                try {RecordStore.deleteRecordStore(STORE_PROFILE);} catch (Exception e) {};
                try {   
                   rs = RecordStore.openRecordStore(STORE_PROFILE,true,RecordStore.AUTHMODE_ANY,true);

                   // Record 1
                   sProfile = myProfile.saveToString(false);
                   rs.addRecord(sProfile.getBytes(),0,sProfile.length());

                   b[0]=0;
                   b[0]|=bAllowMessages?1:0;
                   b[0]|=bAllowMessagesFromFriends?2:0;
                   b[0]|=bAllowPhoneNumber?4:0;
                   b[0]|=bAllowPhoto?8:0;
                   b[0]|=bVibrateOnMessage?16:0;
                   b[0]|=bFlashOnMessage?32:0;
                   b[0]|=bMakeNoiseOnMessage?64:0;
                   b[0]|=bDisplayMessage?128:0;

                   b[1]=0;
                   b[1]|=bAlertFriends?1:0;
                   b[1]|=bAlertBlocks?2:0;
                   b[1]|=bAlertVibrateOnMessage?4:0;
                   b[1]|=bAlertFlashOnMessage?8:0;
                   b[1]|=bAlertMakeNoiseOnMessage?16:0;
                   b[1]|=bEnableAlert[0]?32:0;
                   b[1]|=bEnableAlert[1]?64:0;
                   b[1]|=bEnableAlert[2]?128:0;
                   
                   b[2] = 0;
                   b[2]|=bAlertShowMessage?1:0;
                   b[2]|=bMonitorBluetooth?2:0;
                   b[2]|=bMonitorGPS?4:0;                   
                   b[2]|=blocAdverts?8:0;
                   b[2]|=blocPromos?16:0;
                   b[2]|=blocVenNews?32:0;
                   b[2]|=blocGenNews?64:0;
                   b[2]|=bLiteMode?128:0;
                   
                   b[3]|=bLastLicenceShown;

                   b[4] = 0;
                   //b[4]|=bUseTransitions?1:0;
                   //b[4]|=bUseSmallFont?2:0;
                   b[4]|=bDebugMode?4:0;
                   
                   // Record 2
                   rs.addRecord(b,0,b.length);
                                      
                   // Record 3
                   rs.addRecord(strPicFile.getBytes(),0,strPicFile.length());
               
                   // Record 4-6
                   for (int i=0; i<=2; i++) rs.addRecord(sAlertProfile[i].getBytes(),0,sAlertProfile[i].length());
                   
                   // Record 7
                   rs.addRecord(sUsername.getBytes(),0,sUsername.length());
                   
                   // Record 8
                   rs.addRecord(sPassword.getBytes(),0,sPassword.length());

                   rs.closeRecordStore();
                } catch (Exception e) {}     
            }
            try { rs.closeRecordStore(); } catch (Exception e) {};


            // Save out our friends list
            if ((maskToSave&SAVE_FRIENDS)!=0)
            {
                try {RecordStore.deleteRecordStore(STORE_FRIENDS);} catch (Exception e) {};
                try {               
                   rs = RecordStore.openRecordStore(STORE_FRIENDS,true,RecordStore.AUTHMODE_ANY,true);
                   Enumeration eNum = myFriends.elements();
                   while (eNum.hasMoreElements()) {
                       StoredUser user = (StoredUser)eNum.nextElement();
                       sTemp = user.sProfile;
                       user.sProfile = stipProfileDown(user.sProfile);
                       String sUser = stringFromUser(user);
                       user.sProfile = sTemp;
                       rs.addRecord(sUser.getBytes(),0,sUser.length());
                   }
                   rs.closeRecordStore();
                } catch (Exception e) {}
            }
            try { rs.closeRecordStore(); } catch (Exception e) {};


            // Save out our blocked list
            if ((maskToSave&SAVE_BLOCKS)!=0)
            {
                try {RecordStore.deleteRecordStore(STORE_BLOCKS);} catch (Exception e) {};
                try {       
                   rs = RecordStore.openRecordStore(STORE_BLOCKS,true,RecordStore.AUTHMODE_ANY,true);
                   Enumeration eNum = myBlocks.elements();
                   while (eNum.hasMoreElements()) {
                       StoredUser user = (StoredUser)eNum.nextElement();

                       sTemp = user.sProfile;
                       user.sProfile = stipProfileDown(user.sProfile);
                       String sUser = stringFromUser(user);
                       user.sProfile = sTemp;
                       
                       rs.addRecord(sUser.getBytes(),0,sUser.length());
                   }
                   rs.closeRecordStore();
                } catch (Exception e) {}             
            }
            try { rs.closeRecordStore(); } catch (Exception e) {};

        }
        
        return true;
    }
    
    // Load the data at the top from persistant storage
    public boolean bLoadUserData() {
        RecordStore rs = null;
        
        bAllowMessages = true;
        bAllowMessagesFromFriends = false;
        bVibrateOnMessage = true;
        bFlashOnMessage = true;
        bMakeNoiseOnMessage = true;
        bAllowPhoneNumber = false;
        bAllowPhoto = false;  
        bAlertFriends = true;
        bAlertBlocks = false;
        bAlertVibrateOnMessage = true;
        bAlertFlashOnMessage = true;
        bAlertMakeNoiseOnMessage = true;
        bDisplayMessage = false;
        bEnableAlert[0] = false;
        bEnableAlert[1] = false;
        bEnableAlert[2] = false;
        bMonitorBluetooth = true;
        sAlertProfile[0] = "";
        sAlertProfile[1] = "";
        sAlertProfile[2] = "";
        strPicFile = "";
        sUsername="";
        sPassword="";
        bLastLicenceShown=0;   
        bMonitorGPS = true;
        blocAdverts = true;
        blocPromos = true;
        blocVenNews = true;
        bLiteMode = true;
        blocGenNews = true;
        bDebugMode = baseconf.ISDEBUG;
        
        synchronized (myProfile) {            
            // Load in our profile from disk           
            try {
               rs = RecordStore.openRecordStore(STORE_PROFILE,true);
               if (rs.getNumRecords()>0)
               {    byte b[] = rs.getRecord(1);  // get the first record
                    if (b!=null)
                        if (!myProfile.loadFromString(new String(b,0,b.length))) myProfile = new MobudsProfile();
               }
               // load in the rest of the settings
               if (rs.getNumRecords()>1)
               {
                   byte b[] = rs.getRecord(2);  // get the first record
                   bAllowMessages           = (b[0]&1)!=0;
                   bAllowMessagesFromFriends= (b[0]&2)!=0;
                   bAllowPhoneNumber        = (b[0]&4)!=0;
                   bAllowPhoto              = (b[0]&8)!=0;
                   bVibrateOnMessage        = (b[0]&16)!=0;
                   bFlashOnMessage          = (b[0]&32)!=0;
                   bMakeNoiseOnMessage      = (b[0]&64)!=0;
                   bDisplayMessage          = (b[0]&128)!=0;

                   if (b.length>1) {
                       bAlertFriends =(b[1]&1)!=0;
                       bAlertBlocks =(b[1]&2)!=0;
                       bAlertVibrateOnMessage =(b[1]&4)!=0;
                       bAlertFlashOnMessage =(b[1]&8)!=0;
                       bAlertMakeNoiseOnMessage =(b[1]&16)!=0;
                       bEnableAlert[0] =(b[1]&32)!=0;
                       bEnableAlert[1] =(b[1]&64)!=0;
                       bEnableAlert[2] =(b[1]&128)!=0;

                       if (b.length>2) {
                            bAlertShowMessage = (b[2]&1)!=0;
                            bMonitorBluetooth = (b[2]&2)!=0;
                            bMonitorGPS       = (b[2]&4)!=0;                            
                            blocAdverts = (b[2]&8)!=0;
                            blocPromos = (b[2]&16)!=0;
                            blocVenNews = (b[2]&32)!=0;
                            blocGenNews = (b[2]&64)!=0;
                            bLiteMode = (b[2]&128)!=0;
                            
                            if (b.length>3) bLastLicenceShown = b[3];

                            if (b.length>4) {
                                bDebugMode=(b[4]&4)!=0;
                                //bUseTransitions = (b[4]&1)!=0;
                             //   bUseSmallFont = (b[4]&2)!=0;
                            }
                       }
                   }
               }
                                  
               // load in the alert settings
               if (rs.getNumRecords()>2)
               {
                   byte b[] = rs.getRecord(3);  // get the first record
                   if (b!=null) strPicFile = new String(b,0,b.length);
               }
               
               // Record 4-6
               for (int i=0; i<=2; i++) 
                    if (rs.getNumRecords()>3+i) {
                        byte b[] = rs.getRecord(4+i);
                        if (b!=null) sAlertProfile[i] = new String(b,0,b.length);
                    } else sAlertProfile[i]="";
               
               // load in the username
               if (rs.getNumRecords()>6)
               {
                   byte b[] = rs.getRecord(7);  // get the first record
                   if (b!=null) sUsername = new String(b,0,b.length);
               }               

               // load in the password
               if (rs.getNumRecords()>7)
               {
                   byte b[] = rs.getRecord(8);  // get the first record
                   if (b!=null) sPassword = new String(b,0,b.length);
               }               
               
               rs.closeRecordStore();
            } catch (Exception e)
            {   // On error, reset the profile               
            }
            try {rs.closeRecordStore();} catch (Exception ee) {};

            // Load in our friends list
            try {
                myFriends.clear(); 
                rs = RecordStore.openRecordStore(STORE_FRIENDS,true);
                for (int a=1; a<=rs.getNumRecords(); a++)
                {     byte b[] = rs.getRecord(a);  // get the first record
                      StoredUser u = userFromString(new String(b,0,b.length));
                      u.sProfile = unstipProfileUp(u.sProfile);
                      myFriends.put(u.sBluetoothAddress,u);
                }
                rs.closeRecordStore();
            } catch (Exception e) {}   
            try {rs.closeRecordStore();} catch (Exception ee) {};

            // Load in our blocked list
            try {
               myBlocks.clear();
               rs = RecordStore.openRecordStore(STORE_BLOCKS,true);
               for (int a=1; a<=rs.getNumRecords(); a++)
                {     byte b[] = rs.getRecord(a);  // get the first record
                      StoredUser u = userFromString(new String(b,0,b.length));
                      u.sProfile = unstipProfileUp(u.sProfile);
                      myBlocks.put(u.sBluetoothAddress,u);
               }
               rs.closeRecordStore();
            } catch (Exception e) {}   
            try {rs.closeRecordStore();} catch (Exception ee) {};
        }
        
        return true;
    }
    
    // Returns TRUE if the character is the number '1
    private static boolean isOne(char c) {
        return c == '1';
    }
    
    // Apply settings FROM a string of 1 and 0
    public void getSettingsFromString(String sPrefs) {
        if (sPrefs.length()>0) bAllowMessages=isOne(sPrefs.charAt(0));
        if (sPrefs.length()>1) bAllowMessagesFromFriends=isOne(sPrefs.charAt(1));
        if (sPrefs.length()>2) bAllowPhoneNumber=isOne(sPrefs.charAt(2));
        if (sPrefs.length()>3) bAllowPhoto=isOne(sPrefs.charAt(3));
        if (sPrefs.length()>4) bVibrateOnMessage=isOne(sPrefs.charAt(4));
        if (sPrefs.length()>5) bFlashOnMessage=isOne(sPrefs.charAt(5));
        if (sPrefs.length()>6) bMakeNoiseOnMessage=isOne(sPrefs.charAt(6));
        if (sPrefs.length()>7) bDisplayMessage=isOne(sPrefs.charAt(7));
        if (sPrefs.length()>8) bAlertFriends=isOne(sPrefs.charAt(8));
        if (sPrefs.length()>9) bAlertBlocks=isOne(sPrefs.charAt(9));
        if (sPrefs.length()>10) bAlertVibrateOnMessage=isOne(sPrefs.charAt(10));
        if (sPrefs.length()>11) bAlertFlashOnMessage=isOne(sPrefs.charAt(11));
        if (sPrefs.length()>12) bAlertMakeNoiseOnMessage=isOne(sPrefs.charAt(12));
        if (sPrefs.length()>13) bAlertShowMessage=isOne(sPrefs.charAt(13));
        if (sPrefs.length()>14) bMonitorBluetooth=isOne(sPrefs.charAt(14));
        if (sPrefs.length()>15) bEnableAlert[0]=isOne(sPrefs.charAt(15));
        if (sPrefs.length()>16) bEnableAlert[1]=isOne(sPrefs.charAt(16));
        if (sPrefs.length()>17) bEnableAlert[2]=isOne(sPrefs.charAt(17));
        if (sPrefs.length()>18) bLiteMode=isOne(sPrefs.charAt(18));
    }
    
    // Convert bool to string
    private char bool2str(boolean b) {
        return (b?'1':'0');
    }
    
    // Get settings as a string of 1 and 0
    public String getSettingsAsString() {
        String sPrefs = "";
        sPrefs+=bool2str(bAllowMessages);
        sPrefs+=bool2str(bAllowMessagesFromFriends);
        sPrefs+=bool2str(bAllowPhoneNumber);
        sPrefs+=bool2str(bAllowPhoto);
        sPrefs+=bool2str(bVibrateOnMessage);
        sPrefs+=bool2str(bFlashOnMessage);
        sPrefs+=bool2str(bMakeNoiseOnMessage);
        sPrefs+=bool2str(bDisplayMessage);
        sPrefs+=bool2str(bAlertFriends);
        sPrefs+=bool2str(bAlertBlocks);
        sPrefs+=bool2str(bAlertVibrateOnMessage);
        sPrefs+=bool2str(bAlertFlashOnMessage);
        sPrefs+=bool2str(bAlertMakeNoiseOnMessage);
        sPrefs+=bool2str(bAlertShowMessage);
        sPrefs+=bool2str(bMonitorBluetooth);
        sPrefs+=bool2str(bEnableAlert[0]);
        sPrefs+=bool2str(bEnableAlert[1]);
        sPrefs+=bool2str(bEnableAlert[2]);
        sPrefs+=bool2str(bLiteMode);
        
        return sPrefs;
    }    
}
