package com.wwc2.networks.server.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class AppUtils {

    public static boolean runApk(Context context, String strPacketName) {
        boolean ret = false;
        String strClassName = getAPKLaunchClassName(context, strPacketName);
        if(null != strClassName) {
            ret = runApk(context, strPacketName, strClassName, true);
        }
        return ret;
    }

    private static boolean runApk(Context context, String strPacketName,
                                 String strClassName, boolean noAnimation) {
        boolean ret = false;
        if(isAPKExist(context, strPacketName)) {
            try {
                String e = "";
                String className = "";
                Intent intent = new Intent("android.intent.action.MAIN");
                if(!isActivityExist(context, strPacketName, strClassName)) {
                    strClassName = null;
                }

                if(TextUtils.isEmpty(strClassName)) {
                    e = strPacketName;
                    className = getAPKLaunchClassName(context, strPacketName);
                } else {
                    e = strPacketName;
                    className = strClassName;
                }

                ComponentName comp = new ComponentName(e, className);
                intent.setComponent(comp);
                if(noAnimation) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NO_ANIMATION);
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                }

                intent.putExtra("package", e);
                intent.putExtra("class", className);
                context.startActivity(intent);
                ret = true;
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }
        return ret;
    }

    private static boolean isActivityExist(Context context, String strPacketName,
                                          String strClassName) {
        boolean ret = false;
        if(null != context && !TextUtils.isEmpty(strPacketName)
                && !TextUtils.isEmpty(strClassName)) {
            Intent intent = new Intent();
            intent.setClassName(strPacketName, strClassName);
            if(null != context.getPackageManager().resolveActivity(intent, 0)) {
                ret = true;
            }
        }

        return ret;
    }

    private static String getAPKLaunchClassName(Context context, String strPacketName) {
        String strClassName = null;
        if(null != context && null != strPacketName && !strPacketName.isEmpty()) {
            Intent launchIntent = context.getPackageManager().
                    getLaunchIntentForPackage(strPacketName);
            if (launchIntent != null) {
                strClassName = launchIntent.getComponent().getClassName();
            }
        }
        return strClassName;
    }

    public static boolean isAPKExist(Context context, String strPacketName) {
        if(strPacketName != null && !"".equals(strPacketName)) {
            if(null != context) {
                try {
                    context.getPackageManager().getApplicationInfo(strPacketName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
                    return true;
                } catch (PackageManager.NameNotFoundException var3) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 安装apk
     * @param path
     * @return
     */
    public static boolean install(String path) {
        String[] args = {"pm", "install", "-r", "-d", "-i", "com.wwc2.networks", "--user", "0", path};
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write("/n".getBytes());
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
            LogUtils.d("---utils install result=" + result);
            if (result != null && result.contains("Success")) {
                System.exit(0);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 获取App版本号
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App版本号
     */
    public static String getAppVersionName(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取App版本码
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App版本码
     */
    public static int getAppVersionCode(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean checkSysTime(String stime) {
        try {
            long rtime = Long.parseLong(stime);
            long ctime = System.currentTimeMillis() / 1000;
            long vtime = ctime - rtime;
            long day = (60 * 60) * 24;
            LogUtils.d("checkSysTime rtime:" + rtime + ",ctime:" + ctime + ",vtime:" + vtime + ",day:" + day);
            return Math.abs(vtime) <= day;
        } catch (Exception ex) {
            LogUtils.e(ex);
        }
        return false;
    }

    public static boolean checkAlertTime(String stime) {
        try {
            long rtime = Long.parseLong(stime);
            long ctime = System.currentTimeMillis();
            long vtime = (ctime - rtime) / 1000;
            int day = (int) (vtime / (24 * 3600));
            LogUtils.d("checkAlertTime rtime:" + rtime + ",ctime:" + ctime + ",vtime:" + vtime + ",day:" + day);
            return day >= 6;
        } catch (Exception ex) {
            LogUtils.e(ex);
        }
        return false;
    }

    public static String replaceCity(String city) {
        int start = city.indexOf(";");
        if (start >= 0) {
            city = city.substring(0, start);
        }

        start = city.indexOf("自治区");
        if (start < 0) {
            start = city.indexOf("省");
            if (start >= 0) {
                start += 1;
                city = city.substring(start);
            }
        } else if (start >= 0) {
            start += 3;
            city = city.substring(start);
        }

        int end = city.indexOf("市");
        if (end >= 0) {
            city = city.substring(0, end);
        }
        return city;
    }

    public static int parseInt(String value) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public static String noEmpty(String str) {
        return str == null ? "" : str;
    }

    public static boolean isApkForeground(Context context, String packageName) {
        boolean ret = false;
        if (null != context && !TextUtils.isEmpty(packageName) && isProcessRunning(context, packageName)) {
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            if (null != am) {
                List appProcesses = am.getRunningAppProcesses();
                if (null != appProcesses) {
                    Iterator mIterator = appProcesses.iterator();

                    while(mIterator.hasNext()) {
                        ActivityManager.RunningAppProcessInfo appProcess =
                                (ActivityManager.RunningAppProcessInfo)mIterator.next();
                        if (packageName.equals(appProcess.processName)) {
                            if (appProcess.importance == 100) {
                                ret = true;
                            } else {
                                ret = false;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static boolean isProcessRunning(Context context, String packageName) {
        boolean ret = false;
        if (!TextUtils.isEmpty(packageName) && null != context) {
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            if (null != am) {
                List infos = am.getRunningAppProcesses();
                if (null != infos) {
                    Iterator i$ = infos.iterator();

                    while(i$.hasNext()) {
                        ActivityManager.RunningAppProcessInfo rapi =
                                (ActivityManager.RunningAppProcessInfo)i$.next();
                        if (null != rapi && packageName.equals(rapi.processName)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

     public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }
}
