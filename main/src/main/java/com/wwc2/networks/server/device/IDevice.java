package com.wwc2.networks.server.device;

import com.wwc2.networks.server.provider.Device;

public interface IDevice {

    String getSystemCode();
    String getAPPCode();
    String getBluetoothCode();
    String getMcuCode();
    String getIMEICode();
    String getMacCode();
    String getSerialNumber();
    String getProduct();
    boolean checkUpdate();
    Device getDevice();
    String getCanbusName();
    String getUiStyle();
    String getUiSwitch();
    String getAppVersion();
    String getPlatformType();
}
