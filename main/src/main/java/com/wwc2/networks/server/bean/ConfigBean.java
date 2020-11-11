package com.wwc2.networks.server.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/5/28.
 */

public class ConfigBean extends BaseBean {
    public final static int MIN_TRACE = 1;//轨迹总里程最小单位配置参数
    public final static int MIN_CHANG_LOC = 2;//行驶位置变化最小单位配置参数
    public final static int CAR_DEBUG = 3;//车机日志开启关闭设置
    public final static int TRACE_DIS = 4;//里程起步距离最小变化单位

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class RequestBean {
        private String serNo;

        public String getSerNo() {
            return serNo;
        }

        public void setSerNo(String serNo) {
            this.serNo = serNo;
        }
    }

    public static class DataBean {
        private int type;
        private int value;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
