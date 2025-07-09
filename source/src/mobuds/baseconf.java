package mobuds;

// Basic information, utilitiy functions and constants - never created, all static

import java.io.*;
import java.util.*;

public class baseconf {
    
    
    public static final String sysName = "MoBuds";
    public static final String sysURL = "http://www.mobuds.mobi";
    public final static String TALKEASI_URL = "http://mobile.mobuds.mobi/";
  //  public final static String TALKEASI_URL = "http://mobuds/profilesync.php";
    public final static String sHex = "0123456789ABCDEF";
    public final static String ADVERT_FIELD = "advert";
    
    public final static String strFriend = "MoMate";
    public final static String strBlock = "MoFoe";
    public final static String strFriends = "MoMates";
    public final static String strBlocks = "MoFoe's";

    // Software download URL
    public static final String SOFTWARE_DOWNLOAD_URL = sysURL + "/mobiledownload.php";
    public static final byte ERROR_NO_ERROR                 = 0;
    
    // Messaage sent over connection to signal we're sending a text message
    public static final byte COMMUNICATION_SENDMESSAGE     = 1;
    //public static final byte COMMUNICATION_MESSAGERECEIPT  = 2;
    public static final byte COMMUNICATION_LOCALERT        = 3;
    public static final byte COMMUNICATION_LOCPROMO        = 4;
    public static final byte COMMUNICATION_LOCVENNEWS      = 5;
    public static final byte COMMUNICATION_LOCGENNEWS      = 6;            
        
    // Status messages sent back and forth by the socket
    public static final byte COMMUNICATION_RXUNKNOWN = 2;
    public static final byte COMMUNICATION_RXOK = 1;
    public static final byte COMMUNICATION_RXFAIL = 0;
    
    public static final String heightTypes[] = {"Petite","Average","Above Average","Very Tall"};
    public static final String bodyTypes[]   = {"Slim","Slender","Athletic","Fit","Average","Chubby","Large","Voluptuous"};
    public static final String eyeColours[]  = {"Black","Brown","Blue","Gray","Green","Hazel"};
    public static final String hairColours[] = {"Black","Dark Brown","Light Brown","Blonde","Gray","Red","Bald","Always Changes"};

    public static final String statusList[] = {"Single","Dating","In Relationship","Married","Committed","Divorced","Open Relationship"};
    public static final String orientationList[] = {"Hetrosexual","Hetroinclined","Bisexual","Homoinclined","Homosexual","Curious","Unsure","Sexless"};
    public static final String professionList[] = {"At School","At College","At University","Unemployed","Working Part Time","Working Full Time"};
    public static final String lookingForList[] = {"Friends","Relationship","Dating","Networking","Fun","Religion"};
    public static final int lookingForMasks[] = {MobudsProfile.LOOKINGFOR_FRIENDS,MobudsProfile.LOOKINGFOR_RELATIONSHIPS,MobudsProfile.LOOKINGFOR_DATING,
                                                 MobudsProfile.LOOKINGFOR_NETWORKING,MobudsProfile.LOOKINGFOR_FUN,MobudsProfile.LOOKINGFOR_RELIGION};

    public static final String habbitTypes[] = {"Never","Sometimes","Socially","Always"};
    public static final String starSigns[] = {"Aquarius","Pisces","Aries","Taurus","Gemini","Cancer","Leo","Virgo","Libra","Scorpio","Sagittarius","Capricorn"};
    public static final String monthNames[] = {"January","February","March","April","May","June","July","August","September","October","November","December"};
    public static final String shortMonthNames[] = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    
    public static final String visibilityFields[] = {"Mobile/Cell Number","Website URL",
                                                     "Gender","Date of Birth","Drink Habbits","Smoking Habbits",
                                                     "Height","Body Type", "Eye Colour","Hair Colour","Wears Glasses","Facial Hair", 
                                                     "Martial Status","Sexuality","School/Work",
                                                     "Social Interest","Social Interest With","Personality"
                                                     };
    
    public static final int visibilityBitmasks[] = {MobudsProfile.VISIBLE_NUMBER, MobudsProfile.VISIBLE_URL, 
                                                    MobudsProfile.VISIBLE_GENDER, MobudsProfile.VISIBLE_DOB,  MobudsProfile.VISIBLE_DRINKER, MobudsProfile.VISIBLE_SMOKER, 
                                                    MobudsProfile.VISIBLE_HEIGHT, MobudsProfile.VISIBLE_BODYTYPE, MobudsProfile.VISIBLE_EYECOL, MobudsProfile.VISIBLE_HAIRCOL, 
                                                    MobudsProfile.VISIBLE_GLASSES, MobudsProfile.VISIBLE_HAIRCOL,
                                                    MobudsProfile.VISIBLE_STATUS, MobudsProfile.VISIBLE_ORIENTATION, MobudsProfile.VISIBLE_PROFESSION, 
                                                    MobudsProfile.VISIBLE_LOOKING, MobudsProfile.VISIBLE_LOOKINGFOR, MobudsProfile.VISIBLE_MESSAGE
                                                    };
    
    public static final String strGenders[] = {"Male","Female"};
    public static final String lookingWithList[] = {"Men","Women"};
    public static final int lookingWithMask[] = {MobudsProfile.LOOKINGFORG_MALE,MobudsProfile.LOOKINGFORG_FEMALE};
            
    public static final String UNKNOWN_NAME = "<unknown>";    
    ///////////////////////// bluetooth related
    // Are we debugging this!?
    public static final boolean ISDEBUG = false;

    // max distance allowed
    public static final int MAX_DISTANCE = 6;

    // No more than this many relays
    public static final int MAX_ROUTE_REQUESTS = MAX_DISTANCE * 2;

    // Max distance when we're busy
    public static final int MAX_MINIMUM_DISTANCE = 3;

    // max users allowed
    public static final int MAX_USERS_ALLOWED = 100;

    // Number of retries on messages to be sent
    public static final int MAX_QUEUE_FAIL_TIME = 5*60*1000;

    // Time allowed to send messages (also a delay between scans)
    public static final int MESSAGE_PROCESSING_TIME = 10*1000;

    // 15 minutes between FORCED connection test
    public static final int LAST_CONNECTIONTEST_PERIOD = 15 * 60 * 1000;

    // Seconds always for pause
    public static final int MIN_DISCOVERY_DELAY = 55;

    // Maximum random element added to the above per loop
    public static final int MAX_DISCOVERY_JITTER = 45;

    // Time before messages in the outbox are marked as failed (~5 minutes)
    public static final int MESSAGE_OUTBOX_EXPIRY_TIME = 1000 * 60 * (MAX_DISTANCE+1);

    public static final int DISCOVERY_MISSES_DELETE = MAX_DISTANCE*2;

    // Max time before a user is deleted from the list that hasn't cycled its profile - 4 x Cycle Period
    public static final int MAX_USER_TIMEOUT = (MIN_DISCOVERY_DELAY+(MAX_DISCOVERY_JITTER>>1)) * DISCOVERY_MISSES_DELETE;

    public static final int MAX_USER_TIMEOUT_NONMOBUDS = 60 * 10;  // 10 minutes

    // Timeout period for cinformation of message OK when sending a request in milliseconds
    public static final int MESSAGE_CONFIRMATION_TIMEOUT = 2000;
    
    // Maxiumum length of a message
    public static final int MAX_MEESSAGE_LENGTH = 250;

    public static String long2String(long lng) {
        String r = "";
        while (lng!=0) {
            r=sHex.charAt((int)(lng&15))+r;
            lng>>=4;
        }
        
        if (r.length()<1) r=""+sHex.charAt(0);
        return r;
    }

    // simple hex conversion
    public static byte charHexToByte(char ch) {
        int a = sHex.indexOf(ch);
        if (a<0) a=0;
        return (byte)a;
    }
    
    // convert two byte string of hex to a byte
    public static byte strHexToByte(String str) {
        str = str.toUpperCase();
        if (str.length()>1) return (byte)((charHexToByte(str.charAt(0))<<4) | (charHexToByte(str.charAt(1)))); else
        if (str.length()>0) return charHexToByte(str.charAt(0));  else return 0;
    }
    
    // Convert number to base64
    public static String intToMyBase64(int c) {
        String s="";
        while (c!=0) {
            s+=MobudsProfile.MYBASE64.charAt(c&63);
            if (c==-1) c=0; else c>>=6;
        }
        if (s.length()<1) s=MobudsProfile.MYBASE64.substring(0,1)+"";
        return s;
    }

    
    // Convert number to base64
    public static String longToMyBase64(long c) {
        String s="";
        while (c!=0) {
            s+=MobudsProfile.MYBASE64.charAt((int)c&63);
            if (c==-1) c=0; else c>>=6;
        }
        if (s.length()<1) s=MobudsProfile.MYBASE64.substring(0,1)+"";
        return s;
    }
    
    
    // Reverse the above
    public static int myBase64ToInt(String s) {
        int res=0;
        int j;
        
        for (int a=s.length()-1; a>=0; a--) {
            res<<=6;
            j = MobudsProfile.MYBASE64.indexOf(s.charAt(a));
            if (j>=0) res|=j;
        }
        
        return res;
    }

    // Reverse the above
    public static long myBase64ToLong(String s) {
        long res=0;
        int j;
        
        for (int a=s.length()-1; a>=0; a--) {
            res<<=6;
            j = MobudsProfile.MYBASE64.indexOf(s.charAt(a));
            if (j>=0) res|=j;
        }
        
        return res;
    }
    
    // returns the number 2 digits wide
    public static String twoWide(int i) {
        String s = i+"";
        if (s.length()<2) s="0"+s;
        return s;
    }
    
    // returns a checksum
    static String xorChecksum(String s) {
        int a;
        byte b[] = "CraveCreate".getBytes();
        for (a=0; a<s.length(); a++) b[a%b.length] = (byte)(b[a%b.length] ^ s.charAt(a));
        return encode(b);
    }
    
    // Returns a nice ending to DOB
    static String getNiceField(int day) {
        if ((day==1)||(day==21)||(day==31)) return day+"st";
        if ((day==2)||(day==22)) return day+"nd";
        if ((day==3)||(day==23)) return day+"rd";
        return day+"th";
    }

    // base64 decode
    static int decode(char c) {
        if (c >= 'A' && c <= 'Z') return ((int) c) - 65; 
        if (c >= 'a' && c <= 'z') return ((int) c) - 97 + 26; 
        if (c >= '0' && c <= '9') return ((int) c) - 48 + 26 + 26; 
            switch (c) {
                case '+' : return 62;
                case '/' : return 63;
                case '=' : return 0;
                default : return 0;
            }        
    }

    // encode it
    static char encode(int b) {
        if (b<26) return (char)(65+b);
        if (b<52) return (char)(b-26+97);
        if (b<62) return (char)(b-52+48);
        if (b==62) return '+';
        if (b==63) return '/';
        return '=';
    }
    
        // Get the age of this person
    public static int getAge(int iDay, int iMonth, int iYear) {
         Calendar d = Calendar.getInstance();
         d.setTime(new java.util.Date());
         int years = d.get(Calendar.YEAR) - iYear;
         if ((d.get(Calendar.MONTH)<iMonth) || ((d.get(Calendar.MONTH)==iMonth)&&(d.get(Calendar.DAY_OF_MONTH)<iDay))) years--;
         return years;
    }
    
   // Returns the star sign for a date
    public static int getStarSign(int day, int month) {
        if (((month==0)&&(day>=21)) || ((month==1)&&(day<=19))) return 0; else
        if (((month==1)&&(day>=20)) || ((month==2)&&(day<=20))) return 1; else
        if (((month==2)&&(day>=21)) || ((month==3)&&(day<=20))) return 2; else
        if (((month==3)&&(day>=21)) || ((month==4)&&(day<=21))) return 3; else
        if (((month==4)&&(day>=22)) || ((month==5)&&(day<=21))) return 4; else
        if (((month==5)&&(day>=22)) || ((month==6)&&(day<=23))) return 5; else
        if (((month==6)&&(day>=24)) || ((month==7)&&(day<=23))) return 6; else
        if (((month==7)&&(day>=24)) || ((month==8)&&(day<=23))) return 7; else
        if (((month==8)&&(day>=24)) || ((month==9)&&(day<=23))) return 8; else
        if (((month==9)&&(day>=24)) || ((month==10)&&(day<=22))) return 9; else
        if (((month==10)&&(day>=23)) || ((month==11)&&(day<=21))) return 10; else return 11;
    }
        
    // Sort userlist
    public static void quickSort(Vector elements, int lowIndex, int highIndex) {
        try {
                int lowToHighIndex;
                int highToLowIndex;
                int pivotIndex;
                DiscoveredUsers pivotValue;  // values are Strings in this demo, change to suit your application
                DiscoveredUsers lowToHighValue;
                DiscoveredUsers highToLowValue;
                DiscoveredUsers parking;
                int newLowIndex;
                int newHighIndex;
                int compareResult;

                lowToHighIndex = lowIndex;
                highToLowIndex = highIndex;
                pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
                pivotValue = (DiscoveredUsers)elements.elementAt(pivotIndex);
                newLowIndex = highIndex + 1;
                newHighIndex = lowIndex - 1;

                while ((newHighIndex + 1) < newLowIndex)  {
                    lowToHighValue = (DiscoveredUsers)elements.elementAt(lowToHighIndex);
                    while (lowToHighIndex < newLowIndex & lowToHighValue.compareTo(pivotValue)<0 ) {
                        newHighIndex = lowToHighIndex; // add element to lower part
                        lowToHighIndex ++;
                        lowToHighValue = (DiscoveredUsers)elements.elementAt(lowToHighIndex);
                    }

                    highToLowValue = (DiscoveredUsers)elements.elementAt(highToLowIndex);
                    while (newHighIndex <= highToLowIndex & (highToLowValue.compareTo(pivotValue)>0)) {
                        newLowIndex = highToLowIndex; // add element to higher part
                        highToLowIndex --;
                        highToLowValue = (DiscoveredUsers)elements.elementAt(highToLowIndex);
                    }

                    // swap if needed
                    if (lowToHighIndex == highToLowIndex) newHighIndex = lowToHighIndex; else
                       if (lowToHighIndex < highToLowIndex)  {
                            compareResult = lowToHighValue.compareTo(highToLowValue);
                            if (compareResult >= 0) {
                                parking = lowToHighValue;
                                elements.setElementAt(highToLowValue, lowToHighIndex);
                                elements.setElementAt(parking, highToLowIndex);
                                newLowIndex = highToLowIndex;
                                newHighIndex = lowToHighIndex;
                                lowToHighIndex ++;
                                highToLowIndex --;
                            }
                       }
                }
              // Continue recursion for parts that have more than one element
              if (lowIndex < newHighIndex) quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
              if (newLowIndex < highIndex) quickSort(elements, newLowIndex, highIndex); // sort higher subpart
        } catch (Exception e) {}
   }
    
    
    // Convert to the base64 alphabet hexish
    public static String convertToHexish(String s) {
        String sRes = "";
        int aChar;
        for (int a=0; a<s.length(); a++) {
            aChar = (int)(s.charAt(a));
            sRes+=MobudsProfile.MYBASE64.charAt((int)(aChar/16) & 0x0F);
            sRes+=MobudsProfile.MYBASE64.charAt(aChar & 0x0F);
        }
        return sRes;
    }

    public static String encode(byte[] data) {
        StringBuffer sOutput = new StringBuffer();
        int tmp;
        byte[] b3 = new byte[3];

        for (int a=0; a<data.length; a+=3) {
            if (a<data.length) b3[0] = data[a]; else b3[0]=0;
            if (a+1<data.length) b3[1] = data[a+1]; else b3[1]=0;
            if (a+2<data.length) b3[2] = data[a+2]; else b3[2]=0;

            sOutput.append(encode(((b3[0]>>2)&63)));
            sOutput.append(encode((((b3[0]&0x03)<<4) | ((b3[1]&0xF0) >> 4))));
            if (a+1<data.length) sOutput.append(encode((((b3[1]&0x0F)<<2) | ((b3[2] >> 6)&0x03)))); else sOutput.append('=');
            if (a+2<data.length) sOutput.append(encode((b3[2]&63))); else sOutput.append('=');
        }
        return sOutput.toString();
    }

    // Base64 decode
    public static byte[] decode(String s) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            decode(s, bos);
        }
        catch (Exception e) {
        }
        return bos.toByteArray();
    }

    // Base64 decode
    public static void decode(String s, OutputStream os) {
        int i = 0;
        
        int len = s.length();

        try {
            while (true) {
                while (i < len && s.charAt(i) <= ' ') i++;

                if (i == len) break;

                int tri = (decode(s.charAt(i)) << 18)
                          + (decode(s.charAt(i + 1)) << 12)
                          + (decode(s.charAt(i + 2)) << 6)
                          + (decode(s.charAt(i + 3)));

                os.write((tri >> 16) & 255);
                if (s.charAt(i + 2) == '=') break;
                os.write((tri >> 8) & 255);
                if (s.charAt(i + 3) == '=') break;
                os.write(tri & 255);
                i += 4;
            }
        } catch (Exception e) {}
    }
    
    // URL Encode a string
    public static byte[] urlDecode(String s) {
        String sResult = "";
        char aChar;
        int a1,a2;
        byte b[];
        
        int iLength = s.length();
        int a,i;
        
        // We need to work out the size of the return buffer first
        a=s.indexOf('%');
        while (a>=0) {
            iLength-=2;  // each % rep
            a=s.indexOf('%',a+1);
        }
        
        if (iLength<1) return new byte[0];
        
        // allocate memory for the buffer
        b = new byte[iLength];
        i=0;
                
        for (a=0; a<s.length(); a++) {
            aChar = s.charAt(a);
            
            if (aChar=='+') b[i++]=(byte)' '; else
            if (aChar=='%') {
                try {
                    a1 = sHex.indexOf(s.charAt(a+1));
                    a2 = sHex.indexOf(s.charAt(a+2));
                    b[i++]=(byte)((a1<<4) | a2);
                    a+=2;
                } catch (Exception e) {}
            } else b[i++]=(byte)aChar;
        }
        
        return b;
    }    
    
    

    // URL Encode a string
    public static String urlEncode(String s) {
        String sResult = "";
        int aChar;
        for (int a=0; a<s.length(); a++) {
            aChar = (int)(s.charAt(a));
            
            if (aChar==((int)' ')) sResult+="+"; else
            if (((aChar>=48)&&(aChar<=57))  || ((aChar>=65)&&(aChar<=90)) || ((aChar>=97)&&(aChar<=122))) 
                sResult+=s.charAt(a); else {
                    sResult+="%";
                    sResult+=sHex.charAt((aChar/16) & 0x0F);
                    sResult+=sHex.charAt(aChar & 0x0F);
                }
        }
        return sResult;
    }    
    
    
    
    // Returns a Hash Table with all fields retreived from the internet
    public static Hashtable getDecodeFields(String s)  {
        Hashtable ht = new Hashtable();
        int a = 0;
        int lasta = 0;
        int b;
        String section;
        
        a = s.indexOf("&",lasta);
        while (a>=0) {
            section = s.substring(lasta,a);
            lasta = a+1;
            
            b = section.indexOf("=");
            if (b>=0) ht.put(section.substring(0,b),urlDecode(section.substring(b+1)));
            a = s.indexOf("&",lasta);
        }
        section = s.substring(lasta);
        b = section.indexOf("=");
        if (b>=0) ht.put(section.substring(0,b),urlDecode(section.substring(b+1)));
        
        return ht;
    }
     
}
