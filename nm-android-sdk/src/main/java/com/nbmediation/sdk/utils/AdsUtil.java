// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils;

import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.utils.model.Placement;
import com.nbmediation.sdk.utils.model.Scene;

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
}
