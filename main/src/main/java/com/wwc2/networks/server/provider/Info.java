package com.wwc2.networks.server.provider;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Info {

    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String info;

    @Generated(hash = 1687775978)
    public Info(Long id, String info) {
        this.id = id;
        this.info = info;
    }

    @Generated(hash = 614508582)
    public Info() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
