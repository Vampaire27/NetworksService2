package com.wwc2.networks.server.broadcast;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.R;
import com.wwc2.networks.cmd.CmdManager;
import com.wwc2.networks.cmd.GetVideoListCmd;
import com.wwc2.networks.cmd.TakeLocalFileCmd;
import com.wwc2.networks.cmd.TakePictureCmd;
import com.wwc2.networks.cmd.TakeVideoCmd;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.location.LocationManagement;
import com.wwc2.networks.server.provider.CrashError;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.MesAdpter;
import com.wwc2.networks.server.utils.ThreadUtil;
import com.wwc2.networks.server.utils.ToastUtils;
import com.wwc2.networks.server.utils.Utils;
import com.wwc2.networks.server.view.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.OkHttpClient;
import retrofit2.http.DELETE;

public class MesManager {

    private static final String TAG = "MesManager";
    private AlertDialog mDialog;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static MesManager mesManager;
    private  MesJsonExt mMesJsonExt;
    private MesManager(){}

    public static final int DELETE_CMD = 25;

    public static MesManager getInstance() {
        if(mesManager == null){
            mesManager = new MesManager();
            mesManager.setMesJsonExt(new MesJsonExt());
        }
        return mesManager;
    }

    public void setMesJsonExt(MesJsonExt mesext){
        mMesJsonExt = mesext;
    }

    public void setReceive(final Context context, String values){
        final String result[] = values.split(",");
        LogUtils.d(TAG,"receiver values=" + values);
        if(mMesJsonExt.setReceive(values)){
            return;
        }
        if (result[0].equals("status")) {
            String value = result[1];
            //1=启用
            if (value.equals("1")) {
                SPUtils.put(CarServiceClient.getContext(),
                        Config.IS_DISABLE, true);
                CarServiceClient.stopService();
                try {
                    Thread.sleep(2000);
                }catch (Exception e){}
                CarServiceClient.startService();
                //4=禁用
            } else if (value.equals("4")) {
                SPUtils.put(CarServiceClient.getContext(),
                        Config.IS_DISABLE, false);
                if (CarServiceClient.isRunning()) {
                    CarServiceClient.setAccStatus(Config.System.ACC_OFF);
                }
            }
        }

        if (result[0].equals("sendOrder")) {
            //1=上传结果，0=无需上传
            String type = result[1];
            final String msg = result[2];
            LogUtils.d("jpush receiver sendOrder type:" + type+
                    ",msg=" + msg);

            if(type.equals("1")){
                ThreadUtil.start(new Runnable() {
                    @Override
                    public void run() {
                        String[] shell = msg.split(" ");
                        String value = Utils.execute(shell);
                        String time = format.format(new Date());
                        String fileName = Config.SHELL_FILE + "shell_logs_" + time + ".txt";
                        boolean isOk = FileUtils.writeFile(value,
                                new File(fileName));
                        if(isOk){
                            if(FileUtils.isSDCardPresent()){
                                final CrashError crashError = new CrashError();
                                //0重启 1跑飞 2异常 3log日志
                                crashError.setType(3);
                                crashError.setPkg("");
                                crashError.setLog(fileName);
                                crashError.setTime(String.valueOf(System.currentTimeMillis() / 1000));
                                CarServiceClient.getDaoInstant().getCrashErrorDao().insert(crashError);
                                LogUtils.d("----sendOrder---upload file...");
                                NetWorkManagement.newInstance().postUploadErrorFile(crashError);
                            }
                        }else{
                            LogUtils.d("sendOrder create file error!");
                        }
                    }
                });
            }else if(type.equals("0")){
                ThreadUtil.start(new Runnable() {
                    @Override
                    public void run() {
                        String[] shell = msg.split(" ");
                        String text = Utils.execute(shell);
                        LogUtils.d("sendOrder shell=" + text);
                    }
                });
            }
        }

        if (result[0].equals("getLog")) {
            String type = result[1];
            LogUtils.d(TAG,"receiver getLog type :" + type);
            if (type.equals("3")) {
                NetWorkManagement.newInstance().postUploadLogFile();
            }
        }

        boolean isDisable = (boolean)
                SPUtils.get(context, Config.IS_DISABLE, true);
        LogUtils.d(TAG,"receiver  isDisable=" + isDisable);
        if (isDisable) {
            if (result[0].equals("order") || result[0].equals("0101")) {
                String type = result[1];
                if (type.equals(Config.CONTROL_IMAGE)) {
                    //已停用
//                            DvrManagement dvrManagement = DvrManagement.newInstance();
//                            dvrManagement.uploadImagePath();
                }
                if (type.equals(Config.CONTROL_VIDEO)) {
                    //已停用
//                            DvrManagement dvrManagement = DvrManagement.newInstance();
//                            dvrManagement.uploadVideoPath();
                }
                String value = "";
                if (result[2] != null) {
                    value = result[2];
                }
                if (type.equals(Config.CONTROL_ACC)) {
                    if (value.equals(Config.CONTROL_ON)) {
                        ToastUtils.showShort(context, "远程启动");
                        uploadComplete(Config.CONTROL_ACC,
                                Config.CONTROL_ON,
                                Config.CONTROL_VALUE_SUCCESS);
                    } else if (value.equals(Config.CONTROL_OFF)) {
                        ToastUtils.showShort(context, "远程熄火");
                        uploadComplete(Config.CONTROL_ACC,
                                Config.CONTROL_OFF,
                                Config.CONTROL_VALUE_SUCCESS);
                    }
                }
                if (type.equals(Config.CONTROL_LIGHT)) {
                    if (value.equals(Config.CONTROL_ON)) {
                        ToastUtils.showShort(context, "大灯控制：开");
                        uploadComplete(Config.CONTROL_LIGHT,
                                Config.CONTROL_ON,
                                Config.CONTROL_VALUE_SUCCESS);
                    } else if (value.equals(Config.CONTROL_OFF)) {
                        ToastUtils.showShort(context, "大灯控制：关");
                        uploadComplete(Config.CONTROL_LIGHT,
                                Config.CONTROL_OFF,
                                Config.CONTROL_VALUE_SUCCESS);
                    }
                }
                if (type.equals(Config.CONTROL_DOOR)) {
                    if (value.equals(Config.CONTROL_ON)) {
                        ToastUtils.showShort(context, "车门控制：解锁");
                        uploadComplete(Config.CONTROL_DOOR,
                                Config.CONTROL_ON,
                                Config.CONTROL_VALUE_SUCCESS);
                    } else if (value.equals(Config.CONTROL_OFF)) {
                        ToastUtils.showShort(context, "车门控制：上锁");
                        uploadComplete(Config.CONTROL_DOOR,
                                Config.CONTROL_OFF,
                                Config.CONTROL_VALUE_SUCCESS);
                    }
                }
                if (type.equals(Config.CONTROL_WINDOW)) {
                    if (value.equals(Config.CONTROL_ON)) {
                        ToastUtils.showShort(context, "车窗控制：开");
                        uploadComplete(Config.CONTROL_WINDOW,
                                Config.CONTROL_ON,
                                Config.CONTROL_VALUE_SUCCESS);
                    } else if (value.equals(Config.CONTROL_OFF)) {
                        ToastUtils.showShort(context, "车窗控制：关");
                        uploadComplete(Config.CONTROL_WINDOW,
                                Config.CONTROL_OFF,
                                Config.CONTROL_VALUE_SUCCESS);
                    }
                }
                if (type.equals(Config.CONTROL_SPEAKER)) {
                    if (value.equals(Config.CONTROL_ON)) {
                        ToastUtils.showShort(context, "喇叭控制：开");
                        uploadComplete(Config.CONTROL_SPEAKER,
                                Config.CONTROL_ON,
                                Config.CONTROL_VALUE_SUCCESS);
                    } else if (value.equals(Config.CONTROL_OFF)) {
                        ToastUtils.showShort(context, "喇叭控制：关");
                        uploadComplete(Config.CONTROL_SPEAKER,
                                Config.CONTROL_OFF,
                                Config.CONTROL_VALUE_SUCCESS);
                    }
                }
                if (type.equals(Config.CONTROL_AIRCONDITIONING)) {
                    if (value.equals(Config.CONTROL_ON)) {
                        ToastUtils.showShort(context, "空调控制：开");
                        uploadComplete(Config.CONTROL_AIRCONDITIONING,
                                Config.CONTROL_ON,
                                Config.CONTROL_VALUE_SUCCESS);
                    } else if (value.equals(Config.CONTROL_OFF)) {
                        ToastUtils.showShort(context, "空调控制：关");
                        uploadComplete(Config.CONTROL_AIRCONDITIONING,
                                Config.CONTROL_OFF,
                                Config.CONTROL_VALUE_SUCCESS);
                    }
                }

            } else if (result[0].equals("meet")) {
                final double lng = Double.parseDouble(result[1].trim());
                final double lat = Double.parseDouble(result[2].trim());
                GeocodeSearch geocoderSearch = new GeocodeSearch(context);
                geocoderSearch.setOnGeocodeSearchListener(
                        new GeocodeSearch.OnGeocodeSearchListener() {
                            @Override
                            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                                RegeocodeAddress ra = regeocodeResult.getRegeocodeAddress();
                                String addRess = ra.getFormatAddress();
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("接人消息")
                                        .setMessage("地址:" + addRess + "\n" + "是否前往?")
                                        .setTopImage(R.mipmap.ic_message)
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                mDialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mDialog.dismiss();
                                                Intent myintent = new Intent();
                                                myintent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                                                myintent.putExtra("KEY_TYPE", 10038);
                                                myintent.putExtra("POINAME", "");
                                                myintent.putExtra("LAT", lat);
                                                myintent.putExtra("LON", lng);
                                                myintent.putExtra("DEV", 0);
                                                myintent.putExtra("STYLE", 0);
                                                myintent.putExtra("SOURCE_APP", "C2 App");
                                                context.sendBroadcast(myintent);
                                            }
                                        });
                                mDialog = builder.create();
                                mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                mDialog.show();
                            }

                            @Override
                            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                            }
                        });
                LatLonPoint latLonPoint = new LatLonPoint(lat, lng);
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
                geocoderSearch.getFromLocationAsyn(query);
            } else if (result[0].equals("navigation")) {

                        final double lng = Double.parseDouble(result[1].trim());
                        final double lat = Double.parseDouble(result[2].trim());
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("快速导航")
                                .setMessage("是否立即前往?")
                                .setTopImage(R.mipmap.ic_message)
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mDialog.dismiss();
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDialog.dismiss();
                                        Intent myintent = new Intent();
                                        myintent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                                        myintent.putExtra("KEY_TYPE", 10038);
                                        myintent.putExtra("POINAME", "");
                                        myintent.putExtra("LAT", lat);
                                        myintent.putExtra("LON", lng);
                                        myintent.putExtra("DEV", 0);
                                        myintent.putExtra("STYLE", 0);
                                        myintent.putExtra("SOURCE_APP", "C2 App");
                                        context.sendBroadcast(myintent);
                                    }
                                });
                        mDialog = builder.create();
                        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        mDialog.show();


            } else if (result[0].equals("fence")) {
                if (result[1].equals("1")) {
                    LocationManagement.newInstance().createGeofence(
                            Double.valueOf(result[3]),
                            Double.valueOf(result[2]),
                            Float.valueOf(result[4]));
                } else {
                    LocationManagement.newInstance().destroyGeofence();
                }
            } else if (result[0].equals("config")) {
                int start = values.indexOf(",");
                String json = values.substring(++start, values.length());
                try {
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
                    e.printStackTrace();
                }
            } else if (result[0].equals("mcuUpdate")) {
                String url = result[1];
                final String path = "/storage/emulated/0/DMCU/";
                if (url != null && !url.equals("")) {
                    int temp = url.lastIndexOf("/");
                    final String fileName = url.substring(++temp, url.length())
                            .replace("_urgent", "");
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
                                    if (isFinish) {
                                        Intent mcuIntent = new Intent(wwc2Broadcast.SYSTEM_MCU_STATUS);
                                        mcuIntent.putExtra(wwc2Broadcast.MCU_STATUS,
                                                wwc2Broadcast.MCU_STATUS_OK);
                                        mcuIntent.putExtra(wwc2Broadcast.MCU_PATH,
                                                path + fileName);
                                        CarServiceClient.getContext().sendBroadcast(mcuIntent);
                                    }
                                }

                                @Override
                                public void onError(long id, Exception e) {
                                    Intent mcuIntent = new Intent(wwc2Broadcast.SYSTEM_MCU_STATUS);
                                    mcuIntent.putExtra(wwc2Broadcast.MCU_STATUS,
                                            wwc2Broadcast.MCU_STATUS_ON);
                                    mcuIntent.putExtra(wwc2Broadcast.MCU_PATH,
                                            "");
                                    CarServiceClient.getContext().sendBroadcast(mcuIntent);
                                }
                            });
                }
            } else if (result[0].equals("clearIccid")) {
                FileUtils.deleteFile(Config.Local.SIM_ICCID_PATH);
            } else if (result[0].equals("simNoticeSet")) {
                String type = result[1];
                LogUtils.d("jpush receiver simNoticeSet type:" + type);
                Map<String, String> keys = new HashMap<>(16);
                keys.put(Config.Local.SIM_SET_KEY, "1".equals(type) ? "1" : "0");
                FileUtils.writeFile(keys, new File(Config.Local.SIM_SET_PATH));
            } else if (result[0].equals("takePhoto")) {//一键拍照
                //1=前视 2=后视 3=左视  4=右视  5=四分图
                String type = result[1];
                //指令类型来源, 1=后台 2=公众号 3=App
                String fromType = result[2];
                int acc = CarServiceClient.getAccStatus();

                LogUtils.d(TAG,"receiver takePhoto! ---acc=" + acc
                        + ",,,type=" + type + ",,fromType=" + fromType);

                CarServiceClient.updateLogs("收到'一键拍照'请求.");

                RemotelyBean remotelyBean = new RemotelyBean();

               if(Utils.isCameraSwitch(CarServiceClient.getContext())){
                   if("1".equals(type)){
                       type= "2";
                   }else if("2".equals(type)){
                       type= "1";
                   }
               }


                remotelyBean.setFromType(fromType);
                remotelyBean.setType(MesAdpter.getCameraType(type));
                remotelyBean.setValues(values);
                //zhongyang.hu add for acc

                CmdManager.getInstans().postCmd(new TakePictureCmd(remotelyBean));

            } else if (result[0].equals("videoPage")) {//获取视频分页，回放数据
                //指令类型来源, 1=后台 2=公众号 3=App
                String fromType = result[1];
                //1=前视 2=后视
                String type = result[2];
                //页码
                String page = result[3];
                //数量
                String number = result[4];

                if(Utils.isCameraSwitch(CarServiceClient.getContext())){
                    if("1".equals(type)){
                        type= "2";
                    }else if("2".equals(type)){
                        type= "1";
                    }
                }

                RemotelyBean remotelyBean = new RemotelyBean();
                remotelyBean.setFromType(fromType);
                remotelyBean.setType(MesAdpter.getCameraType(type));
                remotelyBean.setPage(page);
                remotelyBean.setNumber(number);
                remotelyBean.setValues(values);

                LogUtils.d(TAG,"receiver videoPage remotelyBean:" + remotelyBean.toString());

                CmdManager.getInstans().postCmd(new GetVideoListCmd(remotelyBean));

            } else if (result[0].equals("apkUpload")) {//应用添加升级
                //1=新增模式 2=替换模式
                String type = result[1];
                String packageName = result[2];
                String version = result[3];
                String url = result[4];

                LogUtils.d(TAG,"receiver apkUpload!");

                /**
                 * 有可能收到带组的应用，会收到多条消息，必须单独开线程处理
                 */
                if (!TextUtils.isEmpty(type)) {
                    NetWorkManagement.newInstance()
                            .jpushUploadApp(type, version, packageName, url);
                }
            } else if (result[0].equals("takeVideo")) {//融云实时视频
                //指令类型来源, 1=后台 2=公众号 3=App
                String fromType = result[1];
                //视频类型1前视2后视3本地视频
                String type = result[2];
                //房间号
                String roomId = result[3];
                //本地视频地址

                if(Utils.isCameraSwitch(CarServiceClient.getContext())){
                    if("1".equals(type)){
                        type= "2";
                    }else if("2".equals(type)){
                        type= "1";
                    }
                }


                String filePath = "";
                String userId = "";
                if(type.equals("3")){
                    filePath = result[4];
                }
                if (fromType.equals("1")){
                      userId = result[result.length-1];
                }

                RemotelyBean remotelyBean = new RemotelyBean();
                remotelyBean.setFromType(fromType);
                remotelyBean.setType(MesAdpter.getCameraType(type));
                remotelyBean.setRoomId(roomId);
                //类型为3时，才有值，本地源文件全路径
                remotelyBean.setUrl(filePath);
                remotelyBean.setValues(values);
                remotelyBean.setUserId(userId);

                LogUtils.d(TAG,"receiver 融Y takeVideo remotelyBean=" + remotelyBean.toString());
                if(type.equals(Config.STR_LOCAL_VIDEO)) {
                    CmdManager.getInstans().postCmd(new TakeLocalFileCmd(remotelyBean));
                }else{
                    CmdManager.getInstans().postCmd(new TakeVideoCmd(remotelyBean));
                }

            } else if(result[0].equals("videoHeart")){
                //指令类型来源, 1=后台 2=公众号 3=App
                String fromType = result[1];
                //用户ID
                String userId = result[2];
                //房间号
                String roomId = result[3];

               try {
                   if(CmdManager.getInstans().getCurrentCmd() != null
                           && CmdManager.getInstans().getCurrentCmd() instanceof TakeVideoCmd){
                       ((TakeVideoCmd)CmdManager.getInstans().getCurrentCmd()).checkRoomIDAndFeedDog(roomId);
                   }
               }catch (Exception e){
                   e.printStackTrace();
               }
            }else if (result[0].equals("collisionLevel")) {//sensor
                //关闭=0，轻微=1，中等=2，严重=3
                String level = result[1];
                int acc = CarServiceClient.getAccStatus();
                LogUtils.d(TAG,"receiver sensor ---acc=" + acc
                        + ",,,level=" + level);

                if(!TextUtils.isEmpty(level)){
                    String oLevel = (String) SPUtils.get(CarServiceClient.getContext(),
                            Config.SENSOR_LEVEL, "");
                    LogUtils.d(TAG,"receiver oLevel=" + oLevel + ",,level=" + level);
                    if(!level.equals(oLevel)){
                        SPUtils.put(CarServiceClient.getContext(), Config.SENSOR_LEVEL, level);
                        //通知
                        if(acc == Config.System.ACC_ON){
                            DvrManagement.newInstance().updateSensor();
                        }else{
                            LogUtils.d(TAG,"receiver sensor 不支持off设置状态");
                        }
                    }else{
                        LogUtils.d(TAG,"collisionLevel  相同状态不处理...");
                    }
                }
            } else if (result[0].equals("h264stream")) {//sensor
                //zhongyang
                String value = result[1];
                int mode = Integer.valueOf(MesAdpter.getCameraType(value));
                 DvrManagement.newInstance().setH264Stream(mode);
            }
        }
    }

    private void uploadComplete(String type, String key, String value) {
        NetWorkManagement.newInstance().postCompleteInfo(
                type,
                key,
                value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        if (result.equals(Config.System.UPLOAD_COMPLETE_OK)) {
                        } else {
                        }
                    }
                });
    }
}