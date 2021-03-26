// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import android.app.Activity;
import android.os.Looper;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CallbackManager;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.InsUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public abstract class AbstractHybridAd extends AbstractAd {
    private volatile int mCanCallbackIndex;//index of current callback
    protected AtomicBoolean isRefreshTriggered = new AtomicBoolean(false);
    private HandlerUtil.HandlerHolder mHandler;
    private int mLoadedInsIndex = 0;

    protected abstract void loadInsOnUIThread(BaseInstance instances) throws Throwable;

    protected abstract boolean isInsReady(BaseInstance instances);

    protected void destroyAdEvent(BaseInstance instances) {
    }

    public AbstractHybridAd(Activity activity, String placementId) {
        super(activity, placementId);
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
        CallbackManager.getInstance().addCallback(placementId, this);
    }

    @Override
    protected void dispatchAdRequest() {
        mCanCallbackIndex = 0;
        mLoadedInsIndex = 0;
        //starts loading the 1st
        startNextInstance(0);
    }

    @Override
    public void destroy() {
        CallbackManager.getInstance().removeCallback(mPlacementId);
        super.destroy();
    }

    /**
     * 1.calls load method at the given instanceIndex
     * 2.checks totalInstances to see if any instance is available for starting
     */
    private void startNextInstance(int instanceIndex) {
        if (mTotalIns == null) {
            return;
        }

        mCanCallbackIndex = instanceIndex;

        boolean result = checkReadyInstance();
        if (result) {
            DeveloperLog.LogD("Ad is prepared for : " + mPlacementId + " callbackIndex is : " + mCanCallbackIndex);
            return;
        }

        try {
            if (mBs <= 0 || mTotalIns.length <= instanceIndex) {
                callbackAdErrorOnUiThread(ErrorCode.ERROR_NO_FILL);
                return;
            }

            //# of instances to load
            int pendingLoadInstanceCount = mBs;
            while (!hasCallbackToUser() && mTotalIns.length > instanceIndex && pendingLoadInstanceCount > 0) {
                final BaseInstance i = mTotalIns[instanceIndex];
                instanceIndex++;
                pendingLoadInstanceCount--;
                mLoadedInsIndex++;

                if (i == null) {
                    continue;
                }

                //blocked?
                if (AdRateUtil.shouldBlockInstance(mPlacementId + i.getKey(), i)) {
                    onInsError(i, ErrorCode.ERROR_NO_FILL);
                    continue;
                }

                try {
                    if (mBidResponses != null && mBidResponses.containsKey(i.getId())) {
                        i.setBidResponse(mBidResponses.get(i.getId()));
                    }
                    i.setReqId(mReqId);
                    loadInsOnUIThread(i);
                } catch (Throwable e) {
                    onInsError(i, e.getMessage());
                    DeveloperLog.LogD("load ins : " + i.toString() + " error ", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
            //no need to time out if a callback was given
            if (!hasCallbackToUser()) {
                //times out with the index of currently loaded instance
                startTimeout(instanceIndex);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("startNextInstance error", e);
        }
    }

    protected void errorCallbackReport(String error) {
        AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_ERROR, mPlacementId, null,
                new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD, error, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER));
    }

    protected synchronized void onInsReady(boolean doInsReadyReport, final BaseInstance instances, Object o) {
        if (doInsReadyReport) {
            DeveloperLog.LogD("do ins ready report");
            iReadyReport(instances);
        } else {
            DeveloperLog.LogD("do ins useless report");
        }
        if (instances.getHb() == 1) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_PAYLOAD_SUCCESS, instances.buildReportData());
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_SUCCESS, instances.buildReportData());
        }

        if (!isManualTriggered) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_RELOAD_SUCCESS, instances.buildReportData());
        }
        //saves every instance's ready data, mainly for Banner and Native
        instances.setObject(o);
        if (isFo || instances.getIndex() <= mCanCallbackIndex) {
            //gives ready callback without waiting for priority checking 
            placementReadyCallback(instances);
        } else {
            checkReadyInstance();
        }
    }

    @Override
    protected synchronized void onInsReady(String instanceKey, String instanceId, Object o) {
        super.onInsReady(instanceKey, instanceId, o);
        BaseInstance instances = InsUtil.getInsById(mTotalIns, instanceId);
        if (instances == null) {
            return;
        }
        onInsReady(true, instances, o);
    }

    @Override
    protected synchronized void onInsError(String instanceKey, String instanceId, String error) {
        super.onInsError(instanceKey, instanceId, error);
        BaseInstance instance = InsUtil.getInsById(mTotalIns, instanceId);
        if (instance == null) {
            return;
        }
        onInsError(instance, error);
    }

    @Override
    protected synchronized void onInsError(String instanceKey, String instanceId, AdapterError error) {
        super.onInsError(instanceKey, instanceId, error);
        BaseInstance instances = InsUtil.getInsById(mTotalIns, instanceId);
        if (instances == null) {
            return;
        }
        onInsError(instances, error);
    }

    protected synchronized void onInsError(BaseInstance instances, String error) {
        AdapterError adapterError = null;
        switch (getAdType()) {
            case CommonConstants.BANNER:
                adapterError = AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "", error);
                break;
            case CommonConstants.NATIVE:
                adapterError = AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "", error);
                break;
            case CommonConstants.SPLASH:
                adapterError = AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "", error);
                break;
        }
        if (adapterError != null) {
            onInsError(instances, adapterError);
        }
    }

    protected synchronized void onInsError(BaseInstance instances, AdapterError error) {
        if (instances == null) {
            return;
        }
        //handles load error only
        switch (getAdType()) {
            case CommonConstants.BANNER:
                AdLog.getSingleton().LogE("Banner Ad Load Failed: " + error.toString());
                break;
            case CommonConstants.NATIVE:
                AdLog.getSingleton().LogE("Native Ad Load Failed: " + error.toString());
                break;
            case CommonConstants.SPLASH:
                AdLog.getSingleton().LogE("Splash Ad Load Failed: " + error.toString());
                break;
        }
        instances.onInsLoadFailed(error);
        if (!isManualTriggered) {
            instances.onInsReLoadFailed(error);
        }
        internalInsError(instances, error.toString());
    }

    private void internalInsError(BaseInstance instance, String error) {
        addLoadFailedInstance(instance);
        notifyLoadFailedInsBidLose(instance);

        if (getAdType() == CommonConstants.BANNER) {
            //MopubBanner registered a receiver, we need to take care of it
            destroyAdEvent(instance);
        }
        DeveloperLog.LogD("load ins : " + instance.toString() + " error : " + error);

        int len = mTotalIns.length;
        //groupIndex of current failed instance
        int groupIndex = instance.getGrpIndex();
        //allInstanceGroup failed?
        boolean allInstanceGroupIsNull = true;
        //allInstanceGroup member failed?
        boolean allInstanceIsNullAtGroupIndex = true;
        //traverses to set all failed members to null
        for (int a = 0; a < len; a++) {
            BaseInstance i = mTotalIns[a];
            if (i == instance) {
                mTotalIns[a] = null;
            }

            if (mTotalIns[a] != null) {
                allInstanceGroupIsNull = false;
                if (i.getGrpIndex() != groupIndex) {
                    continue;
                }
                allInstanceIsNullAtGroupIndex = false;
            }
        }

        //gives no_fill  call back if allInstanceGroupIsNull
        if (allInstanceGroupIsNull && !hasCallbackToUser()) {
            callbackAdErrorOnUiThread(ErrorCode.ERROR_NO_FILL);
            cancelTimeout();
            return;
        }

        //
        if (allInstanceIsNullAtGroupIndex) {//only this group
            cancelTimeout();
            //loads the next group
            startNextInstance((groupIndex + 1) * mBs);
            return;
        }

        if (instance.isFirst()) {
            //e.g. assuming 0, 1, 2 in the group, bs is 3; when 0 fails, index moves forward by 2: 0 + 3 - 1 = 2
            DeveloperLog.LogD("first instance failed, add callbackIndex : " + instance.toString() + " error : " + error);
            mCanCallbackIndex = instance.getIndex() + mBs - 1;
            checkReadyInstance();
        }
    }

    @Override
    protected void onInstanceClick(String instanceKey, String instanceId) {
        DeveloperLog.LogD("onInstanceClick : " + instanceKey);
        BaseInstance instances = InsUtil.getInsById(mTotalIns, instanceId);
        if (instances == null) {
            return;
        }
        insClickReport(instances);
        callbackAdClickOnUiThread();
    }

    @Override
    protected synchronized void onInsShowSuccess(String instanceKey, String instanceId) {
        super.onInsShowSuccess(instanceKey, instanceId);
        BaseInstance instance = InsUtil.getInsById(mTotalIns, instanceId);
        if (instance == null) {
            return;
        }
        insImpReport(instance);
        onInsShowSuccess(instance);
        callbackAdShowedOnUiThread();
    }

    @Override
    protected synchronized void onInsShowFailed(String instanceKey, String instanceId, AdapterError error) {
        super.onInsShowFailed(instanceKey, instanceId, error);
        BaseInstance instance = InsUtil.getInsById(mTotalIns, instanceId);
        if (instance == null) {
            return;
        }
        instance.onInsShowFailed(error, null);
        callbackAdShowFailedOnUiThread(error == null ? "" : error.getMessage());
    }

    @Override
    protected synchronized void onInsClosed(String instanceKey, String instanceId) {
        super.onInsClosed(instanceKey, instanceId);
        BaseInstance instance = InsUtil.getInsById(mTotalIns, instanceId);
        if (instance == null) {
            return;
        }
        instance.onInsClose(null);
        callbackAdCloseOnUiThread();
    }

    /**
     * Releases all bu the ready instances' AdEvents. Currently for banner only
     */
    protected void releaseAdEvent() {
        if (mTotalIns == null) {
            return;
        }

        for (BaseInstance in : mTotalIns) {
            if (in == null) {
                continue;
            }
            if (in == mCurrentIns) {
                continue;
            }
            destroyAdEvent(in);
        }
    }

    private synchronized void placementReadyCallback(BaseInstance ins) {
        if (mTotalIns == null) {
            return;
        }
        if (mCurrentIns == null) {
            mCurrentIns = ins;
            cancelTimeout();
            aReadyReport();
            notifyUnLoadInsBidLose();
            callbackAdReadyOnUiThread();
        } else if (getAdType() == CommonConstants.BANNER) {
            mCurrentIns = ins;
            if (isRefreshTriggered.get()) {
                cancelTimeout();
                aReadyReport();
                notifyUnLoadInsBidLose();
            }
            callbackAdReadyOnUiThread();
        } else if (mCurrentIns.getIndex() > ins.getIndex()) {
            //Native and Banner doesn't update instance after receiving callback
            if (getAdType() == CommonConstants.NATIVE) {
                return;
            }
            mCurrentIns = ins;
        }
    }

    /**
     * Checks for ready instances between 0 and indexMap. Checks when
     * 1. a new loading sequence starts
     * 2. when the 1st instance in the group failed
     */
    private synchronized boolean checkReadyInstance() {
        try {
            if (mTotalIns == null) {
                return false;
            }
            //traverses to get smaller index ready instances
            for (BaseInstance i : mTotalIns) {
                if (i == null) {
                    continue;
                }
                //ignores trailing members
                if (i.getIndex() > mCanCallbackIndex) {
                    break;
                }
                if (isInsReady(i)) {
                    placementReadyCallback(i);
                    return true;
                }
            }
        } catch (Throwable e) {
            DeveloperLog.LogD("checkReadyInstancesOnUiThread error : " + e.getMessage());
            CrashUtil.getSingleton().saveException(e);
        }
        return false;
    }

    protected void notifyInsBidWin(BaseInstance instance) {
        if (mBidResponses == null || instance == null) {
            return;
        }
        if (!mBidResponses.containsKey(instance.getId())) {
            return;
        }
        BidResponse bidResponse = mBidResponses.get(instance.getId());
        if (bidResponse == null || bidResponse.isNotified()) {
            return;
        }
//        mBidResponses.remove(instance.getId());
        AuctionUtil.notifyWin(instance, bidResponse);
//        instance.setBidResponse(null);
    }

    private void notifyLoadFailedInsBidLose(BaseInstance instance) {
        if (mBidResponses == null || instance == null) {
            return;
        }
        if (!mBidResponses.containsKey(instance.getId())) {
            return;
        }
        BidResponse bidResponse = mBidResponses.get(instance.getId());
        mBidResponses.remove(instance.getId());
        if (bidResponse == null) {
            return;
        }
        AuctionUtil.notifyLose(instance, bidResponse, BidLoseReason.INTERNAL.getValue());
        instance.setBidResponse(null);
    }

    private void notifyUnLoadInsBidLose() {
        if (mTotalIns == null || mBidResponses == null) {
            return;
        }

        if (mLoadedInsIndex == mTotalIns.length) {
            return;
        }
        Map<BaseInstance, BidResponse> unLoadInsBidResponses = new HashMap<>();
        int len = mTotalIns.length;
        for (int i = mLoadedInsIndex; i < len; i++) {
            BaseInstance instance = mTotalIns[i];
            if (instance == null) {
                continue;
            }

            if (!mBidResponses.containsKey(instance.getId())) {
                continue;
            }
            unLoadInsBidResponses.put(instance, mBidResponses.get(instance.getId()));
            mBidResponses.remove(instance.getId());
            instance.setBidResponse(null);
        }
        if (!unLoadInsBidResponses.isEmpty()) {
            AuctionUtil.notifyLose(unLoadInsBidResponses, BidLoseReason.LOST_TO_HIGHER_BIDDER.getValue());
        }
    }

    private void startTimeout(int insIndex) {
        TimeoutRunnable timeout = new TimeoutRunnable(insIndex);
        mHandler.postDelayed(timeout, mPt * 1000L);
    }

    private void cancelTimeout() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private class TimeoutRunnable implements Runnable {

        private int insIndex;

        TimeoutRunnable(int insIndex) {
            this.insIndex = insIndex;
        }

        @Override
        public void run() {
            DeveloperLog.LogD("timeout startNextInstance : " + insIndex);
            startNextInstance(insIndex);
        }
    }
}
