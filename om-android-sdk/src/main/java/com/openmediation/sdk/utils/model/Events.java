// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.crash.CrashUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Events {

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

    /**
     * A list of EventID that need to be reported immediately
     */
    private List<Integer> fids;

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
        try {
            JSONArray idArray = object.optJSONArray("fids");
            if (idArray != null) {
                if (fids == null) {
                    fids = new ArrayList<>();
                } else {
                    fids.clear();
                }
                for (int i = 0; i < idArray.length(); i++) {
                    fids.add(idArray.getInt(i));
                }
            }
        } catch (JSONException e) {
            CrashUtil.getSingleton().saveException(e);
            DeveloperLog.LogD(e.getMessage());
        }
    }

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

    public List<Integer> getFids() {
        return fids;
    }

    public void setFids(List<Integer> fids) {
        this.fids = fids;
    }
}
