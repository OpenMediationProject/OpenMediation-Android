/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector.logs;

public class BaseLog {
    protected int logTag;
    protected int eventTag;
    protected long start;
    protected String detail;
    protected long recordTime;

    /**
     * expand
     */
    private boolean expand = false;

    public boolean isExpand() {
        return expand;
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    public BaseLog(int logTag) {
        this.logTag = logTag;
        this.start = System.currentTimeMillis();
    }

    public int getLogTag() {
        return logTag;
    }

    public int getEventTag() {
        return eventTag;
    }

    public void setEventTag(int eventTag) {
        this.eventTag = eventTag;
        this.recordTime = System.currentTimeMillis();
    }

    public long getStart() {
        return start;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
