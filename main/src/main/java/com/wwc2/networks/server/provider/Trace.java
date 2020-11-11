package com.wwc2.networks.server.provider;

import com.wwc2.networks.server.utils.LogUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Trace {

    @Id(autoincrement = true)
    private Long id;
    @Unique
    private String traceID;
    private String serial_no;
    private Long startTime;
    private Double startLongitude;
    private Double startLatitude;
    private Long endTime;
    private Double endLongitude;
    private Double endLatitude;
    private Float sumTrace;
    private Long sumTime;
    private Float averageSpeed;
    private Float maxSpeed;
    private Integer status;

    @Generated(hash = 632898118)
    public Trace(Long id, String traceID, String serial_no, Long startTime,
            Double startLongitude, Double startLatitude, Long endTime,
            Double endLongitude, Double endLatitude, Float sumTrace, Long sumTime,
            Float averageSpeed, Float maxSpeed, Integer status) {
        this.id = id;
        this.traceID = traceID;
        this.serial_no = serial_no;
        this.startTime = startTime;
        this.startLongitude = startLongitude;
        this.startLatitude = startLatitude;
        this.endTime = endTime;
        this.endLongitude = endLongitude;
        this.endLatitude = endLatitude;
        this.sumTrace = sumTrace;
        this.sumTime = sumTime;
        this.averageSpeed = averageSpeed;
        this.maxSpeed = maxSpeed;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Trace{" +
                "id=" + id +
                ", traceID='" + traceID + '\'' +
                ", serial_no='" + serial_no + '\'' +
                ", startTime=" + startTime +
                ", startLongitude=" + startLongitude +
                ", startLatitude=" + startLatitude +
                ", endTime=" + endTime +
                ", endLongitude=" + endLongitude +
                ", endLatitude=" + endLatitude +
                ", sumTrace=" + sumTrace +
                ", sumTime=" + sumTime +
                ", averageSpeed=" + averageSpeed +
                ", maxSpeed=" + maxSpeed +
                ", status=" + status +
                '}';
    }

    @Generated(hash = 2124630622)
    public Trace() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTraceID() {
        return this.traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public String getSerial_no() {
        return this.serial_no;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Double getStartLongitude() {
        return this.startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getStartLatitude() {
        return this.startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Double getEndLongitude() {
        return this.endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public Double getEndLatitude() {
        return this.endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Float getSumTrace() {
        return this.sumTrace;
    }

    public void setSumTrace(Float sumTrace) {
        this.sumTrace = sumTrace;
    }

    public Long getSumTime() {
        return this.sumTime;
    }

    public void setSumTime(Long sumTime) {
        this.sumTime = sumTime;
    }

    public Float getAverageSpeed() {
        return this.averageSpeed;
    }

    public void setAverageSpeed(Float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public Float getMaxSpeed() {
        return this.maxSpeed;
    }

    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        LogUtils.d("Trace setStatus=" + status);
        this.status = status;
    }
}
