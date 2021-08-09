// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BidS2SAuctionManager {
    private final ConcurrentHashMap<String, List<BidResponse>> mS2SInstanceBidResponse;
    private final ConcurrentHashMap<String, AuctionCallback> mBidResultCallbacks;

    private static final class BidHolder {
        private static final BidS2SAuctionManager INSTANCE = new BidS2SAuctionManager();
    }

    private BidS2SAuctionManager() {
        mS2SInstanceBidResponse = new ConcurrentHashMap<>();
        mBidResultCallbacks = new ConcurrentHashMap<>();
    }

    public static BidS2SAuctionManager getInstance() {
        return BidHolder.INSTANCE;
    }

    /**
     * s2s
     */
    public void bid(final Context context, final String placementId, final String reqId, final int adType, final AuctionCallback callback) {
        resetBidResponse(placementId);
        ConcurrentHashMap<String, List<BaseInstance>> bidInstancesMap = BidAuctionManager.getInstance().getBidInstances();
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
                        BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(bidInstance.getMediationId());
                        if (bidAdapter == null) {
                            bidInstance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
                            continue;
                        }
                        // if MEDIATION_STATE is AVAILABLE, no bid
                        if (cacheAdsType && InsManager.isInstanceAvailable(bidInstance)) {
                            continue;
                        }
                        bidInstance.setReqId(reqId);
                        bidInstance.setBidState(BaseInstance.BID_STATE.BID_PENDING);
                        BidResponse response = getBidInstanceToken(context, bidInstance);
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
                }
            }
        });
    }

    private BidResponse getBidInstanceToken(Context context, BaseInstance bidInstance) {
        if (bidInstance == null) {
            return null;
        }
        BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(bidInstance.getMediationId());
        if (bidAdapter == null) {
            return null;
        }
        BidResponse bidResponse = null;
        try {
            String token = bidAdapter.getBiddingToken(context);
            if (TextUtils.isEmpty(token)) {
                return null;
            }
            bidResponse = new BidResponse();
            bidResponse.setIid(bidInstance.getId());
            bidResponse.setToken(token);
        } catch (Throwable throwable) {
            DeveloperLog.LogW("S2S bid error: " + throwable.toString());
            CrashUtil.getSingleton().saveException(throwable);
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
    }

    private synchronized void callbackBidResult(String placementId) {
        if (mBidResultCallbacks.containsKey(placementId)) {
            AuctionCallback callback = mBidResultCallbacks.get(placementId);
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
