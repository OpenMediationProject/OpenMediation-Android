package com.openmediation.sdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.bid.AuctionCallback;
import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.bid.BidAuctionManager;
import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.utils.lifecycle.ActLifecycle;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.Preconditions;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.device.DeviceUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorBuilder;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.helper.LrReportHelper;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.MediationRule;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.utils.request.network.Request;
import com.openmediation.sdk.utils.request.network.Response;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractAdsApi implements InitCallback, AuctionCallback, Request.OnRequestCallback {
    protected WeakReference<Activity> mActRefs = new WeakReference<>(null);

    protected String mPlacementId;
    protected Placement mPlacement;
    protected boolean isDestroyed;
    protected ListenerWrapper mListenerWrapper;
    protected OmManager.LOAD_TYPE mLoadType;
    //Adapters to be loaded
    protected CopyOnWriteArrayList<BaseInstance> mTotalIns;
    protected Map<Integer, BidResponse> mBidResponses;
    protected boolean isManualTriggered;
    /**
     * AuctionId
     */
    protected String mReqId;
    /**
     * RuleId
     */
    protected int mRuleId = -1;

    private JSONObject mWFJsonInfo;

    public abstract boolean isInventoryAdsType();

    /**
     * Checks if an instance is available
     *
     * @param instance the instance to be checked
     * @return whether the instance's adapter is available
     */
    protected abstract boolean isInsAvailable(BaseInstance instance);

    /**
     * Instance loads ads
     *
     * @param instance the instance to load ads
     * @param extras   the extras
     */
    protected abstract void insLoad(BaseInstance instance, Map<String, Object> extras);

    /**
     * Gets placement info.
     *
     * @return the placement info
     */
    protected abstract PlacementInfo getPlacementInfo();

    protected abstract void resetBeforeGetInsOrder();

    protected abstract void startLoadAds(JSONObject clInfo, List<BaseInstance> instances);

    protected abstract void notifyUnLoadInsBidLose();

    protected void startLoadAdsImpl(JSONObject clInfo, List<BaseInstance> totalIns) {
    }

    public AbstractAdsApi() {
        mTotalIns = new CopyOnWriteArrayList<>();
        mBidResponses = new ConcurrentHashMap<>();
        mListenerWrapper = new ListenerWrapper();
    }

    protected void onResume(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActRefs = new WeakReference<>(activity);
//            if (mTotalIns != null && !mTotalIns.isEmpty()) {
//                for (BaseInstance in : mTotalIns) {
//                    InsManager.onResume(in, activity);
//                }
//            }
        }
    }

    protected void onPause(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActRefs = new WeakReference<>(activity);
//            if (mTotalIns != null && !mTotalIns.isEmpty()) {
//                for (BaseInstance in : mTotalIns) {
//                    InsManager.onPause(in, activity);
//                }
//            }
        }
    }

    protected int getPlacementType() {
        return mPlacement != null ? mPlacement.getT() : -1;
    }

    /**
     * @return activity's availability
     */
    protected boolean checkActRef() {
        if (!DeviceUtil.isActivityAvailable(mActRefs.get())) {
            Activity activity = ActLifecycle.getInstance().getActivity();
            if (activity == null) {
                return false;
            }
            mActRefs = new WeakReference<>(activity);
        }
        return true;
    }

    protected void setActRef() {
        if (mActRefs == null || !DeviceUtil.isActivityAvailable(mActRefs.get())) {
            Activity activity = ActLifecycle.getInstance().getActivity();
            mActRefs = new WeakReference<>(activity);
        }
    }

    protected void startGetInsOrder(OmManager.LOAD_TYPE type) {
        resetBeforeGetInsOrder();
        mReqId = DeviceUtil.createReqId();
        mLoadType = type;
        if (getPlacementType() == CommonConstants.PROMOTION) {
            startLoadAdsImpl(null, InsManager.getInstanceList(mPlacement));
            return;
        }
        BidAuctionManager.getInstance().s2sBid(mActRefs.get(), mPlacement.getId(), mReqId, mPlacement.getT(),
                this);
    }

    /**
     * Sets current placement.
     *
     * @param placement the placement
     */
    protected void setCurrentPlacement(Placement placement) {
        if (Preconditions.checkNotNull(placement)) {
            mPlacement = placement;
            mPlacementId = placement.getId();
            mListenerWrapper.setPlacementId(mPlacementId);
        }
    }

    protected boolean shouldReplenishInventory(OmManager.LOAD_TYPE type) {
        return false;
    }

    protected void callbackLoadError(Error error) {
    }

    protected List<BidResponse> appendLastBidResult() {
        return null;
    }

    protected void inventoryAdsReportAReady() {
    }

    /**
     * Instance initialization method
     *
     * @param instance the instance
     */
    protected void initInsAndSendEvent(BaseInstance instance) {
        setActRef();
    }

    /**
     * For an instance to load ads
     *
     * @param instance the instance
     */
    protected void loadInsAndSendEvent(BaseInstance instance) {
        setActRef();
    }

    protected void notifyInsBidWin(BaseInstance instance) {
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
        AuctionUtil.notifyWin(instance, bidResponse);
    }

    protected void notifyLoadFailedInsBidLose(BaseInstance instance) {
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

    protected synchronized void onInsLoadSuccess(BaseInstance instance) {
        if (instance.getHb() != 1) {
            LrReportHelper.report(instance, mLoadType.getValue(), mPlacement.getWfAbt(), CommonConstants.INSTANCE_READY, 0);
        }
        InsManager.onInsLoadSuccess(instance);
    }

    protected synchronized void onInsLoadFailed(BaseInstance instance, AdapterError error) {
        notifyLoadFailedInsBidLose(instance);
        InsManager.onInsLoadFailed(instance, error, !isManualTriggered);
    }

    protected void onInsShowSuccess(BaseInstance instance, Scene scene) {
        int bid = 0;
        if (instance.getHb() == 1) {
            bid = 1;
        }
        LrReportHelper.report(instance, scene != null ? scene.getId() : -1, mLoadType.getValue(),
                mPlacement == null ? -1 : mPlacement.getWfAbt(),
                CommonConstants.INSTANCE_IMPR, bid);
        InsManager.onInsShowSuccess(instance, scene);
    }

    protected void onInsShowFailed(BaseInstance instance, AdapterError error, Scene scene) {
        InsManager.onInsShowFailed(instance, error, scene);
    }

    protected void onInsClicked(BaseInstance instance, Scene scene) {
        InsManager.onInsClick(instance, scene);
        int bid = 0;
        if (instance.getHb() == 1) {
            bid = 1;
        }
        LrReportHelper.report(instance, scene != null ? scene.getId() : -1, mLoadType.getValue(),
                mPlacement == null ? -1 : mPlacement.getWfAbt(),
                CommonConstants.INSTANCE_CLICK, bid);
    }

    protected void onInsClosed(BaseInstance instance, Scene scene) {
        InsManager.onInsClosed(instance, scene);
    }

    protected void onInsCapped(String adType, BaseInstance instance) {
        AdapterError adapterError = AdapterErrorBuilder.buildLoadCheckError(
                adType, instance.getAdapter().getClass().getSimpleName(), ErrorCode.MSG_LOAD_CAPPED);
        onInsLoadFailed(instance, adapterError);
    }

    public void loadAds(OmManager.LOAD_TYPE type) {
        if (InitImp.isInitRunning()) {
            OmManager.getInstance().pendingInit(this);
            return;
        }

        //checks if initialization was successful
        if (!InitImp.isInit()) {
            InitImp.reInitSDK(this);
            return;
        }

        isManualTriggered = type == OmManager.LOAD_TYPE.MANUAL;

        if (isInventoryAdsType()) {
            if (!shouldReplenishInventory(type)) {
                return;
            }
        }

        Error error = checkLoadAvailable();
        if (error != null) {
            callbackLoadError(error);
            AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
            return;
        }

        startGetInsOrder(type);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(Error result) {

    }

    @Override
    public void onBidS2SComplete(List<BidResponse> responses) {
        WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, appendLastBidResult(), responses,
                InsManager.getInstanceLoadStatuses(mTotalIns), mReqId, this);
    }

    @Override
    public void onBidC2SComplete(List<BaseInstance> c2sInstances, List<BidResponse> responses) {
        try {
            DeveloperLog.LogD("onBidC2SComplete c2sInstances : " + c2sInstances);
            if (responses != null && !responses.isEmpty()) {
                storeC2sResult(responses);
            }
            startLoadAds(mWFJsonInfo, c2sInstances);
        } catch (Exception e) {
            DeveloperLog.LogE("onBidC2SComplete, Placement:" + mPlacement, e);
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                    , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            CrashUtil.getSingleton().saveException(e);
            callbackLoadError(error);
        }
    }


    @Override
    public void onRequestSuccess(Response response) {
        try {
            if (!Preconditions.checkNotNull(response) || response.code() != HttpURLConnection.HTTP_OK) {
                Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                        , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                DeveloperLog.LogE(error.toString() + ", request cl http code:"
                        + (response != null ? response.code() : "null") + ", placement:" + mPlacement);
                callbackLoadError(error);
                return;
            }
            JSONObject clInfo = new JSONObject(response.body().string());

            if (isInventoryAdsType()) {
                inventoryAdsReportAReady();
            }

            int code = clInfo.optInt("code");
            if (code != 0) {
                List<BaseInstance> lastAvailableIns = InsManager.getInsWithStatus(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);
                if (lastAvailableIns == null || lastAvailableIns.isEmpty()) {
                    String msg = clInfo.optString("msg");
                    Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                            , msg, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString());
                    callbackLoadError(error);
                }
                return;
            }

            MediationRule mediationRule = WaterFallHelper.getMediationRule(clInfo);
            if (mediationRule != null) {
                mRuleId = mediationRule.getId();
            }

            mPlacement.setWfAbt(clInfo.optInt("abt"));

            mWFJsonInfo = clInfo;
            List<BaseInstance> c2SInstances = InsManager.getC2SInstances(mReqId, clInfo, mPlacement);
            if (c2SInstances == null || c2SInstances.isEmpty()) {
                startLoadAds(clInfo, null);
            } else {
                BidAuctionManager.getInstance().c2sBid(mActRefs.get(), c2SInstances, mPlacement.getId(), mReqId, mPlacement.getT(),
                        AbstractAdsApi.this);
            }
        } catch (Exception e) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                    , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            DeveloperLog.LogE(error.toString() + ", request cl success, but failed when parse response" +
                    ", Placement:" + mPlacement, e);
            CrashUtil.getSingleton().saveException(e);
            callbackLoadError(error);
        } finally {
            IOUtil.closeQuietly(response);
        }
    }

    @Override
    public void onRequestFailed(String error) {
        Error errorResult = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_FAILED);
        DeveloperLog.LogE(errorResult.toString() + ", request cl failed : " + errorResult + ", error" + error);
        callbackLoadError(errorResult);
    }

    private Error checkLoadAvailable() {
        //empty placementId?
        if (TextUtils.isEmpty(mPlacementId)) {
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_PLACEMENT_ID, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
        }
        //activity effective?
//        if (!checkActRef()) {
//            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
//                    , ErrorCode.ERROR_ACTIVITY, ErrorCode.CODE_INTERNAL_REQUEST_ACTIVITY);
//        }

//        //network available?
//        if (!NetworkChecker.isAvailable(mActRef.get())) {
//            callbackAdErrorOnUiThread(ErrorCode.ERROR_NETWORK_NOT_AVAILABLE);
//            return ErrorBuilder.build(ErrorCode.CODE_LOAD_NETWORK_ERROR
//                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
//        }

        if (isDestroyed) {
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_LOAD_AD_BUT_DESTROYED, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }

        mPlacement = PlacementUtils.getPlacement(mPlacementId);
        if (mPlacement == null) {
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_PLACEMENT_EMPTY, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }
        if (mPlacement.getT() != getPlacementType()) {
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_PLACEMENT_TYPE, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }

        if (AdRateUtil.shouldBlockPlacement(mPlacement) || AdRateUtil.isPlacementCapped(mPlacement)) {
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_NO_FILL, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }
        return null;
    }

    private void storeC2sResult(List<BidResponse> c2sResult) {
        for (BidResponse bidResponse : c2sResult) {
            if (bidResponse == null) {
                continue;
            }
            mBidResponses.put(bidResponse.getIid(), bidResponse);
        }
    }
}
