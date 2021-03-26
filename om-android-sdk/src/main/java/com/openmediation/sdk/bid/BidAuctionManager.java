// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.request.network.AdRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BidAuctionManager {

    private ConcurrentHashMap<String, List<BaseInstance>> mBidInstances;
    private ConcurrentHashMap<String, List<BidResponse>> mInstanceBidResponse;
    private ConcurrentHashMap<String, List<BidResponse>> mS2SInstanceBidResponse;
    private ConcurrentHashMap<Integer, BidTimeout> mBidTimeoutRunnable;
    private ConcurrentHashMap<String, AuctionCallback> mBidResultCallbacks;
    private ConcurrentHashMap<Integer, Long> mBidStartTime;
    private HandlerUtil.HandlerHolder mHandler;

    private static final class BidHolder {
        private static final BidAuctionManager INSTANCE = new BidAuctionManager();
    }

    private BidAuctionManager() {
        mBidInstances = new ConcurrentHashMap<>();
        mInstanceBidResponse = new ConcurrentHashMap<>();
        mS2SInstanceBidResponse = new ConcurrentHashMap<>();
        mBidTimeoutRunnable = new ConcurrentHashMap<>();
        mBidResultCallbacks = new ConcurrentHashMap<>();
        mBidStartTime = new ConcurrentHashMap<>();
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
    }

    public static BidAuctionManager getInstance() {
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
                    BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(instance.getMediationId());
                    if (bidAdapter != null) {
                        try {
                            bidAdapter.initBid(context, BidUtil.makeBidInitInfo(config, instance.getMediationId()),
                                    null);
                            bidInstances.add(instance);
                        } catch (Throwable throwable) {
                            DeveloperLog.LogE("initBid error: " + throwable.toString());
                            CrashUtil.getSingleton().saveException(throwable);
                        }
                    }
                }
            }
            if (bidInstances.size() > 0) {
                mBidInstances.put(placementEntry.getKey(), bidInstances);
            }
        }
    }

    public void bid(Context context, String placementId, String reqId, int adType, AuctionCallback callback) {
        bid(context, placementId, reqId, adType, null, callback);
    }

    /**
     * merge c2s / s2s
     */
    public void bid(Context context, String placementId, String reqId, int adType, AdSize adSize, AuctionCallback callback) {
        resetBidResponse(placementId);
        if (!mBidInstances.containsKey(placementId)) {
            if (callback != null) {
                callback.onBidComplete(null, null);
            }
            return;
        }
        List<BaseInstance> bidInstances = mBidInstances.get(placementId);
        if (bidInstances == null || bidInstances.isEmpty()) {
            if (callback != null) {
                callback.onBidComplete(null, null);
            }
            return;
        }
        if (callback != null) {
            mBidResultCallbacks.put(placementId, callback);
        }
        boolean cacheAdsType = PlacementUtils.isCacheAdsType(adType);
        resetBidState(cacheAdsType, bidInstances);
        int biding = 0;
        for (BaseInstance bidInstance : bidInstances) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(bidInstance.getMediationId());
            if (bidAdapter == null) {
                bidInstance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
                continue;
            }
            // if MEDIATION_STATE is AVAILABLE, no bid
            if (cacheAdsType && bidInstance instanceof Instance &&
                    Instance.MEDIATION_STATE.AVAILABLE == ((Instance) bidInstance).getMediationState()) {
                continue;
            }
            biding++;
            bidInstance.setReqId(reqId);
            bidInstance.setBidState(BaseInstance.BID_STATE.BID_PENDING);
            BidResponse response = getBidInstanceToken(context, bidInstance);
            if (response != null) {
                HbCallback hbCallback = new HbCallback(bidInstance, true);
                hbCallback.bidSuccess(response);
            } else {
                executeBid(context, adType, adSize, bidInstance, bidAdapter);
                mBidStartTime.put(bidInstance.getId(), System.currentTimeMillis());
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_REQUEST, bidInstance.buildReportData());
                startTimeout(bidInstance);
            }
        }

        if (biding == 0 && callback != null) {
            callback.onBidComplete(null, null);
        }
    }

    private void executeBid(Context context, int adType, AdSize adSize, BaseInstance bidInstance, BidAdapter bidAdapter) {
        HbCallback callback = new HbCallback(bidInstance, false);
        try {
            bidAdapter.executeBid(context, BidUtil.makeBidRequestInfo(bidInstance, adType, adSize),
                    callback);
        } catch (Throwable throwable) {
            callback.bidFailed("bid failed");
            DeveloperLog.LogE("bid error: " + throwable.toString());
            CrashUtil.getSingleton().saveException(throwable);
        }
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
            DeveloperLog.LogE("bid error: " + throwable.toString());
            CrashUtil.getSingleton().saveException(throwable);
        }
        return bidResponse;
    }

    public List<BidResponse> getBidToken(Context context, BaseInstance[] instances) {
        if (instances == null || instances.length <= 0) {
            return null;
        }
        List<BidResponse> result = new ArrayList<>();
        for (BaseInstance bidInstance : instances) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(bidInstance.getMediationId());
            if (bidAdapter == null) {
                continue;
            }
            String token = bidAdapter.getBiddingToken(context);
            if (TextUtils.isEmpty(token)) {
                continue;
            }
            BidResponse bidResponse = new BidResponse();
            bidResponse.setIid(bidInstance.getId());
            bidResponse.setToken(token);
            result.add(bidResponse);
        }
        return result;
    }

    public void notifyWin(BaseInstance instance) {
        if (BidAdapterUtil.hasBidAdapter(instance.getMediationId())) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(instance.getMediationId());
            if (bidAdapter != null) {
                bidAdapter.notifyWin(instance.getKey(), null);
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_WIN, instance.buildReportData());
            }
        }
    }

    void notifyWin(String url, BaseInstance instance) {
        AdRequest.get().url(url).performRequest(AdtUtil.getApplication());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_WIN, instance.buildReportData());
    }

    public void notifyLose(BaseInstance instance, int reason) {
        if (BidAdapterUtil.hasBidAdapter(instance.getMediationId())) {
            BidAdapter bidAdapter = BidAdapterUtil.getBidAdapter(instance.getMediationId());
            if (bidAdapter != null) {
                bidAdapter.notifyLose(instance.getKey(), makeNotifyMap(reason));
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_LOSE, instance.buildReportData());
            }
        }
    }

    private Map<String, Object> makeNotifyMap(int reason) {
        Map<String, Object> map = new HashMap<>();
        map.put(BidConstance.BID_NOTIFY_REASON, reason);
        return map;
    }

    void notifyLose(String url, BaseInstance instance) {
        AdRequest.get().url(url).performRequest(AdtUtil.getApplication());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_LOSE, instance.buildReportData());
    }

    private synchronized void bidSuccess(BaseInstance instance, BidResponse response, boolean isS2S) {
        instance.setBidState(BaseInstance.BID_STATE.BID_SUCCESS);
        if (isS2S) {
            List<BidResponse> responseList = mS2SInstanceBidResponse.get(instance.getPlacementId());
            if (responseList == null) {
                responseList = new ArrayList<>();
            }
            response.setIid(instance.getId());
            responseList.add(response);
            mS2SInstanceBidResponse.put(instance.getPlacementId(), responseList);
        } else {
            JSONObject jsonObject = instance.buildReportData();
            if (mBidStartTime != null && mBidStartTime.get(instance.getId()) != null) {
                long start = mBidStartTime.get(instance.getId());
                JsonUtil.put(jsonObject, "duration", (System.currentTimeMillis() - start) / 1000);
            }
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_RESPONSE, jsonObject);
            List<BidResponse> responseList = mInstanceBidResponse.get(instance.getPlacementId());
            if (responseList == null) {
                responseList = new ArrayList<>();
            }
            response.setIid(instance.getId());
            responseList.add(response);
            mInstanceBidResponse.put(instance.getPlacementId(), responseList);
            stopTimeout(instance);
        }
        if (isBidComplete(instance.getPlacementId())) {
            callbackBidResult(instance.getPlacementId());
        }
    }

    private synchronized void bidFailed(BaseInstance instance, String error) {
        instance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
        JSONObject jsonObject = instance.buildReportData();
        JsonUtil.put(jsonObject, "msg", error);
        if (mBidStartTime != null && mBidStartTime.get(instance.getId()) != null) {
            long start = mBidStartTime.get(instance.getId());
            JsonUtil.put(jsonObject, "duration", (System.currentTimeMillis() - start) / 1000);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_BID_FAILED, jsonObject);
        stopTimeout(instance);
        if (isBidComplete(instance.getPlacementId())) {
            callbackBidResult(instance.getPlacementId());
        }
    }

    private synchronized void callbackBidResult(String placementId) {
        if (mBidResultCallbacks.containsKey(placementId)) {
            AuctionCallback callback = mBidResultCallbacks.get(placementId);
            if (callback != null) {
                List<BidResponse> responseList = mInstanceBidResponse.get(placementId);
                List<BidResponse> s2sResponseList = mS2SInstanceBidResponse.get(placementId);
                callback.onBidComplete(responseList, s2sResponseList);
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
            if (cacheAdsType && isInstanceAvailable(instance)) {
                continue;
            }
            instance.setReqId(null);
            instance.setBidState(BaseInstance.BID_STATE.NOT_BIDDING);
        }
    }

    private boolean isInstanceAvailable(BaseInstance instance) {
        return instance instanceof Instance &&
                Instance.MEDIATION_STATE.AVAILABLE == ((Instance) instance).getMediationState();
    }

    private void resetBidResponse(String placementId) {
        if (mInstanceBidResponse != null) {
            mInstanceBidResponse.remove(placementId);
        }
        if (mS2SInstanceBidResponse != null) {
            mS2SInstanceBidResponse.remove(placementId);
        }
    }

    private static class HbCallback implements BidCallback {

        private BaseInstance mInstance;

        private boolean isS2S;

        HbCallback(BaseInstance instance, boolean isS2S) {
            mInstance = instance;
            this.isS2S = isS2S;
        }

        @Override
        public void bidSuccess(BidResponse response) {
            getInstance().bidSuccess(mInstance, response, isS2S);
        }

        @Override
        public void bidFailed(String error) {
            getInstance().bidFailed(mInstance, error);
        }
    }

    private static class BidTimeout implements Runnable {
        private BaseInstance mInstance;

        BidTimeout(BaseInstance instance) {
            mInstance = instance;
        }

        @Override
        public void run() {
            getInstance().bidFailed(mInstance, "timeout");
        }
    }
}
