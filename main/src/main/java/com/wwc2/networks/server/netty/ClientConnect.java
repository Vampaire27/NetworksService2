package com.wwc2.networks.server.netty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.CarSystemServer;
import com.wwc2.networks.server.bean.AccGpsFrameBean;
import com.wwc2.networks.server.bean.ZWCardBean;
import com.wwc2.networks.server.bean.heart.HeartLoginBean;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.location.GpsPointManager;
import com.wwc2.networks.server.parse.ResultPaser;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.AppUtils;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class ClientConnect {
    /**
     * 尝试连接长连接的次数
     */
    public static AtomicInteger tryConnect = new AtomicInteger(0);

    static ClientConnect mClientConnect;

    private ReConnectThread mReConnectThread = null;

    Bootstrap bootstrap = null;
    public String TAG = "nettyConnent";

    private ClientHandler mClientHandler;

    private CarSystemServer mCarSystemServer;

    /**
     *  默认10分钟
     */

    private final int RE_CLIENT_TIME = 10000;
    private final int port = 10083;
    private final String ip= "47.107.45.116";
    private Channel channel = null;

    private int RECONNENT_TIME = 10;

    private int  connectIndex =0;

    private static String version="33049";

    private String beatjson = "{\"cmd\":\"0011\",\"version\":\""+version+"\"}";

    public Channel getChannel(){
        return  channel;
    }

    private M mMesHandler = new M();

    private static final String CMD_YUNRONG_ACK = "9015";

    private static final String CMD_GET_ACC_LOCATION_ACK = "9016";
    private static final String CMD_SEND_ACC_LOCATION = "0016";


    public static ClientConnect getInstance(){
        if(mClientConnect == null){
            mClientConnect =new ClientConnect();
        }
        return  mClientConnect;
    }

    public void ResetLoginTime(){
        if(mClientHandler != null ){
            mClientHandler.resetLoginTime();
        }
    }


    synchronized  public void  createClient( CarSystemServer carSystemServer){
        if(carSystemServer != null) {
            mCarSystemServer = carSystemServer;
        }
        LogUtils.d(TAG,"创建连接......");
        if(bootstrap == null) {
            bootstrap = new Bootstrap();
            mClientHandler = new ClientHandler(ClientConnect.this);
            bootstrap.group(new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("idleStateHandler",
                                    new IdleStateHandler(0, CommonHeartManager.getInstance().CLIENT_TIME+100, 0));
                            pipeline.addLast("frameDecoder",
                                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
                                            4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                            pipeline.addLast("handler", mClientHandler);
                        }
                    });
        }
        try {
            final ChannelFuture future= bootstrap.connect(ip, port).sync();
            if(future.isSuccess()){
                LogUtils.d(TAG,"longlinkIsSuccess");
            }else{
                LogUtils.d(TAG,"longlinkIsError");
            }
            channel = future.channel();
            ClientLogin();
            // 等待客户端链接关闭
            /*future.channel().closeFuture().sync(); 不能使用 会导致剩下的流程不会走
            LogUtils.d(TAG,"longlinkIsShutdown");*/
        } catch (Exception e) {
            LogUtils.d(TAG,"connect 111 server 连接失败");
            NetworkUtil.showNetworkLog();
            e.printStackTrace();
            try {
                Thread.sleep(10000);
                reConnect();
            }catch (Exception e1){
                LogUtils.d(TAG,"reConnect 222 server 连接失败");
                NetworkUtil.showNetworkLog();
                e1.printStackTrace();
            }
        }
    }


   public void closeClient(){
        if (channel != null) {
        //if (channel != null && channel.isOpen()) {
           LogUtils.d(TAG,"channel close ->"  +channel.localAddress()) ;
           channel.close();
           channel = null;
        }
    }

    /**
     * 客户端发送注册登录0010指令
     */
    public  void ClientLogin(){
        DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        String loginJson = ResultPaser.JsonParse(new HeartLoginBean(device.getSerialNumber(), Config.System.LOGIN_CMD,System.currentTimeMillis() / 1000,CommonHeartManager.version));
        LogUtils.d(TAG,"createClient--loginJson->" + loginJson +" address = " +channel.localAddress()) ;
        //注册
        channel.writeAndFlush(loginJson );//注册码,序列号
    }



    private class ReConnectThread extends Thread {
        private boolean isInterrupted ;
        public ReConnectThread( String name) {
            super(name);
            isInterrupted =false;
        }

        @Override
        public void interrupt() {
            isInterrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            LogUtils.d(TAG,"Thread tryConnect:"+ tryConnect.incrementAndGet() );
            if(tryConnect.get()>1000){
                tryConnect = new AtomicInteger(0);
            }
            while(!isInterrupted){
                if( connectIndex++ > RECONNENT_TIME){
                    LogUtils.d(TAG,"重连失败......connectIndex:"+connectIndex );
                    break;
                }
                createClient(null);
                LogUtils.d(TAG,"重连中......connectIndex:" + connectIndex);

                try {
                    Thread.sleep(RE_CLIENT_TIME);
                    LogUtils.d(TAG,"Thread.sleep:" + RE_CLIENT_TIME +" ms over");
                }catch (InterruptedException e){
                    LogUtils.d(TAG,"Thread.sleep Exception");
                    isInterrupted =true;
                }
            }
            mReConnectThread =null;
        }
    }


    public void startChannelRead(){
        if(mReConnectThread != null) {
            mReConnectThread.interrupt();
        }
        LogUtils.d(TAG,"开始维护心跳..........");
        CommonHeartManager.getInstance().startHeart();
    }


    public void reConnect(){

        //frist stop heartbeat  thread.
        //stop heartbeat

        if(mReConnectThread == null){
            mReConnectThread = new ReConnectThread("ReConnect-Thread");
            mReConnectThread.start();
        }else{
            LogUtils.d(TAG,"ReConnectThread is not stop ......");
        }

    }



    public void clearConnectTime(){
        connectIndex =0;
    }

    public void doMes(JSONObject jsonObject){
        try {
            String value =  jsonObject.getString("cmd");
            if(CMD_YUNRONG_ACK.equals(value)){ // 9015 is rongyun token info ..;
                JSONObject data = jsonObject.getJSONObject("data");
                 String token =  data.getString("token");
                LogUtils.d("hzy doMes   token =" + "token");
                 if(token != null && !"unbind".equals(token)){
                     SPUtils.put(CarServiceClient.getContext(),
                             Config.RY_APP_REGISTER, true);
                     mCarSystemServer.rongYunInitAndConnect(token);
                 }
            }else if(CMD_GET_ACC_LOCATION_ACK.equals(value)){ // 9016 is set.;
               Message message = new Message();
               message.what = M.CMD_GET_ACC_LOCATION;
               mMesHandler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG," doMessage  jsonObject=" + jsonObject);
    }


  private class M extends Handler{
        public static final int CMD_MES =1;
        public static final int   CMD_GET_ACC_LOCATION =1;
     @Override
     public void handleMessage(Message msg) {
         LogUtils.d(TAG," doMessage  msg=" + msg.what);
         super.handleMessage(msg);
         switch (msg.what){
             case CMD_GET_ACC_LOCATION:
                 AccGpsFrameBean accGpsFrameBean = new AccGpsFrameBean();
                 accGpsFrameBean.setData(CarServiceClient.getAccStatus(),
                         GpsPointManager.getInstance().getGPSBean().getGtype(),
                         GpsPointManager.getInstance().getGPSBean().getLat(),
                         GpsPointManager.getInstance().getGPSBean().getLng(),
                         GpsPointManager.getInstance().getGPSBean().getTm());
                 String sendStr = accGpsFrameBean.toString();
                 LogUtils.d(TAG," doMessage  sendStr =" +sendStr);
                 NettySendManager.getInstance().send(sendStr);
                 break;
             default:
                 break;

         }

     }
 }

}