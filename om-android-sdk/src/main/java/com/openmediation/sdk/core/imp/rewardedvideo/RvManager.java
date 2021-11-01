// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.rewardedvideo;

import com.openmediation.sdk.core.AbstractInventoryAds;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.MediationRewardVideoListener;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.helper.IcHelper;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoListener;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class RvManager extends AbstractInventoryAds implements RvManagerListener {
    private Map<String, String> mExtIds = new HashMap<>();

    public RvManager() {
        super();
    }

    public void initRewardedVideo() {
        checkScheduleTaskStarted();
    }

    public void loadRewardedVideo() {
        loadAds(OmManager.LOAD_TYPE.MANUAL);
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
    protected void initInsAndSendEvent(BaseInstance instance) {
        super.initInsAndSendEvent(instance);
        if (!(instance instanceof RvInstance)) {
            instance.setMediationState(BaseInstance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance, new Error(ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR,
                    "current is not an rewardedVideo adUnit", -1));
            return;
        }
        RvInstance rvInstance = (RvInstance) instance;
        rvInstance.setRvManagerListener(this);
        rvInstance.initRv(mActRefs.get());
    }

    @Override
    protected boolean isInsAvailable(BaseInstance instance) {
        if (instance instanceof RvInstance) {
            return ((RvInstance) instance).isRvAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(final BaseInstance instance) {
        ((RvInstance) instance).showRv(mActRefs.get(), mScene);
    }

    @Override
    protected void insLoad(BaseInstance instance, Map<String, Object> extras) {
        RvInstance rvInstance = (RvInstance) instance;
        rvInstance.loadRv(mActRefs.get(), extras);
    }

    @Override
    protected void onAvailabilityChanged(boolean available, Error error) {
        mListenerWrapper.onRewardedVideoAvailabilityChanged(available);
    }

    @Override
    protected void callbackAvailableOnManual(BaseInstance instance) {
        super.callbackAvailableOnManual(instance);
        mListenerWrapper.onRewardedVideoAvailabilityChanged(true);
        mListenerWrapper.onRewardedVideoLoadSuccess();
    }

    @Override
    protected void callbackLoadSuccessOnManual(BaseInstance instance) {
        super.callbackLoadSuccessOnManual(instance);
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
        boolean hasCache = hasAvailableInventory();
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
    public void onRewardedVideoInitFailed(RvInstance rvInstance, AdapterError error) {
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        onInsInitFailed(rvInstance, errorResult);
    }

    @Override
    public void onRewardedVideoAdShowFailed(RvInstance rvInstance, AdapterError error) {
        isInShowingProgress = false;
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER, error.toString(), -1);
        onInsShowFailed(rvInstance, error, mScene);
        mListenerWrapper.onRewardedVideoAdShowFailed(mScene, errorResult);
    }

    @Override
    public void onRewardedVideoAdShowSuccess(RvInstance rvInstance) {
        onInsShowSuccess(rvInstance, mScene);
        mListenerWrapper.onRewardedVideoAdShowed(mScene);
    }

    @Override
    public void onRewardedVideoAdClosed(RvInstance rvInstance) {
        onInsClosed(rvInstance, mScene);
    }

    @Override
    public void onRewardedVideoLoadSuccess(RvInstance rvInstance) {
        onInsLoadSuccess(rvInstance, false);
    }

    @Override
    public void onRewardedVideoLoadFailed(RvInstance rvInstance, AdapterError error) {
        onInsLoadFailed(rvInstance, error, false);
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
        onInsClicked(rvInstance, mScene);
        mListenerWrapper.onRewardedVideoAdClicked(mScene);
    }
}
