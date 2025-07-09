/*
 * MobudsProfile.java
 *
 * Created on 10 December 2006, 13:23
 *
 * A single profile
 */

package mobuds;


import java.lang.Math.*;

/*
 * @author rob smith
 */
public class MobudsProfile {
    public static final String MYBASE64 = "AaB1bCc2DdE3eFf4GgH5hIi6JjK7kLl8MmN9nOo0PpQqRrSsT[tUuVvWwXxYy]Zz"; 
     
    public static final char PROFILE_START_MARKER  = '{';
    public static final char PROFILE_END_MARKER    = '}';
    
    /* Some constants to be used with the profile */
    public static final boolean GENDER_MALE       = true;
    public static final boolean GENDER_FEMALE     = false;
    
    public static final byte     MINIMUM_AGE = 14;  // Minimum age
    public static final byte     MAXIMUM_AGE = 120;
    
    public static final byte     ORIENTATION_HETRO   = 0;       // Mini-Kinsey Scale
    public static final byte     ORIENTATION_BI      = 2;       // Mini-Kinsey Scale
    public static final byte     ORIENTATION_HOMO    = 4;       // Mini-Kinsey Scale
    public static final byte     ORIENTATION_CURIOUS = 5;       // Curious
    public static final byte     ORIENTATION_UNSURE  = 6;       // Not Sure
    public static final byte     ORIENTATION_SEXLESS = 7;       // Sexless
    
    public static final byte     OPTIONS_NO        = 0;       // User never does this
    public static final byte     OPTIONS_SOMETIMES = 1;       // User sometimes does this
    public static final byte     OPTIONS_SOCIALLY  = 2;       // User only does this socially
    public static final byte     OPTIONS_ALWAYS    = 3;       // User always does this
    
    public static final byte     STATUS_SINGLE         = 0;   // Is Single
    public static final byte     STATUS_DATING         = 1;   // IS dating
    public static final byte     STATUS_RELATIONSHIP   = 2;   // IS In Relationship
    public static final byte     STATUS_MARRIED        = 3;   // Is Married/Civ Partnetship
    public static final byte     STATUS_COMMITTED      = 4;   // Other kind of committed relationship
    public static final byte     STATUS_DIVORCED       = 5;   // Is Divorced
    public static final byte     STATUS_OPEN           = 6;   // Is in an Open Relationship
        
    public static final byte     LOOKINGFOR_FRIENDS         = 1;    // User is looking for friends
    public static final byte     LOOKINGFOR_RELATIONSHIPS   = 2;    // User is looking for serious relationships
    public static final byte     LOOKINGFOR_DATING          = 4;    // User is looking for dating
    public static final byte     LOOKINGFOR_NETWORKING      = 8;    // User is looking for networking
    public static final byte     LOOKINGFOR_FUN             = 16;   // User is looking for fun
    public static final byte     LOOKINGFOR_RELIGION        = 32;   // User is looking for religion
    
    public static final byte     LOOKINGFORG_MALE           = 1;    // User is looking for men
    public static final byte     LOOKINGFORG_FEMALE         = 2;    // User is looking for women
    
    public static final byte     PROFESSION_SCHOOL          = 0;   // User is at school
    public static final byte     PROFESSION_COLLEGE         = 1;   // User is at college
    public static final byte     PROFESSION_UNIVERSITY      = 2;   // User is at university
    public static final byte     PROFESSION_UNEMPLOYED      = 3;   // User is unemployed
    public static final byte     PROFESSION_PARTTIME        = 4;   // User has a part time job
    public static final byte     PROFESSION_FULLTIME        = 5;   // User has a full time job 

    /* Eye Colour Constants */
    public static final byte     EYECOL_BLACK        = 0;  
    public static final byte     EYECOL_BROWN        = 1;  
    public static final byte     EYECOL_BLUE         = 2;  
    public static final byte     EYECOL_GRAY         = 3;  
    public static final byte     EYECOL_GREEN        = 4;  
    public static final byte     EYECOL_HAZEL        = 5;  
    
    /* Hair Colour Constants */
    public static final byte     HAIRCOL_BLACK       = 0;
    public static final byte     HAIRCOL_DARKBROWN   = 1;
    public static final byte     HAIRCOL_LIGHTBROWN  = 2;
    public static final byte     HAIRCOL_BLONDE      = 3;
    public static final byte     HAIRCOL_GRAY        = 4;
    public static final byte     HAIRCOL_RED         = 5;
    public static final byte     HAIRCOL_BALD        = 6;
    public static final byte     HAIRCOL_CHANGES     = 7;
    
    /* Height */
    public static final byte     HEIGHT_PETITE       = 0;
    public static final byte     HEIGHT_AVERAGE      = 1;
    public static final byte     HEIGHT_TALL         = 2;
    public static final byte     HEIGHT_VERYTALL     = 3;
    
    /* Body Type Constants */
    public static final byte     BUILD_SLIM          = 0;
    public static final byte     BUILD_SLENDER       = 1;
    public static final byte     BUILD_ATHLETIC      = 2;
    public static final byte     BUILD_FIT           = 3;
    public static final byte     BUILD_AVERAGE       = 4;
    public static final byte     BUILD_BIGGER        = 5;
    public static final byte     BUILD_LARGE         = 6;
    public static final byte     BUILD_VOLUPTUOUS    = 7;
    
    // Visibility Usage
    public static final int     VISIBLE_GENDER      = 0x00001;   // Gender Visibility
    public static final int     VISIBLE_DOB         = 0x00002;   // DOB Visibility
    public static final int     VISIBLE_PROFESSION  = 0x00004;  // Profession Visibility

    public static final int     VISIBLE_DRINKER     = 0x00008;   // drinker Visibility
    public static final int     VISIBLE_SMOKER      = 0x00010;   // smoker Visibility
    public static final int     VISIBLE_GLASSES     = 0x00020;   // glasses visibility

    public static final int     VISIBLE_HEIGHT      = 0x00040;  // Height Visibility    
    public static final int     VISIBLE_BODYTYPE    = 0x00080;  // Body Type Visibility
    public static final int     VISIBLE_EYECOL      = 0x00100;  // Eye Colour Visibility
    public static final int     VISIBLE_HAIRCOL     = 0x00200;  // Hair Colour Visibility

    public static final int     VISIBLE_STATUS      = 0x00400;   // Status Visibility
    public static final int     VISIBLE_ORIENTATION = 0x00800;   // Orientation Visibility
    public static final int     VISIBLE_LOOKING     = 0x01000;   // Looking Visibility
    public static final int     VISIBLE_LOOKINGFOR  = 0x02000;   // Looking For Visibility
    
    public static final int     VISIBLE_URL         = 0x04000;  // URL Visibility
    public static final int     VISIBLE_MESSAGE     = 0x08000;  // Comment Visibility
    
    public static final int     VISIBLE_NUMBER      = 0x10000;  // Cell number visible
    public static final int     VISIBLE_FACEHAIR    = 0x20000;   // face hair visibility
                
    /* Restrictions */
    public static final byte     MAX_NICKNAME_LENGTH     = 20;  // we can allow these to be 63
    public static final byte     MAX_URL_LENGTH          = 60;  // we can allow these to be 63
    public static final byte     MAX_DESCRIPTION_LENGTH  = 40;  // we can allow these to be 63
    public static final byte     MAX_PHONE_LENGTH     = 20;
    
    /* Profile details - all gets packed into a small space */
    public boolean bGender;                 // * See GENDER_* static finals
    public byte    bMonth;                  // * Birthday Month
    public byte    bDay;                    // * Birthday Day
    public int     iYear;                   // * Birthday Year
    public byte    iSexualOrientation;      // * See ORIENTATION_* static finals
    public byte    iStatus;                 // * See STATUS_* static finals
    public byte    iLookingFor;             // * See BITMASK of LOOKINGFOR_* static finals
    public byte    iLookingForGender;       // * See BITMASK of LOOKINGFORG_* static finals
    public byte    iSmoker;                 // * The user smokes - see OPTIONS_*
    public byte    iDrinker;                // * The user drinks - see OPTIONS_*
    public byte    iProfession;             // * What the user does - see PROFESSION_* 
    public byte    iEyeColor;               // * Eye Colour - See EYECOL_*
    public byte    iHairColor;              // * Hair Colour - See HAIRCOL_*
    public byte    iBodyType;               // * Body type - see BUILD_*
    public byte    iHeight;                 // * Persons Height
    public boolean bWearsGlasses;           // Person wears glasses
    public boolean bFacialHair;             // Person has facial hair
    public String  strNickName;             // Nick Name
    public String  strURL;                  // URL for the person, possibly a MySpace URL
    public String  strDescription;          // A description
    public int     iProfileVersion;             // Current save counter 
    public byte    bCycleValue;             // Profile cycle save value (used to keep a profile alive)
    public String  strMobileNumber;         // Mobile number
    public byte    bStarsign;               // Starsign index for this person.  No age, but starsign.
    
    /* Profile Visibility/Valid Fields */
    public int lVisibility;             // Visible/Enabled Fields - See VISIBLE_* constants - MAX of 3 BYTES
    
    public boolean isHabbitMatch(byte alertMode, byte bField) {
        if ((alertMode==1) && (bField==0)) return true;
        if ((alertMode==2) && (bField>0)) return true;
        return false;
    }

    // Get the age of this person
    public int getAge() {
        return baseconf.getAge(bDay,bMonth,iYear);
    }
   
    
    // Returns a number that is one less than a bitmask with all elements on
    private int getMaxValue(Object[] ob) {
        return (1<<ob.length)-1;
    }
    
    // Returns TRUE if this profile matches the alert one
    public boolean matchesAlertProfile(AlertProfile alert) {
         // Name of the alert
        int iPossibleMatches = 0;
        
        // Gender match
        if (((lVisibility&VISIBLE_GENDER)!=0)&&(alert.bGender>0)&&(alert.bGender<3)) {
            iPossibleMatches++;
            if ((bGender==GENDER_MALE) && (alert.bGender&1)==0) return false;
            if ((bGender==GENDER_FEMALE) && (alert.bGender&2)==0) return false;
        }

        // Glasses Match
        if ((alert.bWearsGlasses>0)&&((lVisibility&VISIBLE_GLASSES)!=0)) {
            iPossibleMatches++;
            if ((alert.bWearsGlasses+0)!=((bWearsGlasses==true)?2:1)) return false;
        }

        // Facial Hair Match
        if ((alert.bFacialHair>0)&&((lVisibility&VISIBLE_FACEHAIR)!=0)) {
            iPossibleMatches++;
            if ((alert.bFacialHair+0)!=((bFacialHair==true)?2:1)) return false;
        }
        
        // Age Match
        if ((alert.bAgeMode>0)&&((lVisibility&VISIBLE_DOB)!=0))  {
            iPossibleMatches++;
            
            // Work out their age to do the calculation
            int years = getAge();
            if ((years<alert.bMinAge) || (years>alert.bMaxAge)) return false;        
        }
        
        // Job status match
        if ((alert.bJobStatus>0)&&(alert.bJobStatus<getMaxValue(baseconf.professionList))&&((lVisibility&VISIBLE_PROFESSION)!=0)) {
            iPossibleMatches++;
            if (((1<<iProfession)&alert.bJobStatus)==0) return false;
        }
        
        // Drinking Status
        if ((alert.bDrinks>0)&&(alert.bDrinks<getMaxValue(baseconf.habbitTypes))&&((lVisibility&VISIBLE_DRINKER)!=0)) {
            iPossibleMatches++;
            if (((1<<iDrinker)&alert.bDrinks)==0) return false;
        }
        
        // Smoking Status
        if ((alert.bSmokes>0)&&(alert.bSmokes<getMaxValue(baseconf.habbitTypes))&&((lVisibility&VISIBLE_SMOKER)!=0)) {
            iPossibleMatches++;
            if (((1<<iSmoker)&alert.bSmokes)==0) return false;
        }

        // Height Status
        if ((alert.bHeight>0)&&(alert.bHeight<getMaxValue(baseconf.heightTypes))&&((lVisibility&VISIBLE_HEIGHT)!=0)) {
            iPossibleMatches++;
            if (((1<<iHeight)&alert.bHeight)==0) return false;
        }
        // Body Status
        if ((alert.bBody>0)&&(alert.bBody<getMaxValue(baseconf.bodyTypes))&&((lVisibility&VISIBLE_BODYTYPE)!=0)) {
            iPossibleMatches++;
            if (((1<<iBodyType)&alert.bBody)==0) return false;
        }
        
        // Eye Colour
        if ((alert.bEyes>0)&&(alert.bEyes<getMaxValue(baseconf.eyeColours))&&((lVisibility&VISIBLE_EYECOL)!=0)) {
            iPossibleMatches++;
            if (((1<<iEyeColor)&alert.bEyes)==0) return false;
        }
        
        // Hair Colour
        if ((alert.bHair>0)&&(alert.bHair<getMaxValue(baseconf.hairColours))&&((lVisibility&VISIBLE_HAIRCOL)!=0)) {
            iPossibleMatches++;
            if (((1<<iHairColor)&alert.bHair)==0) return false;
        }
        
        // Marital Status
        if ((alert.bMarriedMode>0)&&(alert.bMarriedMode<getMaxValue(baseconf.statusList))&&((lVisibility&VISIBLE_STATUS)!=0)) {
            iPossibleMatches++;
            if (((1<<iStatus)&alert.bMarriedMode)==0) return false;
        }
        
        // Sexuality
        if ((alert.bSexuality>0)&&(alert.bSexuality<getMaxValue(baseconf.orientationList))&&((lVisibility&VISIBLE_ORIENTATION)!=0)) {
            iPossibleMatches++;
            if (((1<<iSexualOrientation)&alert.bSexuality)==0) return false;
        }

        // Networking Mode
        if ((alert.bLookingFor>0)&&(alert.bLookingFor<getMaxValue(baseconf.lookingForList))&&((lVisibility&VISIBLE_LOOKING)!=0)) {
            iPossibleMatches++;
            if (((1<<iLookingFor)&alert.bLookingFor)==0) return false;
        }

        // Networking With
        if ((alert.bLookingForWith>0)&&(alert.bLookingForWith<getMaxValue(baseconf.strGenders))&&((lVisibility&VISIBLE_LOOKINGFOR)!=0)) {
            iPossibleMatches++;
            if (((1<<iLookingForGender)&alert.bLookingForWith)==0) return false;
        }

        // basically, if we get to here, then it matches, and only if at least one field was actually valid!
        return iPossibleMatches>0;
    }
    
    // Creates a new instance of MobudsProfile
    public MobudsProfile() {
        bMonth = 0;
        bDay = 1;
        iYear = 1988;
        lVisibility = VISIBLE_GENDER | VISIBLE_DOB | VISIBLE_PROFESSION | VISIBLE_MESSAGE | VISIBLE_URL |
                      VISIBLE_DRINKER | VISIBLE_SMOKER | VISIBLE_GLASSES | VISIBLE_HEIGHT | VISIBLE_BODYTYPE |
                      VISIBLE_EYECOL | VISIBLE_HAIRCOL | VISIBLE_STATUS | VISIBLE_ORIENTATION | VISIBLE_LOOKING |
                      VISIBLE_LOOKINGFOR | VISIBLE_URL | VISIBLE_NUMBER | VISIBLE_FACEHAIR;
        
        strNickName = new String();
        strURL = new String("http://");
        strDescription = new String();
        strMobileNumber = new String();
        iProfileVersion = 0;
    }    
    
    // Returns data, only if it matches the mask
    private byte maskOff(byte data, boolean bPublic, int maskField) {
        if (((lVisibility&maskField)!=0)||(!bPublic)) return data; else return 0;
    }
    
    // As below, but we know the last parameter
    public String saveToString(boolean bPublic) {
        return saveToString(bPublic,false);
    }
    
    /* For getting the profile as a string - in public mode, only visible fields are exported */
    public String saveToString(boolean bPublic, boolean bCuroffExtras) {
        String sResult;
        byte year = (byte)(iYear-1900);
        byte bb;
                
        // Trim and ensure everything meets the maximum requirements
        strNickName.replace(PROFILE_START_MARKER,'(');    strNickName.replace(PROFILE_END_MARKER,')');
        strURL.replace(PROFILE_START_MARKER,'(');    strURL.replace(PROFILE_END_MARKER,')');
        strDescription.replace(PROFILE_START_MARKER,'(');    strDescription.replace(PROFILE_END_MARKER,')');
        if (strNickName.length()>MAX_NICKNAME_LENGTH) strNickName=strNickName.substring(0,MAX_NICKNAME_LENGTH-1);
        if (strURL.length()>MAX_URL_LENGTH) strURL=strURL.substring(0,MAX_URL_LENGTH-1);
        if (strDescription.length()>MAX_DESCRIPTION_LENGTH) strDescription=strDescription.substring(0,MAX_DESCRIPTION_LENGTH-1);
        iSexualOrientation&=7;
        iStatus&=7;
        iSmoker&=3;
        iLookingForGender&=7;
        iDrinker&=3;
        iProfession&=7;
        iEyeColor&=7;
        iHairColor&=7;
        iBodyType&=7;
        iLookingFor&=63;
        iHeight&=3;
        
        // Needs to be packed into blocks of 6-Bit, easier later on!
        sResult=PROFILE_START_MARKER+"";  
        if (!bCuroffExtras) {
             sResult+=MYBASE64.charAt(bCycleValue&63);
             sResult+=MYBASE64.charAt(iProfileVersion & 63);
             sResult+=MYBASE64.charAt(((iProfileVersion>>6)&63));
             sResult+=MYBASE64.charAt(((iProfileVersion>>12)&63));
             sResult+=MYBASE64.charAt(((iProfileVersion>>18)&63)); 
        }
        sResult+=MYBASE64.charAt(bStarsign&0xF);    // hmmm, again!
        
        //      -----3 Bits-------   --- 3 Bits -- 
        sResult+=MYBASE64.charAt(maskOff(iSexualOrientation,bPublic,VISIBLE_ORIENTATION)| (maskOff(iStatus,bPublic,VISIBLE_STATUS) << 3));
        //      -- 3 Bits -   ---- 3 Bits ---  
        sResult+=MYBASE64.charAt(maskOff(iProfession,bPublic,VISIBLE_PROFESSION) | (maskOff(iEyeColor,bPublic,VISIBLE_EYECOL) << 3));
        //      - 3 Bits -   ---- 3 Bits ---     
        sResult+=MYBASE64.charAt(maskOff(iHairColor,bPublic,VISIBLE_HAIRCOL) | (maskOff(iBodyType,bPublic,VISIBLE_BODYTYPE) << 3));
        //     -- 4 Bits -   --- 2 Bits --
        sResult+=MYBASE64.charAt((maskOff(bMonth,bPublic,VISIBLE_DOB)&15) | (maskOff(iSmoker,bPublic,VISIBLE_SMOKER) << 4));         
        //      ------- 3 Bits -------   --- 2 Bits ---     ----- 1 Bit -----
        if ((!bPublic)||((lVisibility&VISIBLE_GENDER)!=0)) bb=(byte)((bGender?1:0)<<5); else bb=0;
        sResult+=MYBASE64.charAt(maskOff(iLookingForGender,bPublic,VISIBLE_LOOKINGFOR) | (maskOff(iDrinker,bPublic,VISIBLE_DRINKER) << 3) | bb);        
        //      -- 6 Bits --
        sResult+=MYBASE64.charAt(maskOff(iLookingFor,bPublic,VISIBLE_LOOKING));        
        //      -- 2 Bits -- --- 4 Bits ---
        sResult+=MYBASE64.charAt((maskOff((byte)(bWearsGlasses?1:0),bPublic,VISIBLE_GLASSES)) | (maskOff((byte)(bFacialHair?1:0),bPublic,VISIBLE_FACEHAIR)<<1) | ((maskOff(bDay,bPublic,VISIBLE_DOB) & 15)<<2));
        //      -- 2 Bits --   - 4 Bits
        sResult+=MYBASE64.charAt(((maskOff(bDay,bPublic,VISIBLE_DOB)>>4)&3) | ((maskOff(year,bPublic,VISIBLE_DOB) & 15)<<2));
        //       - 4 Bits -
        sResult+=MYBASE64.charAt(((maskOff(year,bPublic,VISIBLE_DOB)>>4)&15) | (maskOff(iHeight,bPublic,VISIBLE_HEIGHT)<<4));                       
        // Now encode the Visibility mask
        sResult+=MYBASE64.charAt(lVisibility&63);
        sResult+=MYBASE64.charAt(((lVisibility>>6)&63));
        sResult+=MYBASE64.charAt(((lVisibility>>12)&63));
        sResult+=MYBASE64.charAt(((lVisibility>>18)&63));              
        
        // Now encode some numbers that are the string lengths of the next bits - use 6 bits for each number - only need to encode 2 lengths as the thirs is derived
        sResult+=MYBASE64.charAt(strNickName.length());  sResult+=strNickName;
        if (((lVisibility&VISIBLE_URL)!=0)||(!bPublic)) sResult+=MYBASE64.charAt(strURL.length())+strURL; else sResult+=MYBASE64.charAt(0); 
        if (((lVisibility&VISIBLE_MESSAGE)!=0)||(!bPublic)) sResult+=MYBASE64.charAt(strDescription.length())+strDescription; else sResult+=MYBASE64.charAt(0); 
        if (((lVisibility&VISIBLE_NUMBER)!=0)||(!bPublic)) sResult+=MYBASE64.charAt(strMobileNumber.length())+strMobileNumber; else sResult+=MYBASE64.charAt(0); 
                
        // RFU - Data to follow this section
        sResult+=MYBASE64.charAt(0);  // 0 bytes follow!

        
        
        return sResult+PROFILE_END_MARKER;
    }
    
    
    public boolean loadFromString(String profile) {
        return loadFromString(profile,false);
    }
    
    /*-------------------------------------------*/
    /* For populating this profile from a string */
    public boolean loadFromString(String profile, boolean wasCutOff) {
        int bTemp;
        int bL1,bL2,bL3;
        int year;
        int bBytesFollow;
        
        
        // Unpack "profile" into the data structure - NOT fun
        lVisibility=0;   // just incase it fails

       //  repair the damaged entry!
       if ((wasCutOff)&&(profile.length()>10)) {
           String middle = MYBASE64.substring(0,1)+MYBASE64.substring(0,1)+MYBASE64.substring(0,1)+MYBASE64.substring(0,1)+MYBASE64.substring(0,1);
           profile = profile.substring(0,1) + middle + profile.substring(1);
       }
        
        // Exit if the profile string is too short
        if (profile.length()<23) return false;
        
        // Check header and footer
        if (!profile.startsWith(PROFILE_START_MARKER+"")) return false;
        if (!profile.endsWith(PROFILE_END_MARKER+"")) return false;
        
        bCycleValue = (byte)(MYBASE64.indexOf(profile.charAt(1)));
        iProfileVersion = (MYBASE64.indexOf(profile.charAt(2)) & 63) | 
                      ((MYBASE64.indexOf(profile.charAt(3)) & 63) << 6) |
                      ((MYBASE64.indexOf(profile.charAt(4)) & 63) << 12) |
                      ((MYBASE64.indexOf(profile.charAt(5)) & 63) << 18);             
        bStarsign = (byte)(MYBASE64.indexOf(profile.charAt(6))&0xF);  // 2 bits spare for something else! - this is a free hint to display
        
        bTemp = MYBASE64.indexOf(profile.charAt(7)); 
        iSexualOrientation = (byte) (bTemp & 7);
        iStatus = (byte)((bTemp >> 3) & 7);
        bTemp = MYBASE64.indexOf(profile.charAt(8)); 
        iProfession = (byte)(bTemp & 7);
        iEyeColor = (byte)((bTemp >> 3) & 7);
        bTemp = MYBASE64.indexOf(profile.charAt(9)); 
        iHairColor = (byte)(bTemp & 7);
        iBodyType = (byte)((bTemp >> 3) & 7);        
        bTemp = MYBASE64.indexOf(profile.charAt(10)); 
        bMonth = (byte)(bTemp & 15);
        iSmoker = (byte)((bTemp >> 4) & 3);         
        bTemp = MYBASE64.indexOf(profile.charAt(11)); 
        iLookingForGender = (byte)(bTemp & 7);
        iDrinker = (byte)((bTemp >> 3) & 3);  
        bGender = ((bTemp>>5)&1)!=0;
        iLookingFor = (byte)(MYBASE64.indexOf(profile.charAt(12)) & 63); 
        bTemp = MYBASE64.indexOf(profile.charAt(13)); 
        bDay = (byte)((bTemp >> 2) & 15); 
        bWearsGlasses = (bTemp&1)!=0;        
        bFacialHair = (bTemp&2)!=0;        
        bTemp = MYBASE64.indexOf(profile.charAt(14)); 
        bDay |= (byte)((bTemp&3)<<4);
        year = (byte)((bTemp >>2) & 15);         
        bTemp = MYBASE64.indexOf(profile.charAt(15)); 
        year |= (byte)((bTemp&15)<<4);
        iHeight = (byte)((bTemp >>4) & 3); 
                        
        iYear = year + 1900;
        
        lVisibility = (MYBASE64.indexOf(profile.charAt(16)) & 63) | 
                      ((MYBASE64.indexOf(profile.charAt(17)) & 63) << 6) |
                      ((MYBASE64.indexOf(profile.charAt(18)) & 63) << 12) |
                      ((MYBASE64.indexOf(profile.charAt(19)) & 63) << 18); 
    
        
        // Now encode some numbers that are the string lengths of the next bits - use 6 bits for each number - only need to encode 2 lengths as the thirs is derived
        profile = profile.substring(20); bL1 = MYBASE64.indexOf(profile.charAt(0));
        strNickName = profile.substring(1,bL1+1);

        profile = profile.substring(bL1+1); bL1 = MYBASE64.indexOf(profile.charAt(0));
        strURL = profile.substring(1,bL1+1);

        profile = profile.substring(bL1+1); bL1 = MYBASE64.indexOf(profile.charAt(0));
        strDescription = profile.substring(1,bL1+1);

        profile = profile.substring(bL1+1); bL1 = MYBASE64.indexOf(profile.charAt(0));
        strMobileNumber = profile.substring(1,bL1+1);
        profile = profile.substring(bL1+1);
        
        bBytesFollow = MYBASE64.indexOf(profile.charAt(0)) & 0x63;

           
        return true;
    }
}
