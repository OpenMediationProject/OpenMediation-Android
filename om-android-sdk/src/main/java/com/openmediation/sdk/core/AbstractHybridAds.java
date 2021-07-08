package com.openmediation.sdk.core;

import android.os.Looper;
import android.text.TextUtils;

import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.helper.LrReportHelper;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Scene;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractHybridAds extends AbstractAdsApi {
    /**
     * The Is destroyed.
     */
    protected boolean isDestroyed;
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

    /**
     * On ad click callback.
     */
//    protected abstract void onAdClickCallback();

    /**
     * Instance Init
     *
     * @param instance the instance to load ads
     * @param extras   the extras
     */
    protected abstract void insInit(BaseInstance instance, Map<String, Object> extras);

    protected void destroyAdEvent(BaseInstance instances) {
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
        if (mCurrentIns != null && mBidResponses != null) {
            mBidResponses.remove(mCurrentIns.getId());
        }
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
    protected void resetBeforeGetInsOrder() {
        mLoadTs = System.currentTimeMillis();
        mCanCallbackIndex = 0;
        mLoadedInsIndex = 0;
        mBs = mPlacement.getBs();
        mPt = mPlacement.getPt();
        isFo = mPlacement.getFo() == 1;
        if (mBs == 0) {
            mBs = 3;
        }
        if (mBidResponses != null) {
            mBidResponses.clear();
        }
    }

    @Override
    protected void startLoadAds(JSONObject clInfo, List<BaseInstance> instances) {
        List<BaseInstance> wfInstances = InsManager.getListInsResult(mReqId, clInfo, mPlacement, mBs);
        DeveloperLog.LogD("AbstractHybridAds startLoadAd wfInstances : " + wfInstances);
        List<BaseInstance> totalIns = InsManager.sort(wfInstances, instances);
        DeveloperLog.LogD("AbstractHybridAds after instances sort: " + totalIns);
        List<BaseInstance> finalTotalIns = InsManager.splitInsByBs(totalIns, mBs);
        DeveloperLog.LogD("AbstractHybridAds after splitInsByBs ins: " + finalTotalIns);
        if (finalTotalIns == null || finalTotalIns.isEmpty()) {
            DeveloperLog.LogD("Ad", "request cl success, but ins[] is empty" + mPlacement);
            Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                    , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
            callbackLoadError(error);
        } else {
            mTotalIns.clear();
            mTotalIns.addAll(finalTotalIns);
            InsManager.resetInsStateOnClResponse(mTotalIns);
            if (mPlacement != null) {
                Map<Integer, BidResponse> bidResponseMap = WaterFallHelper.getS2sBidResponse(mPlacement, clInfo);
                if (bidResponseMap != null && !bidResponseMap.isEmpty()) {
                    mBidResponses.putAll(bidResponseMap);
                }
            }
            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // resetFields();
                    // mCanCallbackIndex = 0;
                    // mLoadedInsIndex = 0;
                    startNextInstance(0);
                }
            });
        }
    }

    @Override
    protected void loadInsAndSendEvent(BaseInstance instance) {
        super.loadInsAndSendEvent(instance);
//        if (!checkActRef()) {
//            onInsError(instance, ErrorCode.ERROR_ACTIVITY);
//            return;
//        }
        if (TextUtils.isEmpty(instance.getKey())) {
            onInsError(instance, ErrorCode.ERROR_EMPTY_INSTANCE_KEY);
            return;
        }
        CustomAdsAdapter adsAdapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
        if (adsAdapter == null) {
            onInsError(instance, ErrorCode.ERROR_CREATE_MEDATION_ADAPTER);
            return;
        }
        instance.setAdapter(adsAdapter);
        if (!adsAdapter.isAdNetworkInit()) {
            Map<String, Object> initDataMap = InsManager.getInitDataMap(instance);
            insInit(instance, initDataMap);
        } else {
            if (instance.getHb() == 1) {
                InsManager.reportInsLoad(instance, EventId.INSTANCE_PAYLOAD_REQUEST);
            } else {
                InsManager.reportInsLoad(instance, EventId.INSTANCE_LOAD);
                iLoadReport(instance);
            }
            Map<String, Object> placementInfo = PlacementUtils.getLoadExtrasMap(mReqId, instance, mBidResponses.get(instance.getId()));
            insLoad(instance, placementInfo);
        }
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
        onInsLoadFailed(instance, adapterError);
    }

    @Override
    protected synchronized void onInsLoadSuccess(BaseInstance instances) {
        super.onInsLoadSuccess(instances);

        if (!isManualTriggered) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_RELOAD_SUCCESS, InsManager.buildReportData(instances));
        }
        if (isFo || instances.getIndex() <= mCanCallbackIndex) {
            //gives ready callback without waiting for priority checking
            placementReadyCallback(instances);
        } else {
            checkReadyInstance();
        }
    }

//    @Override
//    protected void onInsClicked(BaseInstance instance, Scene scene) {
//        super.onInsClicked(instance, scene);
//        callbackAdClickOnUiThread();
//    }

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

//    @Override
//    protected void onInsClosed(BaseInstance instance, Scene scene) {
//        super.onInsClosed(instance, scene);
//        destroyAdEvent(instance);
//        if (mBidResponses != null) {
//            mBidResponses.remove(instance.getId());
//        }
//    }

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
     * Ad click callback
     */
//    void callbackAdClickOnUiThread() {
//        if (isDestroyed) {
//            return;
//        }
//        onAdClickCallback();
//    }

    /**
     * Ad open callback
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
    protected void onInsLoadFailed(BaseInstance instance, AdapterError error) {
        super.onInsLoadFailed(instance, error);

//        testNotifyInsFailed(instance);
        //MoPubBanner registered a receiver, we need to take care of it
        destroyAdEvent(instance);
        DeveloperLog.LogD("load ins : " + instance.toString() + " error : " + error);

        //allInstanceGroup failed?
        int allFailedCount = InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.LOAD_FAILED);
        boolean allInstanceGroupIsFailed = allFailedCount == mTotalIns.size();

        //gives no_fill  call back if allInstanceGroupIsNull
        if (allInstanceGroupIsFailed && !hasCallbackToUser()) {
            Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, ErrorCode.ERROR_NO_FILL, -1);
            callbackLoadError(errorResult);
            cancelTimeout();
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
            int size = mTotalIns.size();
            if (mBs <= 0 || size <= instanceIndex) {
                Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, ErrorCode.ERROR_NO_FILL, -1);
                callbackLoadError(errorResult);
                return;
            }

            //# of instances to load
            int pendingLoadInstanceCount = mBs;
            int groupLoadCount = 0;
            while (!hasCallbackToUser() && size > instanceIndex && pendingLoadInstanceCount > 0) {
                final BaseInstance i = mTotalIns.get(instanceIndex);
                instanceIndex++;
                pendingLoadInstanceCount--;
                mLoadedInsIndex++;

                if (i == null) {
                    continue;
                }

                BaseInstance.MEDIATION_STATE state = i.getMediationState();
                if (state == BaseInstance.MEDIATION_STATE.INIT_PENDING
                        || state == BaseInstance.MEDIATION_STATE.INIT_FAILED
                        || state == BaseInstance.MEDIATION_STATE.LOAD_FAILED) {
                    continue;
                }

                // destroy loading instance
                if (state == BaseInstance.MEDIATION_STATE.LOAD_PENDING) {
                    destroyAdEvent(i);
                }

                //blocked?
                if (AdRateUtil.shouldBlockInstance(mPlacementId + i.getKey(), i)) {
                    onInsCapped(PlacementUtils.getPlacementType(getPlacementType()), i);
                    continue;
                }

                try {
                    if (mBidResponses != null && mBidResponses.containsKey(i.getId())) {
                        i.setBidResponse(mBidResponses.get(i.getId()));
                    }
                    i.setReqId(mReqId);
                    groupLoadCount++;
                    i.setMediationState(BaseInstance.MEDIATION_STATE.LOAD_PENDING);
                    loadInsAndSendEvent(i);
                } catch (Throwable e) {
                    onInsError(i, e.getMessage());
                    DeveloperLog.LogD("load ins : " + i.toString() + " error ", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }

            DeveloperLog.LogD("AbstractHybridAds", "startNextInstance, groupIndex: " + instanceIndex + ", groupLoadCount : " + groupLoadCount);
            //no need to time out if a callback was given
            if (!hasCallbackToUser() && groupLoadCount > 0) {
                //times out with the index of currently loaded instance
                startTimeout(instanceIndex);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("startNextInstance error", e);
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
                mPlacement.getWfAbt(),
                CommonConstants.WATERFALL_READY, 0);
    }

    /**
     * Instance-level load reporting
     *
     * @param instance the instance
     */
    protected void iLoadReport(BaseInstance instance) {
        if (isDestroyed) {
            return;
        }
        LrReportHelper.report(instance,
                isManualTriggered ? OmManager.LOAD_TYPE.MANUAL.getValue() : OmManager.LOAD_TYPE.INTERVAL.getValue(),
                mPlacement.getWfAbt(),
                CommonConstants.INSTANCE_LOAD, 0);
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
        cancelTimeout();
        aReadyReport();
        notifyUnLoadInsBidLose();
        callbackAdReady();
    }

    @Override
    protected void notifyUnLoadInsBidLose() {
        if (mTotalIns == null || mBidResponses == null) {
            return;
        }
        int len = mTotalIns.size();
        if (mLoadedInsIndex == len) {
            return;
        }
        for (int i = mLoadedInsIndex; i < len; i++) {
            BaseInstance instance = mTotalIns.get(i);
            if (instance == null) {
                continue;
            }

            if (!mBidResponses.containsKey(instance.getId())) {
                continue;
            }
            BidResponse bidResponse = mBidResponses.remove(instance.getId());
            if (bidResponse == null) {
                continue;
            }
            AuctionUtil.notifyLose(instance, bidResponse, BidLoseReason.LOST_TO_HIGHER_BIDDER.getValue());
            instance.setBidResponse(null);
        }
    }

    private void startTimeout(int insIndex) {
        TimeoutRunnable timeout = new TimeoutRunnable(insIndex);
        mHandler.postDelayed(timeout, mPt * 1000L);
    }

    protected void cancelTimeout() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
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
        }
    }

}
