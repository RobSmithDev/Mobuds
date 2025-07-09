/*
 * DiscoveryDevices.java
 *
 * Created on 16 December 2006, 21:09
 *
 * Used to store devices after they have been discovered
 */

package mobuds;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 *
 * @author rob smith
 */
public class DiscoveryDevice {

    // has the scan been completed?
    public boolean completed;

    // Transaction ID
    public int transactionID;
    
    // List of services on this device
    public ServiceRecord servRecord;
        
    // Details about the device
    public RemoteDevice remoteDevice;
   
    
    /** Creates a new instance of DiscoveryDevices */
    public DiscoveryDevice() {
           transactionID=-1;
    }
    
}