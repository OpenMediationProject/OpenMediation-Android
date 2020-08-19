// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

public class InstanceLoadStatus {
    //Instances Id
    private int iid;
    // Last Timestamp
    private int lts;
    private long dur;
    private String code;
    private String msg;

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public int getLts() {
        return lts;
    }

    public void setLts(long lts) {
        this.lts = (int) (lts / 1000);
    }

    public long getDur() {
        return dur;
    }

    public void setDur(long dur) {
        this.dur = dur;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
