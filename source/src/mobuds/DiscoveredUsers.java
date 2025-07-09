/*
 * DiscoveredUsers.java
 *
 * Created on 16 December 2006, 21:47
 *
 * Used to store details about a user that has been discovered
 */

package mobuds;

import java.util.*;

/**
 *
 * @author rob smith
 */
public class DiscoveredUsers {
    
    // Profile data for the user we have discovered
    public String userProfile; 
    
    // Persons name, used for SORTING ONLY
    public String txtPersonName;
    
    // When the profile was last discovered
    public long lastDiscovererd;
    
    // The Bluetooth device owening this profile
    public String bluetoothAddress;
    
    // The distance the person is from the phone (based on phone relay)
    public int distance;    
    
    // temp storage
    public int lastDistance;
    
    // Url to connect to this user
    public String sConnectionUrl;
    
    // Profile version (as advertised)
    public int iProfileVersion;
    
    // A list of users that can see me
    public Vector vUsersThatSeeMe;
    
    // Compares the profile name
    public int compareTo(DiscoveredUsers user) {
        return txtPersonName.compareTo(user.txtPersonName);
    }

    // Makes a copy of this - public copy doesnt need everything.
    public DiscoveredUsers copyMe() {
        DiscoveredUsers d = new DiscoveredUsers();
        d.userProfile = userProfile;
        d.lastDiscovererd = lastDiscovererd;
        d.bluetoothAddress = bluetoothAddress;
        d.distance = distance;
        
        return d;
    }
    
    // Makes a copy of every entry of this 
    public DiscoveredUsers copyMeComplete() {
        DiscoveredUsers d = new DiscoveredUsers();
        d.userProfile = userProfile;
        d.lastDiscovererd = lastDiscovererd;
        d.bluetoothAddress = bluetoothAddress;
        d.distance = distance;
        d.sConnectionUrl = sConnectionUrl;
        d.iProfileVersion = iProfileVersion;
        
        return d;
    }    
    
    // return a user as a nice profile string
    public String getUserAsString() {
        return getUserAsString(0);
    }   

    // return a user as a nice profile string
    public String getUserAsString(int iDistanceAddon) {
        String s = bluetoothAddress+","+(distance+iDistanceAddon)+","+iProfileVersion+","+userProfile;
        return baseconf.xorChecksum(s)+","+s;
    }   
    
    // gets a user from a string formed from getUserAsString()
    public boolean getUserFromString(String s) {
           boolean bError = false;
           int iComma = s.indexOf(",");           
           String checksum = s.substring(0,iComma);           
           s = s.substring(iComma+1);           
           // validate - means if someone wants to rip this off and make it compatable they must xor with our secret phrase
           if (checksum.compareTo(baseconf.xorChecksum(s))!=0) return false;
           iComma = s.indexOf(",");           
           bluetoothAddress = s.substring(0,iComma);
           s = s.substring(iComma+1);
           iComma = s.indexOf(",");
           
           try { distance = Integer.parseInt(s.substring(0,iComma));   // whatever they are published as
                 s = s.substring(iComma+1);
                 iComma = s.indexOf(",");
                 iProfileVersion = Integer.parseInt(s.substring(0,iComma));   // profile version number
                 userProfile = s.substring(iComma+1);
           } catch (Exception e) { bError = true;};
           
           return (!bError);
    }
    
    /** Creates a new instance of DiscoveredUser */
    public DiscoveredUsers() {
        vUsersThatSeeMe = new Vector();
    }
    
}
