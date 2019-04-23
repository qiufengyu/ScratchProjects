package com.example.demo.entity;

// 分析结果对应的数据结构
public class SiteIP {
    private String site;
    private String ip;
    private int count;

    public SiteIP(String site, String ip, int count) {
        this.site = site;
        this.ip = ip;
        this.count = count;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
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
