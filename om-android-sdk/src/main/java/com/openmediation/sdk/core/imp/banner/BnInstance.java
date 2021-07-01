// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.banner;

import android.app.Activity;
import android.view.View;

import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.model.BaseInstance;

import java.util.Map;

public class BnInstance extends BaseInstance implements BannerAdCallback {

    private BnManagerListener mListener;

    void initBn(Activity activity, Map<String, Object> extras) {
        if (mAdapter != null) {
            mAdapter.initBannerAd(activity, extras, this);
            InsManager.onInsInitStart(this);
        }
    }

    void loadBn(Activity activity, Map<String, Object> extras) {
        if (mAdapter != null) {
            DeveloperLog.LogD("load BannerAd : " + getMediationId() + " key : " + getKey());
            mLoadStart = System.currentTimeMillis();
            mAdapter.loadBannerAd(activity, getKey(), extras, this);
        }
    }

    boolean isBnAvailable() {
        return getObject() instanceof View && mAdapter != null && mAdapter.isBannerAdAvailable(getKey());
    }

    void destroyBn() {
        if (mAdapter != null) {
            mAdapter.destroyBannerAd(getKey());
        }
        setObject(null);
    }

    void setBnManagerListener(BnManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onBannerAdInitSuccess() {
        InsManager.onInsInitSuccess(this);
        mListener.onBannerAdInitSuccess(this);
    }

    @Override
    public void onBannerAdInitFailed(AdapterError error) {
        InsManager.onInsInitFailed(this, error);
        mListener.onBannerAdInitFailed(this, error);
    }

    @Override
    public void onBannerAdLoadSuccess(View view) {
        setObject(view);
        mListener.onBannerAdLoadSuccess(this);
    }

    @Override
    public void onBannerAdLoadFailed(AdapterError error) {
        mListener.onBannerAdLoadFailed(this, error);
    }

    @Override
    public void onBannerAdImpression() {

    }

    @Override
    public void onBannerAdAdClicked() {
        mListener.onBannerAdAdClicked(this);
    }

}
