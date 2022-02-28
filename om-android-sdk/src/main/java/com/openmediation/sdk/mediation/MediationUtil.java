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

import com.openmediation.sdk.core.AdapterRepository;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.lifecycle.ActLifecycle;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Placement;

import org.json.JSONObject;

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

    public static Map<String, Object> getPolicySettings() {
        return AdapterRepository.getInstance().getPolicySettings();
    }

    public static void event(int eventId, Map<String, Object> extras, int mid) {
        JSONObject object = new JSONObject();
        if (extras != null) {
            Placement placement = null;
            if (extras.containsKey("PlacementId")) {
                String pid = (String) extras.get("PlacementId");
                placement = PlacementUtils.getPlacement(pid);
            }
            BaseInstance ins = null;
            if (placement != null && extras.containsKey("InstanceId")) {
                String instanceId = (String) extras.get("InstanceId");
                ins = InsManager.getInsById(placement, instanceId);
            }
            if (ins != null) {
                object = InsManager.buildReportData(ins);
            } else {
                JsonUtil.put(object, "pid", extras.get("PlacementId"));
                JsonUtil.put(object, "iid", extras.get("InstanceId"));
                JsonUtil.put(object, "bid", extras.get("Bid"));
                JsonUtil.put(object, "mid", mid);
            }
            if (extras.containsKey("pay_load")) {
                JsonUtil.put(object, "payload", String.valueOf(extras.get("pay_load")));
            }
        }
        EventUploadManager.getInstance().uploadEvent(eventId, object);
    }
}
