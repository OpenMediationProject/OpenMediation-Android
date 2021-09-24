// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.event;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.JsonUtil;

import org.json.JSONObject;

/**
 * Event reporting
 */
public class Event {

    private long ts;//app timestamp in mills;  1567479919643; required
    private int eid = -1;//EventId;  100   ; required
    private String msg;//event message
    private int pid = -1;//palcementID;  1111
    private int mid = -1;//Mediation ID; 1
    private int iid = -1;//instance ID;  2222
    private String adapterv;//Adapter Version;  3.0.1
    private String msdkv;//AdNetwork SDK Version;  4.2.0
    private int scene = -1;//SceneID;  0
    private int ot = -1;//Orientation, [1:portrait; 2:landscape];  1
    private int duration = -1;//in seconds;  6
    private int priority = -1;//instance load priority;  2
    private int cs = -1;//cached stock size;  3
    private String code = "";
    private int bid = -1;
    private double price = -1;
    private String cur;
    private int abt = -1;
    private int abtId = -1;

    private String reqId; // AuctionID
    private int ruleId; // Mediation Rule ID
    private JSONObject data;

    Event() throws Exception {
        this((String) null);
    }

    Event(String jsonStr) throws Exception {
        this(new JSONObject(jsonStr));
    }

    Event(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        parseFromJson(jsonObject);
    }

    private void parseFromJson(JSONObject jsonObject) {
        try {
            ts = jsonObject.optLong("ts");
            eid = jsonObject.optInt("eid", -1);
            msg = jsonObject.optString("msg");
            try {
                pid = Integer.parseInt(jsonObject.optString("pid"));
            } catch (Exception e) {
                pid = -1;
            }
            mid = jsonObject.optInt("mid", -1);
            iid = jsonObject.optInt("iid", -1);
            adapterv = jsonObject.optString("adapterv");
            msdkv = jsonObject.optString("msdkv");
            scene = jsonObject.optInt("scene", -1);
            ot = jsonObject.optInt("ot", -1);
            duration = jsonObject.optInt("duration", -1);
            priority = jsonObject.optInt("priority", -1);
            cs = jsonObject.optInt("cs", -1);
            abt = jsonObject.optInt("abt", -1);
            abtId = jsonObject.optInt("abtId", -1);
            code = jsonObject.optString("code");
            bid = jsonObject.optInt("bid", -1);
            price = jsonObject.optDouble("price", -1);
            cur = jsonObject.optString("cur");
            reqId = jsonObject.optString("reqId");
            ruleId = jsonObject.optInt("ruleId", -1);
            data = jsonObject.optJSONObject("data");
        } catch (Exception e) {
            DeveloperLog.LogD("parse Event from json ", e);
        }
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            JsonUtil.put(jsonObject, "ts", ts);
            JsonUtil.put(jsonObject, "eid", eid);
            JsonUtil.put(jsonObject, "msg", msg);
            JsonUtil.put(jsonObject, "pid", pid);
            JsonUtil.put(jsonObject, "mid", mid);
            JsonUtil.put(jsonObject, "iid", iid);
            JsonUtil.put(jsonObject, "adapterv", adapterv);
            JsonUtil.put(jsonObject, "msdkv", msdkv);
            JsonUtil.put(jsonObject, "scene", scene);
            JsonUtil.put(jsonObject, "ot", ot);
            JsonUtil.put(jsonObject, "duration", duration);
            JsonUtil.put(jsonObject, "priority", priority);
            JsonUtil.put(jsonObject, "cs", cs);
            JsonUtil.put(jsonObject, "abt", abt);
            JsonUtil.put(jsonObject, "abtId", abtId);
            JsonUtil.put(jsonObject, "code", code);
            JsonUtil.put(jsonObject, "bid", bid);
            JsonUtil.put(jsonObject, "price", price);
            JsonUtil.put(jsonObject, "cur", cur);
            JsonUtil.put(jsonObject, "reqId", reqId);
            JsonUtil.put(jsonObject, "ruleId", ruleId);
            JsonUtil.put(jsonObject, "data", data);
        } catch (Exception e) {
            DeveloperLog.LogD("Event to json ", e);
        }
        return jsonObject;
    }

    String toJson() {
        return toJSONObject().toString();
    }

    @Override
    public String toString() {
        return toJson();
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public String getAdapterv() {
        return adapterv;
    }

    public void setAdapterv(String adapterv) {
        this.adapterv = adapterv;
    }

    public String getMsdkv() {
        return msdkv;
    }

    public void setMsdkv(String msdkv) {
        this.msdkv = msdkv;
    }

    public int getScene() {
        return scene;
    }

    public void setScene(int scene) {
        this.scene = scene;
    }

    public int getOt() {
        return ot;
    }

    public void setOt(int ot) {
        this.ot = ot;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCs() {
        return cs;
    }

    public void setCs(int cs) {
        this.cs = cs;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCur() {
        return cur;
    }

    public void setCur(String cur) {
        this.cur = cur;
    }

    public void setAbt(int abt) {
        this.abt = abt;
    }

    public int getAbt() {
        return abt;
    }

    public void setAbtId(int abtId) {
        this.abtId = abtId;
    }

    public int getAbtId() {
        return abtId;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }
}
