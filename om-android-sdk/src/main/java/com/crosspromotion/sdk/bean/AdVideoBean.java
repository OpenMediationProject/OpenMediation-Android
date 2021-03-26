package com.crosspromotion.sdk.bean;

import android.text.TextUtils;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.JsonUtil;

import org.json.JSONObject;

public class AdVideoBean {
    private String url;
    private String replaceUrl;
    private int dur;
    private String mOriData;

    public AdVideoBean(JSONObject object) {
        if (object == null) {
            return;
        }
        mOriData = object.toString();
        this.url = object.optString("url");
        this.dur = object.optInt("dur");
        this.replaceUrl = object.optString("replaceUrl");
    }

    public String getUrl() {
        return url;
    }

    public void setReplaceUrl(String url) {
        this.replaceUrl = url;
    }

    public JSONObject getAdObject() {
        JSONObject object = null;
        try {
            object = new JSONObject(mOriData);
            if (!TextUtils.isEmpty(replaceUrl)) {
                JsonUtil.put(object, "url", replaceUrl);
            }
        } catch (Exception ignore) {
        }
        return object;
    }

    public static JSONObject toJSONObject(AdVideoBean bean) {
        if (bean == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", bean.url);
            jsonObject.put("replaceUrl", bean.replaceUrl);
            jsonObject.put("dur", bean.dur);
            jsonObject.put("mOriData", bean.mOriData);
            return jsonObject;
        } catch (Exception e) {
            DeveloperLog.LogE("AdVideoBean convert JSONObject error: " + e.getMessage());
        }
        return null;
    }
}
