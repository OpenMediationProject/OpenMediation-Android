// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class includes AdNetwork's basic info,like id, adapter version, sdk version
 *
 * 
 */
public class AdNetwork implements Parcelable {
    private int mid;
    private String adapterV;
    private String mSdkV;

    public AdNetwork(int mid, String adapterV, String mSdkV) {
        this.mid = mid;
        this.adapterV = adapterV;
        this.mSdkV = mSdkV;
    }

    protected AdNetwork(Parcel in) {
        mid = in.readInt();
        adapterV = in.readString();
        mSdkV = in.readString();
    }

    public static final Creator<AdNetwork> CREATOR = new Creator<AdNetwork>() {
        @Override
        public AdNetwork createFromParcel(Parcel in) {
            return new AdNetwork(in);
        }

        @Override
        public AdNetwork[] newArray(int size) {
            return new AdNetwork[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mid);
        dest.writeString(adapterV);
        dest.writeString(mSdkV);
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put(KeyConstants.AdNetwork.KEY_MID, mid);
            object.put(KeyConstants.AdNetwork.KEY_ADAPTER_V, adapterV);
            object.put(KeyConstants.AdNetwork.KEY_MSDKV, mSdkV);
        } catch (JSONException e) {
            CrashUtil.getSingleton().saveException(e);
        }
        return object;
    }
}
