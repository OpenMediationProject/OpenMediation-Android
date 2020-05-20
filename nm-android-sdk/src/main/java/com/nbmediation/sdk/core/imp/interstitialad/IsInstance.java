// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.core.imp.interstitialad;

import android.app.Activity;

import com.nbmediation.sdk.utils.model.Instance;
import com.nbmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.model.Scene;

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

    void loadIs(Activity activity) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load InterstitialAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mAdapter.loadInterstitialAd(activity, getKey(), this);
            mLoadStart = System.currentTimeMillis();
        }
    }

    void loadIsWithBid(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load InterstitialAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mAdapter.loadInterstitialAd(activity, getKey(), extras, this);
            mLoadStart = System.currentTimeMillis();
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
    public void onInterstitialAdInitFailed(String error) {
        onInsInitFailed(error);
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER
                , ErrorCode.MSG_LOAD_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
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
    public void onInterstitialAdLoadFailed(String error) {
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER
                , ErrorCode.MSG_LOAD_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + "onInterstitialAdLoadFailed : " + toString());
        DeveloperLog.LogE(errorResult.toString() + "onInterstitialAdLoadFailed : " + toString() + " error : " + error);
        onInsLoadFailed(errorResult.toString());
        mListener.onInterstitialAdLoadFailed(errorResult, this);
    }

    @Override
    public void onInterstitialAdShowFailed(String error) {
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + "onInterstitialAdShowFailed : " + toString());
        DeveloperLog.LogE(errorResult.toString() + "onInterstitialAdShowFailed : " + toString() + " error : " + error);
        onInsShowFailed(errorResult.toString(), mScene);
        mListener.onInterstitialAdShowFailed(errorResult, this);
    }

    @Override
    public void onInterstitialAdClick() {
        onInsClick(mScene);
        mListener.onInterstitialAdClick(this);
    }

    @Override
    public void onLoadTimeout() {
        onInterstitialAdLoadFailed(ErrorCode.ERROR_TIMEOUT);
    }
}
