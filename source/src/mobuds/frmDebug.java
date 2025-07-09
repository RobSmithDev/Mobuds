/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobuds;

import javax.microedition.rms.*;
import com.sun.lwuit.*;

/**
 *
 * @author rob smith
 */
public class frmDebug extends ModalForm  {
    
    private int nw = 60;

    
    private void refreshDebug() {
        if (baseconf.ISDEBUG) {
            try {
                startNewSection("Debug Events");
                for (int a=0; a<moSystem.debug.length; a++) addInfoLine((a+1)+". "+moSystem.debug[a]+"\r\n");
            } catch (Exception ff) {}
        }
        try {
            startNewSection("Recent Errors");
            for (int a=0; a<moSystem.errors.length; a++) addInfoLine((a+1)+". "+moSystem.errors[a]);
        } catch (Exception ff) {}
        startNewSection("System");
        try {
            addPair("Free Memory:",new Label(Runtime.getRuntime().freeMemory()+""),nw);
            addPair("Total Memory:",new Label(Runtime.getRuntime().totalMemory()+""),nw);
            addPair("Next Scan:",new Label(moSystem.TimeTillNextScan+"s"),nw);
        } catch (Exception jj) {}
        RecordStore rs;
        try {
            rs=RecordStore.openRecordStore("Space",true);
            addPair("RMS Free:",new com.sun.lwuit.Label(rs.getSizeAvailable()+""),nw);
            addPair("Tracking Blocks:",new Label(moSystem.persData.getAllRecordIds().length+""),nw);
            rs.closeRecordStore();
            RecordStore.deleteRecordStore("Space");
        } catch (Exception e) {}
        rs=null;
        clsMohubServer mh;
        try {
            mh = moSystem.bluetoothAccess.LocationFinder.getLastLocation();
            startNewSection("JSR179 Location");
            addPair("Supported:",new com.sun.lwuit.Label(moSystem.bluetoothAccess.LocationFinder.isAvailable?"Yes":"No"),nw);
            if (mh!=null) {
                addPair("Lat:",new com.sun.lwuit.Label(mh.lat+""),nw);            
                addPair("Lng:",new com.sun.lwuit.Label(mh.lng+""),nw);            
                addPair("Accuracy:",new com.sun.lwuit.Label(mh.accuracy+""),nw);            
                addPair("Time:",new com.sun.lwuit.Label((System.currentTimeMillis()/1000)-mh.lastUse+" ms"),nw);
            }  else {
                addPair("Status:",new com.sun.lwuit.Label("No position yet"),nw);                        
            }     
        } catch (Exception h) {}
        try {
            mh=moSystem.bluetoothAccess.getLocation();
            startNewSection("Used Location");
            if (mh!=null) {
                addPair("Lat:",new com.sun.lwuit.Label(mh.lat+""),nw);            
                addPair("Lng:",new com.sun.lwuit.Label(mh.lng+""),nw);            
                addPair("Accuracy:",new com.sun.lwuit.Label(mh.accuracy+""),nw);            
                addPair("Time:",new com.sun.lwuit.Label((System.currentTimeMillis()/1000)-mh.lastUse+" ms"),nw);
            }  else {
                addPair("Status:",new com.sun.lwuit.Label("No position yet"),nw);                        
            }     
        } catch (Exception gg) {}

        startNewSection("Discovery State");
        addPair("Total Scans:",new com.sun.lwuit.Label(moSystem.bluetoothAccess.scanCount+""),nw);
        String str="Unknown";

        switch (moSystem.bluetoothAccess.pauseStatus) {
            case 0:str = "0:Init"; break;
            case 1:str = "1:Discovery"; break;
            case 2:str = "2:Screen Refresh"; break;
            case 3:str = "3:Alert Check"; break;
            case 4:str = "4:Force GIAC"; break;
            case 5:str = "5:SystemGC"; break;
            case 6:str = "6:Pauser"; break;
            case 7:str = "7:MessageQueue"; break;
        }
        addPair("Scan Thread State:",new com.sun.lwuit.Label(str),nw);
        str="Unknown";
        switch (moSystem.bluetoothAccess.scanStatus) {
            case 0:str = "0:Init"; break;
            case 1:str = "1:starting"; break;
            case 2:str = "2:Issue scan"; break;
            case 3:str = "3:scan Completed"; break;
            case 4:str = "4:Scan aborted"; break;
            case 5:str = "5:DeviceList=NULL"; break;
            case 6:str = "6:Aborted #2"; break;
            case 7:str = "7:Looked@devices"; break;

            case 8:str = "8:Looked"; break;
            case 9:str = "9:Location Done"; break;
            case 10:str = "10:Copy list"; break;
            case 11:str = "11:Updating"; break;
            case 12:str = "12:Deleting"; break;
            case 14:str = "14:Saving"; break;
            case 15:str = "15:Publishing"; break;
            case 16:str = "16:Flushing"; break;
            case 17:str = "17:Complete"; break;
            case 100:str = "100:LoopError"; break;
            case 199:str = "199:InquiryComplete1"; break;
            case 200:str = "200:ScanCompleteNotifyFailed"; break;
        }
        addPair("Discovery State:",new com.sun.lwuit.Label(str),nw);
        

    }

    
    public frmDebug(MobudsSystem moSys) {
        super(moSys,"Debug");
        refreshDebug();
        addModalCommand("Refresh",moSystem.imgCross,MR_CANCEL,CMD_NONE);
        addModalCommand("Close",moSystem.imgTick,MR_OK,CMD_BACK);        
    }
}
