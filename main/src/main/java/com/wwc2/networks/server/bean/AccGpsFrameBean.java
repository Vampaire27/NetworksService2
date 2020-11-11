package com.wwc2.networks.server.bean;

import com.wwc2.networks.server.parse.ResultPaser;

public class AccGpsFrameBean {
    private final String cmd="0016";

    private  DataAction data;

    public AccGpsFrameBean() {
        data = new DataAction();
    }

    public void setData(int accs,int gtype,double lat,double lng,long tm){
        data.setAccs(accs);
        data.setGtype(gtype);
        data.setLat(formatSix(lat));
        data.setLng(formatSix(lng));
        data.setTm(tm);
    }

    public static double formatSix(double d) {
        return (double)Math.round(d*1000000)/1000000;
    }

    @Override
    public String toString() {
        String parsJson= ResultPaser.JsonParse(this);
        return parsJson;
    }

    public class DataAction{
        int accs;
        private int gtype ;
        private double lat;
        private double lng;
        private long tm;

        public DataAction() {
        }

        public void setAccs(int accs) {
            this.accs = accs;
        }

        public void setGtype(int gtype) {
            this.gtype = gtype;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public void setTm(long tm) {
            this.tm = tm;
        }
    }
}
