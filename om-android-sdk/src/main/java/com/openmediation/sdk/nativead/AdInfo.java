// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.nativead;

/**
 * Provides basic ad info
 *
 * 
 */
public class AdInfo {
    private String title;
    private String desc;
    private String callToActionText;
    private double starRating;
    private int type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCallToActionText() {
        return callToActionText;
    }

    public void setCallToActionText(String callToActionText) {
        this.callToActionText = callToActionText;
    }

    public double getStarRating() {
        return starRating;
    }

    public void setStarRating(double starRating) {
        this.starRating = starRating;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
