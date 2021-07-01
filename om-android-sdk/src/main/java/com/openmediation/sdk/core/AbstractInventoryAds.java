package com.openmediation.sdk.core;

import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.bid.BidLoseReason;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.runnable.AdsScheduleTask;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.Preconditions;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorBuilder;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.helper.LrReportHelper;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.utils.model.Scene;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractInventoryAds extends AbstractAdsApi {
    //
    protected Scene mScene;
    private int mInventorySize;
    //
    protected boolean isInLoadingProgress;
    protected boolean isInShowingProgress;

    private final AtomicBoolean mLastAvailability = new AtomicBoolean(false);
    private final AtomicBoolean mDidScheduleTaskStarted = new AtomicBoolean(false);
    private final AtomicBoolean isAReadyReported = new AtomicBoolean(false);
    private final AtomicInteger mAllLoadFailedCount = new AtomicInteger(0);

    protected abstract void onAvailabilityChanged(boolean available, Error error);

    /**
     * Instance shows ads
     *
     * @param instance the instance to show ads
     */
    protected abstract void insShow(BaseInstance instance);

    public AbstractInventoryAds() {
        super();
    }

    @Override
    public boolean isInventoryAdsType() {
        return true;
    }

    /**
     * Ends this loading
     */
    @Override
    protected void callbackLoadError(Error error) {
        isInLoadingProgress = false;
//        isManualTriggered = false;
        AdLog.getSingleton().LogE("Ad load failed placementId: " + mPlacementId + ", " + error);
    }

    protected void callbackAvailableOnManual() {
//        isManualTriggered = false;
    }

    /**
     * Callback load success on manual.
     */
    protected void callbackLoadSuccessOnManual() {
    }

    /**
     * Callback load failed on manual.
     *
     * @param error the error
     */
    protected void callbackLoadFailedOnManual(Error error) {
    }

    protected void callbackShowError(Error error) {
        isInShowingProgress = false;
        AdLog.getSingleton().LogE("Ad show failed placementId: " + mPlacementId + ", " + error);
    }

    /**
     * Callback ad closed.
     */
    protected void callbackAdClosed() {
    }

    @Override
    protected boolean shouldReplenishInventory(OmManager.LOAD_TYPE type) {
        int availableCount = InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);

        //if load is manually triggered
        if (type == OmManager.LOAD_TYPE.MANUAL) {
            //only checks ScheduleTask when manually triggered
            checkScheduleTaskStarted();

            if (availableCount > 0 && shouldNotifyAvailableChanged(true)) {
                callbackAvailableOnManual();
            }
        } else {
            String pid = mPlacement != null ? mPlacement.getId() : "";
            reportEvent(EventId.ATTEMPT_TO_BRING_NEW_FEED, PlacementUtils.placementEventParams(pid));
            if (availableCount > 0) {
                reportEvent(EventId.AVAILABLE_FROM_CACHE, PlacementUtils.placementEventParams(pid));
            }
        }

        if (availableCount >= mInventorySize) {
            DeveloperLog.LogD("Inventory is full, cancel this request");
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , "Inventory is full, cancel this request", ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
            return false;
        }
        return true;
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return null;
    }

    @Override
    protected void resetBeforeGetInsOrder() {
        isAReadyReported.set(false);
        isInLoadingProgress = true;
        removeBidResponseWhenLoad();
    }

    @Override
    protected void setCurrentPlacement(Placement placement) {
        super.setCurrentPlacement(placement);
        mInventorySize = placement.getCs();
    }

    @Override
    protected void inventoryAdsReportAReady() {
        //when not trigger by init, checks cache before aReady reporting
        if (mLoadType != OmManager.LOAD_TYPE.INIT) {
            int availableCount = InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);
            if (availableCount > 0) {
                isAReadyReported.set(true);
                LrReportHelper.report(mReqId, mRuleId, mPlacement.getId(), mLoadType.getValue(), mPlacement.getWfAbt(),
                        CommonConstants.WATERFALL_READY, 0);
            }
        }
    }

    @Override
    protected void startLoadAds(JSONObject clInfo, List<BaseInstance> instances) {
        List<BaseInstance> wfInstances = InsManager.getListInsResult(mReqId, clInfo, mPlacement);
        DeveloperLog.LogD("AbstractInventoryAds startLoadAd wfInstances : " + wfInstances);
        List<BaseInstance> totalIns = InsManager.sort(wfInstances, instances);
        DeveloperLog.LogD("AbstractInventoryAds after instances sort: " + totalIns);
        startLoadAdsImpl(clInfo, totalIns);
    }

    @Override
    protected void startLoadAdsImpl(JSONObject clInfo, List<BaseInstance> totalIns) {
        List<BaseInstance> lastAvailableIns = InsManager.getInsWithStatus(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);
        if (totalIns == null || totalIns.isEmpty()) {
            if (lastAvailableIns == null || lastAvailableIns.isEmpty()) {
                Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                        , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                DeveloperLog.LogE(error.toString() + ", tmp:" + totalIns + ", last:" + lastAvailableIns);
                callbackLoadError(error);
            } else {
                DeveloperLog.LogD("request cl success, but ins[] is empty, but has history");
                isInLoadingProgress = false;
            }
            return;
        }
        if (lastAvailableIns != null && !lastAvailableIns.isEmpty()) {
            InsManager.reOrderIns(lastAvailableIns, totalIns);
        }
        mTotalIns.clear();
        mTotalIns.addAll(totalIns);
        InsManager.resetInsStateOnClResponse(mTotalIns);
        DeveloperLog.LogD("TotalIns is : " + mTotalIns.toString());
        int availableCount = InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);
        reSizeInventorySize();
        DeveloperLog.LogD("after cl, Inventory size is : " + mInventorySize);
        //if availableCount == mCacheSize, do not load any new instance
        if (availableCount == mInventorySize) {
            DeveloperLog.LogD("no new ins should be loaded, current load progress finishes");
            isInLoadingProgress = false;
        } else {
            if (mPlacement != null) {
                Map<Integer, BidResponse> bidResponseMap = WaterFallHelper.getS2sBidResponse(clInfo);
                if (bidResponseMap != null && !bidResponseMap.isEmpty()) {
                    mBidResponses.putAll(bidResponseMap);
                }
            }
            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initOrFetchNextAdapter();
                }
            });
        }
    }

    @Override
    protected List<BidResponse> appendLastBidResult() {
        List<BidResponse> responses = null;
        if (hasAvailableInventory()) {
            responses = new ArrayList<>();
            List<Integer> ids = InsManager.getInsIdWithStatus(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);
            for (Integer id : ids) {
                BidResponse bidResponse = mBidResponses.get(id);
                if (bidResponse != null) {
                    responses.add(bidResponse);
                }
            }
        }
        return responses;
    }

    protected void showAd(String scene) {
        if (!shouldShowAd(scene)) {
            return;
        }
        for (BaseInstance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                resetMediationStateAndNotifyLose(in);
                continue;
            }
            setActRef();
            AdLog.getSingleton().LogD("Ad show placementId: " + mPlacementId);
            notifyInsBidWin(in);
            DataCache.getInstance().setMEM(in.getKey() + KeyConstants.KEY_DISPLAY_SCENE, mScene.getN());
            DataCache.getInstance().setMEM(in.getKey() + KeyConstants.KEY_DISPLAY_ABT, mPlacement.getWfAbt());
            //if availability changed from false to true
            if (shouldNotifyAvailableChanged(false)) {
                onAvailabilityChanged(false, null);
            }
            insShow(in);
            return;
        }
        Error error = ErrorBuilder.build(ErrorCode.CODE_SHOW_NO_AD_READY
                , ErrorCode.MSG_SHOW_NO_AD_READY, -1);
        DeveloperLog.LogE(error.toString());
        callbackShowError(error);
    }

    protected boolean isPlacementAvailable() {
        if (isInShowingProgress || !Preconditions.checkNotNull(mPlacement) || mTotalIns == null || mTotalIns.isEmpty()) {
            return false;
        }
        for (BaseInstance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                resetMediationStateAndNotifyLose(in);
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void loadInsAndSendEvent(BaseInstance instance) {
        super.loadInsAndSendEvent(instance);
        if (AdRateUtil.shouldBlockInstance(Preconditions.checkNotNull(mPlacement) ?
                mPlacement.getId() : "" + instance.getKey(), instance)) {
            instance.setMediationState(BaseInstance.MEDIATION_STATE.CAPPED);
            onInsCapped(PlacementUtils.getPlacementType(getPlacementType()), instance);
            return;
        }

        if (instance.getHb() == 1) {
            InsManager.reportInsLoad(instance, EventId.INSTANCE_PAYLOAD_REQUEST);
            BidResponse bidResponse = mBidResponses.get(instance.getId());
            instance.setBidResponse(bidResponse);
            insLoad(instance, PlacementUtils.getLoadExtrasMap(mReqId, instance, bidResponse));
        } else {
            InsManager.reportInsLoad(instance, EventId.INSTANCE_LOAD);
            LrReportHelper.report(instance, mLoadType.getValue(), mPlacement.getWfAbt(), CommonConstants.INSTANCE_LOAD, 0);
            insLoad(instance, PlacementUtils.getLoadExtrasMap(mReqId, instance, null));
        }
    }

    /**
     * On ins init failed.
     *
     * @param instance the instance
     * @param error    the error
     */
    protected void onInsInitFailed(BaseInstance instance, Error error) {
        notifyLoadFailedInsBidLose(instance);
        if (shouldFinishLoad()) {
            boolean hasInventory = hasAvailableInventory();
            if (isManualTriggered && !hasInventory) {
                callbackLoadFailedOnManual(error);
            }
            if (!hasInventory) {
                whenAllLoadFailed();
            }
            if (shouldNotifyAvailableChanged(hasInventory)) {
                onAvailabilityChanged(hasInventory, error);
            }
            notifyUnLoadInsBidLose();
        } else {
            initOrFetchNextAdapter();
        }
    }

    /**
     * On ins ready.
     *
     * @param instance the instance
     */
    @Override
    protected synchronized void onInsLoadSuccess(BaseInstance instance) {
        super.onInsLoadSuccess(instance);
        mAllLoadFailedCount.set(0);
        if (!shouldFinishLoad()) {
            initOrFetchNextAdapter();
        } else {
            notifyUnLoadInsBidLose();
        }
        if (isManualTriggered) {
            callbackLoadSuccessOnManual();
        }
        if (shouldNotifyAvailableChanged(true)) {
            if (!isAReadyReported.get()) {
                isAReadyReported.set(true);
                LrReportHelper.report(mReqId, mRuleId, instance.getPlacementId(), mLoadType.getValue(), mPlacement.getWfAbt(),
                        CommonConstants.WATERFALL_READY, 0);
            }
            onAvailabilityChanged(true, null);
        }
        AdLog.getSingleton().LogD("Ad load success placementId: " + mPlacementId);
    }

    /**
     * On ins load failed.
     *
     * @param instance the instance
     * @param error    the error
     */
    @Override
    protected synchronized void onInsLoadFailed(BaseInstance instance, AdapterError error) {
        super.onInsLoadFailed(instance, error);
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        if (shouldFinishLoad()) {
            boolean hasInventory = hasAvailableInventory();
            if (isManualTriggered && !hasInventory) {
                callbackLoadFailedOnManual(errorResult);
            }
            if (!hasInventory) {
                whenAllLoadFailed();
            }
            if (shouldNotifyAvailableChanged(hasInventory)) {
                DeveloperLog.LogD("onInsLoadFailed shouldFinishLoad shouldNotifyAvailableChanged " + hasInventory);
                onAvailabilityChanged(hasInventory, errorResult);
            }

            notifyUnLoadInsBidLose();
        } else {
            initOrFetchNextAdapter();
        }
    }

    /**
     * On ins open.
     *
     * @param instance the instance
     */
    @Override
    protected void onInsShowSuccess(BaseInstance instance, Scene scene) {
        super.onInsShowSuccess(instance, scene);
        //if availability changed from false to true
//        if (shouldNotifyAvailableChanged(false)) {
//            onAvailabilityChanged(false, null);
//        }
        isInShowingProgress = true;
        AdLog.getSingleton().LogD("Ad show success placementId: " + mPlacementId);
    }

    /**
     * On ins close.
     */
    @Override
    protected void onInsClosed(BaseInstance instance, Scene scene) {
        super.onInsClosed(instance, scene);
        isInShowingProgress = false;
        callbackAdClosed();
        boolean hasInventory = hasAvailableInventory();
        if (shouldNotifyAvailableChanged(hasInventory)) {
            onAvailabilityChanged(hasInventory, null);
        }
        loadAds(OmManager.LOAD_TYPE.CLOSE);
        AdLog.getSingleton().LogD("Ad close placementId: " + mPlacementId);
    }

    public int getAllLoadFailedCount() {
        return mAllLoadFailedCount.get();
    }

    public Map<Integer, Integer> getRfs() {
        if (mPlacement == null) {
            return null;
        }
        return mPlacement.getRfs();
    }

    protected void checkScheduleTaskStarted() {
        if (!mDidScheduleTaskStarted.get()) {
            scheduleLoadAdTask();
        }
    }

    private boolean shouldShowAd(String scene) {
        Error error = checkShowAvailable(scene);
        if (Preconditions.checkNotNull(error)) {
            callbackShowError(error);
            return false;
        }

        if (AdRateUtil.shouldBlockScene(mPlacement.getId(), mScene)) {
            error = ErrorBuilder.build(ErrorCode.CODE_SHOW_SCENE_CAPPED
                    , ErrorCode.MSG_SHOW_SCENE_CAPPED, -1);
            callbackShowError(error);
            return false;
        }
        return true;
    }

    /**
     * Finishes load when ads count suffices or all instances have been loaded: sum of ready, initFailed,
     * loadFailed, Capped
     *
     * @return should finish load or not
     */
    private boolean shouldFinishLoad() {
        int readyCount = InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);
        int allLoadedCount = InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE,
                BaseInstance.MEDIATION_STATE.INIT_FAILED, BaseInstance.MEDIATION_STATE.LOAD_FAILED,
                BaseInstance.MEDIATION_STATE.CAPPED);
        if (readyCount >= mInventorySize || allLoadedCount == mTotalIns.size()) {
            DeveloperLog.LogD("full of cache or loaded all ins, current load is finished : " +
                    readyCount);
            isInLoadingProgress = false;
            return true;
        }
        return false;
    }

    /**
     * when all ins load failed and no cache, recoder failed load count, report event
     */
    private void whenAllLoadFailed() {
        mAllLoadFailedCount.incrementAndGet();
        reportEvent(EventId.NO_MORE_OFFERS, AdsUtil.buildAbtReportData(mPlacement.getWfAbt(),
                PlacementUtils.placementEventParams(mPlacement != null ? mPlacement.getId() : "")));
    }

    /**
     * schedules Load Ad Task
     */
    private void scheduleLoadAdTask() {
        if (mPlacement == null || mPlacement.getRfs() == null || mPlacement.getRfs().isEmpty()) {
            return;
        }
        Map<Integer, Integer> rfs = mPlacement.getRfs();
        Collection<Integer> values = rfs.values();
        int delay = 0;
        for (Integer value : values) {
            // get first positive delay number
            if (value > 0) {
                delay = value;
                break;
            }
        }
        if (delay > 0) {
            mDidScheduleTaskStarted.set(true);
            DeveloperLog.LogD("post adsScheduleTask delay : " + delay);
            WorkExecutor.execute(new AdsScheduleTask(this, delay), delay,
                    TimeUnit.SECONDS);
        }
    }

    protected boolean shouldNotifyAvailableChanged(boolean available) {
        if (isInShowingProgress) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged : " + false + " because current is in showing");
            return false;
        }

        if ((isManualTriggered || mLastAvailability.get() != available)) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged for placement: " + mPlacement + " " + true);
//            isManualTriggered = false;
            mLastAvailability.set(available);
            return true;
        }
        DeveloperLog.LogD("shouldNotifyAvailableChanged for placement : " + mPlacement + " " + false);
        return false;
    }

    /**
     * Inits an adapter and loads. Skips if already in progress
     */
    private synchronized void initOrFetchNextAdapter() {
        int canLoadCount = 0;
        for (BaseInstance instance : mTotalIns) {
            BaseInstance.MEDIATION_STATE state = instance.getMediationState();
            if (state == BaseInstance.MEDIATION_STATE.INIT_PENDING ||
                    state == BaseInstance.MEDIATION_STATE.LOAD_PENDING) {
                ++canLoadCount;
            } else if (state == BaseInstance.MEDIATION_STATE.NOT_INITIATED) {
                instance.setReqId(mReqId);
                //init first if not
                CustomAdsAdapter adsAdapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
                if (adsAdapter == null) {
                    instance.setMediationState(BaseInstance.MEDIATION_STATE.INIT_FAILED);
                } else {
                    ++canLoadCount;
                    instance.setAdapter(adsAdapter);
                    initInsAndSendEvent(instance);
//                    TestUtil.getInstance().notifyInsConfig(mPlacement != null ? mPlacement.getId() : ""
//                            , instance, getLoadLimit()
//                            , InsManager.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE)
//                            , getLoadCount());
                }
            } else if (state == BaseInstance.MEDIATION_STATE.INITIATED
                    || state == BaseInstance.MEDIATION_STATE.NOT_AVAILABLE) {
                ++canLoadCount;
                instance.setReqId(mReqId);
                loadInsAndSendEvent(instance);
            }

            if (canLoadCount >= getLoadLimit()) {
                return;
            }
        }
        //
        if (canLoadCount == 0) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                    , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, -1);
            DeveloperLog.LogE(error.toString());
            boolean hasCache = hasAvailableInventory();
            if (hasCache) {
                if (shouldNotifyAvailableChanged(hasCache)) {
                    onAvailabilityChanged(hasCache, error);
                }
            } else {
                callbackLoadError(error);
            }
        }
    }

    protected boolean hasAvailableInventory() {
        return InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE) > 0;
    }

    /**
     * @return limit of loadable instances
     */
    private int getLoadLimit() {
        //compares with server issued max concurrent number
        return Math.min(mPlacement.getBs(), mInventorySize -
                InsManager.instanceCount(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE));
    }

    /**
     * re-calculates cached ads count
     */
    private void reSizeInventorySize() {
        int size = mInventorySize;
        if (mPlacement != null) {
            size = mPlacement.getCs();
        }
        mInventorySize = Math.min(size, mTotalIns.size());
    }

    private void removeBidResponseWhenLoad() {
        if (mBidResponses == null || mBidResponses.isEmpty()) {
            return;
        }
        List<Integer> availableIns = InsManager.getInsIdWithStatus(mTotalIns, BaseInstance.MEDIATION_STATE.AVAILABLE);
        if (availableIns.isEmpty()) {
            mBidResponses.clear();
            return;
        }

        Set<Integer> ids = mBidResponses.keySet();
        for (Integer id : ids) {
            if (availableIns.contains(id)) {
                BidResponse bidResponse = mBidResponses.get(id);
                if (bidResponse != null && bidResponse.isExpired()) {
                    resetMediationStateAndNotifyLose(InsManager.getInsById(mTotalIns, id));
                }
            } else {
                mBidResponses.remove(id);
            }
        }
    }

    private void resetMediationStateAndNotifyLose(BaseInstance instance) {
        if (instance.getMediationState() == BaseInstance.MEDIATION_STATE.AVAILABLE) {
            instance.setMediationState(BaseInstance.MEDIATION_STATE.NOT_AVAILABLE);
        }
        notifyUnShowedBidLose(instance);
    }

    private void notifyUnShowedBidLose(BaseInstance instance) {
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
        AuctionUtil.notifyLose(instance, bidResponse, BidLoseReason.INVENTORY_DID_NOT_MATERIALISE.getValue());
        instance.setBidResponse(null);
    }

    @Override
    protected void notifyUnLoadInsBidLose() {
        if (mTotalIns == null || mBidResponses == null) {
            return;
        }
        Map<BaseInstance, BidResponse> unLoadInsBidResponses = new HashMap<>();
        for (BaseInstance in : mTotalIns) {
            if (in == null) {
                continue;
            }
            if ((in.getMediationState() == BaseInstance.MEDIATION_STATE.NOT_INITIATED ||
                    in.getMediationState() == BaseInstance.MEDIATION_STATE.NOT_AVAILABLE) &&
                    mBidResponses.containsKey(in.getId())) {
                unLoadInsBidResponses.put(in, mBidResponses.get(in.getId()));
                mBidResponses.remove(in.getId());
                in.setBidResponse(null);
            }
        }
        if (!unLoadInsBidResponses.isEmpty()) {
            AuctionUtil.notifyLose(unLoadInsBidResponses, BidLoseReason.LOST_TO_HIGHER_BIDDER.getValue());
        }
    }

    private void reportEvent(int eventId, JSONObject data) {
        EventUploadManager.getInstance().uploadEvent(eventId, data);
    }

    /**
     * showing is available if
     * 1.Activity is available
     * 2.init finished
     * 3.placement isn't null
     */
    private Error checkShowAvailable(String scene) {
        if (isInShowingProgress) {
            DeveloperLog.LogE("show ad failed, current is showing");
            return ErrorBuilder.build(-1, "show ad failed, current is showing", -1);
        }
        //Activity is available?
//        if (!checkActRef()) {
//            return ErrorBuilder.build(ErrorCode.CODE_SHOW_UNKNOWN_INTERNAL_ERROR
//                    , ErrorCode.MSG_SHOW_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
//        }

        if (!Preconditions.checkNotNull(mPlacement)) {
            DeveloperLog.LogD("placement is null");
            return ErrorBuilder.build(ErrorCode.CODE_SHOW_INVALID_ARGUMENT
                    , ErrorCode.MSG_SHOW_INVALID_ARGUMENT, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
        }
        mScene = SceneUtil.getScene(mPlacement, scene);
        if (!Preconditions.checkNotNull(mScene)) {
            return ErrorBuilder.build(ErrorCode.CODE_SHOW_SCENE_NOT_FOUND
                    , ErrorCode.MSG_SHOW_SCENE_NOT_FOUND, -1);
        }
        return null;
    }

}
