// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.event.AdvanceEventId;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BidS2SManager {
    private final ConcurrentHashMap<String, List<BidResponse>> mS2SInstanceBidResponse;
    private final ConcurrentHashMap<String, BidResponseCallback> mBidResultCallbacks;

    private static final class BidHolder {
        private static final BidS2SManager INSTANCE = new BidS2SManager();
    }

    private BidS2SManager() {
        mS2SInstanceBidResponse = new ConcurrentHashMap<>();
        mBidResultCallbacks = new ConcurrentHashMap<>();
    }

    public static BidS2SManager getInstance() {
        return BidHolder.INSTANCE;
    }

    /**
     * s2s
     */
    public void bid(final Context context, final String placementId, final String reqId, final int adType, final BidResponseCallback callback) {
        resetBidResponse(placementId);
        ConcurrentHashMap<String, List<BaseInstance>> bidInstancesMap = BidManager.getInstance().getBidInstances();
        if (!bidInstancesMap.containsKey(placementId)) {
            if (callback != null) {
                callback.onBidS2SComplete(null);
            }
            return;
        }
        final List<BaseInstance> bidInstances = bidInstancesMap.get(placementId);
        if (bidInstances == null || bidInstances.isEmpty()) {
            if (callback != null) {
                callback.onBidS2SComplete(null);
            }
            return;
        }

        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (callback != null) {
                        mBidResultCallbacks.put(placementId, callback);
                    }
                    boolean cacheAdsType = PlacementUtils.isCacheAdsType(adType);
                    resetBidState(cacheAdsType, bidInstances);
                    for (BaseInstance bidInstance : bidInstances) {
                        CustomAdsAdapter adapter = AdapterUtil.getCustomAdsAdapter(bidInstance.getMediationId());
                        if (adapter == null) {
                            bidInstance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
                            AdsUtil.advanceEventReport(bidInstance, AdvanceEventId.CODE_S2S_NO_ADAPTER,
                                    AdvanceEventId.MSG_S2S_NO_ADAPTER);
                            continue;
                        }
                        // 判断是否 s2s
                        if (!adapter.isS2S()) {
                            continue;
                        }
                        // if MEDIATION_STATE is AVAILABLE, no bid
                        if (cacheAdsType && InsManager.isInstanceAvailable(bidInstance)) {
                            continue;
                        }
                        bidInstance.setReqId(reqId);
                        bidInstance.setBidState(BaseInstance.BID_STATE.BID_PENDING);
                        BidResponse response = getBidInstanceToken(context, adapter, bidInstance);
                        if (response != null) {
                            bidSuccess(bidInstance, response);
                        } else {
                            bidFailed(bidInstance, "No BidToken");
                        }
                    }
                    callbackBidResult(placementId);
                } catch (Throwable e) {
                    DeveloperLog.LogW("S2S Failed: " + e.getMessage());
                    callbackBidResult(placementId);
                    AdsUtil.advanceEventReport(placementId, AdvanceEventId.CODE_S2S_BID_ERROR,
                            AdvanceEventId.MSG_S2S_BID_ERROR + e.getMessage());
                }
            }
        });
    }

    private BidResponse getBidInstanceToken(Context context, CustomAdsAdapter adapter, BaseInstance bidInstance) {
        if (bidInstance == null) {
            return null;
        }
        BidResponse bidResponse = null;
        try {
            String token = adapter.getBiddingToken(context);
            if (TextUtils.isEmpty(token)) {
                return null;
            }
            bidResponse = new BidResponse();
            bidResponse.setIid(bidInstance.getId());
            bidResponse.setToken(token);
        } catch (Throwable throwable) {
            DeveloperLog.LogW("S2S bid error: " + throwable.toString());
            CrashUtil.getSingleton().saveException(throwable);
            AdsUtil.advanceEventReport(bidInstance, AdvanceEventId.CODE_S2S_GET_TOKEN_ERROR,
                    AdvanceEventId.MSG_S2S_GET_TOKEN_ERROR + throwable.getMessage());
        }
        return bidResponse;
    }

    private synchronized void bidSuccess(BaseInstance instance, BidResponse response) {
        instance.setBidState(BaseInstance.BID_STATE.BID_SUCCESS);
        List<BidResponse> responseList = mS2SInstanceBidResponse.get(instance.getPlacementId());
        if (responseList == null) {
            responseList = new ArrayList<>();
        }
        response.setIid(instance.getId());
        responseList.add(response);
        mS2SInstanceBidResponse.put(instance.getPlacementId(), responseList);
    }

    private synchronized void bidFailed(BaseInstance instance, String error) {
        DeveloperLog.LogD(instance + " S2S Bid Failed: " + error);
        instance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
        // reset BidResponse
        instance.setBidResponse(null);
    }

    private synchronized void callbackBidResult(String placementId) {
        if (mBidResultCallbacks.containsKey(placementId)) {
            BidResponseCallback callback = mBidResultCallbacks.get(placementId);
            if (callback != null) {
                List<BidResponse> s2sResponseList = mS2SInstanceBidResponse.get(placementId);
                callback.onBidS2SComplete(s2sResponseList);
                mBidResultCallbacks.remove(placementId);
            }
        }
    }

    private void resetBidState(boolean cacheAdsType, List<BaseInstance> bidInstances) {
        for (BaseInstance instance : bidInstances) {
            if (cacheAdsType && InsManager.isInstanceAvailable(instance)) {
                continue;
            }
            instance.setReqId(null);
            instance.setBidState(BaseInstance.BID_STATE.NOT_BIDDING);
        }
    }

    private void resetBidResponse(String placementId) {
        mS2SInstanceBidResponse.remove(placementId);
    }

}
