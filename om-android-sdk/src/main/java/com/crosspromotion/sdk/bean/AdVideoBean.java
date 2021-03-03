package com.crosspromotion.sdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.openmediation.sdk.utils.JsonUtil;

import org.json.JSONObject;

public class AdVideoBean implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeInt(this.dur);
    }

    protected AdVideoBean(Parcel in) {
        this.url = in.readString();
        this.dur = in.readInt();
    }

    public static final Creator<AdVideoBean> CREATOR = new Creator<AdVideoBean>() {
        @Override
        public AdVideoBean createFromParcel(Parcel source) {
            return new AdVideoBean(source);
        }

        @Override
        public AdVideoBean[] newArray(int size) {
            return new AdVideoBean[size];
        }
    };
}
