/*
 * TransmittedMessage.java
 *
 * Created on 29 December 2006, 18:05
 *
 * Base class for all types of message
 */

package mobuds;


import java.util.*;

/**
 *
 * @author rob smith
 */
abstract class TransmittedMessage {

    public boolean receivedPasv;

    // Retry counter
    public long lQueueStartTime;
    
    // Type of message
    public byte bMessageType;       
    
    // Target bluetooth device
    public String sBluetoothTo;
    
    // Who its from
    public String sSourceProfile;
    
    // Bluetooth address of sender
    public String sFromBluetooth;
    
    // Route the message went, to prevent back-tracking
    public Vector sRoute;
        
    // Unique ID of the message, well, more of a timestamp really
    public int messageID;
    
    // An error code, or 0 for ok, max value is 15
    public byte bErrorCode;
    
    // Date the message was put into the queue
    public long queueDate;   
               
    // Function that must be used - gets the message as a string
    abstract public String toString();
    
    // must exist, reverse the above
    abstract public void fromString(String s);
    
    /** Creates a new instance of TransmittedMessage */
    public TransmittedMessage() {
        sRoute = new Vector();
        Date d = new Date();
        messageID = (int)d.getTime();
        
        lQueueStartTime = System.currentTimeMillis();
        queueDate = d.getTime();
    }
    
}
