/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobuds;

// Base class for location providers

/**
 *
 * @author rob smith
 */
public class clsLocationBase {
    protected final int     GPS_ACCURACY        = 30;  // 30 meters
    public static final int COORDINATE_SCALE    = 100000;
    protected long lastTimeStamp;
    public long _stamp;
    public int _mohubId,_lat,_lng,_accuracy;
    public boolean isAvailable;


    // publish a location from another source.  Used if this can't
    public void publishOtherLocation(String remoteLocation) {
        clsMohubServer cls = new clsMohubServer();
        cls.publishOtherLocation(remoteLocation);
        _stamp=cls.lastUse;
        _lat=cls.lat;
        _lng=cls.lng;
        _mohubId=cls.mohubID;
        _accuracy=cls.accuracy;
    }

    public clsLocationBase() {
        _accuracy = 0xfffff;
        lastTimeStamp=0;
    }

    public clsMohubServer getLastReceivedLoc() {
        return null;
    }

    public clsMohubServer runSpecialLocation(clsMohubServer ret) {
        return ret;
    }

    public clsMohubServer getLastLocation() {
        clsMohubServer ret = new clsMohubServer();
        ret.lastUse = lastTimeStamp;
        ret.mohubID=-1;
        ret.strBluetoothAddress="";
        ret.strConnectionUrl="";
        ret.strMohubName="";
        ret.strMohubURL="";
        
        ret = runSpecialLocation(ret);

        if ((_mohubId>0)||((ret.accuracy>_accuracy)&&(_accuracy>0)&&(Math.abs(_stamp-(System.currentTimeMillis()/1000))<60*5))) {
            ret.lastUse = _stamp;
            ret.lat = _lat;
            ret.lng = _lng;
            ret.mohubID = _mohubId;
            ret.accuracy=_accuracy;
        }
        
        return ret;
    }
}
