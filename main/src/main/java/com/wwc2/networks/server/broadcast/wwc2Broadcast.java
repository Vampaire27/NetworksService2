package com.wwc2.networks.server.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.cmd.CmdManager;
import com.wwc2.networks.cmd.GsensorCmd;
import com.wwc2.networks.server.bean.AMapBean;
import com.wwc2.networks.server.bean.DeviceBean;
import com.wwc2.networks.server.bean.JsimBean;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.canbus.CanBusNettyManager;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.location.TraceBeanManager;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.NetworkUtil;
import com.wwc2.networks.server.utils.TelephonyUtil;
import com.wwc2.networks.server.utils.Utils;
import com.wwc2.networks.server.view.UrlDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class wwc2Broadcast extends BroadcastReceiver {
    private static final String TAG = "wwc2Broadcast";
    public static final String ACC_OFF = "com.android.wwc2.sleep";
    public static final String ACC_ON = "com.android.wwc2.wakeup";
    public static final String AUTHORITY = "com.wwc2.main.provider.logic";
    public static final String DVR_ENABLE = "dvr_support";
    public static final String CAMERA_STATUS = "camera_status";
    public static final String ACC_STATUS = "acc_status";

    //add for 24V Car, acc off only send Real Acc ;
    public static final String REAL_ACC_STATUS = "real_acc_status";

    public static final String SYSTEM_ERROR = "com.wwc2.system.error";
    public final String TYPE = "TYPE";
    public final String PKG = "PKG";
    public static final String SYSTEM_URL = "com.wwc2.system.url";
    public final String STYLE = "STYLE";
    public static final String SYSTEM_MCU_DOWN = "com.wwc2.system.mcu.download";
    public static final String SYSTEM_MCU_STATUS = "com.wwc2.system.mcu.status";
    public static final String MCU_STATUS = "mcu_status";
    public static final String MCU_PATH = "mcu_path";
    public static final int MCU_STATUS_OK = 1;
    public static final int MCU_STATUS_ON = 0;
    public static final int MCU_STATUS_IGNORE = 2;
    public static final String SIM_SWITCH = "com.wwc2.simswitch";
    //(客户+序号+日期)长度12位
    private final String APK_ID = "APK_ID";
    //1=内置卡；2=外置卡
    private final String SIM_CARD = "SIM_CARD";
    public static final String SIM_SWITCH_ACK = "com.wwc2.simswitchack";
    private final String SIM_FILE = "custom/verify";
    public static final String SYSTEM_APN_DOWN = "com.wwc2.apn_update";
    public static final String SYSTEM_APN_STATUS = "com.wwc2.apn_ack";
    public static final String APN_STATUS = "apn_status";
    //没有网络
    public static final int APN_STATUS_ON_NETWORK = 0;
    //下载成功
    public static final int APN_STATUS_OK = 1;
    //下载失败
    public static final int APN_STATUS_ON = 2;
    //设置广告信息
    public static final String AD_ACTION = "ACTION_SET_ADVERTISING";
    public static final String AD_TITLE = "title";
    //状态，1开2关
    public static final String AD_STATUS = "status";
    public static final String AD_PKG = "pkg";
    //流量管理
    public static final String FLOW_ACTION = "com.wwc2.action.flow";
    //流量类型：1=佳圣，2=智网
    public static final String FLOW_TYPE = "flow_type";
    public static final int FLOW_TYPE_JS = 1;
    public static final int FLOW_TYPE_ZW = 2;
    public static final String FLOW_PKG = "flow_pkg";
    public static final String FLOW_PKG_JS = "com.jiashen";
    public static final String FLOW_PKG_ZW = "com.wwc2.rechargecard";
    //检测佳圣是否实名
    public static final String JS_ACTION_CHECK_SIM = "com.wwc2.sim.check";
    public static final String JS_ACTION_SIM_STATUS = "com.wwc2.sim.status";
    public static final String JS_SIM_STATUS = "sim_status";
    //20秒间隔
    private long times = 0;
    private final long def_time = 20;
    private int old_para = 0;
    private String old_pkg="";
    //唤醒完成
    public static final String ACTION_MAIN_WAKEUP
            = "com.android.wwc2.main.wakeup";
    //通知唤醒
    public static final String ACTION_CARNET_WAKEUP
            = "com.android.wwc2.carnet.wakeup";
    //1开始 2结束
    public static final String KEY_CARNET_WAKEUP
            = "photo.state";

    public static final String KEY_WAKEUP_TIME
            = "time";

    //低电压的广播
    public static final String ACTION_MAIN_SLEEP_VOLTAGE   = "com.wwc2.main.low.voltage";

    /*can_mcu*/
    public static final String SYSTEM_CANMCU_DOWN = "com.wwc2.system.canmcu.download";
    public static final String SYSTEM_CANMCU_STATUS = "com.wwc2.system.canmcu.status";
    public static final String CANMCU_STATUS = "canmcu_status";
    public static final String CANMCU_PATH = "canmcu_path";
    public static final int CANMCU_STATUS_OK = 1;
    public static final int CANMCU_STATUS_ON = 0;
    public static final int CANMCU_STATUS_IGNORE = 2;
    //sensor
    public static final String SYSTEM_SENSOR_KEY = "com.wwc2.otherKeyCode";
    public static final String SENSOR_TYPE = "com.www2.dvr.SENSOR_TYPE";
    public static final String SENSOR_TYPE_VALUE = "com.www2.dvr.SENSOR_VALUE";

    boolean writeFile(String values, File file) {
        if (null != file) {
            try {
                BufferedWriter bufferedWriter =
                        new BufferedWriter(new FileWriter(file, true));
                bufferedWriter.write(values);
                bufferedWriter.newLine();
                bufferedWriter.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private boolean oldWa = true;
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals(ACC_ON)) {
            boolean isRunning = CarServiceClient.isRunning();
            LogUtils.d("--action is " + action + ",isRunning is " + isRunning);
            if (!isRunning) {
                LogUtils.d("receiver boot complate not running.");
                CarServiceClient.startService();
            }
        }

        if(action.equals("com.wwc2.dvr.reboot")){
            Uri uri_dvr = Uri.parse("content://" + wwc2Broadcast.AUTHORITY
                    + "/" + wwc2Broadcast.DVR_ENABLE);
            String dvrEnable = context.getContentResolver().getType(uri_dvr);
            if(dvrEnable.equals("true")){
                LogUtils.d("---收到检测DVR消息---");
                DvrManagement.newInstance().checkAIDL();
            }else{
                LogUtils.d("---dvrEnable=false...return!!---");
            }
        }

        if (action.equals(SYSTEM_URL)) {
            String style = intent.getStringExtra(STYLE);
            if (style != null && !style.equals("")) {
                UrlDialog.Builder builder = new UrlDialog.Builder(context, style);
                UrlDialog mDialog = builder.create();
                mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mDialog.show();
            }
        }
        if (action.equals(SYSTEM_MCU_DOWN)) {
            LogUtils.d("---SYSTEM_MCU_DOWN---");
            final String path = "/storage/emulated/0/DMCU/";
            NetWorkManagement.newInstance().postGetMcuUrl(new Callback<OtherBean>() {
                @Override
                public void onResponse(Call<OtherBean> call, Response<OtherBean> response) {
                    if (response.code() == Config.App.RESULT_CODE) {
                        String code = response.body().getCode();
                        String msg = response.body().getMsg();
                        if (code.equals(Config.App.RESULT_OK)) {
                            String url = response.body().getUrl();
                            if (url != null && !url.equals("")) {
                                LogUtils.d("---MCU_DOWN---url=" + url);
                                int temp = url.lastIndexOf("/");
                                final String fileName = url.substring(++temp, url.length())
                                        .replace("_urgent", "");
                                boolean isExists = FileUtils.fileIsExists(
                                        path + fileName);
                                if (isExists) {
                                    LogUtils.d("---mcu file exists! del...");
                                    FileUtils.deleteFile(path + fileName);
                                }
                                LogUtils.d("---mcu file start download!");
                                NetWorkManagement.newInstance().postDownloadFile(
                                        ProgressManager.getInstance()
                                                .with(new OkHttpClient.Builder())
                                                .build(),
                                        url,
                                        path,
                                        new ProgressListener() {
                                            @Override
                                            public void onProgress(ProgressInfo progressInfo) {
                                                boolean isFinish = progressInfo.isFinish();
                                                LogUtils.d("----isFinish=" + isFinish
                                                        + ",,,---Current=" + progressInfo.getCurrentbytes()
                                                        + ",,,---Content=" + progressInfo.getContentLength());
                                                if (isFinish) {
                                                    Intent mcuIntent = new Intent(SYSTEM_MCU_STATUS);
                                                    mcuIntent.putExtra(MCU_STATUS, MCU_STATUS_OK);
                                                    mcuIntent.putExtra(MCU_PATH, path + fileName);
                                                    context.sendBroadcast(mcuIntent);
                                                }
                                            }

                                            @Override
                                            public void onError(long id, Exception e) {
                                                LogUtils.d("---mcu file download error! Exception="
                                                        + e.toString());
                                                Intent mcuIntent = new Intent(SYSTEM_MCU_STATUS);
                                                mcuIntent.putExtra(MCU_STATUS, MCU_STATUS_ON);
                                                mcuIntent.putExtra(MCU_PATH, "");
                                                context.sendBroadcast(mcuIntent);
                                            }
                                        });
                            }
                        } else {
                            LogUtils.d("---code=" + code + ",,,msg=" + msg);
                            Intent mcuIntent = new Intent(SYSTEM_MCU_STATUS);
                            mcuIntent.putExtra(MCU_STATUS, MCU_STATUS_ON);
                            mcuIntent.putExtra(MCU_PATH, "");
                            context.sendBroadcast(mcuIntent);
                        }
                    }else{
                        LogUtils.d("---response.code()=" + response.code());
                        Intent mcuIntent = new Intent(SYSTEM_MCU_STATUS);
                        mcuIntent.putExtra(MCU_STATUS, MCU_STATUS_ON);
                        mcuIntent.putExtra(MCU_PATH, "");
                        context.sendBroadcast(mcuIntent);
                    }
                }

                @Override
                public void onFailure(Call<OtherBean> call, Throwable t) {
                    LogUtils.d("---onFailure---t=" + t.toString());
                    Intent mcuIntent = new Intent(SYSTEM_MCU_STATUS);
                    mcuIntent.putExtra(MCU_STATUS, MCU_STATUS_ON);
                    mcuIntent.putExtra(MCU_PATH, "");
                    context.sendBroadcast(mcuIntent);
                }
            });
        }
        if (action.equals(SYSTEM_APN_DOWN)) {
            LogUtils.d("---APN_DOWN---");
            if(NetworkUtil.isNetworkAvailable(context)){
                NetWorkManagement.newInstance().postGetAPNUrl(new Callback<OtherBean>() {
                    @Override
                    public void onResponse(Call<OtherBean> call, Response<OtherBean> response) {
                        if (response.code() == Config.App.RESULT_CODE) {
                            String code = response.body().getCode();
                            String msg = response.body().getMsg();
                            if (code.equals(Config.App.RESULT_OK)) {
                                String url = response.body().getUrl();
                                LogUtils.d("---APN_DOWN---url=" + url);
                                if (url != null && !url.equals("")) {
                                    final String fileName = Config.App.APN_PATH +
                                            Config.App.APN_FILE_NAME;
                                    boolean isExists = FileUtils.fileIsExists(fileName);
                                    if (isExists) {
                                        LogUtils.d("---APN file exists! del!");
                                        FileUtils.deleteFile(fileName);
                                    }
                                    LogUtils.d("---APN file start download!");
                                    NetWorkManagement.newInstance().postDownloadFile(
                                            ProgressManager.getInstance()
                                                    .with(new OkHttpClient.Builder())
                                                    .build(),
                                            url,
                                            Config.App.APN_PATH,
                                            new ProgressListener() {
                                                @Override
                                                public void onProgress(ProgressInfo progressInfo) {
                                                    boolean isFinish = progressInfo.isFinish();
                                                    LogUtils.d("----APN isFinish=" + isFinish
                                                            + ",,,---Current=" + progressInfo.getCurrentbytes()
                                                            + ",,,---Content=" + progressInfo.getContentLength());
                                                    if (isFinish) {
                                                        Intent apnIntent = new Intent(SYSTEM_APN_STATUS);
                                                        apnIntent.putExtra(APN_STATUS, APN_STATUS_OK);
                                                        context.sendBroadcast(apnIntent);
                                                    }
                                                }

                                                @Override
                                                public void onError(long id, Exception e) {
                                                    LogUtils.d("---APN_file download error! Exception="
                                                            + e.toString());
                                                    Intent apnIntent = new Intent(SYSTEM_APN_STATUS);
                                                    apnIntent.putExtra(APN_STATUS, APN_STATUS_ON);
                                                    context.sendBroadcast(apnIntent);
                                                }
                                            });
                                }
                            } else {
                                LogUtils.d("---APN_code=" + code + ",,,msg=" + msg);
                                Intent apnIntent = new Intent(SYSTEM_APN_STATUS);
                                apnIntent.putExtra(APN_STATUS, APN_STATUS_ON);
                                context.sendBroadcast(apnIntent);
                            }
                        }else{
                            LogUtils.d("---APN_response.code()=" + response.code());
                            Intent apnIntent = new Intent(SYSTEM_APN_STATUS);
                            apnIntent.putExtra(APN_STATUS, APN_STATUS_ON);
                            context.sendBroadcast(apnIntent);
                        }
                    }

                    @Override
                    public void onFailure(Call<OtherBean> call, Throwable t) {
                        LogUtils.d("---APN_onFailure---t=" + t.toString());
                        Intent apnIntent = new Intent(SYSTEM_APN_STATUS);
                        apnIntent.putExtra(APN_STATUS, APN_STATUS_ON);
                        context.sendBroadcast(apnIntent);
                    }
                });
            }else{
                LogUtils.d("---APN_not network!---");
                Intent apnIntent = new Intent(SYSTEM_APN_STATUS);
                apnIntent.putExtra(APN_STATUS, APN_STATUS_ON_NETWORK);
                context.sendBroadcast(apnIntent);
            }
        }
        if (action.equals(SIM_SWITCH)) {
            int card = intent.getIntExtra(SIM_CARD, 1);
            final String apk_id = intent.getStringExtra(APK_ID);
            LogUtils.d("SIM_SWITCH   card=" + card + ",,--apk_id=" + apk_id);
            //内置
            if (card == 1) {
                //切回内置，不做限制
                SystemProperties.set("persist.sys.simswich", "1");
                int[] subId = getSubId(context, 0);
                setDefaultDataSubId(context, subId[0]);
                Intent simIntent = new Intent(SIM_SWITCH_ACK);
                simIntent.putExtra("RET", 1);
                context.sendBroadcast(simIntent);
            } else if (card == 2) {//外置
                int[] subId = getSubId(context, 1);
                if (subId == null || subId.length == 0 || subId[0] < 0) {
                    Intent simIntent = new Intent(SIM_SWITCH_ACK);
                    simIntent.putExtra("RET", -1);
                    context.sendBroadcast(simIntent);
                } else {
                    boolean isFile = FileUtils.fileIsExists(SIM_FILE);
                    LogUtils.d("isFile=" + isFile);
                    if (isFile) {
                        String value = FileUtils.readFiles(SIM_FILE);
                        String pass = DeviceManagement.newInstance(CarServiceClient.getContext())
                                .getSimVerifyKey(apk_id);
                        LogUtils.d("value=" + value + ",,,---pass=" + pass);
                        if (value.equals(pass)) {
                            SystemProperties.set("persist.sys.simswich", "2");
                            setDefaultDataSubId(context, subId[0]);
                            Intent simIntent = new Intent(SIM_SWITCH_ACK);
                            simIntent.putExtra("RET", 2);
                            context.sendBroadcast(simIntent);
                        } else {
                            switchSim(context, apk_id);
                        }
                    } else {
                        switchSim(context, apk_id);
                    }
                }
            }
        }
        if (action.equals(JS_ACTION_CHECK_SIM)) {
            String iccid = TelephonyUtil.getMime(CarServiceClient.getContext());
            if(!TextUtils.isEmpty(iccid)){
                String old_iccid = (String)SPUtils.get(CarServiceClient.getContext(),
                        Config.SIM_REALNAMEQUERY,"");
                LogUtils.d(TAG,"---JS_ACTION_CHECK_SIM---old_iccid=" + old_iccid + ",,iccid=" + iccid);
                if(! old_iccid.equals(iccid)){
                   // AMapBean bean = CarServiceClient.getAMapBean();  //todo:
                    AMapBean bean=null;
                    String laddress =TraceBeanManager.getInstance().getmLastAdderss();
                    //LogUtils.d(TAG,"---JS_ACTION_CHECK_SIM---bean=" + bean.toString());
                    if (laddress != null) {
                        if(TextUtils.isEmpty(laddress)){
                            getCity();
                        }else{
                            checkRealNameQueryIot(laddress);
                        }
                    }else{
                        getCity();
                    }
                }else{
                    LogUtils.d(TAG,"---JS_ACTION_CHECK_SIM---直接使用！");
                    sendCheckSimBroad(true);
                }
            }else{
                LogUtils.d(TAG,"---JS_ACTION_CHECK_SIM---iccid=null!");
                sendCheckSimBroad(true);
            }
        }
        /**
         * 凌飞项目can_mcu下载功能
         */
        if (action.equals(SYSTEM_CANMCU_DOWN)) {
            LogUtils.d("---SYSTEM_CANMCU_DOWN---");
            final String path = "/storage/emulated/0/CANMCU/";
            NetWorkManagement.newInstance().postGetCanMcuUrl(new Callback<OtherBean>() {
                @Override
                public void onResponse(Call<OtherBean> call, Response<OtherBean> response) {
                    if (response.code() == Config.App.RESULT_CODE) {
                        String code = response.body().getCode();
                        String msg = response.body().getMsg();
                        if (code.equals(Config.App.RESULT_OK)) {
                            String url = response.body().getUrl();
                            if (url != null && !url.equals("")) {
                                LogUtils.d("---CANMCU_DOWN---url=" + url);
                                int temp = url.lastIndexOf("/");
                                final String fileName = url.substring(++temp, url.length());
                                boolean isExists = FileUtils.fileIsExists(
                                        path + fileName);
                                if (isExists) {
                                    LogUtils.d("---CANMCU file exists! del...");
                                    FileUtils.deleteFile(path + fileName);
                                }
                                LogUtils.d("---CANMCU file start download!");
                                NetWorkManagement.newInstance().postDownloadFile(
                                        ProgressManager.getInstance()
                                                .with(new OkHttpClient.Builder())
                                                .build(),
                                        url,
                                        path,
                                        new ProgressListener() {
                                            @Override
                                            public void onProgress(ProgressInfo progressInfo) {
                                                boolean isFinish = progressInfo.isFinish();
                                                LogUtils.d("CANMCU----isFinish=" + isFinish
                                                        + ",,,---Current=" + progressInfo.getCurrentbytes()
                                                        + ",,,---Content=" + progressInfo.getContentLength());
                                                if (isFinish) {
                                                    Intent canMcuIntent = new Intent(SYSTEM_CANMCU_STATUS);
                                                    canMcuIntent.putExtra(CANMCU_STATUS, CANMCU_STATUS_OK);
                                                    canMcuIntent.putExtra(CANMCU_PATH, path + fileName);
                                                    context.sendBroadcast(canMcuIntent);
                                                }
                                            }

                                            @Override
                                            public void onError(long id, Exception e) {
                                                LogUtils.d("---CANMCU file download error! Exception="
                                                        + e.toString());
                                                Intent canMcuIntent = new Intent(SYSTEM_CANMCU_STATUS);
                                                canMcuIntent.putExtra(CANMCU_STATUS, CANMCU_STATUS_ON);
                                                canMcuIntent.putExtra(CANMCU_PATH, "");
                                                context.sendBroadcast(canMcuIntent);
                                            }
                                        });
                            }
                        } else {
                            LogUtils.d("CANMCU---code=" + code + ",,,msg=" + msg);
                            Intent canMcuIntent = new Intent(SYSTEM_CANMCU_STATUS);
                            canMcuIntent.putExtra(CANMCU_STATUS, CANMCU_STATUS_ON);
                            canMcuIntent.putExtra(CANMCU_PATH, "");
                            context.sendBroadcast(canMcuIntent);
                        }
                    }else{
                        LogUtils.d("CANMCU---response.code()=" + response.code());
                        Intent canMcuIntent = new Intent(SYSTEM_CANMCU_STATUS);
                        canMcuIntent.putExtra(CANMCU_STATUS, CANMCU_STATUS_ON);
                        canMcuIntent.putExtra(CANMCU_PATH, "");
                        context.sendBroadcast(canMcuIntent);
                    }
                }

                @Override
                public void onFailure(Call<OtherBean> call, Throwable t) {
                    LogUtils.d("CANMCU---onFailure---t=" + t.toString());
                    Intent mcuIntent = new Intent(SYSTEM_CANMCU_STATUS);
                    mcuIntent.putExtra(CANMCU_STATUS, CANMCU_STATUS_ON);
                    mcuIntent.putExtra(CANMCU_PATH, "");
                    context.sendBroadcast(mcuIntent);
                }
            });
        }
        if (action.equals(SYSTEM_SENSOR_KEY)) {
            int keyCode = intent.getIntExtra("keyValue", 0);
            LogUtils.d("SENSOR---keyCode=" + keyCode);
            if(keyCode == KeyEvent.KEYCODE_MEDIA_STOP){
                CmdManager.getInstans().postCmd(new GsensorCmd(new RemotelyBean()));
            }
        }
        if (action.equals(SENSOR_TYPE)) {
            int sensor = intent.getIntExtra(SENSOR_TYPE_VALUE, 0);
            LogUtils.d("SENSOR---update=" + sensor);

            //zhongyang_gsensor add sync for remote server to dvr 2020.0506
             SPUtils.put(CarServiceClient.getContext(), Config.SENSOR_LEVEL, Integer.toString(sensor));

            NetWorkManagement.newInstance().postSensorApi(sensor +"").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<OtherBean>() {
                        @Override
                        public void accept(OtherBean otherBean) throws Exception {
//                            LogUtils.d("post SensorApi code=" + otherBean.getCode());
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            LogUtils.d("post SensorApi err...throwable=" + throwable);
                        }
                    });
        }
    }

    private void getCity(){
        String tower = TelephonyUtil.newInstance(CarServiceClient.getContext()).getTowerInfo();
        LogUtils.d(TAG,"getCity---tower=" + tower);
        if (tower != null) {
            NetWorkManagement.newInstance().postGetLbsInfo(tower)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<OtherBean>() {
                        @Override
                        public void accept(OtherBean otherBean) throws Exception {
                            LogUtils.d(TAG,"CHECK_SIM:" + otherBean.toString());
                            if (otherBean.getCode().equals(Config.App.RESULT_OK)) {
                                String city = otherBean.getCity();
                                if(!TextUtils.isEmpty(city)) {
                                    checkRealNameQueryIot(city);
                                }else{
                                    sendCheckSimBroad(true);
                                }
                            }else{
                                sendCheckSimBroad(true);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            sendCheckSimBroad(true);
                        }
                    });
        }else{
            sendCheckSimBroad(true);
        }
    }

    private int error = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if(error < 3){
                    error++;
                    String city = (String)msg.obj;
                    LogUtils.d(TAG,"handler: 重试中...");
                    checkRealNameQueryIot(city);
                }else{
                    error = 0;
                    LogUtils.d(TAG,"handler: 重试失败..");
                    sendCheckSimBroad(true);
                }
            }catch (Exception e){
                LogUtils.d(TAG,"handler: e=" + e.toString());
            }
        }
    };

    private void checkRealNameQueryIot(final String city){
        LogUtils.d(TAG,"checkRealNameQueryIot:" + city);
        if(city.contains("广西") || city.contains("福建")){
            Call<JsimBean> js_Card = NetWorkManagement.newInstance().isRealNameQueryIot();
            js_Card.enqueue(new Callback<JsimBean>() {
                @Override
                public void onResponse(Call<JsimBean> call, Response<JsimBean> response) {
                    JsimBean bean = response.body();
                    LogUtils.d(TAG,"checkRealNameQueryIot _ response : " + new Gson().toJson(bean));
                    String code = bean.getError_code();
                    try {
                        //表示此卡未在电信实名验证
                        if ("3".equals(code)) {
                            //限制使用
                            sendCheckSimBroad(false);
                        }else{
                            sendCheckSimBroad(true);
                            String iccid = TelephonyUtil.getMime(CarServiceClient.getContext());
                            LogUtils.d(TAG,"checkRealNameQueryIot---code="+ code +",---iccid=" + iccid);
                            if(!TextUtils.isEmpty(iccid)){
                                SPUtils.put(CarServiceClient.getContext(),
                                        Config.SIM_REALNAMEQUERY,
                                        iccid);
                            }
                        }
                    } catch (Exception e) {
                        LogUtils.d(TAG,"checkRealNameQueryIot---e=" + e.toString());
                        sendCheckSimBroad(true);
                    }
                }

                @Override
                public void onFailure(Call<JsimBean> call, Throwable t) {
                    LogUtils.d(TAG,"checkRealNameQueryIot---e=" + t.toString());
                    Message msg = new Message();
                    msg.obj = city;
                    handler.sendMessageDelayed(msg, 3000);
                }
            });
        }else{
            sendCheckSimBroad(true);
        }
    }

    private void sendCheckSimBroad(boolean status){
        LogUtils.d(TAG,"sendCheckSimBroad _ status=" + status);
        Intent broad = new Intent();
        broad.setAction(wwc2Broadcast.JS_ACTION_SIM_STATUS);
        broad.putExtra(wwc2Broadcast.JS_SIM_STATUS, status);
        CarServiceClient.getContext().sendBroadcast(broad);
    }

    /**
     * 获取当前SUBid标识
     *
     * @param context
     * @param slotId
     * @return
     */
    public int[] getSubId(Context context, int slotId) {
        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
        Method declaredMethod;
        int[] subArr = null;
        try {
            declaredMethod = Class.forName("android.telephony.SubscriptionManager")
                    .getDeclaredMethod("getSubId", new Class[]{Integer.TYPE});
            declaredMethod.setAccessible(true);
            subArr = (int[]) declaredMethod.invoke(mSubscriptionManager, slotId);
        } catch (Exception e) {
            e.printStackTrace();
            declaredMethod = null;
        }
        if (declaredMethod == null) {
            subArr = null;
        }
        return subArr;
    }

    /**
     * 设置默认Sub
     *
     * @param context
     * @param subId
     */
    public void setDefaultDataSubId(Context context, int subId) {
        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
        Method declaredMethod;
        try {
            declaredMethod = Class.forName("android.telephony.SubscriptionManager")
                    .getDeclaredMethod("setDefaultDataSubId", new Class[]{Integer.TYPE});
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(mSubscriptionManager, subId);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return;
    }

    /**
     * 切换Sim卡
     *
     * @param context
     * @param apk_id
     */
    private void switchSim(final Context context, final String apk_id) {
        boolean isAvailable = NetworkUtil.isNetworkAvailable(context);
        if (isAvailable) {
            NetWorkManagement.newInstance().checkSimVerify(apk_id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<DeviceBean>() {
                        @Override
                        public void accept(DeviceBean deviceBean) throws Exception {
                            String code = deviceBean.getCode();
                            LogUtils.d("switchSim-code=" + code);
                            if (code.equals("0")) {
                                //0 激活成功 1已经激活过 2次数超额
                                String status = deviceBean.getStatus();
                                LogUtils.d("switchSim-status=" + status);
                                if (status.equals("0") || status.equals("1")) {
                                    String pass = DeviceManagement.newInstance(CarServiceClient.getContext())
                                            .getSimVerifyKey(apk_id);
                                    FileUtils.writeFile(pass, new File(SIM_FILE));
                                    SystemProperties.set("persist.sys.simswich", "2");
                                    int[] subId = getSubId(context, 1);
                                    setDefaultDataSubId(context, subId[0]);
                                    Intent simIntent = new Intent(SIM_SWITCH_ACK);
                                    simIntent.putExtra("RET", 2);
                                    context.sendBroadcast(simIntent);
                                } else if (status.equals("2")) {
                                    Intent simIntent = new Intent(SIM_SWITCH_ACK);
                                    simIntent.putExtra("RET", 3);
                                    context.sendBroadcast(simIntent);
                                } else {
                                    Intent simIntent = new Intent(SIM_SWITCH_ACK);
                                    simIntent.putExtra("RET", -1);
                                    context.sendBroadcast(simIntent);
                                }
                            } else {
                                Intent simIntent = new Intent(SIM_SWITCH_ACK);
                                simIntent.putExtra("RET", -1);
                                context.sendBroadcast(simIntent);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            LogUtils.d("switchSim-throwable=" + throwable);
                            Intent simIntent = new Intent(SIM_SWITCH_ACK);
                            simIntent.putExtra("RET", -1);
                            context.sendBroadcast(simIntent);
                        }
                    });
        } else {
            Intent simIntent = new Intent(SIM_SWITCH_ACK);
            simIntent.putExtra("RET", 0);
            context.sendBroadcast(simIntent);
        }
    }

    public final static class realAccStatusOberver extends ContentObserver{
        private Context mCtx;
        public realAccStatusOberver(Handler handler,Context context) {
            super(handler);
            mCtx = context;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            LogUtils.d("observer real acc change to  selfChange =" + selfChange);
            Uri uri_acc = Uri.parse("content://" + AUTHORITY + "/" + REAL_ACC_STATUS);
            String strAcc = mCtx.getContentResolver().getType(uri_acc);
            if("false".equals(strAcc)){
                CanBusNettyManager.getInstance().postAccInfoByCanBusCMD(Config.System.ACC_OFF);
            }else if("true".equals(strAcc)){
                CanBusNettyManager.getInstance().postAccInfoByCanBusCMD(Config.System.ACC_ON);
            }

        }
    }

    public final static class accStatusObserver extends ContentObserver {
        private Context context;
        private String oldAcc = "";

        public accStatusObserver(Handler handler, Context con) {
            super(handler);
            context = con;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

           //observer acc change to  uri=content://com.wwc2.main.provider.logic/acc_status

            LogUtils.d("observer acc change to  uri=" + uri);
            Uri uri_acc = Uri.parse("content://" + AUTHORITY + "/" + ACC_STATUS);
            String strAcc = context.getContentResolver().getType(uri_acc);
            if(oldAcc.equals(strAcc)){
                LogUtils.d("observer acc change 重复,return!!");
                return;
            }
            LogUtils.d("observer acc change to oldAcc=" + oldAcc+ "---strAcc=" + strAcc);
            oldAcc = strAcc;
            switch (oldAcc){
                case "true":
                    CarServiceClient.setAccStatus(Config.System.ACC_ON);
                    //重启服务
//                    CarServiceClient.stopService();
//                    try {
//                        //延迟2秒
//                        Thread.sleep(2000);
//                    }catch (Exception e){}
//
//                    CarServiceClient.startService();

                    break;
                case "false":
                    LogUtils.d("wpeng  acc change to oldAcc=" + strAcc + "------------closeVideo!!!" );
                    CarServiceClient.setAccStatus(Config.System.ACC_OFF);

                    break;
            }
        }
    }
}
