package com.wwc2.networks.server.broadcast;

import android.content.Context;

import com.wwc2.networks.cmd.CmdManager;
import com.wwc2.networks.cmd.DeleteCmd;
import com.wwc2.networks.server.bean.heart.MesHeader;
import com.wwc2.networks.server.utils.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MesJsonExt {
    private String TAG = "MesManager";
   // private ArrayList<DeleteVideoCallBackImp > mDeleteVideoCallBacks = new ArrayList<>();

    MesHeader getMesHeader(JSONObject jsonObject ){
        MesHeader mesHeader = new MesHeader();
        try {
            mesHeader.setCmdId(jsonObject.getInt("cmdId"));
            mesHeader.setFlowId(jsonObject.getString("flowId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mesHeader;
    }

    public void askAck(MesHeader mesHeader){
        JsonFeedBack jFeedback= new JsonFeedBack();
        jFeedback.setMesHeader(mesHeader);
        jFeedback.setCode(jFeedback.ACK_REC);
        jFeedback.postACK();
    }

    public boolean setReceive(String values){
        boolean ret = true;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(values);
        } catch (JSONException e) {
            LogUtils.d(TAG,"this cmd not json Type");
            return  false;
        }

        MesHeader header = getMesHeader(jsonObject);
        switch (header.getCmdId()){
            case MesManager.DELETE_CMD:
                LogUtils.d(TAG," DELETE_CMD =" + values);
                askAck(header);
                CmdManager.getInstans().postCmd(new DeleteCmd(jsonObject,header));
                break;
            default:
                ret =false;
                LogUtils.d(TAG,"this cmd not json Type");
                break;
        }
        return ret;
    }
}
