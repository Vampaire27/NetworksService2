package com.wwc2.networks.cmd;


import android.os.SystemProperties;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.dvr.DvrPostImgManager;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.Utils;

import java.io.File;


public class TakePictureCmd  extends SendMesCmd implements DvrPostImgManager.onRequestApi {

    private int CMD_TIME_OUT =10; //10s
    private boolean cmdfinish = false;
    private String mPicturePath;

    public TakePictureCmd( RemotelyBean mRemotelyBean) {
        super(Config.TAKEPHOTO, mRemotelyBean);
    }


    public boolean fourCameraCheckPicture(String path){
        boolean ret = false;
        int i =15;
        while(i-- > 0) { //15s　超时
            if (FileUtils.fileIsExists(path)) {
                ret = true;
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public void exec() {
        LogUtils.d(" TakePictureCmd ........");
        if(mRemotelyBean != null) {

            if(!DvrManagement.newInstance().isTakePhoneSupport(Integer.parseInt(mRemotelyBean.getType()))){
                //sendMesToApp(Config.MSG_TYPE_CAMERASUPPORT,Config.CODE_PICTURE,"不支持");
                nextCmd();
                return;
            }

            int channel = Integer.parseInt(mRemotelyBean.getType());
            DvrManagement dvr = DvrManagement.newInstance();
            DvrPostImgManager mDvrPostImgManager = DvrPostImgManager.getInstans();
            mPicturePath= dvr.takePicture(channel);
            if (mDvrPostImgManager.isFourPlatform() ||
                    mDvrPostImgManager.isTwoPlatform() ||
                    mDvrPostImgManager.isOnePlatform()) {
                if (mPicturePath != null && fourCameraCheckPicture(mPicturePath)) {
                    mDvrPostImgManager.postImgFeedBack(mRemotelyBean.getValues(), true,
                            "拍照成功", mPicturePath, TakePictureCmd.this);
                } else {
                    mDvrPostImgManager.postImgFeedBack(mRemotelyBean.getValues(), false,
                            "拍照失败", mPicturePath, TakePictureCmd.this);
                }
            } else {
                if (dvr.checkPictureAndNetWorkState(mPicturePath, channel)) {

                    long size = new File(mPicturePath).length();
                    LogUtils.d("11===takePicture  size:" + size);
                    if (size > 512) {
                        mDvrPostImgManager.postImgFeedBack(mRemotelyBean.getValues(), true,
                                "拍照成功", mPicturePath, TakePictureCmd.this);

                    } else {
                        LogUtils.d("Front camears sensor==takePicture  size为空!");
                    }
                } else {
                    mDvrPostImgManager.postImgFeedBack(mRemotelyBean.getValues(), false,
                            "拍照失败", mPicturePath, TakePictureCmd.this);
                }
            }

            while(!cmdfinish  && CMD_TIME_OUT >0 ) {
                try {
                    CMD_TIME_OUT--;
                    LogUtils.d("------TakePictureCmd ---wait --CMD_TIME_OUT-----" + CMD_TIME_OUT );
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        nextCmd();

    }

    void deletePicture() {
        if (mPicturePath != null) {
            File pFlie = new File(mPicturePath);
            if(pFlie.exists()){
                pFlie.delete();
                LogUtils.d("------deletePicture =" +mPicturePath );
            }
            mPicturePath = null;
        }

    }

    @Override
    public boolean isSingleCmd() {
        return false;
    }

    @Override
    public void success( ) {
        LogUtils.d("------TakePictureCmd ---success-------" );
        deletePicture();
        cmdfinish= true;
    }

    @Override
    public void onfail() {
        LogUtils.d("------TakePictureCmd ---onfail-------" );
        deletePicture();
        cmdfinish= true;
    }
}
