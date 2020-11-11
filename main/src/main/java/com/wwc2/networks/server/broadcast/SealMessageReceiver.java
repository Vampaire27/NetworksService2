package com.wwc2.networks.server.broadcast;

import android.content.Context;
import android.text.TextUtils;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.utils.LogUtils;

import io.rong.push.PushType;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

/**
 * 当消息无法送达的情况下，push预留通道
 */
public class SealMessageReceiver extends PushMessageReceiver {

    @Override
    public boolean onNotificationMessageArrived(
            Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {

        String content = pushNotificationMessage.getPushContent();
        LogUtils.d("RongIM","SealNotificationReceiver Content=" + content);
        if(!TextUtils.isEmpty(content)){
            MesManager.getInstance().setReceive(CarServiceClient.getContext(), content);
        }
        return true;
    }

    @Override
    public boolean onNotificationMessageClicked(
            Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {

        LogUtils.d("RongIM","SealNotificationReceiver   Clicked   pushNotificationMessage="
                + pushNotificationMessage.toString());

        return true;
    }
}
