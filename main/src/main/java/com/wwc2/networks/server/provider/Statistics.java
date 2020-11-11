package com.wwc2.networks.server.provider;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Statistics {

    @Id(autoincrement = true)
    private Long id;
    private String dates;
    private Integer wifiSum;
    private Long wifiTime;
    private Long wifiFlow;
    private Integer simSum;
    private Long simTime;
    private Long simFlow;
    private Integer simSurplusFlow;
    private Long simFlowTime;
    private Integer outerSimSum;
    private Long outerSimTime;
    private Long outerSimFlow;
    private Long startTime;
    private Long endTime;

    @Override
    public String toString() {
        return "Statistics{" +
                "id=" + id +
                ", dates='" + dates + '\'' +
                ", wifiSum=" + wifiSum +
                ", wifiTime=" + wifiTime +
                ", wifiFlow=" + wifiFlow +
                ", simSum=" + simSum +
                ", simTime=" + simTime +
                ", simFlow=" + simFlow +
                ", simSurplusFlow=" + simSurplusFlow +
                ", simFlowTime=" + simFlowTime +
                ", outerSimSum=" + outerSimSum +
                ", outerSimTime=" + outerSimTime +
                ", outerSimFlow=" + outerSimFlow +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    @Generated(hash = 324911683)
    public Statistics(Long id, String dates, Integer wifiSum, Long wifiTime,
            Long wifiFlow, Integer simSum, Long simTime, Long simFlow,
            Integer simSurplusFlow, Long simFlowTime, Integer outerSimSum,
            Long outerSimTime, Long outerSimFlow, Long startTime, Long endTime) {
        this.id = id;
        this.dates = dates;
        this.wifiSum = wifiSum;
        this.wifiTime = wifiTime;
        this.wifiFlow = wifiFlow;
        this.simSum = simSum;
        this.simTime = simTime;
        this.simFlow = simFlow;
        this.simSurplusFlow = simSurplusFlow;
        this.simFlowTime = simFlowTime;
        this.outerSimSum = outerSimSum;
        this.outerSimTime = outerSimTime;
        this.outerSimFlow = outerSimFlow;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    @Generated(hash = 1975114801)
    public Statistics() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDates() {
        return this.dates;
    }
    public void setDates(String dates) {
        this.dates = dates;
    }
    public Integer getWifiSum() {
        return this.wifiSum;
    }
    public void setWifiSum(Integer wifiSum) {
        this.wifiSum = wifiSum;
    }
    public Long getWifiTime() {
        return this.wifiTime;
    }
    public void setWifiTime(Long wifiTime) {
        this.wifiTime = wifiTime;
    }
    public Long getWifiFlow() {
        return this.wifiFlow;
    }
    public void setWifiFlow(Long wifiFlow) {
        this.wifiFlow = wifiFlow;
    }
    public Integer getSimSum() {
        return this.simSum;
    }
    public void setSimSum(Integer simSum) {
        this.simSum = simSum;
    }
    public Long getSimTime() {
        return this.simTime;
    }
    public void setSimTime(Long simTime) {
        this.simTime = simTime;
    }
    public Long getSimFlow() {
        return this.simFlow;
    }
    public void setSimFlow(Long simFlow) {
        this.simFlow = simFlow;
    }
    public Integer getSimSurplusFlow() {
        return this.simSurplusFlow;
    }
    public void setSimSurplusFlow(Integer simSurplusFlow) {
        this.simSurplusFlow = simSurplusFlow;
    }
    public Long getSimFlowTime() {
        return this.simFlowTime;
    }
    public void setSimFlowTime(Long simFlowTime) {
        this.simFlowTime = simFlowTime;
    }
    public Integer getOuterSimSum() {
        return this.outerSimSum;
    }
    public void setOuterSimSum(Integer outerSimSum) {
        this.outerSimSum = outerSimSum;
    }
    public Long getOuterSimTime() {
        return this.outerSimTime;
    }
    public void setOuterSimTime(Long outerSimTime) {
        this.outerSimTime = outerSimTime;
    }
    public Long getOuterSimFlow() {
        return this.outerSimFlow;
    }
    public void setOuterSimFlow(Long outerSimFlow) {
        this.outerSimFlow = outerSimFlow;
    }
    public Long getStartTime() {
        return this.startTime;
    }
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    public Long getEndTime() {
        return this.endTime;
    }
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

}
