package com.wwc2.networks.server.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.lang.reflect.Method;

public class TelephonyUtil {
    private static TelephonyUtil telephonyUtil = null;
    private TelephonyManager telephonyManager = null;
    private Context con = null;

    public static TelephonyUtil newInstance(Context context){
        if(telephonyUtil == null){
            telephonyUtil = new TelephonyUtil(context);
        }
        return telephonyUtil;
    }

    private TelephonyUtil(Context context){
        this.con = context;
        telephonyManager = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
    }
    
	public String getSerialNumber() {
		String serial = null;
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.serialno");
		} catch (Exception ignored) {
		}
		return serial;
	}

    @SuppressLint("MissingPermission")
    public String getTowerInfo() {
        int mcc = -1;
        int mnc = -1;
        int lac = -1;
        int cellId = -1;

        String operator = telephonyManager.getNetworkOperator();
        if(operator != null && operator.length() >= 3){
            mcc = Integer.parseInt(operator.substring(0, 3));
            mnc = Integer.parseInt(operator.substring(3));
            if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) telephonyManager
                        .getCellLocation();
                cellId = cdmaCellLocation.getBaseStationId();
                lac = cdmaCellLocation.getNetworkId();
            } else {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) telephonyManager
                        .getCellLocation();
                cellId = gsmCellLocation.getCid();
                lac = gsmCellLocation.getLac();
            }
            String tower = String.valueOf(mcc) + ","
                    + String.valueOf(mnc) + ","
                    + String.valueOf(lac) + ","
                    + String.valueOf(cellId);
            return tower;
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    public static String getMime(Context context) {
        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            String imei = telManager.getDeviceId();       //取出IMEI:国际移动设备标识,是手机的识别id(International Mobile Equipment Identity)
//            String tel = telManager.getLine1Number();     //这就是总是不能成功的获取本机手机号方法
            String iccid = telManager.getSimSerialNumber();  //取出ICCID:集成电路卡识别码（固化在手机SIM卡中,就是SIM卡的序列号）很容易伪造哦
//            String imsi = telManager.getSubscriberId();  //取出IMSI:国际移动用户识别码(就是识别你是哪个运营商的SIM卡)
//            String operatorNum = telManager.getSimOperator();
//            String operator = "";
//            if (operatorNum != null) {
//                if (operatorNum.equals("46000") || operatorNum.equals("46002")
//                        || operatorNum.equals("46007")) {
//                    // 中国移动
//                    operator = "中国移动";
//                } else if (operatorNum.equals("46001")) {
//                    // 中国联通
//                    operator = "中国联通";
//                } else if (operatorNum.equals("46003")) {
//                    // 中国电信
//                    operator = "中国电信";
//                }
//            }
//            LogUtils.d("imei--" + imei + "--iccid--" + iccid + "--imsi--" + imsi + "--运营商--" + operatorNum + ":" + operator);

//            LogUtils.d("iccid--" + iccid);
            return iccid;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
