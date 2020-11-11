package com.wwc2.networks.server.bean.heart;

public class MesHeader {
     int cmdId;
     String flowId;

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

    @Override
    public String toString() {
        return super.toString();
    }
}
