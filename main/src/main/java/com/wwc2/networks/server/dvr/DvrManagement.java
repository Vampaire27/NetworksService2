package com.wwc2.networks.server.dvr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemProperties;

import com.wwc2.dvr.DeleteVideoCallBack;
import com.wwc2.dvr.IWCarDvr;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.utils.AppUtils;
import com.wwc2.networks.server.utils.Config;

import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.NetworkUtil;


public class DvrManagement  {

    private final String TAG = "DvrManagement";
    private static DvrManagement dvrManagement = null;

    private int cameraNum = 1000;//defalut value for app todo.
    private DvrManagement(){}

    private static Context context = null;
    private RemoteDvr mRemoteDvr;

    public static DvrManagement newInstance() {
        if(dvrManagement == null){
            dvrManagement = new DvrManagement();
        }
        context = CarServiceClient.getContext();
        return dvrManagement;
    }

    public void checkAIDL(){
        LogUtils.d("DvrManagement  newInstance 检测 mDvrRemotelyProxy=" + mRemoteDvr);
        //检测aidl连接状态,检测dvr运行状态,重新连接
        if(mRemoteDvr != null){
            mRemoteDvr.checkAIDL(mServiceConnection);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            LogUtils.d("==断开=onServiceDisconnected:" + arg0.getPackageName());
            stopBinderDvr();

            LogUtils.d("==尝试重连...");
            checkAIDL();
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            LogUtils.d("==连接=onServiceConnected:" + name.getPackageName());
            startBinderDvr(binder);
        }
    };

    public void initDvrData(){
        mRemoteDvr = new DvrImp(context);
        checkAIDL();
    }

    public void initDvrData(RemoteDvr remoteDvr){
        mRemoteDvr =  remoteDvr;
        checkAIDL();
    }

    public void stopBinderDvr(){
        LogUtils.d("DvrManagement  stop!");
        if(mRemoteDvr != null) {
            mRemoteDvr.stopBinderDvr();
        }
    }

    public void closeDVR(){
        LogUtils.d("DvrManagement  closeDVR!  ..mDvrRemotelyProxy=" + mRemoteDvr);
        if(mRemoteDvr != null){
            try {
                mRemoteDvr.stopDvr();///////////////////////////////
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    public void CloseCamera(){

            if (mRemoteDvr !=null){
                try {

                    LogUtils.d("DvrManagement  doClose  off 状态，释放dvr资源...");
                    mRemoteDvr.close();

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
    }


    public void OpenCamera(){
        try {
            if(mRemoteDvr != null) {
                mRemoteDvr.createDvr();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void startBinderDvr(IBinder binder){
        LogUtils.d("DvrManagement  start!   binder=" + binder);
        if(mRemoteDvr != null) {
            mRemoteDvr.startBinderDvr(binder);
        }
    }



    public void getVideoFilelist(RemotelyBean remotelyBean){
        if(mRemoteDvr != null) {
            mRemoteDvr.getVideoFilelist(remotelyBean);
        }
    }


    public String takePictureWithLockVideo(int CameraNum){
        if(mRemoteDvr != null) {
            return mRemoteDvr.takePictureWithLockVideo(CameraNum);
        }else{
            return null;
        }
    }

    public String takePicture(int CameraNum){
        if(mRemoteDvr != null) {
            return mRemoteDvr.takePicture(CameraNum);
        }else{
            return null;
        }
    }

    public boolean checkPictureAndNetWorkState(String path,int channel ){
        boolean ret =false;
        String cameraProp;

        if(channel == Config.BACK_CAMERA ){
            cameraProp = Config.CAPTURE_MAIN_ACTION;
        }else{
            cameraProp =  Config.CAPTURE_SUB_ACTION;
        }

        if (path!=null && !path.equals("")) {

                //检测照片状态
                int i= 30;
                String action;
                while(i-- > 0){ //15s　超时
                    action = SystemProperties.get(cameraProp);
                    if(action.equals(Config.CAPTURE_STOP)){
                        ret = true;
                        break;
                    }else if (action.equals(Config.CAPTURE_FAIL)){
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    }catch (InterruptedException e){

                    }
                    LogUtils.d("checkPictureAndNetWorkState :=" + action  +", i= "+ i);
                }
        }else{
            LogUtils.d("...checkPictureAndNetWorkState...path　= null .");
        }

        if(!NetworkUtil.isNetworkAvailable(CarServiceClient.getContext())) {
            LogUtils.d("...checkPictureAndNetWorkState...网络不可用.");
           ret = false;
        }

        return ret;
    }



    public void updateSensor(){
        try {
            if (mRemoteDvr != null) {
                mRemoteDvr.updateSensor();
            } else {
                LogUtils.d("updateSensor mDvrRemotelyProxy=null!...dvr断开...");
            }
        } catch (RemoteException e) {}
    }

    public int getSupportCameraNum(){
        try {
            if (mRemoteDvr != null) {
                cameraNum = mRemoteDvr.getTakeCameraStatus();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }finally {
            LogUtils.d("getSupportCameraNum =" +cameraNum);
            return cameraNum;
        }
    }


   public boolean isCamearaSupport(int num){
        if(num > getSupportCameraNum()){
            return false;
        }else{
            return true;
        }

   }


    public boolean isTakePhoneSupport(int num){

        if (DvrPostImgManager.getInstans().isFourPlatform() ||
                DvrPostImgManager.getInstans().isTwoPlatform() ||
                DvrPostImgManager.getInstans().isOnePlatform()) {
            return true;
        }

        if(num > getSupportCameraNum()){
            return false;
        }else{
            return true;
        }
    }

   public boolean checkDVRConnect(){
       if (mRemoteDvr != null) {
          return mRemoteDvr.checkDVRConnect();
        }else {
           return false;
       }
   }

    public void setH264Stream (int mod ){
        try {
            if (mRemoteDvr != null) {
                mRemoteDvr.setH264Mode(mod);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void deleteFile(String path, DeleteVideoCallBack callBack){
        try {
            if (mRemoteDvr != null) {
                mRemoteDvr.deleteFile(path, callBack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
