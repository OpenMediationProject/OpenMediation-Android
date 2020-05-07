// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import android.app.Activity;

import com.openmediation.sdk.bid.AdTimingAuctionManager;
import com.openmediation.sdk.bid.AdTimingBidResponse;
import com.openmediation.sdk.bid.AuctionCallback;
import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.core.runnable.AdsScheduleTask;
import com.openmediation.sdk.utils.ActLifecycle;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdapterUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.helper.HbHelper;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.InsUtil;
import com.openmediation.sdk.utils.helper.LrReportHelper;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.Preconditions;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.device.DeviceUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorBuilder;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.utils.request.network.Request;
import com.openmediation.sdk.utils.request.network.Response;
import com.openmediation.sdk.utils.request.network.util.NetworkChecker;
import com.openmediation.sdk.InitCallback;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Abstract ads manager.
 */
public abstract class AbstractAdsManager extends AdsApi implements InitCallback, AuctionCallback,
        Request.OnRequestCallback, HbHelper.OnHbCallback {
    /**
     * The M activity.
     */
    protected Activity mActivity;
    /**
     * The M placement.
     */
    protected Placement mPlacement;
    /**
     * The M listener wrapper.
     */
    protected ListenerWrapper mListenerWrapper;
    /**
     * The Is in showing progress.
     */
    protected boolean isInShowingProgress;
    /**
     * The M scene.
     */
    protected Scene mScene;
    private List<AdTimingBidResponse> mBidResponses;

    private OmManager.LOAD_TYPE mLoadType;
    //Adapters to be loaded
    private CopyOnWriteArrayList<Instance> mTotalIns;
    //
    private int mCacheSize;
    //
    private boolean isInLoadingProgress;
    //
    private boolean isManualTriggered;

    //last callback's status
    private AtomicBoolean mLastAvailability = new AtomicBoolean(false);
    private AtomicBoolean mDidScheduleTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean isAReadyReported = new AtomicBoolean(false);

    /**
     * Instantiates a new Abstract ads manager.
     */
    public AbstractAdsManager() {
        mTotalIns = new CopyOnWriteArrayList<>();
        mListenerWrapper = new ListenerWrapper();
        mBidResponses = new ArrayList<>();
    }

    /**
     * Ends this loading
     */
    @Override
    protected void callbackLoadError(Error error) {
        isInLoadingProgress = false;
        isManualTriggered = false;
    }

    @Override
    protected void callbackAvailableOnManual() {
        isManualTriggered = false;
    }

    @Override
    protected void callbackShowError(Error error) {
        isInShowingProgress = false;
    }

    /**
     * For an instance to load ads
     */
    @Override
    protected void loadInsAndSendEvent(Instance instance) {
        if (AdRateUtil.shouldBlockInstance(Preconditions.checkNotNull(mPlacement) ?
                mPlacement.getId() : "" + instance.getKey(), instance)) {
            callbackCappedError(instance);
            return;
        }
        LrReportHelper.report(instance, mLoadType.getValue(), mPlacement.getWfAbt(), CommonConstants.INSTANCE_LOAD);
        instance.reportInsLoad();
        if (instance.getBidState() == BaseInstance.BID_STATE.BID_SUCCESS) {
            AuctionUtil.instanceNotifyBidWin(mPlacement.getHbAbt(), instance);
            inLoadWithBid(instance, AuctionUtil.generateMapRequestData(mBidResponses, instance));
            AuctionUtil.removeBidResponse(mBidResponses, instance);
        } else {
            insLoad(instance);
        }
    }

    @Override
    public void onResume(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActivity = activity;
            if (mTotalIns != null && !mTotalIns.isEmpty()) {
                for (Instance in : mTotalIns) {
                    in.onResume(activity);
                }
            }
        }
    }

    @Override
    public void onPause(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActivity = activity;
            if (mTotalIns != null && !mTotalIns.isEmpty()) {
                for (Instance in : mTotalIns) {
                    in.onPause(activity);
                }
            }
        }
    }

    @Override
    protected void setCurrentPlacement(Placement placement) {
        if (Preconditions.checkNotNull(placement)) {
            mPlacement = placement;
            mCacheSize = placement.getCs();
            mListenerWrapper.setPlacementId(placement.getId());
        }
    }

    /**
     * before load starts, checks: init---frequency control---show in progress---trigger type
     * When manually triggered, first checks available ads, and replenishes if necessary before checking if loading is in progress
     * Tiggers other than Manual are automatically called by the SDK,
     *
     * @param type load triggered by: Manual,Init,AdClose,Interval
     */
    @Override
    protected void loadAdWithAction(OmManager.LOAD_TYPE type) {
        DeveloperLog.LogD("loadAdWithAction : " + mPlacement + " action: " + type.toString());

        int availableCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);

        //if load is manually triggered
        if (type == OmManager.LOAD_TYPE.MANUAL) {
            isManualTriggered = true;
            //only checks ScheduleTask when manually triggered
            checkScheduleTaskStarted();
        } else {
            String pid = mPlacement != null ? mPlacement.getId() : "";
            reportEvent(EventId.ATTEMPT_TO_BRING_NEW_FEED, PlacementUtils.placementEventParams(pid));
            if (availableCount > 0) {
                reportEvent(EventId.AVAILABLE_FROM_CACHE, PlacementUtils.placementEventParams(pid));
            }
        }

        //returns if load can't start
        Error error = checkLoadAvailable();
        if (error != null) {
            AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
            return;
        }

        //When manually triggered, first checks available ads in cache
        if (isManualTriggered) {
            if (hasAvailableCache() && shouldNotifyAvailableChanged(true)) {
                callbackAvailableOnManual();
            }
        }

        //to replenish?
        if (availableCount < mCacheSize) {
            delayLoad(type);
        } else {
            DeveloperLog.LogD("cache is full, cancel this request");
            error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , "cache is full, cancel this request", ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
        }
    }

    @Override
    protected void showAd(String scene) {
        Error error = checkShowAvailable(scene);
        if (Preconditions.checkNotNull(error)) {
            callbackShowError(error);
            return;
        }

        if (AdRateUtil.shouldBlockScene(mPlacement.getId(), mScene)) {
            error = ErrorBuilder.build(ErrorCode.CODE_SHOW_SCENE_CAPPED
                    , ErrorCode.MSG_SHOW_SCENE_CAPPED, -1);
            callbackShowError(error);
            return;
        }
        for (Instance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                continue;
            }
            insShow(in);
            return;
        }
        error = ErrorBuilder.build(ErrorCode.CODE_SHOW_NO_AD_READY
                , ErrorCode.MSG_SHOW_NO_AD_READY, -1);
        DeveloperLog.LogE(error.toString());
        callbackShowError(error);
    }

    @Override
    protected boolean isPlacementAvailable() {
        if (isInShowingProgress || !Preconditions.checkNotNull(mPlacement) || mTotalIns == null || mTotalIns.isEmpty()) {
            return false;
        }

        for (Instance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                continue;
            }
            return true;
        }
        return super.isPlacementAvailable();
    }

    @Override
    protected boolean hasAvailableCache() {
        return InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE) > 0;
    }

    /**
     *
     */
    @Override
    protected void checkScheduleTaskStarted() {
        if (!mDidScheduleTaskStarted.get()) {
            scheduleLoadAdTask();
        }
    }

    /**
     * Notifies availability change when manually trigged, or cache availability changed
     */
    @Override
    protected boolean shouldNotifyAvailableChanged(boolean available) {
        if (isInShowingProgress) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged : " + false + " because current is in showing");
            return false;
        }
        if ((isManualTriggered || mLastAvailability.get() != available)) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged for placement: " + mPlacement + " " + true);
            isManualTriggered = false;
            mLastAvailability.set(available);
            return true;
        }
        DeveloperLog.LogD("shouldNotifyAvailableChanged for placement : " + mPlacement + " " + false);
        return super.shouldNotifyAvailableChanged(available);
    }

    @Override
    protected void onInsInitFailed(Instance instance, Error error) {
        if (shouldFinishLoad()) {
            boolean hasCache = hasAvailableCache();
            if (isManualTriggered && !hasCache) {
                callbackLoadFailedOnManual(error);
            }
            if (!hasCache) {
                reportEvent(EventId.NO_MORE_OFFERS, AdsUtil.buildAbtReportData(mPlacement.getWfAbt(),
                        PlacementUtils.placementEventParams(mPlacement != null ? mPlacement.getId() : "")));
            }
            if (shouldNotifyAvailableChanged(hasCache)) {
                onAvailabilityChanged(hasCache, error);
            }
            instanceNotifyBidLose();
        } else {
            initOrFetchNextAdapter();
        }
    }

    @Override
    protected synchronized void onInsReady(final Instance instance) {
        LrReportHelper.report(instance, mLoadType.getValue(), mPlacement.getWfAbt(), CommonConstants.INSTANCE_READY);
        if (!shouldFinishLoad()) {
            initOrFetchNextAdapter();
        } else {
            instanceNotifyBidLose();
        }
        if (isManualTriggered) {
            callbackLoadSuccessOnManual();
        }
        if (shouldNotifyAvailableChanged(true)) {
            if (!isAReadyReported.get()) {
                isAReadyReported.set(true);
                LrReportHelper.report(instance.getPlacementId(), mLoadType.getValue(), mPlacement.getWfAbt(),
                        CommonConstants.WATERFALL_READY);
            }
            onAvailabilityChanged(true, null);
        }
    }

    @Override
    protected synchronized void onInsLoadFailed(Instance instance, Error error) {
        if (shouldFinishLoad()) {
            boolean hasCache = hasAvailableCache();
            if (isManualTriggered && !hasCache) {
                callbackLoadFailedOnManual(error);
            }
            if (!hasCache) {
                reportEvent(EventId.NO_MORE_OFFERS, AdsUtil.buildAbtReportData(mPlacement.getWfAbt(),
                        PlacementUtils.placementEventParams(mPlacement != null ? mPlacement.getId() : "")));
            }
            if (shouldNotifyAvailableChanged(hasCache)) {
                DeveloperLog.LogD("onInsLoadFailed shouldFinishLoad shouldNotifyAvailableChanged " + hasCache);
                onAvailabilityChanged(hasCache, error);
            }

            instanceNotifyBidLose();
        } else {
            initOrFetchNextAdapter();
        }
    }

    @Override
    protected void onInsOpen(final Instance instance) {
        LrReportHelper.report(instance, mScene != null ? mScene.getId() : -1, mLoadType.getValue(),
                CommonConstants.INSTANCE_IMPR);
        //if availability changed from false to true
        if (shouldNotifyAvailableChanged(false)) {
            onAvailabilityChanged(false, null);
        }
        isInShowingProgress = true;
    }

    @Override
    protected void onInsClick(Instance instance) {
        LrReportHelper.report(instance, mScene != null ? mScene.getId() : -1, mLoadType.getValue(),
                CommonConstants.INSTANCE_CLICK);
    }

    @Override
    protected void onInsClose() {
        isInShowingProgress = false;
        callbackAdClosed();
        boolean hasCache = hasAvailableCache();
        if (shouldNotifyAvailableChanged(hasCache)) {
            onAvailabilityChanged(hasCache, null);
        }
        checkShouldLoadsWhenClose();
    }

    @Override
    protected void onInsCapped(Instance instance) {
        Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_CAPPED, "load ad failed", -1);
        if (shouldFinishLoad()) {
            boolean hasCache = hasAvailableCache();
            if (shouldNotifyAvailableChanged(hasCache)) {
                onAvailabilityChanged(hasCache, error);
            }
            instanceNotifyBidLose();
        } else {
            initOrFetchNextAdapter();
        }
    }

    @Override
    public void onSuccess() {
        //only trigged by manual 
        delayLoad(OmManager.LOAD_TYPE.MANUAL);
    }

    @Override
    public void onError(Error result) {
        //
        callbackLoadError(result);
    }

    @Override
    public void onHbSuccess(int abt, BaseInstance[] instances) {
        mPlacement.setHbAbt(abt);
        AdTimingAuctionManager.getInstance().bid(mActivity, mPlacement.getId(), instances, abt,
                mPlacement.getT(), this);
    }

    @Override
    public void onHbFailed(String error) {
        try {
            WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, null, this);
        } catch (Exception e) {
            Error adTimingError = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
            callbackLoadError(adTimingError);
            DeveloperLog.LogD("load ad error", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    @Override
    public void onBidComplete(List<AdTimingBidResponse> responses) {
        try {
            if (responses != null) {
                mBidResponses.addAll(responses);
            }
            WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, responses, this);
        } catch (Exception e) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
            callbackLoadError(error);
            DeveloperLog.LogD("load ad error", e);
            CrashUtil.getSingleton().saveException(e);
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

            //when not trigged by init, checks cache before aReady reporting
            if (mLoadType != OmManager.LOAD_TYPE.INIT) {
                int availableCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
                if (availableCount > 0) {
                    isAReadyReported.set(true);
                    LrReportHelper.report(mPlacement.getId(), mLoadType.getValue(), mPlacement.getWfAbt(),
                            CommonConstants.WATERFALL_READY);
                }
            }

            JSONObject clInfo = new JSONObject(response.body().string());
            mPlacement.setWfAbt(clInfo.optInt("abt"));
            List<Instance> tmp = WaterFallHelper.getListInsResult(clInfo, mPlacement);
            if (tmp == null || tmp.isEmpty()) {
                List<Instance> lastAvailableIns = InsUtil.getInsWithStatus(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
                if (lastAvailableIns == null || lastAvailableIns.isEmpty()) {
                    Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                            , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString() + ", tmp:" + tmp + ", last:" + lastAvailableIns);
                    callbackLoadError(error);
                } else {
                    DeveloperLog.LogD("request cl success, but ins[] is empty, but has history");
                    isInLoadingProgress = false;
                }
            } else {
                List<Instance> lastAvailableIns = InsUtil.getInsWithStatus(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
                if (!lastAvailableIns.isEmpty()) {
                    InsUtil.reOrderIns(lastAvailableIns, tmp);
                }
                mTotalIns.clear();
                mTotalIns.addAll(tmp);
                InsUtil.resetInsStateOnClResponse(mTotalIns);
                DeveloperLog.LogD("TotalIns is : " + mTotalIns.toString());
                int availableCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
                reSizeCacheSize();
                DeveloperLog.LogD("after cl, cache size is : " + mCacheSize);
                //if availableCount == mCacheSize, do not load any new instance
                if (availableCount == mCacheSize) {
                    DeveloperLog.LogD("no new ins should be loaded, current load progress finishes");
                    isInLoadingProgress = false;
                } else {
                    doLoadOnUiThread();
                }
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
        DeveloperLog.LogD(errorResult.toString() + ", request cl failed : " + errorResult + ", error" + error);
        callbackLoadError(errorResult);
    }

    public void loadAdWithInterval() {
        loadAdWithAction(OmManager.LOAD_TYPE.INTERVAL);
    }

    /**
     * schedules Load Ad Task
     */
    private void scheduleLoadAdTask() {
        if (mPlacement == null || mPlacement.getRf() <= 0) {
            return;
        }
        mDidScheduleTaskStarted.set(true);
        WorkExecutor.scheduleWithFixedDelay(new AdsScheduleTask(this), mPlacement.getRf(), mPlacement.getRf(),
                TimeUnit.SECONDS);
    }

    private void delayLoad(final OmManager.LOAD_TYPE type) {
        try {
            isInLoadingProgress = true;
            mLoadType = type;
            isAReadyReported.set(false);
            mBidResponses.clear();
            if (mPlacement.hasHb()) {
                HbHelper.executeHb(mPlacement, type, this);
            } else {
                WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, null, this);
            }
        } catch (Exception e) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
            callbackLoadError(error);
            DeveloperLog.LogD("load ad error", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * load can start if
     * 1.Activity available
     * 2.network available
     */
    private Error checkLoadAvailable() {
        //does nothing if Showing in Progress
        if (isInShowingProgress || isInLoadingProgress) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
            DeveloperLog.LogD("loadAdWithAction: " + mPlacement + ", type:"
                    + mLoadType.toString() + " stopped," +
                    " cause current is in loading/showing progress");
            callbackLoadError(error);
            return error;
        }
        //activity available?
        if (!checkActRef()) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            DeveloperLog.LogE(error.toString() + "load ad but activity is not available");
            callbackLoadError(error);
            return error;
        }

        //network available?
        if (!NetworkChecker.isAvailable(mActivity)) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_NETWORK_ERROR
                    , ErrorCode.MSG_LOAD_NETWORK_ERROR, -1);
            DeveloperLog.LogE("load ad network not available");
            callbackLoadError(error);
            return error;
        }

        if (!Preconditions.checkNotNull(mPlacement)) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
            DeveloperLog.LogE(error.toString() + ", placement is null");
            callbackLoadError(error);
            return error;
        }

        if (AdRateUtil.shouldBlockPlacement(mPlacement)) {
            Error error = new Error(ErrorCode.CODE_LOAD_CAPPED
                    , ErrorCode.MSG_LOAD_CAPPED, -1);
            DeveloperLog.LogD(error.toString() + ", Placement :" + mPlacement.getId() + " is blocked");
            callbackLoadError(error);
            return error;
        }
        return null;
    }

    /**
     *
     */
    private void doLoadOnUiThread() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initOrFetchNextAdapter();
            }
        });
    }

    /**
     * @return limit of loadable instances
     */
    private int getLoadLimit() {
        //compares with server issued max concurrent number
        return Math.min(mPlacement.getBs(), mCacheSize -
                InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE));
    }

    /**
     * Inits an adapter and loads. Skips if already in progress
     */
    private synchronized void initOrFetchNextAdapter() {
        int canLoadCount = 0;
        for (Instance instance : mTotalIns) {
            Instance.MEDIATION_STATE state = instance.getMediationState();
            if (state == Instance.MEDIATION_STATE.INIT_PENDING ||
                    state == Instance.MEDIATION_STATE.LOAD_PENDING) {
                ++canLoadCount;
            } else if (state == Instance.MEDIATION_STATE.NOT_INITIATED) {
                //inits first if not
                CustomAdsAdapter adsAdapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
                if (adsAdapter == null) {
                    instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
                } else {
                    ++canLoadCount;
                    instance.setAdapter(adsAdapter);
                    initInsAndSendEvent(instance);
                }
            } else if (state == Instance.MEDIATION_STATE.INITIATED
                    || state == Instance.MEDIATION_STATE.NOT_AVAILABLE) {
                ++canLoadCount;
                this.loadInsAndSendEvent(instance);
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
            boolean hasCache = hasAvailableCache();
            if (hasCache) {
                if (shouldNotifyAvailableChanged(hasCache)) {
                    onAvailabilityChanged(hasCache, error);
                }
            } else {
                callbackLoadError(error);
            }
        }
    }

    /**
     * Finishes load when ads count suffices or all instances have been loaded: sum of ready, initFailed,
     * loadFailed, Capped
     *
     * @return should finish load or not
     */
    private boolean shouldFinishLoad() {
        int readyCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
        int allLoadedCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE,
                Instance.MEDIATION_STATE.INIT_FAILED, Instance.MEDIATION_STATE.LOAD_FAILED,
                Instance.MEDIATION_STATE.CAPPED);
        if (readyCount >= mCacheSize || allLoadedCount == mTotalIns.size()) {
            DeveloperLog.LogD("full of cache or loaded all ins, current load is finished : " +
                    readyCount);
            isInLoadingProgress = false;
            return true;
        }
        return false;
    }

    /**
     * Called at ads close
     */
    private void checkShouldLoadsWhenClose() {
        loadAdWithAction(OmManager.LOAD_TYPE.CLOSE);
    }

    /**
     * @return activity's availability
     */
    private boolean checkActRef() {
        if (!DeviceUtil.isActivityAvailable(mActivity)) {
            Activity activity = ActLifecycle.getInstance().getActivity();
            if (activity == null) {
                return false;
            }
            mActivity = activity;
        }
        return true;
    }

    /**
     * showing is available if
     * 1.Activity is available
     * 2.init finished
     * 3.placement isn't null
     */
    private Error checkShowAvailable(String scene) {
        if (isInShowingProgress) {
            DeveloperLog.LogE("show ad failed,current is showing");
            return ErrorBuilder.build(-1, "show ad failed,current is showing", -1);
        }
        //Activity is available?
        if (!checkActRef()) {
            return ErrorBuilder.build(ErrorCode.CODE_SHOW_UNKNOWN_INTERNAL_ERROR
                    , ErrorCode.MSG_SHOW_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
        }

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

    /**
     * re-calculates cached ads count
     */
    private void reSizeCacheSize() {
        mCacheSize = Math.min(mCacheSize, mTotalIns.size());
    }

    private void instanceNotifyBidLose() {
        if (mTotalIns == null) {
            return;
        }

        AuctionUtil.instanceNotifyBidLose(mBidResponses, mPlacement);
    }

    private void reportEvent(int eventId, JSONObject data) {
        EventUploadManager.getInstance().uploadEvent(eventId, data);
    }
}
