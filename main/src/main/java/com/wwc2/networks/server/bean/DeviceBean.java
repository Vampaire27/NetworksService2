package com.wwc2.networks.server.bean;

public class DeviceBean extends BaseBean {

    private String os_version;
    private String app_version;
    private String bt_version;
    private String mcu_version;
    private String imei_info;
    private String mac_info;
    private String apkVersion;

    public String getOs_version() {
        return os_version;
    }

    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getBt_version() {
        return bt_version;
    }

    public void setBt_version(String bt_version) {
        this.bt_version = bt_version;
    }

    public String getMcu_version() {
        return mcu_version;
    }

    public void setMcu_version(String mcu_version) {
        this.mcu_version = mcu_version;
    }

    public String getImei_info() {
        return imei_info;
    }

    public void setImei_info(String imei_info) {
        this.imei_info = imei_info;
    }

    public String getMac_info() {
        return mac_info;
    }

    public void setMac_info(String mac_info) {
        this.mac_info = mac_info;
    }

    public String getApkVersion() {
        return apkVersion;
    }

    public void setApkVersion(String apkVersion) {
        this.apkVersion = apkVersion;
    }
}
