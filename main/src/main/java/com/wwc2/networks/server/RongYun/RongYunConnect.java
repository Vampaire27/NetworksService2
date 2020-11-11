package com.wwc2.networks.server.RongYun;

import android.os.Handler;
import android.text.TextUtils;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.OtherBean;
import com.wwc2.networks.server.broadcast.MesManager;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.netty.CommonHeartManager;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.NetworkUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

public class RongYunConnect implements RongIMClient.OnReceiveMessageListener{

    private String ryValue;
    private Handler mHandler;
    private String mToken = null;

    private boolean mConnectStatus = false;
    private Runnable RYrun = new Runnable() {
        @Override
        public void run() {
            LogUtils.d("--------RYrun = " + Thread.currentThread());
            MesManager.getInstance().setReceive(CarServiceClient.getContext(), ryValue);
        }
    };

    public RongYunConnect(Handler mHandler) {
        this.mHandler = mHandler;
    }

     public void init(){
         RongIMClient.init(CarServiceClient.getContext(),"m7ua80gbmo56m",false);
         RongIMClient.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
             @Override
             public void onChanged(ConnectionStatus connectionStatus) {
                 LogUtils.d(" hzy RongIMClient init...ConnectionStatus...value=" + connectionStatus.getValue()
                         + ",,name=" + connectionStatus.getMessage());

                 int value = connectionStatus.getValue();
                 if(value == ConnectionStatus.NETWORK_UNAVAILABLE.getValue() ||
                         value == ConnectionStatus.UNCONNECTED.getValue()){
                     mConnectStatus = false;
                 }else if (value == ConnectionStatus.CONNECTED.getValue()){
                     mConnectStatus = true;
                     RongIMClient.getInstance().sendPing();
                 }
             }
         });

         RongIMClient.getInstance().setOnReceiveMessageListener(this);
         RongIMClient.getInstance().cancelSDKHeartBeat();
     }

    public void getRYtokenAndConnect(){
        LogUtils.d("Network postRYtoKen ing...");
        if(getConnectStatus()){
            LogUtils.d("  RY mConnectStatus. is true ");
        }else if(mToken == null ) {
            NetWorkManagement.newInstance().postRYtoKen("2")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<OtherBean>() {
                        @Override
                        public void accept(OtherBean otherBean) throws Exception {
                            LogUtils.d("postRYtoKen _ otherBean :" + otherBean.toString());
                            if (otherBean.getCode().equals(Config.App.RESULT_OK)) {
                                String token = otherBean.getToken();
                                LogUtils.d("postRYtoKen  token=" + token);
                                if (!TextUtils.isEmpty(token)) {
                                    setToken(token);
                                    RongIMClient.connect(token, connectCallback);
                                } else {
                                    LogUtils.d("postRYtoKen  token 异常！");
                                }
                            } else {
                                LogUtils.d("postRYtoKen 获取token失败！");
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            LogUtils.d("postRYtoKen _ throwable :" + throwable.toString());
                            NetworkUtil.showNetworkLog();
                        }
                    });
        }else {
            if (!TextUtils.isEmpty(mToken)) {
                RongIMClient.connect(mToken, connectCallback);
            } else {
                LogUtils.d("postRYtoKen  token 异常！");
            }
        }

    }

    @Override
    public boolean onReceived(io.rong.imlib.model.Message message, int i) {
        LogUtils.d("--------onReceived = " + Thread.currentThread());


        if(!CarServiceClient.getDvrEnable()){
            if(CarServiceClient.getAccStatus() == Config.System.ACC_OFF){
                LogUtils.d("--------dvrEnable=false! 禁止休眠唤醒..");
                return false;
            }
        }

        if(message != null) {
            MessageContent content = message.getContent();
            LogUtils.d("----------content=" + content);
            if (content instanceof TextMessage) {
                final String values = ((TextMessage) content).getContent();
                LogUtils.d("----------values=" + values);
                if(!TextUtils.isEmpty(values)){
                    ryValue = values;
                    if(ryValue.contains("navigation") || ryValue.contains("meet")){
                        mHandler.post(RYrun);
                    }else {
                        MesManager.getInstance().setReceive(CarServiceClient.getContext(), ryValue);
                    }
                }
            }else{
                LogUtils.d("---------数据类型错误....");
            }
        }
        return false;
    }



    private RongIMClient.ConnectCallback connectCallback = new RongIMClient.ConnectCallback(){

        @Override
        public void onSuccess(String userid) {
            LogUtils.d("connectCallback onSuccess userid=" + userid);
            mConnectStatus = true;
            RongIMClient.getInstance().cancelSDKHeartBeat();
            CommonHeartManager.getInstance().startHeart();
        }


        @Override
        public void onError(RongIMClient.ConnectionErrorCode connectionErrorCode) {
            LogUtils.d("connectCallback onError errorCode=" + connectionErrorCode);
            if(RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT ==  connectionErrorCode){

                NetWorkManagement.newInstance().postRYtoKen("1")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<OtherBean>() {
                            @Override
                            public void accept(OtherBean otherBean) throws Exception {
                                LogUtils.d("2 postRYtoKen _ otherBean :" + otherBean.toString());
                                if (otherBean.getCode().equals(Config.App.RESULT_OK)) {

                                    String token = otherBean.getToken();
                                    if(!TextUtils.isEmpty(token)){
                                        setToken(token);
                                        RongIMClient.connect(token, connectCallback);
                                    }else{
                                        LogUtils.d("2 postRYtoKen  token 异常！");
                                    }

                                }else{

                                    LogUtils.d("2 postRYtoKen 获取token失败！");

                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                LogUtils.d("2 postRYtoKen _ throwable :" + throwable.toString());
                                NetworkUtil.showNetworkLog();
                            }
                        });

            }else {
                mConnectStatus = false;
            }
        }

        @Override
        public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
            LogUtils.d("onDatabaseOpened ...");
        }
    };

    public boolean getConnectStatus(){
        return mConnectStatus;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }
}
