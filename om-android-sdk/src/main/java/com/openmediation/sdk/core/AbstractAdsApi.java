package com.openmediation.sdk.core;

import android.app.Activity;
import android.os.SystemClock;
import android.text.TextUtils;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.bid.BidManager;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.bid.BidResponseCallback;
import com.openmediation.sdk.bid.BidUtil;
import com.openmediation.sdk.inspector.InspectorManager;
import com.openmediation.sdk.inspector.LogConstants;
import com.openmediation.sdk.inspector.logs.InstanceLog;
import com.openmediation.sdk.inspector.logs.InventoryLog;
import com.openmediation.sdk.inspector.logs.WaterfallLog;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
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
import com.openmediation.sdk.utils.event.AdvanceEventId;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.helper.LrReportHelper;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.lifecycle.ActLifecycle;
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
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractAdsApi implements InitCallback, BidResponseCallback, Request.OnRequestCallback {
    protected WeakReference<Activity> mActRefs = new WeakReference<>(null);

    protected String mPlacementId;
    protected Placement mPlacement;
    protected boolean isDestroyed;
    protected ListenerWrapper mListenerWrapper;
    protected OmManager.LOAD_TYPE mLoadType;
    //Adapters to be loaded
    protected CopyOnWriteArrayList<BaseInstance> mTotalIns;
    protected boolean isManualTriggered;
    /**
     * Banner AdSize
     */
    protected AdSize mAdSize;
    /**
     * AuctionId
     */
    protected String mReqId;
    /**
     * RuleId
     */
    protected int mRuleId = -1;

    private JSONObject mWFJsonInfo;

    private long mStartLoadTime;

    /**
     * whether read local wf data
     */
    protected boolean mReadWfFromLocal;

    protected WaterfallLog mWfLog;
    protected Map<BaseInstance, InstanceLog> mInsLogs;
    private boolean isWfFailed = true;

    private final AtomicBoolean wfLoadSuccessReported = new AtomicBoolean(false);

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
    protected void insLoad(BaseInstance instance, Map<String, Object> extras) {
        InstanceLog log = mInsLogs.get(instance);
        if (log == null) {
            log = new InstanceLog(instance);
        }
        if (mWfLog != null) {
            mWfLog.addInsLog(log);
            mWfLog.removeFromUnloadInstance(instance);
        }
        mInsLogs.put(instance, log);
    }

    /**
     * Gets placement info.
     *
     * @return the placement info
     */
    protected abstract PlacementInfo getPlacementInfo();

    protected abstract boolean isReload();

    protected abstract void resetBeforeGetInsOrder();

    protected abstract void notifyUnLoadInsBidLose();

    protected abstract void startLoadAdsImpl(JSONObject clInfo, List<BaseInstance> totalIns);

    /**
     * when all ins load failed and no cache, recoder failed load count, report event
     */
    protected void onAllLoadFailed() {
    }

    public AbstractAdsApi() {
        mTotalIns = new CopyOnWriteArrayList<>();
        mListenerWrapper = new ListenerWrapper();
    }

    protected void onResume(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActRefs = new WeakReference<>(activity);
        }
    }

    protected void onPause(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActRefs = new WeakReference<>(activity);
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
        isManualTriggered = type == OmManager.LOAD_TYPE.MANUAL;
        mWfLog = new WaterfallLog();
        if (mInsLogs == null) {
            mInsLogs = new ConcurrentHashMap<>();
        } else {
            mInsLogs.clear();
        }
        isWfFailed = true;
        wfLoadSuccessReported.set(false);
        if (getPlacementType() == CommonConstants.PROMOTION) {
            startLoadAdsImpl(null, InsManager.getInstanceList(mPlacement));
            return;
        }
        s2sBid();
    }

    /**
     * s2sBid
     */
    protected void s2sBid() {
        BidManager.getInstance().s2sBid(mActRefs.get(), mPlacement.getId(), mReqId, mPlacement.getT(),
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

    /**
     * load block
     *
     * @return boolean
     */
    protected boolean shouldLoadBlock(OmManager.LOAD_TYPE type) {
        return false;
    }

    protected boolean shouldReplenishInventory(OmManager.LOAD_TYPE type) {
        return false;
    }

    protected void callbackLoadError(Error error) {
    }

    protected List<BidResponse> appendLastBidResult() {
        return null;
    }

    protected void finishLoad(Error error) {
        long dur = 0;
        if (mStartLoadTime > 0) {
            dur = SystemClock.elapsedRealtime() - mStartLoadTime;
        }
        mStartLoadTime = 0;
        AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_PLACEMENT_LOAD_DUR,
                AdvanceEventId.MSG_PLACEMENT_LOAD_DUR + dur + " ms.");
        if (mWfLog != null && mPlacement != null && mPlacement.getT() != CommonConstants.PROMOTION) {
            if (isWfFailed) {
                mWfLog.setEventTag(LogConstants.WF_FAILED);
                if (error != null) {
                    mWfLog.setDetail(error.toString());
                }
            } else {
                mWfLog.setEventTag(LogConstants.WF_SUCCESS);
            }
            InspectorManager.getInstance().addWaterfallLog(mPlacementId, mWfLog);
        }
    }

    protected void inventoryAdsReportAReady() {
    }

    protected void startLoadAds(JSONObject clInfo, List<BaseInstance> instances) {
        List<BaseInstance> wfInstances = InsManager.getListInsResult(mReqId, clInfo, mPlacement);
        DeveloperLog.LogD("placement: " + mPlacementId + " startLoadAd wfInstances : " + wfInstances);
        List<BaseInstance> totalIns = InsManager.sort(wfInstances, instances);
        DeveloperLog.LogD("placement: " + mPlacementId + " after instances sort: " + totalIns);
        if (!isInventoryAdsType()) {
            totalIns = InsManager.splitInsByBs(totalIns, mPlacement.getBs());
        }

        if (totalIns == null || totalIns.isEmpty()) {
            Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                    , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD + "Instance is empty", ErrorCode.CODE_INTERNAL_SERVER_ERROR);
            onClRequestFailed(error);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_TOTAL_INS_NULL,
                    AdvanceEventId.MSG_TOTAL_INS_NULL);
            return;
        }

        if (mWfLog != null) {
            mWfLog.setUnloadInstance(totalIns);
        }
        startLoadAdsImpl(clInfo, totalIns);
    }

    /**
     * Instance initialization method
     *
     * @param instance the instance
     */
    protected void initInsAndSendEvent(BaseInstance instance) {
        setActRef();
        InsManager.onInsInitStart(instance);
        onInsInitStart(instance);
    }

    /**
     * For an instance to load ads
     *
     * @param instance the instance
     */
    protected void loadInsAndSendEvent(BaseInstance instance) {
        setActRef();
        if (AdRateUtil.shouldBlockInstance(Preconditions.checkNotNull(mPlacement) ?
                mPlacement.getId() : "" + instance.getKey(), instance)) {
            onInsCapped(PlacementUtils.getPlacementType(getPlacementType()), instance, isReload());
            return;
        }
        if (instance.getHb() == 1) {
            CustomAdsAdapter adapter = instance.getAdapter();
            if (adapter.needPayload()) {//standard c2s/s2s
                InsManager.reportInsEvent(instance, EventId.INSTANCE_PAYLOAD_REQUEST);
            } else {
                // non-standard C2S bid instance in wf queue
                if (instance.getBidResponse() == null) {
                    // 上报bidRequest
                    onInsC2SBidStart(instance);
                }
            }
        } else {
            InsManager.reportInsEvent(instance, EventId.INSTANCE_LOAD);
            LrReportHelper.report(instance, isInventoryAdsType() ? mLoadType.getValue()
                            : (isReload() ? OmManager.LOAD_TYPE.INTERVAL.getValue()
                            : OmManager.LOAD_TYPE.MANUAL.getValue()), mPlacement.getWfAbt(), mPlacement.getWfAbtId()
                    , CommonConstants.INSTANCE_LOAD, 0);
        }
        InsManager.onInsLoadStart(instance);
        insLoad(instance, PlacementUtils.getLoadExtrasMap(mReqId, mPlacementId, instance));
    }

    protected void notifyInsBidWin(BaseInstance instance) {
        BidUtil.notifyWin(instance);
    }

    protected void notifyLoadFailedInsBidLose(BaseInstance instance) {
        BidUtil.notifyLose(instance, BidLoseReason.INTERNAL.getValue());
    }

    protected synchronized void onInsC2SBidStart(BaseInstance instance) {
        InsManager.onInsBidStart(instance);
    }

    protected synchronized void onInsC2SBidSuccess(BaseInstance instance, BidResponse response) {
        // 价格重排序，但hybrid不重排，加埋点上报
        InsManager.onInsBidSuccess(instance, response);
        DeveloperLog.LogD(instance + " C2S Bid Success: " + response);
    }

    protected synchronized void onInsC2SBidFailed(BaseInstance instance, String error) {
        InsManager.onInsBidFailed(instance, error);
        DeveloperLog.LogD(instance + " C2S Bid Failed: " + error);
    }

    protected synchronized void onInsInitStart(BaseInstance instance) {
        InstanceLog log = new InstanceLog(instance);
        if (mWfLog != null) {
            mWfLog.addInsLog(log);
            mWfLog.removeFromUnloadInstance(instance);
        }
        mInsLogs.put(instance, log);
    }

    protected synchronized void onInsInitFailed(BaseInstance instance, Error error) {
        InstanceLog log = mInsLogs.get(instance);
        if (log != null) {
            if (instance.getHb() == 1) {
                log.setEventTag(LogConstants.INS_PAYLOAD_FAILED);
            } else {
                log.setEventTag(LogConstants.INS_LOAD_FAILED);
            }
            log.setDetail(error.toString());
        }
    }

    protected synchronized void onInsLoadSuccess(BaseInstance instance, boolean reload) {
        isWfFailed = false;
        if (instance.getHb() != 1) {
            LrReportHelper.report(instance, mLoadType.getValue(), mPlacement.getWfAbt(), mPlacement.getWfAbtId(),
                    CommonConstants.INSTANCE_READY, 0);
        }
        InsManager.onInsLoadSuccess(instance, reload);

        // repost waterfall ins load success
        if (!wfLoadSuccessReported.get()) {
            wfLoadSuccessReported.set(true);
            LrReportHelper.report(instance.getReqId(), mRuleId, mPlacement.getId(),
                    mLoadType.getValue(), mPlacement.getWfAbt(), mPlacement.getWfAbtId(),
                    CommonConstants.WATERFALL_LOAD_SUCCESS, 0);
        }

        InstanceLog log = mInsLogs.get(instance);
        if (log != null) {
            if (instance.getHb() == 1) {
                log.setEventTag(LogConstants.INS_PAYLOAD_SUCCESS);
            } else {
                log.setEventTag(LogConstants.INS_FILL);
            }
            log.setRevenue(instance.getRevenue());
        }
        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setInstance(instance);
        inventoryLog.setEventTag(LogConstants.INVENTORY_IN);
        InspectorManager.getInstance().addInventoryLog(isInventoryAdsType(), mPlacementId, inventoryLog);
    }

    protected synchronized void onInsLoadFailed(BaseInstance instance, AdapterError error, boolean reload) {
        notifyLoadFailedInsBidLose(instance);
        InsManager.onInsLoadFailed(instance, error, reload);
        InstanceLog log = mInsLogs.get(instance);
        if (log != null) {
            if (instance.getHb() == 1) {
                log.setEventTag(LogConstants.INS_PAYLOAD_FAILED);
            } else {
                log.setEventTag(LogConstants.INS_LOAD_FAILED);
            }
            log.setDetail(error.toString());
        }
    }

    protected void onInsShowSuccess(BaseInstance instance, Scene scene) {
        int bid = 0;
        if (instance.getHb() == 1) {
            bid = 1;
        }
        LrReportHelper.report(instance, scene != null ? scene.getId() : -1, mLoadType.getValue(),
                mPlacement == null ? -1 : mPlacement.getWfAbt(),
                mPlacement == null ? -1 : mPlacement.getWfAbtId(),
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
                mPlacement == null ? -1 : mPlacement.getWfAbtId(),
                CommonConstants.INSTANCE_CLICK, bid);
    }

    protected void onInsClosed(BaseInstance instance, Scene scene) {
        InsManager.onInsClosed(isInventoryAdsType(), instance, scene);
    }

    protected void onInsCapped(String adType, BaseInstance instance, boolean reload) {
        instance.setMediationState(BaseInstance.MEDIATION_STATE.CAPPED);
        AdapterError adapterError = AdapterErrorBuilder.buildLoadCheckError(
                adType, instance.getAdapter().getClass().getSimpleName(), ErrorCode.ERROR_LOAD_CAPPED + "Ins Capped, " + instance);
        onInsLoadFailed(instance, adapterError, reload);
    }

    protected boolean hasAvailableInventory() {
        return InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE) > 0;
    }

    public void loadAds(OmManager.LOAD_TYPE type) {
        mStartLoadTime = SystemClock.elapsedRealtime();
        DeveloperLog.LogD("loadAds : " + mPlacement + " action: " + (type != null ? type.toString() : "null"));
        if (InitImp.isInitRunning()) {
            OmManager.getInstance().pendingInit(this);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_LOAD_WHILE_INIT_PENDING,
                    AdvanceEventId.MSG_LOAD_WHILE_INIT_PENDING);
            return;
        }

        //checks if initialization was successful
        if (!InitImp.isInit()) {
            InitImp.reInitSDK(this);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_LOAD_WHILE_NOT_INIT,
                    AdvanceEventId.MSG_LOAD_WHILE_NOT_INIT);
            return;
        }

        if (shouldLoadBlock(type)) {
            return;
        }

        if (isInventoryAdsType()) {
            if (!shouldReplenishInventory(type)) {
                return;
            }
        }

        Error error = checkLoadAvailable(type);
        if (error != null) {
            callbackLoadError(error);
            AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
            return;
        }
        startGetInsOrder(type);
    }

    @Override
    public void onSuccess() {
        loadAds(OmManager.LOAD_TYPE.MANUAL);
    }

    @Override
    public void onError(Error result) {
        callbackLoadError(result);
        finishLoad(result);
        AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_RE_INIT_ERROR,
                AdvanceEventId.MSG_RE_INIT_ERROR + result);
    }

    @Override
    public void onBidS2SComplete(List<BidResponse> responses) {
        WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, appendLastBidResult(), responses,
                InsManager.getInstanceLoadStatuses(mTotalIns), mReqId, this);
        AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_S2S_BID_COMPLETED,
                AdvanceEventId.MSG_S2S_BID_COMPLETED);
    }

    @Override
    public void onBidC2SComplete(List<BaseInstance> c2sInstances) {
        try {
            DeveloperLog.LogD("onBidC2SComplete c2sInstances : " + c2sInstances);
            startLoadAds(mWFJsonInfo, c2sInstances);
        } catch (Exception e) {
            DeveloperLog.LogE("onBidC2SComplete, Placement:" + mPlacement, e);
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                    , ErrorCode.MSG_LOAD_SERVER_ERROR + " Load ad failed: " + e.getMessage(), ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            CrashUtil.getSingleton().saveException(e);
            callbackLoadError(error);
            finishLoad(error);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_START_LOAD_ERROR,
                    AdvanceEventId.MSG_START_LOAD_ERROR + e.getMessage());
        }
    }

    @Override
    public void onRequestSuccess(Response response) {
        try {
            if (!Preconditions.checkNotNull(response) || response.code() != HttpURLConnection.HTTP_OK) {
                String msg = response == null ? "response is null" : "Http code " + response.code();
                Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                        , ErrorCode.MSG_LOAD_SERVER_ERROR + msg, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                DeveloperLog.LogE(error.toString() + ", request cl http code:"
                        + (response != null ? response.code() : "null") + ", placement:" + mPlacement);
                callbackLoadError(error);
                finishLoad(error);
                AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_WF_RESPONSE_ERROR,
                        AdvanceEventId.MSG_WF_RESPONSE_ERROR + msg);
                return;
            }
            String responseString = response.body().string();
            // save wf data
            OmCacheManager.getInstance().saveWaterfallData(mPlacement.getId(), mPlacement.getT(), responseString);
            if (mReadWfFromLocal) {
                return;
            }
            onInternalRequestSuccess(responseString);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_WF_RESPONSE_DATA,
                    AdvanceEventId.MSG_WF_RESPONSE_DATA, responseString);
        } catch (Exception e) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                    , ErrorCode.MSG_LOAD_SERVER_ERROR + "failed when parse wf response: " + e.getMessage(), ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            DeveloperLog.LogE(error.toString() + ", request cl success, but failed when parse response" +
                    ", Placement:" + mPlacement, e);
            CrashUtil.getSingleton().saveException(e);
            callbackLoadError(error);
            finishLoad(error);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_WF_RESPONSE_EXCEPTION,
                    AdvanceEventId.MSG_WF_RESPONSE_EXCEPTION, e.getMessage());
        } finally {
            IOUtil.closeQuietly(response);
        }
    }

    /**
     * WF RequestSuccess
     *
     * @param response String
     */
    protected final void onInternalRequestSuccess(String response) throws Exception {
        JSONObject wfInfo = new JSONObject(response);
        int code = wfInfo.optInt("code");
        if (code != 0) {
            Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD,
                    wfInfo.optString("msg"), ErrorCode.CODE_INTERNAL_SERVER_ERROR);
            onClRequestFailed(error);
            AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_WF_CODE_ERROR,
                    AdvanceEventId.MSG_WF_CODE_ERROR + ", code is " + code);
            return;
        }
        MediationRule mediationRule = WaterFallHelper.getMediationRule(wfInfo);
        if (mediationRule != null) {
            mRuleId = mediationRule.getId();
            if (mWfLog != null) {
                mWfLog.setMediationRuleId(mRuleId);
                mWfLog.setMediationRuleName(mediationRule.getName());
            }
        }
        InspectorManager.getInstance().notifyWaterfallChanged(mPlacementId, mWfLog);

        mPlacement.setWfAbt(wfInfo.optInt("abt"));
        mPlacement.setWfAbtId(wfInfo.optInt("abtId"));
        // report Waterfall Ready
        if (!mReadWfFromLocal && isInventoryAdsType()) {
            inventoryAdsReportAReady();
        }
        mWFJsonInfo = wfInfo;
        List<BaseInstance> c2SInstances = InsManager.getC2SInstances(mReqId, wfInfo, mPlacement);
        if (c2SInstances == null || c2SInstances.isEmpty()) {
            startLoadAds(wfInfo, null);
        } else {
            BidManager.getInstance().c2sBid(mActRefs.get(), c2SInstances, mPlacement.getId(), mReqId, mPlacement.getT(),
                    mAdSize, AbstractAdsApi.this);
        }
    }

    @Override
    public void onRequestFailed(String error) {
        Error errorResult = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                , ErrorCode.MSG_LOAD_SERVER_ERROR + "WF request failed: " + error, ErrorCode.CODE_INTERNAL_SERVER_FAILED);
        onClRequestFailed(errorResult);
        AdsUtil.advanceEventReport(mPlacementId, AdvanceEventId.CODE_WF_RESPONSE_FAILED,
                AdvanceEventId.MSG_WF_RESPONSE_FAILED, error);
    }

    private void onClRequestFailed(Error error) {
        DeveloperLog.LogE(error.toString() + ", request cl failed : " + error + ", error" + error);
        if (isInventoryAdsType()) {
            if (!hasAvailableInventory()) {
                onAllLoadFailed();
                callbackLoadError(error);
            }
        } else {
            callbackLoadError(error);
        }
        finishLoad(error);
    }

    private Error checkLoadAvailable(OmManager.LOAD_TYPE type) {
        //empty placementId?
        if (TextUtils.isEmpty(mPlacementId)) {
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_PLACEMENT_ID, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
        }

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
                    , ErrorCode.ERROR_LOAD_CAPPED, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }
        if (type != OmManager.LOAD_TYPE.MANUAL && !OmManager.getInstance().getAutoCache()) {
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , "Ad load limit, user \"OmAds.setAutoCache(true)\" to unlock the limit", ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }
        return null;
    }
}
