/*
 * AlertProfile.java
 *
 * Created on 09 January 2007, 21:33
 *
 * Used to store details about an alert
 */

package mobuds;

/**
 *
 * @author rob smith
 */
public class AlertProfile {    
    // This class manages loading and saving an alert profile
    public static final int MAX_ALERT_NAME = 30;
    
    // Name of the alert
    public String sAlertName;
    
    // Gender of the person (just indexes onto the form)
    public byte bGender;
    
    // Age mode
    public byte bAgeMode;
    
    // Age ranges
    public byte bMinAge,bMaxAge;
    
    // Status of job
    public byte bJobStatus;
    
    // Alcohol status
    public byte bDrinks;

    // Smoking status
    public byte bSmokes;
    
    // Wears glasses
    public byte bWearsGlasses;
    
    // Has facial hair
    public byte bFacialHair;

    // Height
    public byte bHeight;

    // Body Type
    public byte bBody;

    // Eye Colour
    public byte bEyes;

    // Hair Colour
    public byte bHair;
    
    // Marital Status
    public byte bMarriedMode;
    
    // Sexuality
    public byte bSexuality;
    
    // Social Interest Mode
    public byte bLookingFor;

    // Social Interest Mode
    public byte bLookingForWith;
    
    
    // Return the class as a string
    public String toString() {
        StringBuffer sResult = new StringBuffer(17+sAlertName.length());
        sResult.append((char)bGender);
        sResult.append((char)bAgeMode);
        sResult.append((char)bMinAge);
        sResult.append((char)bMaxAge);
        sResult.append((char)bJobStatus);
        sResult.append((char)bDrinks);
        sResult.append((char)bSmokes);
        sResult.append((char)bHeight);
        sResult.append((char)bWearsGlasses);
        sResult.append((char)bFacialHair);
        sResult.append((char)bBody);
        sResult.append((char)bEyes);
        sResult.append((char)bHair);
        sResult.append((char)bMarriedMode);
        sResult.append((char)bSexuality);
        sResult.append((char)bLookingFor);
        sResult.append((char)bLookingForWith);
        sResult.append((char)((byte)(sAlertName.length())));
        sResult.append(sAlertName);
        
        return sResult.toString();        
    }
    
    // Load the class data FROM a string
    public void fromString(String s) {
        if (s.length()<17) return;
        bGender = (byte)s.charAt(0);
        bAgeMode = (byte)s.charAt(1);
        bMinAge = (byte)s.charAt(2);
        bMaxAge = (byte)s.charAt(3);
        bJobStatus = (byte)s.charAt(4);
        bDrinks = (byte)s.charAt(5);
        bSmokes = (byte)s.charAt(6);
        bHeight = (byte)s.charAt(7);
        bWearsGlasses = (byte)s.charAt(8);
        bFacialHair = (byte)s.charAt(9);
        bBody = (byte)s.charAt(10);
        bEyes = (byte)s.charAt(11);
        bHair = (byte)s.charAt(12);
        bMarriedMode = (byte)s.charAt(13);
        bSexuality = (byte)s.charAt(14);
        bLookingFor = (byte)s.charAt(15);
        bLookingForWith = (byte)s.charAt(16);
        int strLen = (byte)s.charAt(17);
        try {
            sAlertName = s.substring(18,18+strLen);
        } catch (Exception e) {};
    }
    
    
    /** Creates a new instance of AlertProfile */
    public AlertProfile(int defaultIndex) {
        sAlertName = "Alert "+defaultIndex;
        bMinAge = MobudsProfile.MINIMUM_AGE;
        bMaxAge = MobudsProfile.MAXIMUM_AGE;
        bGender = (byte)0xFF;
        bAgeMode = 0;
        bJobStatus = (byte)0xFF;
        bDrinks = (byte)0xFF;
        bSmokes = (byte)0xFF;
        bHeight = (byte)0xFF;
        bWearsGlasses = 0;
        bFacialHair = 0;
        bBody = (byte)0xFF;
        bEyes = (byte)0xFF;
        bHair = (byte)0xFF;
        bMarriedMode = (byte)0xFF;
        bSexuality = (byte)0xFF;
        bLookingFor = (byte)0xFF;
        bLookingForWith = (byte)0xFF;
    }
    
}
