package com.wwc2.networks.server.canbus;

import android.os.Bundle;

import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.networks.server.canbus.uilistener.uiListener_CarInfo;
import com.wwc2.networks.server.internet.NetWorkManagement;
import com.wwc2.networks.server.utils.LogUtils;

public class CarInfoListener implements uiListener_CarInfo {

    private final String TAG = "CarInfoListener";

    @Override
    public void CarInfoChange(int nId, Bundle bundle) {
        if (nId == CanBusDefine.PARAMETER_STRING) {
            String key = bundle.getString(CanBusDefine.Parameter.PARAMETER_STRING.value());
            String value = bundle.getString(key);
            //CarInfoDefine.Text text = CarInfoDefine.Text.valueOf(key);
            LogUtils.d(TAG,  "---key=" + key + ", value=" + value);
            if("SHARED_CAN_DATA".equals(key)){
                CanBusNettyManager.getInstance().postCanBusInfo(value);
            }

        }
    }
}
