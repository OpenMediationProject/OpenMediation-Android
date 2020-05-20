// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.helper;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.nbmediation.sdk.utils.AdapterUtil;
import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.model.Instance;
import com.nbmediation.sdk.core.imp.interstitialad.IsInstance;
import com.nbmediation.sdk.core.imp.rewardedvideo.RvInstance;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.crash.CrashUtil;
import com.nbmediation.sdk.utils.model.ApiConfigurations;
import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.Configurations;
import com.nbmediation.sdk.utils.model.Events;
import com.nbmediation.sdk.utils.model.Mediation;
import com.nbmediation.sdk.utils.model.Placement;
import com.nbmediation.sdk.utils.model.Scene;
import com.nbmediation.sdk.utils.request.HeaderUtils;
import com.nbmediation.sdk.utils.request.RequestBuilder;
import com.nbmediation.sdk.utils.request.network.AdRequest;
import com.nbmediation.sdk.utils.request.network.ByteRequestBody;
import com.nbmediation.sdk.utils.request.network.Headers;
import com.nbmediation.sdk.utils.request.network.Request;
import com.nbmediation.sdk.utils.request.network.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Init config response parse helper
 */
public class ConfigurationHelper {

    private ConfigurationHelper() {
    }

    /**
     * Gets config data through server Init API
     *
     * @param appKey          the app key
     * @param requestCallback the request callback
     * @throws Exception the exception
     */
    public static void getConfiguration(String appKey, Request.OnRequestCallback requestCallback) throws Exception {
        if (requestCallback == null) {
            return;
        }
        //
        String initUrl = RequestBuilder.buildInitUrl(appKey);
        if (TextUtils.isEmpty(initUrl)) {
            requestCallback.onRequestFailed("empty Url");
            return;
        }
        Headers headers = HeaderUtils.getBaseHeaders();

        AdRequest.post()
                .url(initUrl)
                .headers(headers)
                .body(new ByteRequestBody(RequestBuilder.buildConfigRequestBody(AdapterUtil.getAdns())))
                .connectTimeout(30000)
                .readTimeout(60000)
                .instanceFollowRedirects(true)
                .callback(requestCallback)
                .performRequest(AdtUtil.getApplication());
    }

    /**
     * Check response byte [ ].
     *
     * @param response the response
     * @return the byte [ ]
     */
    public static byte[] checkResponse(Response response) {
        byte[] data = null;
        if (response == null) {
            return null;
        }
        if (response.code() != 200) {
            return null;
        }
        try {
            data = response.body().byteArray();
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
        return data;
    }

    /**
     * Parse form server response configurations.
     *
     * @param json the json
     * @return the configurations
     */
    public static Configurations parseFormServerResponse(String json) {
        try {
            Configurations configurations = new Configurations();
            JSONObject configJson = new JSONObject(json);
            ////////////////test add 20200511 tjt
            Log.e("AdtDebug", "json="+json);
            JSONObject testObj = configJson.optJSONObject("api");
            if (testObj != null && testObj.names() != null) {
                for (int i = 0; i < testObj.names().length(); i++) {
                    String key = testObj.names().getString(i);
                    testObj.putOpt(key, testObj.optString(key).replace("https:", "http:"));
                }
                JSONObject testObj2 = configJson.optJSONObject("events");
                testObj2.putOpt("url", testObj.optString("url").replace("https:", "http:"));
            }
            ////////////////end
            configurations.setD(configJson.optInt("d"));
            configurations.setCoa(configJson.optInt("coa"));
            configurations.setApi(parseApiConfiguration(configJson.optJSONObject("api")));
            //Events
            JSONObject events = configJson.optJSONObject("events");
            if (events != null) {
                configurations.setEvents(new Events(events));
            } else {
                configurations.setEvents(new Events());
            }
            //
            SparseArray<Mediation> mapps = parseMediationConfigurations(configJson.optJSONArray("ms"));
            configurations.setMs(mapps);
            configurations.setPls(formatPlacement(mapps, configJson.optJSONArray("pls")));
            return configurations;
        } catch (Exception e) {
            //
            CrashUtil.getSingleton().saveException(e);
        }
        return null;
    }

    private static ApiConfigurations parseApiConfiguration(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        ApiConfigurations configurations = new ApiConfigurations();
        configurations.setWf(jsonObject.optString("wf"));
        configurations.setLr(jsonObject.optString("lr"));
        configurations.setEr(jsonObject.optString("er"));
        configurations.setIc(jsonObject.optString("ic"));
        configurations.setIap(jsonObject.optString("iap"));
        configurations.setHb(jsonObject.optString("hb"));
        return configurations;
    }

    private static Map<String, Placement> formatPlacement(SparseArray<Mediation> mapps, JSONArray placementArray) {
        Map<String, Placement> placementMap = new HashMap<>();
        int len = placementArray.length();
        if (len == 0) {
            return placementMap;
        }
        SparseBooleanArray mainPlacements = new SparseBooleanArray();
        for (int i = 0; i < len; i++) {
            JSONObject placementObject = placementArray.optJSONObject(i);
            String placementId = String.valueOf(placementObject.optInt("id"));
            int adType = placementObject.optInt("t");
            Placement placement = new Placement();
            placement.setOriData(placementObject.toString());
            placement.setId(placementId);
            placement.setT(adType);
            placement.setFrequencyCap(placementObject.optInt("fc"));
            placement.setFrequencyUnit(placementObject.optInt("fu") * 60 * 60 * 1000);
            placement.setFrequencyInterval(placementObject.optInt("fi") * 1000);
            placement.setRf(placementObject.optInt("rf"));
            placement.setCs(placementObject.optInt("cs"));
            placement.setBs(placementObject.optInt("bs"));
            placement.setFo(placementObject.optInt("fo"));
            placement.setPt(placementObject.optInt("pt"));
            placement.setRlw(placementObject.optInt("rlw"));
            placement.setHasHb(placementObject.optInt("hb") == 1);
            if (placementObject.has("main")) {
                placement.setMain(placementObject.optInt("main"));
            } else {
                if (!mainPlacements.get(adType, false)) {
                    if (placementObject.optInt("ia") != 1) {
                        placement.setMain(1);
                        mainPlacements.append(adType, true);
                    }
                }
            }
            placement.setScenes(formatScenes(placementObject.optJSONArray("scenes")));
            placement.setInsMap(formatInstances(placementId, mapps, adType, placementObject.optJSONArray("ins")));
            placementMap.put(placementId, placement);
        }
        return placementMap;
    }

    private static Map<String, Scene> formatScenes(JSONArray scenes) {
        Map<String, Scene> sceneMap = new HashMap<>();
        if (scenes != null && scenes.length() > 0) {
            for (int i = 0; i < scenes.length(); i++) {
                Scene scene = new Scene(scenes.optJSONObject(i));
                sceneMap.put(scene.getN(), scene);
            }
        }
        return sceneMap;
    }

    private static SparseArray<BaseInstance> formatInstances(String placementId, SparseArray<Mediation> mapps,
                                                             int adType, JSONArray insArray) {
        SparseArray<BaseInstance> instanceSparseArray = new SparseArray<>();
        if (insArray == null || insArray.length() == 0) {
            return instanceSparseArray;
        }

        int len = insArray.length();
        for (int i = 0; i < len; i++) {
            JSONObject insObject = insArray.optJSONObject(i);
            BaseInstance instance = createInstance(adType);
            int instancesId = insObject.optInt("id");
            int mediationId = insObject.optInt("m");
            Mediation mediation = mapps.get(mediationId);
            if (mediation == null) {
                continue;
            }
            if (adType == 0 || adType == 1) {
                instance.setPath(AdapterUtil.getAdapterPathWithType(adType, mediation.getN()));
            }
            instance.setAppKey(mediation.getK());

            String key = insObject.optString("k");
            instance.setKey(key);
            instance.setId(instancesId);
            instance.setPlacementId(placementId);
            instance.setMediationId(mediationId);
            instance.setFrequencyCap(insObject.optInt("fc"));
            instance.setFrequencyUnit(insObject.optInt("fu") * 60 * 60 * 1000);
            instance.setFrequencyInterval(insObject.optInt("fi") * 1000);
            instance.setHb(insObject.optInt("hb"));
            int hbt = insObject.optInt("hbt");
            if (hbt < 1000) {
                hbt = 5000;
            }
            instance.setHbt(hbt);
            instanceSparseArray.put(instancesId, instance);
        }
        return instanceSparseArray;
    }

    private static BaseInstance createInstance(int adType) {
        switch (adType) {
            case CommonConstants.VIDEO:
                RvInstance rvInstance = new RvInstance();
                rvInstance.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
                return rvInstance;
            case CommonConstants.INTERSTITIAL:
                IsInstance isInstance = new IsInstance();
                isInstance.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
                return isInstance;
            default:
                return new Instance();
        }
    }

    private static SparseArray<Mediation> parseMediationConfigurations(JSONArray mediations) throws Exception {
        SparseArray<Mediation> mediationSparseArray = new SparseArray<>();
        if (mediations == null || mediations.length() == 0) {
            return mediationSparseArray;
        }
        int len = mediations.length();
        for (int i = 0; i < len; i++) {
            JSONObject mediationObject = mediations.getJSONObject(i);
            Mediation mediation = new Mediation();
            String key = mediationObject.optString("k");
            int id = mediationObject.optInt("id");
            String name = mediationObject.optString("n");
            mediation.setK(key);
            mediation.setId(id);
            mediation.setN(name);
            mediationSparseArray.put(id, mediation);
        }
        return mediationSparseArray;
    }
}
