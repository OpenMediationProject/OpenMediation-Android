// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.os.Looper;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BidC2SAuctionManager {

    private final ConcurrentHashMap<String, List<BaseInstance>> mBidInstances;
    private final ConcurrentHashMap<String, List<BidResponse>> mInstanceBidResponse;
    private final ConcurrentHashMap<String, List<BaseInstance>> mSuccessInstances;
    private final ConcurrentHashMap<Integer, BidTimeout> mBidTimeoutRunnable;
    private final ConcurrentHashMap<String, AuctionCallback> mBidResultCallbacks;
    private final ConcurrentHashMap<Integer, Long> mBidStartTime;
    private final HandlerUtil.HandlerHolder mHandler;

    private static final class BidHolder {
        private static final BidC2SAuctionManager INSTANCE = new BidC2SAuctionManager();
    }

    private BidC2SAuctionManager() {
        mBidInstances = new ConcurrentHashMap<>();
        mInstanceBidResponse = new ConcurrentHashMap<>();
        mSuccessInstances = new ConcurrentHashMap<>();
        mBidTimeoutRunnable = new ConcurrentHashMap<>();
        mBidResultCallbacks = new ConcurrentHashMap<>();
        mBidStartTime = new ConcurrentHashMap<>();
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
    }

    public static BidC2SAuctionManager getInstance() {
        return BidHolder.INSTANCE;
    }

    /**
     * c2s
     */
    public void bid(final Context context, final List<BaseInstance> bidInstances, final String placementId, final String reqId, final int adType, final AdSize adSize, final AuctionCallback callback) {
        resetBidResponse(placementId);
        if (bidInstances == null || bidInstances.isEmpty()) {
            callback.onBidC2SComplete(null, null);
            return;
        }

        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mBidInstances.put(placementId, bidInstances);
                if (callback != null) {
                    mBidResultCallbacks.put(placementId, callback);
                }
                boolean cacheAdsType = PlacementUtils.isCacheAdsType(adType);
                resetBidState(cacheAdsType, bidInstances);
                int biding = 0;
                for (BaseInstance bidInstance : bidInstances) {
                    if (bidInstance == null) {
                        continue;
                    }
                    BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(bidInstance.getMediationId());
                    if (bidAdapter == null) {
                        bidInstance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
                        continue;
                    }
                    // if MEDIATION_STATE is AVAILABLE, no bid
                    if (cacheAdsType && InsManager.isInstanceAvailable(bidInstance)) {
                        continue;
                    }
                    biding++;
                    bidInstance.setReqId(reqId);
                    bidInstance.setBidState(BaseInstance.BID_STATE.BID_PENDING);
                    executeBid(context, placementId, adType, adSize, bidInstance, bidAdapter);
                    mBidStartTime.put(bidInstance.getId(), System.currentTimeMillis());
                    EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_REQUEST, InsManager.buildReportData(bidInstance));
                    startTimeout(bidInstance);
                }

                if (biding == 0 && callback != null) {
                    callback.onBidC2SComplete(bidInstances, null);
                }
            }
        });
    }

    private void executeBid(Context context, String placementId, int adType, AdSize adSize, BaseInstance bidInstance, BidAdapter bidAdapter) {
        HbCallback callback = new HbCallback(bidInstance);
        try {
            bidAdapter.executeBid(context, BidUtil.makeBidRequestInfo(placementId, bidInstance, adType, adSize),
                    callback);
        } catch (Throwable throwable) {
            callback.bidFailed("C2S bid failed");
            DeveloperLog.LogW("C2S bid error: " + throwable.toString());
            CrashUtil.getSingleton().saveException(throwable);
        }
    }

    private synchronized void bidSuccess(BaseInstance instance, BidResponse response) {
        // TODO
        if (instance.getBidState() != BaseInstance.BID_STATE.BID_PENDING) {
            return;
        }
        instance.setRevenue(response.getPrice());
        instance.setBidState(BaseInstance.BID_STATE.BID_SUCCESS);
        instance.setBidResponse(response);
        JSONObject jsonObject = InsManager.buildReportData(instance);
        if (mBidStartTime != null && mBidStartTime.get(instance.getId()) != null) {
            long start = mBidStartTime.get(instance.getId());
            JsonUtil.put(jsonObject, "duration", (System.currentTimeMillis() - start) / 1000);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_RESPONSE, jsonObject);

        String placementId = instance.getPlacementId();
        List<BidResponse> responseList = mInstanceBidResponse.get(placementId);
        if (responseList == null) {
            responseList = new ArrayList<>();
        }
        response.setIid(instance.getId());
        responseList.add(response);
        mInstanceBidResponse.put(placementId, responseList);

        // Add Success Instances
        List<BaseInstance> instanceList = mSuccessInstances.get(placementId);
        if (instanceList == null) {
            instanceList = new ArrayList<>();
        }
        instanceList.add(instance);
        mSuccessInstances.put(placementId, instanceList);
        stopTimeout(instance);
        if (isBidComplete(placementId)) {
            callbackBidResult(placementId);
        }
    }

    private synchronized void bidFailed(BaseInstance instance, String error) {
        DeveloperLog.LogD(instance + " C2S Bid Failed: " + error);
        instance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
        JSONObject jsonObject = InsManager.buildReportData(instance);
        JsonUtil.put(jsonObject, "msg", error);
        if (mBidStartTime != null && mBidStartTime.get(instance.getId()) != null) {
            long start = mBidStartTime.get(instance.getId());
            JsonUtil.put(jsonObject, "duration", (System.currentTimeMillis() - start) / 1000);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_FAILED, jsonObject);
        stopTimeout(instance);
        String placementId = instance.getPlacementId();
        if (isBidComplete(placementId)) {
            callbackBidResult(placementId);
        }
    }

    private synchronized void callbackBidResult(String placementId) {
        if (mBidResultCallbacks.containsKey(placementId)) {
            AuctionCallback callback = mBidResultCallbacks.get(placementId);
            if (callback != null) {
                List<BidResponse> responseList = mInstanceBidResponse.get(placementId);
                List<BaseInstance> c2sInstances = mSuccessInstances.get(placementId);
                callback.onBidC2SComplete(c2sInstances, responseList);
                mBidResultCallbacks.remove(placementId);
            }
        }
    }

    private void startTimeout(BaseInstance instance) {
        BidTimeout timeout = mBidTimeoutRunnable.get(instance.getId());
        if (timeout == null) {
            timeout = new BidTimeout(instance);
            mBidTimeoutRunnable.put(instance.getId(), timeout);
        }
        mHandler.postDelayed(timeout, instance.getHbt());
    }

    private void stopTimeout(BaseInstance instance) {
        BidTimeout timeout = mBidTimeoutRunnable.get(instance.getId());
        if (timeout != null) {
            mHandler.removeCallbacks(timeout);
            mBidTimeoutRunnable.remove(instance.getId());
        }
    }

    private synchronized boolean isBidComplete(String placementId) {
        List<BaseInstance> instances = mBidInstances.get(placementId);
        if (instances == null || instances.isEmpty()) {
            return true;
        }
        int success = 0;
        int failed = 0;
        for (BaseInstance instance : instances) {
            if (instance.getBidState() == BaseInstance.BID_STATE.BID_SUCCESS) {
                success++;
            } else if (instance.getBidState() == BaseInstance.BID_STATE.BID_FAILED) {
                failed++;
            }
        }
        return success + failed == instances.size();
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
        if (mInstanceBidResponse != null) {
            mInstanceBidResponse.remove(placementId);
        }
        if (mSuccessInstances != null) {
            mSuccessInstances.remove(placementId);
        }
    }

    private static class HbCallback implements BidCallback {

        private final BaseInstance mInstance;

        HbCallback(BaseInstance instance) {
            mInstance = instance;
        }

        @Override
        public void bidSuccess(BidResponse response) {
            getInstance().bidSuccess(mInstance, response);
        }

        @Override
        public void bidFailed(String error) {
            getInstance().bidFailed(mInstance, error);
        }
    }

    private static class BidTimeout implements Runnable {
        private final BaseInstance mInstance;

        BidTimeout(BaseInstance instance) {
            mInstance = instance;
        }

        @Override
        public void run() {
            getInstance().bidFailed(mInstance, "C2S Bid Failed: timeout");
        }
    }
}
