/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobuds;


//#if JSR179
// JSR 179
import javax.microedition.location.*;
//#endif
        

/**
 *
 * @author rob smith
 */
public class clsLocationFinder extends clsLocationBase
        //#if JSR179
        implements LocationListener
        //#endif
{
    /* Location Criteria */
//#if JSR179
    private Criteria locationCriteria;
    private LocationProvider locationProvider;
    private QualifiedCoordinates currentLocation;
//#endif
 
        
    public clsLocationFinder() {
            super();

            try {
                isAvailable = false;

                //#if JSR179
                currentLocation = null;
                locationCriteria = new Criteria();
                locationCriteria.setHorizontalAccuracy(GPS_ACCURACY);
                locationCriteria.setVerticalAccuracy(GPS_ACCURACY);
                locationCriteria.setCostAllowed(false);
                locationCriteria.setPreferredResponseTime(15*60*1000);  // 15 minute max delay

                locationProvider = LocationProvider.getInstance(locationCriteria);
                if (locationProvider==null){
                    locationCriteria.setHorizontalAccuracy(GPS_ACCURACY*10);
                    locationCriteria.setVerticalAccuracy(GPS_ACCURACY*10);
                    locationProvider = LocationProvider.getInstance(null);
                }
                if (locationProvider==null){
                    locationCriteria.setHorizontalAccuracy(GPS_ACCURACY*100);
                    locationCriteria.setVerticalAccuracy(GPS_ACCURACY*100);
                    locationProvider = LocationProvider.getInstance(null);
                }
                if (locationProvider==null){
                    locationCriteria.setHorizontalAccuracy(GPS_ACCURACY*1000);
                    locationCriteria.setVerticalAccuracy(GPS_ACCURACY*1000);
                    locationProvider = LocationProvider.getInstance(null);
                }
                if (locationProvider==null) locationProvider = LocationProvider.getInstance(null);

                if (locationProvider!=null) locationProvider.setLocationListener(this,60,-1,-1);  // 5 mins max out of date
                isAvailable = true;
                //#endif
            } catch (Exception m) {
                //#if JSR179
                locationProvider = null; 
                locationCriteria = null;
                //#endif
            }

    }
    
    // Get last received
    public clsMohubServer getLastReceivedLoc() {
        //#if JSR179
        if (currentLocation==null) return null;
        
        clsMohubServer ret = new clsMohubServer();        
        ret.lastUse = lastTimeStamp;
        ret.mohubID=0;
        ret.strBluetoothAddress="";
        ret.strConnectionUrl="";
        ret.strMohubName="";
        ret.strMohubURL="";

        ret.lat = (int)Math.ceil(currentLocation.getLatitude()*COORDINATE_SCALE);
        ret.lng = (int)Math.ceil(currentLocation.getLongitude()*COORDINATE_SCALE);
        int aclng,aclat;
        if (currentLocation.getHorizontalAccuracy()==Float.NaN)  aclat=0x7ffffff; else aclat = (int)Math.ceil(0.5+currentLocation.getHorizontalAccuracy());
        if (currentLocation.getVerticalAccuracy()==Float.NaN) aclng=0x7ffffff; else aclng = (int)Math.ceil(0.5+currentLocation.getVerticalAccuracy());
        if (aclng<aclat) ret.accuracy=aclng; else ret.accuracy=aclat;
        return ret;
        //#else
//#         return null;
        //#endif
    }
    
    // get the date of the last stamp
    public clsMohubServer runSpecialLocation(clsMohubServer ret) {

    //#if JSR179
        if ((currentLocation!=null)&&((System.currentTimeMillis()/1000)-lastTimeStamp)>60*5) currentLocation=null;
        if (currentLocation!=null) {
            ret.lat = (int)Math.ceil(currentLocation.getLatitude()*COORDINATE_SCALE);
            ret.lng = (int)Math.ceil(currentLocation.getLongitude()*COORDINATE_SCALE);
            int aclng,aclat;
            if (currentLocation.getHorizontalAccuracy()==Float.NaN)  aclat=0x7ffffff; else aclat = (int)Math.ceil(0.5+currentLocation.getHorizontalAccuracy());
            if (currentLocation.getVerticalAccuracy()==Float.NaN) aclng=0x7ffffff; else aclng = (int)Math.ceil(0.5+currentLocation.getVerticalAccuracy());
            if (aclng<aclat) ret.accuracy=aclng; else ret.accuracy=aclat;
        } else {
            ret.accuracy=0xfffffff;
        }
       //#endif
         
        return ret;
    }

    //#if JSR179
    // location updated
    public void locationUpdated(LocationProvider provider, Location location) {
          // Called by the LocationProvider to which this listener is registered.
         if (location.isValid()) {
            QualifiedCoordinates q = location.getQualifiedCoordinates();
            if (q!=null) {
                lastTimeStamp = location.getTimestamp()/1000;
                currentLocation = q;
            }
         }
    }

    public void providerStateChanged(LocationProvider provider, int newState) {
          // We dont care much about this
    }
    //#endif



}


class clsLocationBase2 {
    public clsLocationBase2 () {

    }

    public clsLocationBase getLocation() {
        return new clsLocationFinder();
    }

}
