package com.wwc2.networks;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.google.gson.Gson;
import com.wwc2.networks.server.action.LocationAction;
import com.wwc2.networks.server.bean.AMapBean;
import com.wwc2.networks.server.bean.RequestMsg;
import com.wwc2.networks.server.broadcast.wwc2Broadcast;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.location.AndroidLocations;
import com.wwc2.networks.server.location.LocationManagement;
import com.wwc2.networks.server.provider.greendao.DaoMaster;
import com.wwc2.networks.server.provider.greendao.DaoSession;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.AppUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.Crash;
import com.wwc2.networks.server.utils.GpsUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.Utils;
import com.wwc2.networks.server.wakeup.AccPowerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class CarServiceClient extends Application {
    private final String TAG = "SimpleTag";
    private static Context mContext;
    private static String mileageID = null;
    private static DaoSession daoSession;

    private static int accStatus = 0;
    private static ArrayList<AccStatusInterface> accList = null;


    private wwc2Broadcast.accStatusObserver statusObserver = null;
    private wwc2Broadcast.realAccStatusOberver mRealAccStatusOberver = null;

    static  boolean dvrEnable = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        String appName = AppUtils.getProcessName(this);
        LogUtils.d("Application name is " + appName);
        //com.wwc2.networks:ipc & io.rong.push cannot start  CarSystemServer... 20200709
        if("com.wwc2.networks".equals(appName)) {
            LogUtils.init(mContext);

            accList = new ArrayList<>();

            if (SPUtils.get(mContext, Config.SYS_PROTOCOL, "").equals("")) {
                SPUtils.put(mContext, Config.SYS_PROTOCOL, Config.SYS_PROTOCOL_URL);
                SPUtils.put(mContext, Config.URL_VERSION, Config.DEF_URL_VERSION);
            }
            if (SPUtils.get(mContext, Config.SYS_INTERFACE, "").equals("")) {
                SPUtils.put(mContext, Config.SYS_INTERFACE, Config.SYS_INTERFACE_URL);
            }
            if (SPUtils.get(mContext, Config.APP_STORE, "").equals("")) {
                SPUtils.put(mContext, Config.APP_STORE, Config.APP_STORE_URL);
            }
            if (SPUtils.get(mContext, Config.APP_WEATHER, "").equals("")) {
                SPUtils.put(mContext, Config.APP_WEATHER, Config.APP_WEATHER_URL);
            }


            setupDatabase();

            statusObserver = new wwc2Broadcast.accStatusObserver(
                    new Handler(), CarServiceClient.getContext());

            Uri uri_acc = Uri.parse("content://" + wwc2Broadcast.AUTHORITY + "/"
                    + wwc2Broadcast.ACC_STATUS);
            CarServiceClient.getContext().getContentResolver().
                    registerContentObserver(uri_acc,
                            true, statusObserver);


            mRealAccStatusOberver = new wwc2Broadcast.realAccStatusOberver(
                    new Handler(),CarServiceClient.getContext());
            Uri uri_real_acc = Uri.parse("content://" + wwc2Broadcast.AUTHORITY + "/"
                    + wwc2Broadcast.REAL_ACC_STATUS);
            CarServiceClient.getContext().getContentResolver().
                    registerContentObserver(uri_real_acc,
                            true, mRealAccStatusOberver);


            Crash crashHandler = Crash.getInstance();
            crashHandler.init(mContext);

            startService();
        }
    }

    public static boolean getDvrEnable(){

//        Uri uri_dvr = Uri.parse("content://" + wwc2Broadcast.AUTHORITY
//                + "/" + wwc2Broadcast.DVR_ENABLE);
//
//        if("true".equals(mContext.getContentResolver().getType(uri_dvr))){
//            dvrEnable = true;
//        }else{
//            dvrEnable = false;
//        }
        //return dvrEnable;
        //for 360 persist.sys.dvr_enable is true.... but uri_dvr is false.
        String  value  = SystemProperties.get("persist.sys.dvr_enable","true");
        if("true".equals(value)){
            return  true;
        }else{
            return false;
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getMileageID() {
        return mileageID;
    }

    public static String getFilePath(){
        return mContext.getFilesDir().getPath() + File.separator;
    }
    public static String getFilePath(String pathName){
        return mContext.getFileStreamPath(pathName).getPath() + File.separator;
    }

    public static void createMileageID() {
        String uniqueID = UUID.randomUUID().toString();
        CarServiceClient.mileageID = uniqueID;
    }

    public static void cleanMileageID() {
        mileageID = null;
    }

    private void setupDatabase() {
        try {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getContext(),
                    "carService.db", null);
            SQLiteDatabase db = helper.getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(db);
            daoSession = daoMaster.newSession();
        } catch (Exception e) {
            LogUtils.d("setupDatabase()   e=" + e);
        }
    }

    public static DaoSession getDaoInstant() {
        return daoSession;
    }

    public static int getAccStatus() {
        LogUtils.d("getAccStatus   accStatus=" + accStatus);
        int accStatus = Config.System.ACC_OFF;
        Uri uri_acc = Uri.parse("content://" + wwc2Broadcast.AUTHORITY
                + "/" + wwc2Broadcast.ACC_STATUS);
        String strAcc = CarServiceClient.getContext().getContentResolver().getType(uri_acc);

        LogUtils.d("getAccStatus strAcc:"+ strAcc );

        switch (strAcc){
            case "true":
                accStatus = Config.System.ACC_ON;
                break;
            case "false":
                accStatus = Config.System.ACC_OFF;
                break;
            default:
                break;
        }
        return  accStatus;
    }

    public static void setAccStatus(int status) {
        accStatus = status;

        if (accList != null) {
            for (int i = 0; i < accList.size(); i++) {
                accList.get(i).onAccStatusChanged(accStatus);
            }
        }
    }

    public  static void updateAccStateFromContentResolover(){
        Uri uri_acc = Uri.parse("content://" + wwc2Broadcast.AUTHORITY
                + "/" + wwc2Broadcast.ACC_STATUS);
        String strAcc = CarServiceClient.getContext().getContentResolver().getType(uri_acc);

        LogUtils.d("updateAccStateFromContentResolover strAcc:"+ strAcc );

        switch (strAcc){
            case "true":
                CarServiceClient.setAccStatus(Config.System.ACC_ON);
                break;
            case "false":
                CarServiceClient.setAccStatus(Config.System.ACC_OFF);
                CarServiceClient.removeAccListener();
                break;
             default:
                 break;
        }
    }

    public static void setAccStatusInterface(AccStatusInterface acc) {
        if (accList != null) {
            accList.add(acc);
        }
    }

    public static void removeAccStatusInterface(AccStatusInterface acc) {
        if (accList != null) {
            accList.remove(acc);
        }
    }

    public static void removeAccListener() {
        if (accList != null) {
            accList.clear();
        }
    }

    public static void startService() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                mContext.getPackageName() + ".CarSystemServer");
        intent.setComponent(componentName);
        mContext.startService(intent);

    }

    public static void stopService() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                mContext.getPackageName() + ".CarSystemServer");
        intent.setComponent(componentName);
        mContext.stopService(intent);
    }

    public static boolean isRunning() {
        boolean isRun = Utils.isServiceRunning(mContext,
                mContext.getPackageName() + ".CarSystemServer");
        return isRun;
    }

    public interface AccStatusInterface {
        void onAccStatusChanged(int status);
    }

    private static LogsStatusInterface logsStatusInterface = null;
    public static void updateLogs(String log){
        if(!TextUtils.isEmpty(log)){
            if(logsStatusInterface != null){
                logsStatusInterface.onLogsStatusChanged(log);
            }
        }
    }

    public static void setLogsStatusInterface(LogsStatusInterface logInterface){
        if(logInterface != null){
            logsStatusInterface = logInterface;
        }
    }

    public interface LogsStatusInterface{
        void onLogsStatusChanged(String log);
    }
}
