package com.wwc2.networks.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.utils.Config;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocationManagement {

    private AndroidLocations aLocations = null;
    private GeoFenceClient mGeoFenceClient = null;
    private static final String TAG = "LocationManagement";
    private static LocationManagement locationManagement = null;
    private static final int GAODE_MAX_LOCATION_TIME = 120;
    private int gaodeLocationTime = 0;
    private LocationManagement() {}

    public static LocationManagement newInstance(){
        if(locationManagement == null){
            locationManagement = new LocationManagement();
        }
        return locationManagement;
    }

    public AndroidLocations getAndroidLocations(){
        if(aLocations == null){
            aLocations = AndroidLocations.newInstance();
        }
        return aLocations;
    }

    public void startLocations(){
        if(aLocations != null){
            aLocations.stopLocation();
        }
        getAndroidLocations();
        aLocations.initLocation(CarServiceClient.getContext());
        resetLocationTime();
        aLocations.startLocation();

    }

    public void stopLocations(){
        if(aLocations != null){
            aLocations.stopLocation();
            aLocations.destroyLocation();
        }
    }
    //begin zhongyang add only location one time,whEN gps Fail 20191112
    public  boolean canAmapLocation(){
        return gaodeLocationTime < GAODE_MAX_LOCATION_TIME;
    }

    public void addAmapTimes(){
        gaodeLocationTime++;
    }

    public void resetLocationTime(){
        gaodeLocationTime =0;
    }

    //end

    public static final String GEOFENCE_BROADCAST_ACTION
            = "wwc2.networks.server.location.geofence.broadcast";
    public void createGeofence(double lat, double lng, float radius){
        if(mGeoFenceClient != null){
            mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_OUT);
            DPoint centerPoint = new DPoint();
            centerPoint.setLatitude(lat);
            centerPoint.setLongitude(lng);
            mGeoFenceClient.addGeoFence (centerPoint, radius, "1");
            mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
            mGeoFenceClient.setGeoFenceListener(fenceListenter);
            IntentFilter filter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(GEOFENCE_BROADCAST_ACTION);
            CarServiceClient.getContext().registerReceiver(mGeoFenceReceiver, filter);
        }
    }

    private GeoFenceListener fenceListenter = new GeoFenceListener() {

        @Override
        public void onGeoFenceCreateFinished(List<GeoFence> list,
                                     int errorCode,
                                     String customId) {
        }
    };

    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                if(status == GeoFence.STATUS_OUT){
                    NetWorkManagement.newInstance().postAlarmInfo(Config.ALARM_GEOFENCE, "")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                @Override
                                public void accept(String result) throws Exception {
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                }
                            });
                }
            }
        }
    };

    public void destroyGeofence(){
        if(mGeoFenceClient != null){
            mGeoFenceClient.removeGeoFence();
        }
    }
}
