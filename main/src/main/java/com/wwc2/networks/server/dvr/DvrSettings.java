package com.wwc2.networks.server.dvr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wwc2.networks.CarSystemServer;
import com.wwc2.networks.server.utils.LogUtils;

public class DvrSettings {

    CarSystemServer mCarSystemServer;
    DVRSettingBroadcast mDVRSettingBroadcast;
    public static final String VIDEO_SETTINGS= "Video_settings";
    public static final int VIDEO_DEFALUT =1000;
    public static final String ACTION_VIDEO_SETTINGS= "com.wwc2_dvr_Video_settings";

    boolean isSyncFail = false;

    public DvrSettings(CarSystemServer carserver) {
        this.mCarSystemServer = carserver;
    }


    public void registerDVRBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_VIDEO_SETTINGS);
        mDVRSettingBroadcast = new DVRSettingBroadcast();
        mCarSystemServer.registerReceiver(mDVRSettingBroadcast, intentFilter);
        LogUtils.d("registerDVRBroadcast ");
    }


    public void unregisterDVRBroadcast(){
        mCarSystemServer.unregisterReceiver(mDVRSettingBroadcast);
        LogUtils.d("unregisterDVRBroadcast ");
    }

    private class DVRSettingBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
              String action = intent.getAction();
              if(ACTION_VIDEO_SETTINGS.equals(action)){
                  if(intent.getIntExtra(VIDEO_SETTINGS,VIDEO_DEFALUT)!= VIDEO_DEFALUT) {
                      LogUtils.d("com.wwc2_dvr_Video_setting =" + intent.getIntExtra(VIDEO_SETTINGS,VIDEO_DEFALUT));
                      mCarSystemServer.checkUploadOs();
                  }
              }
        }
    }

    public void setSyncFail(boolean syncFail) {
        isSyncFail = syncFail;
    }

    public void syncSettings(){
        if(isSyncFail){
            mCarSystemServer.checkUploadOs();
        }
    }
}
