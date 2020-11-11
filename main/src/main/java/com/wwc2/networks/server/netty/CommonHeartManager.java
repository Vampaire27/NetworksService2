package com.wwc2.networks.server.netty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.utils.LogUtils;

import io.rong.imlib.RongIMClient;

public class CommonHeartManager {

    private final static  CommonHeartManager mCommonHeartManager = new CommonHeartManager(); //hungry mode ..

    public final int  CLIENT_TIME = 1000 * 6* 25; //2.5 分钟

    public static final String ACTION_NETTY_HEART =
            "com.netty.heart.beat";

    private PendingIntent mPendingIntent;

    private AlarmReceiver mAlarmReceiver;

    public static String version="33049";

    private String beatjson = "{\"cmd\":\"0011\",\"version\":\""+version+"\"}";

    private boolean isStart = false;

    static public CommonHeartManager getInstance(){
        return mCommonHeartManager;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public synchronized void startHeart(){
        if(isStart()){
            LogUtils.d(" heart has been started.");
            return;
        }else{
            setStart(true);
        }

        LogUtils.d("startBeat......"  + CLIENT_TIME + "s.");
        final IntentFilter mFilter = new IntentFilter(ACTION_NETTY_HEART);
        if(mAlarmReceiver == null){
            mAlarmReceiver = new AlarmReceiver();
            CarServiceClient.getContext().registerReceiver(mAlarmReceiver,mFilter);
        }

        // Schedule the alarm!
        Intent intent = new Intent(ACTION_NETTY_HEART);
        mPendingIntent= PendingIntent.getBroadcast(CarServiceClient.getContext(),
                0, intent, 0);
        AlarmManager am = (AlarmManager)CarServiceClient.getContext().getSystemService(Context.ALARM_SERVICE);
        am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+CLIENT_TIME, mPendingIntent);

    }

    public void stopHeart(){
        LogUtils.d("stopBeat．．．");
        AlarmManager mAlarmManager = (AlarmManager)CarServiceClient.getContext().getSystemService(Context.ALARM_SERVICE);
        if (mPendingIntent != null) {
            mAlarmManager.cancel(mPendingIntent);
            mPendingIntent= null;
        }
        setStart(false);
    }

    public  class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean anyConnect = false;
            if(ClientConnect.getInstance().getChannel() != null) {
                LogUtils.d("AlarmReceiver heart .channel is open:"+ClientConnect.getInstance().getChannel().isOpen()
                        +" active:"+ClientConnect.getInstance().getChannel().isActive() +" address = " +ClientConnect.getInstance().getChannel().localAddress()) ;
                ClientConnect.getInstance().getChannel().writeAndFlush(beatjson);
                anyConnect =true;
            }else{
                LogUtils.d("AlarmReceiver heart 10min..channel is null");
            }

            if(RongIMClient.getInstance().getCurrentConnectionStatus()
                == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED ) {
                RongIMClient.getInstance().sendPing();
                LogUtils.d("RongIMClient is connected !");
                anyConnect =true;
            }

            if(anyConnect == true) {
                AlarmManager am = (AlarmManager) CarServiceClient.getContext().getSystemService(Context.ALARM_SERVICE);

                if (mPendingIntent != null) {
                    LogUtils.d(" start a  new setExactAndAllowWhileIdle....");
                    am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + CLIENT_TIME, mPendingIntent);
                }
            }else{
                stopHeart();
            }
        }
    }

}
