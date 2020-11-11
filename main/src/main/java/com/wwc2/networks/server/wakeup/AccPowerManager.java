package com.wwc2.networks.server.wakeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.broadcast.wwc2Broadcast;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class AccPowerManager {
    static  private AccPowerManager mAccPowerManager;
    static  public Context mContext;

    public  AtomicBoolean isWackLcok =new AtomicBoolean(false);

    public WatchdogFeedThead mWatchdogFeedThead = null;


    private WakeupCallBack mWakeupCallBack;

    private int timeout = 0;

    private Timer WakeUpTimeOuttimer ;
    private  final  int WAKEUP_TIME_OUT = 10*1000;



    static public void init(Context mCtx){
        if(mAccPowerManager == null){
            mContext = mCtx;
            mAccPowerManager =new AccPowerManager();
         }
    }


    static public AccPowerManager getInstance(){
        return  mAccPowerManager;
    }

    public void aquireWakeLockLocked(AccPowerManager.WakeupCallBack wakeupCallBack){
        aquireWakeLockLocked(wakeupCallBack,Config.WAKEUP_TIME);
    }


    public void aquireWakeLockLocked(AccPowerManager.WakeupCallBack wakeupCallBack,int time){
        if(isWackLcok.get()){
            LogUtils.d("system has wakeup, wait the cmd exe----");
            return;
        }else {
            LogUtils.d("aquireWakeLockLocked ++++++");
            mWakeupCallBack = wakeupCallBack;
            int acc = CarServiceClient.getAccStatus();

            switch (acc) {
                case Config.System.ACC_ON:
                    isWackLcok.set(true);
                    mWakeupCallBack.wakeUpSuccess();
                    break;
                case Config.System.ACC_OFF:
                    timeout = time;

                    Intent mcuIntent = new Intent(wwc2Broadcast.ACTION_CARNET_WAKEUP);
                    mcuIntent.putExtra(wwc2Broadcast.KEY_CARNET_WAKEUP, Config.GO_WAKEUP);
                    mcuIntent.putExtra(wwc2Broadcast.KEY_WAKEUP_TIME, time);
                    CarServiceClient.getContext().sendBroadcast(mcuIntent);

                    startTimeOutTask();
                    break;
            }
        }

    }



    private void startTimeOutTask() {
        if (WakeUpTimeOuttimer == null) {
            WakeUpTimeOuttimer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (mWakeupCallBack != null) {
                            LogUtils.d("wackup failed ! ");
                            mWakeupCallBack.wakeUpFail();
                            mWakeupCallBack = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        WakeUpTimeOuttimer = null;
                    }
                }
            };

            WakeUpTimeOuttimer.schedule(task, WAKEUP_TIME_OUT);
        }
    }

    private void startWatchDog(int time){
            if(mWatchdogFeedThead == null){
                mWatchdogFeedThead = new WatchdogFeedThead(time);
                mWatchdogFeedThead.start();
            }else{
                mWatchdogFeedThead.setWakeupTime(time);
            }
    }

    public void releaseWakeLockLocked(){
        LogUtils.d("releaseWakeLockLocked ----");
           isWackLcok.set(false);

            if(mWatchdogFeedThead != null) {
                LogUtils.d("mWatchdogFeedThead.interrupt & closeDVR ----");
                mWatchdogFeedThead.interrupt(); //
                mWatchdogFeedThead = null;
            }

           if(CarServiceClient.getAccStatus() == Config.System.ACC_OFF){  //only acc off ,need closeCamera&  send GO_SLEEP
               DvrManagement.newInstance().CloseCamera();
               Intent mcuIntent = new Intent(wwc2Broadcast.ACTION_CARNET_WAKEUP);
               mcuIntent.putExtra(wwc2Broadcast.KEY_CARNET_WAKEUP, Config.GO_SLEEP);
               CarServiceClient.getContext().sendBroadcast(mcuIntent);
           }

        }





    public interface WakeupCallBack{
        void wakeUpSuccess();

        void wakeUpFail();
    }

    public void registerReceiver(Context ctx) {
        IntentFilter localeFilter = new IntentFilter();
        localeFilter.addAction(wwc2Broadcast.ACTION_MAIN_WAKEUP);
        localeFilter.addAction(wwc2Broadcast.ACTION_MAIN_SLEEP_VOLTAGE);
        LogUtils.d("mWakeupFinishBroadcastReceiver--int --registerReceiver =");
        ctx.registerReceiver(mWakeupFinishBroadcastReceiver, localeFilter);
    }

    public void unregisterReceiver(Context ctx) {
        LogUtils.d("unregisterReceiver--mWakeupFinishBroadcastReceiver ");
        ctx.unregisterReceiver(mWakeupFinishBroadcastReceiver);
    }


     private BroadcastReceiver mWakeupFinishBroadcastReceiver  = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d("mWakeupFinishBroadcastReceiver----action = 1 " + action);
            if (action.equals(wwc2Broadcast.ACTION_MAIN_WAKEUP)) {
                if (mWakeupCallBack != null) {

                    LogUtils.d("wakeup system from acc off , wakeUpSuccess, startWatchDog");
                    WakeUpTimeOuttimer.cancel();
                    WakeUpTimeOuttimer = null;

                    isWackLcok.set(true); //reciver the main ack. wakup ok
                    startWatchDog(timeout);
                    mWakeupCallBack.wakeUpSuccess();
                    mWakeupCallBack = null;
                }
            } else if (action.equals(wwc2Broadcast.ACTION_MAIN_SLEEP_VOLTAGE)) {
                //ACC OFF低电压时需退出视频推流。2019-11-23
                CarServiceClient.updateAccStateFromContentResolover();
            }
        }
    };
}
