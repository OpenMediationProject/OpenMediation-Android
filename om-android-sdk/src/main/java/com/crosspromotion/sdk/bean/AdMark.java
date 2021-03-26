// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.bean;

import com.openmediation.sdk.utils.DeveloperLog;

import org.json.JSONObject;

public class AdMark {
    // logo url, default logo if null
    private String logo;
    // link url, to open with the system browser after click; does nothing if null
    private String link;

    public AdMark(JSONObject object) {
        if (object == null) {
            return;
        }
        this.logo = object.optString("logo");
        this.link = object.optString("link");
    }

    public String getLink() {
        return link;
    }

    public String getLogo() {
        return logo;
    }

    public static JSONObject toJSONObject(AdMark adMark) {
        if (adMark == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("logo", adMark.logo);
            jsonObject.put("link", adMark.link);
            return jsonObject;
        } catch (Exception e) {
            DeveloperLog.LogE("AdMark convert JSONObject error: " + e.getMessage());
        }
        return null;
    }
}
