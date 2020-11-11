package com.wwc2.networks.cmd;

import com.google.gson.Gson;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.bean.RongyunDataBean;
import com.wwc2.networks.server.utils.LogUtils;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public abstract class SendMesCmd extends BaseCmd{

    public SendMesCmd(int cmdType, RemotelyBean mRemotelyBean) {
        super(cmdType, mRemotelyBean);
    }

    public void sendMesToApp(int type,String code,String msg){
        if(mRemotelyBean != null){
            LogUtils.d("sendMes ... ");
            RongyunDataBean mRequestMsg  = new RongyunDataBean();
            RongyunDataBean.RongData myData = new RongyunDataBean.RongData();
            mRequestMsg.setType(type);
            myData.setCode(code);
            myData.setMsg(msg);
            mRequestMsg.setData(myData);

            Gson gson = new Gson();
            LogUtils.d("sendMes ... requestJson--->" + gson.toJson(mRequestMsg));

            // 构建文本消息实例
            TextMessage textMessage = TextMessage.obtain( gson.toJson(mRequestMsg));
            /* 生成 Message 对象。
             */
            String values = mRemotelyBean.getValues();
            String userId = values.substring(values.lastIndexOf(",")+1);
            LogUtils.d("...sendMes...userId=" + userId);
            Message myMessage = Message.obtain(userId, Conversation.ConversationType.PRIVATE, textMessage);
            RongIMClient.getInstance().sendMessage(myMessage, null, null, new IRongCallback.ISendMessageCallback() {
                @Override
                public void onAttached(Message message) {
                    LogUtils.d("message--onAttached---->" + message.getContent().toString());
                }

                @Override
                public void onSuccess(Message message) {
                    LogUtils.d("message--onSuccess---->" + message.getContent().toString());

                }

                @Override
                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                    LogUtils.d("message--onError---->" + message.getContent().toString());
                }
            });
            return;
        }
    }
}
