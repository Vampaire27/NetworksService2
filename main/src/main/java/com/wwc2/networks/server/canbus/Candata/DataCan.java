package com.wwc2.networks.server.canbus.Candata;

public class DataCan {
    Integer series; //车型
    String version; //盒子版本
    Double speed; //车速 (KM/H)
    Integer engine; //发动机转速 (rpm)
    Double mileage; //里程 (KM)
    Double mileageEndurance; //续航里程 (KM)
    Double oil; //油耗 (L/100KM)
    Double voltage; //电压 (V)
    Double tireLeftFront; //左前胎压
    Double tireRightFront; //右前胎压
    Double tireLeftRear; //左后胎压
    Double tireRightRear; //右后胎压
    String tireUnit;
    Integer doorStatus; //门状态 （从Bit5~~Bit0分别为：引擎盖、尾箱、左前门、右前门、左后门、右后门）
    Integer doorMirStatus;//门反转状态（Bit1~~Bit0分别为：前门反转、后门反转）
    int accs;
    long time;

    public DataCan() {

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTireUnit() {
        return tireUnit;
    }


    public void setTireUnit(String tireUnit) {
        this.tireUnit = tireUnit;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Integer getEngine() {
        return engine;
    }

    public void setEngine(Integer engine) {
        this.engine = engine;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public Double getMileageEndurance() {
        return mileageEndurance;
    }

    public void setMileageEndurance(Double mileageEndurance) {
        this.mileageEndurance = mileageEndurance;
    }

    public Double getOil() {
        return oil;
    }

    public void setOil(Double oil) {
        this.oil = oil;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getTireLeftFront() {
        return tireLeftFront;
    }

    public void setTireLeftFront(Double tireLeftFront) {
        this.tireLeftFront = tireLeftFront;
    }

    public Double getTireRightFront() {
        return tireRightFront;
    }

    public void setTireRightFront(Double tireRightFront) {
        this.tireRightFront = tireRightFront;
    }

    public Double getTireLeftRear() {
        return tireLeftRear;
    }

    public void setTireLeftRear(Double tireLeftRear) {
        this.tireLeftRear = tireLeftRear;
    }

    public Double getTireRightRear() {
        return tireRightRear;
    }

    public void setTireRightRear(Double tireRightRear) {
        this.tireRightRear = tireRightRear;
    }

    public Integer getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(Integer doorStatus) {
        this.doorStatus = doorStatus;
    }

    public Integer getDoorMirStatus() {
        return doorMirStatus;
    }

    public void setDoorMirStatus(Integer doorMirStatus) {
        this.doorMirStatus = doorMirStatus;
    }

    public int getAccs(){
        return  accs;
    }

    public void setAccs(int accs) {
        this.accs = accs;
    }

    @Override
    public String toString() {
        return "DataCan{" +
                "series=" + series +
                ", version='" + version + '\'' +
                ", speed=" + speed +
                ", engine=" + engine +
                ", mileage=" + mileage +
                ", mileageEndurance=" + mileageEndurance +
                ", oil=" + oil +
                ", voltage=" + voltage +
                ", tireLeftFront=" + tireLeftFront +
                ", tireRightFront=" + tireRightFront +
                ", tireLeftRear=" + tireLeftRear +
                ", tireRightRear=" + tireRightRear +
                ", doorStatus=" + doorStatus +
                ", doorMirStatus=" + doorMirStatus +
                '}';
    }

}