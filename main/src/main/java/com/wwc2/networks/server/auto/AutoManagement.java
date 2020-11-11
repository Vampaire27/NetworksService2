package com.wwc2.networks.server.auto;

import android.content.Context;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.AMapBean;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutoManagement implements IAutoData {

    private final String TAG = "AutoManagement";
    private static Context mContext = null;
    private static AutoManagement autoManagement = null;
    public static UploadLevel mUploadLevel = new UploadLevel() ;
    private AutoManagement() { }

    public static AutoManagement newInstance(Context con) {
        if (autoManagement == null) {
            autoManagement = new AutoManagement();
        }
        mContext = con;
        return autoManagement;
    }

    @Override
    public void registerCar(final String jPushID, final AutoCallback autoCallback) {
        Call<OtherBean> call = NetWorkManagement.newInstance().postRegister(jPushID);
        call.enqueue(new Callback<OtherBean>() {
            @Override
            public void onResponse(Call<OtherBean> call, Response<OtherBean> response) {
                LogUtils.d("response.code:" + response.code());
                if (response.code() == Config.App.RESULT_CODE) {
                    try {
                        OtherBean otherBean = response.body();
                        String code = otherBean.getCode();
                        String msg = otherBean.getMsg();
                        String status = otherBean.getStatus();
                        String logType = otherBean.getLogType();
                        String config = otherBean.getConfigVersion();
                        int time = otherBean.getNetworkTime();

                        String sensor = otherBean.getCollisionLevel();
                        LogUtils.d("---------------1 code:---sensor=" + sensor);

                        //zhongyang_gsensor add sync for remote server to dvr 2020.0506
                       // SPUtils.put(CarServiceClient.getContext(), Config.SENSOR_LEVEL, sensor);
//                        String test = (String) SPUtils.get(CarServiceClient.getContext(),
//                                Config.SENSOR_LEVEL, "");


                        LogUtils.d("---------------1 code:" + code + ",status:" + status + ",msg=" + msg
                                + ",logType=" + logType + ",configVer=" + config + ",time=" + time + ",sensor=" + sensor);

                        mUploadLevel.setInfoLevel(Integer.valueOf(status));

                        if (code.equals(Config.App.RESULT_OK)) {
                            if(autoCallback != null)
                                autoCallback.onSuccess(otherBean);
                        } else {
                            if(autoCallback != null)
                                autoCallback.onFailure();
                        }
                    }catch (Exception e){
                        if(autoCallback != null)
                            autoCallback.onFailure();
                        LogUtils.e(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<OtherBean> call, Throwable t) {
                if(autoCallback != null)
                    autoCallback.onFailure();
                LogUtils.e(t);
            }
        });
    }

    @Override
    public AMapBean getLocation() {
        AMapBean aMapBean = new AMapBean();
        return aMapBean;
    }

    public interface AutoCallback {
        void onSuccess(OtherBean otherBean);

        void onFailure();
    }
}
