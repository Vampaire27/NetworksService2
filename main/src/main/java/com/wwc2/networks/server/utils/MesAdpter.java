package com.wwc2.networks.server.utils;

import com.wwc2.networks.server.dvr.DvrPostImgManager;

public class MesAdpter {

    public  static String getCameraType(String type){
        String mType ;
        switch (type) {
            case "0":
                if (DvrPostImgManager.getInstans().isTwoPlatform()) {
                    mType = Config.DUAL_H264;
                } else if (DvrPostImgManager.getInstans().isOnePlatform()) {
                    mType = Config.FRONT_H264;
                } else {
                    mType = Config.QUART_H264;
                }
                break;
            case  "11":
                mType = Config.LEFT_DISPLAY;
                break;
            case  "12":
                mType = Config.RIGHT_DISPLAY;
                break;
            default:
                mType = type;
        }
        return mType;
    }
}
