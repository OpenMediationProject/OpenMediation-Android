package com.crosspromotion.sdk.utils.webview;

import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.utils.request.RequestBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public class JsReportParams {

    public static JSONObject buildInitEventParams(String placementId, String sceneName,
                                                  String campaign, int abt) throws Exception {
        JSONObject body = new JSONObject();
        JsonUtil.put(body, "type", JsBridgeConstants.EVENT_INIT);
        JsonUtil.put(body, "bfs", buildBFS(abt));
        JsonUtil.put(body, "campaign", buildCampaign(campaign));
        JsonUtil.put(body, "placement", buildPlacement(placementId));
        JsonUtil.put(body, "scene", buildScene(placementId, sceneName));
        return body;
    }

    public static JSONObject buildEventParams(String event) {
        JSONObject body = new JSONObject();
        JsonUtil.put(body, "type", event);
        return body;
    }

    private static JSONObject buildBFS(int abt) throws Exception {
        JSONObject bfs = RequestBuilder.getRequestBodyBaseJson();
        JsonUtil.put(bfs, "appk", DataCache.getInstance().get(KeyConstants.KEY_APP_KEY, String.class));
        JsonUtil.put(bfs, "sdkv", CommonConstants.SDK_VERSION_NAME);
        JsonUtil.put(bfs, "abt", abt);
        return bfs;
    }

    private static JSONObject buildCampaign(String campaign) throws JSONException {
        return new JSONObject(campaign);
    }

    private static JSONObject buildPlacement(String placementId) throws JSONException {
        Placement placement = PlacementUtils.getPlacement(placementId);
        return placement != null ? new JSONObject(placement.getOriData()) : null;
    }

    private static JSONObject buildScene(String placementId, String sceneName) throws JSONException {
        Placement placement = PlacementUtils.getPlacement(placementId);
        if (placement == null) {
            return null;
        }
        Scene scene = SceneUtil.getScene(placement, sceneName);
        return scene != null ? new JSONObject(scene.getOriData()) : null;
    }
}
