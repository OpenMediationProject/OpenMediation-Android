// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

public class BidResponse {
    private int iid;
    private double price = 0;
    private String cur;
    private String original;
    private String payLoad;
    private String token;
    private String nurl;
    private String lurl;
    private int expire;
    private long createTime;
    private boolean isNotified;

    public BidResponse() {
        createTime = System.currentTimeMillis();
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public int getIid() {
        return iid;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setCur(String cur) {
        this.cur = cur;
    }

    public String getCur() {
        return cur;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getOriginal() {
        return original;
    }

    public void setPayLoad(String payLoad) {
        this.payLoad = payLoad;
    }

    public String getPayLoad() {
        return payLoad;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getNurl() {
        return nurl;
    }

    public void setNurl(String nurl) {
        this.nurl = nurl;
    }

    public String getLurl() {
        return lurl;
    }

    public void setLurl(String lurl) {
        this.lurl = lurl;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public boolean isExpired() {
        if (expire <= 0) {
            return false;
        }
        return (System.currentTimeMillis() - createTime) > (expire * 60 * 1000);
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    public boolean isNotified() {
        return isNotified;
    }
}
