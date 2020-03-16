// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import android.app.Activity;
import android.os.Looper;

import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.mediation.CallbackManager;
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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public abstract class AbstractHybridAd extends AbstractAd {
    private volatile int mCanCallbackIndex;//index of current callback
    protected AtomicBoolean isRefreshTriggered = new AtomicBoolean(false);
    private HandlerUtil.HandlerHolder mHandler;

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
        //starts loading the 1st
        startNextInstance(0);
    }

    @Override
    public void destroy() {
        CallbackManager.getInstance().removeCallback(mPlacementId);
        super.destroy();
    }

    /**
     * 1、calls load method at the given instanceIndex
     * 2、checks totalInstances to see if any instance is available for starting
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

                if (i == null) {
                    continue;
                }

                //blocked?
                if (AdRateUtil.shouldBlockInstance(mPlacementId + i.getKey(), i)) {
                    onInsError(i, ErrorCode.ERROR_NO_FILL);
                    continue;
                }

                try {
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
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_SUCCESS, instances.buildReportData());
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
        BaseInstance instances = InsUtil.getInsById(mTotalIns, instanceId);
        if (instances == null) {
            return;
        }
        onInsError(instances, error);
    }

    protected synchronized void onInsError(BaseInstance instances, String error) {
        if (instances == null) {
            return;
        }
        //handles load error only
        instances.onInsLoadFailed(error);

        if (getAdType() == CommonConstants.BANNER) {
            //MopubBanner registered a receiver, we need to take care of it
            destroyAdEvent(instances);
        }
        DeveloperLog.LogD("load ins : " + instances.toString() + " error : " + error);

        int len = mTotalIns.length;
        //groupIndex of current failed instance
        int groupIndex = instances.getGrpIndex();
        //allInstanceGroup failed?
        boolean allInstanceGroupIsNull = true;
        //allInstanceGroup member failed?
        boolean allInstanceIsNullAtGroupIndex = true;
        //traverses to set all failed members to null
        for (int a = 0; a < len; a++) {
            BaseInstance i = mTotalIns[a];
            if (i == instances) {
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

        if (instances.isFirst()) {
            //e.g. assuming 0, 1, 2 in the group, bs is 3; when 0 fails, index moves forward by 2: 0 + 3 - 1 = 2
            DeveloperLog.LogD("first instance failed, add callbackIndex : " + instances.toString() + " error : " + error);
            mCanCallbackIndex = instances.getIndex() + mBs - 1;
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
            instanceNotifyBidLose();
            callbackAdReadyOnUiThread();
        } else if (getAdType() == CommonConstants.BANNER) {
            mCurrentIns = ins;
            if (isRefreshTriggered.get()) {
                cancelTimeout();
                aReadyReport();
                instanceNotifyBidLose();
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

    private void instanceNotifyBidLose() {
        if (mTotalIns == null) {
            return;
        }
        AuctionUtil.instanceNotifyBidLose(mBidResponses, mPlacement);
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
