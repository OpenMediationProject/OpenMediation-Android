// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.rewardedvideo;

import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.mediation.MediationRewardVideoListener;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.helper.IcHelper;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoListener;
import com.openmediation.sdk.core.AbstractAdsManager;
import com.openmediation.sdk.core.OmManager;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class RvManager extends AbstractAdsManager implements RvManagerListener {
    private Map<String, String> mExtIds = new HashMap<>();

    public RvManager() {
        super();
    }

    public void initRewardedVideo() {
        checkScheduleTaskStarted();
    }

    public void loadRewardedVideo() {
        loadAdWithAction(OmManager.LOAD_TYPE.MANUAL);
    }

    public void showRewardedVideo(String scene) {
        showAd(scene);
    }

    public boolean isRewardedVideoReady() {
        return isPlacementAvailable();
    }

    public void setRewardedExtId(String scene, String extId) {
        Scene placementScene = SceneUtil.getScene(mPlacement, scene);
        if (placementScene != null) {
            mExtIds.put(placementScene.getN(), extId);
        }
    }

    @Deprecated
    public void setRewardedVideoListener(RewardedVideoListener listener) {
        mListenerWrapper.addRewardedVideoListener(listener);
    }

    public void addRewardedVideoListener(RewardedVideoListener listener) {
        mListenerWrapper.addRewardedVideoListener(listener);
    }

    public void removeRewardedVideoListener(RewardedVideoListener listener) {
        mListenerWrapper.removeRewardedVideoListener(listener);
    }

    public void setMediationRewardedVideoListener(MediationRewardVideoListener listener) {
        mListenerWrapper.setMediationRewardedVideoListener(listener);
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacement.getId()).getPlacementInfo(mPlacement.getT());
    }

    @Override
    protected void initInsAndSendEvent(Instance instance) {
        if (!(instance instanceof RvInstance)) {
            instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance, new Error(ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR,
                    "current is not an rewardedVideo adUnit", -1));
            return;
        }
        RvInstance rvInstance = (RvInstance) instance;
        rvInstance.setRvManagerListener(this);
        rvInstance.initRv(mActivityReference.get());
    }

    @Override
    protected boolean isInsAvailable(Instance instance) {
        if (instance instanceof RvInstance) {
            return ((RvInstance) instance).isRvAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(final Instance instance) {
        ((RvInstance) instance).showRv(mActivityReference.get(), mScene);
    }

    @Override
    protected void insLoad(Instance instance, Map<String, Object> extras) {
        RvInstance rvInstance = (RvInstance) instance;
        rvInstance.loadRv(mActivityReference.get(), extras);
    }

    @Override
    protected void onAvailabilityChanged(boolean available, Error error) {
        mListenerWrapper.onRewardedVideoAvailabilityChanged(available);
    }

    @Override
    protected void callbackAvailableOnManual() {
        super.callbackAvailableOnManual();
        mListenerWrapper.onRewardedVideoAvailabilityChanged(true);
        mListenerWrapper.onRewardedVideoLoadSuccess();
    }

    @Override
    protected void callbackLoadSuccessOnManual() {
        super.callbackLoadSuccessOnManual();
        mListenerWrapper.onRewardedVideoLoadSuccess();
    }

    @Override
    protected void callbackLoadFailedOnManual(Error error) {
        super.callbackLoadFailedOnManual(error);
        mListenerWrapper.onRewardedVideoLoadFailed(error);
    }

    @Override
    protected void callbackLoadError(Error error) {
        mListenerWrapper.onRewardedVideoLoadFailed(error);
        boolean hasCache = hasAvailableCache();
        if (shouldNotifyAvailableChanged(hasCache)) {
            mListenerWrapper.onRewardedVideoAvailabilityChanged(hasCache);
        }
        super.callbackLoadError(error);
    }

    @Override
    protected void callbackShowError(Error error) {
        super.callbackShowError(error);
        mListenerWrapper.onRewardedVideoAdShowFailed(mScene, error);
    }

    @Override
    protected void callbackAdClosed() {
        mListenerWrapper.onRewardedVideoAdClosed(mScene);
    }

    @Override
    public void onRewardedVideoInitSuccess(RvInstance rvInstance) {
        loadInsAndSendEvent(rvInstance);
    }

    @Override
    public void onRewardedVideoInitFailed(Error error, RvInstance rvInstance) {
        onInsInitFailed(rvInstance, error);
    }

    @Override
    public void onRewardedVideoAdShowFailed(Error error, RvInstance rvInstance) {
        isInShowingProgress = false;
        mListenerWrapper.onRewardedVideoAdShowFailed(mScene, error);
    }

    @Override
    public void onRewardedVideoAdShowSuccess(RvInstance rvInstance) {
        onInsOpen(rvInstance);
        mListenerWrapper.onRewardedVideoAdShowed(mScene);
    }

    @Override
    public void onRewardedVideoAdClosed(RvInstance rvInstance) {
        onInsClose();
    }

    @Override
    public void onRewardedVideoLoadSuccess(RvInstance rvInstance) {
        onInsReady(rvInstance);
    }

    @Override
    public void onRewardedVideoLoadFailed(Error error, RvInstance rvInstance) {
        DeveloperLog.LogD("RvManager onRewardedVideoLoadFailed : " + rvInstance + " error : " + error);
        onInsLoadFailed(rvInstance, error);
    }

    @Override
    public void onRewardedVideoAdStarted(RvInstance rvInstance) {
        mListenerWrapper.onRewardedVideoAdStarted(mScene);
    }

    @Override
    public void onRewardedVideoAdEnded(RvInstance rvInstance) {
        mListenerWrapper.onRewardedVideoAdEnded(mScene);
    }

    @Override
    public void onRewardedVideoAdRewarded(RvInstance rvInstance) {
        if (!mExtIds.isEmpty() && mScene != null && mExtIds.containsKey(mScene.getN())) {
            IcHelper.icReport(rvInstance.getPlacementId(), rvInstance.getMediationId(),
                    rvInstance.getId(), mScene.getId(), mExtIds.get(mScene.getN()));
        }
        mListenerWrapper.onRewardedVideoAdRewarded(mScene);
    }

    @Override
    public void onRewardedVideoAdClicked(RvInstance rvInstance) {
        mListenerWrapper.onRewardedVideoAdClicked(mScene);
        onInsClick(rvInstance);
    }
}
