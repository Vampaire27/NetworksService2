package com.wwc2.networks.server.canbus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.wwc2.canbus_interface.AMPDefine;
import com.wwc2.canbus_interface.AirConditionDefine;
import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.canbus_interface.CarInfoDefine;
import com.wwc2.canbus_interface.CarSettingsDefine;
import com.wwc2.canbus_interface.RadarDefine;
import com.wwc2.canbus_interface.ServiceDefine;
import com.wwc2.canbus_interface.SyncDefine;
import com.wwc2.canbus_interface.TimeDefine;
import com.wwc2.canbussdk.ICanbusCallback;
import com.wwc2.canbussdk.ICanbusDriver;
import com.wwc2.networks.server.canbus.uilistener.uiListener_AMP;
import com.wwc2.networks.server.canbus.uilistener.uiListener_Air;
import com.wwc2.networks.server.canbus.uilistener.uiListener_CarInfo;
import com.wwc2.networks.server.canbus.uilistener.uiListener_CarSettings;
import com.wwc2.networks.server.canbus.uilistener.uiListener_Radar;
import com.wwc2.networks.server.canbus.uilistener.uiListener_Service;
import com.wwc2.networks.server.canbus.uilistener.uiListener_Sync;
import com.wwc2.networks.server.canbus.uilistener.uiListener_Time;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CanbusDriver {

    private static final String TAG = "CanbusDriver";
    private static final int MSG_DATA_UPDATE = 1;
    private static CanbusDriver mCanbusDriver = null;
    private CallbackHandler mHandler = null;
    private static ICanbusDriver iDriver = null;
    private ICanbusCallback iCallback = null;

    public static final int TIMER_CLOSE_WINDOWN = 5;
    public static final int TIMER_REQUEST_INFO = 6;
    public static final long DELAY_TIMES = 1000;

    private ArrayList<uiListener_Air> mGroupAirUIListener = new ArrayList<uiListener_Air>();
    private ArrayList<uiListener_AMP> mGroupAMPUIListener = new ArrayList<uiListener_AMP>();
    private ArrayList<uiListener_CarInfo> mGroupCarInfoUIListener = new ArrayList<uiListener_CarInfo>();
    private ArrayList<uiListener_CarSettings> mGroupCarSettingsUIListener = new ArrayList<uiListener_CarSettings>();
    private ArrayList<uiListener_Radar> mGroupRadarUIListener = new ArrayList<uiListener_Radar>();
    private ArrayList<uiListener_Service> mGroupServiceUIListener = new ArrayList<uiListener_Service>();
    private ArrayList<uiListener_Time> mGroupTimeUIListener = new ArrayList<uiListener_Time>();
    private ArrayList<uiListener_Sync> mGroupSyncUIListener = new ArrayList<uiListener_Sync>();

    public class canbusCallback extends ICanbusCallback.Stub {
        @Override
        public void setCanSerises(int i, String s) throws RemoteException {

        }

        @Override
        public boolean sendDataToCan(byte[] bytes) throws RemoteException {
            return false;
        }

        @Override
        public boolean carSteerProcess(int i) throws RemoteException {
            return false;
        }

        @Override
        public void canBusVersion(String s) throws RemoteException {

        }

        @Override
        public void canBusConnect(boolean b) throws RemoteException {

        }

        @Override
        public boolean canbusDataCallBack(String s, int i, Bundle bundle) throws RemoteException {
            Message message = new Message();
            message.what = MSG_DATA_UPDATE;
            message.obj = s;
            message.arg1 = i;
            message.setData(bundle);
            if (mHandler != null) {
                mHandler.sendMessage(message);
            } else {
                Log.e(TAG, "mHandler is null!");
            }

            return true;
        }

        @Override
        public void supportFunction(String[] strings) throws RemoteException {

        }

        @Override
        public void setSoundChannelToMcu(int i) throws RemoteException {

        }
    }

    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected ");
            iDriver = ICanbusDriver.Stub.asInterface(service);
            if (iDriver == null) {
                Log.e(TAG, "iDriver == null");
                return;
            }

            iCallback = new canbusCallback();
            if (iCallback != null) {
                try {
                    iDriver.registerCallback(iCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "iCallback == null");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (iDriver != null && iCallback != null) {
                    iDriver.unregisterCallback(iCallback);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            iDriver = null;
        }
    };


    public CanbusDriver() {
        mHandler = new CallbackHandler(this);
    }

    public static CanbusDriver getInstance() {
        if (mCanbusDriver == null) {
            mCanbusDriver = new CanbusDriver();
        }
        return mCanbusDriver;
    }

    public ICanbusDriver getDriver() {
        return iDriver;
    }

    public void bindService(Context context) {
        Log.i(TAG, "bindService begin----context=" + context);
        Intent intent = new Intent(CanBusDefine.CANBUS_SERVICE_NAME);
        ComponentName component = new ComponentName(CanBusDefine.CANBUS_SERVICE_PACKET_NAME,
                CanBusDefine.CANBUS_SERVICE_CLASS_NAME);
        intent.setComponent(component);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "bindService end");

        CanBusNettyManager.getInstance().registerCanbus();
    }

    public void unbindService(Context context) {
        Log.i(TAG, "unbindService");
        context.unbindService(conn);
    }


    public void registerCarInfoUIListener(uiListener_CarInfo listener) {
        boolean found = false;
        for (int i = 0; i < mGroupCarInfoUIListener.size(); i++) {
            if (mGroupCarInfoUIListener.get(i) == listener) {
                found = true;
                break;
            }
        }
        if (!found)
            mGroupCarInfoUIListener.add(listener);
    }

    public void unRegisterCarInfoUIListener(uiListener_CarInfo listener) {
        for (int i = 0; i < mGroupCarInfoUIListener.size(); i++) {
            if (mGroupCarInfoUIListener.get(i) == listener) {
                mGroupCarInfoUIListener.remove(i);
                break;
            }
        }
    }



    private static class CallbackHandler extends Handler {
        private WeakReference<CanbusDriver> reference;

        public CallbackHandler(CanbusDriver manager) {
            reference = new WeakReference<CanbusDriver>(manager);
        }

        public void handleMessage(Message msg) {
            CanbusDriver manager = reference.get();
            if (manager != null) {
                switch (msg.what) {
                    case MSG_DATA_UPDATE:
                        String module = (String) msg.obj;
                        int nId = msg.arg1;
                        Bundle bundle = msg.getData();
                        manager.dispathCallback(module, nId, bundle);
                        break;
                }
            }
        }
    }

    public void dispathCallback(String module, int nID, Bundle bundle) {
//        Log.d(TAG, "dispathCallback  module="+module+", nID="+nID);
         if (CarInfoDefine.MODULE.equals(module)) {
            for (int i = 0; i < mGroupCarInfoUIListener.size(); i++) {
                uiListener_CarInfo mCarInfo = mGroupCarInfoUIListener.get(i);
                if (null == mCarInfo) {
                    continue;
                }
                mCarInfo.CarInfoChange(nID, bundle);
            }
        }
    }
}
