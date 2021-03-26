// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.promotion;

import android.app.Activity;

import com.openmediation.sdk.core.runnable.LoadTimeoutRunnable;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.PromotionAdCallback;
import com.openmediation.sdk.promotion.PromotionAdRect;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.model.Instance;
import com.openmediation.sdk.utils.model.Scene;

import java.util.Map;

/**
 * CpInstance
 */
public class CpInstance extends Instance implements PromotionAdCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private CpManagerListener mListener;
    private Scene mScene;

    public CpInstance() {
    }

    void initCp(Activity activity) {
        setMediationState(MEDIATION_STATE.INIT_PENDING);
        if (mAdapter != null) {
            mAdapter.initPromotionAd(activity, getInitDataMap(), this);
            onInsInitStart();
        }
    }

    void loadCp(Activity activity, Map<String, Object> extras) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load PromotionAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mLoadStart = System.currentTimeMillis();
            mAdapter.loadPromotionAd(activity, getKey(), extras, this);
        }
    }

    void showCp(Activity activity, PromotionAdRect rect, Scene scene) {
        if (mAdapter != null) {
            mScene = scene;
            mAdapter.showPromotionAd(activity, getKey(), PromotionAdRect.getExtraData(rect), this);
            onInsShow(scene);
        }
    }

    void hideCp() {
        if (mAdapter != null) {
            mAdapter.hidePromotionAd(getKey(), this);
        }
    }

    boolean isCpAvailable() {
        return mAdapter != null && mAdapter.isPromotionAdAvailable(getKey())
                && getMediationState() == MEDIATION_STATE.AVAILABLE;
    }

    void setCpManagerListener(CpManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onPromotionAdInitSuccess() {
        onInsInitSuccess();
        mListener.onPromotionAdInitSuccess(this);
    }

    @Override
    public void onPromotionAdInitFailed(AdapterError error) {
        AdLog.getSingleton().LogE("Promotion Ad Init Failed: " + error.toString());
        onInsInitFailed(error);
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        mListener.onPromotionAdInitFailed(errorResult, this);
    }

    @Override
    public void onPromotionAdShowSuccess() {
        onInsShowSuccess(mScene);
        mListener.onPromotionAdShowSuccess(this);
    }

    @Override
    public void onPromotionAdLoadSuccess() {
        DeveloperLog.LogD("onPromotionAdLoadSuccess : " + toString());
        onInsLoadSuccess();
        mListener.onPromotionAdLoadSuccess(this);
    }

    @Override
    public void onPromotionAdLoadFailed(AdapterError error) {
        AdLog.getSingleton().LogE("Promotion Ad Load Failed: " + error.toString());
        onInsLoadFailed(error);
        DeveloperLog.LogE("PromotionAdLoadFailed: " + error);
        Error errorResult = new Error(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER, error.toString(), -1);
        mListener.onPromotionAdLoadFailed(errorResult, this);
    }

    @Override
    public void onPromotionAdShowFailed(AdapterError error) {
        AdLog.getSingleton().LogE("Promotion Ad Show Failed: " + error.toString());
        DeveloperLog.LogE("PromotionAdShowFailed: " + error);
        onInsShowFailed(error, mScene);
        Error errorResult = new Error(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER, error.toString(), -1);
        mListener.onPromotionAdShowFailed(errorResult, this);
    }

    @Override
    public void onPromotionAdVisible() {
        mListener.onPromotionAdVisible(this);
    }

    @Override
    public void onPromotionAdClicked() {
        onInsClick(mScene);
        mListener.onPromotionAdClicked(this);
    }

    @Override
    public void onPromotionAdHidden() {
        onInsClosed(mScene);
        mListener.onPromotionAdHidden(this);
        mScene = null;
    }

    @Override
    public void onLoadTimeout() {
        onPromotionAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapter == null ? "" : mAdapter.getClass().getSimpleName(), ErrorCode.ERROR_TIMEOUT));
    }
}
