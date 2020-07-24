// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.Map;

public abstract class CustomBannerEvent extends CustomAdEvent {

    protected static final String DESC_BANNER = "BANNER";
    protected static final String DESC_LEADERBOARD = "LEADERBOARD";
    protected static final String DESC_RECTANGLE = "RECTANGLE";
    protected static final String DESC_SMART = "SMART";

    protected String getBannerDesc(Map<String, String> config) {
        String description = "";
        if (config != null && config.containsKey("description")) {
            try {
                description = config.get("description");
            } catch (Exception ignored) {
            }
        }
        return description;
    }

    public static boolean isLargeScreen(Context context) {
        if (context == null) {
            return false;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (dpHeight > 720.0F && dpWidth >= 728.0F);
    }
}
