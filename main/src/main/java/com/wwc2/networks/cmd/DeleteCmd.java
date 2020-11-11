package com.wwc2.networks.cmd;

import com.wwc2.networks.server.bean.heart.MesHeader;
import com.wwc2.networks.server.broadcast.JsonFeedBack;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.dvr.jni.DeleteVideoCallBackImp;
import com.wwc2.networks.server.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class DeleteCmd extends  BaseCmd{

    JSONObject mJSONObject;
    MesHeader mMesHeader;

    public DeleteCmd(JSONObject jsonObject , MesHeader mesHeader) {
        super(0, null,true);
        mJSONObject = jsonObject;
        mMesHeader = mesHeader;
    }

    public void deleteCmd(JSONObject jsonObject ,MesHeader mesHeader){

        String path = null;
        try {
            path = jsonObject.getJSONObject("data").getString("fileUrls");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DeleteVideoCallBackImp mDeleteVideoCallBack = new DeleteVideoCallBackImp();
        JsonFeedBack jFeedback= new JsonFeedBack();
        jFeedback.setMesHeader(mesHeader);
        mDeleteVideoCallBack.setJsonFeedBack(jFeedback);

        //mDeleteVideoCallBacks.add(mDeleteVideoCallBack);
        DvrManagement.newInstance().deleteFile(path,mDeleteVideoCallBack);
    }

    @Override
    public void exec() {
        deleteCmd(mJSONObject,mMesHeader);
        nextCmd();
    }

    @Override
    public boolean isSingleCmd() {
        return false;
    }
}
