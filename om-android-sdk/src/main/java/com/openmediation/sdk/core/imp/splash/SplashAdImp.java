package com.openmediation.sdk.core.imp.splash;

import android.app.Activity;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.bid.AuctionUtil;
import com.openmediation.sdk.core.AbstractHybridAd;
import com.openmediation.sdk.core.AdManager;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.splash.SplashAdListener;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.PlacementInfo;

import java.util.Map;

public class SplashAdImp extends AbstractHybridAd {

    private SplashAdListener mAdListener;

    private long mLoadTimeout;

    private int mWidth, mHeight;

    SplashAdImp(Activity activity, String placementId) {
        super(activity, placementId);
    }

    void setLoadTimeout(long timeout) {
        mLoadTimeout = timeout;
    }

    @Override
    public void loadAd(OmManager.LOAD_TYPE type) {
        AdsUtil.callActionReport(mPlacementId, 0, EventId.CALLED_LOAD);
        super.loadAd(type);
    }

    @Override
    protected void loadInsOnUIThread(BaseInstance instances) throws Throwable {
        instances.reportInsLoad(EventId.INSTANCE_LOAD);
        if (!checkActRef()) {
            onInsError(instances, ErrorCode.ERROR_ACTIVITY);
            return;
        }
        if (TextUtils.isEmpty(instances.getKey())) {
            onInsError(instances, ErrorCode.ERROR_EMPTY_INSTANCE_KEY);
            return;
        }
        CustomSplashEvent splashEvent = getAdEvent(instances);
        if (splashEvent == null) {
            onInsError(instances, ErrorCode.ERROR_CREATE_MEDATION_ADAPTER);
            return;
        }
        instances.setStart(System.currentTimeMillis());
        String payload = "";
        if (mBidResponses != null && mBidResponses.containsKey(instances.getId())) {
            payload = AuctionUtil.generateStringRequestData(mBidResponses.get(instances.getId()));
        }
        Map<String, String> placementInfo = PlacementUtils.getPlacementInfo(mReqId, mPlacementId, instances, payload);
        placementInfo.put("Timeout", String.valueOf(mLoadTimeout));
        placementInfo.put("Width", String.valueOf(mWidth));
        placementInfo.put("Height", String.valueOf(mHeight));
        splashEvent.loadAd(mActRef.get(), placementInfo);
        iLoadReport(instances);
    }

    @Override
    protected boolean isInsReady(BaseInstance instance) {
        return isReady(instance);
    }

    public void setAdListener(SplashAdListener listener) {
        mAdListener = listener;
    }

    private boolean isReady(BaseInstance instance) {
        if (instance == null) {
            return false;
        }
        CustomSplashEvent splashEvent = getAdEvent(instance);
        if (splashEvent == null) {
            return false;
        }
        return splashEvent.isReady();
    }

    public boolean isReady() {
        return isReady(mCurrentIns);
    }

    public void show(Activity activity, ViewGroup container) {
        if (!isReady()) {
            onAdShowFailed("SplashAd Show Failed: Not Ready");
            return;
        }
        getAdEvent(mCurrentIns).show(activity, container);
    }

    public void show(Activity activity) {
        if (!isReady()) {
            onAdShowFailed("SplashAd Show Failed: Not Ready");
            return;
        }
        getAdEvent(mCurrentIns).show(activity);
    }

    private void onAdShowFailed(final String error) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onAdShowFailedCallback(error);
            }
        });
    }

    @Override
    protected int getAdType() {
        return CommonConstants.SPLASH;
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacementId).getPlacementInfo(getAdType());
    }

    @Override
    protected void onAdErrorCallback(String error) {
        if (mAdListener != null) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_ERROR,
                    PlacementUtils.placementEventParams(mPlacementId));
            mAdListener.onSplashAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void onAdReadyCallback() {
        if (mAdListener != null) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_SUCCESS,
                    PlacementUtils.placementEventParams(mPlacementId));
            mAdListener.onSplashAdLoad(mPlacementId);
        }
    }

    @Override
    protected void onAdClickCallback() {
        if (mAdListener != null) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_CLICK,
                    PlacementUtils.placementEventParams(mPlacementId));
            mAdListener.onSplashAdClicked(mPlacementId);
        }
    }

    @Override
    protected void onAdShowedCallback() {
        if (mAdListener != null) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_PRESENT_SCREEN,
                    PlacementUtils.placementEventParams(mPlacementId));
            mAdListener.onSplashAdShowed(mPlacementId);
        }
        if (mCurrentIns != null) {
            notifyInsBidWin(mCurrentIns);
        }
    }

    @Override
    protected void onAdShowFailedCallback(String error) {
        if (mAdListener != null) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_SHOW_FAILED,
                    PlacementUtils.placementEventParams(mPlacementId));
            mAdListener.onSplashAdShowFailed(mPlacementId, error);
        }
        if (mCurrentIns != null && mBidResponses != null) {
            mBidResponses.remove(mCurrentIns.getId());
        }
    }

    @Override
    protected void onAdCloseCallback() {
        if (mAdListener != null) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_DISMISS_SCREEN,
                    PlacementUtils.placementEventParams(mPlacementId));
            mAdListener.onSplashAdDismissed(mPlacementId);
        }
    }

    @Override
    protected synchronized void onInsClosed(String instanceKey, String instanceId) {
        super.onInsClosed(instanceKey, instanceId);
        if (mCurrentIns != null) {
            destroyAdEvent(mCurrentIns);
            AdManager.getInstance().removeInsAdEvent(mCurrentIns);
            if (mBidResponses != null) {
                mBidResponses.remove(mCurrentIns.getId());
            }
        }
        cleanAfterCloseOrFailed();
    }

    @Override
    protected void onInsTick(String instanceKey, String instanceId, long millisUntilFinished) {
        super.onInsTick(instanceKey, instanceId, millisUntilFinished);
        if (mAdListener != null) {
            mAdListener.onSplashAdTick(mPlacementId, millisUntilFinished);
        }
    }

    private CustomSplashEvent getAdEvent(BaseInstance instances) {
        return (CustomSplashEvent) AdManager.getInstance().getInsAdEvent(CommonConstants.SPLASH, instances);
    }

    void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }
}
