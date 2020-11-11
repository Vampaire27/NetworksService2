package com.wwc2.networks.server.bean;

import java.util.List;

/**
 * @Date 19-1-28
 * @Author ZHua.
 * @Description
 */
public class VideoBean {

    private int total;
    private List<ListBean> list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {

        private String name;
        private String qnname;
        private double size;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getQnname() {
            return qnname;
        }

        public void setQnname(String qnname) {
            this.qnname = qnname;
        }

        public double getSize() {
            return size;
        }

        public void setSize(double size) {
            this.size = size;
        }
    }
}
