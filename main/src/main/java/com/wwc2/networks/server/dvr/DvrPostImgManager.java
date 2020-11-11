package com.wwc2.networks.server.dvr;

import android.util.Base64;

import com.google.gson.Gson;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.internet.ApiFactory;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.NetworkUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * description ： 请求微信公众号上传图片
 * user: wangpeng on 2019/9/9.
 * emai: wpeng@waterworld.com.cn
 */

public class DvrPostImgManager {


    private  static DvrPostImgManager mPostImgFeedBack;

    public static DvrPostImgManager getInstans(){
        if (mPostImgFeedBack ==null){
            mPostImgFeedBack = new DvrPostImgManager();
        }
        return mPostImgFeedBack;
    }

    private DvrPostImgManager(){

    }


        public void postImgFeedBack(String jPush,
                                    boolean result, String msg,
                                    final String imgpath, final DvrPostImgManager.onRequestApi mOnRequestApi) {

            String code = result == true ? "0" : "1";
            LogUtils.d("WPTAGSSS","wangpeng postFeedBack "+"---jPush=" + jPush +"---code=" + code + ",msg=" + msg);
            ApiFactory.FeedBackApi feedBackApi = ApiFactory.newInstance(CarServiceClient.getContext())
                    .setFeedBackApi();
            LogUtils.d("WPTAGSSS","1111111111111111");

            DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
            LogUtils.d("WPTAGSSS","222222222222222" + device);

            OtherBean otherBean = new OtherBean();
            otherBean.setSerial_no(device.getSerialNumber());
            otherBean.setCommand(jPush);
            //0=成功，1=异常
            otherBean.setCode(code);
            otherBean.setMsg(msg);

            if (imgpath != null && !imgpath.equals("")){
                otherBean.setImgStr(getImageStr(imgpath));
            }

            //test add 时间
            otherBean.setTime(System.currentTimeMillis() / 1000);

            //请求
            feedBackApi.getValue(otherBean)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<OtherBean>() {
                        @Override
                        public void accept(OtherBean otherBean) throws Exception {
                            Gson gson1 = new Gson();
                            String string = gson1.toJson(otherBean);

                            LogUtils.d("WPTAGSSS", "string --------------->" + string);

                            if(mOnRequestApi != null) {
                                mOnRequestApi.success();
                            }

                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if(mOnRequestApi != null) {
                                mOnRequestApi.onfail();
                            }

                            LogUtils.d("WPTAGSSS", "postFeedBack ---throwable!" + throwable.toString());
                            NetworkUtil.showNetworkLog();
                        }
                    });

        }



    public interface  onRequestApi{
        void success();
        void onfail();
    }




    /**
     * @Description: 根据图片地址转换为base64编码字符串
     * @Author:
     * @CreateTime:
     * @return
     */
    public static String getImageStr(String imgFile) {
        InputStream inputStream = null;
        // 加密
        String imgBase64 = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();

            imgBase64 = Base64.encodeToString(data,Base64.DEFAULT);
        } catch (IOException e) {
            LogUtils.d("getImageStr e=" + e.toString());
//            e.printStackTrace();
        }

//        LogUtils.d("WPTAG","imgBase64------>" + imgBase64);
        return "data:image/jpeg;base64," + imgBase64;
    }

    public boolean isFourPlatform(){
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        String platformType  = device.getPlatformType();
        LogUtils.d("isFour Platform  platformType:" +platformType);
        if(platformType.equals("four_stream") ||
                platformType.equals("quart_stream") ||
                platformType.equals(Config.TYPE_360)){
            return  true;
        }else {
            return  false;
        }
    }

    public boolean isTwoPlatform(){
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        String platformType  = device.getPlatformType();
        LogUtils.d("isTwo Platform  platformType:" +platformType);
        if (platformType.equals("two_stream") ||
                platformType.equals("dual_stream")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnePlatform(){
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        String platformType  = device.getPlatformType();
        LogUtils.d("isTwo Platform  platformType:" +platformType);
        if (platformType.equals("one_stream")) {
            return true;
        } else {
            return false;
        }
    }
}
