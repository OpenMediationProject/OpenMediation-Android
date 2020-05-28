// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.helper;

import android.text.TextUtils;
import android.util.SparseArray;

import com.nbmediation.sdk.bid.AdTimingBidResponse;
import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.utils.AdsUtil;
import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.JsonUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.Preconditions;
import com.nbmediation.sdk.utils.model.Instance;
import com.nbmediation.sdk.utils.cache.DataCache;
import com.nbmediation.sdk.utils.constant.KeyConstants;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.Configurations;
import com.nbmediation.sdk.utils.model.Placement;
import com.nbmediation.sdk.utils.model.PlacementInfo;
import com.nbmediation.sdk.utils.request.HeaderUtils;
import com.nbmediation.sdk.utils.request.RequestBuilder;
import com.nbmediation.sdk.utils.request.network.AdRequest;
import com.nbmediation.sdk.utils.request.network.ByteRequestBody;
import com.nbmediation.sdk.utils.request.network.Headers;
import com.nbmediation.sdk.utils.request.network.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Water fall helper.
 */
public class WaterFallHelper {
    //testing instance
    private static Map<String, BaseInstance[]> testInstanceMap = new HashMap<>();

    /**
     * Sets test instance.
     *
     * @param placementId   the placement id
     * @param testInstances the test instances
     */
    public static void setTestInstance(String placementId, BaseInstance[] testInstances) {
        testInstanceMap.put(placementId, testInstances);
    }

    /**
     * Clean test instance.
     */
    public static void cleanTestInstance() {
        testInstanceMap.clear();
    }

    private static Map<String, BaseInstance[]> getTestInstanceMap() {
        return testInstanceMap;
    }


    /**
     * Wf request.
     *
     * @param info         the info
     * @param type         the type
     * @param responseList the response list
     * @param callback     the callback
     * @throws Exception the exception
     */
    public static void wfRequest(PlacementInfo info, NmManager.LOAD_TYPE type,
                                 List<AdTimingBidResponse> responseList,
                                 Request.OnRequestCallback callback) throws Exception {
        AdsUtil.realLoadReport(Preconditions.checkNotNull(info) ? info.getId() : "");
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getWf())) {
            callback.onRequestFailed("empty Url");
            return;
        }
        String url = RequestBuilder.buildWfUrl(config.getApi().getWf());

        byte[] bytes = RequestBuilder.buildWfRequestBody(responseList, info.getId(),
                String.valueOf(info.getWidth()),
                String.valueOf(info.getHeight()),
                String.valueOf(type.getValue()),
                String.valueOf(PlacementUtils.getPlacementImprCount(info.getId())),
                IapHelper.getIap());

        if (bytes == null) {
            callback.onRequestFailed("build request data error");
            return;
        }

        ByteRequestBody requestBody = new ByteRequestBody(bytes);
        Headers headers = HeaderUtils.getBaseHeaders();
        AdRequest.post().url(url).body(requestBody).headers(headers).connectTimeout(30000).readTimeout(60000)
                .callback(callback).performRequest(AdtUtil.getApplication());
    }

    /**
     * Gets list ins result.
     *
     * @param clInfo    the cl info
     * @param placement the placement
     * @return the list ins result
     */
    public static List<Instance> getListInsResult(JSONObject clInfo, Placement placement) {

        Instance[] test = (Instance[]) getTestInstanceMap().get(placement.getId());
        //for testing
        if (test != null && test.length > 0) {
            Instance[] tmp = splitAbsIns((test));
            return new ArrayList<>(Arrays.asList(tmp));
        }

        JSONArray insArray = clInfo.optJSONArray("ins");
        if (insArray == null || insArray.length() <= 0) {
            return Collections.emptyList();
        }

        SparseArray<BaseInstance> insMap = placement.getInsMap();
        if (insMap == null || insMap.size() <= 0) {
            return Collections.emptyList();
        }

        int abt = clInfo.optInt("abt");
        List<Instance> instancesList = new ArrayList<>();
        for (int i = 0; i < insArray.length(); i++) {
            Instance ins = (Instance) insMap.get(insArray.optInt(i));
            if (ins != null) {
                ins.setWfAbt(abt);
                ins.setIndex(i);
                instancesList.add(ins);
            } else {
                JSONObject jsonObject = PlacementUtils.placementEventParams(placement.getId());
                JsonUtil.put(jsonObject, "iid", insArray.optInt(i));
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_NOT_FOUND, jsonObject);
            }
        }
        //
        if (instancesList.size() == 0) {
            return Collections.emptyList();
        }
        return instancesList;
    }

    /**
     * Parses mediation order, and saves ad campaigns to memory
     *
     * @param clInfo    the cl info
     * @param placement the placement
     * @param bs        the bs
     * @return the base instance [ ]
     */
    public static BaseInstance[] getArrayInstances(JSONObject clInfo, Placement placement, int bs) {
        if (bs == 0 || placement == null) {
            return new Instance[0];
        }

        BaseInstance[] test = getTestInstanceMap().get(placement.getId());
        //for testing
        if (test != null && test.length > 0) {
            return splitInsByBs(test, bs);
        }

        JSONArray insArray = clInfo.optJSONArray("ins");
        if (insArray == null || insArray.length() <= 0) {
            return new Instance[0];
        }

        SparseArray<BaseInstance> insMap = placement.getInsMap();
        if (insMap == null || insMap.size() <= 0) {
            return new Instance[0];
        }

        int abt = clInfo.optInt("abt");
        List<Instance> instancesList = new ArrayList<>();
        for (int i = 0; i < insArray.length(); i++) {
            BaseInstance ins = insMap.get(insArray.optInt(i));
            if (ins != null) {
                ins.setWfAbt(abt);
                instancesList.add((Instance) ins);
            }
        }
        //
        if (instancesList.size() == 0) {
            return new Instance[0];
        }

        return splitInsByBs(instancesList.toArray(new Instance[instancesList.size()]), bs);
    }

    private static Instance[] splitAbsIns(Instance[] origin) {
        //shallow copy!!!
        Instance[] result = Arrays.copyOf(origin, origin.length);
        int len = origin.length;
        for (int a = 0; a < len; a++) {
            Instance i = result[a];

            //resets instance's state if init failed or load failed
            Instance.MEDIATION_STATE state = i.getMediationState();
            if (state == Instance.MEDIATION_STATE.INIT_FAILED) {
                i.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
            } else if (state == Instance.MEDIATION_STATE.LOAD_FAILED) {
                i.setMediationState(Instance.MEDIATION_STATE.NOT_AVAILABLE);
            }
            DeveloperLog.LogD("ins state : " + i.getMediationState().toString());
            if (state != Instance.MEDIATION_STATE.AVAILABLE) {
                i.setObject(null);
                i.setStart(0);
            }
        }
        return result;
    }

    private static BaseInstance[] splitInsByBs(BaseInstance[] origin, int bs) {
        BaseInstance[] result = Arrays.copyOf(origin, origin.length);
        int len = origin.length;
        int grpIndex = 0;
        for (int a = 0; a < len; a++) {
            BaseInstance i = result[a];

            i.setIndex(a);

            //when index of instance >= group index, increase group index
            if (bs != 0) {
                if (a >= (grpIndex + 1) * bs) {
                    grpIndex++;
                }

                i.setGrpIndex(grpIndex);

                if (a % bs == 0) {
                    i.setFirst(true);
                }
            }
            i.setObject(null);
            i.setStart(0);
        }
        return result;
    }
}
