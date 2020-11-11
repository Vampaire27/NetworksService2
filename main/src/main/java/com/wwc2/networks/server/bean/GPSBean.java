package com.wwc2.networks.server.bean;

import com.wwc2.networks.server.parse.ResultPaser;

public class GPSBean {
    private int gtype ;
    private double lat;
    private double lng;
    private float speed;
    private float bear;
    private long tm;

    public GPSBean() {
    }

    public GPSBean(int gtype, double lat, double lng, float speed, float bear, long tm) {
        this.gtype = gtype;
        this.lat = lat;
        this.lng = lng;
        this.speed = speed;
        this.bear = bear;
        this.tm = tm;
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

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBear() {
        return bear;
    }

    public void setBear(float bear) {
        this.bear = bear;
    }

    public long getTm() {
        return tm;
    }

    public void setTm(long tm) {
        this.tm = tm;
    }

    @Override
    public String toString() {
        String parsJson= ResultPaser.JsonParse(this);
        return parsJson;
    }
}

