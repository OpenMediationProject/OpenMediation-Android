// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.banner;

public enum AdSize {

    BANNER(320, 50, "BANNER"),
    LEADERBOARD(728, 90, "LEADERBOARD"),
    MEDIUM_RECTANGLE(300, 250, "RECTANGLE"),
    SMART(-1, -1, "SMART");

    int width;
    int height;
    String mDescription = "";

    AdSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    AdSize(int width, int height, String desc) {
        this.width = width;
        this.height = height;
        this.mDescription = desc;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDescription() {
        return mDescription;
    }
}
