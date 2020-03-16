// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.crash.CrashUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Events implements Parcelable {

    /**
     * for reporting
     */
    private String url;
    /**
     * maxNumOfEventsToUpload 
     */
    private int mn;
    /**
     * checkInterval in seconds
     */
    private int ci;
    /**
     * EventIDs
     */
    private List<Integer> ids;

    public Events() {
    }

    public Events(JSONObject object) {
        url = object.optString("url");
        mn = object.optInt("mn");
        ci = object.optInt("ci");
        try {
            JSONArray idArray = object.optJSONArray("ids");
            if (idArray != null) {
                if (ids == null) {
                    ids = new ArrayList<>();
                } else {
                    ids.clear();
                }
                for (int i = 0; i < idArray.length(); i++) {
                    ids.add(idArray.getInt(i));
                }
            }
        } catch (JSONException e) {
            CrashUtil.getSingleton().saveException(e);
            DeveloperLog.LogD(e.getMessage());
        }
    }

    public Events(String url, int mn, int ci, List<Integer> ids) {
        this.url = url;
        this.mn = mn;
        this.ci = ci;
        this.ids = ids;
    }

    protected Events(Parcel in) {
        url = in.readString();
        mn = in.readInt();
        ci = in.readInt();
        if (in.readByte() == (byte) 1) {
            int[] chas = in.createIntArray();
            ids = new ArrayList<>();
            for (int i = 0; i < chas.length; i++) {
                ids.add(chas[i]);
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeInt(mn);
        dest.writeInt(ci);
        if (ids != null && ids.size() > 0) {
            dest.writeByte((byte) 1);
            int[] chas = new int[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                chas[i] = ids.get(i);
            }
            dest.writeIntArray(chas);
        } else {
            dest.writeByte((byte) 0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Events> CREATOR = new Creator<Events>() {
        @Override
        public Events createFromParcel(Parcel in) {
            return new Events(in);
        }

        @Override
        public Events[] newArray(int size) {
            return new Events[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMn() {
        return mn;
    }

    public void setMn(int mn) {
        this.mn = mn;
    }

    public int getCi() {
        return ci;
    }

    public void setCi(int ci) {
        this.ci = ci;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

}
