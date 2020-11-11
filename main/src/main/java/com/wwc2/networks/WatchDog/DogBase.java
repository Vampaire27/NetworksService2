package com.wwc2.networks.WatchDog;

import android.support.annotation.NonNull;

import com.wwc2.networks.server.utils.LogUtils;

public  class DogBase implements  Comparable<DogBase>{

    private int INTERVAL;//单位s, 最新1S .
    private long dogReachTime;
    private TimeOut mTimeOut;


    public DogBase(int INTERVAL,TimeOut timeOut) {
        this.INTERVAL = INTERVAL;
        this.mTimeOut = timeOut;
    }

    public void FeadDog(){
        LogUtils.d(WatchdogManager.TAG,"FeadDog....  ");
        dogReachTime = System.currentTimeMillis() + INTERVAL *1000;
    }

    public boolean hasDogCome(){
        boolean ret = false;
        if(dogReachTime  <  System.currentTimeMillis()){
           ret = true;
        }
        return  ret;
    }


    public void DogDog(){
        mTimeOut.FeedDogTimtOut();
    }

    @Override
    public int compareTo(@NonNull DogBase o) {
        return (this.dogReachTime < o.dogReachTime) ? -1 : ((this.dogReachTime == o.dogReachTime) ? 0 : 1);
    }
}
