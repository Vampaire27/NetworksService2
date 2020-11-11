package com.wwc2.networks.server.location;

import android.location.Location;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.auto.AutoManagement;
import com.wwc2.networks.server.bean.GPSBean;
import com.wwc2.networks.server.bean.GPSFrameBean;
import com.wwc2.networks.server.netty.NettySendManager;
import com.wwc2.networks.server.utils.LogUtils;


public class GpsPointManager implements  AndroidLocations.LocationsInterface{

    private static GpsPointManager mGpsPointManager;

    private GPSFrameBean mGPSCurrentFrame ;

    private GPSBean mCurrentGPSBean = new GPSBean();

    private long startTime;
    static public long DURATION_TIME = 60*3; // S

    private static String TAG = "GpsPointManager";

    //only send succe,sendt to  2020
    private boolean mFirstUpdate = true;



    public static GpsPointManager getInstance(){
          if(mGpsPointManager == null){
              mGpsPointManager = new GpsPointManager();
          }
          return mGpsPointManager;
    }


    public void  initAndRegister(){ //可以重复调用。
        startTime=0;
        LocationManagement.newInstance().getAndroidLocations().setLocationListener(this);
    }


     public  GPSBean getGPSBean(){
       return mCurrentGPSBean;
     }


     boolean checkAvailableGPSPoint(GPSBean lBean){

         if((int)mCurrentGPSBean.getLng() == 0){ //first location
             mCurrentGPSBean = lBean;
             return true;
         }else{

              if(lBean.getSpeed() > 50 ){ //50KM
                  return true;
              }

             float distance = LocationUtils.calculateLineDistance(mCurrentGPSBean.getLng(), mCurrentGPSBean.getLat(),
                     lBean.getLng(), lBean.getLat());
              LogUtils.d(TAG,"checkAvailableGPSPoint distance =" + distance);

              if(distance > 25){
                  return true;
              }
         }

         return false;
     }

     private void resetGPSFrame(){
         mGPSCurrentFrame.clearData();
         mGPSCurrentFrame = null;
         startTime = System.currentTimeMillis() / 1000;
     }

    public boolean overflow(){
        if((System.currentTimeMillis()/1000 - startTime) > DURATION_TIME ){
            return  true;
        }
        return false;
    }


    public  synchronized void sendFrame(){
        if(mGPSCurrentFrame != null && mGPSCurrentFrame.hasAvailableData()) {
            TraceBeanManager.getInstance().updateCurrentTrace(mCurrentGPSBean);
            mGPSCurrentFrame.setTripIfo(TraceBeanManager.getInstance().getCurrentTrace());
            mGPSCurrentFrame.setAccStat(CarServiceClient.getAccStatus());
            String sendStr = mGPSCurrentFrame.toString();
            resetGPSFrame();
            LogUtils.d("GpsPointManager .mGPSCurrentFrame.toString() " + sendStr);
            if(AutoManagement.newInstance(CarServiceClient.getContext()).mUploadLevel.canSendGPS()) {
                if(NettySendManager.getInstance().send(sendStr)){
                    setFirstUpdate(false);
                };
            }
        }
    }


    public  void resetLocationTimes(){
        mFirstUpdate = true;
    }

    public void setFirstUpdate(boolean mFirstUpdate) {
        this.mFirstUpdate = mFirstUpdate;
    }

    @Override
    public void onLocationsChanged(Location lLocation, GPSBean lBean) {
        LogUtils.d("GpsPointManager ... onLocationsChanged. ");

        if(mGPSCurrentFrame == null) {
            mGPSCurrentFrame = new GPSFrameBean();
            startTime = System.currentTimeMillis() / 1000;
            mCurrentGPSBean = lBean;
            mGPSCurrentFrame.writeData(lBean);
        }else if(checkAvailableGPSPoint(lBean)) {
            mCurrentGPSBean = lBean;
            mGPSCurrentFrame.writeData(lBean);
        }
        if(mFirstUpdate){
            TraceBeanManager.getInstance().vaildStartTime(lBean.getTm());
            sendFrame();
        }else if (overflow()) {
            sendFrame();
        }

    }



}
