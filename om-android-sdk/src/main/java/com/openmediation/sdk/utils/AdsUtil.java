// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.text.TextUtils;

import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.Scene;

import org.json.JSONObject;

public class AdsUtil {

    public static void realLoadReport(String placementId) {
        JSONObject jsonObject = PlacementUtils.placementEventParams(placementId);
        EventUploadManager.getInstance().uploadEvent(EventId.LOAD, jsonObject);
    }

    public static void loadBlockedReport(String placementId, Error error) {
        JSONObject jsonObject = PlacementUtils.placementEventParams(placementId);
        JsonUtil.put(jsonObject, "msg", error.toString());
        EventUploadManager.getInstance().uploadEvent(EventId.LOAD_BLOCKED, jsonObject);
    }

    public static void callbackActionReport(int eventId, String placementId, Scene scene, Error error) {
        Placement placement = PlacementUtils.getPlacement(placementId);
        JSONObject jsonObject = SceneUtil.sceneReport(placementId, scene);
        if (placement != null) {
            JsonUtil.put(jsonObject, "abt", placement.getWfAbt());
            JsonUtil.put(jsonObject, "abtId", placement.getWfAbtId());
        }
        if (error != null) {
            JsonUtil.put(jsonObject, "msg", error.toString());
        }
        EventUploadManager.getInstance().uploadEvent(eventId, jsonObject);
    }

    public static JSONObject buildAbtReportData(int abt, int abtId, JSONObject object) {
        JsonUtil.put(object, "abt", abt);
        JsonUtil.put(object, "abtId", abtId);
        return object;
    }

    public static void callActionReport(String placementId, int sceneId, int eventId) {
        JSONObject object = new JSONObject();
        JsonUtil.put(object, "pid", placementId);
        JsonUtil.put(object, "scene", sceneId);
        EventUploadManager.getInstance().uploadEvent(eventId, object);
    }

    public static void callActionReport(int eventId, String placementId, String scene, int adType) {
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = PlacementUtils.getPlacement(adType);
            if (placement != null) {
                placementId = placement.getId();
            }
        }
        Scene s = SceneUtil.getScene(PlacementUtils.getPlacement(placementId), scene);
        callActionReport(placementId, s != null ? s.getId() : 0, eventId);
    }


    public static void advanceEventReport(int code, String msg) {
        advanceEventReport("", code, msg);
    }

    public static void advanceEventReport(String placementId, int code, String msg) {
        advanceEventReport(placementId, code, msg, null);
    }

    public static void advanceEventReport(String placementId, int code, String msg, Object object) {
        JSONObject jsonObject = null;
        if (!TextUtils.isEmpty(placementId)) {
            jsonObject = PlacementUtils.placementEventParams(placementId);
        } else {
            jsonObject = new JSONObject();
        }
        advanceEventReport(buildInternalData(jsonObject, code, msg, object));
    }

    public static void advanceEventReport(BaseInstance instance, int code, String msg) {
        JSONObject jsonObject = null;
        if (instance != null) {
            jsonObject = InsManager.buildReportData(instance);
        } else {
            jsonObject = new JSONObject();
        }
        advanceEventReport(buildInternalData(jsonObject, code, msg, null));
    }

    private static JSONObject buildInternalData(JSONObject object, int code, String msg, Object extra) {
        JSONObject data = new JSONObject();
        JsonUtil.put(data, "code", code);
        JsonUtil.put(data, "msg", msg);
        JsonUtil.put(data, "extra", extra);
        JsonUtil.put(object, "data", data);
        return object;
    }

    private static void advanceEventReport(JSONObject object) {
        EventUploadManager.getInstance().uploadEvent(EventId.ADVANCE, object);
    }
}
