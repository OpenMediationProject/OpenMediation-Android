// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.splash;

import android.app.Activity;
import android.view.ViewGroup;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.Map;

public class SpInstance extends BaseInstance implements SplashAdCallback {

    private static final String TAG = "SplashInstance: ";
    private SpManagerListener mListener;

    public SpInstance() {
    }

    void setSplashManagerListener(SpManagerListener listener) {
        mListener = listener;
    }

    void initSplashAd(Activity activity) {
        if (mAdapter != null) {
            mAdapter.initSplashAd(activity, InsManager.getInitDataMap(this), this);
        }
    }

    void loadSplashAd(Activity activity, Map<String, Object> extras) {
        if (mAdapter == null) {
            DeveloperLog.LogE("Load SplashAd Error: " + getMediationId() + " key : " + getKey());
            return;
        }
        DeveloperLog.LogD(TAG, "Load SplashAd : " + getMediationId() + " key : " + getKey());
        mAdapter.loadSplashAd(activity, getKey(), extras, this);
    }

    void showSplashAd(Activity activity, ViewGroup container) {
        if (mAdapter != null) {
            mAdapter.showSplashAd(activity, getKey(), container, this);
            InsManager.onInsShow(this, null);
        }
    }

    boolean isSplashAdAvailable() {
        return getMediationState() == MEDIATION_STATE.AVAILABLE &&
                mAdapter != null && mAdapter.isSplashAdAvailable(getKey());
    }

    void destroySplashAd() {
        if (mAdapter != null) {
            mAdapter.destroySplashAd(getKey());
        }
        setObject(null);
    }

    @Override
    public void onSplashAdInitSuccess() {
        InsManager.onInsInitSuccess(this);
        mListener.onSplashAdInitSuccess(this);
        AdLog.getSingleton().LogD(TAG + "Splash Ad Init Success");
    }

    @Override
    public void onSplashAdInitFailed(AdapterError error) {
        AdLog.getSingleton().LogE(TAG + "Splash Ad Init Failed: " + error.toString());
        InsManager.onInsInitFailed(this, error);
        mListener.onSplashAdInitFailed(this, error);
    }

    @Override
    public void onSplashAdLoadSuccess(Object view) {
        setObject(view);
        mListener.onSplashAdLoadSuccess(this);
    }

    @Override
    public void onSplashAdLoadFailed(AdapterError error) {
        DeveloperLog.LogE(TAG + "Splash Ad Load Failed: " + error.toString() + ", " + this);
        mListener.onSplashAdLoadFailed(this, error);
    }

    @Override
    public void onSplashAdShowSuccess() {
        mListener.onSplashAdShowSuccess(this);
    }

    @Override
    public void onSplashAdShowFailed(AdapterError error) {
        DeveloperLog.LogE(TAG + "SplashAd ShowFailed: " + error);
        mListener.onSplashAdShowFailed(this, error);
    }

    @Override
    public void onSplashAdTick(long millisUntilFinished) {
        DeveloperLog.LogD(TAG + "Splash Ad Show Tick: " + millisUntilFinished);
        mListener.onSplashAdTick(this, millisUntilFinished);
    }

    @Override
    public void onSplashAdAdClicked() {
        mListener.onSplashAdAdClicked(this);
    }

    @Override
    public void onSplashAdDismissed() {
        mListener.onSplashAdDismissed(this);
    }

    @Override
    public void onBidSuccess(BidResponse response) {
        mListener.onBidSuccess(this, response);
    }

    @Override
    public void onBidFailed(String error) {
        mListener.onBidFailed(this, error);
    }
}
