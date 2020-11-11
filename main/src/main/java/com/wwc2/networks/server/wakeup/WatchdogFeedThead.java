package com.wwc2.networks.server.wakeup;

import android.content.Intent;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.broadcast.wwc2Broadcast;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;

public class WatchdogFeedThead extends Thread{
    private int wakeup_time ; //　default time  10s;

    private final Object mWakeupTimeLock = new Object();

    public WatchdogFeedThead( int time) {
        super();
        wakeup_time = time;
    }

    public void setWakeupTime(int time){
        synchronized (mWakeupTimeLock) {
            wakeup_time = time;
        }
    }

    @Override
    public void run() {
         while (!isInterrupted()) {

            int time;
            synchronized (mWakeupTimeLock) {
                time = wakeup_time;
            }

            LogUtils.d("watch dog feed  ..........time = " + time/2);
            Intent mcuIntent = new Intent(wwc2Broadcast.ACTION_CARNET_WAKEUP);
            mcuIntent.putExtra(wwc2Broadcast.KEY_CARNET_WAKEUP, Config.GO_WAKEUP);
            mcuIntent.putExtra(wwc2Broadcast.KEY_WAKEUP_TIME, time);
            CarServiceClient.getContext().sendBroadcast(mcuIntent);

            try {
                Thread.sleep(time / 2);
            }catch (InterruptedException e){
                break;
            }

        }

        LogUtils.d("WatchdogFeedThead  isInterrupted .....");
    }
}
