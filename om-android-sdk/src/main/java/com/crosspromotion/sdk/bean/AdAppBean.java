package com.crosspromotion.sdk.bean;

import android.text.TextUtils;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.JsonUtil;

import org.json.JSONObject;

public class AdAppBean {

    private String id;
    private String name;
    private String icon;
    private String replaceIcon;
    private double rating;

    private String mOriData;

    public AdAppBean(JSONObject object) {
        if (object == null) {
            return;
        }
        mOriData = object.toString();
        this.id = object.optString("id");
        this.name = object.optString("name");
        this.icon = object.optString("icon");
        this.rating = object.optDouble("rating", 0);
        this.replaceIcon = object.optString("replaceIcon");
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public void setReplaceIcon(String icon) {
        this.replaceIcon = icon;
    }

    public JSONObject getAdObject() {
        JSONObject object = null;
        try {
            object = new JSONObject(mOriData);
            if (!TextUtils.isEmpty(replaceIcon)) {
                JsonUtil.put(object, "icon", replaceIcon);
            }
        } catch (Exception ignore) {
        }
        return object;
    }

    public static JSONObject toJSONObject(AdAppBean bean) {
        if (bean == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", bean.id);
            jsonObject.put("name", bean.name);
            jsonObject.put("icon", bean.icon);
            jsonObject.put("rating", bean.rating);
            jsonObject.put("replaceIcon", bean.replaceIcon);
            jsonObject.put("mOriData", bean.mOriData);
            return jsonObject;
        } catch (Exception e) {
            DeveloperLog.LogE("AdAppBean convert JSONObject error: " + e.getMessage());
        }
        return null;
    }
}
