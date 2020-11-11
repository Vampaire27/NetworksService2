package com.wwc2.networks.server.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;

import java.io.File;

public class UpgradeApkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtils.d("receiver package replaced is :"+packageName);
            if(packageName.equals(context.getPackageName())){
                FileUtils.deleteDir(new File(Config.APK_PATH));
                CarServiceClient.stopService();
                try {
                    Thread.sleep(2000);
                }catch (Exception e){}
                CarServiceClient.startService();
            }
        }
    }
}
