package com.wwc2.networks.server.provider;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class CrashError {

    @Id(autoincrement = true)
    private Long id;
    private int type;
    private String pkg;
    private String log;
    private String time;
    @Generated(hash = 1771684486)
    public CrashError(Long id, int type, String pkg, String log, String time) {
        this.id = id;
        this.type = type;
        this.pkg = pkg;
        this.log = log;
        this.time = time;
    }
    @Generated(hash = 712071334)
    public CrashError() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getPkg() {
        return this.pkg;
    }
    public void setPkg(String pkg) {
        this.pkg = pkg;
    }
    public String getLog() {
        return this.log;
    }
    public void setLog(String log) {
        this.log = log;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
