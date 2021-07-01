// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.nativead;

import android.app.Activity;

import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.Map;

public class NaInstance extends BaseInstance implements NativeAdCallback {

    private NaManagerListener mListener;

    void initNa(Activity activity, Map<String, Object> extras) {
        if (mAdapter != null) {
            mAdapter.initNativeAd(activity, extras, this);
            InsManager.onInsInitStart(this);
        }
    }

    void loadNa(Activity activity, Map<String, Object> extras) {
        if (mAdapter != null) {
            DeveloperLog.LogD("load NativeAd : " + getMediationId() + " key : " + getKey());
            mLoadStart = System.currentTimeMillis();
            mAdapter.loadNativeAd(activity, getKey(), extras, this);
        }
    }

    void registerView(NativeAdView adView) {
        if (mAdapter != null) {
            mAdapter.registerNativeAdView(getKey(), adView, this);
        }
    }

    boolean isNaAvailable() {
        return mAdapter != null && getObject() instanceof AdInfo;
    }

    void destroyNa() {
        if (mAdapter != null) {
            mAdapter.destroyNativeAd(getKey());
        }
        setObject(null);
    }

    void setNaManagerListener(NaManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onNativeAdInitSuccess() {
        InsManager.onInsInitSuccess(this);
        mListener.onNativeAdInitSuccess(this);
    }

    @Override
    public void onNativeAdInitFailed(AdapterError error) {
        InsManager.onInsInitFailed(this, error);
        mListener.onNativeAdInitFailed(this, error);
    }

    public void onNativeAdLoadSuccess(AdInfo info) {
        setObject(info);
        mListener.onNativeAdLoadSuccess(this);
    }

    @Override
    public void onNativeAdLoadFailed(AdapterError error) {
        mListener.onNativeAdLoadFailed(this, error);
    }

    @Override
    public void onNativeAdAdClicked() {
        mListener.onNativeAdAdClicked(this);
    }
}
