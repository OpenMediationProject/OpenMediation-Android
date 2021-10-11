// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.nativead;

import android.app.Activity;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.Map;

public class NaInstance extends BaseInstance implements NativeAdCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private NaManagerListener mListener;

    private boolean mInsShowed;

    void initNa(Activity activity) {
        if (mAdapter != null) {
            mAdapter.initNativeAd(activity, InsManager.getInitDataMap(this), this);
        }
    }

    void loadNa(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load NativeAd : " + getMediationId() + " key : " + getKey());
            InsManager.startInsLoadTimer(this, this);
            mAdapter.loadNativeAd(activity, getKey(), extras, this);
        }
    }

    void registerView(NativeAdView adView, AdnAdInfo adInfo) {
        if (mAdapter != null) {
            mAdapter.registerNativeAdView(getKey(), adView, adInfo, this);
        }
    }

    boolean isNaAvailable() {
        return getMediationState() == MEDIATION_STATE.AVAILABLE &&
                mAdapter != null && getObject() instanceof AdnAdInfo;
    }

    void destroyNa(AdnAdInfo adInfo) {
        if (mAdapter != null) {
            mAdapter.destroyNativeAd(getKey(), adInfo);
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

    @Override
    public void onNativeAdLoadSuccess(AdnAdInfo info) {
        setObject(info);
        AdInfo adInfo = new AdInfo();
        adInfo.setView(info.getView());
        adInfo.setTemplateRender(info.isTemplateRender());
        adInfo.setCallToActionText(info.getCallToActionText());
        adInfo.setDesc(info.getDesc());
        adInfo.setStarRating(info.getStarRating());
        adInfo.setTitle(info.getTitle());
        adInfo.setType(info.getType());
        mListener.onNativeAdLoadSuccess(this, adInfo);
    }

    @Override
    public void onNativeAdLoadFailed(AdapterError error) {
        mListener.onNativeAdLoadFailed(this, error);
    }

    @Override
    public void onNativeAdImpression() {
        if (!mInsShowed) {
            mInsShowed = true;
            mListener.onNativeAdImpression(this);
        }
    }

    @Override
    public void onNativeAdAdClicked() {
        mListener.onNativeAdAdClicked(this);
    }

    @Override
    public void onLoadTimeout() {
        AdapterError errorResult = AdapterErrorBuilder.buildLoadCheckError(
                AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapter == null ? "" : mAdapter.getClass().getSimpleName(), ErrorCode.ERROR_TIMEOUT);
        onNativeAdLoadFailed(errorResult);
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
