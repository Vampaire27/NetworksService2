package com.wwc2.networks.server.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.GPSBean;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.location.GpsPointManager;
import com.wwc2.networks.server.location.TraceBeanManager;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.TelephonyUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CarProvider extends ContentProvider {
    //authorities="wwc2.server.provider.carinfo"
    private static final String REGISTER_STATUS = "register_status";
    private static final String INFO_SERIAL = "info_serial";
    private static final String INFO_APP_URL = "info_app_url";
    private static final String WEATHER_URL = "weather_url";
    private static final String STORE_URL = "store_url";
    private static final String LOCATION = "location";
    private static final String ADDRESS = "address";
    private static final String HOST = "host";
    private static final String SIMSWICH = "simswich";
    private static final String SENSOR = "sensor";
    private String ret = "Unknown";


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String path = uri.getEncodedPath();
        String[] param = path.split("/");
        if (param.length <= 1) return ret;
        if (CarServiceClient.getContext() != null) {
            if (param[1].equals(HOST)) {
                ret = (String) SPUtils.get(CarServiceClient.getContext(),
                        Config.SYS_INTERFACE, Config.SYS_INTERFACE_URL);
            } else if (param[1].equals(REGISTER_STATUS)) {
//                boolean isReg = (boolean)
//                        SPUtils.get(CarServiceClient.getContext(), Config.IS_REGISTER, false);
//                ret = isReg == true ? "true" : "false";
            } else if (param[1].equals(INFO_SERIAL)) {
                String serial = DeviceManagement.newInstance(CarServiceClient.getContext())
                        .getSerialNumber();
                ret = serial;
            } else if (param[1].equals(INFO_APP_URL)) {
                String app_url = SPUtils.get(CarServiceClient.getContext(),
                        Config.SYS_INTERFACE, Config.SYS_INTERFACE_URL) + Config.APP_URL;
                ret = app_url;
            } else if (param[1].equals(WEATHER_URL)) {
                String w_url = (String) SPUtils.get(CarServiceClient.getContext(),
                        Config.APP_WEATHER, Config.APP_WEATHER_URL);
                ret = w_url;
            } else if (param[1].equals(STORE_URL)) {
                String w_url = (String) SPUtils.get(CarServiceClient.getContext(),
                        Config.APP_STORE, Config.APP_STORE_URL);
                ret = w_url;
            } else if (param[1].equals(LOCATION)) {
                GPSBean lGPSBean = GpsPointManager.getInstance().getGPSBean();
                if (lGPSBean != null) {
                    if (lGPSBean.getLat() > 0 && lGPSBean.getLng() > 0) {
                        ret = lGPSBean.getLng() + "," + lGPSBean.getLat();
                    }
                }
            } else if (param[1].equals(ADDRESS)) {
                String lAddress = TraceBeanManager.getInstance().getmLastAdderss();
                if (lAddress != null) {
                    if(TextUtils.isEmpty(lAddress)){
                        String tower = TelephonyUtil.newInstance(CarServiceClient.getContext()).getTowerInfo();
                        LogUtils.d("onLocationsChanged lbs tower:" + tower);
                        if (tower != null) {
                            NetWorkManagement.newInstance().postGetLbsInfo(tower)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<OtherBean>() {
                                        @Override
                                        public void accept(OtherBean otherBean) throws Exception {
                                            if (otherBean.getCode().equals(Config.App.RESULT_OK)) {
                                                String city = otherBean.getCity();
                                                if(!TextUtils.isEmpty(city)){
                                                    ret = city;
                                                }
                                            }
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                        }
                                    });
                        }
                    }else{
                        ret = lAddress;
                    }
                }
            } else if (param[1].equals(SIMSWICH)) {
                ret = SystemProperties.get("persist.sys.simswich", "1");
            } else if (param[1].equals(SENSOR)) {
                ret = (String) SPUtils.get(CarServiceClient.getContext(),
                        Config.SENSOR_LEVEL, "");
            }
        }
        return ret == null ? "Unknown" : ret;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
