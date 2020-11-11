/**
 * Copyright 2016 JustWayward Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wwc2.networks.server.utils;

import android.content.Context;
import android.util.Log;

import com.wwc2.networks.CarServiceClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {
    private static Boolean LOG_SWITCH = true; // 日志文件总开关
    private static Boolean LOG_TO_FILE = true; // 日志写入文件开关
    private static String LOG_TAG = "CarNetworkService"; // 默认的tag
    private static char LOG_TYPE = 'v';// 输入日志类型，v代表输出所有信息,w则只输出警告...
    private static int FILE_MAX_SIZE = 1024 * 1024 * 10;//文件最大大小 10M
    private final static SimpleDateFormat LOG_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static String LOG_FILE_PATH; // 日志文件保存路径
    private static String LOG_FILE_NAME;// 日志文件保存名称

    public static void init(Context context) {
        LOG_FILE_PATH = CarServiceClient.getFilePath();
        LOG_FILE_NAME = "LOGS.txt";
    }

    public static void setLogStatus(boolean state) {
        LOG_TO_FILE = state;
    }

    public static String logFile() {
        return LOG_FILE_PATH + LOG_FILE_NAME;
    }

    public static String logFileName() {
        return LOG_FILE_NAME;
    }

    /****************************
     * Warn
     ****************************/
    public static void w(Object msg) {
        w(LOG_TAG, msg);
    }

    public static void w(String tag, Object msg) {
        w(tag, msg, null);
    }

    public static void w(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'w');
    }

    /***************************
     * Error
     ***************************/
    public static void e(Object msg) {
        e(LOG_TAG, msg);
    }

    public static void e(String tag, Object msg) {
        e(tag, msg, null);
    }

    public static void e(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'e');
    }

    /***************************
     * Debug
     ***************************/
    public static void d(Object msg) {
        d(LOG_TAG, msg);
    }

    public static void d(String tag, Object msg) {// 调试信息
        d(tag, msg, null);
    }

    public static void d(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'd');
    }

    /****************************
     * Info
     ****************************/
    public static void i(Object msg) {
        i(LOG_TAG, msg);
    }

    public static void i(String tag, Object msg) {
        i(tag, msg, null);
    }

    public static void i(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'i');
    }

    /**************************
     * Verbose
     **************************/
    public static void v(Object msg) {
        v(LOG_TAG, msg);
    }

    public static void v(String tag, Object msg) {
        v(tag, msg, null);
    }

    public static void v(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'v');
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     */
    private static void log(String tag, String msg, Throwable tr, char level) {
        if (LOG_SWITCH) {
            String logmsg = createMessage(msg);
            if ('e' == level && ('e' == LOG_TYPE || 'v' == LOG_TYPE)) { // 输出错误信息
                Log.e(tag, logmsg, tr);
            } else if ('w' == level && ('w' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.w(tag, logmsg, tr);
            } else if ('d' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.d(tag, logmsg, tr);
            } else if ('i' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.i(tag, logmsg, tr);
            } else {
                Log.v(tag, logmsg, tr);
            }
            if (LOG_TO_FILE) {
                try {
                    log2File(String.valueOf(level), tag, logmsg + tr == null ? "" : logmsg + Log.getStackTraceString(tr));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static String getFunctionName() {
        try {
            StackTraceElement[] sts = Thread.currentThread().getStackTrace();
            if (sts == null) {
                return null;
            }
            for (StackTraceElement st : sts) {
                if (st.isNativeMethod()) {
                    continue;
                }
                if (st.getClassName().equals(Thread.class.getName())) {
                    continue;
                }
                if (st.getFileName().equals("LogUtils.java")) {
                    continue;
                }
                return "[" + Thread.currentThread().getName() + "("
                        + Thread.currentThread().getId() + "): " + st.getFileName()
                        + ":" + st.getLineNumber() + "]";
            }
        }catch (Exception e){ }
        return null;
    }

    private static String createMessage(String msg) {
        String functionName = getFunctionName();
        String message = (functionName == null ? msg
                : (functionName + " - " + msg));
        return message;
    }

    /**
     * 打开日志文件并写入日志
     *
     * @return
     **/
    private synchronized static void log2File(String mylogtype, String tag, String text) {
        String dateLogContent = LOG_FORMAT.format(new Date()) + " " + text; // 日志输出格式
        if(LOG_FILE_PATH == null || LOG_FILE_PATH.equals("") || LOG_FILE_PATH.contains("null")){
            return;
        }
        try {
            File destDir = new File(LOG_FILE_PATH);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File file = new File(LOG_FILE_PATH, LOG_FILE_NAME);
            if(!file.exists()) {
                file.createNewFile();
            }

            long size = file.length();
            //大于10m直接删除
            if (size > FILE_MAX_SIZE) {
                file.delete();
            }
            FileWriter filerWriter = new FileWriter(file, true);
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(dateLogContent);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (Exception e) {
            i("log2File...e=" + e.toString());
//            .e=java.io.FileNotFoundException: /data/user/0/com.wwc2.networks/files/LOGS.txt (Too many open files)
        }
    }
}
