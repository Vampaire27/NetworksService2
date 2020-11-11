package com.wwc2.networks.server.bean;

public class ViewBean extends AMapBean{

    private int type;
    private int allTime;
    private double size;
    private String url;
    //1前视2后视
    private String mirrorType;

    @Override
    public String toString() {
        return "ViewBean{" +
                "type=" + type +
                ", allTime=" + allTime +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", mirrorType='" + mirrorType + '\'' +
                '}';
    }

    public String getMirrorType() {
        return mirrorType;
    }

    public void setMirrorType(String mirrorType) {
        this.mirrorType = mirrorType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAllTime() {
        return allTime;
    }

    public void setAllTime(int allTime) {
        this.allTime = allTime;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
