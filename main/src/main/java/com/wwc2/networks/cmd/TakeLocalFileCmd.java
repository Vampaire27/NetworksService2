package com.wwc2.networks.cmd;




import android.media.MediaRecorder;
import android.os.SystemProperties;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.FileUtils;
import com.wwc2.networks.server.utils.LogUtils;

import java.io.File;
import java.util.List;
import cn.rongcloud.rtc.CenterManager;
import cn.rongcloud.rtc.RTCErrorCode;
import cn.rongcloud.rtc.RongRTCConfig;
import cn.rongcloud.rtc.RongRTCEngine;
import cn.rongcloud.rtc.callback.JoinRoomUICallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.custom.OnSendListener;
import cn.rongcloud.rtc.events.RongRTCEventsListener;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.local.RongRTCAVOutputStream;
import cn.rongcloud.rtc.stream.remote.RongRTCAVInputStream;
import cn.rongcloud.rtc.user.RongRTCLocalUser;
import cn.rongcloud.rtc.user.RongRTCRemoteUser;
import io.rong.imlib.model.Message;

import static cn.rongcloud.rtc.RongRTCConfig.RongRTCVideoResolution.RESOLUTION_360_640;

public class TakeLocalFileCmd extends  SendMesCmd implements CarServiceClient.AccStatusInterface {

    private final String TAG = "TakeLocalFileCmd";

    private boolean mHasFinish = false;  //make sure onlyr finish one times.

    private RongRTCRoom myRongRTCRoom = null;

    private RongRTCLocalUser localUser;



    public TakeLocalFileCmd(RemotelyBean mRemotelyBean) {
        super(Config.RY_TAKELIVE, mRemotelyBean);
    }


    @Override
    public void exec() {

        int cnt =15;
        boolean itMount = false;

        while (cnt -- > 0 ) {
            if(FileUtils.isPathMounted(CarServiceClient.getContext(), mRemotelyBean.getUrl())){
                itMount =true;
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!itMount){
            LogUtils.d(TAG,"disk not mount =" + mRemotelyBean.getUrl());
            //提示后台......
            NetWorkManagement.newInstance()
                    .postFeedBack(mRemotelyBean.getValues(),
                            true, "U盘没有挂载！");
            nextCmd();

        }

        File mFile = new File(mRemotelyBean.getUrl());
        if(!mFile.exists()){
            LogUtils.d(TAG,"file not exists=" + mRemotelyBean.getUrl());
            //提示后台......
            NetWorkManagement.newInstance()
                    .postFeedBack(mRemotelyBean.getValues(),
                            true, "文件不存在！");
            nextCmd();
            return;

        }


        RongRTCConfig.Builder mBuilder= new RongRTCConfig.Builder();
        //mBuilder.videoProfile(RONGRTC_VIDEO_PROFILE_360P_15f_3);
        mBuilder.setVideoResolution(RESOLUTION_360_640);
        mBuilder.setMaxRate(1024);
        mBuilder.setMinRate(200);

//        mBuilder.setNoiseSuppressionLevel(RongRTCConfig.NSLevel.NS_VERYHIGH);
//        mBuilder.setEchoCancel(RongRTCConfig.AECMode.AEC_MODE2);
//        mBuilder.setNoiseSuppression(RongRTCConfig.NSMode.NS_MODE3);
//        mBuilder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
//        mBuilder.build();

        mBuilder.buildDefaultMode();

       // RongRTCCapture.getInstance().setRTCConfig(mBuilder.build());

        CarServiceClient.setAccStatusInterface(this);
        startRtc();

    }

    @Override
    public void nextCmd() {
           super.nextCmd();
    }

    private void startRtc(){

            if(CenterManager.getInstance().isInRoom() ){
                LogUtils.d(TAG,"...check...当前正在房间中,....");

                RongRTCRoom rr = CenterManager.getInstance().getRongRTCRoom();
                if(rr != null){
                    RongRTCEngine.getInstance().quitRoom(rr.getRoomId(), new RongRTCResultUICallBack() {
                        @Override
                        public void onUiSuccess() {
                            LogUtils.d(TAG,"..check 纠错.onUiSuccess..重新进入..");
                            joinRoom(mRemotelyBean.getRoomId());
                        }

                        @Override
                        public void onUiFailed(RTCErrorCode rtcErrorCode) {
                            LogUtils.d(TAG,"..check..纠错..异常  onUiFailed rtcErrorCode=" + rtcErrorCode);

                            //提示后台......
                            NetWorkManagement.newInstance()
                                    .postFeedBack(mRemotelyBean.getValues(),
                                            true, "进入房间失败");
                            videoFinish();
                        }
                    });
                }else{
                    resetApp();
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
                publishLocalVideo();

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



    private void publishLocalVideo(){

        localUser = myRongRTCRoom.getLocalUser();
        new RongRTCConfig.Builder().enableMicrophone(false);
        //发布音频流
        localUser.publishAVStream(localUser.getDefaultAudioStream(),new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                //TODO
                localUser.publishVideoFile(mRemotelyBean.getUrl(), true,false,new OnSendListener(){
                    @Override
                    public void onSendStart(RongRTCAVOutputStream rongRTCAVOutputStream) {
                        LogUtils.d(TAG,"publishDefaultAVStream 发布默认视频 onSendStart--->");
                        //本地视频发送成功
                        NetWorkManagement.newInstance()
                                .postFeedBack(mRemotelyBean.getValues(),true, "本地视频发布成功");
                    }

                    @Override
                    public void onSendComplete(RongRTCAVOutputStream rongRTCAVOutputStream) {
                        sendMesToApp(Config.MSG_TYPE_VIDEO_FINISH,"0","播放完毕"); //通知APP播放视频完毕
                        LogUtils.d(TAG,"publishDefaultAVStream  默认视频播放完毕");
                        videoFinish();

                    }

                    @Override
                    public void onSendFailed() {
                        LogUtils.d(TAG,"onSendFailed--->");
                        SystemProperties.set("media.extractor.restart", "1");
                        resetApp();
                        localUser.unPublishVideoFile(null);
                        videoFinish();
                    }

                });
            }

            @Override
            public void onUiFailed(RTCErrorCode errorCode) {
                LogUtils.d(TAG,"onUiFailed..--->");
            }
        });
    }


    private void resetApp(){
        LogUtils.d(TAG,"onSendFailed  killProcess .....");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
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
            LogUtils.d(TAG,"onUserJoined----------->" + rongRTCRemoteUser.getUserId());
         /*   dataList.add(rongRTCRemoteUser.getUserId());
            userList = new ArrayList<String>(new HashSet<String>(dataList));

            for (String data: userList){
                LogUtils.d(" onUserJoined user: .....>"  + data);
            }*/

        }

        @Override
        public void onUserLeft(RongRTCRemoteUser rongRTCRemoteUser) {
            LogUtils.d(TAG,"...rongRTCRoom  用户离开房间...myRongRTCRoom=" + myRongRTCRoom);
            if(myRongRTCRoom != null){
                LogUtils.d(TAG,"myRongRTCRoom.getRoomId---->" + myRongRTCRoom.getRoomId());
                LogUtils.d(TAG,"...rongRTCRoom  用户离开房间...size=" + myRongRTCRoom.getRemoteUsers().size());
            }
            videoFinish();
        }

        @Override
        public void onUserOffline(RongRTCRemoteUser rongRTCRemoteUser) {
            LogUtils.d(TAG,"----------onUserOffline-----------");
            videoFinish();
        }

        @Override
        public void onVideoTrackAdd(String s, String s1) {
            LogUtils.d(TAG,"----------onUserOffline-----------");
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


    public void  videoFinish(){

        if(mHasFinish){
            return;
        }
        mHasFinish =true;
        LogUtils.d(TAG,"video... Finish begin . mHasFinish ");
        if (localUser != null) {
            localUser.unPublishVideoFile(null);
            localUser.unpublishAVStream(localUser.getDefaultAudioStream(), null);
            localUser = null;
        }

        if (myRongRTCRoom != null) {
            if (CenterManager.getInstance().isInRoom()) {
                RongRTCEngine.getInstance().quitRoom(myRongRTCRoom.getRoomId(), new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {

                        LogUtils.d(TAG, "...publishRyStream  退出房间.成功!!");

                    }

                    @Override
                    public void onUiFailed(RTCErrorCode rtcErrorCode) {
                        LogUtils.d(TAG, "...publishRyStream  退出房间.失败...rtcErrorCode=" + rtcErrorCode);
                    }
                });
            }

            myRongRTCRoom.unregisterEventsListener(rtcEventsListener);
            myRongRTCRoom = null;
        }
        CarServiceClient.removeAccStatusInterface(this);
        LogUtils.d(TAG, "video... Finish end ");

        nextCmd();
    }

      public void onAccStatusChanged(int status) {
          if(status == Config.System.ACC_OFF) {
              LogUtils.d(TAG,".TakeVideoCmd..ACC OFF !!");
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

}
