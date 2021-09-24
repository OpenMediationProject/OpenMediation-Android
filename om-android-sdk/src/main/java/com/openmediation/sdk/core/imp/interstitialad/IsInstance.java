// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.interstitialad;

import android.app.Activity;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Scene;

import java.util.Map;


public class IsInstance extends BaseInstance implements InterstitialAdCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private IsManagerListener mListener;

    public IsInstance() {
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    void initIs(Activity activity) {
        setMediationState(MEDIATION_STATE.INIT_PENDING);
        if (mAdapter != null) {
            mAdapter.initInterstitialAd(activity, InsManager.getInitDataMap(this), this);
        }
    }

    void loadIs(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load InterstitialAd : " + getMediationId() + " key : " + getKey());
            InsManager.startInsLoadTimer(this, this);
            mAdapter.loadInterstitialAd(activity, getKey(), extras, this);
        }
    }

    void showIs(Activity activity, Scene scene) {
        if (mAdapter != null) {
            mAdapter.showInterstitialAd(activity, getKey(), this);
            InsManager.onInsShow(this, scene);
        }
    }

    boolean isIsAvailable() {
        return getMediationState() == MEDIATION_STATE.AVAILABLE &&
                mAdapter != null && mAdapter.isInterstitialAdAvailable(getKey());
    }

    void setIsManagerListener(IsManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onInterstitialAdInitSuccess() {
        InsManager.onInsInitSuccess(this);
        mListener.onInterstitialAdInitSuccess(this);
    }

    @Override
    public void onInterstitialAdInitFailed(AdapterError error) {
        AdLog.getSingleton().LogE("Interstitial Ad Init Failed: " + error.toString());
        InsManager.onInsInitFailed(this, error);
        mListener.onInterstitialAdInitFailed(this, error);
    }

    @Override
    public void onInterstitialAdShowSuccess() {
        mListener.onInterstitialAdShowSuccess(this);
    }

    @Override
    public void onInterstitialAdClosed() {
        mListener.onInterstitialAdClosed(this);
    }

    @Override
    public void onInterstitialAdLoadSuccess() {
        DeveloperLog.LogD("onInterstitialAdLoadSuccess : " + toString());
        mListener.onInterstitialAdLoadSuccess(this);
    }

    @Override
    public void onInterstitialAdLoadFailed(AdapterError error) {
        DeveloperLog.LogE(" onInterstitialAdLoadFailed : " + toString() + " error : " + error);
        mListener.onInterstitialAdLoadFailed(this, error);
    }

    @Override
    public void onInterstitialAdShowFailed(AdapterError error) {
        DeveloperLog.LogE("onInterstitialAdShowFailed : " + toString() + " error : " + error);
        mListener.onInterstitialAdShowFailed(this, error);
    }

    @Override
    public void onInterstitialAdClicked() {
        mListener.onInterstitialAdClicked(this);
    }

    @Override
    public void onLoadTimeout() {
        AdapterError errorResult = AdapterErrorBuilder.buildLoadCheckError(
                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapter == null ? "" : mAdapter.getClass().getSimpleName(), ErrorCode.ERROR_TIMEOUT);
        onInterstitialAdLoadFailed(errorResult);
    }

    @Override
    public void onBidSuccess(BidResponse response) {
        mListener.onBidSuccess(this, response);
    }

    @Override
    public void onBidFailed(String error) {
        mListener.onBidFailed(this, error);
    }

    @Override
    public void onAdExpired() {
        mListener.onAdExpired(this);
    }
}
