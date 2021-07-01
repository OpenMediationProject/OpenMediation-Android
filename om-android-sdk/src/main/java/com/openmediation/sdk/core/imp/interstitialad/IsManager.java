// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.interstitialad;

import com.openmediation.sdk.core.AbstractInventoryAds;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.MediationInterstitialListener;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;

import java.util.Map;

public final class IsManager extends AbstractInventoryAds implements IsManagerListener {

    public IsManager() {
        super();
    }

    public void initInterstitialAd() {
        checkScheduleTaskStarted();
    }

    public void loadInterstitialAd() {
        loadAds(OmManager.LOAD_TYPE.MANUAL);
    }

    public void showInterstitialAd(String scene) {
        showAd(scene);
    }

    public boolean isInterstitialAdReady() {
        return isPlacementAvailable();
    }

    @Deprecated
    public void setInterstitialAdListener(InterstitialAdListener listener) {
        mListenerWrapper.addInterstitialListener(listener);
    }

    public void addInterstitialAdListener(InterstitialAdListener listener) {
        mListenerWrapper.addInterstitialListener(listener);
    }

    public void removeInterstitialAdListener(InterstitialAdListener listener) {
        mListenerWrapper.removeInterstitialListener(listener);
    }

    public void setMediationInterstitialAdListener(MediationInterstitialListener listener) {
        mListenerWrapper.setMediationInterstitialListener(listener);
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacement.getId()).getPlacementInfo(mPlacement.getT());
    }

    @Override
    protected void initInsAndSendEvent(BaseInstance instance) {
        super.initInsAndSendEvent(instance);
        if (!(instance instanceof IsInstance)) {
            instance.setMediationState(BaseInstance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance, new Error(ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR,
                    "current is not an rewardedVideo adUnit", -1));
            return;
        }
        IsInstance isInstance = (IsInstance) instance;
        isInstance.setIsManagerListener(this);
        isInstance.initIs(mActRefs.get());
    }

    @Override
    protected boolean isInsAvailable(BaseInstance instance) {
        if (instance instanceof IsInstance) {
            return ((IsInstance) instance).isIsAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(final BaseInstance instance) {
        ((IsInstance) instance).showIs(mActRefs.get(), mScene);
    }

    @Override
    protected void insLoad(BaseInstance instance, Map<String, Object> extras) {
        IsInstance isInstance = (IsInstance) instance;
        isInstance.loadIs(mActRefs.get(), extras);
    }

    @Override
    protected void onAvailabilityChanged(boolean available, Error error) {
        mListenerWrapper.onInterstitialAdAvailabilityChanged(available);
    }

    @Override
    protected void callbackAvailableOnManual() {
        super.callbackAvailableOnManual();
        mListenerWrapper.onInterstitialAdAvailabilityChanged(true);
        mListenerWrapper.onInterstitialAdLoadSuccess();
    }

    @Override
    protected void callbackLoadSuccessOnManual() {
        super.callbackLoadSuccessOnManual();
        mListenerWrapper.onInterstitialAdLoadSuccess();
    }

    @Override
    protected void callbackLoadFailedOnManual(Error error) {
        super.callbackLoadFailedOnManual(error);
        mListenerWrapper.onInterstitialAdLoadFailed(error);
    }

    @Override
    protected void callbackLoadError(Error error) {
        mListenerWrapper.onInterstitialAdLoadFailed(error);
        boolean hasCache = hasAvailableInventory();
        if (shouldNotifyAvailableChanged(hasCache)) {
            mListenerWrapper.onInterstitialAdAvailabilityChanged(hasCache);
        }
        super.callbackLoadError(error);
    }

    @Override
    protected void callbackShowError(Error error) {
        super.callbackShowError(error);
        mListenerWrapper.onInterstitialAdShowFailed(mScene, error);
    }

    @Override
    protected void callbackAdClosed() {
        mListenerWrapper.onInterstitialAdClosed(mScene);
    }

    @Override
    public void onInterstitialAdInitSuccess(IsInstance isInstance) {
        loadInsAndSendEvent(isInstance);
    }

    @Override
    public void onInterstitialAdInitFailed(IsInstance isInstance, AdapterError error) {
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        onInsInitFailed(isInstance, errorResult);
    }

    @Override
    public void onInterstitialAdShowFailed(IsInstance isInstance, AdapterError error) {
        isInShowingProgress = false;
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationID:" + isInstance.getMediationId() + ", error:" + error, -1);
        onInsShowFailed(isInstance, error, mScene);
        mListenerWrapper.onInterstitialAdShowFailed(mScene, errorResult);
//        TestUtil.getInstance().notifyInsFailed(isInstance.getPlacementId(), isInstance);
    }

    @Override
    public void onInterstitialAdShowSuccess(IsInstance isInstance) {
        onInsShowSuccess(isInstance, mScene);
        mListenerWrapper.onInterstitialAdShowed(mScene);
    }

    @Override
    public void onInterstitialAdClicked(IsInstance isInstance) {
        onInsClicked(isInstance, mScene);
        mListenerWrapper.onInterstitialAdClicked(mScene);
    }

    @Override
    public void onInterstitialAdClosed(IsInstance isInstance) {
        onInsClosed(isInstance, mScene);
    }

    @Override
    public void onInterstitialAdLoadSuccess(IsInstance isInstance) {
        onInsLoadSuccess(isInstance);
    }

    @Override
    public void onInterstitialAdLoadFailed(IsInstance isInstance, AdapterError error) {
        onInsLoadFailed(isInstance, error);
    }
}
