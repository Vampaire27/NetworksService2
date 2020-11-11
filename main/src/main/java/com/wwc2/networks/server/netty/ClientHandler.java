package com.wwc2.networks.server.netty;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.auto.AutoManagement;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.utils.LogUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<String>{

    public static final AttributeKey<Boolean> LOGIN_FLAG = AttributeKey.valueOf("LOGIN_FLAG");
    public String TAG = "nettyConnent";
    /**
     * 当前登录次数
     */
    public int login_time =0;
    /**
     * 最大登录次数
     */
    private final int LOGIN_TIME_MAX = 20;

    private ClientConnect mClientConnect;


	public ClientHandler(ClientConnect connect){
        mClientConnect = connect;
    }

	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        LogUtils.d(TAG,"userEventTriggered 10 then channelInactive");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                LogUtils.d(TAG," Write idle, channel will be closed");

                LogUtils.d(TAG,"...重新登录.0...");
//                DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
//                ctx.writeAndFlush(LoginPackage + device.getSerialNumber());
                if(login_time < LOGIN_TIME_MAX) {
                    mClientConnect.ClientLogin();
                }else{
                    LogUtils.d(TAG," userEventTriggered, login_time > 20");
                }
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext var1) throws Exception {
        super.channelRegistered(var1);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    /**
     * 客户端收到消息
     * @param ctx
     * @param msg
     */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        LogUtils.d(TAG,"client channelRead0 get msg"+msg+ "address = " + ctx.channel().localAddress()) ;

        JSONObject object = new JSONObject(msg);
        String value =  object.getString("cmd");

        if(mClientConnect.getChannel() == null){
            LogUtils.d(TAG,"channel Inactive this Channel has been close");
            return;
        }

        //DeviceManagement device = DeviceManagement.newInstance(CarServiceClient.getContext());
        if (ctx.channel().attr(LOGIN_FLAG).get() == null) {
            if (!value.equals("9010")) {
                LogUtils.d(TAG,"...重新登录.1...");
                // 重新登录
               // ctx.writeAndFlush(LoginPackage + device.getSerialNumber());
                if(login_time++ < LOGIN_TIME_MAX) {
                    mClientConnect.ClientLogin();
                }else{
                    LogUtils.d(TAG," channelRead0, login_time > 10");
                }
                return;
            }
        } else {
            if (!ctx.channel().attr(LOGIN_FLAG).get()) {
                LogUtils.d(TAG,"...重新登录.2...");
                // 重新登录
                //ctx.writeAndFlush(LoginPackage + device.getSerialNumber());
                if(login_time++ < LOGIN_TIME_MAX) {
                       mClientConnect.ClientLogin();
                }else{
                    LogUtils.d(TAG," channelRead0 . 2, login_time > 10");
                }
                return;
            }
        }

        //注册成功
        if (value.equals("9010")) {
            ctx.channel().attr(LOGIN_FLAG).set(true);
            LogUtils.d(TAG,"...client 注册成功... address = " + ctx.channel().localAddress()) ;
            //开始维持心跳,3分钟/次
            if(mClientConnect != null){
                mClientConnect.startChannelRead();
            }
        }else{
            mClientConnect.doMes(object);
        }

	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.d(TAG,"exceptionCaught =" + String.format("[%s] %s", cause.getMessage()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	super.channelInactive(ctx);
        LogUtils.d(TAG,"channel channelInactive  address ：" +ctx.channel().localAddress()) ;
    	if(mClientConnect.getChannel() == null){
            LogUtils.d(TAG,"channel Inactive this Channel has been close");
            return;
        }
        if(!AutoManagement.newInstance(CarServiceClient.getContext()).mUploadLevel.canSendGPS()) {
            return;
        }
        try {
            LogUtils.d(TAG,"mClientConnect.getChannel().id =" +  mClientConnect.getChannel().id()  + " address ="+mClientConnect.getChannel().localAddress());
            LogUtils.d(TAG,"ctx.channel().id=" +  ctx.channel().id() + " address = " + ctx.channel().localAddress()) ;
            if(mClientConnect.getChannel().id()== ctx.channel().id() ) {
                //重连10次
                ctx.channel().attr(LOGIN_FLAG).set(false);
                mClientConnect.reConnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void resetLoginTime(){
        mClientConnect.clearConnectTime();
        login_time = 0;
    }
}
