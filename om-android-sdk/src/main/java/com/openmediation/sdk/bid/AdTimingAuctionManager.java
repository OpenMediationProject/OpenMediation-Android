// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.os.Looper;

import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AdTimingAuctionManager {

    private ConcurrentHashMap<String, BaseInstance[]> mBidInstances;
    private ConcurrentHashMap<String, List<AdTimingBidResponse>> mInstanceBidResponse;
    private ConcurrentHashMap<Integer, BidTimeout> mBidTimeoutRunnable;
    private ConcurrentHashMap<String, AuctionCallback> mBidResultCallbacks;
    private ConcurrentHashMap<Integer, Long> mBidStartTime;
    private HandlerUtil.HandlerHolder mHandler;

    private static final class BidHolder {
        private static final AdTimingAuctionManager INSTANCE = new AdTimingAuctionManager();
    }

    private AdTimingAuctionManager() {
        mBidInstances = new ConcurrentHashMap<>();
        mInstanceBidResponse = new ConcurrentHashMap<>();
        mBidTimeoutRunnable = new ConcurrentHashMap<>();
        mBidResultCallbacks = new ConcurrentHashMap<>();
        mBidStartTime = new ConcurrentHashMap<>();
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
    }

    public static AdTimingAuctionManager getInstance() {
        return BidHolder.INSTANCE;
    }

    public void bid(Context context, String placementId, BaseInstance[] instances, int abt, int adType,
                    AuctionCallback callback) {
        if (instances == null || instances.length <= 0) {
            if (callback != null) {
                callback.onBidComplete(null);
            }
            return;
        }
        mBidInstances.put(placementId, instances);
        mBidResultCallbacks.put(placementId, callback);
        resetBidState(instances);
        int biding = 0;
        for (BaseInstance bidInstance : instances) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(bidInstance.getMediationId());
            if (bidAdapter == null) {
                continue;
            }
            biding++;
            bidInstance.setBidState(BaseInstance.BID_STATE.BID_PENDING);
            bidAdapter.executeBid(context, BidUtil.makeBidRequestInfo(bidInstance, adType),
                    new HbCallback(abt, bidInstance));
            mBidStartTime.put(bidInstance.getId(), System.currentTimeMillis());
            JSONObject jsonObject = bidInstance.buildReportData();
            JsonUtil.put(jsonObject, "abt", abt);
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_REQUEST, jsonObject);
            startTimeout(abt, bidInstance);
        }

        if (biding == 0) {
            if (callback != null) {
                callback.onBidComplete(null);
            }
        }
    }

    public void notifyWin(int abt, BaseInstance instance) {
        if (BidAdapterUtil.hasBidAdapter(instance.getMediationId())) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(instance.getMediationId());
            if (bidAdapter != null) {
                bidAdapter.notifyWin(instance.getKey());
                JSONObject jsonObject = instance.buildReportData();
                JsonUtil.put(jsonObject, "abt", abt);
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_WIN, jsonObject);
            }
        }
    }

    public void notifyLose(int abt, BaseInstance instance) {
        if (BidAdapterUtil.hasBidAdapter(instance.getMediationId())) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(instance.getMediationId());
            if (bidAdapter != null) {
                bidAdapter.notifyLose(instance.getKey());
                JSONObject jsonObject = instance.buildReportData();
                JsonUtil.put(jsonObject, "abt", abt);
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_LOSE, jsonObject);
            }
        }
    }

    private synchronized void bidSuccess(int abt, BaseInstance instance, AdTimingBidResponse response) {
        instance.setBidState(BaseInstance.BID_STATE.BID_SUCCESS);
        JSONObject jsonObject = instance.buildReportData();
        JsonUtil.put(jsonObject, "abt", abt);
        if (mBidStartTime != null && mBidStartTime.get(instance.getId()) != null) {
            long start = mBidStartTime.get(instance.getId());
            JsonUtil.put(jsonObject, "duration", (System.currentTimeMillis() - start) / 1000);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_RESPONSE, jsonObject);
        List<AdTimingBidResponse> responseList = mInstanceBidResponse.get(instance.getPlacementId());
        if (responseList == null) {
            responseList = new ArrayList<>();
        } else {
            responseList.clear();
        }
        response.setIid(instance.getId());
        responseList.add(response);
        mInstanceBidResponse.put(instance.getPlacementId(), responseList);
        stopTimeout(instance);
        if (isBidComplete(instance.getPlacementId())) {
            callbackBidResult(instance.getPlacementId());
            mBidInstances.remove(instance.getPlacementId());
        }
    }

    private synchronized void bidFailed(int abt, BaseInstance instance, String error) {
        instance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
        JSONObject jsonObject = instance.buildReportData();
        JsonUtil.put(jsonObject, "msg", error);
        JsonUtil.put(jsonObject, "abt", abt);
        if (mBidStartTime != null && mBidStartTime.get(instance.getId()) != null) {
            long start = mBidStartTime.get(instance.getId());
            JsonUtil.put(jsonObject, "duration", (System.currentTimeMillis() - start) / 1000);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_FAILED, jsonObject);
        stopTimeout(instance);
        if (isBidComplete(instance.getPlacementId())) {
            callbackBidResult(instance.getPlacementId());
            mBidInstances.remove(instance.getPlacementId());
        }
    }

    private synchronized void callbackBidResult(String placementId) {
        if (mBidResultCallbacks.containsKey(placementId)) {
            AuctionCallback callback = mBidResultCallbacks.get(placementId);
            if (callback != null) {
                List<AdTimingBidResponse> responseList = mInstanceBidResponse.get(placementId);
                callback.onBidComplete(responseList);
                mBidResultCallbacks.remove(placementId);
            }
        }
    }

    private void startTimeout(int abt, BaseInstance instance) {
        BidTimeout timeout = mBidTimeoutRunnable.get(instance.getId());
        if (timeout == null) {
            timeout = new BidTimeout(abt, instance);
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
        BaseInstance[] instances = mBidInstances.get(placementId);
        if (instances == null || instances.length <= 0) {
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

        return success + failed == instances.length;
    }

    private void resetBidState(BaseInstance[] bidInstances) {
        for (BaseInstance instance : bidInstances) {
            instance.setBidState(BaseInstance.BID_STATE.NOT_BIDDING);
        }
    }

    private static class HbCallback implements BidCallback {

        private BaseInstance mInstance;
        private int abt;

        HbCallback(int abt, BaseInstance instance) {
            mInstance = instance;
            this.abt = abt;
        }

        @Override
        public void bidSuccess(AdTimingBidResponse response) {
            getInstance().bidSuccess(abt, mInstance, response);
        }

        @Override
        public void bidFailed(String error) {
            getInstance().bidFailed(abt, mInstance, error);
        }
    }

    private static class BidTimeout implements Runnable {
        private BaseInstance mInstance;
        private int abt;

        BidTimeout(int abt, BaseInstance instance) {
            mInstance = instance;
            this.abt = abt;
        }

        @Override
        public void run() {
            getInstance().bidFailed(abt, mInstance, "timeout");
        }
    }
}
