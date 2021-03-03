// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import android.util.SparseArray;

import java.util.Map;

public class Configurations {
    private int d;
    private int coa;
    // Impression Callback Switch, 0-off, 1-on
    private int ics;
    private ApiConfigurations api;
    private Events events;
    private SparseArray<Mediation> ms;
    private Map<String, Placement> pls;

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public void setIcs(int ics) {
        this.ics = ics;
    }

    public boolean auctionEnabled() {
        return ics == 1;
    }

    public int getCoa() {
        return coa;
    }

    public void setCoa(int coa) {
        this.coa = coa;
    }

    public ApiConfigurations getApi() {
        return api;
    }

    public void setApi(ApiConfigurations api) {
        this.api = api;
    }

    public Events getEvents() {
        return events;
    }

    public void setEvents(Events events) {
        this.events = events;
    }

    public SparseArray<Mediation> getMs() {
        return ms;
    }

    public void setMs(SparseArray<Mediation> ms) {
        this.ms = ms;
    }

    public Map<String, Placement> getPls() {
        return pls;
    }

    public void setPls(Map<String, Placement> pls) {
        this.pls = pls;
    }
}
