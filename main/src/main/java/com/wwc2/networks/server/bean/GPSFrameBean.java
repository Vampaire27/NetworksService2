package com.wwc2.networks.server.bean;


import com.wwc2.networks.server.parse.ResultPaser;
import com.wwc2.networks.server.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class GPSFrameBean {
    private final String cmd="0014";
    private List<GPSBean> data = new ArrayList<>();
    private TraceBean trip;
    private int acc;

    public GPSFrameBean() {
    }

    public void writeData(GPSBean lGpsBean){
        data.add(lGpsBean);
    }

    public boolean hasAvailableData(){
      if(data.size()>0){
         return true;
      }else{
          return false;
      }
    }

    public void clearData(){
        data.clear();
    }

    public void setTripIfo(TraceBean tb){
        trip = tb;
    }

    public void setAccStat(int stat){
        acc = stat;
    }

    @Override
    public String toString() {
        String parsJson= ResultPaser.JsonParse(this);
        return parsJson;
    }
}
