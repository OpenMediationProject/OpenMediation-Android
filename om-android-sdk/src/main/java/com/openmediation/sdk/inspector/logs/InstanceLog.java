/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector.logs;

import com.openmediation.sdk.utils.model.BaseInstance;

public class InstanceLog {
    private int eventTag;
    protected long start;
    protected String detail;
    protected long recordTime;
    private BaseInstance ins;
    private int position;
    private double revenue;

    public InstanceLog(BaseInstance instance) {
        this.start = System.currentTimeMillis();
        this.ins = instance;
    }

    public BaseInstance getIns() {
        return ins;
    }

    public void setEventTag(int eventTag) {
        if (this.eventTag != 0) {
            return;
        }
        this.eventTag = eventTag;
        this.recordTime = System.currentTimeMillis();
    }

    public int getEventTag() {
        return eventTag;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public long getStart() {
        return start;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getRevenue() {
        return revenue;
    }
}
