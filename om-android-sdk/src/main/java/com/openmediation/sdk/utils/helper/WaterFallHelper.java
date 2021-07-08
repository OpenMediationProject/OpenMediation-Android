// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.helper;

import android.text.TextUtils;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Water fall helper.
 */
public class WaterFallHelper {
    private static final String AUCTION_PRICE = "${AUCTION_PRICE}";
    //testing instance
    private static final Map<String, List<BaseInstance>> testInstanceMap = new HashMap<>();

    /**
     * Sets test instance.
     *
     * @param placementId   the placement id
     * @param testInstances the test instances
     */
    public static void setTestInstance(String placementId, List<BaseInstance> testInstances) {
        testInstanceMap.put(placementId, testInstances);
    }

    /**
     * Clean test instance.
     */
    public static void cleanTestInstance() {
        testInstanceMap.clear();
    }

    public static Map<String, List<BaseInstance>> getTestInstanceMap() {
        return testInstanceMap;
    }


    /**
     * Wf request.
     *
     * @param type      the type
     * @param s2sResult the response list
     * @param callback  the callback
     * @param reqId     the reqId
     */
    public static void wfRequest(final PlacementInfo info, final OmManager.LOAD_TYPE type,
                                 final List<BidResponse> c2sResult,
                                 final List<BidResponse> s2sResult,
                                 final List<InstanceLoadStatus> statusList,
                                 final String reqId,
                                 final Request.OnRequestCallback callback) {

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
                            .callback(callback).performRequest(AdtUtil.getInstance().getApplicationContext());
                } catch (Exception e) {
                    DeveloperLog.LogE("WaterFall Error: " + e.getMessage());
                    CrashUtil.getSingleton().saveException(e);
                    callback.onRequestFailed("Load failed: unknown exception occurred");
                }
            }
        });
    }

    public static Map<Integer, BidResponse> getS2sBidResponse(Placement placement, JSONObject clInfo) {
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
            BaseInstance instance = InsManager.getInsById(placement, String.valueOf(iid));
            if (instance != null) {
                instance.setBidResponse(response);
            }
            bidResponses.put(iid, response);
        }
        return bidResponses;
    }


    public static MediationRule getMediationRule(JSONObject clInfo) {
        JSONObject ruleObject = clInfo.optJSONObject("rule");
        MediationRule rule = null;
        if (ruleObject != null) {
            rule = MediationRule.create(ruleObject);
        }
        return rule;
    }

}
