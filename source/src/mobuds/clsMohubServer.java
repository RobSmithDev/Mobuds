/*
 * clsMohubServer.java
 *
 * Created on 06 January 2009, 20:57
 *
 * Definition about a mohub server
 */

package mobuds;



/**
 *
 * @author rob smith
 */
public class clsMohubServer {
    
    public static final char MOHUB_PROFILE_START = '[';
    public static final char MOHUB_PROFILE_STOP = ']';
    
    public String strMohubName;
    public String strMohubURL;
    public String strBluetoothAddress;
    public String strConnectionUrl;
    public int mohubID;
    public int lng,lat;
    public long lastUse;
    public int accuracy;  // accuracy in meters of the long and lat
    
    public boolean isExpired() {
        return ((System.currentTimeMillis()/1000)-lastUse>baseconf.MAX_USER_TIMEOUT);
    }
    
    // publish a location from another source.  Used if this can't
    public void publishOtherLocation(String remoteLocation) {
        int a;
        if (remoteLocation.length()<4) return;
        a = remoteLocation.indexOf(','); lat = baseconf.myBase64ToInt(remoteLocation.substring(0,a));    remoteLocation = remoteLocation.substring(a+1);
        a = remoteLocation.indexOf(','); lng = baseconf.myBase64ToInt(remoteLocation.substring(0,a));    remoteLocation = remoteLocation.substring(a+1);
        a = remoteLocation.indexOf(','); accuracy = baseconf.myBase64ToInt(remoteLocation.substring(0,a));    remoteLocation = remoteLocation.substring(a+1);
        if (remoteLocation.charAt(0)=='1') {
            mohubID=(int)accuracy;
            accuracy=1;
        } else {
            mohubID=0;
        }
        remoteLocation = remoteLocation.substring(1);
        lastUse = baseconf.myBase64ToLong(remoteLocation);
    }

    
    // Format is [Mohub Name,Mohub URL,Mobud ID,Longitude,Latidude,Checksum]
    //       EG: [Mohub Name,Mohub URL,10,20,30,10411912097107106105107103107107]
    public boolean updateProfile(String sConnectionURL, String sBluetoothAddress, String sProfile) {
        int iPos;
        String tmp;
        String strNewUrl,strNewHubName;
        int newlng,newlat;
        int newid;
                
        accuracy=1;
        
        if (!sProfile.startsWith(MOHUB_PROFILE_START+"")) return false;   sProfile=sProfile.substring(1);
        if (!sProfile.endsWith(MOHUB_PROFILE_STOP+"")) return false;   sProfile=sProfile.substring(0,sProfile.length()-1);
        
        lastUse = System.currentTimeMillis()/1000;

        // Get the mohub name
        iPos = sProfile.indexOf(",");
        if (iPos<0) return false;
        strNewHubName = sProfile.substring(0,iPos);
        sProfile = sProfile.substring(iPos+1);

        // Get the mohub URL
        iPos = sProfile.indexOf(",");
        if (iPos<0) return false;
        strNewUrl = sProfile.substring(0,iPos);
        sProfile = sProfile.substring(iPos+1);

        // Get the mohub ID
        iPos = sProfile.indexOf(",");
        if (iPos<0) return false;
        newid = Integer.parseInt(sProfile.substring(0,iPos));
        sProfile = sProfile.substring(iPos+1);

        // Get the longitude
        iPos = sProfile.indexOf(",");
        if (iPos<0) return false;
        newlng = Integer.parseInt(sProfile.substring(0,iPos));
        sProfile = sProfile.substring(iPos+1);

        // get the latitude
        iPos = sProfile.indexOf(",");
        if (iPos<0) return false;
        newlat = Integer.parseInt(sProfile.substring(0,iPos));
        sProfile = sProfile.substring(iPos+1);

        // whats left is a checksum
        tmp = baseconf.xorChecksum(sBluetoothAddress+newid+strNewUrl+newlng+strNewHubName+newlat);
        if (tmp.compareTo(sProfile)!=0) return false;

        strMohubName = strNewHubName;
        strConnectionUrl = sConnectionURL;
        strBluetoothAddress = sBluetoothAddress;  
        strMohubURL = strNewUrl;
        lng = newlng;
        lat = newlat;
        mohubID = newid;
        
        return true;
    }    
}
