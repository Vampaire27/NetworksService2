package com.wwc2.networks.server.bean;

public class JsimBean {
    public String error_code;
    public String error_msg;
    public Card data;

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public Card getData() {
        return data;
    }

    public void setData(Card data) {
        this.data = data;
    }

    public class Card {
        private String endtime;
        private String liuliangtype;
        private String liuliangtypetext;
        private String type;
        private String productname;
        private String remaining;

        public String getRemaining() {
            return remaining;
        }

        public void setRemaining(String remaining) {
            this.remaining = remaining;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getEndtime() {
            return endtime;
        }

        public void setEndtime(String endtime) {
            this.endtime = endtime;
        }

        public String getLiuliangtype() {
            return liuliangtype;
        }

        public void setLiuliangtype(String liuliangtype) {
            this.liuliangtype = liuliangtype;
        }

        public String getLiuliangtypetext() {
            return liuliangtypetext;
        }

        public void setLiuliangtypetext(String liuliangtypetext) {
            this.liuliangtypetext = liuliangtypetext;
        }

        public String getProductname() {
            return productname;
        }

        public void setProductname(String productname) {
            this.productname = productname;
        }
    }

    @Override
    public String toString() {
        return "JsimBean{" +
                "error_code='" + error_code + '\'' +
                ", error_msg='" + error_msg + '\'' +
                ", data=" + data +
                '}';
    }
}
