// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.interstitialad;

import android.app.Activity;

import com.openmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.Scene;

import java.util.Map;


public class IsInstance extends Instance implements InterstitialAdCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private IsManagerListener mListener;
    private Scene mScene;

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
            mAdapter.initInterstitialAd(activity, getInitDataMap(), this);
            onInsInitStart();
        }
    }

    void loadIs(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load InterstitialAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mLoadStart = System.currentTimeMillis();
            mAdapter.loadInterstitialAd(activity, getKey(), extras, this);
        }
    }

    void showIs(Activity activity, Scene scene) {
        if (mAdapter != null) {
            mScene = scene;
            mAdapter.showInterstitialAd(activity, getKey(), this);
            onInsShow(scene);
        }
    }

    boolean isIsAvailable() {
        return mAdapter != null && mAdapter.isInterstitialAdAvailable(getKey())
                && getMediationState() == MEDIATION_STATE.AVAILABLE;
    }

    void setIsManagerListener(IsManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onInterstitialAdInitSuccess() {
        onInsInitSuccess();
        mListener.onInterstitialAdInitSuccess(this);
    }

    @Override
    public void onInterstitialAdInitFailed(AdapterError error) {
        AdLog.getSingleton().LogE("Interstitial Ad Init Failed: " + error.toString());
        onInsInitFailed(error);
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        mListener.onInterstitialAdInitFailed(errorResult, this);
    }

    @Override
    public void onInterstitialAdShowSuccess() {
        onInsShowSuccess(mScene);
        mListener.onInterstitialAdShowSuccess(this);
    }

    @Override
    public void onInterstitialAdClosed() {
        onInsClosed(mScene);
        mListener.onInterstitialAdClosed(this);
        mScene = null;
    }

    @Override
    public void onInterstitialAdLoadSuccess() {
        DeveloperLog.LogD("onInterstitialAdLoadSuccess : " + toString());
        onInsLoadSuccess();
        mListener.onInterstitialAdLoadSuccess(this);
    }

    @Override
    public void onInterstitialAdLoadFailed(AdapterError error) {
        AdLog.getSingleton().LogE("Interstitial Ad Load Failed: " + error.toString());
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        DeveloperLog.LogE(errorResult.toString() + " onInterstitialAdLoadFailed : " + toString() + " error : " + error);
        onInsLoadFailed(error);
        mListener.onInterstitialAdLoadFailed(errorResult, this);
    }

    @Override
    public void onInterstitialAdShowFailed(AdapterError error) {
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE("Interstitial Ad Show Failed: " + error.toString());
        DeveloperLog.LogE(errorResult.toString() + "onInterstitialAdShowFailed : " + toString() + " error : " + error);
        onInsShowFailed(error, mScene);
        mListener.onInterstitialAdShowFailed(errorResult, this);
    }

    @Override
    public void onInterstitialAdClick() {
        onInsClick(mScene);
        mListener.onInterstitialAdClick(this);
    }

    @Override
    public void onLoadTimeout() {
        AdapterError errorResult = AdapterErrorBuilder.buildLoadCheckError(
                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapter == null ? "" : mAdapter.getClass().getSimpleName(), ErrorCode.ERROR_TIMEOUT);
        onInterstitialAdLoadFailed(errorResult);
    }
}
