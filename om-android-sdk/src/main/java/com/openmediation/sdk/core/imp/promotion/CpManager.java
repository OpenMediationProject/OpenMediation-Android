// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.promotion;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.AbstractInventoryAds;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.promotion.PromotionAdListener;
import com.openmediation.sdk.promotion.PromotionAdRect;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;

import java.lang.ref.WeakReference;
import java.util.Map;

public final class CpManager extends AbstractInventoryAds implements CpManagerListener {

    private PromotionAdRect mRect;
    private CpInstance mShowingInstance;

    private WeakReference<Activity> mCpActReference;

    public CpManager() {
        super();
    }

    public void initPromotionAd() {
        checkScheduleTaskStarted();
    }

    public void loadPromotionAd() {
        loadAds(OmManager.LOAD_TYPE.MANUAL);
    }

    public void showPromotionAd(Activity activity, PromotionAdRect rect, String scene) {
        String errorMsg = "";
        if (activity == null || activity.isFinishing()) {
            errorMsg = "PromotionAd Show Failed: activity is destroyed";
        }
        if (rect == null || (rect.getWidth() <= 0 && rect.getHeight() <= 0)) {
            errorMsg = "PromotionAd Show Failed: width or height must be positive";
        }
        if (!TextUtils.isEmpty(errorMsg)) {
            AdLog.getSingleton().LogE(errorMsg);
            Error error = new Error(ErrorCode.CODE_SHOW_INVALID_ARGUMENT, errorMsg, -1);
            mListenerWrapper.onPromotionAdShowFailed(SceneUtil.getScene(mPlacement, scene), error);
            return;
        }
        mRect = rect;
        mCpActReference = new WeakReference<>(activity);
        showAd(scene);
    }

    public void hidePromotionAd() {
        if (mShowingInstance != null) {
            mShowingInstance.hideCp();
        } else {
            DeveloperLog.LogD("PromotionAd is not showing");
            AdLog.getSingleton().LogD("PromotionAd is not showing");
        }
    }

    public boolean isPromotionAdReady() {
        return isPlacementAvailable();
    }

    public void addPromotionAdListener(PromotionAdListener listener) {
        mListenerWrapper.addPromotionAdListener(listener);
    }

    public void removePromotionAdListener(PromotionAdListener listener) {
        mListenerWrapper.removePromotionAdListener(listener);
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacement.getId()).getPlacementInfo(mPlacement.getT());
    }

    @Override
    protected void initInsAndSendEvent(BaseInstance instance) {
        super.initInsAndSendEvent(instance);
        if (!(instance instanceof CpInstance)) {
            instance.setMediationState(BaseInstance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance, new Error(ErrorCode.CODE_LOAD_UNKNOWN_ERROR,
                    "current is not an promotion adUnit", -1));
            return;
        }
        CpInstance cpInstance = (CpInstance) instance;
        cpInstance.setCpManagerListener(this);
        cpInstance.initCp(mActRefs.get());
    }

    @Override
    protected boolean isInsAvailable(BaseInstance instance) {
        if (instance instanceof CpInstance) {
            return ((CpInstance) instance).isCpAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(BaseInstance instance) {
        if (instance instanceof CpInstance) {
            mShowingInstance = (CpInstance) instance;
            Activity activity = mCpActReference == null ? null : mCpActReference.get();
            mShowingInstance.showCp(activity, mRect, mScene);
        }
    }

    @Override
    protected void insLoad(BaseInstance instance, Map<String, Object> extras) {
        super.insLoad(instance, extras);
        CpInstance cpInstance = (CpInstance) instance;
        cpInstance.loadCp(mActRefs.get(), extras);
    }

    @Override
    protected void onAvailabilityChanged(boolean available, Error error) {
        mListenerWrapper.onPromotionAdAvailabilityChanged(available);
    }

    @Override
    protected void callbackAvailableOnManual(BaseInstance instance) {
        super.callbackAvailableOnManual(instance);
        mListenerWrapper.onPromotionAdAvailabilityChanged(true);
    }

    @Override
    protected void callbackLoadError(Error error) {
        boolean hasCache = hasAvailableInventory();
        if (shouldNotifyAvailableChanged(hasCache)) {
            mListenerWrapper.onPromotionAdAvailabilityChanged(hasCache);
        }
        super.callbackLoadError(error);
    }

    @Override
    protected void callbackShowError(Error error) {
        super.callbackShowError(error);
        mListenerWrapper.onPromotionAdShowFailed(mScene, error);
    }

    @Override
    protected void callbackAdClosed() {
        mListenerWrapper.onPromotionAdHidden(mScene);
    }

    @Override
    public void onPromotionAdInitSuccess(CpInstance instance) {
        loadInsAndSendEvent(instance);
    }

    @Override
    public void onPromotionAdInitFailed(CpInstance instance, AdapterError error) {
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        onInsInitFailed(instance, errorResult);
    }

    @Override
    public void onPromotionAdShowFailed(CpInstance instance, AdapterError error) {
        isInShowingProgress = false;
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationID:" + instance.getMediationId() + ", error:" + error, -1);
        onInsShowFailed(instance, error, mScene);
        mListenerWrapper.onPromotionAdShowFailed(mScene, errorResult);
    }

    @Override
    public void onPromotionAdShowSuccess(CpInstance instance) {
        onInsShowSuccess(instance, mScene);
        mListenerWrapper.onPromotionAdShowed(mScene);
    }

    @Override
    public void onPromotionAdClicked(CpInstance instance) {
        onInsClicked(instance, mScene);
        mListenerWrapper.onPromotionAdClicked(mScene);
    }

    @Override
    public void onPromotionAdHidden(CpInstance instance) {
        if (mActRefs != null) {
            mActRefs.clear();
        }
        onInsClosed(instance, mScene);
    }

    @Override
    public void onPromotionAdVisible(CpInstance instance) {
    }

    @Override
    public void onPromotionAdLoadSuccess(CpInstance instance) {
        onInsLoadSuccess(instance, false);
    }

    @Override
    public void onPromotionAdLoadFailed(CpInstance instance, AdapterError error) {
        DeveloperLog.LogE("CpManager onPromotionAdLoadFailed : " + instance + " error : " + error);
        onInsLoadFailed(instance, error, false);
    }

    @Override
    public void onBidSuccess(BaseInstance instance, BidResponse response) {
        onInsC2SBidSuccess(instance, response);
    }

    @Override
    public void onBidFailed(BaseInstance instance, String error) {
        onInsC2SBidFailed(instance, error);
    }

    @Override
    public void onAdExpired(BaseInstance instance) {
        resetMediationStateAndNotifyLose(instance);
    }
}
