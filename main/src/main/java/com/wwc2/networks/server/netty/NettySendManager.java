package com.wwc2.networks.server.netty;

import com.wwc2.networks.server.location.GpsPointManager;
import com.wwc2.networks.server.utils.LogUtils;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class NettySendManager {
    public static String TAG = "NettySendManager";

    private final static  NettySendManager mNettySendManager = new NettySendManager(); //hungry mode ..

    private Thread mSendThread;

    List<String> mSendList = new ArrayList<>();

    public static NettySendManager getInstance(){
        return mNettySendManager;
    }


    void startSendThread(){
        mSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    while (mSendList.size() >0) {

                        synchronized (mSendList) {
                            ClientConnect.getInstance().getChannel().writeAndFlush(mSendList.remove(0));
                            LogUtils.d(TAG, "send cmd  one by one  over");
                        }

                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        LogUtils.d(TAG, "send cmd  Thread. wait-wait-wait");
                        synchronized (mSendList) {
                            mSendList.wait();
                        }
                        LogUtils.d(TAG, "send cmd  Thread.is wake up--wake up");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mSendThread.start();
    }
  /*
  * return false: add to cache ,need send next time.
  * return true : send direct ...
  * */
    public boolean send(String value){
        boolean ret = false;
        if(ClientConnect.getInstance().getChannel()!= null
           && ClientConnect.getInstance().getChannel().attr(ClientHandler.LOGIN_FLAG).get()){
            LogUtils.d(TAG, "connect ok, just write..");
            ClientConnect.getInstance().getChannel().writeAndFlush(value);
            //check if the is any data in cache need sen.
             ret = true;
           if (mSendList.size() >0) {
                if (mSendThread == null) {
                    startSendThread();
                } else {
                    mSendThread.notifyAll();
                }
           }
        }else {
            synchronized (mSendList) {
                if(ClientConnect.getInstance().getChannel()!= null){
                    LogUtils.d(TAG, "connect fail channel isActive:"+ClientConnect.getInstance().getChannel().isActive()+" mSendList add " + value);
                }else{
                    LogUtils.d(TAG, "connect fail channel is null mSendList add " + value);
                }
                if(mSendList.size() >100){
                    mSendList.remove(0);
                }
                mSendList.add(value);
            }
        }
        return  ret;
    }

}
