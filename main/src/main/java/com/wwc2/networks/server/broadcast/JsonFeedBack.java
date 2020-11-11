package com.wwc2.networks.server.broadcast;

import com.wwc2.networks.server.bean.heart.MesHeader;
import com.wwc2.networks.server.internet.NetWorkManagement;

public class JsonFeedBack {

    private MesHeader mMesHeader;
    private int code; //0:success 1 :fail  2:rec mes

    public static int ACK_SUCCESS = 0;
    public static int ACK_FAIL = 1;
    public static int ACK_REC = 2;


    public JsonFeedBack() {
    }


    public void setCode(int code) {
        this.code = code;
    }

    public void setMesHeader(MesHeader mMesHeader) {
        this.mMesHeader = mMesHeader;
    }

    public void postACK(){
        NetWorkManagement.newInstance().postFeedBackJson(mMesHeader.getCmdId(),mMesHeader.getFlowId(),code);
    }
}
