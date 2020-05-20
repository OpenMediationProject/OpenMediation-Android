// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.core.imp.rewardedvideo;

import android.app.Activity;

import com.nbmediation.sdk.utils.model.Instance;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.nbmediation.sdk.utils.model.Scene;

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

    void loadRv(Activity activity) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load RewardedVideoAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mAdapter.loadRewardedVideo(activity, getKey(), this);
            mLoadStart = System.currentTimeMillis();
        }
    }

    void loadRvWithBid(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load RewardedVideoAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mAdapter.loadRewardedVideo(activity, getKey(), extras, this);
            mLoadStart = System.currentTimeMillis();
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
    public void onRewardedVideoInitFailed(String error) {
        onInsInitFailed(error);
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER
                , ErrorCode.MSG_LOAD_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
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
    public void onRewardedVideoLoadFailed(String error) {
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER
                , ErrorCode.MSG_LOAD_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + ", onRewardedVideoLoadFailed :" + toString());
        DeveloperLog.LogD("RvInstance onRewardedVideoLoadFailed : " + toString() + " error : " + errorResult);
        onInsLoadFailed(errorResult.toString());
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
    public void onRewardedVideoAdShowFailed(String error) {
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationId:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + ", onRewardedVideoAdShowFailed: " + toString());
        DeveloperLog.LogE(errorResult.toString() + ", onRewardedVideoAdShowFailed: " + toString());
        onInsShowFailed(errorResult.toString(), mScene);
        mListener.onRewardedVideoAdShowFailed(errorResult, this);
    }

    @Override
    public void onRewardedVideoAdClicked() {
        onInsClick(mScene);
        mListener.onRewardedVideoAdClicked(this);
    }

    @Override
    public void onLoadTimeout() {
        DeveloperLog.LogD("rvInstance onLoadTimeout : " + toString());
        onRewardedVideoLoadFailed(ErrorCode.ERROR_TIMEOUT);
    }
}
