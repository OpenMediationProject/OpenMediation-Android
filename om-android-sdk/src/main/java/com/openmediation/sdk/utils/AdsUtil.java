// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.text.TextUtils;

import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
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
        }
        if (error != null) {
            JsonUtil.put(jsonObject, "msg", error.toString());
        }
        EventUploadManager.getInstance().uploadEvent(eventId, jsonObject);
    }

    public static JSONObject buildAbtReportData(int abt, JSONObject object) {
        JsonUtil.put(object, "abt", abt);
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
}
