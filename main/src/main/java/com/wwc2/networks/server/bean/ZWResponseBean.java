package com.wwc2.networks.server.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/4/26.
 */

public class ZWResponseBean implements Serializable {
    private static final long serialVersionUID = 6923471069251608667L;

    private int errCode;
    private int err;
    private Object msg;

    @Override
    public String toString() {
        return "ResponseEntry{" +
                "errCode=" + errCode +
                ", err=" + err +
                ", msg=" + msg +
                '}';
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public int getErr() {
        return err;
    }

    public void setErr(int err) {
        this.err = err;
    }
}
