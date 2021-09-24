// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.splash;

import android.view.ViewGroup;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.core.imp.HybridCacheManager;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.splash.SplashAdListener;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;

import java.util.HashMap;
import java.util.Map;

public class SpManager extends HybridCacheManager implements SpManagerListener {

    private long mLoadTimeout;

    private int mWidth, mHeight;

    void setLoadTimeout(long timeout) {
        mLoadTimeout = timeout;
    }

    public SpManager(String placementId) {
        super(placementId);
    }

    void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public void setAdListener(SplashAdListener listener) {
        mListenerWrapper.setSplashAdListener(listener);
    }

    public void showSplashAd(ViewGroup container) {
        if (null != mCurrentIns && mCurrentIns instanceof SpInstance) {
            ((SpInstance) mCurrentIns).showSplashAd(mActRefs.get(), container);
        }
    }

    public void showSplashAd() {
        this.showSplashAd(null);
    }

    public boolean isReady() {
        return isInsAvailable(mCurrentIns);
    }

    @Override
    public void loadAds(OmManager.LOAD_TYPE type) {
        super.loadAds(type);
    }

    @Override
    protected void initInsAndSendEvent(BaseInstance instance) {
        super.initInsAndSendEvent(instance);
        if (instance instanceof SpInstance) {
            SpInstance splashInstance = (SpInstance) instance;
            splashInstance.setSplashManagerListener(this);
            splashInstance.initSplashAd(mActRefs.get());
        }
    }

    @Override
    protected void insLoad(BaseInstance instance, Map<String, Object> extras) {
        super.initInsAndSendEvent(instance);
        if (instance instanceof SpInstance) {
            SpInstance splashInstance = (SpInstance) instance;
            splashInstance.setSplashManagerListener(this);
            if (extras == null) {
                extras = new HashMap<>();
            }
            extras.put("Timeout", String.valueOf(mLoadTimeout));
            extras.put("Width", String.valueOf(mWidth));
            extras.put("Height", String.valueOf(mHeight));
            splashInstance.loadSplashAd(mActRefs.get(), extras);
        }
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacement.getId()).getPlacementInfo(mPlacement.getT());
    }

    @Override
    protected boolean isInsAvailable(BaseInstance instance) {
        if (instance instanceof SpInstance) {
            return ((SpInstance) instance).isSplashAdAvailable();
        }
        return false;
    }

    @Override
    public void onSplashAdInitSuccess(SpInstance instance) {
        loadInsAndSendEvent(instance);
    }

    @Override
    public void onSplashAdInitFailed(SpInstance instance, AdapterError error) {
        onInsLoadFailed(instance, error, !isManualTriggered);
    }

    @Override
    public void onSplashAdLoadSuccess(SpInstance instance) {
        onInsLoadSuccess(instance, !isManualTriggered);
    }

    @Override
    public void onSplashAdLoadFailed(SpInstance instance, AdapterError error) {
        onInsLoadFailed(instance, error, !isManualTriggered);
    }

    @Override
    public void onSplashAdShowSuccess(SpInstance instance) {
        onInsShowSuccess(instance, null);
    }

    @Override
    public void onSplashAdShowFailed(SpInstance instance, AdapterError error) {
        onInsShowFailed(instance, error, null);
    }

    @Override
    public void onSplashAdTick(SpInstance instance, long millisUntilFinished) {
        mListenerWrapper.onSplashAdTick(mPlacementId, millisUntilFinished);
    }

    @Override
    public void onSplashAdAdClicked(SpInstance instance) {
        onInsClicked(instance, null);
        mListenerWrapper.onSplashAdClicked(mPlacementId);
    }

    @Override
    public void onSplashAdDismissed(SpInstance instance) {
        onInsClosed(instance, null);
        mListenerWrapper.onSplashAdDismissed(mPlacementId);
    }

    @Override
    protected void destroyAdEvent(BaseInstance instance) {
        if (instance instanceof SpInstance) {
            SpInstance splashInstance = (SpInstance) instance;
            splashInstance.destroySplashAd();
            InsManager.reportInsDestroyed(splashInstance);
        }
    }

    @Override
    protected void onAdErrorCallback(Error error) {
        mListenerWrapper.onSplashAdFailed(mPlacementId, error);
    }

    @Override
    protected void onAdReadyCallback() {
        mListenerWrapper.onSplashAdLoad(mPlacementId);
    }

    @Override
    protected void onAdShowedCallback() {
        super.onAdShowedCallback();
        mListenerWrapper.onSplashAdShowed(mPlacementId);
    }

    @Override
    protected void onAdShowFailedCallback(Error error) {
        super.onAdShowFailedCallback(error);
        mListenerWrapper.onSplashAdShowFailed(mPlacementId, error);
    }

    @Override
    public void onBidSuccess(BaseInstance instance, BidResponse response) {
        onInsC2SBidSuccess(instance, response);
    }

    @Override
    public void onBidFailed(BaseInstance instance, String error) {
        onInsC2SBidFailed(instance, error);
    }
}
