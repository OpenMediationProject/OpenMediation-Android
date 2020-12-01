package com.openmediation.sdk.adn.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.openmediation.sdk.utils.JsonUtil;

import org.json.JSONObject;

public class AdAppBean implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.icon);
        dest.writeDouble(this.rating);
        dest.writeString(this.mOriData);
    }

    protected AdAppBean(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.icon = in.readString();
        this.rating = in.readDouble();
        this.mOriData = in.readString();
    }

    public static final Creator<AdAppBean> CREATOR = new Creator<AdAppBean>() {
        @Override
        public AdAppBean createFromParcel(Parcel source) {
            return new AdAppBean(source);
        }

        @Override
        public AdAppBean[] newArray(int size) {
            return new AdAppBean[size];
        }
    };
}
