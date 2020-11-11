package com.wwc2.networks.server.provider;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Device {

    @Id(autoincrement = true)
    private Long id;
    private String os_version;
    private String app_version;
    private String bt_version;
    private String mcu_version;
    private String imei_info;
    private String mac_info;
    private String serial_number;
    private String product;
    private String canbus_name;
    private String ui_style;
    private String ui_show_switch;
    private String apk_version;

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", os_version='" + os_version + '\'' +
                ", app_version='" + app_version + '\'' +
                ", bt_version='" + bt_version + '\'' +
                ", mcu_version='" + mcu_version + '\'' +
                ", imei_info='" + imei_info + '\'' +
                ", mac_info='" + mac_info + '\'' +
                ", serial_number='" + serial_number + '\'' +
                ", product='" + product + '\'' +
                ", canbus_name='" + canbus_name + '\'' +
                ", ui_style='" + ui_style + '\'' +
                ", ui_show_switch='" + ui_show_switch + '\'' +
                ", apk_version='" + apk_version + '\'' +
                '}';
    }

    @Generated(hash = 1707215097)
    public Device(Long id, String os_version, String app_version, String bt_version,
            String mcu_version, String imei_info, String mac_info,
            String serial_number, String product, String canbus_name,
            String ui_style, String ui_show_switch, String apk_version) {
        this.id = id;
        this.os_version = os_version;
        this.app_version = app_version;
        this.bt_version = bt_version;
        this.mcu_version = mcu_version;
        this.imei_info = imei_info;
        this.mac_info = mac_info;
        this.serial_number = serial_number;
        this.product = product;
        this.canbus_name = canbus_name;
        this.ui_style = ui_style;
        this.ui_show_switch = ui_show_switch;
        this.apk_version = apk_version;
    }

    @Generated(hash = 1469582394)
    public Device() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOs_version() {
        return this.os_version;
    }
    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }
    public String getApp_version() {
        return this.app_version;
    }
    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }
    public String getBt_version() {
        return this.bt_version;
    }
    public void setBt_version(String bt_version) {
        this.bt_version = bt_version;
    }
    public String getMcu_version() {
        return this.mcu_version;
    }
    public void setMcu_version(String mcu_version) {
        this.mcu_version = mcu_version;
    }
    public String getImei_info() {
        return this.imei_info;
    }
    public void setImei_info(String imei_info) {
        this.imei_info = imei_info;
    }
    public String getMac_info() {
        return this.mac_info;
    }
    public void setMac_info(String mac_info) {
        this.mac_info = mac_info;
    }
    public String getCanbus_name() {
        return this.canbus_name;
    }
    public void setCanbus_name(String canbus_name) {
        this.canbus_name = canbus_name;
    }
    public String getSerial_number() {
        return this.serial_number;
    }
    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }
    public String getProduct() {
        return this.product;
    }
    public void setProduct(String product) {
        this.product = product;
    }

    public String getApk_version() {
        return this.apk_version;
    }

    public void setApk_version(String apk_version) {
        this.apk_version = apk_version;
    }

    public String getUi_style() {
        return this.ui_style;
    }

    public void setUi_style(String ui_style) {
        this.ui_style = ui_style;
    }

    public String getUi_show_switch() {
        return this.ui_show_switch;
    }

    public void setUi_show_switch(String ui_show_switch) {
        this.ui_show_switch = ui_show_switch;
    }
}
