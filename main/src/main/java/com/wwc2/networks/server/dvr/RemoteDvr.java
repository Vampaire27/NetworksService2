package com.wwc2.networks.server.dvr;

import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.wwc2.dvr.DeleteVideoCallBack;
import com.wwc2.dvrassist.DeleteVideoCallBack360;
import com.wwc2.networks.server.bean.RemotelyBean;


public interface RemoteDvr {
    abstract void checkAIDL(ServiceConnection conn);
    abstract public void startBinderDvr(IBinder binder);
    abstract void createDvr() throws RemoteException;
    abstract  public void stopBinderDvr();
    abstract boolean getDvrStatus(int id) throws RemoteException;
    abstract boolean stopDvr() throws RemoteException;

    //fromType 1:前置 2;后置  pageNum:第几页 pageSize:每页多少条数据
    abstract  public void getVideoFilelist(RemotelyBean remotelyBean);
    abstract public String takePictureWithLockVideo(int CameraNum);

    abstract  public String takePicture(int CameraNum);

    abstract String postSensor(boolean isOff, boolean syncStatus);
    abstract void updateSensor() throws RemoteException;

    //关闭
    abstract void close() throws RemoteException;

    abstract int getTakeCameraStatus() throws RemoteException;//0:获取异常 1:单录 2:双录

    abstract void setH264Mode(int mode) throws RemoteException;

    abstract void deleteFile(String path,DeleteVideoCallBack callBack) throws RemoteException;

    abstract void deleteFile(String path, DeleteVideoCallBack360 callBack) throws RemoteException;

    abstract public boolean checkDVRConnect();
}
