package com.wwc2.networks.server.bean;

/**
 * @Date 19-1-28
 * @Author ZHua.
 * @Description 远程指令实体
 */
public class RemotelyBean {
    //指令类型来源, 1=后台 2=公众号 3=App
    String fromType;
    //1=前视 2=后视
    String type;
    //页码
    String page;
    //数量
    String number;
    //推流地址
    String url;
    //指令标识
    String values;
    //回放文件
    String file;
    //推流开关,1=开,2=关
    String sWitch;
    //融云直播房间号
    String roomId;
    /**
     *  服务端目标ID
     */
    String userId;

    @Override
    public String toString() {
        return "RemotelyBean{" +
                "fromType='" + fromType + '\'' +
                ", type='" + type + '\'' +
                ", page='" + page + '\'' +
                ", number='" + number + '\'' +
                ", url='" + url + '\'' +
                ", values='" + values + '\'' +
                ", file='" + file + '\'' +
                ", sWitch='" + sWitch + '\'' +
                ", roomId='" + roomId + '\'' +
                ", roomId='" + userId + '\'' +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getFromType() {
        return fromType;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getsWitch() {
        return sWitch;
    }

    public void setsWitch(String sWitch) {
        this.sWitch = sWitch;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
