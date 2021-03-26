// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.helper;

import android.text.TextUtils;
import android.util.SparseArray;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.InstanceLoadStatus;
import com.openmediation.sdk.utils.model.MediationRule;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.utils.request.HeaderUtils;
import com.openmediation.sdk.utils.request.RequestBuilder;
import com.openmediation.sdk.utils.request.network.AdRequest;
import com.openmediation.sdk.utils.request.network.ByteRequestBody;
import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.request.network.Request;

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
    private static final String AUCTION_PRICE = "${AUCTION_PRICE}";
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
     * @param type      the type
     * @param s2sResult the response list
     * @param callback  the callback
     * @param reqId     the reqId
     * @throws Exception the exception
     */
    public static void wfRequest(final PlacementInfo info, final OmManager.LOAD_TYPE type,
                                 final List<BidResponse> c2sResult,
                                 final List<BidResponse> s2sResult,
                                 final List<InstanceLoadStatus> statusList,
                                 final String reqId,
                                 final Request.OnRequestCallback callback) throws Exception {

        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
                    if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getWf())) {
                        callback.onRequestFailed("empty Url");
                        return;
                    }
                    String url = RequestBuilder.buildWfUrl(config.getApi().getWf());

                    byte[] bytes = RequestBuilder.buildWfRequestBody(info, c2sResult, s2sResult, statusList,
                            reqId,
                            IapHelper.getIap(),
                            String.valueOf(PlacementUtils.getPlacementImprCount(info.getId())),
                            String.valueOf(type.getValue())
                    );

                    if (bytes == null) {
                        callback.onRequestFailed("build request data error");
                        return;
                    }
                    AdsUtil.realLoadReport(info.getId());
                    ByteRequestBody requestBody = new ByteRequestBody(bytes);
                    Headers headers = HeaderUtils.getBaseHeaders();
                    AdRequest.post().url(url).body(requestBody).headers(headers).connectTimeout(30000).readTimeout(60000)
                            .callback(callback).performRequest(AdtUtil.getApplication());
                } catch (Exception e) {
                    DeveloperLog.LogE("WaterFall Error: " + e.getMessage());
                    CrashUtil.getSingleton().saveException(e);
                    callback.onRequestFailed("Load failed: unknown exception occurred");
                }
            }
        });
    }

    public static Map<Integer, BidResponse> getS2sBidResponse(JSONObject clInfo) {
        if (clInfo == null) {
            return null;
        }
        JSONArray bidresp = clInfo.optJSONArray("bidresp");
        if (bidresp == null || bidresp.length() == 0) {
            return null;
        }
        Map<Integer, BidResponse> bidResponses = new HashMap<>();
        int len = bidresp.length();
        for (int i = 0; i < len; i++) {
            JSONObject object = bidresp.optJSONObject(i);
            if (object == null) {
                continue;
            }
            int iid = object.optInt("iid");
            String adm = object.optString("adm");
            if (TextUtils.isEmpty(adm)) {
                int nbr = object.optInt("nbr");
                String err = object.optString("err");
                DeveloperLog.LogD("Ins : " + iid + " bid failed cause" + " nbr : " + nbr + " err : " + err);
                continue;
            }
            BidResponse response = new BidResponse();
            response.setIid(iid);
            response.setPayLoad(adm);
            double price = object.optDouble("price", 0);
            String nurl = object.optString("nurl");
            if (!TextUtils.isEmpty(nurl) && nurl.contains(AUCTION_PRICE)) {
                nurl = nurl.replace(AUCTION_PRICE, String.valueOf(price));
            }
            response.setNurl(nurl);
            String lurl = object.optString("lurl");
            if (!TextUtils.isEmpty(lurl) && lurl.contains(AUCTION_PRICE)) {
                lurl = lurl.replace(AUCTION_PRICE, String.valueOf(price + 0.1));
            }
            response.setLurl(lurl);
            response.setPrice(price);
            response.setExpire(object.optInt("expire"));
            bidResponses.put(iid, response);
        }
        return bidResponses;
    }

    /**
     * Gets list ins result.
     *
     * @param clInfo    the cl info
     * @param placement the placement
     * @return the list ins result
     */
    public static List<Instance> getListInsResult(String reqId, JSONObject clInfo, Placement placement) {

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

        MediationRule mediationRule = getMediationRule(clInfo);
        int abt = clInfo.optInt("abt");
        List<Instance> instancesList = new ArrayList<>();
        for (int i = 0; i < insArray.length(); i++) {
            JSONObject insObject = insArray.optJSONObject(i);
            Instance instance = getInstance(reqId, placement, insObject, insMap, mediationRule, abt);
            if (instance != null) {
                instance.setIndex(i);
                instancesList.add(instance);
            }
        }
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
    public static BaseInstance[] getArrayInstances(String reqId, MediationRule mediationRule, JSONObject clInfo, Placement placement, int bs) {
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
            JSONObject insObject = insArray.optJSONObject(i);
            Instance instance = getInstance(reqId, placement, insObject, insMap, mediationRule, abt);
            if (instance != null) {
                instance.setBidResponse(null);
                instancesList.add(instance);
            }
        }
        //
        if (instancesList.size() == 0) {
            return new Instance[0];
        }

        return splitInsByBs(instancesList.toArray(new Instance[instancesList.size()]), bs);
    }

    private static Instance getInstance(String reqId, Placement placement, JSONObject insObject, SparseArray<BaseInstance> insMap, MediationRule mediationRule, int abt) {
        if (insObject != null) {
            int insId = insObject.optInt("id");
            Instance ins = (Instance) insMap.get(insId);
            if (ins != null) {
                ins.setWfAbt(abt);
                if (ins.getMediationState() != Instance.MEDIATION_STATE.AVAILABLE) {
                    ins.setPriority(insObject.optInt("i", -1));
                    ins.setRevenue(insObject.optDouble("r", 0d));
                    ins.setRevenuePrecision(insObject.optInt("rp", -1));
                    ins.setMediationRule(mediationRule);
                    ins.setReqId(reqId);
                }
            } else {
                JSONObject jsonObject = PlacementUtils.placementEventParams(placement.getId());
                JsonUtil.put(jsonObject, "iid", insId);
                JsonUtil.put(jsonObject, "reqId", reqId);
                if (mediationRule != null) {
                    JsonUtil.put(jsonObject, "ruleId", mediationRule.getId());
                }
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_NOT_FOUND, jsonObject);
            }
            return ins;
        }
        return null;
    }

    public static MediationRule getMediationRule(JSONObject clInfo) {
        JSONObject ruleObject = clInfo.optJSONObject("rule");
        MediationRule rule = null;
        if (ruleObject != null) {
            rule = MediationRule.create(ruleObject);
        }
        return rule;
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
