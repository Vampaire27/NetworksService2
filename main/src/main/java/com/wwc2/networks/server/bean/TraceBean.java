package com.wwc2.networks.server.bean;

import com.wwc2.networks.server.parse.ResultPaser;

public class TraceBean {

    private final String cmd ="0003";

    //开始经纬度
    private int sGtype ;
    private double slat;
    private double slng;

    //结束经纬度
    private int eGtype ;
    private double elat;
    private double elng;

    //空气质量
    private int air;

    // 累计油耗
    private Long sumfuel;  //Double L

    private Long sumTime;  //Int s秒

    //平均速度
    private Float aSpeed;  //km/h

    //累计里程
    private Float sumTrace;  // m  :修改服务器数据km->m

    //最大速度
    private Float maxSpeed;  //int km/h

    //结束时间
    private Long endTime;    // s

    private int tripId;

    public TraceBean() {
        this.maxSpeed =0f;
        this.sumTrace =0f;
    }

    public void setsGPS(int sGtype, double slat, double slng) {
        this.sGtype = sGtype;
        this.slat = slat;
        this.slng = slng;
    }


    public void seteGtype(int eGtype,double elat,double elng) {
        this.eGtype = eGtype;
        this.elat = elat;
        this.elng = elng;
    }


    public void setSumfuel(Long sumfuel) {
        this.sumfuel = sumfuel;
    }

    public void setSumTime(Long sumTime) {
        this.sumTime = sumTime;
    }

    public void setaSpeed(Float aSpeed) {
        this.aSpeed = aSpeed;
    }


    public void sumTrance(float distance){
        sumTrace += distance;
    }

    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public Float getSumTrace() {
        return sumTrace;
    }

    public Long getSumTime() {
        return sumTime;
    }

    public Float getMaxSpeed() {
        return maxSpeed;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    @Override
    public String toString() {
        String parsJson= ResultPaser.JsonParse(this);
        return parsJson;
    }
}
