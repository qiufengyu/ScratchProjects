package com.example.demo.entity;

public class TransIP {
    private String trans;
    private String ip;
    private int count;

    public TransIP(String trans, String ip, int count) {
        this.trans = trans;
        this.ip = ip;
        this.count = count;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
