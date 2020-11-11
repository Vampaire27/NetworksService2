package com.wwc2.networks.server.dvr.jni;

import android.os.RemoteException;

import com.wwc2.dvr.DeleteVideoCallBack;
import com.wwc2.networks.server.broadcast.JsonFeedBack;


public class DeleteVideoCallBackImp extends DeleteVideoCallBack.Stub {
    JsonFeedBack mJsonFeedBack;

    public void setJsonFeedBack(JsonFeedBack mJsonFeedBack) {
        this.mJsonFeedBack = mJsonFeedBack;
    }

    @Override
    public void deleteAction(String filepath, boolean result) throws RemoteException {
        if (result){
           mJsonFeedBack.setCode(mJsonFeedBack.ACK_SUCCESS);
        }else{
            mJsonFeedBack.setCode(mJsonFeedBack.ACK_FAIL);
        }
        mJsonFeedBack.postACK();
    }
}
