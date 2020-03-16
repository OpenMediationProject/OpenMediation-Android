// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.banner;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.openmediation.sdk.banner.BannerAdListener;
import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.core.AbstractHybridAd;
import com.openmediation.sdk.core.AdManager;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;

/**
 * Actual banner ad imp
 */
public final class BannerImp extends AbstractHybridAd implements View.OnAttachStateChangeListener {
    private BannerAdListener mBannerListener;
    private FrameLayout mLytBanner;
    private RefreshTask mRefreshTask;
    private HandlerUtil.HandlerHolder mRlwHandler;

    public BannerImp(Activity activity, String placementId, BannerAdListener listener) {
        super(activity, placementId);
        mBannerListener = listener;
        mLytBanner = new FrameLayout(activity);
        mLytBanner.setBackgroundColor(Color.TRANSPARENT);
        mRlwHandler = new HandlerUtil.HandlerHolder(null);
    }

    @Override
    public void loadAd(OmManager.LOAD_TYPE type) {
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
        CustomBannerEvent bannerEvent = getAdEvent(instances);
        if (bannerEvent == null) {
            onInsError(instances, ErrorCode.ERROR_CREATE_MEDATION_ADAPTER);
            return;
        }

        instances.setStart(System.currentTimeMillis());
        if (instances.getBidState() == BaseInstance.BID_STATE.BID_SUCCESS) {
            AuctionUtil.instanceNotifyBidWin(mPlacement.getHbAbt(), instances);
            AuctionUtil.removeBidResponse(mBidResponses, instances);
        }
        bannerEvent.loadAd(mActRef.get(), PlacementUtils.getPlacementInfo(mPlacementId, instances,
                AuctionUtil.generateStringRequestData(mBidResponses, instances)));
        iLoadReport(instances);
    }

    @Override
    public void destroy() {
        EventUploadManager.getInstance().uploadEvent(EventId.DESTROY, mCurrentIns != null ?
                PlacementUtils.placementEventParams(mCurrentIns.getPlacementId()) : null);
        if (mCurrentIns != null) {
            destroyAdEvent(mCurrentIns);
            AdManager.getInstance().removeInsAdEvent(mCurrentIns);
        }
        cleanAfterCloseOrFailed();
        if (mRlwHandler != null) {
            mRlwHandler.removeCallbacks(mRefreshTask);
            mRlwHandler = null;
        }
        mLytBanner.removeAllViews();
        mLytBanner = null;
        super.destroy();
    }

    @Override
    protected boolean isInsReady(BaseInstance instances) {
        return instances != null && instances.getObject() != null && instances.getObject() instanceof View;
    }

    @Override
    protected int getAdType() {
        return CommonConstants.BANNER;
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacementId).getPlacementInfo(getAdType());
    }

    @Override
    protected void onAdErrorCallback(String error) {
        startRefreshTask();
        if (mBannerListener != null && isManualTriggered) {
            mBannerListener.onAdFailed(error);
            errorCallbackReport(error);
        }
        isManualTriggered = false;
    }

    @Override
    protected void onAdReadyCallback() {
        if (mBannerListener == null) {
            return;
        }
        if (isRefreshTriggered.get()) {
            isRefreshTriggered.set(false);
        }
        if (mCurrentIns != null) {
            if (mCurrentIns.getObject() instanceof View) {
                View banner = (View) mCurrentIns.getObject();
                banner.removeOnAttachStateChangeListener(this);
                mLytBanner.removeAllViews();
                banner.addOnAttachStateChangeListener(this);
                mLytBanner.addView(banner);
                releaseAdEvent();
//
                mBannerListener.onAdReady(mLytBanner);
                AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_SUCCESS, mPlacementId, null, null);
//                }
            } else {
                if (isManualTriggered) {
                    mBannerListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
                    errorCallbackReport(ErrorCode.ERROR_NO_FILL);
                }
            }
        } else {
            if (isManualTriggered) {
                mBannerListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
                errorCallbackReport(ErrorCode.ERROR_NO_FILL);
            }
        }
        isManualTriggered = false;
    }

    @Override
    protected void onAdClickCallback() {
        if (mBannerListener != null) {
            mBannerListener.onAdClicked();
            AdsUtil.callbackActionReport(EventId.CALLBACK_CLICK, mPlacementId, null, null);
        }
    }

    @Override
    protected void destroyAdEvent(BaseInstance instances) {
        super.destroyAdEvent(instances);
        CustomBannerEvent bannerEvent = getAdEvent(instances);
        if (bannerEvent != null && checkActRef()) {
            bannerEvent.destroy(mActRef.get());
            instances.reportInsDestroyed();
        }
        instances.setObject(null);
    }

    private CustomBannerEvent getAdEvent(BaseInstance instances) {
        return (CustomBannerEvent) AdManager.getInstance().getInsAdEvent(CommonConstants.BANNER, instances);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        startRefreshTask();
        if (mCurrentIns == null) {
            return;
        }
        insImpReport(mCurrentIns);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        v.removeOnAttachStateChangeListener(this);
        if (mCurrentIns != null) {
            mCurrentIns.onInsClosed(null);
        }
    }

    private void startRefreshTask() {
        if (mRefreshTask != null) {
            return;
        }
        if (mPlacement == null || isDestroyed) {
            return;
        }
        int rlw = mPlacement.getRlw();

        if (mRefreshTask == null) {
            mRefreshTask = new RefreshTask(rlw);
        }

        mRlwHandler.postDelayed(mRefreshTask, rlw * 1000);
    }

    private class RefreshTask implements Runnable {

        private int mInterval;

        RefreshTask(int interval) {
            mInterval = interval;
        }

        @Override
        public void run() {
            mRlwHandler.postDelayed(mRefreshTask, mInterval * 1000);
            if (mLoadTs > mCallbackTs) {
                return;
            }
            EventUploadManager.getInstance().uploadEvent(EventId.REFRESH_INTERVAL,
                    PlacementUtils.placementEventParams(mPlacement != null ? mPlacement.getId() : ""));
            isRefreshTriggered.set(true);
            setManualTriggered(false);
            loadAd(OmManager.LOAD_TYPE.INTERVAL);
        }
    }
}
