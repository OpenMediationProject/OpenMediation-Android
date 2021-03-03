// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.banner;

public enum AdSize {

    BANNER(320, 50),
    LEADERBOARD(728, 90),
    MEDIUM_RECTANGLE(300, 250),
    SMART(-1, -1);

    int width;
    int height;

    AdSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}