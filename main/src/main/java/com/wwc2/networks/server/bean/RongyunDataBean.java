package com.wwc2.networks.server.bean;

import java.io.Serializable;

/**
 * description ： 拍照融云反馈
 * user: wangpeng on 2019/9/23.
 * emai: wpeng@waterworld.com.cn
 */
public class RongyunDataBean  implements Serializable {
    private int type;
    private RongData data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RongData getData() {
        return data;
    }

    public void setData(RongData data) {
        this.data = data;
    }

    public static class  RongData implements Serializable {
        private String msg;
        private String code;
        private String command;
        private String  url;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }



}
