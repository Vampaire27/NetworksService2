package com.wwc2.networks.server.utils;

import com.wwc2.networks.server.bean.ConfigBean;

public class Config {

    public static final String SYS_PROTOCOL = "1";
    public static final String SYS_PROTOCOL_URL = "http://119.23.61.10:8082";
    public static final String SYS_INTERFACE = "2";
    public static final String SYS_INTERFACE_URL = "http://119.23.61.10:8081";
    public static final String APP_STORE = "3";
    public static final String APP_STORE_URL = "http://119.23.61.10:8081";
    public static final String APP_WEATHER = "4";
    public static final String APP_WEATHER_URL = "http://119.23.61.10:8083";
    public static final String URL_VERSION = "url_version";
    public static final String DEF_URL_VERSION = "1";
    public static final String APP_URL = "/uploadFiles/download.html?serNo=";
    public static final String APK_PATH = "/storage/emulated/0/.d_upgrade/.temp/";
    public static final String ERROR_FILE = "/custom/crash_last.txt";
    public static final String SHELL_FILE = "/custom/";
    public static final boolean DEBUG = true;
    public static final String APPS_STATUS = "appsStatus";
    public static final String JP_ID = "jp_id";
    public static final String LOG_STATUS = "log_status";
    public static final String SENSOR_LEVEL = "sensor_level";

    public static final String RY_APP_REGISTER ="ry_app";

    /**
     * 佳圣物联网
     */
    public final static String JS_URL = "http://weixin.gzjs020.com/index.php/";
    public final static String JS_QUERYCAR = "Openapi/querycarCard";
    public final static String JS_ISSET = "Openapi/issetjiashnegcard";
    public final static String JS_CARD_APPID = "jscarrefer";
    public final static String JS_CARD_APPKEY = "a85m5325sgrex0ca";
    public final static String JS_QUERY_REALNAME = "Openapi/isRealNameQueryIot";
    public final static String JS_CODE_KEY = "js_code_key";
    public final static String JS_CODE_DEF = "2";

    /**
     * 智网物联网
     */
    public final static String ZW_URL = "http://m2m.huidesichuang.com/";
    public final static String ZW_ISSET = "Api/Recharge/getIccidStatus";
    public final static String ZW_AGENTID = "1688";
    public final static long ZW_DEF_FLOW = 50L;

    public final static String ch010_project ="ch010";

    /**
     * sim卡实名
     */
    public final static String SIM_REALNAMEQUERY = "sim_realnamequery";

    //拍照
    public final static int TAKEPHOTO = 1;
    //实时预览
    public final static int TAKELIVE = 2;
    //回放列表
    public final static int VIDEOPAGE = 3;
    //回放数据
    public final static int VIDEOFILE = 4;
    //融云实时预览
    public final static int RY_TAKELIVE = 5;
    //碰撞
    public final static int SENSOR = 6;

    //拍照，1.5秒后检查结果
    public static final String CAPTURE_FILE_NAME = "wwc2.captrue.file.name";
    public static final String CAPTURE_MAIN_ACTION = "wwc2.main.capture.action";
    public static final String CAPTURE_SUB_ACTION = "wwc2.sub.capture.action";

    public static final String CAPTURE_START = "11";
    public static final String CAPTURE_RUNNING = "12";
    public static final String CAPTURE_STOP = "13";
    public static final String CAPTURE_FAIL = "99";
    public static final String CAPTURE_UNKONW = "100";


    public static final int GO_WAKEUP =1;
    public static final int GO_SLEEP =2;

    public static final int WAKEUP_TIME =20*1000; //20s

    public static final String TYPE_360 = "quart_360";
    /**
     * 协议
     */
    public class System {
        public static final String version = "v19-001";
        public static final String SYSTEM_URL = "/rest/data/upload";
        public static final String REMOTE_TIME_URL = "/rest/protocol/get_time";
        public static final String REMOTE_CARSET_URL = "/rest/protocol/get_carset";
        //版本信息
        public static final String UPLOAD_OS = "0001";
        public static final String UPLOAD_OS_OK = "9001";
        //基础信息
        public static final String UPLOAD_INFO = "0002";
        public static final String UPLOAD_INFO_OK = "9002";
        //行程信息
        public static final String UPLOAD_MILEAGE = "0003";
        public static final String UPLOAD_MILEAGE_OK = "9003";
        //报警信息
        public static final String UPLOAD_ALARM = "0004";
        public static final String UPLOAD_ALARM_OK = "9004";
        //三急信息
        public static final String UPLOAD_URGENT = "0005";
        public static final String UPLOAD_URGENT_OK = "9005";
        //完成通知
        public static final String UPLOAD_COMPLETE = "0801";
        public static final String UPLOAD_COMPLETE_OK = "9801";
        public static final int ACC_ON = 1;
        public static final int ACC_OFF = 0;


        public static final String LOGIN_CMD = "0010";

        public static final String CAN_INFO = "0012";
        //异常信息
        public static final String UPLOAD_ERROR = "0006";
        public static final String UPLOAD_ERROR_OK = "9006";

    }

    /**
     * 天气
     */
    public class Weather {
        public static final String APP_URL = "/rest/weather";
        public static final String LBS_URL = APP_URL + "/lbsInfo";
    }

    public class WaterCmd{

       public static final  String CMD_GET_ACC_LOCATION = "9016";
       public static final  String CMD_SEND_ACC_LOCATION = "0016";


    }
    /**
     * 接口
     */
    public class App {
        public static final String APP_URL = "/rest/protocol";
        public static final String REGISTER = APP_URL + "/register";
        public static final String GET_TOKEN = APP_URL + "/get_uploadToken";
        public static final String GET_RY_TOKEN = APP_URL + "/get_ryToken";
        public static final int IMAGE = 1;
        public static final int VIDEO = 2;
        public static final String UPLOAD_VIEW = APP_URL + "/upload_view";
        public static final String MCU_URL = APP_URL + "/get_mcuUrl";
        public static final String CAN_MCU_URL = APP_URL + "/get_canmcuUrl";
        public static final String APN_URL = APP_URL + "/get_apnUrl";
        public static final String APN_PATH = "/custom/";
        public static final String APN_FILE_NAME = "apns-conf.xml";
        public static final String MALL_REQUIRED = APP_URL + "/get_mallRequired";
        public static final String SYS_CONFIG = APP_URL + "/get_config";
        public static final String SYS_TIME = APP_URL + "/get_time";
        public static final String SIM_VERIFY = APP_URL + "/set_apkset";
        public static final String SYS_ADVER = APP_URL + "/get_adset";
        public static final String SYS_FEEDBACK = APP_URL + "/feedBack_jg";
        public static final String SYS_ACTIVATEDEVICE = APP_URL + "/activateDevice";
        public static final String SYS_NETWORK = APP_URL + "/add_network";
        public static final String SYS_SENSOR = APP_URL + "/set_collisionLevel";

        public static final String SYS_FEEDBACK_yr = APP_URL + "/feedBack_ry";
        public static final String RESULT_OK = "0";
        public static final String RESULT_NO = "1";
        public static final int RESULT_CODE = 200;

        public static final String clientPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALAuJ3IJ85edkz67" +
                "cAmR/c2m5M8pbX+EoyV0trD0/DUDhV0KmzmmkHE9mRoZ4CsByUMkWlJYa6tgyXKU" +
                "j4eKkgBplWQxw9vtO9Z+p+g77vjBn/6QItwyK18f2w2+9X0DuHXpFj4JHEK9a9Oi" +
                "7nnYasrWylE2kIK08LLBPW5Xk/cZAgMBAAECgYACQWUB4by7mSnUaMPja3oLUvKk" +
                "8EvVkRYhmZ/pHgnIh/YCHeukzkwVFwvJuMEU693uvR1m5njiZcfObrZzrsN1tnCj" +
                "Eg1evoLOb1KhOSsbhtBAVAeWlMfpYUy8O0FcP1zO5PHlXbkeyA/zWqIfq7UOjCwk" +
                "Z4NessFHGgvHCvMKvQJBAORQI4TIFJ2i11r+M6fGwGRztvneFZuUfMuoKrAi1SWd" +
                "6Bjwgd8avVB/eGwW4ET4/3DQYq6H6aJpj6RrTrFFWyMCQQDFi5SuEc9HzTCHuWe/" +
                "sGYoJiJGqA9miMyZu2vBwDHeZFDdBtKN8Ee8CE0AFDoflIBEgddycEamhVmhlkrB" +
                "b/aTAkB2TrCZFbTZGpDNrfvGbM81PmEKiXJY9FDWl4XHT8VzgKfMhJRHr+t50xPr" +
                "rs737q8vHkyEdBk0985fPNvCMFZRAkB2tRfVOlKK3RyqkplrsT7SJYg3WUpX3SDF" +
                "yMBKH4pYD4QgLbddH+Sbc4mB4KXE0GH7yOKEnD94VW21EjE96GANAkAn9+J513uR" +
                "7abpLpcGlbDoZGBmxUl2JmcdnI2lTqIiA3Mw4R/4vr/VUmpzmb5JjCyFgpb/odIZ" +
                "c90lvhk35JoZ";

        public static final String serverPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC9hMQDi1UT7SxyNOw2PshOVZOF" +
                "DMRwwqV59+HG6zK34Keze4jX2tXYhGyGjC1b9IpIABMmeNoHAn07rcGlsujHvvFY" +
                "t80qy5uKeCRxO95RWeHQbLfH0u4wAQzagCt7xstkBok2rO0qW0sncSFsIDrn2kmp" +
                "V7a0p2uVILe/BMAchwIDAQAB";
    }

    public class Local {
        public static final String SIM_ACTION = "wwc2_sim_auth_fail";
        public static final String SIM_CONFIG_PATH = "/system/etc/wwc2_sim_auth_config.ini";//SIM卡鉴权文件
        public static final String SIM_CONFIG_KEY = "wwc2_sim_auth";
        public static final String SIM_CONFIG_KEY_YES = "yes";
        public static final String SIM_CONFIG_KEY_ON = "on";
        public static final String SIM_PROVIDER_KEY = "sim_provider";//流量商提供标识
        public static final String SIM_PROVIDER_JS = "jiashen";//佳圣
        public static final String SIM_PROVIDER_EC = "ecar";
        public static final String SIM_ICCID_PATH = "/custom/iccid.ini";//保存ICCID文件
        public static final String SIM_JSSIM_PATH = "/custom/jssim.ini";//保存佳圣ICCID文件
        public static final String SIM_ICCID_KEY = "sim_iccid";
        public static final String SIM_ICCID_CODE = "sim_iccid_code";

        public static final String SIM_ALERT_PATH = "/custom/simalert.ini";//sim卡弹框文件
        public static final String SIM_SET_PATH = "/custom/setsim.ini";//打开还是关闭弹框1:开，0:关
        public static final String SIM_SET_KEY = "show";
        public static final String SIM_ALERT_COUNT = "count";
        public static final String SIM_ALERT_TIME = "time";
    }

    /**
     * 是否禁用
     * def = true.
     */
    public static final String IS_DISABLE = "isDisable";
    /**
     * 控制类型:图片
     */
    public static final String CONTROL_IMAGE = "1";
    /**
     * 控制类型:视频
     */
    public static final String CONTROL_VIDEO = "2";
    /**
     * 控制类型:远程启动
     */
    public static final String CONTROL_ACC = "3";
    /**
     * 控制类型:大灯
     */
    public static final String CONTROL_LIGHT = "4";
    /**
     * 控制类型:车门
     */
    public static final String CONTROL_DOOR = "5";
    /**
     * 控制类型:车窗
     */
    public static final String CONTROL_WINDOW = "6";
    /**
     * 控制类型:喇叭
     */
    public static final String CONTROL_SPEAKER = "7";
    /**
     * 控制类型:空调
     */
    public static final String CONTROL_AIRCONDITIONING = "8";
    /**
     * 控制类型:接人
     */
    public static final String CONTROL_MEET = "80";
    /**
     * 控制类型:一键导航
     */
    public static final String CONTROL_NAVIGATION = "81";
    /**
     * 控制类型:开
     */
    public static final String CONTROL_ON = "1";
    /**
     * 控制类型:关
     */
    public static final String CONTROL_OFF = "0";
    /**
     * 控制类型:成功
     */
    public static final String CONTROL_VALUE_SUCCESS = "1";
    /**
     * 控制类型:失败
     */
    public static final String CONTROL_VALUE_FAIL = "0";
    /**
     * 紧急类型：急加速
     */
    public static final int URGENT_ACCELERATE = 1;
    /**
     * 紧急类型：急减速
     */
    public static final int URGENT_DECELERATE = 2;
    /**
     * 紧急类型：急转弯
     */
    public static final int URGENT_TURN = 3;
    /**
     * 报警类型：胎压
     */
    public static final int ALARM_TPMS = 1;
    /**
     * 报警类型：水温
     */
    public static final int ALARM_WATER_TEMPERATURE = 2;
    /**
     * 报警类型：油量
     */
    public static final int ALARM_OIL_WARN = 3;
    /**
     * 报警类型：发动机
     */
    public static final int ALARM_ENGINE = 4;
    /**
     * 报警类型：车窗
     */
    public static final int ALARM_WINDOW = 5;
    /**
     * 报警类型：钥匙
     */
    public static final int ALARM_LOCK_OPENER = 6;
    /**
     * 报警类型：疲劳驾驶
     */
    public static final int ALARM_FATIGUE_DRIVING = 7;
    /**
     * 报警类型：碰撞
     */
    public static final int ALARM_COLLISION = 8;
    /**
     * 报警类型：主安全带
     */
    public static final int ALARM_SEAT_BELT = 9;
    /**
     * 报警类型：电子围栏
     */
    public static final int ALARM_GEOFENCE = 10;
    /**
     * 报警类型：超速
     */
    public static final int ALARM_SPEEDING = 11;

    /**
     * 默认行驶位置变化最小单位(米)
     */
    public static final int LOC_DISTANCE = 80;

    /**
     * 默认总里程最小单位(米)
     */
    public static final int TRACE_DISTANCE = 300;

    /**
     * 默认里程起步距离最小变化单位(米)
     */
    public static final int DIS_DISTANCE = 500;

    private static ConfigBean.DataBean traceConfig = null;//轨迹总里程最小单位配置参数
    private static ConfigBean.DataBean locConfig = null;//行驶位置变化最小单位配置参数
    private static ConfigBean.DataBean disConfig = null;//里程起步距离最小变化单位


    public static int FRONT_CAMERA = 1;
    public static int BACK_CAMERA  = 2;
    public static int LOCAL_VIDEO  = 3;

    /**capture_mode 拍照模式*/
    public static int DISABLE_CAPTURE = 0;
    public static int FRONT_CAPTURE   = 1;
    public static int BACK_CAPTURE    = 2;
    public static int LEFT_CAPTURE    = 3;
    public static int RIGHT_CAPTURE   = 4;
    public static int QUART_CAPTURE   = 5;
    public static int FOUR_CAPTURE    = 6;
    public static int DUAL_CAPTURE    = 7;//双路二合一，16：9压缩方式，同QUART_CAPTURE
    public static int TWO_CAPTURE     = 8;//双路二合一，32：9未压缩方式,同FOUR_CAPTURE

    public final static int AMAP_GPS = 1;
    public final static int AMAP_LBS = 2;
    public final static int AMAP_GAODE = 3;



    public static final String FRONT_DISPLAY ="1";
    public static final String BACK_DISPLAY ="2";
    public static final String LEFT_DISPLAY ="3";
    public static final String RIGHT_DISPLAY ="4";
    public static final String QUART_DISPLAY ="5";
    public static final String FOUR_DISPLAY ="6";

    /**h264_mode 直播模式*/
    public static final String DISABLE_H264    = "0";
    public static final String FRONT_H264      = "1";
    public static final String BACK_H264       = "2";
    public static final String LEFT_H264       = "3";
    public static final String RIGHT_H264      = "4";
    public static final String QUART_H264      = "5";
    public static final String FOUR_H264       = "6";
    public static final String DUAL_H264       = "7";//双路左右分屏
    public static final String UNKNOW_H264     = "10";
    public static final String START_H264      = "11";
    public static final String STOP_H264       = "12";

    public static String STR_LOCAL_VIDEO  = "3";

    // send mes to shouji app
    public static int MSG_TYPE_VIDEO_FINISH = 5;

    public static int MSG_TYPE_ACCOFF = 8;

    public static int MSG_TYPE_CAMERASUPPORT = 9;

    public static String CODE_PICTURE = "0";
    public static String CODE_VIDEO = "1";




    public static void setDisConfig(ConfigBean.DataBean bean) {
        Config.disConfig = bean;
    }

    public static int getDisConfig() {
        if (Config.disConfig == null) {
            return DIS_DISTANCE;
        }
        int trace = disConfig.getValue();
        if (trace <= 0) {
            return DIS_DISTANCE;
        }
        return trace;
    }

    public static void setTraceConfig(ConfigBean.DataBean bean) {
        Config.traceConfig = bean;
    }

    public static int getTraceConfig() {
        if (Config.traceConfig == null) {
            return TRACE_DISTANCE;
        }
        int trace = traceConfig.getValue();
        if (trace <= 0) {
            return TRACE_DISTANCE;
        }
        return trace;
    }

    public static void setLocConfig(ConfigBean.DataBean bean) {
        Config.locConfig = bean;
    }

    public static int getLocConfig() {
        if (Config.locConfig == null) {
            return LOC_DISTANCE;
        }
        int loc = locConfig.getValue();
        if (loc <= 0) {
            return LOC_DISTANCE;
        }
        return loc;
    }
}
