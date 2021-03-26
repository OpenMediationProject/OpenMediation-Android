// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.nativead;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.core.AbstractHybridAd;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.mediation.CustomNativeEvent;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.NativeAdListener;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.core.AdManager;

import java.util.Map;

/**
 *
 */
public final class NativeImp extends AbstractHybridAd implements View.OnAttachStateChangeListener {
    private NativeAdListener mNativeListener;
    private NativeAdView mNativeAdView;
    private boolean isImpressed;

    public NativeImp(Activity activity, String placementId, NativeAdListener listener) {
        super(activity, placementId);
        mNativeListener = listener;
    }

    @Override
    public void loadAd(OmManager.LOAD_TYPE type) {
        AdsUtil.callActionReport(mPlacementId, 0, EventId.CALLED_LOAD);
        setManualTriggered(true);
        super.loadAd(type);
    }

    @Override
    protected void loadInsOnUIThread(BaseInstance instances) throws Throwable {
        if (instances.getHb() == 1) {
            instances.reportInsLoad(EventId.INSTANCE_PAYLOAD_REQUEST);
        } else {
            instances.reportInsLoad(EventId.INSTANCE_LOAD);
            iLoadReport(instances);
        }
        if (!checkActRef()) {
            onInsError(instances, ErrorCode.ERROR_ACTIVITY);
            return;
        }
        if (TextUtils.isEmpty(instances.getKey())) {
            onInsError(instances, ErrorCode.ERROR_EMPTY_INSTANCE_KEY);
            return;
        }
        CustomNativeEvent nativeEvent = getAdEvent(instances);
        if (nativeEvent == null) {
            onInsError(instances, ErrorCode.ERROR_CREATE_MEDATION_ADAPTER);
            return;
        }
        String payload = "";
        if (mBidResponses != null && mBidResponses.containsKey(instances.getId())) {
            payload = AuctionUtil.generateStringRequestData(mBidResponses.get(instances.getId()));
        }
        Map<String, String> placementInfo = PlacementUtils.getPlacementInfo(mReqId, mPlacementId, instances, payload);
        instances.setStart(System.currentTimeMillis());
        nativeEvent.loadAd(mActRef.get(), placementInfo);
    }

    @Override
    public void destroy() {
        EventUploadManager.getInstance().uploadEvent(EventId.DESTROY, mCurrentIns != null ?
                PlacementUtils.placementEventParams(mCurrentIns.getPlacementId()) : null);

        if (mNativeAdView != null) {
            mNativeAdView.removeAllViews();
            mNativeAdView = null;
        }
        if (mCurrentIns != null) {
            CustomNativeEvent nativeEvent = getAdEvent(mCurrentIns);
            if (nativeEvent != null) {
                nativeEvent.destroy(mActRef.get());
                mCurrentIns.reportInsDestroyed();
            }
            AdManager.getInstance().removeInsAdEvent(mCurrentIns);
        }
        cleanAfterCloseOrFailed();
        super.destroy();
    }

    @Override
    protected boolean isInsReady(BaseInstance instances) {
        return instances != null && instances.getObject() != null && instances.getObject() instanceof AdInfo;
    }

    @Override
    protected int getAdType() {
        return CommonConstants.NATIVE;
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacementId).getPlacementInfo(getAdType());
    }

    @Override
    protected void onAdErrorCallback(String error) {
        if (mNativeListener != null) {
            mNativeListener.onAdFailed(error);
            errorCallbackReport(error);
        }
    }

    @Override
    protected void onAdReadyCallback() {
        if (mNativeListener == null) {
            return;
        }
        if (mCurrentIns != null) {
            Object o = mCurrentIns.getObject();
            if (o instanceof AdInfo) {
                AdInfo adInfo = (AdInfo) o;
                mNativeListener.onAdReady(adInfo);
                AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_SUCCESS, mPlacementId, null, null);
            } else {
                mNativeListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
                errorCallbackReport(ErrorCode.ERROR_NO_FILL);
            }
        } else {
            mNativeListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
            errorCallbackReport(ErrorCode.ERROR_NO_FILL);
        }
    }

    @Override
    protected void onAdClickCallback() {
        if (mNativeListener != null) {
            mNativeListener.onAdClicked();
            AdsUtil.callbackActionReport(EventId.CALLBACK_CLICK, mPlacementId, null, null);
        }
    }

    public void registerView(NativeAdView adView) {
        if (isDestroyed) {
            return;
        }
        mNativeAdView = adView;
        if (mCurrentIns != null) {
            CustomNativeEvent nativeEvent = getAdEvent(mCurrentIns);
            if (nativeEvent != null) {
                mNativeAdView.addOnAttachStateChangeListener(this);
                nativeEvent.registerNativeView(adView);
            }
        }
    }

    private CustomNativeEvent getAdEvent(BaseInstance instances) {
        return (CustomNativeEvent) AdManager.getInstance().getInsAdEvent(CommonConstants.NATIVE, instances);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (isImpressed || mCurrentIns == null) {
            return;
        }

        isImpressed = true;
        insImpReport(mCurrentIns);
        onInsShowSuccess(mCurrentIns);
        notifyInsBidWin(mCurrentIns);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        isImpressed = false;
        v.removeOnAttachStateChangeListener(this);
        if (mCurrentIns != null) {
            if (mBidResponses != null) {
                mBidResponses.remove(mCurrentIns.getId());
            }
            mCurrentIns.onInsClosed(null);
        }
    }
}
