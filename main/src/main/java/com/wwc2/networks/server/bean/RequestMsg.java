package com.wwc2.networks.server.bean;

/**
 * description ： 聊天通讯
 * user: wangpeng on 2019/10/14.
 * emai: wpeng@waterworld.com.cn
 */
public class RequestMsg {

    private int type;
    private MyData data;

    public static class  MyData{
        private String code;
        private String msg;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MyData getData() {
        return data;
    }

    public void setData(MyData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RequestMsg{" +
                "type=" + type +
                ", data=" + data +
                 ", code=" + data.getCode() +
                 ", msg=" + data.getMsg() +
                '}';
    }
}
