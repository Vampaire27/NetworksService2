package com.wwc2.networks.server.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.CarSystemServer;

public class NetworkUtil {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission")
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static final String NET_TYPE = "NET_TYPE";
    public static final int NET_NOTE = 0;
    public static final int NET_BUILT = 1;
    public static final int NET_EXTERNAL = 2;
    public static final int NET_WIFI = 3;
    public static int getNetworkType(Context context) {
        if(isWifi(context)){
            return NET_WIFI;
        }else{
            String subId = SystemProperties.get("persist.sys.simswich");
            switch (subId){
                //内置
                case "1":
                    return NET_BUILT;
                //外置
                case "2":
                    return NET_EXTERNAL;
            }
        }
        return NET_NOTE;
    }

    /**
     * 检查网络打印出网络日志 方便查找是否是网络原因导致
     */
    public static void showNetworkLog() {
        try {
            boolean networkAvailable =isNetworkAvailable(CarServiceClient.getContext());
            int  networkType=getNetworkType(CarServiceClient.getContext());
            if(networkType==1){
                LogUtils.d("showNetworkLog networkAvailable :"+networkAvailable + " networkType:inIccid");
            }else if(networkType==2){
                LogUtils.d("showNetworkLog networkAvailable :"+networkAvailable + " networkType :outIccid");
            }else if(networkType==3){
                LogUtils.d("showNetworkLog networkAvailable :"+networkAvailable + " networkType :wifi");
            }else{
                LogUtils.d("showNetworkLog networkAvailable :"+networkAvailable + " networkType :no get");
            }
        }catch (Exception e){
            LogUtils.d("showNetworkLog Exception :" + e.toString());
        }
    }


}
