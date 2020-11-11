package com.wwc2.networks.server.bean;

public class AMapBean extends BaseBean {
    public final static int AMAP_GPS = 1;
    public final static int AMAP_LBS = 2;
    public final static int AMAP_GAODE = 3;

    private String tripId;
    private int located;
    private int lngLoc;
    private double lng;
    private int latLoc;
    private double lat;
    private float speed;
    private float bearing;
    private String address;
    private int gpstype = AMAP_GPS;//1:GPS,2:基站,3:高德
    private double gpslng;
    private double gpslat;

    @Override
    public String toString() {
        return "AMapBean{" +
                "tripId='" + tripId + '\'' +
                ", located=" + located +
                ", lngLoc=" + lngLoc +
                ", lng=" + lng +
                ", latLoc=" + latLoc +
                ", lat=" + lat +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", address='" + address + '\'' +
                ", gpstype=" + gpstype +
                ", gpslng=" + gpslng +
                ", gpslat=" + gpslat +
                '}';
    }

    public double getLongitude() {
        return lng;
    }

    public void setLongitude(double longitude) {
        this.lng = longitude;
    }

    public double getLatitude() {
        return lat;
    }

    public void setLatitude(double latitude) {
        this.lat = latitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public int getLocate() {
        return located;
    }

    public void setLocate(int locate) {
        this.located = locate;
    }

    public int getLngLoc() {
        return lngLoc;
    }

    public void setLngLoc(int lngLoc) {
        this.lngLoc = lngLoc;
    }

    public int getLatLoc() {
        return latLoc;
    }

    public void setLatLoc(int latLoc) {
        this.latLoc = latLoc;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getGpstype() {
        return gpstype;
    }

    public void setGpstype(int gpstype) {
        this.gpstype = gpstype;
    }

    public double getGpslng() {
        return gpslng;
    }

    public void setGpslng(double gpslng) {
        this.gpslng = gpslng;
    }

    public double getGpslat() {
        return gpslat;
    }

    public void setGpslat(double gpslat) {
        this.gpslat = gpslat;
    }
}
