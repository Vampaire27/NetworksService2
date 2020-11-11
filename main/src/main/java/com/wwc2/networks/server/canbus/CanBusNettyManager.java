package com.wwc2.networks.server.canbus;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.auto.AutoManagement;
import com.wwc2.networks.server.canbus.Candata.BaseCanBean;
import com.wwc2.networks.server.canbus.Candata.DataCan;
import com.wwc2.networks.server.netty.ClientConnect;
import com.wwc2.networks.server.netty.NettySendManager;
import com.wwc2.networks.server.parse.ResultPaser;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;


public class CanBusNettyManager {
    private static String TAG ="CanBusNettyManager";

    private static CanBusNettyManager mCanBusNettyManager;
    private static DataCan mDataCan;

    private VoltateObserver voltageObserver = new VoltateObserver(new Handler());
    private static final String AUTHORITY   = "com.wwc2.main.provider.logic";
    private static final String CUR_VOLTAGE = "cur_voltage";
    private final Uri uri_voltate           = Uri.parse("content://" + AUTHORITY + "/" + CUR_VOLTAGE);

    public final long INTERVAL_HOUR = 60 * 60 * 1000;

    public final long INTERVAL_DAY = 24* INTERVAL_HOUR;

    private static String[] mImmediatelycmds ={"voltage","tireLeftFront","tireRightFront","tireLeftRear","tireRightRear",
            "doorStatus"};

   private static int SPEED_LOW_LIMIT = 60;//Km
   private static int SPEED_CHANGER =10; //
   private float mNewSpeed = 50;

    private static final Double DEFAULT_CHANGE_REPORTING_THRESHOLD = 0.5; // 0.5v

    private  List<TimeBean> mTimeBean;

    private final class VoltateObserver extends ContentObserver {
        public VoltateObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Double voltage = getCurVoltate();
            //Cannot update Voltage alone. Voltage update too quickly!. 202000703
            if(mDataCan.getVoltage() != null ) {
                Double delta = mDataCan.getVoltage() - voltage;
                if (delta >= DEFAULT_CHANGE_REPORTING_THRESHOLD || delta <= DEFAULT_CHANGE_REPORTING_THRESHOLD) {
                    mDataCan.setVoltage(voltage);
                    mDataCan.setTime(System.currentTimeMillis() / 1000);
                    String jsonString = ResultPaser.JsonParse(mDataCan);
                    LogUtils.d("VoltateObserver 12----jsonString=" + jsonString);
                    postCanBusAction(jsonString);
                }
            }else{
                mDataCan.setVoltage(voltage);
                mDataCan.setTime(System.currentTimeMillis() / 1000);
                String jsonString = ResultPaser.JsonParse(mDataCan);
                LogUtils.d("VoltateObserver the first ----jsonString=" + jsonString);
                postCanBusAction(jsonString);
            }
        }
    }

    private Double getCurVoltate() {
        String voltate = CarServiceClient.getContext().getContentResolver().getType(uri_voltate);
        LogUtils.d("VoltateObserver----voltage=" + voltate);
        try {
            if (voltate != null) {
                int iVoltate = Integer.parseInt(voltate);
                return Double.valueOf(iVoltate / 10.0);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    private CanBusNettyManager() {

    }

    public void registerCanbus() {
        if (voltageObserver != null) {
            CarServiceClient.getContext().getContentResolver().unregisterContentObserver(voltageObserver);
            CarServiceClient.getContext().getContentResolver().registerContentObserver(uri_voltate, true, voltageObserver);
        } else {
            LogUtils.e("VoltateObserver----CanBusNettyManager");
        }
    }

    public  static CanBusNettyManager getInstance(){
        if(mCanBusNettyManager == null){
            mCanBusNettyManager = new CanBusNettyManager();

            mDataCan= new DataCan();
        }
        return mCanBusNettyManager;
    }


    boolean needImmediatelyWrite(String value){

        for(int i=0;i < mImmediatelycmds.length;i++){
            if(value.contains(mImmediatelycmds[i])){
                return true;
            }
        }
        return false;
    }

    boolean needTimeUpdateWrite(JSONObject object ){
        for(int i=0;i < mTimeBean.size();i++){
            TimeBean tmpTimeBean =mTimeBean.get(i);
            if(object.has(tmpTimeBean.key)) {
                if (System.currentTimeMillis() > tmpTimeBean.LastupdateTime + tmpTimeBean.interval) {
                    LogUtils.d(TAG, "needTimeUpdateWrite key= " + tmpTimeBean.key);
                    tmpTimeBean.setUpdateTime(System.currentTimeMillis());
                    return true;
                } else {
                    LogUtils.d(TAG, "System.currentTimeMillis() = " + System.currentTimeMillis());

                }
            }
        }
        return false;
    }


    boolean speedCheck(JSONObject object ){
        float speed=0;
        if(object.has("speed")){
            try {
                String speedString= object.getString("speed");
                speed = Float.parseFloat(speedString);
            }catch (Exception e){
                e.printStackTrace();
            }

            if(speed > SPEED_LOW_LIMIT
                    && Math.abs(speed - mNewSpeed) >= SPEED_CHANGER){
                mNewSpeed = speed;
                return true;
            }
        }
        return  false;
    }


    void postCanBusAction(String value){

        if(!AutoManagement.newInstance(CarServiceClient.getContext()).mUploadLevel.canSendGPS()) {
            return;
        }

        try {

            JSONObject object = new JSONObject(value);
            DataCan jsonRootBean = ResultPaser.paserObject(object, DataCan.class);
            BaseCanBean mBaseCanBean = new BaseCanBean();
            mBaseCanBean.setCmd(Config.System.CAN_INFO);
            jsonRootBean.setAccs(CarServiceClient.getAccStatus());
            jsonRootBean.setVoltage(getCurVoltate());
            mBaseCanBean.setData(jsonRootBean);

            String parsJson= ResultPaser.JsonParse(mBaseCanBean);

            if(ClientConnect.getInstance().getChannel() != null ) {
                ClientConnect.getInstance().getChannel().writeAndFlush(parsJson);
            }else{
                LogUtils.d(TAG,"getChannel() == null!" );
            }

        }catch (JSONException e){
            LogUtils.d("e-->" + e.getCause().toString());
            e.printStackTrace();
        }

    }

   public void postAccInfoByCanBusCMD(int accState){

        JSONObject object = new JSONObject();
        DataCan jsonRootBean = ResultPaser.paserObject(object, DataCan.class);

        BaseCanBean mBaseCanBean = new BaseCanBean();
        mBaseCanBean.setCmd(Config.System.CAN_INFO);
        jsonRootBean.setAccs(accState);
        mBaseCanBean.setData(jsonRootBean);
        String parsJson= ResultPaser.JsonParse(mBaseCanBean);

        if(ClientConnect.getInstance().getChannel() != null ) {
            ClientConnect.getInstance().getChannel().writeAndFlush(parsJson);
        }else{
            LogUtils.d(TAG,"getChannel() == null!" );
        }

    }


   public void postCanBusInfo(String value) {
       if (mTimeBean == null) {
           mTimeBean = new ArrayList<>();
           mTimeBean.add(new TimeBean("series", INTERVAL_DAY));
           mTimeBean.add(new TimeBean("version", INTERVAL_DAY));
           mTimeBean.add(new TimeBean("mileage", INTERVAL_DAY));
           mTimeBean.add(new TimeBean("mileageEndurance", INTERVAL_DAY));
           mTimeBean.add(new TimeBean("oil", INTERVAL_DAY));
       }
       try{
           JSONObject object = new JSONObject(value);

           if (needImmediatelyWrite(value)) {
               LogUtils.d(TAG, "SHARED_CAN_DATA  ImmediatelyWrite  -->" + value);
               postCanBusAction(value);
           } else if (needTimeUpdateWrite(object)){
               LogUtils.d(TAG, "SHARED_CAN_DATA needTimeUpdateWrite -->" + value);
               postCanBusAction(value);
           }else if(speedCheck(object)){
                postCanBusAction(value);
                LogUtils.d(TAG,"SHARED_CAN_DATA   speedCheck -->" + value);
           }else{
               LogUtils.d(TAG,"SHARED_CAN_DATA  no ImmediatelyWrite -->" + value);
           }
       }catch (JSONException e){
           LogUtils.d("e-->" + e.getCause().toString());
           e.printStackTrace();
       }

    }


    class TimeBean{
        private String key;
        private long LastupdateTime;
        private long interval;

        public TimeBean(String key, long interval) {
            this.key = key;
            this.interval = interval;
        }

        public void setUpdateTime(long time){
            LastupdateTime= time;
        }
    }

}