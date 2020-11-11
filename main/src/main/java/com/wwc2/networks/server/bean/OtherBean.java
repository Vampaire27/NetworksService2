package com.wwc2.networks.server.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OtherBean extends AMapBean {

    private String url;
    private String outLink;
    private String uploadToken;
    private List<DataBean> data;
    private String lbsData;
    private String sign;
    private int type;
    private String iccid;
    public String error_code;
    public String error_msg;
    //1内置卡，2外置卡
    private String simMode;
    private String title;
    private String packageName;
    private String city;
    private int minute;
    private String command;
    //加密协议
    private String deviceId;
    private String agentName;
    private String deviceSign;
    private String reqData;
    private String reqEncryptkey;
    //融云token
    private String token;
    private String collisionLevel;

    @Override
    public String toString() {
        return "OtherBean{" +
                "url='" + url + '\'' +
                ", outLink='" + outLink + '\'' +
                ", uploadToken='" + uploadToken + '\'' +
                ", data=" + data +
                ", lbsData='" + lbsData + '\'' +
                ", sign='" + sign + '\'' +
                ", type=" + type +
                ", iccid='" + iccid + '\'' +
                ", error_code='" + error_code + '\'' +
                ", error_msg='" + error_msg + '\'' +
                ", simMode='" + simMode + '\'' +
                ", title='" + title + '\'' +
                ", packageName='" + packageName + '\'' +
                ", city='" + city + '\'' +
                ", minute=" + minute +
                ", command='" + command + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", deviceSign='" + deviceSign + '\'' +
                ", reqData='" + reqData + '\'' +
                ", reqEncryptkey='" + reqEncryptkey + '\'' +
                ", token='" + token + '\'' +
                ", collisionLevel='" + collisionLevel + '\'' +
                ", code='" + getCode() + '\'' +
                ", msg='" + getMsg() + '\'' +
                '}';
    }

    public String getCollisionLevel() {
        return collisionLevel;
    }

    public void setCollisionLevel(String collisionLevel) {
        this.collisionLevel = collisionLevel;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOutLink() {
        return outLink;
    }

    public void setOutLink(String outLink) {
        this.outLink = outLink;
    }

    public String getUploadToken() {
        return uploadToken;
    }

    public void setUploadToken(String uploadToken) {
        this.uploadToken = uploadToken;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public String getLbsData() {
        return lbsData;
    }

    public void setLbsData(String lbsData) {
        this.lbsData = lbsData;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getSimMode() {
        return simMode;
    }

    public void setSimMode(String simMode) {
        this.simMode = simMode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getDeviceSign() {
        return deviceSign;
    }

    public void setDeviceSign(String deviceSign) {
        this.deviceSign = deviceSign;
    }

    public String getReqData() {
        return reqData;
    }

    public void setReqData(String reqData) {
        this.reqData = reqData;
    }

    public String getReqEncryptkey() {
        return reqEncryptkey;
    }

    public void setReqEncryptkey(String reqEncryptkey) {
        this.reqEncryptkey = reqEncryptkey;
    }

    public static class DataBean {
        private String name;
        private String packageName;
        @SerializedName("url")
        private String app_url;
        private String version;
        private String versionCode;
        private int isForce;

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        public int getIsForce() {
            return isForce;
        }

        public void setIsForce(int isForce) {
            this.isForce = isForce;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getAppUrl() {
            return app_url;
        }

        public void setAppUrl(String app_url) {
            this.app_url = app_url;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
