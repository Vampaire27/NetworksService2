package com.wwc2.networks.server.bean;

public class BaseBean implements Cloneable{

    private String serNo;
    private long time;
    private String registrationId;
    private String proVersion;
    private String config;
    private String code;
    private String msg;
    private String status;
    private String logType;
    private String configVersion;
    /**
     * 1代表重新从融云生成token 2代表不必重新生成主要处理token过期或者无效情况
     */
    private String rebuild;
    private int networkTime;
    private String networkType;

    private String imgStr; //图片base64

    public String getSerNo() {
        return serNo;
    }

    public void setSerNo(String serNo) {
        this.serNo = serNo;
    }

    public String getImgStr() {
        return imgStr;
    }

    public void setImgStr(String imgStr) {
        this.imgStr = imgStr;
    }

    public String getRebuild() {
        return rebuild;
    }

    public void setRebuild(String rebuild) {
        this.rebuild = rebuild;
    }

    public String getSerial_no() {
        return serNo;
    }

    public void setSerial_no(String serNo) {
        this.serNo = serNo;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProVersion() {
        return proVersion;
    }

    public void setProVersion(String proVersion) {
        this.proVersion = proVersion;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public int getNetworkTime() {
        return networkTime;
    }

    public void setNetworkTime(int networkTime) {
        this.networkTime = networkTime;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
