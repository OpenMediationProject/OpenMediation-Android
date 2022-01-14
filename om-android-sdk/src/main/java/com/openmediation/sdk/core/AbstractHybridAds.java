package com.openmediation.sdk.core;

import android.os.Looper;
import android.text.TextUtils;

import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.bid.BidUtil;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.AdvanceEventId;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.helper.LrReportHelper;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Scene;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractHybridAds extends AbstractAdsApi {
    /**
     * The M load ts.
     */
    protected long mLoadTs;
    /**
     * The M callback ts.
     */
    protected long mCallbackTs;

    /**
     * The M current ins.
     */
    protected BaseInstance mCurrentIns;


    /**
     * The M bs.
     */
    int mBs;
    /**
     * The Is fo.
     */
    boolean isFo;
    /**
     * The M pt.
     */
    int mPt;

    private final HandlerUtil.HandlerHolder mHandler;
    private int mLoadedInsIndex = 0;
    private int mCanCallbackIndex;//index of current callback
    private int mCurrentLoadGroupIndex = -1;
    private Map<Integer, TimeoutRunnable> mTimeouts = new HashMap<>();

    public AbstractHybridAds(String placementId) {
        super();
        this.mPlacementId = placementId;
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
        setCurrentPlacement(PlacementUtils.getPlacement(placementId));
    }

    /**
     * On ad error callback.
     *
     * @param error the error
     */
    protected abstract void onAdErrorCallback(Error error);

    /**
     * On ad ready callback.
     */
    protected abstract void onAdReadyCallback();

    protected void destroyAdEvent(BaseInstance instance) {
        AdsUtil.advanceEventReport(instance, AdvanceEventId.CODE_INS_DESTROY,
                AdvanceEventId.MSG_INS_DESTROY);
    }

    /**
     * On ad show success callback.
     */
    protected void onAdShowedCallback() {
        if (mCurrentIns != null) {
            notifyInsBidWin(mCurrentIns);
        }
    }

    /**
     * On ad show failed callback.
     */
    protected void onAdShowFailedCallback(Error error) {
    }

    protected void onViewAttachToWindow() {
        if (mCurrentIns == null) {
            return;
        }
        onInsShowSuccess(mCurrentIns, null);
    }

    protected void onViewDetachFromWindow() {
        onInsClosed(mCurrentIns, null);
    }

    protected void destroy() {
        for (BaseInstance in : mTotalIns) {
            if (in == null) {
                continue;
            }
            destroyAdEvent(in);
        }
        mTotalIns.clear();
        mBs = 0;
        mPt = 0;
        isFo = false;
        EventUploadManager.getInstance().uploadEvent(EventId.DESTROY, TextUtils.isEmpty(mPlacementId) ?
                PlacementUtils.placementEventParams(mPlacementId) : null);
        mCurrentIns = null;
        isDestroyed = true;
    }

    @Override
    public boolean isInventoryAdsType() {
        return false;
    }

    @Override
    protected boolean isReload() {
        return !isManualTriggered;
    }

    @Override
    protected void resetBeforeGetInsOrder() {
        mLoadTs = System.currentTimeMillis();
        mCanCallbackIndex = 0;
        mLoadedInsIndex = 0;
        mCurrentLoadGroupIndex = -1;
        mTimeouts.clear();
        mBs = mPlacement.getBs();
        mPt = mPlacement.getPt();
        isFo = mPlacement.getFo() == 1;
        if (mBs == 0) {
            mBs = 3;
        }
    }

    @Override
    protected void startLoadAdsImpl(JSONObject clInfo, List<BaseInstance> totalIns) {
        mTotalIns.clear();
        mTotalIns.addAll(totalIns);
        InsManager.resetInsStateOnClResponse(mTotalIns);
        if (mPlacement != null) {
            WaterFallHelper.getS2sBidResponse(mPlacement, clInfo);
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startNextInstance(0);
            }
        });
    }

    /**
     * SDK Check Error
     */
    protected synchronized void onInsError(BaseInstance instance, String error) {
        if (instance == null) {
            return;
        }
        String adType = PlacementUtils.getPlacementType(getPlacementType());
        String adapterName = instance.getAdapter() == null ? "" : instance.getAdapter().getClass().getSimpleName();
        AdapterError adapterError = AdapterErrorBuilder.buildLoadCheckError(
                adType, adapterName, error);
        onInsLoadFailed(instance, adapterError, !isManualTriggered);
    }

    @Override
    protected synchronized void onInsLoadSuccess(BaseInstance instances, boolean reload) {
        super.onInsLoadSuccess(instances, reload);
        if (isFo || instances.getIndex() <= mCanCallbackIndex) {
            //gives ready callback without waiting for priority checking
            placementReadyCallback(instances);
        } else {
            checkReadyInstance();
        }
    }

    /**
     * Instance-level show success
     *
     * @param instance the instance
     */
    @Override
    protected void onInsShowSuccess(BaseInstance instance, Scene scene) {
        super.onInsShowSuccess(instance, scene);
        callbackAdShowedOnUiThread();
        AdRateUtil.onInstancesShowed(instance.getPlacementId(), instance.getKey());
    }

    @Override
    protected void onInsShowFailed(BaseInstance instance, AdapterError error, Scene scene) {
        super.onInsShowFailed(instance, error, scene);
        String msg = error == null ? "" : error.getMessage();
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER, msg, -1);
        callbackAdShowFailedOnUiThread(errorResult);
    }

    /**
     * Ad open callback
     */
    void callbackAdShowedOnUiThread() {
        if (isDestroyed) {
            return;
        }
        onAdShowedCallback();
    }

    /**
     * Ad open failed callback
     */
    void callbackAdShowFailedOnUiThread(final Error error) {
        if (isDestroyed) {
            return;
        }
        onAdShowFailedCallback(error);
    }

    /**
     * Checks if callback has been triggered
     *
     * @return the boolean
     */
    boolean hasCallbackToUser() {
        return mLoadTs <= mCallbackTs;
    }

    @Override
    protected void callbackLoadError(Error error) {
        super.callbackLoadError(error);
        if (mLoadTs > mCallbackTs) {
            mCallbackTs = System.currentTimeMillis();
        }
        onAdErrorCallback(error);
    }

    /**
     * Ad loading success callback
     */
    void callbackAdReady() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD("Ad load success placementId: " + mPlacementId);
        if (mLoadTs > mCallbackTs) {
            mCallbackTs = System.currentTimeMillis();
        }
        onAdReadyCallback();
    }

    @Override
    protected void onInsLoadFailed(BaseInstance instance, AdapterError error, boolean reload) {
        super.onInsLoadFailed(instance, error, reload);

        //MoPubBanner registered a receiver, we need to take care of it
        destroyAdEvent(instance);
        DeveloperLog.LogD("load ins : " + instance.toString() + " error : " + error);

        //allInstanceGroup failed?
        int allFailedCount = InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.LOAD_FAILED);
        boolean allInstanceGroupIsFailed = allFailedCount == mTotalIns.size();

        //gives no_fill call back if allInstanceGroupIsNull
        if (allInstanceGroupIsFailed && !hasCallbackToUser()) {
            Error errorResult = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD, ErrorCode.MSG_LOAD_NO_AVAILABLE_AD + "All ins load failed, PlacementId: " + mPlacementId, -1);
            DeveloperLog.LogE(errorResult.toString());
            callbackLoadError(errorResult);
            finishLoad(errorResult);
            cancelTimeout(true, -1);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_INS_GROUP_LOAD_FAILED,
                    AdvanceEventId.MSG_INS_GROUP_LOAD_FAILED);
            return;
        }

        //groupIndex of current failed instance
        int groupIndex = instance.getGrpIndex();
        List<BaseInstance> groupInsList = new CopyOnWriteArrayList<>();
        for (BaseInstance ins : mTotalIns) {
            if (ins != null && ins.getGrpIndex() == groupIndex) {
                groupInsList.add(ins);
            }
        }
        //allInstanceGroup member failed?
        int groupFailedCount = InsManager.instanceCount(groupInsList, BaseInstance.MEDIATION_STATE.LOAD_FAILED);
        boolean allInstanceIsFailedAtGroup = groupFailedCount == groupInsList.size();

        if (allInstanceIsFailedAtGroup) {//only this group
            cancelTimeout(false, groupIndex);
            //loads the next group
            if (groupIndex == mCurrentLoadGroupIndex) {
                startNextInstance((groupIndex + 1) * mBs);
            }
            return;
        }

        if (instance.isFirst()) {
            //e.g. assuming 0, 1, 2 in the group, bs is 3; when 0 fails, index moves forward by 2: 0 + 3 - 1 = 2
            DeveloperLog.LogD("first instance failed, add callbackIndex : " + instance.toString() + " error : " + error);
            mCanCallbackIndex = instance.getIndex() + mBs - 1;
            checkReadyInstance();
        }
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
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_HAS_READY_INSTANCE,
                    AdvanceEventId.MSG_HAS_READY_INSTANCE);
            return;
        }

        try {
            int size = mTotalIns.size();
            if (mBs <= 0 || size <= instanceIndex) {
                Error errorResult = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD, ErrorCode.ERROR_NO_FILL + "No available instance", -1);
                callbackLoadError(errorResult);
                finishLoad(errorResult);
                DeveloperLog.LogE(errorResult.toString());
                AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_LOAD_INS_INDEX_OOB,
                        AdvanceEventId.MSG_LOAD_INS_INDEX_OOB + "bs = " + mBs + ", size = " + size + ", loadIndex = " + instanceIndex);
                return;
            }

            //# of instances to load
            int pendingLoadInstanceCount = mBs;
            int groupLoadCount = 0;
            while (!hasCallbackToUser() && size > instanceIndex && pendingLoadInstanceCount > 0) {
                final BaseInstance instance = mTotalIns.get(instanceIndex);
                instanceIndex++;
                pendingLoadInstanceCount--;
                mLoadedInsIndex++;

                if (instance == null) {
                    continue;
                }

                mCurrentLoadGroupIndex = instance.getGrpIndex();
                instance.setReqId(mReqId);
                CustomAdsAdapter adapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
                if (adapter == null) {
                    onInsError(instance, ErrorCode.ERROR_CREATE_MEDIATION_ADAPTER);
                    continue;
                }
                instance.setAdapter(adapter);

                groupLoadCount++;
                BaseInstance.MEDIATION_STATE state = instance.getMediationState();
                if (state == BaseInstance.MEDIATION_STATE.NOT_INITIATED) {
                    initInsAndSendEvent(instance);
                    continue;
                }

                try {
                    instance.setMediationState(BaseInstance.MEDIATION_STATE.LOAD_PENDING);
                    loadInsAndSendEvent(instance);
                } catch (Throwable e) {
                    onInsError(instance, e.getMessage());
                    DeveloperLog.LogD("load ins : " + instance.toString() + " error ", e);
                    CrashUtil.getSingleton().saveException(e);
                    AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_INS_LOAD_EXP,
                            AdvanceEventId.MSG_INS_LOAD_EXP + e.getMessage());
                }
            }

            DeveloperLog.LogD("AbstractHybridAds", "startNextInstance, instance index: " + instanceIndex + ", groupLoadCount : " + groupLoadCount);
            //no need to time out if a callback was given
            if (!hasCallbackToUser() && groupLoadCount > 0) {
                //times out with the index of currently loaded instance
                startTimeout(instanceIndex);
            }
        } catch (Throwable e) {
            DeveloperLog.LogD("startNextInstance error", e);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_NEXT_INS_LOAD_EXP,
                    AdvanceEventId.MSG_NEXT_INS_LOAD_EXP + e.getMessage());
        }
    }

    /**
     * All-is-ready reporting
     */
    protected void aReadyReport() {
        if (isDestroyed) {
            return;
        }

        LrReportHelper.report(mReqId, mRuleId, mPlacementId,
                isManualTriggered ? OmManager.LOAD_TYPE.MANUAL.getValue() : OmManager.LOAD_TYPE.INTERVAL.getValue(),
                mPlacement.getWfAbt(),mPlacement.getWfAbtId(),
                CommonConstants.WATERFALL_READY, 0);
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
                if (isInsAvailable(i)) {
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

    private synchronized void placementReadyCallback(BaseInstance ins) {
        if (mTotalIns == null) {
            return;
        }
        if (hasCallbackToUser()) {
            return;
        }
        mCurrentIns = ins;
        cancelTimeout(true, -1);
        aReadyReport();
        notifyUnLoadInsBidLose();
        callbackAdReady();
        finishLoad(null);
    }

    @Override
    protected void notifyUnLoadInsBidLose() {
        if (mTotalIns == null) {
            return;
        }
        int len = mTotalIns.size();
        if (mLoadedInsIndex == len) {
            return;
        }
        for (int i = mLoadedInsIndex; i < len; i++) {
            BaseInstance instance = mTotalIns.get(i);
            BidUtil.notifyLose(instance, BidLoseReason.LOST_TO_HIGHER_BIDDER.getValue());
        }
    }

    private void startTimeout(int insIndex) {
        TimeoutRunnable timeout = new TimeoutRunnable(insIndex);
        mTimeouts.put(mCurrentLoadGroupIndex, timeout);
        mHandler.postDelayed(timeout, mPt * 1000L);
    }

    protected void cancelTimeout(boolean all, int grpIndex) {
        if (mHandler != null) {
            if (all) {
                mHandler.removeCallbacksAndMessages(null);
                mTimeouts.clear();
            } else {
                TimeoutRunnable timeout = mTimeouts.get(grpIndex);
                if (timeout != null) {
                    mHandler.removeCallbacks(timeout);
                    mTimeouts.remove(grpIndex);
                }
            }
        }
    }

    private class TimeoutRunnable implements Runnable {

        private final int insIndex;

        TimeoutRunnable(int insIndex) {
            this.insIndex = insIndex;
        }

        @Override
        public void run() {
            DeveloperLog.LogD("timeout startNextInstance : " + insIndex);
            startNextInstance(insIndex);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_INS_GROUP_LOAD_TIMEOUT,
                    AdvanceEventId.MSG_INS_GROUP_LOAD_TIMEOUT + "startNextInstance : " + insIndex);
        }
    }

}
