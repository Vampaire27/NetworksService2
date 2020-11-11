package com.wwc2.networks.server.internet;

import android.os.SystemProperties;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.wwc2.networks.BuildConfig;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.auto.AutoManagement;
import com.wwc2.networks.server.bean.AMapBean;
import com.wwc2.networks.server.bean.BaseBean;
import com.wwc2.networks.server.bean.ConfigBean;
import com.wwc2.networks.server.bean.DeviceBean;
import com.wwc2.networks.server.bean.GPSBean;
import com.wwc2.networks.server.bean.JsimBean;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.bean.TimeBean;
import com.wwc2.networks.server.bean.ViewBean;
import com.wwc2.networks.server.bean.ZWResponseBean;
import com.wwc2.networks.server.bean.heart.MesJsonAck;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.dvr.jni.DvrDataNative;
import com.wwc2.networks.server.location.GpsPointManager;
import com.wwc2.networks.server.location.LocationUtils;
import com.wwc2.networks.server.protection.AES;
import com.wwc2.networks.server.protection.ConvertUtils;
import com.wwc2.networks.server.protection.EncryUtil;
import com.wwc2.networks.server.protection.RSA;
import com.wwc2.networks.server.protection.SecureRandomUtil;
import com.wwc2.networks.server.provider.CrashError;
import com.wwc2.networks.server.provider.Statistics;
import com.wwc2.networks.server.provider.Trace;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.AppUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.NetworkUtil;
import com.wwc2.networks.server.utils.SignUtils;
import com.wwc2.networks.server.utils.TelephonyUtil;
import com.wwc2.networks.server.utils.ThreadUtil;
import com.wwc2.networks.server.utils.Utils;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetWorkManagement {
    private final String TAG = "NetWorkManagement";
    private static NetWorkManagement netWorkManagement = null;

    private NetWorkManagement() {
    }

    public static NetWorkManagement newInstance() {
        if (netWorkManagement == null) {
            netWorkManagement = new NetWorkManagement();
        }
        return netWorkManagement;
    }

    public Flowable<String> postUploadOs() {

        ApiFactory.UploadSystemApi uploadSystemApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getUploadSystem();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        String type = device.getPlatformType();
        LogUtils.d(" postUploadInfo type before = " +type);
        if(Config.TYPE_360.equals(type)){
            type = "quart_stream";

        }
        LogUtils.d(" postUploadInfo type post= " +type);
        String[] values = {Config.System.UPLOAD_OS,
                device.getSerialNumber(),
                device.getSystemCode(),
                device.getAPPCode(),
                device.getBluetoothCode(),
                device.getMcuCode(),
                device.getIMEICode(),
                device.getMacCode(),
                device.getProduct(),
                device.getCanbusName(),
                BuildConfig.VERSION_NAME,
                device.getUiStyle(),
                device.getUiSwitch(),
                String.valueOf(System.currentTimeMillis() / 1000),
                String.valueOf(DvrManagement.newInstance().getSupportCameraNum()), //zhongyang.hu add for CameraSupport 20200110
                type};
        String bean = Utils.conversions(true, values);
        RequestBody requestBody = RequestBody.create(null, bean);
        return uploadSystemApi.getValue(requestBody);
    }

//    public void postUploadInfo(AMapBean bean, final Info nInfo, final boolean isdao) {  //only GPS info
//        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
//        if (bean == null) {
//            bean = GpsPointManager.getInstance().getGPSBean();
//        }
//        LogUtils.d("定位33333333333:" +bean.toString() );
//        double lat = 0;
//        double lng = 0;
//        if (bean.getGpstype() ==AMapBean.AMAP_GAODE){
//            lat =  bean.getLatitude();
//            lng =  bean.getLongitude();
//        }else {
//            lat =  bean.getGpslat();
//            lng =  bean.getGpslng();
//        }
//        if (lat ==0.0 || lng ==0.0){
//            return;
//        }
//
//        LocationBean locationBean = new LocationBean();
//        locationBean.setCmd("0002");
//        locationBean.setVer("v1.0.0_20191123");
//        locationBean.setGtype(bean.getGpstype());
//        locationBean.setLat(lat);
//        locationBean.setLng(lng);
//        locationBean.setSp(LocationUtils.conversionSpeed(bean.getSpeed()));
//        locationBean.setBear(bean.getBearing());
//        locationBean.setTm(System.currentTimeMillis() / 1000);
//
//      String locationJson =   ResultPaser.JsonParse(locationBean);
//      LogUtils.d("定位44444444444 发送数据-------->" + locationJson);
//        ClientConnect.getInstance().getChannel().write(locationJson);
//        //isdao:true缓存，false非缓存
//        //如果是缓存数据上传失败不再重复缓存
//       /* ApiFactory.UploadSystemApi uploadSystemApi = ApiFactory.newInstance(
//                CarServiceClient.getContext()).getUploadSystem();
//        RequestBody requestBody;
//        final String uploadInfo;
//        if (nInfo != null) {
//            uploadInfo = nInfo.getInfo();
//            requestBody = RequestBody.create(null, nInfo.getInfo());
//        } else {
//            DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
//            CanbusManagement canbus = CanbusManagement.newInstance();
//            if (bean == null) {
//                bean = CarServiceClient.getAMapBean();
//            }
//            String[] values = {Config.System.UPLOAD_INFO,
//                    device.getSerialNumber(),
//                    "V1",
//                    bean.getGpstype() + "",
//                    (bean.getLocate()) + "",
//                    "1",
//                    bean.getLongitude() + "",
//                    "1",
//                    bean.getLatitude() + "",
//                    canbus.getTotalMileage() + "",
//                    canbus.getInstantFuel() + "",
//                    canbus.getResidualOilVolume() + "",
//                    LocationUtils.conversionSpeed(bean.getSpeed()) + "",
//                    canbus.getEngineSpeed() + "",
//                    bean.getBearing() + "",
//                    canbus.getBatteryLevel() + "",
//                    "0",
//                    "0",
//                    canbus.getTpmTires(0) + "",
//                    canbus.getTpmTires(1) + "",
//                    canbus.getTpmTires(2) + "",
//                    canbus.getTpmTires(3) + "",
//                    "0",
//                    "0",
//                    "0",
//                    "0",
//                    "0",
//                    "0",
//                    CarServiceClient.getAccStatus() + "",
//                    String.valueOf(System.currentTimeMillis() / 1000)};
//            String info = Utils.conversions(true, values);
//            uploadInfo = info;
//            requestBody = RequestBody.create(null, info);
//            LogUtils.d(bean.getLongitude() + "|" + bean.getLatitude() + "|" + bean.getGpstype());
//        }
//        LogUtils.d("postUploadInfo..");
//        uploadSystemApi.getValue(requestBody)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String result) throws Exception {
//                        LogUtils.d("postUploadInfo result:" + result + ",uploadInfo=" + uploadInfo);
//                        if (result.equals(Config.System.UPLOAD_INFO_OK)) {
//                            if (nInfo != null) {
//                                CarServiceClient.getDaoInstant().getInfoDao().delete(nInfo);
//                            }
//                        }else{
//                            LogUtils.d("postUploadInfo 异常观察............");
//
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        LogUtils.d("upload error...");
//                        if (!isdao) {
//                            try {
//                                Info info = new Info();
//                                info.setInfo(uploadInfo);
//                                CarServiceClient.getDaoInstant().getInfoDao().insertOrReplace(info);
//                            } catch (Exception e) {
//                            }
//                        }
//                    }
//                });*/
//    }

//    public Flowable<String> postUploadTrace(Trace trace) {
//        ApiFactory.UploadSystemApi uploadSystemApi = ApiFactory.newInstance(
//                CarServiceClient.getContext()).getUploadSystem();
//        int maxSpeed = LocationUtils.conversionSpeed(trace.getMaxSpeed());
//        int averageSpeed = LocationUtils.conversionSpeed(trace.getAverageSpeed());
//        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
//        String[] values = {Config.System.UPLOAD_MILEAGE,
//                device.getSerialNumber(),
//                "1",
//                "1",
//                trace.getStartLongitude() + "",
//                "1",
//                trace.getStartLatitude() + "",
//                "1",
//                "1",
//                trace.getEndLongitude() + "",
//                "1",
//                trace.getEndLatitude() + "",
//                "100",
//                "0.0",
//                trace.getSumTime() + "",
//                trace.getSumTrace() + "",
//                averageSpeed + "",
//                maxSpeed + "",
//                "0",
//                "0",
//                "0",
//                "0",
//                trace.getEndTime() + ""};
//        final String info = Utils.conversions(true, values);
//        RequestBody requestBody = RequestBody.create(null, info);
//        return uploadSystemApi.getValue(requestBody);
//    }

//    public Flowable<String> postUploadStatistics(Statistics statistics) {
//        ApiFactory.UploadStatisticsApi uploadStatisticsApi = ApiFactory.newInstance(
//                CarServiceClient.getContext()).getUploadStatistics();
//        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
//        String[] values = {Config.System.UPLOAD_STATISTICS,
//                device.getSerialNumber(),
//                device.getProduct(),
//                statistics.getDates() != null ? statistics.getDates() : "0",
//                statistics.getWifiSum() != null ? statistics.getWifiSum()+"" : "0",
//                statistics.getWifiTime() != null ? statistics.getWifiTime() / 1000 +"" : "0",
//                statistics.getWifiFlow() != null ? statistics.getWifiFlow()+"" : "0",
//                statistics.getOuterSimSum() != null ? statistics.getOuterSimSum()+"" : "0",
//                statistics.getOuterSimTime() != null ? statistics.getOuterSimTime() / 1000 +"" : "0",
//                statistics.getOuterSimFlow() != null ? statistics.getOuterSimFlow()+"" : "0",
//                statistics.getSimSum() != null ? statistics.getSimSum()+"" : "0",
//                statistics.getSimTime() != null ? statistics.getSimTime() / 1000 +"" : "0",
//                statistics.getSimFlow() != null ? statistics.getSimFlow()+"" : "0",
//                statistics.getSimSurplusFlow() != null ? statistics.getSimSurplusFlow()+"" : "0",
//                statistics.getSimFlowTime() != null ? statistics.getSimFlowTime()+"" : "0"};
//        String bean = Utils.conversions(true, values);
//        RequestBody requestBody = RequestBody.create(null, bean);
//        return uploadStatisticsApi.getValue(requestBody);
//    }

    public Flowable<String> postAlarmInfo(int type, String fileUrl) {
        ApiFactory.UploadSystemApi uploadSystemApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getUploadSystem();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        GPSBean lGPSBean = GpsPointManager.getInstance().getGPSBean();
        String[] values = {Config.System.UPLOAD_ALARM,
                device.getSerialNumber(),
                "1"+ "",
                lGPSBean.getGtype() + "",
                "1",
                lGPSBean.getLng() + "",
                "1",
                lGPSBean.getLat() + "",
                type + "",
                type == 8 ? fileUrl : "",
                String.valueOf(System.currentTimeMillis() / 1000)};
        String info = Utils.conversions(true, values);
        RequestBody requestBody = RequestBody.create(null, info);
        return uploadSystemApi.getValue(requestBody);
    }

    public Flowable<String> postCompleteInfo(String type, String key, String value) {
        ApiFactory.UploadSystemApi uploadSystemApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getUploadSystem();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        String[] values = {Config.System.UPLOAD_COMPLETE,
                device.getSerialNumber(),
                type,
                key,
                value};
        String info = Utils.conversions(true, values);
        RequestBody requestBody = RequestBody.create(null, info);
        return uploadSystemApi.getValue(requestBody);
    }

    public Call<OtherBean> postRegister(String JPushID) {
        ApiFactory.RegisterApi register = ApiFactory.newInstance(
                CarServiceClient.getContext()).getRegister();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
       // AMapBean bean = CarServiceClient.getAMapBean();
        GPSBean lGPSBean = GpsPointManager.getInstance().getGPSBean();
        OtherBean otherBean = new OtherBean();
        otherBean.setSerial_no(device.getSerialNumber());
        if (JPushID != null) {
            otherBean.setRegistrationId(JPushID);
        }
        otherBean.setProVersion(device.getProduct());
        Map<String, String> iccIds = FileUtils.readFile(Config.Local.SIM_JSSIM_PATH);
        String type = "0";
        if (iccIds != null) {
            type = iccIds.get(Config.Local.SIM_ICCID_CODE);
            if (type == null || type.equals("")) {
                type = "0";
            }
        }
        otherBean.setType(Integer.parseInt(type));
        otherBean.setIccid(TelephonyUtil.getMime(CarServiceClient.getContext()));
        otherBean.setTime(System.currentTimeMillis() / 1000);
        otherBean.setSimMode(SystemProperties.get("persist.sys.simswich", "1"));
        otherBean.setLatitude(lGPSBean.getLat());
        otherBean.setLongitude(lGPSBean.getLng());
        otherBean.setGpstype(lGPSBean.getGtype());
        return register.getValue(otherBean);
    }

    public Call<OtherBean> postGetConfig(String url) {
        ApiFactory.ConfigApi register = ApiFactory.newInstance(
                CarServiceClient.getContext()).getConfig(url);
        OtherBean otherBean = new OtherBean();
        long time = System.currentTimeMillis() / 1000;
        otherBean.setTime(time);
        String value = "";
        try {
            String md5 = Utils.md5("watercar" + time);
            value = md5.substring(0, 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        otherBean.setSign(value);
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        otherBean.setSerial_no(device.getSerialNumber());
        return register.getValue(otherBean);
    }

    public Call<OtherBean> postGetSysTime() {
        ApiFactory.SysTimeApi sysTimeApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getSysTime();
        return sysTimeApi.getValue();
    }

    public Flowable<JsimBean> getQueryCard() {
        Map<String, Object> map = new HashMap<>();
        ApiFactory.JiaShenInfoApi jiaShenInfoApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getJiaShenInfoApi();
        String appid = Config.JS_CARD_APPID;
        String card_code = TelephonyUtil.getMime(CarServiceClient.getContext());
        String timeline = String.valueOf(System.currentTimeMillis());
        map.put("appid", appid);
        map.put("card_code", card_code);
        map.put("timeline", timeline);
        String sign = SignUtils.createSign(Config.JS_CARD_APPKEY, map);
        return jiaShenInfoApi.queryCard(appid, sign, card_code, timeline);
    }

    public Call<JsimBean> isRealNameQueryIot() {
        Map<String, Object> map = new HashMap<>();
        ApiFactory.JsQueryRealnameApi jiaShenInfoApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getJsQueryRealnameApi();
        String appid = Config.JS_CARD_APPID;
        String card_code = TelephonyUtil.getMime(CarServiceClient.getContext());
        String timeline = String.valueOf(System.currentTimeMillis());
        map.put("appid", appid);
        map.put("card_code", card_code);
        map.put("timeline", timeline);
        String sign = SignUtils.createSign(Config.JS_CARD_APPKEY, map);
        return jiaShenInfoApi.queryRealname(appid, sign, card_code, timeline);
    }

    public Flowable<ZWResponseBean> getQueryZWCard() {
        ApiFactory.ZhiWangQueryApi zhiWangQueryApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getZWQueryApi();
        //代理id(必须填写身份识别参数，否则请求返回非法)
        String agentid = Config.ZW_AGENTID;
        //当前时间戳，10位数字，该时间戳在50秒内会失效，50秒后访问该地址，会返回请求时间戳不合法！
        String stamp = Utils.transformStamp(String.valueOf(System.currentTimeMillis()));
        //32位的MD5加密字符串，加密方法为你的私人密钥跟当前时间戳中间用双竖线拼接后MD5加密，
        //比如你的APP_KEY是4d8b48210b1a3f338d6709b262b231df(右上角查看该值)，
        //利用加密函数MD5(4d8b48210b1a3f338d6709b262b231df||1474579817),其中1474579817为stamp参数的值，
        //必须保证一致，生成新的32位字符串是a98a367c8c677168a5876792794ce2b2，该新字符串即为nonce的值。
        String nonce = Utils.md5("d3e9407bd4b8a14238ee9e34b170cd1f||" + stamp);
        //19或者20位iccid。
        String iccid = TelephonyUtil.getMime(CarServiceClient.getContext());//"89860918700006150020";
        return zhiWangQueryApi.querySim(agentid, stamp, nonce, iccid);
    }

    public Flowable<OtherBean> getCheckSim() {
        ApiFactory.JiaShenCheckApi jiaShenCheckApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getJiaShenCheckApi();
        String card_code = TelephonyUtil.getMime(CarServiceClient.getContext());
        return jiaShenCheckApi.checkSim(card_code);
    }

    /**
     * 检查APK_ID次数
     *
     * @param apkId
     * @return
     */
    public Flowable<DeviceBean> checkSimVerify(String apkId) {
        ApiFactory.SimVerifyApi simVerifyApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).checkSimVerify();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.setApkVersion(apkId);
        deviceBean.setSerial_no(device.getSerialNumber());
        return simVerifyApi.getValue(deviceBean);
    }




    public void uploadSensorPicture( String path,final NetWorkManagement.onRequestApi mOnRequestApi){
        LogUtils.d("uploadSensorPicture path=" + path);// + ",,,,fil=" + FileUtils.getImageStr(path));
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        GPSBean lGPSBean = GpsPointManager.getInstance().getGPSBean();
        ViewBean view = new ViewBean();
        view.setSerial_no(device.getSerialNumber());
        view.setLocate(1);
        view.setLngLoc(1);
        view.setLongitude(lGPSBean.getLng());
        view.setLatLoc(1);
        view.setLatitude(lGPSBean.getLat());
        view.setType(Config.App.IMAGE);
        view.setAllTime(0);
        view.setMirrorType("2");
        view.setTime(System.currentTimeMillis() / 1000);
        view.setImgStr(FileUtils.getImageStr(path));
        ApiFactory.UploadViewApi uploadImage = ApiFactory.newInstance(
                CarServiceClient.getContext()).getUploadView();
        uploadImage.getValue(view)
                .subscribeOn(Schedulers.io())
                .timeout(20, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<OtherBean>() {
                    @Override
                    public void accept(OtherBean otherBean) throws Exception {
                        String code = otherBean.getCode();
                        if (code.equals(Config.App.RESULT_OK)) {
                            String fileUrl = otherBean.getUrl();
                            LogUtils.d(".......uploadSensorPicture fileUrl=" + fileUrl);
                            if(!TextUtils.isEmpty(fileUrl)){
                                NetWorkManagement.newInstance().postAlarmInfo(Config.ALARM_COLLISION, fileUrl)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<String>() {
                                            @Override
                                            public void accept(String result) throws Exception {
                                                LogUtils.d("...上报完成....uploadSensorPicture result=" + result);

                                                if(mOnRequestApi != null) {
                                                    mOnRequestApi.success();
                                                }

                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                LogUtils.d(".......uploadSensorPicture throwable=" + throwable);
                                                NetworkUtil.showNetworkLog();
                                                if(mOnRequestApi != null) {
                                                    mOnRequestApi.onfail();
                                                }
                                            }
                                        });
                            }
                        }else{
                            LogUtils.d("uploadSensorPicture msg=" + otherBean.getMsg());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
//                        LogUtils.d(".......postAlarmInfo throwable=" + throwable);
                    }
                });
    }

    public void postGetCanMcuUrl(Callback<OtherBean> callback) {
        ApiFactory.CanMcuUrlApi canMcuUrl = ApiFactory.newInstance(
                CarServiceClient.getContext()).getCanMcuUrl();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setProVersion(device.getProduct());
        otherBean.setSerial_no(device.getSerialNumber());
        Call<OtherBean> call = canMcuUrl.getValue(otherBean);
        call.enqueue(callback);
    }

    public void postGetMcuUrl(Callback<OtherBean> callback) {
        ApiFactory.McuUrlApi mcuUrl = ApiFactory.newInstance(
                CarServiceClient.getContext()).getMcuUrl();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setProVersion(device.getProduct());
        otherBean.setSerial_no(device.getSerialNumber());
        Call<OtherBean> call = mcuUrl.getValue(otherBean);
        call.enqueue(callback);
    }

    public void postGetAPNUrl(Callback<OtherBean> callback) {
        ApiFactory.APNUrlApi apnUrl = ApiFactory.newInstance(
                CarServiceClient.getContext()).getAPNUrl();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setProVersion(device.getProduct());
        otherBean.setSerial_no(device.getSerialNumber());
        Call<OtherBean> call = apnUrl.getValue(otherBean);
        call.enqueue(callback);
    }

    public void postGetMallRequired(Callback<OtherBean> callback) {
        ApiFactory.MallRequiredApi mallRequired = ApiFactory.newInstance(
                CarServiceClient.getContext()).getMallRequired();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setProVersion(device.getProduct());
        otherBean.setSerial_no(device.getSerialNumber());
        Call<OtherBean> call = mallRequired.getValue(otherBean);
        call.enqueue(callback);
    }

    public void getRemoteTime(Callback<TimeBean> callback) {
        ApiFactory.RemoteTimeApi timeRequired = ApiFactory.newInstance(
                CarServiceClient.getContext()).getTime();
        Call<TimeBean> call = timeRequired.getTime();
        call.enqueue(callback);
    }

    public void getRemoteConfig(Callback<ConfigBean> callback) {
        ConfigBean.RequestBean bean = new ConfigBean.RequestBean();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        bean.setSerNo(device.getSerialNumber());

        ApiFactory.RemoteConfigApi configRequired = ApiFactory.newInstance(
                CarServiceClient.getContext()).getRemoteConfig();
        Call<ConfigBean> call = configRequired.getRemoteConfig(bean);
        call.enqueue(callback);
    }

    public Flowable<OtherBean> postGetLbsInfo(String lbs) {
        ApiFactory.LbsInfoApi lbsRequired = ApiFactory.newInstance(
                CarServiceClient.getContext()).getLbsInfo();
        OtherBean otherBean = new OtherBean();
        otherBean.setLbsData(lbs);
        return lbsRequired.getValue(otherBean);
    }

    public Flowable<OtherBean> postGetAdvertising(){
        ApiFactory.GetAdvertisingApi advertisingApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getAdvertising();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setProVersion(device.getProduct());
        otherBean.setSerial_no(device.getSerialNumber());
        String iccid = TelephonyUtil.getMime(CarServiceClient.getContext());
        if(iccid != null){
            otherBean.setIccid(iccid);
        }
        return advertisingApi.getValue(otherBean);
    }

    public void postDownloadFile(final OkHttpClient mOkHttpClient,
                                 final String url,
                                 final String dir,
                                 ProgressListener progressListener) {
        final String mNewDownloadUrl = ProgressManager
                .getInstance()
                .addDiffResponseListenerOnSameUrl(url, progressListener);
        int temp = url.lastIndexOf("/");
        final String fileName = url.substring(++temp, url.length())
                .replace("_urgent", "");
        LogUtils.d("postDownloadFile...fileName=" + fileName);
        ThreadUtil.start(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url(mNewDownloadUrl)
                            .build();
                    okhttp3.Response
                            response = mOkHttpClient.newCall(request).execute();
                    InputStream is = response.body().byteStream();
                    if (FileUtils.checkParent(dir)) {
                        File file = new File(dir, fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedInputStream bis = new BufferedInputStream(is);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = bis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.flush();
                        fos.close();
                        bis.close();
                        is.close();
                    } else {
                        ProgressManager.getInstance().notifyOnErorr(mNewDownloadUrl,
                                null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ProgressManager.getInstance().notifyOnErorr(mNewDownloadUrl, e);
                }
            }
        });
    }

    public void postFeedBack( String jPush, boolean result, String msg) {
        String code = result == true ? "0" : "1";
        LogUtils.d("---jPush=" + jPush +"---code=" + code + ",msg=" + msg);
        ApiFactory.FeedBackApi feedBackApi = ApiFactory.newInstance(CarServiceClient.getContext())
                .setFeedBackApi();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setSerial_no(device.getSerialNumber());
        otherBean.setCommand(jPush);
        //0=成功，1=异常
        otherBean.setCode(code);
        otherBean.setMsg(msg);
        //请求
        feedBackApi.getValue(otherBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<OtherBean>() {
                    @Override
                    public void accept(OtherBean otherBean) throws Exception {
                        LogUtils.d("postFeedBack OK---jPush = "  );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable Throwable) throws Exception {
                       Throwable.printStackTrace();
                    }
                });

    }



    public void postFeedBackJson(int cmdId,String flowId,int code) {

        ApiFactory.FeedBackYRApi mfeedBackYRApi = ApiFactory.newInstance(CarServiceClient.getContext())
                .getFeedBackRYApi();

        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());

        MesJsonAck mMes= new MesJsonAck(device.getSerialNumber(),cmdId,flowId,code);

        mfeedBackYRApi.getValue(mMes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MesJsonAck>() {
                    @Override
                    public void accept(MesJsonAck otherBean) throws Exception {
                        LogUtils.d("MesJsonAck ---jPush = " +otherBean );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable Throwable) throws Exception {
                        Throwable.printStackTrace();
                    }
                });


    }



    public Flowable<OtherBean> postActivateDevice(){
        ApiFactory.ActivateDeviceApi activateDeviceApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).postActivateDeviceApi();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        DvrDataNative dvrDataNative = DvrDataNative.newInstance();
        OtherBean otherBean = new OtherBean();

        try {

            TreeMap<String, Object> params = new TreeMap<String, Object>();
            String key1 = Integer.toHexString(dvrDataNative.getDeviceKey0());
            String key2 = Integer.toHexString(dvrDataNative.getDeviceKey1());
            params.put("deviceId", key1 + "," + key2);

            params.put("proVersion", device.getProduct());//"ch001_wt");//
            params.put("agentName", "");


            params.put("serNo", device.getSerialNumber());
            params.put("time", System.currentTimeMillis() / 1000);

            LogUtils.d("postActivateDevice params:" + params.toString());

            String sign = EncryUtil.handleRSA(params, Config.App.clientPrivateKey);
            params.put("sign", sign);

            String info = new Gson().toJson(params);

            String aesKey = SecureRandomUtil.getRandom(16);
            String data = AES.encryptToBase64(ConvertUtils.stringToHexString(info), aesKey);
            String encryptkey = RSA.encrypt(aesKey, Config.App.serverPublicKey);

            otherBean.setReqData(data);
            otherBean.setReqEncryptkey(encryptkey);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return activateDeviceApi.getValue(otherBean);
    }

    public Flowable<OtherBean> postNetworkType(int type){
        ApiFactory.NetworkTypeApi networkTypeApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).postNetworkTypeApi();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setSerial_no(device.getSerialNumber());
        otherBean.setProVersion(device.getProduct());
        otherBean.setNetworkType(type+"");
        return networkTypeApi.getValue(otherBean);
    }

    public void jpushUploadApp(final String type, final String ver,
                               final String pkg, final String url){
        String version = AppUtils.getAppVersionName(
                CarServiceClient.getContext(),
                pkg);
        if(type.equals("2")){
            if (version == null) {
                LogUtils.d("...jpushUploadApp...替换模式...apk不存在.");
                return;
            }
        }
        if(version != null && version.equals(ver)){
           LogUtils.d("...jpushUploadApp...版本相同.");
           return;
        }
        dowAndInsApk(url);
    }

    public void dowAndInsApk(final String url){
        ThreadUtil.start(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(url)) {
                    final String path = Config.APK_PATH;
                    int temp = url.lastIndexOf("/");
                    final String fileName = url.substring(++temp, url.length())
                            .replace("_urgent", "");
                    final String mPath = path + fileName;
                    boolean isExists = FileUtils.fileIsExists(mPath);
                    LogUtils.d("...dowAndInsApk...mPath="+ mPath
                            +",,,---fileName=" + fileName
                            + ",,,---isExists=" + isExists);
                    if (isExists) {
                        boolean isDel = FileUtils.deleteFile(mPath);
                        LogUtils.d("file isExists. isDel=" + isDel);
                    }
                    NetWorkManagement.newInstance().postDownloadFile(
                            ProgressManager.getInstance()
                                    .with(new OkHttpClient.Builder())
                                    .build(),
                            url,
                            path,
                            new ProgressListener() {
                                @Override
                                public void onProgress(ProgressInfo progressInfo) {
                                    progressInfo.getId();
                                    progressInfo.getContentLength();
                                    progressInfo.getCurrentbytes();
                                    boolean isFinish = progressInfo.isFinish();
                                    LogUtils.d("---app---id="
                                            + progressInfo.getId() +"---size="
                                            + progressInfo.getContentLength()+"---byte="
                                            + progressInfo.getCurrentbytes()+"---isFinish="
                                            + isFinish);
                                    if (isFinish) {
                                        boolean res = AppUtils.install(mPath);
                                        LogUtils.d("---install apk...res=" + res);
                                        if(res){
                                            boolean del = FileUtils.deleteFile(mPath);
                                            LogUtils.d("---install apk...del=" + del);
                                        }else{
                                            LogUtils.d("install apk...false!!!!!!");
                                        }
                                    }
                                }

                                @Override
                                public void onError(long id, Exception e) {
                                    LogUtils.d("install apk...onError...e=" + e.toString());
                                }
                            });
                }
            }
        });
    }

    public Flowable<OtherBean> postRYtoKen(String rebuild){
        ApiFactory.GetRY_TokenApi ryTokenApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).getRY_Token();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        BaseBean baseBean = new BaseBean();
        baseBean.setSerial_no(device.getSerialNumber());
        baseBean.setRebuild(rebuild);
        return ryTokenApi.getValue(baseBean);
    }

    public Flowable<OtherBean> postSensorApi(String level){
        ApiFactory.SensorApi networkTypeApi = ApiFactory.newInstance(
                CarServiceClient.getContext()).postSensorApi();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        OtherBean otherBean = new OtherBean();
        otherBean.setSerial_no(device.getSerialNumber());
        otherBean.setCollisionLevel(level);
        return networkTypeApi.getValue(otherBean);
    }

    public void postUploadErrorFile(final CrashError crashError) {

        ApiFactory.GetTokenApi getToken = ApiFactory.newInstance(
                CarServiceClient.getContext()).getToken();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        BaseBean bean = new BaseBean();
        bean.setSerial_no(device.getSerialNumber());
        Call<OtherBean> call = getToken.getValue(bean);
        call.enqueue(new Callback<OtherBean>() {
            @Override
            public void onResponse(
                    Call<OtherBean> call, Response<OtherBean> response) {
                LogUtils.d("---postUploadErrorFile---response=" + response.toString());
                if (response.code() == 200) {
                    String code = response.body().getCode();
                    String msg = response.body().getMsg();
                    final String outLink = response.body().getOutLink();
                    final String token = response.body().getUploadToken();
                    LogUtils.d("---postUploadErrorFile---code=" + code
                            + ",msg=" + msg + ",outLink=" + outLink + ",,,token=" + token);
                    if (code.equals(Config.App.RESULT_OK)) {
                        UploadWithKeyFiles uploadWithKeyFiles = new UploadWithKeyFiles();
                        uploadWithKeyFiles.uploadFile(CarServiceClient.getContext(),
                                crashError.getLog(), token, null,
                                new UploadWithKeyFiles.onUploadListener() {
                                    @Override
                                    public void onComplete(String key) {
                                        ApiFactory.UploadErrorApi uploadErrorApi =
                                                ApiFactory.newInstance(
                                                        CarServiceClient.getContext()).getUploadError();
                                        DeviceManagement device =
                                                DeviceManagement.newInstance(CarServiceClient.getContext());
                                        GPSBean lGPSBean = GpsPointManager.getInstance().getGPSBean();
                                        String[] values = {Config.System.UPLOAD_ERROR,
                                                device.getSerialNumber(),
                                                "1",
                                                lGPSBean.getGtype() + "",
                                                "1",
                                                lGPSBean.getLng() + "",
                                                "1",
                                                lGPSBean.getLat() + "",
                                                device.getProduct(),
                                                crashError.getType() + "",
                                                crashError.getPkg(),
                                                outLink + "/" + key,
                                                crashError.getTime()};
                                        String info = Utils.conversions(true, values);
                                        LogUtils.d("------postUploadErrorFile------info=" + info);
                                        RequestBody requestBody = RequestBody.create(null, info);
                                        Call<String> call = uploadErrorApi.getValue(requestBody);
                                        call.enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                if (response.code() == Config.App.RESULT_CODE) {
                                                    String result = response.body();
                                                    if (result.equals(Config.System.UPLOAD_ERROR_OK)) {
                                                        FileUtils.deleteFile(crashError.getLog());
                                                        CarServiceClient.getDaoInstant().getCrashErrorDao().delete(crashError);
                                                    }
                                                } else {
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {
                                                LogUtils.d("---postUploadErrorFile---onFailure---t=" + t.toString());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(String result) {
                                        LogUtils.d("---postUploadErrorFile---onFailure---t=" + result);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onFailure(Call<OtherBean> call, Throwable t) {
                t.printStackTrace();
                LogUtils.d("---postUploadErrorFile---onFailure=" + t.toString());
            }
        });
    }

    public synchronized void postUploadLogFile() {
        final String logFile = LogUtils.logFile();
        if (TextUtils.isEmpty(logFile)) {
            return;
        }
        ApiFactory.GetTokenApi getToken = ApiFactory.newInstance(
                CarServiceClient.getContext()).getToken();
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        BaseBean bean = new BaseBean();
        bean.setSerial_no(device.getSerialNumber());
        Call<OtherBean> call = getToken.getValue(bean);
        call.enqueue(new Callback<OtherBean>() {
            @Override
            public void onResponse(
                    Call<OtherBean> call, Response<OtherBean> response) {
                LogUtils.d("postUploadLogFile response code :" + response.code());
                if (response.code() == 200) {
                    String code = response.body().getCode();
                    String msg = response.body().getMsg();
                    final String outLink = response.body().getOutLink();
                    final String token = response.body().getUploadToken();
                    LogUtils.d("postUploadLogFile code::" + code);
                    if (code.equals(Config.App.RESULT_OK)) {
                        LogUtils.d("postUploadLogFile file:" + logFile);
                        LogUtils.setLogStatus(false);
                        UploadWithKeyFiles uploadWithKeyFiles = new UploadWithKeyFiles();
                        uploadWithKeyFiles.uploadFile(CarServiceClient.getContext(),
                                logFile, token, System.currentTimeMillis() + LogUtils.logFileName(),
                                new UploadWithKeyFiles.onUploadListener() {
                                    @Override
                                    public void onComplete(String key) {
                                        String file = outLink + "/" + key;
                                        ApiFactory.UploadErrorApi uploadErrorApi =
                                                ApiFactory.newInstance(
                                                        CarServiceClient.getContext()).getUploadError();
                                        DeviceManagement device =
                                                DeviceManagement.newInstance(CarServiceClient.getContext());
                                        GPSBean lGPSBean = GpsPointManager.getInstance().getGPSBean();
                                        String[] values = {Config.System.UPLOAD_ERROR,
                                                device.getSerialNumber(),
                                                "1",
                                                lGPSBean.getGtype() + "",
                                                "1",
                                                lGPSBean.getLng() + "",
                                                "1",
                                                lGPSBean.getLat() + "",
                                                device.getProduct(),
                                                "4",
                                                "",
                                                file,
                                                String.valueOf(System.currentTimeMillis() / 1000)};
                                        String info = Utils.conversions(true, values);
                                        LogUtils.d("------postUploadLogFile------info=" + info);
                                        RequestBody requestBody = RequestBody.create(null, info);
                                        Call<String> call = uploadErrorApi.getValue(requestBody);
                                        call.enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                if (response.code() == Config.App.RESULT_CODE) {
                                                    String result = response.body();
                                                    if (result.equals(Config.System.UPLOAD_ERROR_OK)) {
                                                        File ff = new File(logFile);
                                                        if (ff.exists()) {
                                                            ff.delete();
                                                        }
                                                        LogUtils.setLogStatus(true);
                                                        SPUtils.put(CarServiceClient.getContext(),
                                                                Config.LOG_STATUS, false);
                                                    }
                                                } else {
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {
                                                LogUtils.d("---postUploadLogFile---onFailure---t=" + t.toString());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(String result) {
                                        //ToastUtils.showShort(CarServiceClient.getContext(),"------上传七牛服务器失败------");
                                        LogUtils.setLogStatus(true);
                                        LogUtils.d("postUploadLogFile post onFailure result=" + result);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onFailure(Call<OtherBean> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    public interface  onRequestApi{
        void success();
        void onfail();
    }

}
