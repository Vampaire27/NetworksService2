package com.wwc2.networks.cmd.jni;

import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;

import java.io.FileOutputStream;
import java.io.IOException;

public class CameraDataSync {

    private static String dataDev = "/dev/wwc2_yuv_sync";

    private static String controlDev ="/sys/devices/platform/wwc2_camera_combine/camera_action";

    public static final int MODE_WWC2_H264 =3;
    public static final int MODE_WWC2_H264_START =5;
    public static final int MODE_WWC2_H264_STOP =11;


     private static CameraDataSync mCameraDataSync;
     public static synchronized CameraDataSync getInstance(){
         if(mCameraDataSync == null){
             mCameraDataSync = new CameraDataSync();
         }
        return mCameraDataSync;
     }


    static {
        System.loadLibrary("cameradata_jni");
    }

    private void writeTextFile(String tivoliMsg, String fileName) {
        try {
            byte[] bMsg = tivoliMsg.getBytes();
            FileOutputStream fOut = new FileOutputStream(fileName);
            fOut.write(bMsg);
            fOut.close();
        } catch (IOException e) {
            //throw the exception
            LogUtils.e("------write TextFile --e=" + e.toString());
        }
    }

    public boolean startCameraBlock(String mapFileName){
         boolean ret = false;

         String action = MODE_WWC2_H264 +" " + MODE_WWC2_H264_START;
         writeTextFile(action,controlDev);

        for(int i= 0;i < 10; i++ ){
            if(FileUtils.fileIsExists(mapFileName)){
                ret = true;
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
         }
        return ret;
    }



    public void stopCameraNoBlock(){
        String action = MODE_WWC2_H264 +" " + MODE_WWC2_H264_STOP;
        writeTextFile(action,controlDev);
    }

    public void openCameraDevice(){
       openDev(dataDev);
    }

    public void closeCameraDataDev(){
         closeDev(dataDev);
    }

    public byte readCameraDataDev(){
       return blockRead();
    }
     native boolean openDev(String path);

     native boolean closeDev(String path);

     native byte blockRead();

     native int blockWrite();
}
