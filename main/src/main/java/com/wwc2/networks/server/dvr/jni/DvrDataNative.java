package com.wwc2.networks.server.dvr.jni;

import android.os.SystemProperties;

import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.ShellUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DvrDataNative {

    private final String TAG = "DvrManagement";
    private static DvrDataNative dvrDataNative = null;
    private DvrDataNative(){}
    public final static String H264_RECORD_ACTION = "wwc2.h264.record.action";

    public final static String RECORD_H264_MAIN_START = "11";
    public final static String RECORD_H264_MAIN_RUNNING = "12";
    public final static String RECORD_H264_MAIN_STOP = "13";

    public final static String RECORD_H264_SUB_START = "21";
    public final static String RECORD_H264_SUB_RUNNING = "22";
    public final static String RECORD_H264_SUB_STOP = "23";

    public final static String RECORD_H264_DUAL_START = "31";
    public final static String RECORD_H264_DUAL_RUNNING = "32";
    public final static String RECORD_H264_DUAL_MAIN_STOP = "33";

    public final static String RECORD_UNKONW = "100";


    public final static String H264_MAIN_BIN_PATH = "/sdcard/mainh264stream";

    public final static String H264_SUB_BIN_PATH = "/sdcard/subh264stream";

    private final String WWC2_CAMERA_NODE = "/sys/class/gpiodrv/gpio_ctrl/do_capture";
    private final String WWC2_CAMERA_BIN = "/system/bin/wwc2_capture ";

    private final String CAMERA_SIGNAL_NODE = "/sys/class/gpiodrv/gpio_ctrl/sig_check";

    public static DvrDataNative newInstance() {
        if(dvrDataNative == null){
            dvrDataNative = new DvrDataNative();
        }
        return dvrDataNative;
    }
    public void createTakePicture(){
        LogUtils.e(TAG,"------createTakePicture！");
        writeTextFile("1", WWC2_CAMERA_NODE);
    }

    public String startTakePicture(final String fileName){
        LogUtils.e(TAG,"------startTakePicture！");

        ShellUtils.CommandResult commandResult =
                ShellUtils.execCommand(
                        WWC2_CAMERA_BIN + fileName, false);
        LogUtils.e("startTakePicture result=" + commandResult.result + ",,successMsg="
                + commandResult.successMsg + ",,errorMsg=" +commandResult.errorMsg);


        //success
        return "";
    }

    public int checkCamera(){
        String val = readTextFile(CAMERA_SIGNAL_NODE);
        return val.equals("") ? 0 : Integer.valueOf(val);
    }

    public void destroyTakePicture(){
        LogUtils.e(TAG,"------destroyTakePicture！");
        writeTextFile("0", WWC2_CAMERA_NODE);
    }

    public void openMainH264(){
        LogUtils.e(TAG,"------openMainH264！");
        openMain(H264_MAIN_BIN_PATH);
    }

    public void closeMainH264(){
        closeMain();
    }

    public void stopMainH264(){
        SystemProperties.set(H264_RECORD_ACTION, RECORD_H264_MAIN_STOP);
    }

    public void startMainH264(){
//        writeTextFile("0", H264_DVR_NODE);

        String action = SystemProperties.get(H264_RECORD_ACTION);
        if(!action.equals(RECORD_UNKONW)){
            stopMainH264();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SystemProperties.set(H264_RECORD_ACTION, RECORD_H264_MAIN_START);

    }

    public void openSubH264(){
        LogUtils.e(TAG,"------openSubH264！");
        openSub(H264_SUB_BIN_PATH);
    }

    public void closeSubH264(){
        closeSub();
    }

    public void stopSubH264(){
        SystemProperties.set(H264_RECORD_ACTION, RECORD_H264_SUB_STOP);
    }

    public void startSubH264(){
//        writeTextFile("0", H264_DVR_NODE);

        String action = SystemProperties.get(H264_RECORD_ACTION);
        if(!action.equals(RECORD_UNKONW)){
            stopSubH264();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SystemProperties.set(H264_RECORD_ACTION, RECORD_H264_SUB_START);

    }
    private void writeTextFile(String tivoliMsg, String fileName) {
        try {
            byte[] bMsg = tivoliMsg.getBytes();
            FileOutputStream fOut = new FileOutputStream(fileName);
            fOut.write(bMsg);
            fOut.close();
        } catch (IOException e) {
            //throw the exception
            LogUtils.e(TAG,"------writeTextFile --e=" + e.toString());
        }
    }

    private String readTextFile(String realPath) {
        String txt = "";
        try {
            File file = new File(realPath);
            if (!file.exists()) {
                LogUtils.e(TAG,"File not exist!");
                return null;
            }
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(realPath), "UTF-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                txt += temp;
            }
            br.close();
        }catch (Exception e){
            LogUtils.e(TAG,"------readTextFile --e=" + e.toString());
        }
        return txt;
    }

    public int getDeviceKey0(){
        return getKeyO();
    }

    public int getDeviceKey1(){
        return getKeyT();
    }

    public int getStatus(){
        return getOsStatus();
    }

    static {
        System.loadLibrary("_dvrdata");
    }

    native void openMain(String path);

    native void openSub(String path);

    native void closeMain();

    native void closeSub();

    native int getKeyO();

    native int getKeyT();

    native int getOsStatus();
}
