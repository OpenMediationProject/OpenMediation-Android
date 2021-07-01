/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mediation;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.openmediation.sdk.utils.lifecycle.ActLifecycle;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.HandlerUtil;

import java.util.Map;

public class MediationUtil {
    public static final String DESC_BANNER = "BANNER";
    public static final String DESC_LEADERBOARD = "LEADERBOARD";
    public static final String DESC_RECTANGLE = "RECTANGLE";
    public static final String DESC_SMART = "SMART";

    public static Application getApplication() {
        return AdtUtil.getInstance().getApplicationContext();
    }

    public static Context getContext() {
        return AdtUtil.getInstance().getApplicationContext();
    }

    public static void runOnUiThread(Runnable runnable) {
        HandlerUtil.runOnUiThread(runnable);
    }

    public static int dip2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static Activity getActivity() {
        return ActLifecycle.getInstance().getActivity();
    }

    public static String getBannerDesc(Map<String, Object> config) {
        String description = "";
        if (config != null && config.containsKey("description")) {
            try {
                description = config.get("description").toString();
            } catch(Exception ignored) {
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
