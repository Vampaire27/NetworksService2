package com.wwc2.networks.server.canbus.Candata;

public class BaseCanBean {
    private String cmd;//指令类型　０００２
    private DataCan data;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }


    public DataCan getData() {
        return data;
    }

    public void setData(DataCan data) {
        this.data = data;
    }
}
