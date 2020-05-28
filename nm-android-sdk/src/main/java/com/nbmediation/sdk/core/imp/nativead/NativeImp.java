// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.core.imp.nativead;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.nbmediation.sdk.bid.AuctionUtil;
import com.nbmediation.sdk.core.AbstractHybridAd;
import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdListener;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdsUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.PlacementInfo;
import com.nbmediation.sdk.core.AdManager;

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
    public void loadAd(NmManager.LOAD_TYPE type) {
        AdsUtil.callActionReport(mPlacementId, 0, EventId.CALLED_LOAD);
        super.loadAd(type);
    }

    @Override
    protected void loadInsOnUIThread(BaseInstance instances) throws Throwable {
        instances.reportInsLoad();
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
        Map<String, String> config = PlacementUtils.getPlacementInfo(mPlacementId, instances,
                AuctionUtil.generateStringRequestData(mBidResponses, instances));
        instances.setStart(System.currentTimeMillis());
        if (instances.getBidState() == BaseInstance.BID_STATE.BID_SUCCESS) {
            AuctionUtil.instanceNotifyBidWin(mPlacement.getHbAbt(), instances);
            AuctionUtil.removeBidResponse(mBidResponses, instances);
        }
        nativeEvent.loadAd(mActRef.get(), config);
        iLoadReport(instances);
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
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        isImpressed = false;
        v.removeOnAttachStateChangeListener(this);
        if (mCurrentIns != null) {
            mCurrentIns.onInsClosed(null);
        }
    }
}
