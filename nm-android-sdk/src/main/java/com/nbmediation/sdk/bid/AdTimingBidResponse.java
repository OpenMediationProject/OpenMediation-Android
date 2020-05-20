// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.bid;

public class AdTimingBidResponse {
    private int iid;
    private double price;
    private String cur;
    private String original;
    private String payLoad;

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
}
