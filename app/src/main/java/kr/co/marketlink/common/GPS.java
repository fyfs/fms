package kr.co.marketlink.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yangjaesang on 2017. 2. 2..
 */

public class GPS {

    static private boolean hasListener=false;
    static public double lastLat=0;
    static public double lastLng=0;
    static public double lastAcr=0;
    static public double lastAlt=0;

    static public HashMap<String,String> getGps(Context context){
        if(!hasListener)addListener(context);
        HashMap<String,String> ret = new HashMap<String,String>();
        ret.put("lat","-1");
        ret.put("lng","-1");
        ret.put("acr","-1");
        ret.put("alt","-1");
        ret.put("RESULT","NOTOK");
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            ret.put("lat","-2");
            ret.put("lng","-2");
            ret.put("acr","-2");
            ret.put("alt","-2");
            ret.put("RESULT","PERMISSION");
            return ret;
        }
        LocationManager mLocationManager = (LocationManager)context.getApplicationContext().getSystemService(context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(new Criteria(),true);
        Location bestLocation = null;
        if(providers.size()==0) {
            ret.put("lat","-3");
            ret.put("lng","-3");
            ret.put("acr","-3");
            ret.put("alt","-3");
            ret.put("RESULT","GPSOFF");
            return ret;
        }
        try {
            for (String provider : providers) {
                Location location = mLocationManager.getLastKnownLocation(provider);
                if (location == null) {
                    continue;
                }
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                    ret.put("lat",Double.toString(bestLocation.getLatitude()));//37.5608695
                    ret.put("lng",Double.toString(bestLocation.getLongitude()));//126.9619209
                    ret.put("acr",Double.toString(bestLocation.getAccuracy()));//30.0
                    ret.put("alt",Double.toString(bestLocation.getAltitude()));//0
                    lastLat=bestLocation.getLatitude();
                    lastLng=bestLocation.getLongitude();
                    lastAcr=bestLocation.getAccuracy();
                    lastAlt=bestLocation.getAltitude();
                    ret.put("RESULT","OK");
                }

            }
        } catch (SecurityException | NullPointerException e){
            ret.put("lat","-4");
            ret.put("lng","-4");
            ret.put("acr","-4");
            ret.put("alt","-4");
            ret.put("RESULT","PERMISSION");
        }
        return ret;
    }


    //권한 받기
    static public void getPermission(Activity activity){
        ActivityCompat.requestPermissions(activity,new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
    }

    //리스너 등록
    static private void addListener(Context context){
        try {
            LocationManager mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener, Looper.getMainLooper());
            hasListener=true;
            Common.log("GPS Listener added");
        } catch (SecurityException e) {
            Common.log("MServiceMonitor onReceive:"+e.toString());
        }
    }
    public static LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

}
