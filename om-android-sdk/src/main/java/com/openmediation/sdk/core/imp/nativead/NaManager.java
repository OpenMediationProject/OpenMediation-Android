// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.nativead;

import android.view.View;

import com.openmediation.sdk.core.AbstractHybridAds;
import com.openmediation.sdk.core.InsManager;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.NativeAdListener;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorBuilder;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;

import java.util.Map;

public class NaManager extends AbstractHybridAds implements NaManagerListener, View.OnAttachStateChangeListener {

    private NativeAdView mNativeAdView;
    private boolean isImpressed;

    public NaManager(String placementId, NativeAdListener listener) {
        super(placementId);
        mListenerWrapper.setNaListener(listener);
    }

    @Override
    protected void onAdErrorCallback(Error error) {
        mListenerWrapper.onNativeAdLoadFailed(mPlacementId, error);
    }

    @Override
    protected void onAdReadyCallback() {
        if (mCurrentIns != null) {
            Object o = mCurrentIns.getObject();
            if (o instanceof AdInfo) {
                AdInfo adInfo = (AdInfo) o;
                mListenerWrapper.onNativeAdLoaded(mPlacementId, adInfo);
            } else {
                Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD,
                        ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
                mListenerWrapper.onNativeAdLoadFailed(mPlacementId, error);
            }
        } else {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD,
                    ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
            mListenerWrapper.onNativeAdLoadFailed(mPlacementId, error);
        }
    }

//    @Override
//    protected void onAdClickCallback() {
//        mListenerWrapper.onNativeAdClicked(mPlacementId);
//    }

    @Override
    protected boolean isInsAvailable(BaseInstance instance) {
        return (instance instanceof NaInstance) && (((NaInstance) instance).isNaAvailable());
    }

    @Override
    protected void insInit(BaseInstance instance, Map<String, Object> extras) {
        if (instance instanceof NaInstance) {
            NaInstance naInstance = (NaInstance) instance;
            naInstance.setNaManagerListener(this);
            naInstance.initNa(mActRefs.get(), extras);
        }
    }

    @Override
    protected void insLoad(BaseInstance instance, Map<String, Object> extras) {
        if (instance instanceof NaInstance) {
            NaInstance naInstance = (NaInstance) instance;
            naInstance.setNaManagerListener(this);
            naInstance.loadNa(mActRefs.get(), extras);
        }
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacementId).getPlacementInfo(getPlacementType());
    }

    @Override
    public void loadAds(OmManager.LOAD_TYPE type) {
        AdsUtil.callActionReport(mPlacementId, 0, EventId.CALLED_LOAD);
        super.loadAds(type);
    }

    @Override
    public void destroy() {
        if (mNativeAdView != null) {
            mNativeAdView.removeAllViews();
            mNativeAdView = null;
        }
        super.destroy();
    }

    @Override
    protected void destroyAdEvent(BaseInstance instances) {
        NaInstance naInstance = (NaInstance) instances;
        naInstance.destroyNa();
        InsManager.reportInsDestroyed(instances);
    }

    public void registerView(NativeAdView adView) {
        if (isDestroyed) {
            return;
        }
        mNativeAdView = adView;
        if (mCurrentIns != null && mCurrentIns instanceof NaInstance) {
            NaInstance instance = (NaInstance) mCurrentIns;
            mNativeAdView.addOnAttachStateChangeListener(this);
            instance.registerView(adView);
        }
    }

    @Override
    public void onNativeAdInitSuccess(NaInstance instance) {
        loadInsAndSendEvent(instance);
    }

    @Override
    public void onNativeAdInitFailed(NaInstance instance, AdapterError error) {
        onInsLoadFailed(instance, error);
    }

    @Override
    public void onNativeAdLoadSuccess(NaInstance instance) {
        onInsLoadSuccess(instance);
    }

    @Override
    public void onNativeAdLoadFailed(NaInstance instance, AdapterError error) {
        onInsLoadFailed(instance, error);
    }

    @Override
    public void onNativeAdAdClicked(NaInstance instance) {
        onInsClicked(instance, null);
        mListenerWrapper.onNativeAdClicked(mPlacementId);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (isImpressed) {
            return;
        }
        isImpressed = true;
        onViewAttachToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        isImpressed = false;
        v.removeOnAttachStateChangeListener(this);
        onViewDetachFromWindow();
    }
}
