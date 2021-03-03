// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.helper;

import android.text.TextUtils;
import android.util.SparseArray;

import com.openmediation.sdk.core.imp.interstitialad.IsInstance;
import com.openmediation.sdk.core.imp.promotion.CpInstance;
import com.openmediation.sdk.core.imp.rewardedvideo.RvInstance;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.ApiConfigurations;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.Events;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.Mediation;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.utils.request.HeaderUtils;
import com.openmediation.sdk.utils.request.RequestBuilder;
import com.openmediation.sdk.utils.request.network.AdRequest;
import com.openmediation.sdk.utils.request.network.ByteRequestBody;
import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.request.network.Request;
import com.openmediation.sdk.utils.request.network.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
    public static void getConfiguration(String appKey, String host, Request.OnRequestCallback requestCallback) throws Exception {
        if (requestCallback == null) {
            return;
        }
        //
        if (TextUtils.isEmpty(host)) {
            host = CommonConstants.INIT_URL;
        }
        String initUrl = RequestBuilder.buildInitUrl(host, appKey);
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
            configurations.setD(configJson.optInt("d"));
            configurations.setCoa(configJson.optInt("coa"));
            configurations.setIcs(configJson.optInt("ics"));
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
        configurations.setCd(jsonObject.optString("cd"));
        configurations.setHb(jsonObject.optString("hb"));
        configurations.setCpcl(jsonObject.optString("cpcl"));
        configurations.setCppl(jsonObject.optString("cppl"));
        return configurations;
    }

    private static Map<String, Placement> formatPlacement(SparseArray<Mediation> mapps, JSONArray placementArray) {
        Map<String, Placement> placementMap = new HashMap<>();
        int len = placementArray.length();
        if (len == 0) {
            return placementMap;
        }
        for (int i = 0; i < len; i++) {
            JSONObject placementObject = placementArray.optJSONObject(i);
            String placementId = String.valueOf(placementObject.optInt("id"));
            int adType = placementObject.optInt("t");
            Placement placement = new Placement();
            placement.setOriData(placementObject.toString());
            placement.setId(placementId);
            placement.setName(placementObject.optString("n"));
            placement.setT(adType);
            placement.setFrequencyCap(placementObject.optInt("fc"));
            placement.setFrequencyUnit(placementObject.optInt("fu") * 60 * 60 * 1000);
            placement.setFrequencyInterval(placementObject.optInt("fi") * 1000);
            placement.setRf(placementObject.optInt("rf"));
            JSONObject rfsObject = placementObject.optJSONObject("rfs");
            if (rfsObject != null) {
                Map<Integer, Integer> rfsMap = new TreeMap<>();
                Iterator<String> keys = rfsObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    int value = rfsObject.optInt(key);
                    rfsMap.put(Integer.valueOf(key), value);
                }
                placement.setRfs(rfsMap);
            }
            placement.setCs(placementObject.optInt("cs", 1));
            placement.setBs(placementObject.optInt("bs"));
            placement.setFo(placementObject.optInt("fo"));
            placement.setPt(placementObject.optInt("pt"));
            placement.setRlw(placementObject.optInt("rlw"));
            placement.setHasHb(placementObject.optInt("hb") == 1);
            placement.setMain(placementObject.optInt("main"));
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
            if (adType == CommonConstants.BANNER || adType == CommonConstants.NATIVE || adType == CommonConstants.SPLASH) {
                instance.setPath(AdapterUtil.getAdapterPathWithType(adType, mediation.getId()));
            }
            instance.setAppKey(mediation.getK());

            String key = insObject.optString("k");
            instance.setKey(key);
            instance.setId(instancesId);
            instance.setName(insObject.optString("n"));
            instance.setPlacementId(placementId);
            instance.setMediationId(mediationId);
            instance.setFrequencyCap(insObject.optInt("fc"));
            instance.setFrequencyUnit(insObject.optInt("fu") * 60 * 60 * 1000);
            instance.setFrequencyInterval(insObject.optInt("fi") * 1000);
            instance.setHb(insObject.optInt("hb"));
            int hbt = insObject.optInt("hbt");
            if (hbt < 1000) {
                hbt = CommonConstants.HEAD_BIDDING_TIMEOUT;
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
            case CommonConstants.PROMOTION:
                CpInstance instance = new CpInstance();
                instance.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
                return instance;
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
            mediation.setNn(mediationObject.optString("nn"));
            mediationSparseArray.put(id, mediation);
        }
        return mediationSparseArray;
    }
}
