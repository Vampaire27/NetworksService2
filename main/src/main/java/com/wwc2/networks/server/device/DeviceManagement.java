package com.wwc2.networks.server.device;

import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;

import com.wwc2.networks.BuildConfig;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.provider.CrashError;
import com.wwc2.networks.server.provider.Device;
import com.wwc2.networks.server.provider.greendao.CrashErrorDao;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.TelephonyUtil;
import com.wwc2.networks.server.utils.ThreadUtil;
import com.wwc2.networks.server.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceManagement implements IDevice {
    public static final String AUTHORITY = "com.wwc2.main.provider.logic";
    public static final String VERSION_OS = "version_os";
    public static final String VERSION_APP = "version_app";
    public static final String VERSION_BLUE = "version_blue";
    public static final String VERSION_MCU = "version_mcu";
    public static final String INFO_IMEI = "info_imei";
    public static final String INFO_WIFI_MAC = "info_wifi_mac";
    public static final String INFO_SERIAL = "info_serial";
    public static final String CLIENT_ID = "client_id";
    //UI类型
    public static final String UI_STYLE = "ui_style";
    //UI开关
    public static final String UI_SHOW_SWITCH = "ui_show_switch";

    public static final String PLATFORM_TYPE = "persist.wwc2camera.platformtype";



    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static Context mContext = null;

    private static DeviceManagement deviceManagement = null;
    private DeviceManagement(){}

    public static DeviceManagement newInstance(Context con){
        if(deviceManagement == null){
            deviceManagement = new DeviceManagement();
        }
        mContext = con;
        return deviceManagement;
    }

    @Override
    public String getSystemCode() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + VERSION_OS);
        String strOs = mContext.getContentResolver().getType(uri);
        return strOs == null ? "Unknown" : strOs;
    }

    @Override
    public String getAPPCode() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + VERSION_APP);
        String strApp = mContext.getContentResolver().getType(uri);
        return strApp  == null ? "Unknown" : strApp;
    }

    @Override
    public String getBluetoothCode() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + VERSION_BLUE);
        String strBlue = mContext.getContentResolver().getType(uri);
        return strBlue  == null ? "Unknown" : strBlue;
    }

    @Override
    public String getMcuCode() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + VERSION_MCU);
        String strMcu = mContext.getContentResolver().getType(uri);
        return strMcu  == null ? "Unknown" : strMcu;
    }

    @Override
    public String getIMEICode() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + INFO_IMEI);
        String strImei = mContext.getContentResolver().getType(uri);
        return strImei  == null ? "Unknown" : strImei;
    }

    @Override
    public String getMacCode() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + INFO_WIFI_MAC);
        String strMac = mContext.getContentResolver().getType(uri);
        return strMac == null ? "Unknown" : strMac;
    }

    @Override
    public String getSerialNumber() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + INFO_SERIAL);
        String strSN = mContext.getContentResolver().getType(uri);
        if(strSN != null && strSN.equals("Unknown")){
            strSN = TelephonyUtil.newInstance(mContext).getSerialNumber();
        }else{
            strSN = TelephonyUtil.newInstance(mContext).getSerialNumber();
        }
        return strSN == null ? "Unknown" : strSN;
    }

    @Override
    public String getProduct() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + CLIENT_ID);
        String product = mContext.getContentResolver().getType(uri);
//        String product = SystemProperties.get("ro.build.product");
        return product == null ? "Unknown" : product;
    }

    @Override
    public String getCanbusName() {
       // int nameId = CanbusManagement.newInstance().getCanbusTypeInfo(); //todo:
        return "-1";
    }

    @Override
    public String getUiStyle() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + UI_STYLE);
        String ui_style = mContext.getContentResolver().getType(uri);
        LogUtils.d("---getUiStyle---ui_style=" + ui_style);
        if(ui_style == null || ui_style.equals("Unknown")){
            ui_style = "0";
        }
        return ui_style;
    }

    @Override
    public String getUiSwitch() {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + UI_SHOW_SWITCH);
        String ui_switch = mContext.getContentResolver().getType(uri);
        LogUtils.d("---getUiSwitch---ui_switch=" + ui_switch);
        if(ui_switch == null || ui_switch.equals("Unknown")){
            //1=true,0=false;
            ui_switch = "0";
        }else{
            if(ui_switch.equals("true")){
                ui_switch = "1";
            }else if(ui_switch.equals("false")){
                ui_switch = "0";
            }
        }
        return ui_switch;
    }

    @Override
    public String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public boolean checkUpdate() {
        List<Device> all = CarServiceClient.getDaoInstant().getDeviceDao().loadAll();
        if(all.size() > 0){
            Device device = all.get(0);
            if(! device.getApp_version().equalsIgnoreCase(getAPPCode())){
                return true;
            }
            if(! device.getOs_version().equalsIgnoreCase(getSystemCode())){
                return true;
            }
            if(! device.getBt_version().equalsIgnoreCase(getBluetoothCode())){
                return true;
            }
            if(! device.getMcu_version().equalsIgnoreCase(getMcuCode())){
                return true;
            }
            if(! device.getImei_info().equalsIgnoreCase(getIMEICode())){
                return true;
            }
            if(! device.getMac_info().equalsIgnoreCase(getMacCode())){
                return true;
            }
            if(! device.getSerial_number().equalsIgnoreCase(getSerialNumber())){
                return true;
            }
            if(! device.getProduct().equalsIgnoreCase(getProduct())){
                return true;
            }
            if(! device.getCanbus_name().equalsIgnoreCase(getCanbusName())){
                return true;
            }
            if(! device.getUi_style().equalsIgnoreCase(getUiStyle())){
                return true;
            }
            if(! device.getUi_show_switch().equalsIgnoreCase(getUiSwitch())){
                return true;
            }
            if(! device.getApk_version().equalsIgnoreCase(getAppVersion())){
                return true;
            }
        }
        return false;
    }

    @Override
    public Device getDevice() {
        Device device = new Device();
        device.setId(1L);
        device.setOs_version(getSystemCode());
        device.setApp_version(getAPPCode());
        device.setBt_version(getBluetoothCode());
        device.setMcu_version(getMcuCode());
        device.setImei_info(getIMEICode());
        device.setMac_info(getMacCode());
        device.setSerial_number(getSerialNumber());
        device.setProduct(getProduct());
        device.setCanbus_name(getCanbusName());
        device.setUi_style(getUiStyle());
        device.setUi_show_switch(getUiSwitch());
        device.setApk_version(getAppVersion());
        return device;
    }
    

    /**
     * @param apkId ID秘钥
     * @return
     */
    public String getSimVerifyKey(String apkId){
        String sn = getSerialNumber();
        String key = "wtc2" + apkId + sn;
        String val = Utils.md5(key);
        String pass = Utils.md5(val.substring(0, 6));
        return pass;
    }

    @Override
    public String getPlatformType() {
        return SystemProperties.get(PLATFORM_TYPE,"undefine");
    }
}
