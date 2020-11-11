package com.wwc2.networks.server.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private static boolean isShow = true;
    private static Toast mToast = null;

    private ToastUtils() { }
    public static void showShort(Context context, CharSequence message) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            } else {
                mToast.setText(message);
            }
            mToast.show();
        }
    }
}
