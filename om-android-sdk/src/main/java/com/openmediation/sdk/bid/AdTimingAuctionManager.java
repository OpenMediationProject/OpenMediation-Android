// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.request.network.AdRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public void initBid(Context context, Configurations config) {
        if (config == null) {
            return;
        }

        Map<String, Placement> placementMap = config.getPls();
        if (placementMap == null || placementMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Placement> placementEntry : placementMap.entrySet()) {
            if (placementEntry == null) {
                continue;
            }
            List<BaseInstance> bidInstances = new ArrayList<>();
            SparseArray<BaseInstance> insMap = placementEntry.getValue().getInsMap();

            if (insMap == null || insMap.size() <= 0) {
                continue;
            }

            int size = insMap.size();
            for (int i = 0; i < size; i++) {
                BaseInstance instance = insMap.valueAt(i);
                if (instance == null) {
                    continue;
                }

                if (instance.getHb() == 1) {
                    bidInstances.add(instance);
                    BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(instance.getMediationId());
                    if (bidAdapter != null) {
                        bidAdapter.initBid(context, BidUtil.makeBidInitInfo(config, instance.getMediationId()),
                                null);
                    }
                }
            }
//            mBidInstances.put(placementEntry.getKey(), bidInstances);
        }
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
                bidInstance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
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

    public List<AdTimingBidResponse> getBidToken(Context context, BaseInstance[] instances) {
        if (instances == null || instances.length <= 0) {
            return null;
        }
        List<AdTimingBidResponse> result = new ArrayList<>();
        for (BaseInstance bidInstance : instances) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(bidInstance.getMediationId());
            if (bidAdapter == null) {
                continue;
            }
            String token = bidAdapter.getBiddingToken(context);
            if (TextUtils.isEmpty(token)) {
                continue;
            }
            AdTimingBidResponse bidResponse = new AdTimingBidResponse();
            bidResponse.setIid(bidInstance.getId());
            bidResponse.setToken(token);
            result.add(bidResponse);
        }
        return result;
    }

    void notifyWin(int abt, BaseInstance instance) {
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

    void notifyWin(String url, BaseInstance instance) {
        AdRequest.get().url(url).performRequest(AdtUtil.getApplication());
        JSONObject jsonObject = instance.buildReportData();
        JsonUtil.put(jsonObject, "abt", instance.getWfAbt());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_WIN, jsonObject);
    }

    void notifyLose(int abt, BaseInstance instance) {
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

    void notifyLose(int abt, String url, BaseInstance instance) {
        AdRequest.get().url(url).performRequest(AdtUtil.getApplication());
        JSONObject jsonObject = instance.buildReportData();
        JsonUtil.put(jsonObject, "abt", abt);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_LOSE, jsonObject);
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
