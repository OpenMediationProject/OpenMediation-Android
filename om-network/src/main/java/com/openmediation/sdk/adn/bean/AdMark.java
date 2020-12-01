// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.adn.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class AdMark implements Parcelable {
    // logo url, default logo if null
    private String logo;
    // link url, to open with the system browser after click; does nothing if null
    private String link;

    public AdMark(JSONObject object) {
        this.logo = object.optString("logo");
        this.link = object.optString("link");
    }

    public String getLink() {
        return link;
    }

    public String getLogo() {
        return logo;
    }

    protected AdMark(Parcel in) {
        logo = in.readString();
        link = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(logo);
        dest.writeString(link);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AdMark> CREATOR = new Creator<AdMark>() {
        @Override
        public AdMark createFromParcel(Parcel in) {
            return new AdMark(in);
        }

        @Override
        public AdMark[] newArray(int size) {
            return new AdMark[size];
        }
    };
}
