// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.rewardedvideo;

import android.app.Activity;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Scene;

import java.util.Map;

/**
 *
 */
public class RvInstance extends BaseInstance implements RewardedVideoCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private RvManagerListener mListener;
    private Scene mScene;

    public RvInstance() {

    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    void initRv(Activity activity) {
        setMediationState(MEDIATION_STATE.INIT_PENDING);
        if (mAdapter != null) {
            mAdapter.initRewardedVideo(activity, InsManager.getInitDataMap(this), this);
        }
    }

    void loadRv(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load RewardedVideoAd : " + getMediationId() + " key : " + getKey());
            InsManager.startInsLoadTimer(this, this);
            mAdapter.loadRewardedVideo(activity, getKey(), extras, this);
        }
    }

    void showRv(Activity activity, Scene scene) {
        if (mAdapter != null) {
            mScene = scene;
            InsManager.onInsShow(this, scene);
            mAdapter.showRewardedVideo(activity, getKey(), this);
        }
    }

    boolean isRvAvailable() {
        return getMediationState() == MEDIATION_STATE.AVAILABLE &&
                mAdapter != null && mAdapter.isRewardedVideoAvailable(getKey());
    }

    void setRvManagerListener(RvManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onRewardedVideoInitSuccess() {
        InsManager.onInsInitSuccess(this);
        mListener.onRewardedVideoInitSuccess(this);
    }

    @Override
    public void onRewardedVideoInitFailed(AdapterError error) {
        AdLog.getSingleton().LogE("RewardedVideo Ad Init Failed: " + error.toString());
        InsManager.onInsInitFailed(this, error);
        mListener.onRewardedVideoInitFailed(this, error);
    }

    @Override
    public void onRewardedVideoAdShowSuccess() {
        mListener.onRewardedVideoAdShowSuccess(this);
    }

    @Override
    public void onRewardedVideoAdClosed() {
        mListener.onRewardedVideoAdClosed(this);
        mScene = null;
    }

    @Override
    public void onRewardedVideoLoadSuccess() {
        DeveloperLog.LogD("RvInstance onRewardedVideoLoadSuccess : " + toString());
        mListener.onRewardedVideoLoadSuccess(this);
    }

    @Override
    public void onRewardedVideoLoadFailed(AdapterError error) {
        DeveloperLog.LogE("RvInstance onRewardedVideoLoadFailed : " + toString() + " error : " + error);
        mListener.onRewardedVideoLoadFailed(this, error);
    }

    @Override
    public void onRewardedVideoAdStarted() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_START, InsManager.buildReportDataWithScene(this, mScene));
        mListener.onRewardedVideoAdStarted(this);
    }

    @Override
    public void onRewardedVideoAdEnded() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_COMPLETED, InsManager.buildReportDataWithScene(this, mScene));
        mListener.onRewardedVideoAdEnded(this);
    }

    @Override
    public void onRewardedVideoAdRewarded() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_REWARDED, InsManager.buildReportDataWithScene(this, mScene));
        mListener.onRewardedVideoAdRewarded(this);
    }

    @Override
    public void onRewardedVideoAdShowFailed(AdapterError error) {
        DeveloperLog.LogE(error + ", onRewardedVideoAdShowFailed: " + toString());
        mListener.onRewardedVideoAdShowFailed(this, error);
    }

    @Override
    public void onRewardedVideoAdClicked() {
        mListener.onRewardedVideoAdClicked(this);
    }

    @Override
    public void onLoadTimeout() {
        AdapterError errorResult = AdapterErrorBuilder.buildLoadCheckError(
                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapter == null ? "" : mAdapter.getClass().getSimpleName(), ErrorCode.ERROR_TIMEOUT);
        onRewardedVideoLoadFailed(errorResult);
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
