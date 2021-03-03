// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import android.text.TextUtils;

public class Mediation {
    //id
    private int id;
    //ad network's n
    private String n;
    // Network Name
    private String nn;
    //ad network's app key
    private String k;
//    
//    public HashMap<String,String> mediationData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public void setNn(String nn) {
        this.nn = nn;
    }

    public String getNetworkName() {
        return TextUtils.isEmpty(nn) ? n : nn;
    }
}
