package com.wwc2.networks.cmd;


import android.media.MediaRecorder;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.util.Slog;

import com.google.gson.Gson;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.WatchDog.DogBase;
import com.wwc2.networks.WatchDog.TimeOut;
import com.wwc2.networks.WatchDog.WatchdogManager;
import com.wwc2.networks.cmd.jni.CameraDataSync;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.bean.RongyunDataBean;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.dvr.DvrPostImgManager;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.ThreadUtil;
import com.wwc2.networks.server.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.rtc.CenterManager;
import cn.rongcloud.rtc.RTCErrorCode;
import cn.rongcloud.rtc.RongRTCAudioMixer;
import cn.rongcloud.rtc.RongRTCConfig;
import cn.rongcloud.rtc.RongRTCEngine;
import cn.rongcloud.rtc.callback.JoinRoomUICallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.core.RendererCommon;
import cn.rongcloud.rtc.custom.MediaMode;
import cn.rongcloud.rtc.custom.OnSendListener;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.events.RongRTCEventsListener;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.MediaStreamTypeMode;
import cn.rongcloud.rtc.stream.MediaType;
import cn.rongcloud.rtc.stream.local.RongRTCAVOutputStream;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.stream.local.RongRTCLocalSourceManager;
import cn.rongcloud.rtc.stream.remote.RongRTCAVInputStream;
import cn.rongcloud.rtc.user.RongRTCLocalUser;
import cn.rongcloud.rtc.user.RongRTCRemoteUser;
import cn.rongcloud.rtc.utils.AudioBufferStream;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

import static cn.rongcloud.rtc.RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_15f_3;
import static cn.rongcloud.rtc.RongRTCConfig.RongRTCVideoResolution.RESOLUTION_360_640;

public class TakeVideoCmd extends  SendMesCmd implements CarServiceClient.AccStatusInterface, TimeOut {
    private final String TAG = "TakeVideoCmd";
    private MappedByteBuffer yuv_memoryMap = null;
    private RandomAccessFile yuv_random = null;
    private String mainFile = "/storage/sdcard0/.mainH264";
    private String subFile = "/storage/sdcard0/.subH264";
    private String fourFlie = "/storage/sdcard0/.h264Img";
    private final int YUV_PUSH_DEF = 0;
    private final int YUV_PUSH_START = 1;
    private final int YUV_PUSH_STOP = 2;
    //0=DEF,1=推流中,2=停止
    private int yuv_push = YUV_PUSH_DEF;
    //默认宽
    private int DEF_W = 640;
    //默认高
    private int DEF_H = 360;
    //像素点
    private float PIXEL = 1.5f;

    private static final String H264_SOCKET = "h264YuvSocket";
    private static final int BUFFER_SIZE = 20;

    private static final int STATUS_DATA_READY =1;

    private static final int STATUS_DATA_WRITE_FINISH =1;

    private static final int STATUS_DATA_WRITE_STOP =0Xff;

    YUVSendThread mYUVSendThread;


    private boolean nextCmdHasExe = false;  //make sure the next cmd only exe one.

    private RongRTCRoom myRongRTCRoom = null;
    private RongRTCAVOutputStream mOutputStream;

    private RongRTCLocalUser localUser;

    private boolean joinRoomSucce =false;


    private int RYMAX = 100;

    private DogBase mDogBase;
    private final int DOGFEED_TIME = 50; // 单位30s


    public TakeVideoCmd( RemotelyBean mRemotelyBean) {
        super(Config.RY_TAKELIVE, mRemotelyBean);
    }


    @Override
    public void exec() {

         if(!DvrManagement.newInstance().isCamearaSupport(Integer.parseInt(mRemotelyBean.getType()))){
            //sendMesToApp(Config.MSG_TYPE_CAMERASUPPORT,Config.CODE_VIDEO,"不支持");
            nextCmd();
            return;
        }

        RongRTCConfig.Builder mBuilder= new RongRTCConfig.Builder();
        //mBuilder.videoProfile(RONGRTC_VIDEO_PROFILE_360P_15f_3);
        mBuilder.setVideoResolution(RESOLUTION_360_640);
        mBuilder.setMaxRate(1024);
        mBuilder.setMinRate(200);

        mBuilder.buildDefaultMode();

       // RongRTCCapture.getInstance().setRTCConfig(mBuilder.build());


        CarServiceClient.setAccStatusInterface(this);
        DvrManagement.newInstance().OpenCamera();

        startRtc();

        String name =getMMapFile();

        int i = 0;
        while(i++ < RYMAX) {

            LogUtils.d(TAG," joinRoomSucce = " + joinRoomSucce);
            if ( joinRoomSucce && checkMapFile(name)) {
                if (DvrPostImgManager.getInstans().isTwoPlatform()) {
                    DvrManagement.newInstance().setH264Stream(Integer.valueOf(Config.DUAL_H264));
                } else if (DvrPostImgManager.getInstans().isOnePlatform()) {
                    DvrManagement.newInstance().setH264Stream(Integer.valueOf(Config.FRONT_H264));
                } else {
                    DvrManagement.newInstance().setH264Stream(Integer.valueOf(Config.QUART_H264));
                }
                if(ish264ImgMap()) {
                    YUVSocketSend(name);
                }else{
                    sendYHV420Live(name);
                }

                mDogBase = new DogBase(DOGFEED_TIME,this);
                WatchdogManager.getInStance().registerDog(mDogBase);
                return;
            }
            if(nextCmdHasExe){ //maybe nextCmd be exe by InterruptCmd, so need not to wait..
                LogUtils.d(" nextCmdHasExe has be exec....");
                return;
            }
            try {
                Thread.sleep(200);
            }catch (InterruptedException e){

            }

        }

        nextCmd();

    }

    @Override
    public void nextCmd() {
       if(!nextCmdHasExe) {
           super.nextCmd();
           nextCmdHasExe = true;
       }
    }

    private void startRtc(){

            if(CenterManager.getInstance().isInRoom() ){
                LogUtils.d("...check...当前正在房间中,....");

                RongRTCRoom rr = CenterManager.getInstance().getRongRTCRoom();
                if(rr != null){
                    RongRTCEngine.getInstance().quitRoom(rr.getRoomId(), new RongRTCResultUICallBack() {
                        @Override
                        public void onUiSuccess() {
                            LogUtils.d("..check 纠错.onUiSuccess..重新进入..");
                            joinRoom(mRemotelyBean.getRoomId());
                        }

                        @Override
                        public void onUiFailed(RTCErrorCode rtcErrorCode) {
                            LogUtils.d("..check..纠错..异常  onUiFailed rtcErrorCode=" + rtcErrorCode);
                           if(rtcErrorCode ==RTCErrorCode.RongRTCCodeNotInRoom ) {
                               joinRoom(mRemotelyBean.getRoomId());
                           }else {
                               //提示后台......
                               NetWorkManagement.newInstance()
                                       .postFeedBack(mRemotelyBean.getValues(),
                                               true, "进入房间失败");
                               videoFinish();
                           }
                        }
                    });
                }else{
                    videoFinish();
                }

            }else{
                //首次进入房间
                joinRoom(mRemotelyBean.getRoomId());
            }
        }


    private void joinRoom(String roomId){

        LogUtils.d(TAG,"<----------joinRoom ------------->"  + roomId);

        RongRTCEngine.getInstance().joinRoom(roomId, new JoinRoomUICallBack() {
            @Override
            protected void onUiSuccess(final RongRTCRoom rongRTCRoom) {
                LogUtils.d(TAG,"...publishRyStream  onUiSuccess 房间加入成功. rongRTCRoom=" + rongRTCRoom);
                myRongRTCRoom = rongRTCRoom;

                //监听房间,检测用户离开,人数为0时停止推
                myRongRTCRoom.registerEventsListener(rtcEventsListener);
                publishStream();

            }

            @Override
            protected void onUiFailed(RTCErrorCode rtcErrorCode) {
                LogUtils.d(TAG,"...joinRoom  onUiFailed 加入房间失败 rtcErrorCode=" + rtcErrorCode
                        + ",myRongRTCRoom=" + myRongRTCRoom);

                if(myRongRTCRoom != null){
                    myRongRTCRoom.unregisterEventsListener(rtcEventsListener);
                    myRongRTCRoom = null;
                }
                videoFinish();
                NetWorkManagement.newInstance()
                        .postFeedBack(mRemotelyBean.getValues(),
                                true, "进入房间失败");

            }
        });
    }




    private void publishStream(){

        localUser = myRongRTCRoom.getLocalUser();
        new RongRTCConfig.Builder().enableMicrophone(true);
        //发布资源
        localUser.publishAVStream(localUser.getDefaultAudioStream(), new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                LogUtils.d(TAG,"...publishStream  onUiSuccess...发布资源成功...dvr.");
                mOutputStream = new RongRTCAVOutputStream(MediaType.VIDEO,"USB");
                localUser.publishAVStream(mOutputStream, new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {
                        //TODO
                        joinRoomSucce =true;
                    }

                    @Override
                    public void onUiFailed(RTCErrorCode errorCode) {
                        videoFinish();
                    }
                });
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {

                LogUtils.d("...publishStream   onUiFailed...");
                videoFinish();

            }
        });


    }

    boolean checkMapFile(String name){
        if(ish264ImgMap()){
            return true;
         }
        File mFile = new File(name);
        if(mFile.exists()){
            return true;
        }else{
            return false;
        }
    }



    public void sendYHV420Live(String name){
        LogUtils.d(TAG,"------sendYHV420Live---------remotelyBean=" + mRemotelyBean.toString());
        //false
        NetWorkManagement.newInstance()
                .postFeedBack(mRemotelyBean.getValues(),true, "DVR实时视频发布成功");
        try {
            LogUtils.d(TAG,"------sendYHV420Live---------name=" + name);
            yuv_random = new RandomAccessFile(name, "rw");
            FileChannel fc = yuv_random.getChannel();
            //size=该映射文件的总大小
            yuv_memoryMap = fc.map(FileChannel.MapMode.READ_WRITE, 0, yuv_random.length());

            ThreadUtil.start(new Runnable() {
                @Override
                public void run() {
                    //分辨率*像素点
                    int val = (int) (DEF_W * DEF_H * PIXEL);
                    byte isOK = 0;
                    //先清零
                    yuv_memoryMap.put(val, (byte) 0);
                    byte[] date = new byte[val];
                    if (mOutputStream == null) {
                        LogUtils.d("mOutputStream == null ");
                        videoFinish();
                    }

                    while (true){
                        if(yuv_push == YUV_PUSH_STOP){
                            LogUtils.d(TAG,"-----停止dvr推流------sendYHV420Live-----return!!!");
                            //重置
                            yuv_push = YUV_PUSH_DEF;

                            return;
                        }
                        yuv_push = YUV_PUSH_START;
                        isOK = yuv_memoryMap.get(val);
                        //LogUtils.d("-----------sendYHV420Live----------isOK=" + isOK);
                        if(isOK == 1){
                            yuv_memoryMap.position(0);
                            yuv_memoryMap.get(date,0,val);
                            mOutputStream.writeByteBuffer(date,  DEF_W,DEF_H,0);
                            //读取完，清零
                            yuv_memoryMap.put(val, (byte) 0);
                        }

                        try {
                            Thread.sleep(10);
                        }catch (InterruptedException e){

                        }
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (yuv_random != null) {
                try {
                    yuv_random.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(yuv_memoryMap != null){
                yuv_memoryMap.clear();
            }
        }
    }


    private void RTCSendData(byte data[]){
        if(mOutputStream!= null) {
            mOutputStream.writeByteBuffer(data, DEF_W, DEF_H, 0);
        }
    }

    private void YUVSocketSend(String name){
        LogUtils.d("------sendYHV420Live---------remotelyBean=" + mRemotelyBean.toString());
        NetWorkManagement.newInstance()
                .postFeedBack(mRemotelyBean.getValues(),true, "DVR实时视频发布成功");
        yuv_push = YUV_PUSH_START;
        mYUVSendThread = new YUVSendThread(name);
        mYUVSendThread.start();
    }

    class YUVSendThread extends Thread{


        private RandomAccessFile yuyvFile;
        private MappedByteBuffer yuv_Map = null;
        private String mapFileName;


        public YUVSendThread(String name) {
            super();
            mapFileName =name;
        }

        @Override
        public void run() {
            super.run();
            int size = (int) (DEF_W * DEF_H * PIXEL);
            byte[] date = new byte[size];
            byte[] readyFlag = new byte[1];

            if(!CameraDataSync.getInstance().startCameraBlock(mapFileName)){
                CameraDataSync.getInstance().closeCameraDataDev();
                CameraDataSync.getInstance().stopCameraNoBlock();
                Log.d(TAG, " start CameraBlock fail!"  );
                return;
            }

            CameraDataSync.getInstance().openCameraDevice();

            try {
                yuyvFile =new RandomAccessFile(mapFileName, "rw");
                FileChannel fc = yuyvFile.getChannel();
                //size=该映射文件的总大小
                yuv_Map = fc.map(FileChannel.MapMode.READ_WRITE, 0, yuyvFile.length());
            }catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                 if(yuv_push == YUV_PUSH_STOP){
                     Log.d(TAG, " yuv_push = YUV_PUSH_STOP !"  );
                     CameraDataSync.getInstance().closeCameraDataDev();
                     CameraDataSync.getInstance().stopCameraNoBlock();
                     break;
                 }
                byte value = CameraDataSync.getInstance().readCameraDataDev();  //this is block read.....

                 if(value == 0x0f ){
                    Log.d(TAG, "  readyFlag----------- =" + value  );
                    videoFinish();
                    break;
                 }

                readyFlag[0] = yuv_Map.get(size);
                if(readyFlag[0] == 1) {
                    yuv_Map.position(0);
                    yuv_Map.get(date, 0, size);
                    RTCSendData(date);
                    yuv_Map.put(size,(byte)0);
                }
             }

            if (yuyvFile != null) {
                try {
                    yuyvFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(yuv_Map != null){
                yuv_Map.clear();
            }
        }

    }


    public String getMMapFile(){
        String name = mainFile;
        if(mRemotelyBean.getType().equals("1")){
            name = subFile;
        }else if(mRemotelyBean.getType().equals("2")){
            name = mainFile;
        }

        if(ish264ImgMap()){
            name = fourFlie;
        }
        return name;
    }


    boolean ish264ImgMap(){
        boolean ret =false;
        if (DvrPostImgManager.getInstans().isFourPlatform() ||
                DvrPostImgManager.getInstans().isTwoPlatform() ||
                DvrPostImgManager.getInstans().isOnePlatform()) {
            ret = true;
        }
        return ret;
    }



    /**
     * 监听房间变化
     */
    private RongRTCEventsListener rtcEventsListener = new RongRTCEventsListener() {
        @Override
        public void onKickedByServer() {

        }

        @Override
        public void onRemoteUserPublishResource(RongRTCRemoteUser rongRTCRemoteUser, List<RongRTCAVInputStream> list) {

        }

        @Override
        public void onRemoteUserAudioStreamMute(RongRTCRemoteUser rongRTCRemoteUser, RongRTCAVInputStream rongRTCAVInputStream, boolean b) {

        }

        @Override
        public void onRemoteUserVideoStreamEnabled(RongRTCRemoteUser rongRTCRemoteUser, RongRTCAVInputStream rongRTCAVInputStream, boolean b) {

        }

        @Override
        public void onRemoteUserUnpublishResource(RongRTCRemoteUser rongRTCRemoteUser, List<RongRTCAVInputStream> list) {

        }

        @Override
        public void onUserJoined(RongRTCRemoteUser rongRTCRemoteUser) {
            LogUtils.d("onUserJoined----------->" + rongRTCRemoteUser.getUserId());
         /*   dataList.add(rongRTCRemoteUser.getUserId());
            userList = new ArrayList<String>(new HashSet<String>(dataList));

            for (String data: userList){
                LogUtils.d(" onUserJoined user: .....>"  + data);
            }*/

        }

        @Override
        public void onUserLeft(RongRTCRemoteUser rongRTCRemoteUser) {
            LogUtils.d("...rongRTCRoom  用户离开房间...myRongRTCRoom=" + myRongRTCRoom);
            if(myRongRTCRoom != null){

                LogUtils.d("myRongRTCRoom.getRoomId---->" + myRongRTCRoom.getRoomId());

                LogUtils.d("...rongRTCRoom  用户离开房间...size=" + myRongRTCRoom.getRemoteUsers().size());

                LogUtils.d("...rongRTCRoom  关闭所有推流..");
            }
            videoFinish();
        }

        @Override
        public void onUserOffline(RongRTCRemoteUser rongRTCRemoteUser) {
            LogUtils.d("----------onUserOffline-----------");
            videoFinish();
        }

        @Override
        public void onVideoTrackAdd(String s, String s1) {
            LogUtils.d("----------onUserOffline-----------");
        }

        @Override
        public void onFirstFrameDraw(String s, String s1) {

        }

        @Override
        public void onLeaveRoom() {

        }

        @Override
        public void onReceiveMessage(Message message) {

        }
    };


   public synchronized void  videoFinish(){

        yuv_push = YUV_PUSH_STOP;
        joinRoomSucce =false;
        mYUVSendThread = null;

        try{
            Thread.sleep(200);
        }catch (Exception e){
            LogUtils.d("...need sleep same time to close camera...");
        }

        if(localUser != null) {
            localUser.unpublishAVStream(localUser.getDefaultAudioStream(), null);
           // if(mOutputStream != null) {  //maybe more fd no release
           //     localUser.unpublishAVStream(mOutputStream, null);
           //     mOutputStream= null;
           // }
            localUser =null;
        }

        if(myRongRTCRoom != null){
            if(CenterManager.getInstance().isInRoom()) {
                RongRTCEngine.getInstance().quitRoom(myRongRTCRoom.getRoomId(), new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {

                        LogUtils.d("...publishRyStream  退出房间.成功!!");

                    }

                    @Override
                    public void onUiFailed(RTCErrorCode rtcErrorCode) {
                        LogUtils.d("...publishRyStream  退出房间.失败...rtcErrorCode=" + rtcErrorCode);
                    }
                });
            }

            myRongRTCRoom.unregisterEventsListener(rtcEventsListener);
            myRongRTCRoom = null;
        }
        CarServiceClient.removeAccStatusInterface(this);
        if(mDogBase!=null){
            WatchdogManager.getInStance().unregisterDog(mDogBase);
        }
        nextCmd();
    }

      public void onAccStatusChanged(int status) {
          if (status == Config.System.ACC_OFF) {
              LogUtils.d(".TakeVideoCmd..ACC OFF !!");
              sendMesToApp(Config.MSG_TYPE_ACCOFF, "0", "ACC OFF");
              videoFinish();
          }else{
              videoFinish();
          }
      }

    @Override
    public boolean isSingleCmd() {
        return true;
    }

    @Override
    public void InterruptCmd() {
        super.InterruptCmd();
        videoFinish();
    }

    @Override
    public void FeedDogTimtOut() {
        LogUtils.d(".FeedDogTimtOut  !!");
        videoFinish();
    }

    public  void checkRoomIDAndFeedDog(String roomId){

        if(mDogBase != null ) {
            mDogBase.FeadDog();
        }
    }
}
