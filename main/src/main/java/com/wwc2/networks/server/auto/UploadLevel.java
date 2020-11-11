package com.wwc2.networks.server.auto;

import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.LogUtils;

public class UploadLevel {

    public static final String UPDATE_LEVEL = "updateLevel";
    //    status 为1时 说明设备正常
//    status 为2时 说明设备无效  即gps只上传一次 里程不需要上传   注册接口会在点火时触发
//    status 为3时 说明设备被拉黑  即点火后不再请求注册接口 也不再上传gps和里程  但是天气和应用商店 以及其他可以使用  全部使用本地的配置
//    status 为4时 说明设备被禁止 即carnetwork 无效 这个时候和carnetwork相关的天气 应用商店都不能使用 即本身carnetwokr 完全无效
    private static int LEVEL_NORMAL  = 1;
    private static int LEVEL_INVALID  = 2;
    private static int LEVEL_BLACKLIST  = 3;
    private static int LEVEL_FORBIDDEN  = 4;

    private static int LEVEL_UNDEFINE  = 55;

    private int infoLevel = LEVEL_UNDEFINE;



    private boolean updateOnetime = true;  //for  LEVEL_INVALID user to upload gps one times.


    public int getInfoLevel() {
        if(infoLevel == LEVEL_UNDEFINE){
            infoLevel = (int) SPUtils.get(CarServiceClient.getContext(), UPDATE_LEVEL,LEVEL_NORMAL);
        }
        return infoLevel;
    }

    public void setInfoLevel(int infoLevel) {
        LogUtils.d(" set infoLevel : =" + infoLevel);
        this.infoLevel = infoLevel;
        SPUtils.put(CarServiceClient.getContext(),
                UPDATE_LEVEL, infoLevel);
        resetGPSUpload();
    }

    public void resetGPSUpload(){
        updateOnetime = true;
    }

    public boolean canSendTrace(){
        boolean ret = true;
        if(getInfoLevel() >= LEVEL_INVALID ){
            ret = false;
        }
        LogUtils.d(" infoLevel : can SendTrace  =" + ret);
        return ret;
    }

    public boolean canSendGPS(){
        boolean ret = true;
        if(getInfoLevel() >= LEVEL_BLACKLIST){
            ret = false;
        }else if(getInfoLevel() >= LEVEL_INVALID && !updateOnetime){
            ret = false;
        }
        updateOnetime =false;
        LogUtils.d(" infoLevel : can SendGPS  =" + ret);
        return ret;
    }


    public boolean isOnlyRegisterByService(){
        boolean ret = false;
        if(getInfoLevel() >= LEVEL_BLACKLIST){
            ret = true;
        }
        LogUtils.d(" infoLevel : isOnlyRegister ByService  =" + ret);
        return ret;
    }

    public boolean isDisableByService(){
        boolean ret = false;
        if(getInfoLevel() == LEVEL_FORBIDDEN){
            ret = true;
        }
        LogUtils.d(" infoLevel : isDisable ByService  =" + ret);
        return ret;
    }


}
