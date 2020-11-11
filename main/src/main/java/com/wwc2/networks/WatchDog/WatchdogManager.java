package com.wwc2.networks.WatchDog;


import com.wwc2.networks.server.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WatchdogManager  extends  Thread{
    final static String TAG = "WatchDogManager-Thread";

    final static int basetime =500;
    private List<DogBase> mDogList = new ArrayList<>();
    private final Object mLock = new Object();

    private  final Object waitLock = new Object();

    static private WatchdogManager mWatchdogManager;

    private WatchdogManager() {
        super(TAG);
    }

   static public WatchdogManager  getInStance() {
        if(mWatchdogManager == null){
            mWatchdogManager = new WatchdogManager();
            mWatchdogManager.start();
        }
          return mWatchdogManager;
    }

    public void registerDog(DogBase dog){
        synchronized(mLock){
            mDogList.add(dog);
            dog.FeadDog(); //need add in mLock, in first registerDog, may be  error hasDogCome
        }

        synchronized (waitLock) {
            waitLock.notifyAll();
        }
    }

    public void unregisterDog(DogBase dog){
        synchronized (mLock){
            mDogList.remove(dog);
        }
    }

    @Override
    public void run() {
        DogBase mDog;
        super.run();
        while(true){
             while(true) {
                 mDog = null;

                 synchronized (mLock) {
                     if (mDogList.size() > 0) {
                         Collections.sort(mDogList);
                         if (mDogList.get(0).hasDogCome()) {
                             mDog = mDogList.get(0);
                         }
                     }else{
                         break;
                     }
                 }

                 if (mDog != null) {
                     LogUtils.d(TAG,"mDog.DogDog....  ");
                     mDog.DogDog();
                 }
                 try {
                     Thread.sleep(basetime);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }

            try {
                synchronized (waitLock) {
                    waitLock.wait();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }
    }


}
