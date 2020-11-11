package com.wwc2.networks.server.location;

import android.location.Location;
import android.util.Log;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.GPSBean;
import com.wwc2.networks.server.bean.TraceBean;
import com.wwc2.networks.server.netty.NettySendManager;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;

import java.util.Random;

public class TraceBeanManager implements CarServiceClient.AccStatusInterface,AndroidLocations.LocationsInterface {

    private static String  TAG = "TraceBeanManager";

    private final int TRACE_STATE_START_UNSTART = 0 ;

    private final int TRACE_STATE_START = 1;

    private final int TRACE_STATE_ING= 2;

    private final int TRACE_STATE_END = 3;

    private int mTraceState = TRACE_STATE_START_UNSTART;

    private final static int GPS_LOCATION_METER = 5; //gps定位　精度　5m

    private TraceBean mTraceBean;
    private long startTime;

    Random random = new Random();
    private int mCurrentTripID;


    private GPSBean mLastGPSBean;

    private static TraceBeanManager mTraceBeanManager = new TraceBeanManager();

    private  String mLastAdderss ;

    private TraceBeanManager(){
    }

    public String getmLastAdderss() {
        return mLastAdderss;
    }

    public void setmLastAdderss(String mLastAdderss) {
        this.mLastAdderss = mLastAdderss;
    }

    public static TraceBeanManager getInstance(){
         return mTraceBeanManager;
    }

    public void init(){
         LocationManagement.newInstance().getAndroidLocations().setLocationListener(this);
         CarServiceClient.setAccStatusInterface(this);
         mCurrentTripID= random.nextInt();
    }

     public void startTrace(){
         mTraceBean = new TraceBean();
         mTraceState = TRACE_STATE_START;
         startTime = System.currentTimeMillis()/1000;
         mCurrentTripID = random.nextInt();
         mTraceBean.setTripId(mCurrentTripID);
     }

    public void  vaildStartTime(long time) {
        LogUtils.d(TAG, "gps time  :" + time);
        LogUtils.d(TAG, "startTime :" + startTime);
        if(time - startTime > 3600 *10) {
            startTime = time;
        }
    }

    public void stopTrace(){
        if(mTraceState == TRACE_STATE_ING
                && mTraceBean.getSumTrace() > Config.getDisConfig() ){
            mTraceState = TRACE_STATE_END;
            mTraceBean.setEndTime(System.currentTimeMillis() / 1000);
            mTraceBean.setSumTime(mTraceBean.getEndTime() - startTime);
            mTraceBean.setaSpeed(mTraceBean.getSumTrace()*(float)3.6 / mTraceBean.getSumTime());
            mTraceBean.seteGtype(mLastGPSBean.getGtype(), mLastGPSBean.getLat(), mLastGPSBean.getLng());
            LogUtils.d(TAG, "send a Trace :" + mTraceBean.toString());
            NettySendManager.getInstance().send(mTraceBean.toString());

        }
         resetTrace();
     }

     public void updateCurrentTrace(GPSBean newGPSBean){
         mTraceBean.setEndTime(System.currentTimeMillis() / 1000);
         mTraceBean.setSumTime(mTraceBean.getEndTime() - startTime);
         mTraceBean.setaSpeed(mTraceBean.getSumTrace() *(float)3.6/ mTraceBean.getSumTime());
         mTraceBean.seteGtype(newGPSBean.getGtype(), newGPSBean.getLat(), newGPSBean.getLng());
     }

    public TraceBean getCurrentTrace(){
         return mTraceBean;
     }

     public int getCurrentTripID(){
        return mCurrentTripID;
     }

     public void resetTrace(){
         mTraceState = TRACE_STATE_START_UNSTART;
         mTraceBean  = null;
     }

    public void addDistanceAndLastBean(GPSBean mBean) {
        LogUtils.d(TAG," addDistanceAndLastBean " + mBean.toString());
        float distance = LocationUtils.calculateLineDistance(mLastGPSBean.getLng(), mLastGPSBean.getLat(),
                mBean.getLng(), mBean.getLat());

        if(distance < GPS_LOCATION_METER){ // 小于５M没有意义的点
             LogUtils.d(TAG," this GPS point is less than 5m... ");
             return;
        }
        mTraceBean.sumTrance(distance);
        mLastGPSBean = mBean;
    }


    public void updateMaxSpeed(GPSBean mBean){
        float currentSpeed = mBean.getSpeed()*(float)3.6;  // m/s  -> km/h
        if(mTraceBean.getMaxSpeed() < currentSpeed ){
            mTraceBean.setMaxSpeed(currentSpeed );
        }
    }


    @Override
    public void onAccStatusChanged(int status) {
        if (status == Config.System.ACC_ON) {
            LogUtils.d(TAG," begin start Trace ...wait first location ");
            startTrace();
        }else{
            GpsPointManager.getInstance().sendFrame();// force send the last frame.
            GpsPointManager.getInstance().resetLocationTimes();
            stopTrace();
        }

    }

    @Override
    public void onLocationsChanged(Location location, GPSBean mBean) {
        LogUtils.d(TAG," onLocationsChanged  "+ mBean);
         switch (mTraceState){
             case TRACE_STATE_START:
                 mTraceBean.setsGPS(mBean.getGtype(),mBean.getLat(),mBean.getLng());
                 mLastGPSBean = mBean;
                 mTraceState = TRACE_STATE_ING;
                 break;
             case TRACE_STATE_ING:
                 addDistanceAndLastBean(mBean);
                 updateMaxSpeed(mBean);
                 break;
              default:
                  break;
         }
    }

}
