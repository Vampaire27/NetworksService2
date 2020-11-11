package com.wwc2.networks.server.bean;

public class CarBean extends BaseBean {

    private String door;
    private String windows;
    private String lamp;
    private String air_conditioner;
    private String tire_pressure;
    private String mileage;
    private String oil;

    public String getDoor() {
        return door;
    }

    public void setDoor(String door) {
        this.door = door;
    }

    public String getWindows() {
        return windows;
    }

    public void setWindows(String windows) {
        this.windows = windows;
    }

    public String getLamp() {
        return lamp;
    }

    public void setLamp(String lamp) {
        this.lamp = lamp;
    }

    public String getAir_conditioner() {
        return air_conditioner;
    }

    public void setAir_conditioner(String air_conditioner) {
        this.air_conditioner = air_conditioner;
    }

    public String getTire_pressure() {
        return tire_pressure;
    }

    public void setTire_pressure(String tire_pressure) {
        this.tire_pressure = tire_pressure;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getOil() {
        return oil;
    }

    public void setOil(String oil) {
        this.oil = oil;
    }
}
