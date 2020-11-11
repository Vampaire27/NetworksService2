package com.wwc2.networks.server.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.action.LocationAction;
import com.wwc2.networks.server.bean.AMapBean;
import com.wwc2.networks.server.bean.GPSBean;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.GpsUtils;
import com.wwc2.networks.server.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class AndroidLocations {

    private final int MIN_TIME = 1000 * 3;
    private static ArrayList<String> PROVIDER_ARRAY = null;
    private static ArrayList<LocationsInterface> interfacesList = null;
    private LocationManager locationManager;
    private String locationProvider;
    private Handler handler = null;

    private static AndroidLocations androidLocations = null;

    private AndroidLocations() {
    }

    public static AndroidLocations newInstance() {
        if (androidLocations == null) {
            androidLocations = new AndroidLocations();
            interfacesList = new ArrayList<>();
            PROVIDER_ARRAY = new ArrayList<>();
            PROVIDER_ARRAY.add(LocationManager.GPS_PROVIDER);
            PROVIDER_ARRAY.add(LocationManager.NETWORK_PROVIDER);
            PROVIDER_ARRAY.add(LocationManager.PASSIVE_PROVIDER);
        }
        return androidLocations;
    }

    public interface LocationsInterface {
        void onLocationsChanged(Location location, GPSBean mBean);
    }

    public void setLocationListener(AndroidLocations.LocationsInterface listener) {
        if (interfacesList != null) {
            int index = interfacesList.indexOf(listener);
            if (index == -1) {
                interfacesList.add(listener);
            }
        }
    }

    public void removeListener(AndroidLocations.LocationsInterface listener) {
        if (interfacesList != null && interfacesList.size() > 0) {
            int index = interfacesList.indexOf(listener);
            if (index > -1) {
                interfacesList.remove(index);
            }
        }
    }

    public void removeAllListener() {
        if (interfacesList != null) {
            interfacesList.clear();
        }
    }

    public void startLocation() {
        if (handler != null)
            handler.post(checkLocation);
    }

    public void stopLocation() {
        if (handler != null)
            handler.removeCallbacks(checkLocation);
    }

    public void destroyLocation() {
        //removeAllListener();
        if ((locationManager != null) && (gpsLocationListener != null)) {
            locationManager.removeUpdates(gpsLocationListener);
        }

        if ((locationManager != null) && (networkLocationListener != null)) {
            locationManager.removeUpdates(networkLocationListener);
        }

        if ((locationManager != null) && (passiveLocationListener != null)) {
            locationManager.removeUpdates(passiveLocationListener);
        }

        if (androidLocations != null) {
            androidLocations = null;
        }
    }

    private Runnable checkLocation = new Runnable() {
        @Override
        public void run() {
            getLocationProvider();
            updateLocation();
            handler.postDelayed(checkLocation, MIN_TIME);
        }
    };

    @SuppressLint("MissingPermission")
    public void initLocation(Context context) {
        locationProvider = null;
        locationManager = null;
        locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return;
        }
        if (handler == null) {
            handler = new Handler();
        }
        List<String> allProviders = locationManager.getAllProviders();
        if (allProviders != null) {
            for (String provider : allProviders) {
                if ((provider != null) && (PROVIDER_ARRAY.contains(provider))) {
                    if (LocationManager.GPS_PROVIDER.equals(provider)) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME,
                                0,
                                gpsLocationListener);
                    } else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME,
                                0,
                                networkLocationListener);
                    } else if (LocationManager.PASSIVE_PROVIDER.equals(provider)) {
                        locationManager.requestLocationUpdates(
                                LocationManager.PASSIVE_PROVIDER,
                                MIN_TIME,
                                0,
                                passiveLocationListener);
                    }
                }
            }
        }
    }

    private synchronized void getLocationProvider() {
        if (locationManager != null) {
            List<String> providers = locationManager.getAllProviders();
            if (providers == null || providers.size() <= 0) {
                locationProvider = null;
            } else {
                String bestProvider = null;
                Location bestLocation = null;
                for (String provider : providers) {
                    if ((provider != null) && (PROVIDER_ARRAY.contains(provider))) {
                        @SuppressLint("MissingPermission")
                        Location location = locationManager.getLastKnownLocation(provider);
                        if (location == null) {
                            continue;
                        }

                        if (bestLocation == null) {
                            bestLocation = location;
                            bestProvider = provider;
                            continue;
                        }

                        if (Float.valueOf(
                                location.getAccuracy()).compareTo(
                                bestLocation.getAccuracy()) >= 0) {
                            bestLocation = location;
                            bestProvider = provider;
                        }
                    }
                }
                locationProvider = bestProvider;
            }
        } else {
            locationProvider = null;
        }
    }

    private void updateLocation() {
        if ((locationProvider != null) && (!locationProvider.equals(""))
                && (PROVIDER_ARRAY.contains(locationProvider))) {
            try {
                @SuppressLint("MissingPermission")
                Location currentLocation = locationManager.
                        getLastKnownLocation(locationProvider);
                GPSBean lGPSBean;
                if(currentLocation.getTime() == 0) {
                     lGPSBean = new GPSBean(Config.AMAP_GPS, currentLocation.getLatitude(),
                            currentLocation.getLongitude(), currentLocation.getSpeed(), currentLocation.getBearing(),
                            System.currentTimeMillis() / 1000);
                }else{
                     lGPSBean = new GPSBean(Config.AMAP_GPS, currentLocation.getLatitude(),
                            currentLocation.getLongitude(), currentLocation.getSpeed(), currentLocation.getBearing(),
                            currentLocation.getTime() / 1000);
                }

                LogUtils.d("gps 定位  :" +lGPSBean.toString() );
                LocationManagement.newInstance().resetLocationTime();

                if (currentLocation != null) {
                    if (interfacesList != null) {
                        for (int i = 0; i < interfacesList.size(); i++) {
                            interfacesList.get(i).onLocationsChanged(currentLocation,lGPSBean);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if(LocationManagement.newInstance().canAmapLocation()){
            //zhongyang.hu add system location fail, add AMapLocation one times.
            AMapLocation(CarServiceClient.getContext());
        }
    }


    private static void AMapLocation(Context context) {
        LocationAction.newInstance(context).getLocation(new LocationAction.LocationCallback() {
            @Override
            public void onLocationFailure(int errorCode) {
                //requestLbs();
            }

            @Override
            public void onLocationResults(AMapLocation aMapLocation) {
                if (aMapLocation.getLongitude() <= 0 && aMapLocation.getLatitude() <= 0) {
                    return;
                }
                //
               // aMapLocation.getAddress();
                //转换为原始GPS点
                LatLonPoint point = GpsUtils.toGPSPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                GPSBean lGPSBean;
               if(aMapLocation.getTime() == 0) {
                    lGPSBean = new GPSBean(Config.AMAP_GPS, point.getLatitude(),
                           point.getLongitude(), aMapLocation.getSpeed(), aMapLocation.getBearing(),
                            System.currentTimeMillis() / 1000);
               }else{
                    lGPSBean = new GPSBean(Config.AMAP_GPS, point.getLatitude(),
                           point.getLongitude(), aMapLocation.getSpeed(), aMapLocation.getBearing(),
                           aMapLocation.getTime() / 1000);
               }

                if (interfacesList != null) {
                    for (int i = 0; i < interfacesList.size(); i++) {
                        interfacesList.get(i).onLocationsChanged(aMapLocation,lGPSBean);
                    }
                }
               TraceBeanManager.getInstance().setmLastAdderss(aMapLocation.getAddress());
                LogUtils.d("gaode 定位 :" +lGPSBean.toString() );

                LocationManagement.newInstance().addAmapTimes(); //高德定位成功 10,在下次GPS定位成功之前,不再定位．
            }
        });
    }

    private LocationListener gpsLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            getLocationProvider();
        }

        @Override
        public void onProviderDisabled(String provider) {
            getLocationProvider();
        }
    };
    private LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            getLocationProvider();
        }

        @Override
        public void onProviderDisabled(String provider) {
            getLocationProvider();
        }
    };
    private LocationListener passiveLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            getLocationProvider();
        }

        @Override
        public void onProviderDisabled(String provider) {
            getLocationProvider();
        }
    };
}
