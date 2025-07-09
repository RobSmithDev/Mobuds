/*
 * BTManager.java
 *
 * Created on 09 December 2006, 22:01
 *
 * Bluetooth Mobuds interface
 */

package mobuds;


//import com.sun.lwuit.*;

///#define L2CAPMODE

// jsr082 API
import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.DeviceClass;
//#ifdef L2CAPMODE
//# import javax.bluetooth.L2CAPConnection;
//# import javax.bluetooth.L2CAPConnectionNotifier;
//#else
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
//#endif
import java.util.*;
import javax.bluetooth.UUID;


// midp/cldc API
import java.util.Vector;
import javax.microedition.io.Connector;
import java.util.Hashtable;
/*
 *
 * @author rob smith
 */
public class BTManager implements Runnable, DiscoveryListener  {

    // Connection string
    public String sOurConnectionURL = "";

    /* Keeps the local device reference. */
    private LocalDevice bluetoothService;

    /* location lookup */
    clsLocationBase LocationFinder;

    /* Accepts new connections. */
//#ifdef L2CAPMODE
//#     private L2CAPConnectionNotifier newConnNotify;
//#else
    private StreamConnectionNotifier newConnNotify;
//#endif

    /* Keeps the information about this server. */
    private ServiceRecord serviceInfo;

    /* if the master/slave switch is allowed */
    private boolean masterSlaveSwitch;

    /* Quick access to the main midlet if needed */
    private MobudsSystem moSystem;

    /* Keeps the discovery agent reference. */
    private DiscoveryAgent bluetoothDiscovery;

    // Number of completed scans
    public int scanCount = 0;

    // Position within the scan progress
    public int scanStatus = 0;

    // Position in teh pause loop
    public int pauseStatus = 0;

    // Is Closed?
    private boolean bIsClosed;

    // Is a mohub around?
    private clsMohubServer mohubServer;

    /* Bluetooth started ok */
    private boolean boolStartedOK;

    /* Cached list of UUID things we want */
    private UUID[] cacheUUID;

    /* Cached list of the attributes we want */
    private int[] cacheAttribs;

    // Message Thread holder!
    private BTConnectionHandler connectionHandler;

    // Message queue for sending messages to other people
    private BTMessageSender messageQueue;

    /* Creates a discovery thread */
    private final Thread discoveryThread = new Thread(this);

    /* Device list or DiscoveryDevice() */
    private Vector deviceList;

    // Used for fast access to the device list during service discovery
    private int iCurrentDeviceNumber;

    /* list of users to be updates */
    private Hashtable userList;
    private Hashtable messagesToIgnore;  // details about messsages we have already received and should ignore
    private Hashtable newList;   // new userlist so the lock on this is shorter
    private Vector vNewUsers;
    private Hashtable nonMobudsUsers = new Hashtable();  // list of other bluetooth addresses not runnign mobuds

    private int newNumberOfMatchedUsers,newNumberOfFriends,newNumberOfBlocks;

    // Inbox of messages
    private Vector inBox;

    // Signal the profile has changed
    private boolean bProfileChanged;

    // Useful!
    private Vector devicesNotRunningMobuds;

    // Allow alerts to be checked
    private boolean allowAlertsCheck = false;

    // What type of service are we
    public final static int  SERVICE_OBJECT_TRANSFER = 0x100000;
    public final static int  SERVICE_TELEPHONY = 0x400000;

    // Full long service name for this service
    public static final String myServiceUUID    = "10819CA4FBAF4F8EA0259BA165599A04";

    // Describes this server.
    public static final UUID TALKEASI_SERVER_UUID = new UUID(myServiceUUID, false);

    // The attribute id of the record item containing US
    public static final int DIRECTORY_ATTRIBUTE     = 0xE01F;

    // set to true if something changes
    private boolean setChangedFlag = false;

    private final Object sync_NotRunningMobudsSync = new Object();
    private final Object sync_BTDiscovery = new Object();
    private final Object sync_UserListSaver = new Object();
    private final Object sync_Inbox = new Object();

    // returns true if the list has changed and resets the flag
    public boolean hasUserlistChanged() {
        if (setChangedFlag) {
            setChangedFlag = false;
            return true;
        }
        return false;
    }

    // Gets OUR bluetooth address
    public String getBluetoothAddress() {
        return bluetoothService.getBluetoothAddress();
    }

    // Causes a bluetooth scan to occur but just of the people who ARE NOT running mobuds!
    public Vector scanForUnknownUsers() {
        devicesNotRunningMobuds = new Vector();
        synchronized(sync_NotRunningMobudsSync) {
            try {
                sync_NotRunningMobudsSync.wait();    // wait for it to happen
            } catch (Exception e) {
                moSystem.logErrorMessage("scanForUnknownUsers:"+e.toString());
                devicesNotRunningMobuds = null;
            }
        }
        Vector v = devicesNotRunningMobuds;
        devicesNotRunningMobuds = null;
        return v;
    }


    // Tell a friend
    public int tellAFriend(RemoteDevice dev) {
        clsTellFriend teller = new clsTellFriend(moSystem,bluetoothDiscovery,dev);
        int a = teller.run("1106");
        if (a!=0) a = teller.run("1105");
        return a;
    }

    // Are there messages in the inbox
    public boolean isMessages() {
        return (!inBox.isEmpty());
    }

    public clsMohubServer getLocation() {
        if (mohubServer!=null) return mohubServer; else return LocationFinder.getLastLocation();
    }

    // returns the location string for where we are or may be
    public String getLocationString() {
        clsMohubServer loc = getLocation();
        if (loc==null) return "";
        if (loc.accuracy>=0xfffff) return "";
        return baseconf.longToMyBase64(loc.lat)+","+baseconf.longToMyBase64(loc.lng)+","+baseconf.longToMyBase64(loc.accuracy)+","+(loc.mohubID>0?"1":"0")+baseconf.longToMyBase64(loc.lastUse);
    }

    // go through the user list and add them back to the location finder
    public void signalRefindAllPeople() {

        // For public use of the list
        synchronized (sync_UserListSaver) {
            Enumeration eNum = userList.elements();
            while (eNum.hasMoreElements()) {
                moSystem.persData.notifyNewBluetoothAddress(((DiscoveredUsers)eNum.nextElement()).bluetoothAddress, true);
            }
        }
    }

    // Save new inbox
    public void saveNewInbox(Vector vec) {
        inBox = vec;
        notifyMessageQueueSizeChange();
    }

    // Get a copy of the inbox
    public Vector getInboxCopy() {
        Vector v = new Vector();
        int i;

        synchronized (sync_Inbox) {
            for (i=0; i<inBox.size(); i++)
                v.addElement(inBox.elementAt(i));
        }

        return v;
    }

    // Get a copy of the inbox
    public Vector getOutboxCopy() {
        Vector v = new Vector();
        int i;

        synchronized (messageQueue.sync_messageQueue) {
            for (i=0; i<messageQueue.messageQueue.size(); i++)
                v.addElement(messageQueue.messageQueue.elementAt(i));
            for (i=0; i<messageQueue.outboxDelayQueue.size(); i++)
                v.addElement(messageQueue.outboxDelayQueue.elementAt(i));
        }

        return v;
    }

    // Signal saving messages to disk.
    public void saveMessagesToDisk() {
        moSystem.persData.saveMessagesToDisk(getInboxCopy());
        notifyMessageQueueSizeChange();
    }

    // Load messages
    private void loadMessagesFromDisk() {
        Vector messages = moSystem.persData.loadMessagesFromDisk();

        synchronized(sync_Inbox) {
            inBox = messages;
        }
    }

    // Removes a message from the list
    public void removeMessage(Object tm) {
        synchronized (sync_Inbox) {
            inBox.removeElement(tm);
        }

        // Save all messages to disk
        saveMessagesToDisk();
    }

    // Did bluetooth start ok?
    public boolean isOK() {
        return boolStartedOK;
    }

    // Signal that the profile has changed and so needs to be saved
    public void signalProfileChanged() {
        bProfileChanged = true;
    }

    // Give up some time to another process (discovery thread)
    private void giveUpTime() {
        Thread.yield();
    }

    // Do nothing here
    private void showDebug(String s) {
        moSystem.logDebugMessage(s);
    }

    // Do nothing here
    private void showError(String s) {
        moSystem.logErrorMessage(s);
    }

    private int getTimeSeconds() {
        return PersistantData.getTheTime();
    }

    //  ancel's the devices/services search
    void cancelSearching() {
        try { bluetoothDiscovery.cancelInquiry(this); } catch (Exception e) {moSystem.logErrorMessage("cancelSearching:"+e.toString());}

        if (deviceList!=null)
            for (int i = 0; i < deviceList.size(); i++)  {
                   DiscoveryDevice dev = (DiscoveryDevice)deviceList.elementAt(i);
                   try {bluetoothDiscovery.cancelServiceSearch(dev.transactionID); } catch (Exception e) {}
            }
    }

    // Invoked by system when device discovery is done.
    public void inquiryCompleted(int discType) {
        scanStatus=199;
        synchronized (sync_BTDiscovery) {
            try {
                scanStatus=200;
                sync_BTDiscovery.notifyAll();
            } catch (Exception e) {}
        }
    }

   // Wait for a the above to be called
    private void waitForCompletion(boolean timeout) {
        synchronized (sync_BTDiscovery) {
            try {
                if (timeout) sync_BTDiscovery.wait(60000); else {   // 60 seconds
                        sync_BTDiscovery.wait(120000);  // abort after 120 minutes seconds
                        cancelSearching();
                }
            } catch (Exception e) {}
        }
    }

    // Devices discovered
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        // Remove duplicates
        try {
            for (int a=0; a<deviceList.size(); a++)
                 if (btDevice==((DiscoveryDevice)deviceList.elementAt(a)).remoteDevice) return;

            DiscoveryDevice dev = new DiscoveryDevice();
            dev.remoteDevice = btDevice;
            deviceList.addElement(dev);
        } catch (Exception e) {showError("deviceDiscovered() "+e.toString());}
    }

    // Need to find the device and add the services to it
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        try {
              if (servRecord.length>0) {
                  DiscoveryDevice dev = (DiscoveryDevice)deviceList.elementAt(iCurrentDeviceNumber);

                  if (transID==dev.transactionID) {
                       dev.servRecord=servRecord[0];  // assume its Mobuds
                       return;
                  }
              }
         } catch (Exception e) {showError("servicesDiscovered() "+e.toString());}
    }

    // Bluetooth search for services completed on a device
     public void serviceSearchCompleted(int transID, int respCode) {
         try {
                DiscoveryDevice dev = (DiscoveryDevice)deviceList.elementAt(iCurrentDeviceNumber);
                if (dev.transactionID == transID) {
                   if (baseconf.ISDEBUG) showDebug("Service search complete on "+dev.remoteDevice.getBluetoothAddress());
                   dev.transactionID = -dev.transactionID;
                   dev.completed = true;

                    // ok, as we only allow one to be discovered at a time, lets wake up the main thread and start the next one
                    synchronized (sync_BTDiscovery) {
                    try {
                            sync_BTDiscovery.notify();
                        } catch (Exception e) {
                            showDebug("Error: "+e.getMessage());
                        }
                    }
                } else
                    if (baseconf.ISDEBUG) showDebug("Service search complete on unknown device");

           } catch (Exception e) {showError("serviceSearchCompleted() "+e.toString()); }
     }

    // Get a version number from a profile string
    private int getVersionFromProfileString(String sProfile) {
        if ((sProfile.startsWith(MobudsProfile.PROFILE_START_MARKER+""))&&(sProfile.endsWith(MobudsProfile.PROFILE_END_MARKER+""))) {
             return    (MobudsProfile.MYBASE64.indexOf(sProfile.charAt(2)) & 63) |
                      ((MobudsProfile.MYBASE64.indexOf(sProfile.charAt(3)) & 63) << 6) |
                      ((MobudsProfile.MYBASE64.indexOf(sProfile.charAt(4)) & 63) << 12) |
                      ((MobudsProfile.MYBASE64.indexOf(sProfile.charAt(5)) & 63) << 18) |
                      ((MobudsProfile.MYBASE64.indexOf(sProfile.charAt(6)) & 63) << 24);
        } else return 0;
    }

    // Get cycle value from a profile string
    private int getProfileCycleFromProfileString(String sProfile) {
        if ((sProfile.startsWith(MobudsProfile.PROFILE_START_MARKER+""))&&(sProfile.endsWith(MobudsProfile.PROFILE_END_MARKER+""))) {
             return MobudsProfile.MYBASE64.indexOf(sProfile.charAt(1));
        } else return 0;
    }

    // Returns true if the profile cycled forward
    private boolean profileCycleForward(String p1, String p2) {
        int i1 = getProfileCycleFromProfileString(p1);  // new value
        int i2 = getProfileCycleFromProfileString(p2);  // old one

        //           Backwards wrap     Forwards
        return (  ((i1<3)&&(i2>60))  || (i1>i2) );
    }

    // Returns the distance of a user, or -1 if not found
    public int onlineDistance(String sBluetoothAddress) {
        synchronized (sync_UserListSaver) {
            DiscoveredUsers u = (DiscoveredUsers)userList.get(sBluetoothAddress);
            if (u!=null) return u.distance; else return -1;
        }
    }

    /* makes a copy of the userlist */
    public Vector getUserlistCopy() {
        Vector res = new Vector();
        MobudsProfile tmpProfile  = new  MobudsProfile();
        DiscoveredUsers dis;
        int i;

        // For public use of the list
        synchronized (sync_UserListSaver) {
            Enumeration eNum = userList.elements();
            while (eNum.hasMoreElements()) {
                dis = ((DiscoveredUsers)eNum.nextElement()).copyMe();
                tmpProfile.loadFromString(dis.userProfile);
                dis.txtPersonName = tmpProfile.strNickName;
                res.addElement(dis);
            }
        }

        // Now we have a copy, we need to sort it
        if (res.size()>1) {
            try { baseconf.quickSort(res,0,res.size()-1); }
            catch (Exception e) {}
            for (i=0; i<res.size(); i++) ((DiscoveredUsers)res.elementAt(i)).txtPersonName = null;
        }

        return res;
    }

    // Refresh user counter
    private void refreshScreen() {
        // Notify the application that stuff has happened
        moSystem.notifyUserlistChanged(userList.size(),newNumberOfMatchedUsers,newNumberOfFriends,newNumberOfBlocks);
        giveUpTime();
   }

    // Manually works out the connection URL from a service record
    private String manualConnectionUrl(ServiceRecord record, String bluetoothAddress) {
        DataElement protocolDescriptorList = record.getAttributeValue(0x0004);
        Enumeration e = (Enumeration) protocolDescriptorList.getValue(); // DATSEQ | DATALT


//#ifdef L2CAPMODE
//#         DataElement protocolDescriptorL2CAP = (DataElement) e.nextElement();
//#         e = (Enumeration) protocolDescriptorL2CAP.getValue(); // DATSEQ
//#else
        e.nextElement(); // L2CAP (ignored)
        DataElement protocolDescriptorRFCOMM = (DataElement) e.nextElement();
        e = (Enumeration) protocolDescriptorRFCOMM.getValue(); // DATSEQ
//#endif

        e.nextElement(); // UUID (ignored)
        StringBuffer nameBuffer = new StringBuffer(5+3+12+1+2+19+14+13);

//#ifdef L2CAPMODE
//#         DataElement channelL2CAP = (DataElement) e.nextElement();
//#         long channel = channelL2CAP.getLong(); // U_INT_1
//#         nameBuffer.append("btl2cap://");
//#else
        DataElement channelRFCOMM = (DataElement) e.nextElement();
        long channel = channelRFCOMM.getLong(); // U_INT_1
        nameBuffer.append("btspp://");
//#endif

        nameBuffer.append(bluetoothAddress);
        nameBuffer.append(":");
//#ifdef L2CAPMODE
//#         nameBuffer.append(baseconf.long2String(channel));
//#         nameBuffer.append(";authenticate=false;encrypt=false");
        //#else
        nameBuffer.append(channel);
        nameBuffer.append(";authenticate=false;encrypt=false");
        //if (masterSlaveSwitch) nameBuffer.append(";master=true");
//#endif

        return nameBuffer.toString();
    }

    // Get profile for a user
    public DiscoveredUsers getUserCopy(String sBluetoothAddress) {
        synchronized (sync_UserListSaver) {
            DiscoveredUsers d = (DiscoveredUsers)userList.get(sBluetoothAddress);
            if (d!=null) return d.copyMe(); else return null;
        }
    }

    // Make sure we are discoverable
    /*private void forceDiscoverable() {
        // And put it back
        try {
             bluetoothService.setDiscoverable(DiscoveryAgent.GIAC);
        } catch (Exception e) {
            try {
                bluetoothService.setDiscoverable(DiscoveryAgent.LIAC);
            } catch (Exception f) {}
        }
    }*/

    private void forceDiscoverable() {
        // And put it back
        try {
             bluetoothService.setDiscoverable(DiscoveryAgent.GIAC);
        } catch (Exception e) {};

        long iStartTime = System.currentTimeMillis();
        while (System.currentTimeMillis()-iStartTime<5000) {
            if (bluetoothService.getDiscoverable()!=DiscoveryAgent.GIAC) {
                try {
                     bluetoothService.setDiscoverable(DiscoveryAgent.GIAC);
                } catch (Exception e) {};
                if (bluetoothService.getDiscoverable()!=DiscoveryAgent.GIAC)
                    try {Thread.sleep(500); } catch (Exception ww) {}
            } else return;
        }
    }

    // Add or update a user in the internal list newList
    private boolean addUpdateUser(String sUrl, String blueSeenBy, String blueAddress, int iDistance, String sProfile) {
          boolean changed = false;
          try {
              if (iDistance>=baseconf.MAX_DISTANCE) return false;  // or this could get a bit silly

              // Check if this is a MOHUB!
              if ((sProfile.startsWith(clsMohubServer.MOHUB_PROFILE_START+""))&&(sProfile.endsWith(clsMohubServer.MOHUB_PROFILE_STOP+""))) {

                  // YES! A Mo-hub
                  if (iDistance==0)
                      if (mohubServer == null) {
                            mohubServer = new clsMohubServer();
                            if (!mohubServer.updateProfile(sUrl,blueAddress,sProfile)) mohubServer=null;
                      } else mohubServer.updateProfile(sUrl,blueAddress,sProfile);

                  return false;  // don't pass this on
              }

              // Does this user exist?
              DiscoveredUsers user = (DiscoveredUsers)newList.get(blueAddress);

              if (user!=null)
              {    // Has the actual profile changed?
                   if (!user.userProfile.equals(sProfile)) {
                       
                       if (getVersionFromProfileString(sProfile)>getVersionFromProfileString(user.userProfile)) {
                          user.userProfile = sProfile;  // update!
                          user.lastDiscovererd = getTimeSeconds();   // If it changed, its obviously in range
                          changed = true;
                       } else if (profileCycleForward(sProfile,user.userProfile)) {
                           // Profile is the same, even though it physically changed.  But its still alive
                           user.userProfile = sProfile;  // update!
                           user.lastDiscovererd = getTimeSeconds();   // If it changed, its obviously in range
                       } 
                   }  
                   if (iDistance<user.distance) {
                       user.distance=iDistance;
                       if (iDistance<1) user.sConnectionUrl = sUrl;
                   }
              } else {
                   changed = true;
                   user = new DiscoveredUsers();
                   user.distance = iDistance;
                   user.lastDiscovererd = getTimeSeconds();
                   user.userProfile = sProfile;
                   user.bluetoothAddress = blueAddress;
                   if (iDistance<1) user.sConnectionUrl = sUrl;
                   vNewUsers.addElement(user);
                   newList.put(user.bluetoothAddress,user);
              }

              // Keep a list of addresses that have seen us CLOSE BY, incase we need them later on
              if (!blueSeenBy.equals(blueAddress)) user.vUsersThatSeeMe.addElement(blueSeenBy);

          } catch (Exception e) {showError("addUpdateUser() "+e.toString()); }

          return changed;
   }

    // Perform friend discovery!
    private void performDiscovery() {
        deviceList = new Vector();
        int i;
        boolean changesDetected = false;
        DiscoveredUsers disUser = new DiscoveredUsers();
        DiscoveryDevice dev;
        String bluetoothSelfAddress = bluetoothService.getBluetoothAddress();
        boolean checkForUnknowns = false;
        scanCount++;
        scanStatus=1;

        if (moSystem.persData.getAllRecordIds().length<4) nonMobudsUsers=null;  // not much storage so dont do this

        if (baseconf.ISDEBUG) showDebug("start discovery. "+bluetoothService.getDiscoverable());

        // Perform Mobile Phone Discovery
        try {
            if (bluetoothDiscovery.startInquiry(DiscoveryAgent.GIAC, this))
            {   // Wait for discovery to finish
                scanStatus=2;
                waitForCompletion(false);

                // See what happened.
                if (bIsClosed)
                {   deviceList = null;
                    scanStatus=4;
                    return;
                }
            } else  {
                deviceList = null;
                showError("performDiscovery(-1) returned false");
                return;
            }
        } catch (Exception e) {
            showError("performDiscovery(1) "+e.toString());
            deviceList = null;
            return;
        }
        scanStatus=3;

        if (deviceList==null) {
                scanStatus=5;
                return;
        }
        if (bIsClosed) {
                scanStatus=6;
                return;
        }

        if ((baseconf.ISDEBUG)&&(deviceList!=null))  showDebug("Discovery Completed. "+deviceList.size()+" found");

        // a short pause
        try {
            Thread.sleep(150);
        } catch (Exception e) {}

        scanStatus=7;

        // Perform Service Discovery - most devices dont support simultaneous discovery, so we just do one at a time
        for (i=0; i<deviceList.size(); i++) {
            dev = (DiscoveryDevice)deviceList.elementAt(i);
            try {
                iCurrentDeviceNumber = i;
                dev.completed = false;
                showDebug("Service Search:"+dev.remoteDevice.getBluetoothAddress());
                dev.transactionID=bluetoothDiscovery.searchServices(cacheAttribs, cacheUUID, dev.remoteDevice, this);
                if (dev.transactionID>=0) {
                        waitForCompletion(true);
                        try {
                            bluetoothDiscovery.cancelServiceSearch(Math.abs(dev.transactionID));
                        } catch (Exception e2) {}

                        if (!dev.completed) showDebug("SEARCH TIMEOUT"); else showDebug("Search Complete");

                } else showDebug("Error "+dev.transactionID+" looking for services");
                dev.transactionID=-1;
            } catch (Exception e) {dev.transactionID=-1;}
        }

        scanStatus=8;


        clsMohubServer mh = getLocation();
        if (mh!=null) if (mh.mohubID<0) mh=null;
        if (mh==null) moSystem.persData.notifyNewLocation("",-1); else moSystem.persData.notifyNewLocation(getLocationString(),mh.mohubID);

        scanStatus=9;

        // a short pause
        try {
            Thread.sleep(150);
        } catch (Exception e) {}

        forceDiscoverable();

        if (bIsClosed) return;

        if (baseconf.ISDEBUG)  showDebug("Service Search Complete");

        // Analyize the results - we wanna to sync because userList is gonna be touched.
        DataElement de,deRoot;
        Enumeration list;
        //int iDistance;
        //MobudsProfile tmpProfile = new MobudsProfile();
        DiscoveredUsers du,nu;

        newList = new Hashtable();
        vNewUsers = new Vector();

        // remove old mohub server if it doesnt exist anymore
        if (mohubServer!=null)
            if (mohubServer.isExpired()) mohubServer=null;

        try {
                scanStatus=10;

                // Step 1: Make a copy of the list, and do stuff with it.
                Enumeration en = userList.elements();
                while (en.hasMoreElements()) {
                    du = (DiscoveredUsers)en.nextElement();
                    nu = du.copyMeComplete();
                    nu.lastDistance = nu.distance;
                    nu.distance = baseconf.MAX_DISTANCE+1;
                    newList.put(nu.bluetoothAddress,nu);
                }


                newNumberOfMatchedUsers=0;
                newNumberOfFriends=0;
                newNumberOfBlocks=0;
                String sBlue;
                boolean match;
                AlertProfile ap[] = new AlertProfile[3];
                MobudsProfile mp = new MobudsProfile();

                // load alerts
                for (i=0; i<3; i++)
                    if (moSystem.persData.bEnableAlert[i]) {
                        ap[i] = new AlertProfile(i+1);
                        ap[i].fromString(moSystem.persData.sAlertProfile[i]);
                    } else ap[i] = null;

                checkForUnknowns = (devicesNotRunningMobuds!=null);
                giveUpTime();

                scanStatus=11;
                // Step 2: Update entries in the list
                for (i = 0; i < deviceList.size(); i++) {
                      dev = (DiscoveryDevice)deviceList.elementAt(i);

                      // See whats being broadcast
                      if (dev.servRecord == null) {
                           if (baseconf.ISDEBUG) showDebug(dev.remoteDevice.getBluetoothAddress()+" is not running MoBuds");
                           try {
                                if (nonMobudsUsers!=null)
                                    if (nonMobudsUsers.put(dev.remoteDevice.getBluetoothAddress(),PersistantData.getTheTime()+"")==null) moSystem.persData.notifyNewBluetoothAddress(dev.remoteDevice.getBluetoothAddress(),true);
                           } catch (Exception moo2) {}
                           if ((devicesNotRunningMobuds!=null)&&(checkForUnknowns)) {
                               // Add any unknown devices
                               try {
                                    devicesNotRunningMobuds.addElement(dev.remoteDevice);
                               } catch (Exception eee) {}
                           }
                      } else {
                          try {
                              deRoot = dev.servRecord.getAttributeValue(DIRECTORY_ATTRIBUTE);
                              if (deRoot!=null) {
                                  list = (Enumeration)deRoot.getValue();

                                  if (list.hasMoreElements()) {
                                      de = (DataElement)list.nextElement();
                                      if (de!=null) {
                                           en = (Enumeration) de.getValue();
                                           while (en.hasMoreElements()) {
                                                // Get profile from device search
                                                de = (DataElement) en.nextElement();
                                                PersonMessage te = new PersonMessage();
                                                te.fromPasvSaving((String)de.getValue());
                                                receiveMessage(te,false);
                                           }
                                      }
                                  }

                                   if (list.hasMoreElements()) {
                                      de = (DataElement)list.nextElement();
                                      if (de!=null) {
                                           en = (Enumeration) de.getValue();
                                           if (en.hasMoreElements()) {
                                                // Get profile from device search
                                                de = (DataElement) en.nextElement();
                                                LocationFinder.publishOtherLocation((String)de.getValue());
                                           }
                                      }
                                   }


                                   if (list.hasMoreElements()) {
                                      de = (DataElement)list.nextElement();
                                      if (de!=null) {
                                           en = (Enumeration) de.getValue();
                                           while (en.hasMoreElements()) {
                                                // Get profile from device search
                                                de = (DataElement) en.nextElement();

                                                 if (disUser.getUserFromString((String)de.getValue()))
                                                    if (!disUser.bluetoothAddress.equals(bluetoothSelfAddress)) {
                                                        sBlue = dev.remoteDevice.getBluetoothAddress();

                                                        if (nonMobudsUsers!=null) nonMobudsUsers.remove(sBlue);  // remove from the non-Mobuds User List

                                                        if (moSystem.persData.isBlocked(sBlue)) newNumberOfBlocks++;
                                                        if (moSystem.persData.isFriend(sBlue)) newNumberOfFriends++;

                                                        if ( ((ap[0]!=null)||(ap[1]!=null)||(ap[2]!=null)) && (!moSystem.persData.isBlocked(sBlue)) ) {
                                                            match=false;
                                                            mp.loadFromString(disUser.userProfile);
                                                            if ((ap[0]!=null)&&(!match)) if (mp.matchesAlertProfile(ap[0])) match=true;
                                                            if ((ap[1]!=null)&&(!match)) if (mp.matchesAlertProfile(ap[1])) match=true;
                                                            if ((ap[2]!=null)&&(!match)) if (mp.matchesAlertProfile(ap[2])) match=true;
                                                            if (match) newNumberOfMatchedUsers++;
                                                        }
                                                        if (addUpdateUser(manualConnectionUrl(dev.servRecord,sBlue),sBlue,disUser.bluetoothAddress,disUser.distance,disUser.userProfile)) changesDetected=true;
                                                    }
                                           }
                                      }
                                   }
                              } else {
                                   // This device does NOT support mobuds, add it to our "friends" list
                                   if (baseconf.ISDEBUG) showDebug(dev.remoteDevice.getBluetoothAddress()+" is invalid");
                                   try {
                                       if (nonMobudsUsers!=null)
                                            if (nonMobudsUsers.put(dev.remoteDevice.getBluetoothAddress(),PersistantData.getTheTime()+"")==null) moSystem.persData.notifyNewBluetoothAddress(dev.remoteDevice.getBluetoothAddress(),true);
                                   } catch (Exception moo1) {}
                                   if ((devicesNotRunningMobuds!=null)&&(checkForUnknowns)) {
                                       // Add any unknown devices
                                       try {
                                            devicesNotRunningMobuds.addElement(dev.remoteDevice);
                                       } catch (Exception eee) {}
                                   }
                              }
                          } catch (Exception eee2) {}
                      }
                }

        } catch (Exception e) {showError("performDiscovery(4) "+e.toString());}

        // Tell the other bit we're ready now
        if ((devicesNotRunningMobuds!=null)&&(checkForUnknowns)) {
            try {
                synchronized (sync_NotRunningMobudsSync) {
                    sync_NotRunningMobudsSync.notify();
                }
            } catch (Exception hhhd) {}
        }

        scanStatus=12;
        if (bIsClosed) return;

        // Now remove users that have expired
        int dNowTime = getTimeSeconds();
        Enumeration eNum = newList.keys();
        DiscoveredUsers user;
        String key;

        giveUpTime();

        while (eNum.hasMoreElements()) {
             key = (String)eNum.nextElement();
             user = (DiscoveredUsers)newList.get(key);
             if (user.distance>baseconf.MAX_DISTANCE) user.distance = user.lastDistance;

             if ((dNowTime-user.lastDiscovererd>baseconf.MAX_USER_TIMEOUT) || ((newList.size()>baseconf.MAX_USERS_ALLOWED)&&(user.distance>baseconf.MAX_MINIMUM_DISTANCE))) {
                 if (baseconf.ISDEBUG) showDebug("User: "+user.bluetoothAddress+" expired");
                 newList.remove(key);
                 changesDetected = true;
                 moSystem.persData.notifyNewBluetoothAddress(user.bluetoothAddress,false);
             }
        }
        scanStatus=14;

        // apply a short lock on it only to copy it across, not holding it up now
        synchronized (sync_UserListSaver) {
            userList = newList;
        }

        // we dont need it anymore.....!
        newList = null;
        scanStatus=15;

        publishNewUsersToBluetooth();
        if (changesDetected) setChangedFlag = true;

        scanStatus=16;

        flushNonMobudsAddresses();

        // free up - we dont need it outside of here
        deviceList = null;
        scanStatus=17;
    }


    // Detes addresses from the list that havent been seen in a while
    private void flushNonMobudsAddresses() {
        if (nonMobudsUsers==null) return;
        Enumeration e = nonMobudsUsers.keys();
        int iNowTime = PersistantData.getTheTime();
        while (e.hasMoreElements()) {
            try {
                String bt = (String)e.nextElement();
                int i = Integer.parseInt((String)nonMobudsUsers.get(bt));
                if (iNowTime-i>baseconf.MAX_USER_TIMEOUT_NONMOBUDS) {
                    moSystem.persData.notifyNewBluetoothAddress(bt,false);
                    try {
                        nonMobudsUsers.remove(bt);
                    } catch (Exception Moo3) {}
                }
            } catch (Exception moo4) {}
        }
    }

    // Delete a history of old messages
    private void removeOldMessageHistory() {
        Enumeration en = messagesToIgnore.keys();
        long i;
        long nowI = System.currentTimeMillis();
        String key;
        Vector v = new Vector();

        while (en.hasMoreElements()) {
            key = (String)en.nextElement();
            i = Long.parseLong((String)messagesToIgnore.get(key));
            if (nowI-i>baseconf.MESSAGE_OUTBOX_EXPIRY_TIME*2) v.addElement(key);
        }

        // Now delete things in "V"
        for (int j=0; j<v.size(); j++) {
            key = (String)(v.elementAt(j));
            messagesToIgnore.remove(key);
        }
    }

    // Checks to see if any alerts need to be generated
    private void checkForAlerts(boolean checkEverything) {
        if (vNewUsers==null) return;
        AlertProfile ap = new AlertProfile(0);
        MobudsProfile profile = new MobudsProfile();
        DiscoveredUsers du;
        String s;

        // check through them all
        for (int a=0; a<vNewUsers.size(); a++) {
            du = (DiscoveredUsers)vNewUsers.elementAt(a);
            // We found a match?
            if (checkEverything) {
                profile.loadFromString(du.userProfile);
                s = profile.strNickName.trim();
                if (s.length()<1) s=baseconf.UNKNOWN_NAME;

                for (int alert = 0; alert<3; alert++)
                    if (moSystem.persData.bEnableAlert[alert]) {
                        ap.sAlertName="Alert "+(alert+1);
                        ap.fromString(moSystem.persData.sAlertProfile[alert]);
                        if (profile.matchesAlertProfile(ap)) moSystem.signalNearbyPerson("Alert","'"+s+"' is near-by and matches the alert profile '"+ap.sAlertName+"'",alert,profile);
                    }
                if ((moSystem.persData.bAlertFriends) && (moSystem.persData.isFriend(du.bluetoothAddress))) moSystem.signalNearbyPerson(baseconf.strFriend+" Alert","Your "+baseconf.strFriend+" '"+s+"' is near-by",-1,profile);
                if ((moSystem.persData.bAlertBlocks) && (moSystem.persData.isBlocked(du.bluetoothAddress))) moSystem.signalNearbyPerson(baseconf.strBlock+" Alert","'"+s+"' is on your "+baseconf.strBlocks+" list and is near-by",-2,profile);
            }
            if (nonMobudsUsers.remove(du.bluetoothAddress)==null)  moSystem.persData.notifyNewBluetoothAddress(du.bluetoothAddress,true);
        }

            // don't need it now
        vNewUsers = null;
    }

    // update mail stats
    public void refreshMailStatistics() {
        notifyMessageQueueSizeChange();
    }

    // Threadded
    public void run() {
        int a;
        Random randGen = new Random();
        randGen.setSeed(System.currentTimeMillis());


        pauseStatus=0;
        while (!bIsClosed)
        {
            try {
                try {
                    moSystem.logDebugMessage("performDiscovery");
                    pauseStatus=1;
                    performDiscovery();
                    // Update
                    pauseStatus=2;
                    refreshScreen();
                    // The first time through we dont want to check for alerts as we will pick up any mates we have around us.
                    moSystem.logDebugMessage("checkForAlerts");
                    pauseStatus=3;
                    checkForAlerts(allowAlertsCheck);
                    allowAlertsCheck = true;
                    // Push any delayed messages back into the outbox
                 } catch (Exception eev) {
                    showError("Discovery Error "+eev.toString());
                 }

                 try {
                     moSystem.logDebugMessage("forceDiscoverable");
                     pauseStatus=4;
                     messageQueue.runMessageProcessing(baseconf.MESSAGE_PROCESSING_TIME);

                     // Force garbage collection! - turned off, not recommedned by LWUIT
                     // pauseStatus=5;
                     // System.gc();   // SE K800i locked up here

                     moSystem.logDebugMessage("forceDiscoverable");
                     forceDiscoverable();
                 } catch (Exception eeeem) {
                    showError("DiscErr2:"+eeeem.getMessage());
                 }

                // Add some jitter to prevent phones from syncing refresh periods
                long i;
                int p;
                long endtime;
                p=0;
                pauseStatus=6;
                i = Math.abs(randGen.nextInt()%baseconf.MAX_DISCOVERY_JITTER)+baseconf.MIN_DISCOVERY_DELAY;
                if (baseconf.ISDEBUG) i/=4;
                endtime = System.currentTimeMillis() + (i*1000);
                if (devicesNotRunningMobuds==null) {
                    p=1;
                    moSystem.TimeTillNextScan = (int)(endtime-System.currentTimeMillis())/1000;
                    while (System.currentTimeMillis()<endtime)
                           try {
                               // Sleep for a random period to make this a bit weirder
                               p=2;
                               synchronized(discoveryThread) {
                                   p=3;
                                   discoveryThread.wait(1000);
                                   p=4;
                               }
                               if ((devicesNotRunningMobuds!=null)||(bIsClosed)) endtime=0;
                               moSystem.TimeTillNextScan = (int)(endtime-System.currentTimeMillis())/1000;
                           } catch (Exception e) {showError("run(2) "+e.toString()+" ("+p+")");}
                    forceDiscoverable();
                }

                pauseStatus=7;
                messageQueue.runMessageProcessing(1000);
            } catch (Exception m) {moSystem.logErrorMessage("BTManager: Run() "+m.getMessage());}
        }

        scanStatus=100;

        moSystem.logDebugMessage("loop_restart");

        try {
             newConnNotify.close();  // stop listening
             newConnNotify = null;
        } catch (Exception e) {}
    }

    // Update profiles in stored messages
    private void updateInboxMessages(Hashtable userList) {
        synchronized(sync_Inbox) {
            for (int a=0; a<inBox.size(); a++) {
                PersonMessage pm = (PersonMessage)inBox.elementAt(a);

                try {
                    DiscoveredUsers du = (DiscoveredUsers)userList.get(pm.sFromBluetooth);

                    // Not bothered about actually forcing a save here
                    if (du!=null) pm.sSourceProfile = du.userProfile;
                } catch (Exception e) {}
            }
        }
    }

    // Haha, add these as discoverable too
    private boolean publishNewUsersToBluetooth() {
        try {
            //#ifdef L2CAPMODE
//#             L2CAPConnectionNotifier tmpNew;
            //#else
            StreamConnectionNotifier tmpNew;
            //#endif

            bProfileChanged = false;
            DiscoveredUsers u = new DiscoveredUsers();

            if (bIsClosed) return false;

            // Modify the cycle value
            moSystem.persData.myProfile.bCycleValue = (byte)(((moSystem.persData.myProfile.bCycleValue+1)&63));

            // Can prepare this too
            // Get the record we wanna update - This will be a DATASEQ record
            DataElement dir = new DataElement(DataElement.DATSEQ);
            DataElement base = new DataElement(DataElement.DATSEQ);
            DataElement loc = new DataElement(DataElement.DATSEQ);
            DataElement msg = new DataElement(DataElement.DATSEQ);

            // Save all users
            moSystem.persData.myProfile.bStarsign = (byte)baseconf.getStarSign(moSystem.persData.myProfile.bDay,moSystem.persData.myProfile.bMonth);
            u.userProfile = moSystem.persData.myProfile.saveToString(true);
            u.bluetoothAddress = bluetoothService.getBluetoothAddress();
            u.distance = 0;
            u.iProfileVersion = moSystem.persData.myProfile.iProfileVersion;

            base.addElement(new DataElement(DataElement.STRING, u.getUserAsString() ));
            Enumeration eNum = userList.elements();
            while (eNum.hasMoreElements())
            {   u = (DiscoveredUsers)eNum.nextElement();
                base.addElement(new DataElement(DataElement.STRING, u.getUserAsString(1)));
            }

            giveUpTime();
            // Update the friends list
            moSystem.persData.updateFriendsBlocks(userList);
            updateInboxMessages(userList);

            // Save messages too
            Vector outbox = getOutboxCopy();
            for (int i=0; i<outbox.size(); i++)
            {   TransmittedMessage m = (TransmittedMessage)outbox.elementAt(i);
                if (m instanceof PersonMessage)
                    msg.addElement(new DataElement(DataElement.STRING,((PersonMessage)m).forPasvSaving()));
            }
            outbox = null;
            giveUpTime();

            loc.addElement(new DataElement(DataElement.STRING, getLocationString()));
            dir.addElement(msg);
            dir.addElement(loc);
            dir.addElement(base);

            if (newConnNotify!=null) {
                   if (serviceInfo!=null) {
                       serviceInfo.setAttributeValue(DIRECTORY_ATTRIBUTE,dir);
                       try {
                           bluetoothService.updateRecord(serviceInfo);
                           forceDiscoverable();
                           removeOldMessageHistory();
                           return true;
                       } catch (Exception eee) {
                            try {
                                serviceInfo = null;
                                newConnNotify.close();  // stop listening
                            } catch (Exception e) {showError("publishNewUsersToBluetooth(0) "+e.toString());}
                            newConnNotify = null;
                        // a short pause
                        try {
                            Thread.sleep(150);
                        } catch (Exception e) {}
                      }
                }
            }

          
             // create notifier now
            //#ifdef L2CAPMODE
//#              try {
//#                   tmpNew = (L2CAPConnectionNotifier) Connector.open("btl2cap://localhost:"+TALKEASI_SERVER_UUID.toString()+";name=MoBuds;authorize=false;authenticate=false;encrypt=false");
//#              } catch (Exception e) {showError("publishNewUsersToBluetooth(1) "+e.toString()); return false; }
             //#else
            try {
                  tmpNew = (StreamConnectionNotifier) Connector.open("btspp://localhost:"+TALKEASI_SERVER_UUID.toString()+";name=Mobuds;authorize=false;authenticate=false;encrypt=false");
            } catch (Exception e) {showError("publishNewUsersToBluetooth(1) "+e.toString()); return false; }
            //#endif


            // and remember the service record for the later updates
             serviceInfo = bluetoothService.getRecord(tmpNew);

             // Store connection url
             sOurConnectionURL = manualConnectionUrl(serviceInfo,bluetoothService.getBluetoothAddress());

             try {
                DataElement elm = new DataElement(DataElement.DATSEQ);

                elm.addElement(new DataElement(DataElement.UUID,new UUID(0x1002)));
                serviceInfo.setAttributeValue( 0x0005, elm ); // browse group list
             } catch (Exception e1){showError("publishNewUsersToBluetooth(x1) "+e1.getMessage());}

             // This service time to live information
             try {
                serviceInfo.setAttributeValue( 0x0007, new DataElement( DataElement.U_INT_4, baseconf.MESSAGE_PROCESSING_TIME*2) );
             } catch (Exception e1){showError("publishNewUsersToBluetooth(x2) "+e1.getMessage());}

             // This operation is optional - should make us visible
             try {
                serviceInfo.setAttributeValue( 0x0008, new DataElement( DataElement.U_INT_1, 0xFF ) );
             } catch (Exception e1){showError("publishNewUsersToBluetooth(x3) "+e1.getMessage());}

             // This is cool, shows others where to download Mobuds from!
             try {
                 serviceInfo.setAttributeValue( 0x000B, new DataElement( DataElement.URL, baseconf.SOFTWARE_DOWNLOAD_URL ) );
             } catch (Exception e1){showError("publishNewUsersToBluetooth(x31) "+e1.getMessage());}

             // Set what class we are
             try {
                serviceInfo.setDeviceServiceClasses(SERVICE_TELEPHONY);
             } catch (Exception e1){showError("publishNewUsersToBluetooth(x4) "+e1.getMessage());}

             // Finally, save it back
             try {
                serviceInfo.setAttributeValue(DIRECTORY_ATTRIBUTE,dir);
             } catch (Exception e1){showError("publishNewUsersToBluetooth(x5) "+e1.getMessage());}

                 // Notify bluetooth about the data
              try { bluetoothService.updateRecord(serviceInfo); }
              catch (Exception e)
             {
                 showError("publishNewUsersToBluetooth(2) "+e.getMessage());
                 // try one more time
                 try { bluetoothService.updateRecord(serviceInfo); }
                 catch (Exception e2)
                 {
                     showError("publishNewUsersToBluetooth(3) "+e2.getMessage());
                     try {
                        tmpNew.close();
                        tmpNew = null;
                     } catch (Exception e3) {}
                    return false;
                 }
             }

             forceDiscoverable();
             removeOldMessageHistory();

             if (baseconf.ISDEBUG)  showDebug("publishNewUsersToBluetooth() Completed");
            // and allow the thread to see it again
            newConnNotify = tmpNew;
            return true;
        } catch (Exception major) {
            showError("PublishUsersMajor:"+major.getMessage());
            return false;
        }
    }

    // Send a message to a bluetooth address
    public void sendMessageToAddress(String sBluetoothAddress, String sMessage) {
        PersonMessage pm = new PersonMessage();
        Date d = new Date();

        if (sMessage.length()>baseconf.MAX_MEESSAGE_LENGTH) sMessage = sMessage.substring(0,baseconf.MAX_MEESSAGE_LENGTH-1);

        pm.lTime = d.getTime();
        pm.sBluetoothTo = sBluetoothAddress;
        pm.sMessage = sMessage;
        pm.sFromBluetooth = bluetoothService.getBluetoothAddress();
        pm.sSourceProfile = moSystem.persData.myProfile.saveToString(true);
        pm.bMessageType = baseconf.COMMUNICATION_SENDMESSAGE;

        // Only output message if in debug mode
        if (baseconf.ISDEBUG) showDebug("Sending message ID "+pm.messageID+" FROM: "+pm.sFromBluetooth+" to "+sBluetoothAddress);

        messageQueue.sendMessage(pm);
        notifyMessageQueueSizeChange();
    }

    // Inbox size
    public int inboxSize() {
        return inBox.size();
    }

    // New inbox size
    public int inboxNewSize() {
        int i,count;
        count = 0;
        synchronized(sync_Inbox) {
            for (i=0; i<inBox.size(); i++)
                if (!((PersonMessage)inBox.elementAt(i)).hasBeenRead) count++;
        }

        return count;
    }

    // Outbox size
    public int outboxSize() {
        if (messageQueue==null) return 0; else return messageQueue.outboxSize();
    }

    // number of new unread alerts
    public int newAlertSize() {
        return 0;
    }

    // total number of  alerts
    public int alertSize() {
        return 0;
    }

    // Messaage queue changed
    public void notifyMessageQueueSizeChange() {
        moSystem.setInOut(inboxNewSize(),inboxSize(),outboxSize(),newAlertSize(),alertSize());
    }

    // Deletes a message that has been read/replied to
    private boolean deleteReadMessage() {
        for (int i=0; i<inBox.size(); i++)
              if (!((PersonMessage)inBox.elementAt(i)).hasReplied) {
                  inBox.removeElementAt(i);
                  return true;
              } else
              if (!((PersonMessage)inBox.elementAt(i)).hasBeenRead) {
                  inBox.removeElementAt(i);
                  return true;
              }
        return false;
    }

    // add a message into the inbox
    public boolean addMessageToInbox(PersonMessage pm) {

        // Firstly, see if we have received this message before
        synchronized (sync_Inbox) {
            inBox.addElement(pm);

            // Too many messages? Delete replied ones, then read ones before deleting unread messages
            while (inBox.size()>=PersistantData.SETTING_MAX_SAVED_MESSAGES)
                if (!deleteReadMessage()) inBox.removeElementAt(0);

            MobudsProfile p = new MobudsProfile();
            p.loadFromString(pm.sSourceProfile);
            String s = p.strNickName.trim();
            if (s.length()<1) s=baseconf.UNKNOWN_NAME;

            switch (pm.bMessageType) {
                case baseconf.COMMUNICATION_SENDMESSAGE:
                    moSystem.signalNewMessage(s,1);
                    break;
                case  baseconf.COMMUNICATION_LOCALERT: moSystem.signalNewMessage(s,2); break;
                case  baseconf.COMMUNICATION_LOCPROMO: moSystem.signalNewMessage(s,3); break;
                case  baseconf.COMMUNICATION_LOCVENNEWS: moSystem.signalNewMessage(s,4); break;
                case  baseconf.COMMUNICATION_LOCGENNEWS: moSystem.signalNewMessage(s,5); break;
            }
        }
        saveMessagesToDisk();
        return true;
    }

    // Does the bits that actually make this work
    private boolean startThreads() {
            // Make discoverable
            forceDiscoverable();

            publishNewUsersToBluetooth();

            // we dont much care about outgoing messages anymore
            messageQueue.prepareOutgoingMessages();

            connectionHandler.startThread();

            // Start Thread for discovery of other phones
            discoveryThread.start();

            // Try to force the discovery thread as a lower priority than everything else
            try {
                int lowerPriority = Thread.NORM_PRIORITY-1;
                if (lowerPriority<Thread.MIN_PRIORITY) lowerPriority=Thread.MIN_PRIORITY;
                discoveryThread.setPriority(lowerPriority);
            } catch (Exception exPriority) {}

            return true;
    }

    //#if JSR179
    private clsLocationBase makeSpecialLocation() {
        try {
            Class c = Class.forName("mobuds.clsLocationFinder");
            clsLocationBase provider = (clsLocationBase)(c.newInstance());
            return provider;
        } catch (Exception f) {
            return null;
        }
    }
    //#endif

    /* Creates a new instance of BTManager */
    public BTManager(MobudsSystem mosys, boolean useLocationServices) {
        try {

            moSystem = mosys;
            messagesToIgnore = new Hashtable();

            bProfileChanged = false;
            bIsClosed = false;
            boolStartedOK = false;

            // Inbox of messages
            inBox = new Vector();

            userList = new Hashtable(5);

            /* Prepare Bluetooth Discovery */
            cacheUUID = new UUID[1];
            cacheUUID[0] = TALKEASI_SERVER_UUID;

            cacheAttribs = new int[1];
            cacheAttribs[0] = DIRECTORY_ATTRIBUTE;

            /* Try to startup bluetooth */
            bluetoothService = LocalDevice.getLocalDevice();

            String btsw = LocalDevice.getProperty("bluetooth.master.switch");

            masterSlaveSwitch = ((btsw!=null)&(btsw.compareTo("true")==0));

            /* Get bluetooth discovery */
            bluetoothDiscovery = bluetoothService.getDiscoveryAgent();

            // we need these!
            loadMessagesFromDisk();

            // Start thread for sending messages
            messageQueue = new BTMessageSender();

            // Start Thread for monitoring for connections
            connectionHandler = new BTConnectionHandler();

            //#if JSR179
            // Only enable location based stuff if its actually supported
            LocationFinder = null;
            try {
                    if (useLocationServices) LocationFinder = makeSpecialLocation();
                    if (LocationFinder==null) LocationFinder = new clsLocationBase();
            } catch( Exception e ) {
                LocationFinder = new clsLocationBase();
            }
            //#else
//#             LocationFinder = new clsLocationBase();
            //#endif
           
            // Setup our profile
            if (!startThreads()) return;

            // Update the display
            notifyMessageQueueSizeChange();
            boolStartedOK = true;
        } catch (Exception e)
        {
            if (baseconf.ISDEBUG) {
                String s = e.toString();
                showDebug(s);
            }
        }
    }

    // Receive a message and place it in our message queue
    private void receiveMessage(TransmittedMessage pm, boolean fromIsFrom) {
        if (pm instanceof PersonMessage) {
            if (messagesToIgnore.containsKey(((PersonMessage)pm).getUniqueID())) return;
            messagesToIgnore.put(((PersonMessage)pm).getUniqueID(),System.currentTimeMillis()+"");
        }

        // ok, this message MAY need routing
        if (pm.sBluetoothTo.equals(bluetoothService.getBluetoothAddress())) {
            switch (pm.bMessageType) {
                case baseconf.COMMUNICATION_SENDMESSAGE:
                        if (moSystem.persData.bAllowMessages) {
                            // Yes, its for us, but are blocking this person?
                            if (!moSystem.persData.isBlocked(pm.sFromBluetooth)) {
                                // Only get here if the person is not blocked!
                                if ((!moSystem.persData.bAllowMessagesFromFriends) || (  (moSystem.persData.bAllowMessagesFromFriends) && (moSystem.persData.isFriend(pm.sFromBluetooth)) )) {
                                    // Message is valid.
                                    addMessageToInbox((PersonMessage)(pm));
                                }
                            }
                        }
                        break;
             }
        } else {
            // No, needs relaying on.
            showDebug("Relay message with ID "+pm.messageID);
            messageQueue.sendMessage(pm);
        }
    }

    // Terminate all bluetooth stuff
    public void terminateBluetooth() {
        bIsClosed = true;
        cancelSearching();

        if (newConnNotify!=null) try { newConnNotify.close(); } catch (Exception e) {}
        newConnNotify = null;
    }

//#ifdef L2CAPMODE
//#     private class L2CapIOManager {
//#         private L2CAPConnection conn;
//#         byte[] buffer;
//#         boolean reading;
//#         int bufpos,bufsize;
//#         int MaxOutBufSize;
//#
//#         private L2CapIOManager(L2CAPConnection soc, boolean InputMode) {
//#             conn = soc;
//#             reading = InputMode;
//#
//#             try {
//#                 MaxOutBufSize = Math.min(conn.getTransmitMTU(),conn.getReceiveMTU());
//#             } catch (Exception e) {MaxOutBufSize=320;}
//#             if (MaxOutBufSize>320) MaxOutBufSize=320;
//#             buffer = new byte[MaxOutBufSize];
//#             bufpos=0;
//#             bufsize=0;
//#         }
//#
//#         public void write(byte[] data, int start, int length) {
//#             if (reading) return;
//#             for (int a=0; a<length; a++) {
//#                 buffer[bufpos++]=data[a+start];
//#                 if (bufpos>=buffer.length) flush();
//#             }
//#         }
//#
//#         public byte read() {
//#             if (bufpos>=bufsize) {
//#                 try {
//#                     bufsize = conn.receive(buffer);
//#                     bufpos=0;
//#
//#                  } catch (Exception e) {
//#                     return 0;
//#                 }
//#             }
//#
//#             return buffer[bufpos++];
//#         }
//#
//#
//#         public void read(byte[] data, int start, int length) {
//#             if (!reading) return;
//#
//#             while (length>0) {
//#                 data[start] = read();
//#                 start++;
//#                 length--;
//#             }
//#         }
//#
//#         public void flush() {
//#             if (!reading) {
//#                 if (bufpos>0) {
//#                     byte[] tmp = new byte[bufpos];
//#                     System.arraycopy(buffer,0,tmp,0,bufpos);
//#
//#                     try {
//#                         conn.send(tmp);
//#                     } catch (Exception e) {
//#                         if (baseconf.ISDEBUG)  showDebug("Error sending data: "+e.toString());
//#                     }
//#                     bufpos=0;
//#                 }
//#             }
//#         }
//#
//#         public int available() {
//#             int b = 0;
//#             try {
//#                 b = (conn.ready()?1:0);
//#             } catch (Exception e) {b=0;}
//#             return b;
//#         }
//#
//#         public void write(byte[] data) {
//#             write(data,0,data.length);
//#         }
//#
//#         public void write(byte data) {
//#             byte[] d = new byte[1];
//#             d[0] = data;
//#             write(d,0,d.length);
//#         }
//#
//#         public void read(byte[] data) {
//#             read(data,0,data.length);
//#         }
//#
//#         public void close() {
//#             flush();
//#             conn = null;
//#         }
//#     }
    //#endif

    // Class for handling a special message transmittion thread
    private class BTMessageSender {
         private final Object sync_messageQueue = new Object();
         private final Object sync_messageQueueOutbox = new Object();
         private final Object sync_messageQueueOutboxDelayQueue = new Object();

         private Vector messageQueue;
         private Vector outboxDelayQueue;
         private Vector outboxMessageQueue;
         private long lLastConnectionOutboundTest;
         int iFakeCounter;

         // Pumps out a dummy ocnnection to ensure the user is allowing outbound connections
         public boolean prepareOutgoingMessages() {
            //#ifdef L2CAPMODE
//#             L2CAPConnection soc = null;
            //#else
            StreamConnection soc = null;
            //#endif
            boolean fResult = true;
            lLastConnectionOutboundTest  = System.currentTimeMillis();
            try {
                try {
                    // Doesn't matter what.  we're not actually gonna use the connection
                     //#ifdef L2CAPMODE
//#                     soc = (L2CAPConnection)Connector.open("btl2cap://000000000001:1;authenticate=false;encrypt=false;master=false",Connector.READ_WRITE,true);
                    //#else
                    soc = (StreamConnection)Connector.open("btspp://000000000001:1;authenticate=false;encrypt=false;master=false",Connector.READ_WRITE,true);
                    //#endif
                } catch (SecurityException eee) {
                    // Security exception, we dont have permissions!
                    fResult=false;
                }
            } catch (Exception e) {}
            try { soc.close();} catch (Exception e) {}
            soc = null;

            return fResult;
         }

         // Size of the outbox
         public int outboxSize() {
             int nTotal = 0;
             String btAddress = bluetoothService.getBluetoothAddress();

             synchronized (sync_messageQueue) {
                for (int a=0; a<messageQueue.size(); a++)
                    if (((TransmittedMessage)messageQueue.elementAt(a)).bMessageType==baseconf.COMMUNICATION_SENDMESSAGE)
                        if (((TransmittedMessage)messageQueue.elementAt(a)).sFromBluetooth.equals(btAddress)) nTotal++;

                for (int a=0; a<outboxDelayQueue.size(); a++)
                    if (((TransmittedMessage)outboxDelayQueue.elementAt(a)).bMessageType==baseconf.COMMUNICATION_SENDMESSAGE)
                        if (((TransmittedMessage)outboxDelayQueue.elementAt(a)).sFromBluetooth.equals(btAddress)) nTotal++;
             }

             return (nTotal+outboxMessageQueue.size()+iFakeCounter);
         }

         // Remove message from outbox with a specific id
         public String removeMessageWithId(int messageID) {
             synchronized(sync_messageQueueOutbox) {
                 for (int i=0; i<outboxMessageQueue.size(); i++)
                     if (((TransmittedMessage)outboxMessageQueue.elementAt(i)).messageID==messageID) {
                        String s = "";
                        if (((PersonMessage)outboxMessageQueue.elementAt(i)).bMessageType==baseconf.COMMUNICATION_SENDMESSAGE) s=((PersonMessage)outboxMessageQueue.elementAt(i)).sMessage;
                        outboxMessageQueue.removeElementAt(i);
                        return s;
                     }
             }
             return "";
         }

         // Sends a message into the queue
         public void sendMessage(TransmittedMessage pm) {
             synchronized(sync_messageQueue) {
                 messageQueue.addElement(pm);
                 sync_messageQueue.notify();
             }
         }

         // Purge outbox messafes that have failed
         private void purgeFailedMessages() {
             Date d = new Date();
             int i = 0;
             synchronized(sync_messageQueueOutbox) {
                 while (i<outboxMessageQueue.size()) {
                     TransmittedMessage tm = ((TransmittedMessage)outboxMessageQueue.elementAt(i));
                     if ((d.getTime()-tm.queueDate>baseconf.MESSAGE_OUTBOX_EXPIRY_TIME)) {
                         outboxMessageQueue.removeElementAt(i);
                     } else i++;
                 }
             }
         }

         // Purge the delay queue back into the main message
         public void purgeDelayQueue() {
             synchronized(sync_messageQueueOutboxDelayQueue) {
                 synchronized(sync_messageQueue) {
                     for (int a=0; a<outboxDelayQueue.size(); a++)
                         messageQueue.addElement(outboxDelayQueue.elementAt(a));
                     // Make it process again!
                     sync_messageQueue.notify();
                 }
                 // Delete delay queue entries
                 outboxDelayQueue.removeAllElements();
             }
         }

         // Returns true if theres ANY work to be done
         public boolean isBusyProcessing() {
             return ((!outboxDelayQueue.isEmpty())||(!messageQueue.isEmpty()));
         }

         // Sends a message into process DELAY queue - this is copied back to the main queue once every discovery
         public void sendMessageToDelay(TransmittedMessage pm) {

             synchronized(sync_messageQueueOutboxDelayQueue) {
                 outboxDelayQueue.addElement(pm);
             }
         }

         // Constructor
         private BTMessageSender() {
             messageQueue = new Vector();
             outboxMessageQueue = new Vector();
             outboxDelayQueue = new Vector();
         }

         // get a list of Discovered Users that can see sBlueAddress, sorted by distance
         private Vector getUsersCloseBy(String sBlueAddress) {
            synchronized(sync_UserListSaver) {
                DiscoveredUsers du = (DiscoveredUsers)userList.get(sBlueAddress);
                DiscoveredUsers fu;

                // Search for this person.  If they're not anywhere to be seen then return NULL
                if (du==null) return null; else {
                    Vector closeBy = new Vector();

                    // Add anyone on the list that can see this person, in stance order
                    for (int i=0; i<du.vUsersThatSeeMe.size(); i++) {
                        try {
                            fu = ((DiscoveredUsers)userList.get((String)du.vUsersThatSeeMe.elementAt(i)));

                            // Are they near enough to be connected to?
                            if (fu!=null) {
                                if (fu.distance<=1) {
                                    fu = fu.copyMeComplete();
                                    // Now work backwards in the list and add it in in the right order
                                    for (int j=closeBy.size()-1; j>=1; j--) {
                                        if (fu.distance>=((DiscoveredUsers)closeBy.elementAt(j)).distance) {
                                            closeBy.insertElementAt(fu,j);
                                            fu = null;
                                            break;
                                        }
                                    }
                                    if (fu!=null) closeBy.insertElementAt(fu,0);
                                } else fu = null;
                            }
                        }catch (Exception moo) {}
                    }


                    // Is the actual person close enough to be tried?
                    if (du.distance<1) closeBy.insertElementAt(du.copyMeComplete(),0);

                    return closeBy;
                }
            }
         }

         // Get a message from the queue
         private TransmittedMessage getMessageFromQueue(int iTimeout) {
             TransmittedMessage pm = null;

             //String s = iTimeout+"";
             if ((!bIsClosed) && (messageQueue.isEmpty())) {
                    synchronized(sync_messageQueue) {
                        try { sync_messageQueue.wait(iTimeout); } catch(Exception e){}
                    }
             }

              synchronized(sync_messageQueue) {

                    if(!messageQueue.isEmpty()) {
                        pm = (TransmittedMessage)messageQueue.elementAt(0);
                        messageQueue.removeElementAt(0);

                        // Only save it if it's for u, this is so the notifications work
                        iFakeCounter=1;
                        if (pm.sFromBluetooth.equals(bluetoothService.getBluetoothAddress()))
                            if (pm.bMessageType==baseconf.COMMUNICATION_SENDMESSAGE)
                                synchronized(sync_messageQueueOutbox) {
                                    iFakeCounter = 0;
                                    Date d = new Date();
                                    removeMessageWithId(pm.messageID);
                                    pm.queueDate = d.getTime();
                                    outboxMessageQueue.addElement(pm);
                                }
                    }
                }

             return pm;

         }

         // Transmit a message to a person
         private boolean transmitMessage(TransmittedMessage msg, DiscoveredUsers sendToVia) {
             return transmitMessage(msg,sendToVia,"");
         }

         // Transmit a message to a person
         private boolean transmitMessage(TransmittedMessage msg, DiscoveredUsers sendToVia, String strRelayAddress) {
             if (msg.receivedPasv) return false;
             if (sendToVia!=null)
                if (msg.sRoute.contains(sendToVia.bluetoothAddress))  return false; // already been this way so disallow it

             // To send the message, we need to connect to the above, and then pass it on.  Then let it handle it
             boolean bResult = false;
             //#ifdef L2CAPMODE
//#              L2CAPConnection soc = null;
//#              L2CapIOManager out = null;
//#              L2CapIOManager in = null;
             //#else
             StreamConnection soc = null;
             OutputStream out = null;
             InputStream in = null;
             //#endif
             String tx = msg.toString();
             byte messageLength[] = new byte[16];
             byte bNum;
             // Get a copy of this person's profile
             String sOurProfile = "";
             if (strRelayAddress.length()>0) showDebug("Send message "+msg.bMessageType+" to mohub "+strRelayAddress); else
                                             showDebug("Send message "+msg.bMessageType+" to "+msg.sBluetoothTo);
             if (!msg.sFromBluetooth.equals(bluetoothService.getBluetoothAddress())) sOurProfile = moSystem.persData.myProfile.saveToString(true);

             // Prepare message header
             messageLength[0] = (byte)(msg.bMessageType&0xff);
             messageLength[1] = (byte)(msg.sBluetoothTo.length()&0xff);
             messageLength[2] = (byte)(msg.sFromBluetooth.length()&0xff);
             messageLength[3] = (byte)(msg.sSourceProfile.length()&0xff);
             messageLength[4] = (byte)(msg.sSourceProfile.length()>>8);
             messageLength[5] = (byte)(tx.length()&0xff);
             messageLength[6] = (byte)(tx.length()>>8);
             messageLength[7] = (byte)(msg.sRoute.size()&0xff);
             messageLength[8] = (byte)(msg.messageID & 0xff);
             messageLength[9] = (byte)((msg.messageID>>8) & 0xff);
             messageLength[10] = (byte)((msg.messageID>>16) & 0xff);
             messageLength[11] = (byte)((msg.messageID>>24) & 0xff);
             messageLength[12] = (byte)(msg.bErrorCode&0xff);
             messageLength[13] = (byte)(sOurProfile.length()&0xff);
             messageLength[14] = (byte)(sOurProfile.length()>>8);
             messageLength[15] = (byte)(sOurConnectionURL.length()&0xff);

             try {
                // Attempt to connect to the other phone
//#ifdef L2CAPMODE
//#                  if (strRelayAddress.length()>0)
//#                      soc = (L2CAPConnection)Connector.open(strRelayAddress,Connector.READ_WRITE,true); else
//#                      soc = (L2CAPConnection)Connector.open(sendToVia.sConnectionUrl,Connector.READ_WRITE,true);
//#
//#                 // Send the message
//#                 out = new L2CapIOManager(soc,false);
//#                 // Open the input/receive stream
//#                 in = new L2CapIOManager(soc,true);
//#else 
                if (strRelayAddress.length()>0)
                     soc = (StreamConnection)Connector.open(strRelayAddress,Connector.READ_WRITE,true); else
                     soc = (StreamConnection)Connector.open(sendToVia.sConnectionUrl,Connector.READ_WRITE,true);

                // Send the message
                out = soc.openOutputStream();

                // Open the input/receive stream
                in = soc.openInputStream();

//#endif
                // Send the message length
                out.write(messageLength);

                // Send the address history
                for (int a=0; a<msg.sRoute.size(); a++) {
                    String sAdd = (String)msg.sRoute.elementAt(a);
                    bNum = (byte)sAdd.length();
                    out.write(bNum);
                    out.write(sAdd.getBytes(),0,sAdd.length());
                }

                // Send the parts of the header
                out.write(msg.sBluetoothTo.getBytes(),0,msg.sBluetoothTo.length());
                out.write(msg.sFromBluetooth.getBytes(),0,msg.sFromBluetooth.length());
                out.write(msg.sSourceProfile.getBytes(),0,msg.sSourceProfile.length());
                out.write(sOurProfile.getBytes(),0,sOurProfile.length());
                out.write(sOurConnectionURL.getBytes(),0,sOurConnectionURL.length());

                // Send the message content/body
                out.write(tx.getBytes(),0,tx.length());

                // Ok, now read the OK that it was received ok
                out.flush();
                out.close();

                // Pause for a short time to see if data was transmitted ok
                long lStart = System.currentTimeMillis();
                while (((System.currentTimeMillis()-lStart)<baseconf.MESSAGE_CONFIRMATION_TIMEOUT) && (in.available()<1)) {
                    try {
                        Thread.sleep(40);
                    } catch (Exception ee) {}
                 }

                byte bread = baseconf.COMMUNICATION_RXUNKNOWN;
                //#ifdef L2CAPMODE
//#                 if (in.available()>0) bread=in.read();
                //#else
                if (in.available()>0) bread=(byte)in.read();
                //#endif
                bResult = (bread==baseconf.COMMUNICATION_RXOK); 

                in.close();

                lLastConnectionOutboundTest  = System.currentTimeMillis();
             } catch (Exception e) {
                showError("transmitMessage: "+e.toString());
             }

             try {in.close();} catch (Exception e) {}
             try {out.close();} catch (Exception e) {}
             in = null;
             out = null;
             try {soc.close();} catch (Exception e) {}
             soc = null;

             return bResult;
         }

         // Run the message processing algorithm for 1 message
         public void runMessageProcessing(long iTotalTime) {
             long lStartTime = System.currentTimeMillis();
             TransmittedMessage pm;
             int iFailCounter = 0;

             iFakeCounter=0;

             try {
                 purgeFailedMessages();
                 purgeDelayQueue();
                 notifyMessageQueueSizeChange();
             } catch (Exception rrr) {
                 showError("rpm:"+rrr.getMessage());
             }

             moSystem.logDebugMessage("ms: send for "+(iTotalTime/1000)+" seconds");
             // Run for the required amount of time and that time only
             while (System.currentTimeMillis()-lStartTime<iTotalTime) {
                 if (bIsClosed) return;

                 if (devicesNotRunningMobuds!=null) return;

                 // Request a message
                 iFakeCounter=0;
                 pm = getMessageFromQueue(1000);

                 if (bIsClosed) return;

                 if (pm==null) {
                    try {Thread.sleep(50);} catch (Exception e) {}
                    iFailCounter++;
                    if (iFailCounter>10) {
                        purgeFailedMessages();
                        purgeDelayQueue();
                        notifyMessageQueueSizeChange();
                        iFailCounter=0;
                    }

                    if (System.currentTimeMillis()-lLastConnectionOutboundTest>baseconf.LAST_CONNECTIONTEST_PERIOD) {
                        prepareOutgoingMessages();
                    }
                 } else {
                     processMessage(pm);
                     notifyMessageQueueSizeChange();
                 }

                 // exit if the profile changes
                 if (bProfileChanged) {
                        moSystem.logDebugMessage("ms: profilechanged");
                        return;
                 }
             }
             moSystem.logDebugMessage("ms: done");
         }

         // Main thread
         private void processMessage(TransmittedMessage pm) {
            Vector closeBy;
            DiscoveredUsers fu;
            int i;

            if (pm.sRoute.size()>baseconf.MAX_ROUTE_REQUESTS) {
                 pm = null;
            } else {

                boolean sendNornally = false;

                // Can we send it via a MoHub?
                if (mohubServer!=null) {
                    if (transmitMessage(pm,null,mohubServer.strConnectionUrl)) {
                        pm = null;
                    } else sendNornally = true;
                } else sendNornally = true;

                if (sendNornally) {
                    // Get a list if people near to the person
                    closeBy = getUsersCloseBy(pm.sBluetoothTo);

                    if (closeBy!=null) {
                        // Ok, now we have a list of ways to contact the person, so lets try it.
                        for (i=0; i<closeBy.size(); i++)
                            if (transmitMessage(pm,(DiscoveredUsers)closeBy.elementAt(i))) {

                               if ((pm.sBluetoothTo.equals(((DiscoveredUsers)closeBy.elementAt(i)).bluetoothAddress)) && ( pm.sFromBluetooth.equals(bluetoothService.getBluetoothAddress()))) {
                                   // Dont need to wait for confirmation if relaying isnt used
                                   removeMessageWithId(pm.messageID);
                               }

                                pm = null;  // sent
                                break;
                            }
                    }
                }

                // Not good, message still here, we cant find that person
                if (pm!=null) {
                    if (System.currentTimeMillis()-pm.lQueueStartTime<baseconf.MAX_QUEUE_FAIL_TIME) {
                        // Remove it from our "outbox notification" if it fails to send.
                        removeMessageWithId(pm.messageID);
                        sendMessageToDelay(pm);
                        iFakeCounter=0;
                    }
                }
            }
        }
    }

    // Class for another thread for handling connections from people - this is blocking which is why we do this
    private class BTConnectionHandler implements Runnable {
         private Thread processorThread;

         public void startThread() {
            processorThread.start();
         }

         private BTConnectionHandler() {
             processorThread = new Thread(this);
         }

         // Handle connection from another computer.  We only handle one at a time because its easier
//#ifdef L2CAPMODE
//#          private void processAndHandleConnection(L2CAPConnection soc) {
//#              L2CapIOManager in = null;
//#              L2CapIOManager out = null;
//#else
         private void processAndHandleConnection(StreamConnection soc) {
             InputStream in = null;
             OutputStream out = null;
//#endif
             byte messageLengths[] = new byte[16];
             int messageLength[] = new int[16];
             byte data[];
             boolean quitnow=false;
             TransmittedMessage msg;
             int iLength;
             String sMsg;
             String sTransmitterProfile = "";
             String sTransmitterAddress;
             String sTransmitterConnectionUrl;

             try {
                 // All messages are in the same format
                 //#ifdef L2CAPMODE
//#                  in = new L2CapIOManager(soc,true);
//#                  out = new L2CapIOManager(soc,false);
                   //#else
                 in = soc.openInputStream();
                 out = soc.openOutputStream();
                 //#endif

                 // Read in the header
                 in.read(messageLengths);

                  // Fix byte problems with signed/unsigned numbers
                  for (int i=0; i<messageLengths.length; i++) messageLength[i]=messageLengths[i]&0xFF;

                 // Now read in the parts of data from the header
                  switch (messageLength[0]) {
                      case baseconf.COMMUNICATION_SENDMESSAGE:msg = new PersonMessage(); break;
                      default: return;
                  }

                  msg.bErrorCode = (byte)messageLength[12];
                  msg.messageID = (messageLength[8]) | (messageLength[9]<<8) | (messageLength[10]<< 16) | (messageLength[11] << 24);

                  // Get List
                  for (iLength=0; iLength<messageLength[7]; iLength++) {
                      data = new byte[in.read()];
                      in.read(data);
                      sMsg = new String(data,0,data.length);
                      msg.sRoute.addElement(sMsg);
                  }

                  // Now add to the list the remote connection
                  RemoteDevice remote = RemoteDevice.getRemoteDevice(soc);
                  sTransmitterAddress = remote.getBluetoothAddress();

                  if (!msg.sRoute.contains(remote.getBluetoothAddress())) msg.sRoute.addElement(sTransmitterAddress);
                  remote = null;

                  data = new byte[messageLength[1]]; in.read(data); msg.sBluetoothTo = new String(data,0,data.length);
                  data = new byte[messageLength[2]]; in.read(data); msg.sFromBluetooth = new String(data,0,data.length);
                  data = new byte[messageLength[3] | (messageLength[4]<<8)]; in.read(data); msg.sSourceProfile = new String(data,0,data.length);
                  data = new byte[messageLength[13] | (messageLength[14]<<8)]; in.read(data); sTransmitterProfile = new String(data,0,data.length);
                  data = new byte[messageLength[15]]; in.read(data); sTransmitterConnectionUrl = new String(data,0,data.length);

                  // Now read in the content
                  data = new byte[messageLength[5] | (messageLength[6]<<8)]; in.read(data); sMsg = new String(data,0,data.length);
                  msg.bMessageType = (byte)messageLength[0];
                  msg.fromString(sMsg);

                  in.close();

                  // Check message type
                  switch (messageLength[0]) {
                    case baseconf.COMMUNICATION_SENDMESSAGE:
                         out.write(baseconf.COMMUNICATION_RXOK);
                         break;
                    default:
                         out.write(baseconf.COMMUNICATION_RXFAIL);
                         quitnow=true;
                         break;
                   }

                  try {
                      out.flush();
                  } catch (Exception e2) {}

                   try {
                      out.close();
                      soc.close();
                   } catch (Exception e) {}

                   if (quitnow) return;

                   // send here
                   receiveMessage(msg,sTransmitterAddress.equals(msg.sFromBluetooth));

                   // The transmitter person may actually be someone we havent discovered yet.  Lets add them for our sake
                   if ((sTransmitterProfile.length()<1) && (sTransmitterAddress.equals(msg.sFromBluetooth))) sTransmitterProfile = msg.sSourceProfile; else sTransmitterProfile="";

                   // Does it exist then?
                   if (sTransmitterProfile.length()>0) {
                       synchronized (sync_UserListSaver) {
                           DiscoveredUsers du = (DiscoveredUsers)userList.get(sTransmitterAddress);
                           if (du!=null) {
                               // User exists, lets make sure the connection url is up to date as it may have changed since last scan
                               du.sConnectionUrl = sTransmitterConnectionUrl;
                               // And we have seen it recently
                               du.lastDiscovererd = getTimeSeconds();

                               // And it must be near-by
                               du.distance = 0;
                           } else {
                               du = new DiscoveredUsers();
                               du.bluetoothAddress = sTransmitterAddress;
                               du.distance = 0;  // well, if they're close enough to see us, then we must be able to see them
                               du.iProfileVersion = getVersionFromProfileString(sTransmitterProfile);
                               du.lastDiscovererd = getTimeSeconds();
                               du.sConnectionUrl = sTransmitterConnectionUrl;
                               du.userProfile = sTransmitterProfile;

                               // Save in the list
                               userList.put(sTransmitterAddress,du);
                               refreshScreen();
                           }
                       }
                   }
               } catch (Exception em) {}

             // just make sure
             try {in.close(); } catch (Exception e) {}
             try {out.close(); } catch (Exception e) {}
             in = null;
             out = null;
             try {soc.close(); } catch (Exception e) {}
         }

         public void run() {
                   while (!bIsClosed) {
                        try {
                            //#ifdef L2CAPMODE
//#                             L2CAPConnection conn = null;
                            //#else
                             StreamConnection conn = null;
                            //#endif
                            if (newConnNotify!=null) {
                                showError(System.currentTimeMillis()+" acceptAndOpen() Ready");
                                conn = newConnNotify.acceptAndOpen();
                            }

                            if (conn!=null) {
                                try {
                                  //  PersonMessage msg;
                                    processAndHandleConnection(conn);
                                    //msg = new PersonMessage();
                                    //msg.bMessageType = baseconf.COMMUNICATION_SENDMESSAGE;
                                    //msg.hasBeenRead=false;
                                    //msg.hasReplied=false;
                                    //msg.lTime=System.currentTimeMillis();
                                    //msg.messageID=(int)System.currentTimeMillis();
                                    //msg.sBluetoothTo=bluetoothService.getBluetoothAddress();
                                    //msg.sFromBluetooth=bluetoothService.getBluetoothAddress();
                                    //msg.sMessage="Hello World "+(int)System.currentTimeMillis();
                                    //msg.sSourceProfile = moSystem.persData.myProfile.saveToString(true);
                                    //msg.bErrorCode  = 0;
                                    //receiveMessage(msg,false);
                                    //moSystem.pause(10000);

                                } catch (Exception moo) {}
                                try {
                                    conn.close();
                                } catch (Exception ee) {}
                                conn = null;
                                notifyMessageQueueSizeChange();
                            }
                        } catch (Exception e) {
                               showError(System.currentTimeMillis()+" ERROR "+e.getMessage());
                        }

                        if  ((newConnNotify==null) && (!bIsClosed)) {
                            while ((newConnNotify==null) && (!bIsClosed)) {
                                try {Thread.yield(); Thread.sleep(50); } catch (Exception e) {}
                            }
                        }
                   }
         }
    }
}
