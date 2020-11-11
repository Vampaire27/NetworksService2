package com.wwc2.networks;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.wwc2.networks.WatchDog.DebugSignal;
import com.wwc2.networks.server.RongYun.RongYunConnect;
import com.wwc2.networks.server.auto.AutoManagement;
import com.wwc2.networks.server.bean.JsimBean;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.bean.ZWCardBean;
import com.wwc2.networks.server.bean.ZWResponseBean;
import com.wwc2.networks.server.broadcast.wwc2Broadcast;
import com.wwc2.networks.server.canbus.CanBusNettyManager;
import com.wwc2.networks.server.canbus.CanbusDriver;
import com.wwc2.networks.server.canbus.CarInfoListener;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.dvr.Dvr360Imp;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.dvr.DvrSettings;
import com.wwc2.networks.server.dvr.jni.DvrDataNative;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.location.GpsPointManager;
import com.wwc2.networks.server.location.LocationManagement;
import com.wwc2.networks.server.location.TraceBeanManager;
import com.wwc2.networks.server.netty.ClientConnect;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.AppUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.NetworkUtil;
import com.wwc2.networks.server.utils.ShellUtils;
import com.wwc2.networks.server.utils.TelephonyUtil;
import com.wwc2.networks.server.utils.Utils;
import com.wwc2.networks.server.view.WarnDialog;
import com.wwc2.networks.server.wakeup.AccPowerManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.telephony.PhoneStateListener;

public class CarSystemServer extends Service
        implements CarServiceClient.AccStatusInterface,
        AutoManagement.AutoCallback, SensorEventListener {
    private static final String TAG = "CarSystemServer";
    private Handler handler = null;
    private String jPushID = null;
    private CanbusDriver canbusDriver = null;
    private CarInfoListener carInfoListener = null;
    private Map<String, String> sim_config = null;
    private String curIccid = null;
//    private boolean systimeSuccess;
    private double oldLatitude;
    private double oldLongitude;
    private double oldGaodeLatitude;
    private double oldGaodeLongitude;
    private int simCount = 0;
    private boolean simAlert = false;
    private boolean countAlert = false;
    //注册次数
    private int isRegister = 0;
    //最大重试次数
    private final int MAX_REGISTER = 5;
    private final int MAX_ID = MAX_REGISTER;
    private Context context = null;
    private DvrManagement dvrManagement = null;
    private ConnectBroadcastReceiver mConnectBroadcastReceiver = null;

    private NetWorkRYConnectThread mNetWorkRYConnectThread = null;

    private RongYunConnect mRongYunConnect;
    DvrSettings mDvrSettings;
    private DebugSignal mDebugSignal;
    private SensorManager mSensorManager = null; //open g-sensor for
    private final int RATE_100HZ_IN_US = 10000;

    @Override
    public void onCreate() {
        super.onCreate();
        if(AutoManagement.newInstance(this).mUploadLevel.isDisableByService()){
            return;
        }

        mDebugSignal = new DebugSignal(this);
        mDebugSignal.init();

        String appName = AppUtils.getProcessName(this);
        LogUtils.d(" Server Application name is " + appName);
        context = CarServiceClient.getContext();
        CarServiceClient.updateAccStateFromContentResolover();

        AccPowerManager.init(this);
        AccPowerManager.getInstance().registerReceiver(this);

        if (handler == null) {
            handler = new Handler();
        }

        if((boolean)SPUtils.get(CarServiceClient.getContext(),
                Config.RY_APP_REGISTER, false)) {
            mRongYunConnect = new RongYunConnect(handler);
            mRongYunConnect.init();
            LogUtils.d(" app register . init " );
        }else{
            LogUtils.d(" app not register , not init ry" );
        }

        mDvrSettings = new DvrSettings(this);
        mDvrSettings.registerDVRBroadcast();

        if (mConnectBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mConnectBroadcastReceiver = new ConnectBroadcastReceiver();
            registerReceiver(mConnectBroadcastReceiver, intentFilter);
        }


        if (context != null ) {

            canbusDriver = CanbusDriver.getInstance();
            canbusDriver.bindService(context);
            carInfoListener = new CarInfoListener();
            canbusDriver.registerCarInfoUIListener(carInfoListener);


            CarServiceClient.removeAccListener();
            CarServiceClient.setAccStatusInterface(this);

            curIccid = TelephonyUtil.getMime(context);
            boolean state = NetworkUtil.isNetworkAvailable(CarSystemServer.this);
            if(!state){
//                boolean isDisable = (boolean) SPUtils.get(context,
//                        Config.IS_DISABLE, true);
//                LogUtils.d("onCreate isDisable=" + isDisable);
//                if(!isDisable){
//                    LogUtils.d("onCreate 离线状态，当前被禁用！");
//                    return;
//                }

                checkSystemOs();
            }

            Utils.setLocationMode(context, 3);

            //checkNetwork();
        }

        GpsPointManager.getInstance().initAndRegister();
        TraceBeanManager.getInstance().init();

        if(Config.System.ACC_ON ==CarServiceClient.getAccStatus()) {
            LocationManagement.newInstance().startLocations();
            TraceBeanManager.getInstance().startTrace();
        }

        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        String platformType  = device.getPlatformType();

        if(Config.TYPE_360.equals(platformType)){
            LogUtils.d("is  360  type  !");
            dvrManagement = DvrManagement.newInstance();
            dvrManagement.initDvrData(new Dvr360Imp(CarSystemServer.this));
        }else if(CarServiceClient.getDvrEnable()){
            dvrManagement = DvrManagement.newInstance();
            dvrManagement.initDvrData();
        }else{
            LogUtils.d("__dvrEnable=false!...");
        }

       if(mNetWorkRYConnectThread == null ) {
           mNetWorkRYConnectThread = new NetWorkRYConnectThread();
           mNetWorkRYConnectThread.start();
       }
        openSensor();
    }

    private void openSensor(){
        if(mSensorManager == null) {
            mSensorManager = (SensorManager) getApplicationContext()
                    .getSystemService(Context.SENSOR_SERVICE);
            Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

   private void closeSensor(){
        if(mSensorManager != null){
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d("===onStartCommand!");
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Android GPS Service")
                .setContentText("never kill me！");
        startForeground(8888, builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //LogUtils.i("onSensorChanged event="+event.values.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
       // LogUtils.d("onAccuracyChanged!...");
    }


    private final class NetWorkRYConnectThread extends Thread {
        public NetWorkRYConnectThread() {
            super("NetWorkRYConnectThread- start");
        }

        @Override
        public void run() {
            super.run();
            boolean state = NetworkUtil.isNetworkAvailable(CarSystemServer.this);

            while(!state){
                LogUtils.d("NetWorkRYConnectThread Network is unconnect, sleep 3s " );
                try {
                    Thread.sleep(3000);
                }catch (InterruptedException e){

                }
                state = NetworkUtil.isNetworkAvailable(CarSystemServer.this);
            }


                ClientConnect.getInstance().createClient(CarSystemServer.this);
                ClientConnect.getInstance().ResetLoginTime();

                //hzy todo:
                //NetWorkManagement.newInstance().postUploadInfo(null, null, false);
                checkSystemOs();

                AutoManagement.newInstance(context).registerCar(
                        (String) SPUtils.get(context, Config.JP_ID, ""),
                        CarSystemServer.this);

            rongYunConnect();

            while(!DvrManagement.newInstance().checkDVRConnect()) {
                try {
                    Thread.sleep(3000);
                }catch (InterruptedException e){
                }
            }

            checkUploadOs();
            CanBusNettyManager.getInstance().postAccInfoByCanBusCMD(CarServiceClient.getAccStatus());
            mNetWorkRYConnectThread = null;

        }
    }

//    private void checkNetwork() {
//        handler.removeCallbacks(networkRun);
//        handler.post(networkRun);
//    }

//    private Runnable networkRun = new Runnable() {
//        @Override
//        public void run() {
//            boolean state = NetworkUtil.isNetworkAvailable(CarSystemServer.this);
//            if (!state) {
//                handler.removeCallbacks(networkRun);
//                handler.postDelayed(networkRun, 3000);
//                return;
//            }
//            LogUtils.d("check network state is " + state);
//            handler.post(runReg);
//        }
//    };

    @Override
    public void onSuccess(OtherBean otherBean) {

        String status = otherBean.getStatus();
        String logType = otherBean.getLogType();
        String config = otherBean.getConfigVersion();
        int time = otherBean.getNetworkTime();

        String mToken = otherBean.getToken();
        LogUtils.d(" mToken =" + mToken );
        if(mToken != null  &&  !"unbind".equals(mToken)){
            SPUtils.put(CarServiceClient.getContext(),
                    Config.RY_APP_REGISTER, true);
            rongYunInitAndConnect(mToken);
        }

        switch (status){
            case "1"://enable
            case "2"://enable
                SPUtils.put(CarServiceClient.getContext(),
                        Config.IS_DISABLE, true);
                mallRequired();

                String oldConfig = (String) SPUtils.get(context, Config.URL_VERSION, Config.DEF_URL_VERSION);
                LogUtils.d("check Config oldConfig=" + oldConfig + ",config=" + config);
                if(! config.equals(oldConfig)){
                    updateConfig(config);
                }

                checkUploadOsForChange();

                //checkInfos(); gps info has use the new connect machine 20191204. no need save..


                netCheckSims();

                checkSimOrAdvertising();

                if (Utils.isCh004Js(CarSystemServer.this)) {
                    jsimAlert();
                }

                if(time > 0){
                    LogUtils.d("onSuccess second=" + time);
                    handler.removeCallbacks(uploadNetwork);
                    handler.postDelayed(uploadNetwork, time * 1000);
                }

                break;
            case "4"://disable
                SPUtils.put(CarServiceClient.getContext(),
                        Config.IS_DISABLE, false);
                mallRequired();
                break;
        }
    }


    @Override
    public void onFailure() {
        if(isRegister <= MAX_REGISTER){
            isRegister++;
            LogUtils.d("run reg failure!  isRegister=" + isRegister);
            if(mNetWorkRYConnectThread == null){
                mNetWorkRYConnectThread =new NetWorkRYConnectThread();
                mNetWorkRYConnectThread.start();
            }
           // handler.removeCallbacks(runReg);
           // handler.postDelayed(runReg, 5000);
        }else{
            LogUtils.d("register error! return");
            //handler.removeCallbacks(runReg);
            SPUtils.put(context, Config.JP_ID, "");
        }
    }

    private class ConnectBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                boolean isAvailable = NetworkUtil.isNetworkAvailable(context);
                int acc = CarServiceClient.getAccStatus();
                String temp = (acc == Config.System.ACC_ON ? "ACC_ON" : "ACC_OFF");
                LogUtils.d("connectivity receiver acc:" + temp + ",isAvailable:" + isAvailable);
                if (isAvailable && acc == Config.System.ACC_ON) {
                    LogUtils.d("connectivity receiver 网络波动、恢复正常，同步数据");
                    //checkInfos(); //todo:

                    String sensor = (String) SPUtils.get(CarServiceClient.getContext(),
                            Config.SENSOR_LEVEL, "");
                    if(!sensor.equals("")){
                        NetWorkManagement.newInstance().postSensorApi(sensor).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<OtherBean>() {
                                    @Override
                                    public void accept(OtherBean otherBean) throws Exception {
                                        LogUtils.d("post SensorApi code=" + otherBean.getCode());
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        LogUtils.d("post SensorApi err...throwable=" + throwable);
                                        NetworkUtil.showNetworkLog();
                                    }
                                });
                    }
                    mDvrSettings.syncSettings();
                }

                //检测连接
                if(ClientConnect.getInstance().getChannel()!= null
                        &&!ClientConnect.getInstance().getChannel().isActive()){
                    LogUtils.d("connectivity 重新注册连接......");
                    ClientConnect.getInstance().createClient(CarSystemServer.this);
                    rongYunConnect();
                }

                //need checkry
            }
        }
    }


    private void rongYunConnect(){
        if(mRongYunConnect == null){
            LogUtils.d("  mRongYunConnect not init " );
            return;
        }else{
            LogUtils.d("  mRongYunConnect ..int done.. " );
        }
        mRongYunConnect.getRYtokenAndConnect();
    }

    public void rongYunInitAndConnect(String token){

        LogUtils.d("   rongYunInitAndConnect  =" +mRongYunConnect );
        if(mRongYunConnect == null){
            mRongYunConnect = new RongYunConnect(handler);
            mRongYunConnect.init();
        }
        if(token != null){
            mRongYunConnect.setToken(token);
        }
        rongYunConnect();
    }

    private Runnable uploadNetwork = new Runnable() {
        @Override
        public void run() {
            int oldType = (int) SPUtils.get(CarServiceClient.getContext(),
                    NetworkUtil.NET_TYPE, -1);
            final int type = NetworkUtil.getNetworkType(CarServiceClient.getContext());
            LogUtils.d("uploadNetwork...type=" + type + ",,oldType=" + oldType);
            if(oldType != type){
                if(type > 0){
                    NetWorkManagement.newInstance().postNetworkType(type)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<OtherBean>() {
                                @Override
                                public void accept(OtherBean otherBean) throws Exception {
                                    if (otherBean.getCode().equals(Config.App.RESULT_OK)) {
                                        LogUtils.d("postNetworkType _ 请求成功！");
                                        SPUtils.put(CarServiceClient.getContext(),
                                                NetworkUtil.NET_TYPE, type);
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    LogUtils.d("postNetworkType _ throwable :" + throwable.toString());
                                    NetworkUtil.showNetworkLog();
                                }
                            });
                }
            }
        }
    };

    private Runnable defUpdateConfig = new Runnable() {
        @Override
        public void run() {
            Call<OtherBean> call = NetWorkManagement.newInstance().postGetConfig(Config.SYS_INTERFACE_URL);
            call.enqueue(new Callback<OtherBean>() {
                @Override
                public void onResponse(Call<OtherBean> call, Response<OtherBean> response) {
                    if (response.code() == Config.App.RESULT_CODE) {
                        try {
                            String json = response.body().getConfig();
                            JSONArray array = new JSONArray(json);
                            for (int i = 0; i < array.length(); i++) {
                                JSONArray config = array.getJSONArray(i);
                                String style = config.getString(0);
                                String value = config.getString(1);
                                if (style.equals(Config.SYS_PROTOCOL)) {
                                    SPUtils.put(CarServiceClient.getContext(),
                                            Config.SYS_PROTOCOL, "http://" + value);
                                } else if (style.equals(Config.SYS_INTERFACE)) {
                                    SPUtils.put(CarServiceClient.getContext(),
                                            Config.SYS_INTERFACE, "http://" + value);
                                } else if (style.equals(Config.APP_STORE)) {
                                    SPUtils.put(CarServiceClient.getContext(),
                                            Config.APP_STORE, "http://" + value);
                                } else if (style.equals(Config.APP_WEATHER)) {
                                    SPUtils.put(CarServiceClient.getContext(),
                                            Config.APP_WEATHER, "http://" + value);
                                }
                            }
                        } catch (JSONException e) {
                            LogUtils.d("defUpdateConfig e:" + e.toString());
                        }
                    }
                }

                @Override
                public void onFailure(Call<OtherBean> call, Throwable t) {
                }
            });
        }
    };

    private void updateConfig(final String config) {
        Call<OtherBean> call = NetWorkManagement.newInstance().postGetConfig(null);
        call.enqueue(new Callback<OtherBean>() {
            @Override
            public void onResponse(Call<OtherBean> call, Response<OtherBean> response) {
                if (response.code() == Config.App.RESULT_CODE) {
                    try {
                        String json = response.body().getConfig();
                        JSONArray array = new JSONArray(json);
                        SPUtils.put(context, Config.URL_VERSION, config);
                        for (int i = 0; i < array.length(); i++) {
                            JSONArray config = array.getJSONArray(i);
                            String style = config.getString(0);
                            String value = config.getString(1);
                            if (style.equals(Config.SYS_PROTOCOL)) {
                                SPUtils.put(CarServiceClient.getContext(),
                                        Config.SYS_PROTOCOL, "http://" + value);
                            } else if (style.equals(Config.SYS_INTERFACE)) {
                                SPUtils.put(CarServiceClient.getContext(),
                                        Config.SYS_INTERFACE, "http://" + value);
                            } else if (style.equals(Config.APP_STORE)) {
                                SPUtils.put(CarServiceClient.getContext(),
                                        Config.APP_STORE, "http://" + value);
                            } else if (style.equals(Config.APP_WEATHER)) {
                                SPUtils.put(CarServiceClient.getContext(),
                                        Config.APP_WEATHER, "http://" + value);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    handler.post(defUpdateConfig);
                }
            }

            @Override
            public void onFailure(Call<OtherBean> call, Throwable t) {
                handler.post(defUpdateConfig);
            }
        });
    }

    private Runnable showWarn = new Runnable() {
        @Override
        public void run() {

            LogUtils.d("checkSystemOs _ showWarn  mDialog=" + mDialog);

            if(mDialog != null){
                mDialog.dismiss();
            }
            WarnDialog.Builder builder = new WarnDialog.Builder(context);
            mDialog = builder.create();
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mDialog.show();

            handler.removeCallbacks(showWarn);
            handler.postDelayed(showWarn, DEF_WARN);
        }
    };

    private int DEF_WARN = 1000 * 60 * 5;
    private WarnDialog mDialog;
    private void checkSystemOs(){
        boolean isExists = FileUtils.fileIsExists("/vendor/lib/libwwc2verify.so");
        LogUtils.d("checkSystemOs _ isExists :" + isExists);
        if(isExists){
            DvrDataNative dvrDataNative = DvrDataNative.newInstance();
            int status = dvrDataNative.getStatus();
            LogUtils.d("checkSystemOs _ status :" + status);
            //0=未通过时才校验
            if(status == 0){
                boolean state = NetworkUtil.isNetworkAvailable(CarSystemServer.this);
                if(!state){
                    handler.removeCallbacks(showWarn);
                    handler.post(showWarn);
                }else{
                    NetWorkManagement.newInstance().postActivateDevice()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<OtherBean>() {
                                @Override
                                public void accept(OtherBean otherBean) throws Exception {
                                    LogUtils.d("checkSystemOs _ otherBean :" + otherBean.toString());
                                    if (otherBean.getCode().equals(Config.App.RESULT_OK)) {
                                        String sign = otherBean.getDeviceSign();

                                        LogUtils.d("checkSystemOs_ 校验成功！");
                                        ShellUtils.CommandResult commandResult =
                                                ShellUtils.execCommand("/system/bin/jbset 4 " + sign, false);
                                        LogUtils.d("checkSystemOs_ result=" + commandResult.result + ",,successMsg="
                                                + commandResult.successMsg + ",,errorMsg=" +commandResult.errorMsg);

                                        if(mDialog != null){
                                            mDialog.dismiss();
                                        }
                                        handler.removeCallbacks(showWarn);

                                    }else{
                                        LogUtils.d("checkSystemOs_ 校验失败！");

                                        handler.removeCallbacks(showWarn);
                                        handler.post(showWarn);

                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    LogUtils.d("checkSystemOs _ throwable :" + throwable.toString());

                                    LogUtils.d("checkSystemOs_ 请求异常！");

                                    handler.removeCallbacks(showWarn);
                                    handler.post(showWarn);
                                }
                            });
                }
            }
        }
    }

//    private int checksum = 0;
//    private Runnable jPushRun = new Runnable() {
//        @Override
//        public void run() {
//            if(JPushInterface.isPushStopped(CarServiceClient.getContext())){
//                LogUtils.d("checkID run jpush resumePush!!");
//                JPushInterface.resumePush(CarServiceClient.getContext());
//            }
//            jPushID = JPushInterface.getRegistrationID(CarServiceClient.getContext());
//            LogUtils.d("checkID run jpush jPushID:" + jPushID);
//            if (!jPushID.equals("") && jPushID != null) {
//                checksum = 0;
//                handler.removeCallbacks(jPushRun);
//                if (! SPUtils.get(context, Config.JP_ID, "").equals(jPushID)) {
//                    SPUtils.put(context, Config.JP_ID, jPushID);
//                    AutoManagement.newInstance(context).registerCar(jPushID, null);
//                }
//            } else {
//                checksum++;
//                if(checksum >= MAX_ID){
//                    JPushInterface.stopPush(context);
//                    try {
//                        Thread.sleep(2000);
//                    }catch (Exception e){}
//                    JPushInterface.resumePush(context);
//                }else{
//                    handler.removeCallbacks(jPushRun);
//                    handler.postDelayed(jPushRun, 3000);
//                }
//            }
//        }
//    };


    @Override
    public void onAccStatusChanged(int status) {

        if (status == Config.System.ACC_ON) {
            LogUtils.d("acc status change to ACC_ON");
            openSensor();
            LocationManagement.newInstance().startLocations();


            if(NetworkUtil.isNetworkAvailable(context)) {
                if (ClientConnect.getInstance().getChannel() != null
                        && !ClientConnect.getInstance().getChannel().isActive()) {
                    LogUtils.d("ACC_ON connectivity createClient......");
                    ClientConnect.getInstance().createClient(CarSystemServer.this);
                }
                mDvrSettings.syncSettings();

                AutoManagement.newInstance(context).registerCar(
                        (String) SPUtils.get(context, Config.JP_ID, ""),
                        CarSystemServer.this);
            }
            ClientConnect.getInstance().ResetLoginTime();

        } else if (status == Config.System.ACC_OFF) {
            LogUtils.d("acc status change to ACC_OFF");
            LogUtils.d("postUploadInfo4444444444");
            closeSensor();
            //hzy todo
           // NetWorkManagement.newInstance().postUploadInfo(null, null, false);

            if(handler != null) {
                handler.removeCallbacksAndMessages(null);
            }

            LocationManagement.newInstance().stopLocations();
           // LocationManagement.newInstance().getAndroidLocations().removeAllListener();

            if(mDialog != null){
                mDialog.dismiss();
            }
        }

        CanBusNettyManager.getInstance().postAccInfoByCanBusCMD(status);
    }

    private void mallRequired() {
        NetWorkManagement.newInstance().postGetMallRequired(new Callback<OtherBean>() {
            @Override
            public void onResponse(Call<OtherBean> call, Response<OtherBean> response) {
                if (response.code() == Config.App.RESULT_CODE) {
                    try {
                        String code = response.body().getCode();
                        LogUtils.d("...mallRequired...code=" + code);
                        if (code.equals(Config.App.RESULT_OK)) {
                            final List<OtherBean.DataBean> data = response.body().getData();
                            LogUtils.d("...mallRequired...data.size=" + data.size());
                            if(data.size() > 0){
                                for (int i = 0; i < data.size(); i++) {
                                    String pack = data.get(i).getPackageName();
                                    String ver = data.get(i).getVersion();
                                    String verCode = data.get(i).getVersionCode();
                                    String url = data.get(i).getAppUrl();
                                    //是否强制升级,1否2是
                                    int isForce = data.get(i).getIsForce();
                                    String version = AppUtils.getAppVersionName(
                                            CarServiceClient.getContext(),
                                            pack);
                                    if (version != null) {
                                        LogUtils.d("...mallRequired...version=" + version + ",,ver=" + ver);
                                        if (!version.equals(ver)) {
                                            LogUtils.d("...mallRequired...isForce=" + isForce);
                                            if(isForce == 2){
                                                LogUtils.d("..强制...mallRequired...url=" + url);
                                                NetWorkManagement.newInstance().dowAndInsApk(url);
                                            }else if(isForce == 1){
                                                int versionCode = AppUtils.getAppVersionCode(
                                                        CarServiceClient.getContext(),
                                                        pack);
                                                int newCode = Integer.parseInt(verCode);
                                                LogUtils.d("..mallRequired...oldVersionCode="
                                                        + versionCode + ",,newCode=" + newCode);
                                                if(newCode > versionCode){
                                                    LogUtils.d(".升级..mallRequired...url=" + url);
                                                    NetWorkManagement.newInstance().dowAndInsApk(url);
                                                }else{
                                                    LogUtils.d("...mallRequired...code error..return!");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }catch (Exception e){
                        LogUtils.d("mallRequired...e=" + e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<OtherBean> call, Throwable t) {
                LogUtils.d("postGetMallRequired...onFailure");
            }
        });
    }

    private Runnable adverRunable = new Runnable() {
        @Override
        public void run() {
            LogUtils.d("adverRunable _ time end!");
            sendAdBroad("2");
        }
    };

    private int simOrAdvertisErr = 0;
    private Runnable simOrAdvertis = new Runnable() {
        @Override
        public void run() {
            if(simOrAdvertisErr < 3){
                LogUtils.e("checkSimOrAdvertising : 重试中！");
                simOrAdvertisErr++;
                checkSimOrAdvertising();
            }else{
                LogUtils.e("checkSimOrAdvertising : 重试失败！");
                simOrAdvertisErr = 0;
                sendAdBroad("2");
            }
        }
    };
    private void checkSimOrAdvertising(){
        //只处理ch004
        if (Utils.isCh004Js(CarSystemServer.this)) {
            String iccid = TelephonyUtil.getMime(CarServiceClient.getContext());
            if(iccid != null){
                //1.查询佳圣sim卡是否需要充值
                NetWorkManagement.newInstance().getQueryCard()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<JsimBean>() {
                            @Override
                            public void accept(JsimBean jsimBean) throws Exception {
                                if(jsimBean == null){
                                    return;
                                }
                                LogUtils.d("checkSimOrAdvertising _ jsimBean : " + jsimBean.toString());
                                String code = jsimBean.getError_code();
                                try {
                                    //表示此卡需要续费，要弹出提醒
                                    if ("0".equals(code) || "3".equals(code)) {
                                        //提醒主页流量管理图标
                                        sendFlowBroad(wwc2Broadcast.FLOW_TYPE_JS);
                                        //获取广告语
                                        NetWorkManagement.newInstance().postGetAdvertising()
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<OtherBean>() {
                                                    @Override
                                                    public void accept(OtherBean otherBean) throws Exception {
                                                        LogUtils.d("checkSimOrAdvertising _ otherBean :" + otherBean.toString());
                                                        String status = otherBean.getStatus();
                                                        switch (status){
                                                            case "1":
                                                                int minute = otherBean.getMinute();
                                                                if(minute > 0){
                                                                    //开启倒计时
                                                                    handler.removeCallbacks(adverRunable);
                                                                    handler.postDelayed(adverRunable, Utils.getDelayMillis(minute));
                                                                }
                                                                Intent broad = new Intent();
                                                                broad.setAction(wwc2Broadcast.AD_ACTION);
                                                                broad.putExtra(wwc2Broadcast.AD_STATUS, "1");
                                                                broad.putExtra(wwc2Broadcast.AD_TITLE, otherBean.getTitle());
                                                                broad.putExtra(wwc2Broadcast.AD_PKG, otherBean.getPackageName());
                                                                sendBroadcast(broad);
                                                                break;
                                                            case "2":
                                                                sendAdBroad(status);
                                                                break;
                                                        }
                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        sendAdBroad("2");
                                                    }
                                                });
                                    }
                                } catch (Exception e) {
                                    LogUtils.d("checkSimOrAdvertising---e=" + e);
                                    sendAdBroad("2");
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                LogUtils.e("checkSimOrAdvertising -- t=" + throwable);
                                handler.removeCallbacks(simOrAdvertis);
                                handler.postDelayed(simOrAdvertis, 3000);
                            }
                        });
            }
        }
    }

    private void sendAdBroad(String status){
        LogUtils.d("sendAdBroad _ status=" + status);
        Intent broad = new Intent();
        broad.setAction(wwc2Broadcast.AD_ACTION);
        broad.putExtra(wwc2Broadcast.AD_STATUS, status);
        broad.putExtra(wwc2Broadcast.AD_TITLE, "");
        broad.putExtra(wwc2Broadcast.AD_PKG, "");
        sendBroadcast(broad);
    }

    private void sendFlowBroad(int type){
        LogUtils.d("sendFlowBroad---type=" + type);
        Intent broad = new Intent();
        broad.setAction(wwc2Broadcast.FLOW_ACTION);
        broad.putExtra(wwc2Broadcast.FLOW_TYPE, type);
        switch (type){
            case wwc2Broadcast.FLOW_TYPE_JS:
                broad.putExtra(wwc2Broadcast.FLOW_PKG, wwc2Broadcast.FLOW_PKG_JS);
                break;
            case wwc2Broadcast.FLOW_TYPE_ZW:
                broad.putExtra(wwc2Broadcast.FLOW_PKG, wwc2Broadcast.FLOW_PKG_ZW);
                break;
        }
        sendBroadcast(broad);
    }

    private void checkZWCard(){
        NetWorkManagement.newInstance().getQueryZWCard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ZWResponseBean>() {
                    @Override
                    public void accept(ZWResponseBean entry) throws Exception {
                        LogUtils.d("checkZWCard---entry=" + entry.toString());
                        if (entry != null) {
                            if (entry.getErrCode() == 0) {
                                ZWCardBean card = null;
                                try {
                                    Gson gson = new Gson();
                                    String cont = entry.getMsg().toString();
                                    cont = cont.replace("using_service_package=,", "using_service_package=0,");
                                    card = gson.fromJson(cont, ZWCardBean.class);
                                } catch (Exception ex) {
                                    LogUtils.d("gson card error.");
                                    ex.printStackTrace();
                                }
                                if (card != null) {
                                    long flow = card.getDatausage_curremaining() / (1024 * 1024);
                                    LogUtils.d("checkZWCard---flow=" + flow);
                                    //50M试用流量
                                    if(flow <= Config.ZW_DEF_FLOW){
                                        sendFlowBroad(wwc2Broadcast.FLOW_TYPE_ZW);
                                    }
                                }
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.d("checkZWCard---t=" + throwable);
                    }
                });
    }
//
//    private void checkInfos() {
//        final List<Info> info = CarServiceClient.getDaoInstant().getInfoDao().loadAll();
//        if (info == null) {
//            return;
//        }
//        LogUtils.d("upload history info:" + info.size());
//
//        if(info.size() > 0){
//            ThreadUtil.start(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        for (int i = 0; i < info.size(); i++) {
//                            Thread.sleep(3000);
//                            LogUtils.d("postUploadInfo555555555555");
//                            NetWorkManagement.newInstance().postUploadInfo(null, info.get(i), true);
//                        }
//                    } catch (Exception e) {
//                        LogUtils.e("e:" + e.toString());
//                    }
//                }
//            });
//        }
//    }

    private void checkUploadOsForChange() {
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        int size = CarServiceClient.getDaoInstant().getDeviceDao().loadAll().size();
        LogUtils.e("checkUploadOs   size:" + size);
        if (size <= 0) {
            CarServiceClient.getDaoInstant().
                    getDeviceDao().insertOrReplace(device.getDevice());
            checkUploadOs();
        } else {
            LogUtils.e("checkUploadOs   device.checkUpdate():" + device.checkUpdate());
            if (device.checkUpdate()) {
                CarServiceClient.getDaoInstant().getDeviceDao().update(device.getDevice());
                checkUploadOs();
            }
        }
    }

    public void checkUploadOs() {
        NetWorkManagement.newInstance().postUploadOs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        LogUtils.e("uploadOs   result:" + result);
                        if (result.equals(Config.System.UPLOAD_OS_OK)) {
                            mDvrSettings.setSyncFail(false);
                        } else {
                            mDvrSettings.setSyncFail(true);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e("uploadOs   throwable:" + throwable);
                        NetworkUtil.showNetworkLog();
                        mDvrSettings.setSyncFail(true);
                        //  CarServiceClient.getDaoInstant().getDeviceDao().deleteAll();
                    }
                });
    }


    private boolean initSims() {
        //读取SIM鉴权配置文件
        sim_config = FileUtils.readFile(Config.Local.SIM_CONFIG_PATH);
        if (sim_config != null && sim_config.size() > 0) {
            String auth = sim_config.get(Config.Local.SIM_CONFIG_KEY);
            //判断SIM卡鉴权操作是否需要yes,no
            if (auth.equals(Config.Local.SIM_CONFIG_KEY_YES)) {
                //判断ICCID是否为空
                if (curIccid != null && curIccid != "") {
                    return true;
                } else {
                    //车机不可以正常使用广播
                    //Intent sim_intent = new Intent(Config.Local.SIM_ACTION);
                    //CarServiceClient.getContext().sendBroadcast(sim_intent);
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void localCheckSims() {
        boolean isSim = initSims();
        if (isSim) {
            if (curIccid != null && curIccid != "") {
                Map<String, String> iccIds = FileUtils.readFile(Config.Local.SIM_JSSIM_PATH);
                if (iccIds != null && iccIds.size() > 0) {
                    String iccId = iccIds.get(Config.Local.SIM_ICCID_KEY);
                    if (!iccId.equals(curIccid)) {
                        //Intent sim_intent = new Intent(Config.Local.SIM_ACTION);
                        //CarServiceClient.getContext().sendBroadcast(sim_intent);
                    }
                } else {
                    //Intent sim_intent = new Intent(Config.Local.SIM_ACTION);
                    //CarServiceClient.getContext().sendBroadcast(sim_intent);
                }
            }
        }
    }

    private void netCheckSims() {
        //读取ICCID文件
        Map<String, String> iccIds = FileUtils.readFile(Config.Local.SIM_JSSIM_PATH);
        LogUtils.d("netCheckSims iccIds:" + iccIds);
        if (iccIds != null && iccIds.size() > 0) {
            String iccId = iccIds.get(Config.Local.SIM_ICCID_KEY);
            //判断ICCID是否相同
            if (!iccId.equals(curIccid)) {
                checkJsSim();
            } else {
                //handler.post(cardRunnable);
            }
        } else {
            checkJsSim();
        }
    }

    private void checkJsSim() {
        //检测当前SIM卡是否是佳圣的卡
        NetWorkManagement.newInstance().getCheckSim()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<OtherBean>() {
                    @Override
                    public void accept(OtherBean otherBean) throws Exception {
                        if(otherBean == null){
                            return;
                        }
                        try {
                            OtherBean bean = otherBean;
                            LogUtils.d("checkJsSim " + new Gson().toJson(bean));
                            String code = bean.getError_code();
                            //是否是佳圣的卡0:是
                            if (code.equals("0")) {
                                LogUtils.d("save js card iccid[" + curIccid + "] to file...");
                                Map<String, String> keys = new HashMap<>(16);
                                keys.put(Config.Local.SIM_ICCID_KEY, curIccid);
                                keys.put(Config.Local.SIM_ICCID_CODE, "1");
                                FileUtils.writeFile(keys, new File(Config.Local.SIM_JSSIM_PATH));
                            } else {
                                //handler.post(cardRunnable);
                                //Intent sim_intent = new Intent(Config.Local.SIM_ACTION);
                                //CarServiceClient.getContext().sendBroadcast(sim_intent);
                            }
                        }catch (Exception e){
                            LogUtils.d("checkJsSim  e:" + e.toString());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //handler.post(cardRunnable);
                        LogUtils.e("checkJsSim  t:"+ throwable);
                        //Intent sim_intent = new Intent(Config.Local.SIM_ACTION);
                        //CarServiceClient.getContext().sendBroadcast(sim_intent);
                    }
                });
    }

    private void jsimAlert() {
        //获取SIM车机端通知设置状态1：开启，0：关闭
        Map<String, String> showMaps = FileUtils.readFile(Config.Local.SIM_SET_PATH);
        LogUtils.d("jsimAlert showMaps:" + showMaps);
        if (showMaps != null && showMaps.size() > 0) {
            int show = AppUtils.parseInt(showMaps.get(Config.Local.SIM_SET_KEY));
            LogUtils.d("read file show is " + show);
            simAlert = show == 1;
        }
        LogUtils.d("show alert is " + simAlert);

        //获取弹框总次数
        Map<String, String> simMaps = FileUtils.readFile(Config.Local.SIM_ALERT_PATH);
        LogUtils.d("jsimAlert simMaps:" + simMaps);
        if (simMaps != null && simMaps.size() > 0) {
            simCount = AppUtils.parseInt(simMaps.get(Config.Local.SIM_ALERT_COUNT));
            LogUtils.d("jsimAlert simCount:" + simCount);

            //20181019 > 不限制次数.  add by zhua.
            //>=10次则不在弹框
//            if (simCount >= 10) {
//                countAlert = false;
//            } else {
                //获取上次弹框的时间，如果时间间隔不大于6天则不弹窗
                String time = simMaps.get(Config.Local.SIM_ALERT_TIME);
                LogUtils.d("jsimAlert time:" + time);
                if (time != null && !AppUtils.checkAlertTime(time)) {
                    countAlert = false;
                } else {
                    countAlert = true;
                }
//            }
        }

        NetWorkManagement.newInstance().getQueryCard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<JsimBean>() {
                    @Override
                    public void accept(JsimBean bean) throws Exception {
                        if(bean == null){
                            return;
                        }
                        LogUtils.d("response : " + bean.toString());
                        String code = bean.getError_code();
                        try {
                            Intent intent = new Intent();
                            //表示此卡需要续费，要弹出提醒
                            if ("0".equals(code) || "3".equals(code)) {
                                //发送SIM卡通知
                                if (simAlert) {
                                    JsimBean.Card card = null;
                                    if (bean.getData() != null) {
                                        card = bean.getData();
                                    }

                                    String sim = "0";
                                    String time = "0";
                                    if (card != null) {
                                        if (card.getRemaining() != null) {
                                            sim = card.getRemaining();
                                        }
                                        if (card.getEndtime() != null) {
                                            time = card.getEndtime();
                                        }
                                    }
//                                    LogUtils.d("StatisticsData","check js _ sim=" +sim + ",,time=" + time);
//                                    StatisticsData.newInstance().updateJsValue(sim, time);

                                    String param = "剩余体验流量为" + sim + "M，请及时充值";
                                    Intent broad = new Intent();
                                    broad.setAction("launcher_right_pos_tips");
                                    broad.putExtra("tips", param);
                                    broad.putExtra("pkg", "com.jiashen");
                                    sendBroadcast(broad);
                                }
                                //累计弹出SIM充值窗口
                                if (countAlert) {
                                    int count = simCount + 1;
                                    Map<String, String> keys = new HashMap<>(16);
                                    keys.put(Config.Local.SIM_ALERT_COUNT, String.valueOf(count));
                                    keys.put(Config.Local.SIM_ALERT_TIME, String.valueOf(System.currentTimeMillis()));
                                    FileUtils.writeFile(keys, new File(Config.Local.SIM_ALERT_PATH));
                                    intent.setAction("com.wwc2.jiashen.RECHARGET_ACTION");
                                    intent.putExtra("type", Integer.parseInt(code));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    CarServiceClient.getContext().startActivity(intent);
                                }
                            }
                        } catch (Exception e) {
                            LogUtils.d("jsimAlert---e=" + e);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e(throwable);
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.d("--onDestroy");
        AccPowerManager.getInstance().unregisterReceiver(this);
        ClientConnect.getInstance().closeClient();
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        CarServiceClient.removeAccListener();
        if (canbusDriver != null) {
            canbusDriver.unbindService(CarServiceClient.getContext());
            if (carInfoListener != null) {
                canbusDriver.unRegisterCarInfoUIListener(carInfoListener);
            }
            canbusDriver = null;
        }
        LocationManagement.newInstance().stopLocations();
        LocationManagement.newInstance().getAndroidLocations().removeAllListener();


        if(mDialog != null){
            mDialog.dismiss();
        }


        if (mConnectBroadcastReceiver != null) {
            unregisterReceiver(mConnectBroadcastReceiver);
            mConnectBroadcastReceiver = null;
        }

        //支持唤醒的,无需断开
//        RongIMClient.getInstancqe().disconnect();
        mDvrSettings.unregisterDVRBroadcast();
        mDebugSignal.destory();
        mSensorManager.unregisterListener(this);
        closeSensor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
