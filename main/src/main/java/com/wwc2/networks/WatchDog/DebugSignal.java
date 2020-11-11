package com.wwc2.networks.WatchDog;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.wwc2.networks.server.utils.LogUtils;

public class DebugSignal {
    private Context mContext;

    public DebugSignal(Context mContext) {
        this.mContext = mContext;
    }

    PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
        @Override
        public void onSignalStrengthsChanged(SignalStrength signal) {

            String mLteSignal = signal.toString();
           // LogUtils.d( "debugonSignalStrengthsChanged, signal:33034 " + mLteSignal);

        }

    };

    public void init(){
        TelephonyManager Tel = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        Tel.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY);
        LogUtils.d(" DebugSignal .init ");
    }

    public void destory(){
        TelephonyManager Tel = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(mPhoneStateListener, 0);
    }


}
