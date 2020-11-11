package com.wwc2.networks.server.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/4/26.
 */

public class ZWCardBean implements Serializable {
    private static final long serialVersionUID = 7286773842469895844L;

    private String iccid;//如你的iccid参数是19位，有可能返回20位
    private String iccid19;//sim卡片上印刷的前19位数字
    private String sim;//对应的15位sim号码
    private Long datausage_curremaining;//流量叠加包剩余可用流量(整月)，(单位字节，包时卡如不带流量包该值为0)
    private Long datausage_expired_time;//流量叠加包剩余可用流量到期时间戳(整月).(如无可用流量包该值为0)
    private String service_expired_time;//时长卡总到期时间戳 (注意0表示无包时长或者包时已经过期)
    private String using_service_package;//正在生效的包时长套餐名字(空字符串表示无包时套餐或者已经过期)
    private String using_service_expired_time;//正在使用的包时套餐过期时间戳(若没有包时套餐或套餐过期，该值为0)
    private String simstate;//已激活||已停用||可激活||暂无, (表示sim卡的状态信息，值为以上4个其中之一)
    private Long dateactivated;//iccid激活时间戳(0表示暂无数据或者未激活)

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getIccid19() {
        return iccid19;
    }

    public void setIccid19(String iccid19) {
        this.iccid19 = iccid19;
    }

    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    public Long getDatausage_curremaining() {
        return datausage_curremaining;
    }

    public void setDatausage_curremaining(Long datausage_curremaining) {
        this.datausage_curremaining = datausage_curremaining;
    }

    public Long getDatausage_expired_time() {
        return datausage_expired_time;
    }

    public void setDatausage_expired_time(Long datausage_expired_time) {
        this.datausage_expired_time = datausage_expired_time;
    }

    public String getService_expired_time() {
        return service_expired_time;
    }

    public void setService_expired_time(String service_expired_time) {
        this.service_expired_time = service_expired_time;
    }

    public String getUsing_service_package() {
        return using_service_package;
    }

    public void setUsing_service_package(String using_service_package) {
        this.using_service_package = using_service_package;
    }

    public String getUsing_service_expired_time() {
        return using_service_expired_time;
    }

    public void setUsing_service_expired_time(String using_service_expired_time) {
        this.using_service_expired_time = using_service_expired_time;
    }

    public String getSimstate() {
        return simstate;
    }

    public void setSimstate(String simstate) {
        this.simstate = simstate;
    }

    public Long getDateactivated() {
        return dateactivated;
    }

    public void setDateactivated(Long dateactivated) {
        this.dateactivated = dateactivated;
    }
}
