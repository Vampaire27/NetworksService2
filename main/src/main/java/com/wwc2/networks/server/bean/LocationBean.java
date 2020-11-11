package com.wwc2.networks.server.bean;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2019/11/23.
 * emai: wpeng@waterworld.com.cn
 */
public class LocationBean {


    /*
         指令类型0002
     */
    private String cmd;
    /*
        当前协议版本
     */
    private String ver;
    /*
        1:GPS 2:基站 3:高德
     */
    private int gtype;
    /*
        纬度
     */
    private double lat;
    /*
        经度
     */
    private double lng;
    /*
     速度 km/h
     */
    private double sp;
    /*
        方向角
     */
    private double bear;
    /*
        时间
     */
    private long tm;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public int getGtype() {
        return gtype;
    }

    public void setGtype(int gtype) {
        this.gtype = gtype;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getSp() {
        return sp;
    }

    public void setSp(double sp) {
        this.sp = sp;
    }

    public double getBear() {
        return bear;
    }

    public void setBear(double bear) {
        this.bear = bear;
    }

    public long getTm() {
        return tm;
    }

    public void setTm(long tm) {
        this.tm = tm;
    }
}
