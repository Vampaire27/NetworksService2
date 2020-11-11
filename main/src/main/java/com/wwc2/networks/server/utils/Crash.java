package com.wwc2.networks.server.utils;

import android.content.Context;

import com.wwc2.networks.server.wakeup.AccPowerManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

public class Crash implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler mDefaultHandler;
    private static Crash INSTANCE = new Crash();
//    private Context mContext;
    private Crash() { }
    public static Crash getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
//        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        LogUtils.d( collectCrashInfo(ex));
        //hzy add for when app crash ,maybe cannot sleep..
        AccPowerManager.getInstance().releaseWakeLockLocked();
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            LogUtils.d("uncaughtException ex:" + ex);
//            try {
//                Thread.sleep(5000);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
//            } catch (Exception e) { }
        }
    }

    private String collectCrashInfo(Throwable ex) {
        if (ex == null) return "";

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable throwable = ex.getCause();
        while (throwable != null) {
            throwable.printStackTrace(printWriter);
            throwable = throwable.getCause();//逐级获取错误信息
        }
        String crashInfo = writer.toString();
        LogUtils.i("bqt", "【错误信息】" + crashInfo);
        printWriter.close();
        return crashInfo;
    }

    public boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        return true;
    }
}
