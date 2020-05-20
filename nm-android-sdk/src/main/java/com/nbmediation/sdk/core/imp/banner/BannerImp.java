// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.core.imp.banner;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nbmediation.sdk.banner.AdSize;
import com.nbmediation.sdk.banner.BannerAdListener;
import com.nbmediation.sdk.bid.AuctionUtil;
import com.nbmediation.sdk.core.AbstractHybridAd;
import com.nbmediation.sdk.core.AdManager;
import com.nbmediation.sdk.core.OmManager;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.utils.AdsUtil;
import com.nbmediation.sdk.utils.HandlerUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.crash.CrashUtil;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.PlacementInfo;

import java.util.Map;

/**
 * Actual banner ad imp
 */
public final class BannerImp extends AbstractHybridAd implements View.OnAttachStateChangeListener {
    private BannerAdListener mBannerListener;
    private FrameLayout mLytBanner;
    private RefreshTask mRefreshTask;
    private HandlerUtil.HandlerHolder mRlwHandler;
    private AdSize mAdSize;
    private Activity mActivity;

    public BannerImp(Activity activity, String placementId, BannerAdListener listener) {
        super(activity, placementId);
        mBannerListener = listener;
        mActivity = activity;
        mLytBanner = createBannerParent(activity);
        mRlwHandler = new HandlerUtil.HandlerHolder(null);
    }

    private FrameLayout createBannerParent(Activity activity) {
        FrameLayout layout = new FrameLayout(activity);
        layout.setBackgroundColor(Color.TRANSPARENT);
        return layout;
    }

    @Override
    public void loadAd(OmManager.LOAD_TYPE type) {
        AdsUtil.callActionReport(mPlacementId, 0, EventId.CALLED_LOAD);
        super.loadAd(type);
    }

    public void setAdSize(AdSize adSize) {
        mAdSize = adSize;
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
        Map<String, String> placementInfo = PlacementUtils.getPlacementInfo(mPlacementId, instances,
                AuctionUtil.generateStringRequestData(mBidResponses, instances));
        if (mAdSize != null) {
            placementInfo.put("width", String.valueOf(mAdSize.getWidth()));
            placementInfo.put("height", String.valueOf(mAdSize.getHeight()));
        }
        bannerEvent.loadAd(mActRef.get(), placementInfo);
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
        try {
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
                    if (banner.getParent() != null) {
                        ViewGroup parent = (ViewGroup) banner.getParent();
                        parent.removeView(banner);
                    }
                    banner.addOnAttachStateChangeListener(this);
                    mLytBanner = createBannerParent(mActivity);
                    mLytBanner.addView(banner);
                    releaseAdEvent();
                    //
                    mBannerListener.onAdReady(mLytBanner);
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_SUCCESS,
                            PlacementUtils.placementEventParams(mPlacementId));
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
        } catch (Exception e) {
            if (isManualTriggered) {
                mBannerListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
                errorCallbackReport(ErrorCode.ERROR_NO_FILL);
            }
            CrashUtil.getSingleton().saveException(e);
        }
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
