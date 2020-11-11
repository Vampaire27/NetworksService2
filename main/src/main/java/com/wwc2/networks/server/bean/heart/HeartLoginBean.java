package com.wwc2.networks.server.bean.heart;

public class HeartLoginBean {
    private String deviceId;
    private String cmd;
    private String version;
    private long time;

    public HeartLoginBean() {

    }

    public HeartLoginBean(String deviceId, String cmd, long time, String version) {
        this.deviceId = deviceId;
        this.cmd = cmd;
        this.time = time;
        this.version = version;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
