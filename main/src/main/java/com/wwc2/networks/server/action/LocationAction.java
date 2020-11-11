package com.wwc2.networks.server.action;

import android.content.Context;
import android.os.AsyncTask;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * Created by swd1 on 17-10-11.
 */

public class LocationAction implements AMapLocationListener {
    private static LocationAction action = null;
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private Context context;
    protected LocationCallback callback;

    private LocationAction(Context context) {
        this.context = context;
        initLoc();
    }

    //定位
    private void initLoc() {
        //初始化定位
        mLocationClient = new AMapLocationClient(context.getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：启动定位时SDK会返回最近3s内精度最高的一次定位结果。
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置是否使用缓存定位，默认为true
        mLocationOption.setLocationCacheEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
    }

    public static void stopLocation() {
        if (action != null) {
            action.stop();
        }
    }

    private void stop() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
    }

    public static LocationAction newInstance(Context context) {
        if (action == null) {
            action = new LocationAction(context);
        }
        return action;
    }

    public void getLocation(LocationCallback callback) {
        this.callback = callback;
        new MyAsyncTask().execute();
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            //启动定位
            mLocationClient.startLocation();
            return null;
        }
    }

    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
//                //获取纬度
//                double latitude = amapLocation.getLatitude();
//                //获取经度
//                double longitude = amapLocation.getLongitude();
//                //地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//                String address = amapLocation.getAddress();
//                String city = amapLocation.getCity();
                if (callback != null) {
                    callback.onLocationResults(amapLocation);
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                if (callback != null) {
                    callback.onLocationFailure(amapLocation.getErrorCode());
                }
            }
        }
    }

    public interface LocationCallback {
        public void onLocationFailure(int errorCode);

        public void onLocationResults(AMapLocation aMapLocation);
    }
}
