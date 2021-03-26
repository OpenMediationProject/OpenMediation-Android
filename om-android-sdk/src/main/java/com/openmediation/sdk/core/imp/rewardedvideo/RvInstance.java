// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.rewardedvideo;

import android.app.Activity;

import com.openmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.Scene;

import java.util.Map;

/**
 *
 */
public class RvInstance extends Instance implements RewardedVideoCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

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
            mAdapter.initRewardedVideo(activity, getInitDataMap(), this);
            onInsInitStart();
        }
    }

    void loadRv(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load RewardedVideoAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mLoadStart = System.currentTimeMillis();
            mAdapter.loadRewardedVideo(activity, getKey(), extras, this);
        }
    }

    void showRv(Activity activity, Scene scene) {
        if (mAdapter != null) {
            mScene = scene;
            mAdapter.showRewardedVideo(activity, getKey(), this);
            onInsShow(scene);
        }
    }

    boolean isRvAvailable() {
        return mAdapter != null && mAdapter.isRewardedVideoAvailable(getKey())
                && getMediationState() == MEDIATION_STATE.AVAILABLE;
    }

    void setRvManagerListener(RvManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onRewardedVideoInitSuccess() {
        onInsInitSuccess();
        mListener.onRewardedVideoInitSuccess(this);
    }

    @Override
    public void onRewardedVideoInitFailed(AdapterError error) {
        AdLog.getSingleton().LogE("RewardedVideo Ad Init Failed: " + error.toString());
        onInsInitFailed(error);
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        mListener.onRewardedVideoInitFailed(errorResult, this);
    }

    @Override
    public void onRewardedVideoAdShowSuccess() {
        onInsShowSuccess(mScene);
        mListener.onRewardedVideoAdShowSuccess(this);
    }

    @Override
    public void onRewardedVideoAdClosed() {
        onInsClosed(mScene);
        mListener.onRewardedVideoAdClosed(this);
        mScene = null;
    }

    @Override
    public void onRewardedVideoLoadSuccess() {
        DeveloperLog.LogD("RvInstance onRewardedVideoLoadSuccess : " + toString());
        onInsLoadSuccess();
        mListener.onRewardedVideoLoadSuccess(this);
    }

    @Override
    public void onRewardedVideoLoadFailed(AdapterError error) {
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        AdLog.getSingleton().LogE("RewardedVideo Ad Load Failed: " + error.toString());
        DeveloperLog.LogD("RvInstance onRewardedVideoLoadFailed : " + toString() + " error : " + errorResult);
        onInsLoadFailed(error);
        mListener.onRewardedVideoLoadFailed(errorResult, this);
    }

    @Override
    public void onRewardedVideoAdStarted() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_START, buildReportDataWithScene(mScene));
        mListener.onRewardedVideoAdStarted(this);
    }

    @Override
    public void onRewardedVideoAdEnded() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_COMPLETED, buildReportDataWithScene(mScene));
        mListener.onRewardedVideoAdEnded(this);
    }

    @Override
    public void onRewardedVideoAdRewarded() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_REWARDED, buildReportDataWithScene(mScene));
        mListener.onRewardedVideoAdRewarded(this);
    }

    @Override
    public void onRewardedVideoAdShowFailed(AdapterError error) {
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER, error.toString(), -1);
        AdLog.getSingleton().LogE("RewardedVideo Ad Show Failed: " + error.toString());
        DeveloperLog.LogE(errorResult.toString() + ", onRewardedVideoAdShowFailed: " + toString());
        onInsShowFailed(error, mScene);
        mListener.onRewardedVideoAdShowFailed(errorResult, this);
    }

    @Override
    public void onRewardedVideoAdClicked() {
        onInsClick(mScene);
        mListener.onRewardedVideoAdClicked(this);
    }

    @Override
    public void onLoadTimeout() {
        AdapterError errorResult = AdapterErrorBuilder.buildLoadCheckError(
                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapter == null ? "" : mAdapter.getClass().getSimpleName(), ErrorCode.ERROR_TIMEOUT);
        onRewardedVideoLoadFailed(errorResult);
    }
}
