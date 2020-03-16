// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import org.json.JSONObject;

public class Scene extends Frequency {
    //Scene ID      0
    private int id;
    //Scene Name    Default
    private String n;
    //isDefault   1
    private int isd;

    private String oriData;

    public Scene(JSONObject jsonObject) {
        oriData = jsonObject.toString();
        id = jsonObject.optInt("id");
        n = jsonObject.optString("n");
        isd = jsonObject.optInt("isd");
        setFrequencyCap(jsonObject.optInt("fc"));
        setFrequencyUnit(jsonObject.optInt("fu") * 60 * 60 * 1000);
    }

    public int getId() {
        return id;
    }

    public String getN() {
        return n;
    }

    public int getIsd() {
        return isd;
    }

    public String getOriData() {
        return oriData;
    }

    @Override
    public String toString() {
        return "Scene{" +
                "id=" + id +
                ", n='" + n + '\'' +
                ", isd=" + isd +
                '}';
    }
}
