// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;
import android.os.Looper;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.mediation.BidCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.event.AdvanceEventId;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BidC2SManager {

    private final ConcurrentHashMap<String, List<BaseInstance>> mBidInstances;
    private final ConcurrentHashMap<String, List<BaseInstance>> mSuccessInstances;
    private final ConcurrentHashMap<Integer, BidTimeout> mBidTimeoutRunnable;
    private final ConcurrentHashMap<String, BidResponseCallback> mBidResultCallbacks;
    private final HandlerUtil.HandlerHolder mHandler;

    private static final class BidHolder {
        private static final BidC2SManager INSTANCE = new BidC2SManager();
    }

    private BidC2SManager() {
        mBidInstances = new ConcurrentHashMap<>();
        mSuccessInstances = new ConcurrentHashMap<>();
        mBidTimeoutRunnable = new ConcurrentHashMap<>();
        mBidResultCallbacks = new ConcurrentHashMap<>();
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
    }

    public static BidC2SManager getInstance() {
        return BidHolder.INSTANCE;
    }

    /**
     * c2s
     */
    public void bid(final Context context, final List<BaseInstance> bidInstances, final String placementId, final String reqId, final int adType, final AdSize adSize, final BidResponseCallback callback) {
        resetBidResponse(placementId);
        if (bidInstances == null || bidInstances.isEmpty()) {
            if (callback != null) {
                callback.onBidC2SComplete(null);
            }
            return;
        }

        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mBidInstances.put(placementId, bidInstances);
                if (callback != null) {
                    mBidResultCallbacks.put(placementId, callback);
                }
                resetBidState(bidInstances);
                int biding = 0;
                for (BaseInstance bidInstance : bidInstances) {
                    if (bidInstance == null) {
                        continue;
                    }
                    CustomAdsAdapter adapter = AdapterUtil.getCustomAdsAdapter(bidInstance.getMediationId());
                    if (adapter == null) {
                        bidInstance.setBidState(BaseInstance.BID_STATE.BID_FAILED);
                        AdsUtil.advanceEventReport(bidInstance, AdvanceEventId.CODE_C2S_NO_ADAPTER,
                                AdvanceEventId.MSG_C2S_NO_ADAPTER);
                        continue;
                    }
                    biding++;
                    bidInstance.setReqId(reqId);
                    bidStart(bidInstance, bidInstance.getHbt());
                    executeBid(context, placementId, adType, adSize, bidInstance, adapter);
                }

                if (biding == 0 && callback != null) {
                    callback.onBidC2SComplete(null);
                }
            }
        });
    }

    /**
     * C2S bid start
     *
     * @param bidInstance bidInstance
     */
    private void bidStart(BaseInstance bidInstance, long delay) {
        InsManager.onInsBidStart(bidInstance);
        startTimeout(bidInstance, delay);
    }

    private void executeBid(Context context, String placementId, int adType, AdSize adSize, BaseInstance bidInstance, CustomAdsAdapter adapter) {
        HbCallback hbCallback = new HbCallback(bidInstance);
        try {
            adapter.getBidResponse(context, BidUtil.makeBidRequestInfo(placementId, bidInstance, adType, adSize),
                    hbCallback);
        } catch (Throwable e) {
            hbCallback.onBidFailed("C2S bid failed: " + bidInstance);
            DeveloperLog.LogW("C2S bid error: " + e.toString() + ", Instance: " + bidInstance);
            CrashUtil.getSingleton().saveException(e);
            AdsUtil.advanceEventReport(bidInstance, AdvanceEventId.CODE_C2S_BID_ERROR,
                    AdvanceEventId.MSG_C2S_BID_ERROR + e.getMessage());
        }
    }

    private void onBidResponse(BaseInstance instance, BidResponse response) {
        stopTimeout(instance);
        InsManager.onInsBidSuccess(instance, response);
    }

    private synchronized void bidSuccess(BaseInstance instance, BidResponse response) {
        if (instance.getBidState() != BaseInstance.BID_STATE.BID_PENDING) {
            return;
        }
        onBidResponse(instance, response);

        String placementId = instance.getPlacementId();

        // Add Success Instances
        List<BaseInstance> instanceList = mSuccessInstances.get(placementId);
        if (instanceList == null) {
            instanceList = new ArrayList<>();
        }
        instanceList.add(instance);
        mSuccessInstances.put(placementId, instanceList);
        if (isBidComplete(placementId)) {
            callbackBidResult(placementId);
        }
    }

    private void onBidFailed(BaseInstance instance, String error) {
        DeveloperLog.LogD(instance + " C2S Bid Failed: " + error);
        stopTimeout(instance);
        InsManager.onInsBidFailed(instance, error);
    }

    private synchronized void bidFailed(BaseInstance instance, String error) {
        onBidFailed(instance, error);
        String placementId = instance.getPlacementId();
        if (isBidComplete(placementId)) {
            callbackBidResult(placementId);
        }
    }

    private synchronized void callbackBidResult(String placementId) {
        if (mBidResultCallbacks.containsKey(placementId)) {
            BidResponseCallback callback = mBidResultCallbacks.get(placementId);
            if (callback != null) {
                List<BaseInstance> c2sInstances = mSuccessInstances.get(placementId);
                callback.onBidC2SComplete(c2sInstances);
                mBidResultCallbacks.remove(placementId);
            }
        }
    }

    private void startTimeout(BaseInstance instance, long delay) {
        BidTimeout timeout = mBidTimeoutRunnable.get(instance.getId());
        if (timeout == null) {
            timeout = new BidTimeout(instance);
            mBidTimeoutRunnable.put(instance.getId(), timeout);
        }
        mHandler.postDelayed(timeout, delay);
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

    private void resetBidState(List<BaseInstance> bidInstances) {
        for (BaseInstance instance : bidInstances) {
            instance.setReqId(null);
            instance.setBidState(BaseInstance.BID_STATE.NOT_BIDDING);
        }
    }

    private void resetBidResponse(String placementId) {
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
        public void onBidSuccess(BidResponse response) {
            getInstance().bidSuccess(mInstance, response);
        }

        @Override
        public void onBidFailed(String error) {
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
            AdsUtil.advanceEventReport(mInstance, AdvanceEventId.CODE_C2S_BID_TIMEOUT,
                    AdvanceEventId.MSG_C2S_BID_TIMEOUT);
        }
    }
}
