package com.wwc2.networks.server.bean.heart;

import com.wwc2.networks.server.parse.ResultPaser;

public class MesJsonAck {
     private String serNo;
     private int cmdId;
     private String flowId;
     private ack data;

    public MesJsonAck(String serNo, int cmdId, String flowId, int code) {
        this.serNo = serNo;
        this.cmdId = cmdId;
        this.flowId = flowId;
        this.data = new ack(code);
    }

    public String getSerNo() {
        return serNo;
    }

    public void setSerNo(String serNo) {
        this.serNo = serNo;
    }

    public int getCmdId() {
        return cmdId;
    }

    public void setCmdId(int cmdId) {
        this.cmdId = cmdId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public ack getData() {
        return data;
    }

    public void setData(ack data) {
        this.data = data;
    }

     class ack{
        int code;

        public ack(int code) {
            this.code = code;
        }
    }

    @Override
    public String toString() {
        String parsJson= ResultPaser.JsonParse(this);
        return parsJson;
    }
}
