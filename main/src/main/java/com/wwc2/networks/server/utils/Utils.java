package com.wwc2.networks.server.utils;


import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Utils {

    public static String md5(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(password.getBytes());
            StringBuffer buffer = new StringBuffer();
            for (byte b : result) {
                int number = b & 0xff;
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(str);
            }
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setLocationMode(Context context, int mode) {
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        int currentMode = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
        intent.putExtra("CURRENT_MODE", currentMode);
        intent.putExtra("NEW_MODE", mode);
        context.sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, mode);
    }

    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(999);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static String resolves(String values) {
        if (values != null && !values.equals("")) {
            int t1 = values.lastIndexOf("(");
            int t2 = values.lastIndexOf(")");
            if (t1 >= 0 && t2 > 0) {
                String val = values.substring(++t1, t2);
                return val;
            }
        }
        return values;
    }

    public static String conversions(boolean aes, String[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i == 0)
                builder.append("(");
            builder.append(values[i]);
            if (i == (values.length - 1))
                builder.append(")");
            else
                builder.append(",");
        }
        return builder.toString();
    }

    public static boolean isNetwork(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                // 建立网络数组
                NetworkInfo[] net_info = cm.getAllNetworkInfo();
                if (net_info != null) {
                    for (int i = 0; i < net_info.length; i++) {
                        // 判断获得的网络状态是否是处于连接状态
                        if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean isCh004Js(Context context) {
        try {
            Uri uri_client = Uri.parse("content://com.wwc2.main.provider.logic/client_id");
            String strClient = context.getContentResolver().getType(uri_client);
            LogUtils.d("serial number is " + strClient);
            if (strClient == null || strClient.trim().length() == 0) {
                strClient = "";
            }
            return "ch004_js".equalsIgnoreCase(strClient);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

 //zhongyang.hu add for
    public static boolean isCameraSwitch(Context context) {
        boolean ret= false;
        try {
            Uri uri_client = Uri.parse("content://com.wwc2.main.provider.logic/client_id");
            String strClient = context.getContentResolver().getType(uri_client);
            LogUtils.d("serial number is " + strClient);
            if (strClient == null || strClient.trim().length() == 0) {
                strClient = "";
            }
            ret = "ch006_15".equalsIgnoreCase(strClient);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }


    public static String transformStamp(String time) {
        try {
            if (!TextUtils.isEmpty(time)) {
                return time.substring(0, 10);
            }
        } catch (Exception ex) {
            LogUtils.e(ex);
        }
        return time;
    }

    private static final String BREAK_LINE = "\n";
    private static final byte[] COMMAND_EXIT = "\nexit\n".getBytes();
    private static byte[] BUFFER = new byte[32];
    public static String execute(String... params) {
        Process process = null;
        StringBuilder sbReader = null;
        BufferedReader bReader = null;
        InputStreamReader isReader = null;
        InputStream in = null;
        InputStream err = null;
        OutputStream out = null;
        try {
            process = new ProcessBuilder()
                    .command(params)
                    .start();
            out = process.getOutputStream();
            in = process.getInputStream();
            err = process.getErrorStream();

            out.write(COMMAND_EXIT);
            out.flush();

            process.waitFor();

            isReader = new InputStreamReader(in);
            bReader = new BufferedReader(isReader);

            String s;
            if ((s = bReader.readLine()) != null) {
                sbReader = new StringBuilder();
                sbReader.append(s);
                sbReader.append(BREAK_LINE);
                while ((s = bReader.readLine()) != null) {
                    sbReader.append(s);
                    sbReader.append(BREAK_LINE);
                }
            }

            while ((err.read(BUFFER)) > 0) {}
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeAllStream(out, err, in, isReader, bReader);
            if (process != null) {
                processDestroy(process);
                process = null;
            }
        }
        if (sbReader == null) {
            return null;
        }else {
            return sbReader.toString();
        }
    }

    private static void closeAllStream(OutputStream out,
                                       InputStream err,
                                       InputStream in,
                                       InputStreamReader isReader,
                                       BufferedReader bReader) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (err != null) {
            try {
                err.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isReader != null) {
            try {
                isReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bReader != null) {
            try {
                bReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void killProcess(Process process) {
        int pid = getProcessId(process);
        if (pid != 0) {
            try {
                android.os.Process.killProcess(pid);
            } catch (Exception e) {
                try {
                    process.destroy();
                } catch (Exception ex) {
                }
            }
        }
    }

    private static int getProcessId(Process process) {
        String str = process.toString();
        try {
            int i = str.indexOf("=") + 1;
            int j = str.indexOf("]");
            str = str.substring(i, j);
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void processDestroy(Process process) {
        if (process != null) {
            try {
                if (process.exitValue() != 0) {
                    killProcess(process);
                }
            } catch (IllegalThreadStateException e) {
                killProcess(process);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static String getDates(){
        String sMonth;
        String sDay;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        if(month < 10){
            sMonth = "0"+ month;
        }else{
            sMonth = "" + month;
        }
        if(day < 10){
            sDay = "0"+ day;
        }else{
            sDay = "" + day;
        }
        String date = year + sMonth + sDay;
        LogUtils.d("Statistics--date=" + date);
        return date;
    }

    public static long getTime(long startTime, long endTime){
        long time = endTime - startTime;
        return time / 1000;
    }

    public static long getDelayMillis(int minute){
        return minute * 60 * 1000;
    }
}
