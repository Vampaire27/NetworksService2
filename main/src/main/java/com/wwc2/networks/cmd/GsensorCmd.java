package com.wwc2.networks.cmd;


import android.hardware.Camera;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.dvr.DvrPostImgManager;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.Utils;

import java.io.File;


public class GsensorCmd extends BaseCmd implements NetWorkManagement.onRequestApi {
    int CMD_TIME_OUT = 10; //10s
    private boolean cmdfinish = false;

    public GsensorCmd(RemotelyBean mRemotelyBean) {
        super(Config.SENSOR, mRemotelyBean);
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
        DvrManagement dvr = DvrManagement.newInstance();
        if (DvrPostImgManager.getInstans().isFourPlatform()) {
            String Path = dvr.takePictureWithLockVideo(Config.FOUR_CAPTURE);
            if (fourCameraCheckPicture(Path)) {
                uploadPicture(Path);
            }
        } else if (DvrPostImgManager.getInstans().isTwoPlatform()) {
            String Path = dvr.takePictureWithLockVideo(Config.TWO_CAPTURE);
            if (fourCameraCheckPicture(Path)) {
                uploadPicture(Path);
            }
        } else if (DvrPostImgManager.getInstans().isOnePlatform()) {
            String Path = dvr.takePictureWithLockVideo(Config.FRONT_CAPTURE);
            if (fourCameraCheckPicture(Path)) {
                uploadPicture(Path);
            }
        } else {
            if (dvr.isCamearaSupport(Config.FRONT_CAMERA)) {
                String photoPath = dvr.takePictureWithLockVideo(Config.FRONT_CAMERA);
                if (dvr.checkPictureAndNetWorkState(photoPath, Config.FRONT_CAMERA)) {
                    uploadPicture(photoPath);
                } else {
                    cmdfinish = true;
                }

                {
                    LogUtils.d("CMD_TIME_OUT = " + CMD_TIME_OUT);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }while (!cmdfinish && CMD_TIME_OUT-- > 0);
            }

            if (dvr.isCamearaSupport(Config.BACK_CAMERA)) {
                String backphotoPath = dvr.takePictureWithLockVideo(Config.BACK_CAMERA);
                if (dvr.checkPictureAndNetWorkState(backphotoPath, Config.BACK_CAMERA)) {
                    uploadPicture(backphotoPath);
                }
            }
        }

        nextCmd();
    }

    public void uploadPicture(String path){
        long size = new File(path).length();
        LogUtils.d("11===takePicture  size:" + size);
        if(size <= 0){
            LogUtils.d("Front camears sensor==takePicture  size为空!");
            return;
        }

        NetWorkManagement.newInstance().uploadSensorPicture(path,this);
    }

    @Override
    public boolean isSingleCmd() {
        return false;
    }


    @Override
    public void success() {
      cmdfinish = true;
    }

    @Override
    public void onfail() {
        cmdfinish = true;
    }


}

