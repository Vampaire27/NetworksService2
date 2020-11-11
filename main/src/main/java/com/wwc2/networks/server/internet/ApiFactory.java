package com.wwc2.networks.server.internet;

import android.content.Context;

import com.wwc2.networks.server.bean.BaseBean;
import com.wwc2.networks.server.bean.ConfigBean;
import com.wwc2.networks.server.bean.DeviceBean;
import com.wwc2.networks.server.bean.JsimBean;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.bean.TimeBean;
import com.wwc2.networks.server.bean.ViewBean;
import com.wwc2.networks.server.bean.ZWResponseBean;
import com.wwc2.networks.server.bean.heart.MesJsonAck;
import com.wwc2.networks.server.utils.Config;

import io.reactivex.Flowable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class ApiFactory {

    private static Context context = null;
    private static ApiFactory apiFactory = null;

    private ApiFactory() {
    }

    public static ApiFactory newInstance(Context con) {
        if (apiFactory == null) {
            apiFactory = new ApiFactory();
        }
        context = con;
        return apiFactory;
    }

    private RegisterApi registerApi;

    public interface RegisterApi {
        @POST(Config.App.REGISTER)
        Call<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public RegisterApi getRegister() {
        registerApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(RegisterApi.class);
        return registerApi;
    }

    private ConfigApi configApi;

    public interface ConfigApi {
        @POST(Config.App.SYS_CONFIG)
        Call<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public ConfigApi getConfig(String url) {
        configApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(url)
                .create(ConfigApi.class);
        return configApi;
    }

    private SysTimeApi sysTimeApi;

    public interface SysTimeApi {
        @POST(Config.App.SYS_TIME)
        Call<OtherBean> getValue();
    }

    public SysTimeApi getSysTime() {
        sysTimeApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(SysTimeApi.class);
        return sysTimeApi;
    }

    private McuUrlApi mcuUrlApi;

    public interface McuUrlApi {
        @POST(Config.App.MCU_URL)
        Call<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public McuUrlApi getMcuUrl() {
        mcuUrlApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(McuUrlApi.class);
        return mcuUrlApi;
    }

    private CanMcuUrlApi canMcuUrlApi;

    public interface CanMcuUrlApi {
        @POST(Config.App.CAN_MCU_URL)
        Call<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public CanMcuUrlApi getCanMcuUrl() {
        canMcuUrlApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(CanMcuUrlApi.class);
        return canMcuUrlApi;
    }

    private APNUrlApi apnUrlApi;

    public interface APNUrlApi {
        @POST(Config.App.APN_URL)
        Call<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public APNUrlApi getAPNUrl() {
        apnUrlApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(APNUrlApi.class);
        return apnUrlApi;
    }

    private MallRequiredApi mallRequiredApi;

    public interface MallRequiredApi {
        @POST(Config.App.MALL_REQUIRED)
        Call<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public MallRequiredApi getMallRequired() {
        mallRequiredApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(MallRequiredApi.class);
        return mallRequiredApi;
    }

    private LbsInfoApi lbsInfoApi;

    public interface LbsInfoApi {
        @POST(Config.Weather.LBS_URL)
        Flowable<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public LbsInfoApi getLbsInfo() {
        lbsInfoApi = Retrofit2Utils.newInstance(context).getWeatherRetrofit(null)
                .create(LbsInfoApi.class);
        return lbsInfoApi;
    }

    private UploadSystemApi uploadSystemApi;

    public interface UploadSystemApi {
        @POST(Config.System.SYSTEM_URL)
        Flowable<String> getValue(@Body RequestBody bean);
    }

    public UploadSystemApi getUploadSystem() {
        uploadSystemApi = Retrofit2Utils.newInstance(context).getProtocolRetrofit(null)
                .create(UploadSystemApi.class);
        return uploadSystemApi;
    }

    private UploadStatisticsApi uploadStatisticsApi;

    public interface UploadStatisticsApi {
        @POST(Config.System.SYSTEM_URL)
        Flowable<String> getValue(@Body RequestBody bean);
    }

    public UploadStatisticsApi getUploadStatistics() {
        uploadStatisticsApi = Retrofit2Utils.newInstance(context).getProtocolRetrofit(null)
                .create(UploadStatisticsApi.class);
        return uploadStatisticsApi;
    }

    private GetTokenApi getTokenApi;

    public interface GetTokenApi {
        @POST(Config.App.GET_TOKEN)
        Call<OtherBean> getValue(@Body BaseBean bean);
    }

    public GetTokenApi getToken() {
        getTokenApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(GetTokenApi.class);
        return getTokenApi;
    }

    private RemoteTimeApi remoteTimeApi;

    public interface RemoteTimeApi {
        @POST(Config.System.REMOTE_TIME_URL)
        Call<TimeBean> getTime();
    }

    public RemoteTimeApi getTime() {
        remoteTimeApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(RemoteTimeApi.class);
        return remoteTimeApi;
    }

    private RemoteConfigApi remoteConfigApi;

    public interface RemoteConfigApi {
        @POST(Config.System.REMOTE_CARSET_URL)
        Call<ConfigBean> getRemoteConfig(@Body ConfigBean.RequestBean bean);
    }

    public RemoteConfigApi getRemoteConfig() {
        remoteConfigApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(RemoteConfigApi.class);
        return remoteConfigApi;
    }

    private UploadViewApi uploadViewApi;

    public interface UploadViewApi {
        @POST(Config.App.UPLOAD_VIEW)
        Flowable<OtherBean> getValue(@Body ViewBean viewBean);
    }

    public UploadViewApi getUploadView() {
        uploadViewApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(UploadViewApi.class);
        return uploadViewApi;
    }

    private UploadErrorApi uploadErrorApi;

    public interface UploadErrorApi {
        @POST(Config.System.SYSTEM_URL)
        Call<String> getValue(@Body RequestBody bean);
    }

    public UploadErrorApi getUploadError() {
        uploadErrorApi = Retrofit2Utils.newInstance(context).getProtocolRetrofit(null)
                .create(UploadErrorApi.class);
        return uploadErrorApi;
    }

    private SimVerifyApi simVerifyApi;

    public interface SimVerifyApi {
        @POST(Config.App.SIM_VERIFY)
        Flowable<DeviceBean> getValue(@Body DeviceBean deviceBean);
    }

    public SimVerifyApi checkSimVerify() {
        simVerifyApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(SimVerifyApi.class);
        return simVerifyApi;
    }

    private GetAdvertisingApi getAdvertisingApi;

    public interface GetAdvertisingApi {
        @POST(Config.App.SYS_ADVER)
        Flowable<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public GetAdvertisingApi getAdvertising() {
        getAdvertisingApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(GetAdvertisingApi.class);
        return getAdvertisingApi;
    }

    private JiaShenInfoApi jiaShenInfoApi;

    public interface JiaShenInfoApi {
        @GET(Config.JS_QUERYCAR)
        Flowable<JsimBean> queryCard(
                @Query("appid") String appid,
                @Query("sign") String sign,
                @Query("card_code") String card_code,
                @Query("timeline") String timeline);
    }

    public JiaShenInfoApi getJiaShenInfoApi() {
        jiaShenInfoApi = Retrofit2Utils.newInstance(context).getJiaShenRetrofit()
                .create(JiaShenInfoApi.class);
        return jiaShenInfoApi;
    }

    private JiaShenCheckApi jiaShenCheckApi;

    public interface JiaShenCheckApi {
        @GET(Config.JS_ISSET)
        Flowable<OtherBean> checkSim(
                @Query("card_code") String card_code);
    }

    public JiaShenCheckApi getJiaShenCheckApi() {
        jiaShenCheckApi = Retrofit2Utils.newInstance(context).getJiaShenRetrofit()
                .create(JiaShenCheckApi.class);
        return jiaShenCheckApi;
    }

    private JsQueryRealnameApi jsQueryRealnameApi;

    public interface JsQueryRealnameApi {
        @GET(Config.JS_QUERY_REALNAME)
        Call<JsimBean> queryRealname(
                @Query("appid") String appid,
                @Query("sign") String sign,
                @Query("card_code") String card_code,
                @Query("timeline") String timeline);
    }

    public JsQueryRealnameApi getJsQueryRealnameApi() {
        jsQueryRealnameApi = Retrofit2Utils.newInstance(context).getJiaShenRetrofit()
                .create(JsQueryRealnameApi.class);
        return jsQueryRealnameApi;
    }

    private ZhiWangQueryApi zhiWangQueryApi;

    public interface ZhiWangQueryApi {
        @GET(Config.ZW_ISSET)
        Flowable<ZWResponseBean> querySim(
                @Query("agentid") String agentid,
                @Query("stamp") String stamp,
                @Query("nonce") String nonce,
                @Query("iccid") String iccid);
    }

    public ZhiWangQueryApi getZWQueryApi() {
        zhiWangQueryApi = Retrofit2Utils.newInstance(context).getZWRetrofit()
                .create(ZhiWangQueryApi.class);
        return zhiWangQueryApi;
    }

    private FeedBackApi feedBackApi;

    public interface FeedBackApi {
        @POST(Config.App.SYS_FEEDBACK)
        Flowable<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public FeedBackApi setFeedBackApi() {
        feedBackApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(FeedBackApi.class);
        return feedBackApi;
    }


    public interface FeedBackYRApi {
        @POST(Config.App.SYS_FEEDBACK_yr)
        Flowable<MesJsonAck> getValue(@Body MesJsonAck value);
    }

    public FeedBackYRApi getFeedBackRYApi() {
         FeedBackYRApi feedBackApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(FeedBackYRApi.class);
        return feedBackApi;
    }

    private ActivateDeviceApi activateDeviceApi;

    public interface ActivateDeviceApi {
        @POST(Config.App.SYS_ACTIVATEDEVICE)
        Flowable<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public ActivateDeviceApi postActivateDeviceApi() {
        activateDeviceApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(ActivateDeviceApi.class);
        return activateDeviceApi;
    }

    private GetRY_TokenApi getRY_TokenApi;

    public interface GetRY_TokenApi {
        @POST(Config.App.GET_RY_TOKEN)
        Flowable<OtherBean> getValue(@Body BaseBean bean);
    }

    public GetRY_TokenApi getRY_Token() {
        getRY_TokenApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(GetRY_TokenApi.class);
        return getRY_TokenApi;
    }

    private NetworkTypeApi networkTypeApi;

    public interface NetworkTypeApi {
        @POST(Config.App.SYS_NETWORK)
        Flowable<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public NetworkTypeApi postNetworkTypeApi() {
        networkTypeApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(NetworkTypeApi.class);
        return networkTypeApi;
    }

    private SensorApi sensorApi;

    public interface SensorApi {
        @POST(Config.App.SYS_SENSOR)
        Flowable<OtherBean> getValue(@Body OtherBean otherBean);
    }

    public SensorApi postSensorApi() {
        sensorApi = Retrofit2Utils.newInstance(context).getInterfaceRetrofit(null)
                .create(SensorApi.class);
        return sensorApi;
    }
}
