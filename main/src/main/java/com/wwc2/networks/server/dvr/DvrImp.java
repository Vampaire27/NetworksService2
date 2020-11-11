package com.wwc2.networks.server.dvr;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;

import com.wwc2.dvr.DeleteVideoCallBack;
import com.wwc2.dvr.IWCarDvr;
import com.wwc2.dvrassist.DeleteVideoCallBack360;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.utils.AppUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;


public class DvrImp implements RemoteDvr {

public Context context;
private static IWCarDvr mDvrRemotelyProxy;

 public DvrImp(Context context) {
   this.context = context;
 }

 @Override
    public void checkAIDL(ServiceConnection conn) {
     if(AppUtils.isAPKExist(context, "com.wwc2.dvr")){
         LogUtils.d("------启动DVR------");
         Intent intent = new Intent();
         intent.setPackage("com.wwc2.dvr");
         intent.putExtra("package" , "com.wwc2.networks");
         intent.setAction("com.wwc2.dvr.RecordService");
         CarServiceClient.getContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
     }
    }

    @Override
    public void startBinderDvr(IBinder binder) {
        mDvrRemotelyProxy = IWCarDvr.Stub.asInterface(binder);
    }


    @Override
    public void stopBinderDvr() {
        mDvrRemotelyProxy = null;
    }

    @Override
    public void createDvr() throws RemoteException {
        if(mDvrRemotelyProxy != null) {
            mDvrRemotelyProxy.createDvr();
        }
    }

    @Override
    public boolean getDvrStatus(int id) throws RemoteException {
        if(mDvrRemotelyProxy != null) {
            mDvrRemotelyProxy.getDvrStatus(id);
        }
        return true;
    }

    @Override
    public boolean stopDvr() throws RemoteException {
        if(mDvrRemotelyProxy != null) {
          return   mDvrRemotelyProxy.stopDvr();
        }else{
            return  false;
        }
    }



    @Override
    public void getVideoFilelist(RemotelyBean remotelyBean) {
        try {
            if (mDvrRemotelyProxy != null) {
                int channel = Integer.parseInt(remotelyBean.getType());
                LogUtils.d("===getVideoFilelist  channel:" + channel);
                final String json = mDvrRemotelyProxy.getAllDriveVideo(
                        Integer.parseInt(remotelyBean.getType()),
                        Integer.parseInt(remotelyBean.getPage()),
                        Integer.parseInt(remotelyBean.getNumber()));
                LogUtils.d("===getVideoFilelist  json:" + json);

                NetWorkManagement.newInstance()
                        .postFeedBack(remotelyBean.getValues(),true, json);


            }

        }
        catch (Exception e) { //may be dvr exception ,cannot sleep.
            e.printStackTrace();
        }
    }


    @Override
    public String takePictureWithLockVideo(int CameraNum) {
        String path = null;
        LogUtils.d("takePictureWithLockVideo ");
        if(mDvrRemotelyProxy != null) {
            try {
                //take photos
                mDvrRemotelyProxy.postSensor(false, true);//视频加锁
                //take photos　need frist reset the  prop value,may use the last value.

                if(CameraNum == Config.BACK_CAMERA){
                    SystemProperties.set(Config.CAPTURE_MAIN_ACTION,Config.CAPTURE_UNKONW);
                }else if(CameraNum == Config.FRONT_CAMERA){
                    SystemProperties.set(Config.CAPTURE_SUB_ACTION,Config.CAPTURE_UNKONW);
                }

                path = mDvrRemotelyProxy.takePicture(false, CameraNum);
                mDvrRemotelyProxy.postSensor(false, false);  //取消加锁

                if ("1000".equals(path)) {//内存不足
                    LogUtils.d("11===takePictureWithLockVideo 内存不足,无法拍照...");
                    return null;
                } else if ("1001".equals(path)) {//文件为空
                    LogUtils.d("11===takePictureWithLockVideo 文件为空,dvr拍照异常...");
                    return null;
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    @Override
    public String takePicture(int CameraNum) {
        String path = null;

        if(mDvrRemotelyProxy != null) {
            try {
                //take photos　need frist reset the  prop value,may use the last value.
                if(CameraNum == Config.BACK_CAMERA){
                    SystemProperties.set(Config.CAPTURE_MAIN_ACTION,Config.CAPTURE_UNKONW);
                }else if(CameraNum == Config.FRONT_CAMERA){
                    SystemProperties.set(Config.CAPTURE_SUB_ACTION,Config.CAPTURE_UNKONW);
                }
                path = mDvrRemotelyProxy.takePicture(false, CameraNum);


                if ("1000".equals(path)) {//内存不足
                    LogUtils.d("11===takePicture 内存不足,无法拍照...");
                    return null;
                } else if ("1001".equals(path)) {//文件为空
                    LogUtils.d("11===takePicture 文件为空,dvr拍照异常...");
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("takePictureWithLockVideo path=" + path);
        return path;
    }

    @Override
    public String postSensor(boolean isOff, boolean syncStatus) {
        return null;
    }

    @Override
    public void updateSensor() throws RemoteException {
        if(mDvrRemotelyProxy != null) {
            mDvrRemotelyProxy.updateSensor();
        }
    }

    @Override
    public void close() throws RemoteException {
        if(mDvrRemotelyProxy != null) {
            mDvrRemotelyProxy.close();
        }
    }

    @Override
    public int getTakeCameraStatus() throws RemoteException {
        if(mDvrRemotelyProxy != null) {
           return mDvrRemotelyProxy.getTakeCameraStatus();
        }
        return 1000;
    }

    @Override
    public boolean checkDVRConnect() {
        if(mDvrRemotelyProxy != null) {
            return  true;
        }
        return false;
    }

    @Override
    public void setH264Mode(int mode) throws RemoteException {
        if(mDvrRemotelyProxy != null) {
            mDvrRemotelyProxy.setH264Mode(mode);
        }
    }

    @Override
    public void deleteFile(String path, DeleteVideoCallBack callBack) throws RemoteException {
        if(mDvrRemotelyProxy != null) {
            mDvrRemotelyProxy.deleteFile(path,callBack);
        }
    }

    @Override
    public void deleteFile(String path, DeleteVideoCallBack360 callBack) throws RemoteException {
        LogUtils.d("deleteFile do nothing only for adapter interface ! " );
    }

}
