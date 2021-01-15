// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.promotion;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.core.AbstractAdsManager;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.promotion.PromotionAdListener;
import com.openmediation.sdk.promotion.PromotionAdRect;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.PlacementInfo;

import java.lang.ref.WeakReference;
import java.util.Map;

public final class CpManager extends AbstractAdsManager implements CpManagerListener {

    private WeakReference<Activity> mActRefs;
    private PromotionAdRect mRect;
    private CpInstance mShowingInstance;

    public CpManager() {
        super();
    }

    public void initPromotionAd() {
        checkScheduleTaskStarted();
    }

    public void loadPromotionAd() {
        loadAdWithAction(OmManager.LOAD_TYPE.MANUAL);
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
        mActRefs = new WeakReference<>(activity);
        mRect = rect;
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
    protected void initInsAndSendEvent(Instance instance) {
        super.initInsAndSendEvent(instance);
        if (!(instance instanceof CpInstance)) {
            instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance, new Error(ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR,
                    "current is not an promotion adUnit", -1));
            return;
        }
        CpInstance cpInstance = (CpInstance) instance;
        cpInstance.setCpManagerListener(this);
        cpInstance.initCp(mActivityReference.get());
    }

    @Override
    protected boolean isInsAvailable(Instance instance) {
        if (instance instanceof CpInstance) {
            return ((CpInstance) instance).isCpAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(Instance instance) {
        if (instance instanceof CpInstance) {
            mShowingInstance = (CpInstance) instance;
            Activity activity = mActRefs == null ? mActivityReference.get() : mActRefs.get();
            mShowingInstance.showCp(activity, mRect, mScene);
        }
    }

    @Override
    protected void insLoad(Instance instance, Map<String, Object> extras) {
        CpInstance cpInstance = (CpInstance) instance;
        cpInstance.loadCp(mActivityReference.get(), extras);
    }

    @Override
    protected void onAvailabilityChanged(boolean available, Error error) {
        mListenerWrapper.onPromotionAdAvailabilityChanged(available);
    }

    @Override
    protected void callbackAvailableOnManual() {
        super.callbackAvailableOnManual();
        mListenerWrapper.onPromotionAdAvailabilityChanged(true);
    }

    @Override
    protected void callbackLoadError(Error error) {
        boolean hasCache = hasAvailableCache();
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
    public void onPromotionAdInitFailed(Error error, CpInstance instance) {
        onInsInitFailed(instance, error);
    }

    @Override
    public void onPromotionAdShowFailed(Error error, CpInstance instance) {
        isInShowingProgress = false;
        mListenerWrapper.onPromotionAdShowFailed(mScene, error);
    }

    @Override
    public void onPromotionAdShowSuccess(CpInstance instance) {
        onInsOpen(instance);
        mListenerWrapper.onPromotionAdShowed(mScene);
    }

    @Override
    public void onPromotionAdClicked(CpInstance instance) {
        mListenerWrapper.onPromotionAdClicked(mScene);
        onInsClick(instance);
    }

    @Override
    public void onPromotionAdHidden(CpInstance instance) {
        if (mActRefs != null) {
            mActRefs.clear();
        }
        onInsClose();
    }

    @Override
    public void onPromotionAdVisible(CpInstance instance) {
    }

    @Override
    public void onPromotionAdLoadSuccess(CpInstance instance) {
        onInsReady(instance);
    }

    @Override
    public void onPromotionAdLoadFailed(Error error, CpInstance instance) {
        DeveloperLog.LogD("CpManager onPromotionAdLoadFailed : " + instance + " error : " + error);
        onInsLoadFailed(instance, error);
    }
}
