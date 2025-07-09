/*
 * PersonMessage.java
 *
 * Created on 28 December 2006, 20:54
 *
 * Used to send a messahe to a person
 */

package mobuds;


/**
 *
 * @author rob smith
 */

public class PersonMessage extends TransmittedMessage {
    
    // Actual message to send
    public String sMessage;
    
    // Date/time the message was sent
    public long lTime;
    
    // Had the message been replied to?
    public boolean hasReplied;
    
    // Has the message been read?
    public boolean hasBeenRead;

    // A unique Message Identifier
    public String getUniqueID() {
        return this.messageID+'|'+this.sFromBluetooth;
    }
    
    // Get a string of the above
    public String toString() {
        return lTime+","+sMessage;
    }
    
    // Reverse the above
    public void fromString(String s) {
        int iPos = s.indexOf(",");
        lTime = Long.parseLong(s.substring(0,iPos));
        sMessage = s.substring(iPos+1);
    }
    
    // Save to saving string
    public String forSaving() {
        int code = bErrorCode<<2;
        if (hasReplied) code|=1;
        if (hasBeenRead) code|=2;
        return MobudsProfile.MYBASE64.charAt(code&63)+baseconf.intToMyBase64(messageID)+","+sFromBluetooth+","+sSourceProfile+toString();
    }


    // Load back from saving
    public void fromSaving(String s) {
        int a,code;
        if (s.length()<14) return;

        code = MobudsProfile.MYBASE64.indexOf(s.substring(0,1));
        hasReplied = (code&1)!=0;
        hasBeenRead = (code&2)!=0;
        bErrorCode = (byte)(code>>2);

        s=s.substring(1);
        a = s.indexOf(",");
        messageID = baseconf.myBase64ToInt(s.substring(0,a));   s = s.substring(a+1);  a = s.indexOf(",");
        sFromBluetooth = s.substring(0,a);   s = s.substring(a+1);  a = s.indexOf(MobudsProfile.PROFILE_END_MARKER);
        sSourceProfile = s.substring(0,a+1); s = s.substring(a+1);
        fromString(s);
    }












    // Save to saving string
    public String forPasvSaving() {
        String st = ""+MobudsProfile.MYBASE64.charAt(bMessageType&63)+MobudsProfile.MYBASE64.charAt(sRoute.size()&63);
        st+=sBluetoothTo+",";
        st+=baseconf.intToMyBase64(messageID)+","+sFromBluetooth+","+sSourceProfile;
        st+=toString();
        return st;
    }


    // Load back from saving
    public void fromPasvSaving(String s) {
        int a;
        if (s.length()<14) return;
        receivedPasv = true;

        bMessageType = (byte)MobudsProfile.MYBASE64.indexOf(s.substring(0,1));
        a = (byte)MobudsProfile.MYBASE64.indexOf(s.substring(1,1));
        while (sRoute.size()<=a) sRoute.addElement("");  // ensure we know the counter! (+1!)
        s = s.substring(2);
        a = s.indexOf(",");
        sBluetoothTo = s.substring(0,a); s = s.substring(a+1); a = s.indexOf(",");
        messageID = baseconf.myBase64ToInt(s.substring(0,a));   s = s.substring(a+1);  a = s.indexOf(",");
        sFromBluetooth = s.substring(0,a);   s = s.substring(a+1);  a = s.indexOf(MobudsProfile.PROFILE_END_MARKER);
        sSourceProfile = s.substring(0,a+1); s = s.substring(a+1);
        fromString(s);
    }


    
    /** Creates a new instance of PersonMessage */
    public PersonMessage() {
    }
    
}
