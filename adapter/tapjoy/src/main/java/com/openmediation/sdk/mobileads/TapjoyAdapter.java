// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.tapjoy.BuildConfig;
import com.openmediation.sdk.utils.AdLog;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementVideoListener;
import com.tapjoy.Tapjoy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TapjoyAdapter extends CustomAdsAdapter implements TJConnectListener, TJPlacementListener, TJPlacementVideoListener {

    private static final String ADT_MEDIATION_NAME = "OM";
    private InitState mInitState = InitState.NOT_INIT;
    private ConcurrentMap<String, TJPlacement> mVideos;
    private ConcurrentMap<TJPlacement, RewardedVideoCallback> mVideoCallbacks;
    private ConcurrentMap<String, TJPlacement> mInterstitialAds;
    private ConcurrentMap<TJPlacement, InterstitialAdCallback> mInterstitialAdCallbacks;
    private Handler mHandler;

    public TapjoyAdapter() {
        mHandler = new Handler(Looper.getMainLooper());
        mVideos = new ConcurrentHashMap<>();
        mVideoCallbacks = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mInterstitialAdCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return Tapjoy.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_11;
    }

    private synchronized void initSDK(final Activity activity) {
        mInitState = InitState.INIT_PENDING;
        Tapjoy.limitedConnect(activity.getApplicationContext(), mAppKey, this);
    }

    private synchronized TJPlacement requestVideoAd(String placementId) {
        TJPlacement placement = null;
        if (mVideos.containsKey(placementId) && mVideos.get(placementId) != null) {
            placement = mVideos.get(placementId);
        }
        if (placement == null) {
            placement = Tapjoy.getLimitedPlacement(placementId, this);
            placement.setVideoListener(this);
            placement.setMediationName(ADT_MEDIATION_NAME);
            placement.setAdapterVersion(getAdapterVersion());
            mVideos.put(placementId, placement);
        }
        return placement;
    }

    private synchronized TJPlacement requestInterstitialAd(String placementId) {
        TJPlacement placement = null;
        if (mInterstitialAds.containsKey(placementId) && mInterstitialAds.get(placementId) != null) {
            placement = mInterstitialAds.get(placementId);
        }
        if (placement == null) {
            placement = Tapjoy.getLimitedPlacement(placementId, this);
            placement.setMediationName(ADT_MEDIATION_NAME);
            placement.setAdapterVersion(getAdapterVersion());
            mInterstitialAds.put(placementId, placement);
        }
        return placement;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    Tapjoy.setActivity(activity);
                    initSDK(activity);
                    if (dataMap.get("pid") != null && callback != null) {
                        mVideoCallbacks.put(requestVideoAd((String) dataMap.get("pid")), callback);
                    }
                    break;
                case INIT_PENDING:
                    if (dataMap.get("pid") != null && callback != null) {
                        mVideoCallbacks.put(requestVideoAd((String) dataMap.get("pid")), callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                    break;
                case INIT_FAIL:
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed("Tapjoy init failed");
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!Tapjoy.isLimitedConnected()) {
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed("Tapjoy video load failed cause not init");
                }
            } else {
                TJPlacement placement = requestVideoAd(adUnitId);
                if (placement != null) {
                    if (!mVideoCallbacks.containsKey(placement) && callback != null) {
                        mVideoCallbacks.put(placement, callback);
                    }
                    if (placement.isContentReady()) {
                        if (callback != null) {
                            callback.onRewardedVideoLoadSuccess();
                        }
                    } else {
                        placement.requestContent();
                    }
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (TextUtils.isEmpty(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("Tapjoy Video ad placementId is null");
            }
            return;
        }
        TJPlacement placement = mVideos.get(adUnitId);
        if (placement != null && placement.isContentReady()) {
            if (!mVideoCallbacks.containsKey(placement) && callback != null) {
                mVideoCallbacks.put(placement, callback);
            }
            placement.showContent();
        } else {
            AdLog.getSingleton().LogE("Om-Tapjoy: Tapjoy Video ad not ready ");
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("Tapjoy Video ad not ready ");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        TJPlacement placement = mVideos.get(adUnitId);
        return placement != null && placement.isContentReady();
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    Tapjoy.setActivity(activity);
                    initSDK(activity);
                    if (dataMap.get("pid") != null && callback != null) {
                        mInterstitialAdCallbacks.put(requestInterstitialAd((String) dataMap.get("pid")), callback);
                    }
                    break;
                case INIT_PENDING:
                    if (dataMap.get("pid") != null && callback != null) {
                        mInterstitialAdCallbacks.put(requestInterstitialAd((String) dataMap.get("pid")), callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                    break;
                case INIT_FAIL:
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed("Tapjoy init failed");
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!Tapjoy.isLimitedConnected()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed("Tapjoy interstitial load failed cause not init");
                }
            } else {
                TJPlacement placement = requestInterstitialAd(adUnitId);
                if (placement != null) {
                    if (!mInterstitialAdCallbacks.containsKey(placement) && callback != null) {
                        mInterstitialAdCallbacks.put(placement, callback);
                    }
                    if (placement.isContentReady()) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadSuccess();
                        }
                    } else {
                        placement.requestContent();
                    }
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (TextUtils.isEmpty(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("Tapjoy Interstitial ad placementId is null");
            }
            return;
        }
        TJPlacement placement = mInterstitialAds.get(adUnitId);
        if (placement != null && placement.isContentReady()) {
            if (!mInterstitialAdCallbacks.containsKey(placement) && callback != null) {
                mInterstitialAdCallbacks.put(placement, callback);
            }
            placement.showContent();
        } else {
            AdLog.getSingleton().LogE("Om-Tapjoy: Tapjoy Interstitial ad not ready ");
            if (callback != null) {
                callback.onInterstitialAdShowFailed("Tapjoy Interstitial ad not ready ");
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        TJPlacement placement = mInterstitialAds.get(adUnitId);
        return placement != null && placement.isContentReady();
    }

    @Override
    public void onConnectSuccess() {
        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy init success ");
        for (TJPlacement tjPlacement : mVideoCallbacks.keySet()) {
            callbackOnMainThread(8, tjPlacement, "");
        }
        for (TJPlacement tjPlacement : mInterstitialAdCallbacks.keySet()) {
            callbackOnMainThread(8, tjPlacement, "");
        }
        mInitState = InitState.INIT_SUCCESS;
    }

    @Override
    public void onConnectFailure() {
        AdLog.getSingleton().LogE("Om-Tapjoy: Tapjoy init failed ");
        for (TJPlacement tjPlacement : mVideoCallbacks.keySet()) {
            callbackOnMainThread(9, tjPlacement, "Tapjoy init failed");
        }
        for (TJPlacement tjPlacement : mInterstitialAdCallbacks.keySet()) {
            callbackOnMainThread(9, tjPlacement, "Tapjoy init failed");
        }
        mInitState = InitState.INIT_FAIL;
    }

    @Override
    public void onRequestSuccess(TJPlacement tjPlacement) {
        if (!tjPlacement.isContentAvailable()) {
            //no fill
            callbackOnMainThread(1, tjPlacement, "no fill");
        }
    }

    @Override
    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
        callbackOnMainThread(1, tjPlacement, tjError.message);
    }

    @Override
    public void onContentReady(TJPlacement tjPlacement) {
        callbackOnMainThread(0, tjPlacement, null);
    }

    @Override
    public void onContentShow(TJPlacement tjPlacement) {
        callbackOnMainThread(3, tjPlacement, null);
    }

    @Override
    public void onContentDismiss(TJPlacement tjPlacement) {
        callbackOnMainThread(5, tjPlacement, null);
    }

    @Override
    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

    }

    @Override
    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy video ad reward request " + s);
    }

    @Override
    public void onClick(TJPlacement tjPlacement) {
        callbackOnMainThread(4, tjPlacement, null);
    }

    @Override
    public void onVideoStart(TJPlacement tjPlacement) {
        callbackOnMainThread(7, tjPlacement, null);
    }

    @Override
    public void onVideoError(TJPlacement tjPlacement, String s) {
        callbackOnMainThread(2, tjPlacement, null);
    }

    @Override
    public void onVideoComplete(TJPlacement tjPlacement) {
        callbackOnMainThread(6, tjPlacement, null);
    }

    private void callbackOnMainThread(final int callbackType, final TJPlacement placement, final String error) {
        final RewardedVideoCallback videoCallback = mVideoCallbacks.get(placement);
        final InterstitialAdCallback isCallback = mInterstitialAdCallbacks.get(placement);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                switch (callbackType) {
                    case 0:
                        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy ad load success " + placement.getName());
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoLoadSuccess();
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdLoadSuccess();
                        }
                        break;
                    case 1:
                        AdLog.getSingleton().LogE("Om-Tapjoy: Tapjoy ad load failed :" + error);
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoLoadFailed(error);
                            removeRvCallbackKey(placement);
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdLoadFailed(error);
                            removeIsCallbackKey(placement);
                        }
                        break;
                    case 2:
                        AdLog.getSingleton().LogE("Om-Tapjoy: Tapjoy ad show failed :" + error);
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdShowFailed(error);
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdShowFailed(error);
                        }
                        break;
                    case 7:
                        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy video ad start");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdStarted();
                        }
                        break;
                    case 3:
                        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy ad open");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdShowSuccess();
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdShowSuccess();
                        }
                        break;
                    case 4:
                        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy ad click");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdClicked();
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdClick();
                        }
                        break;
                    case 5:
                        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy ad close");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdClosed();
                            removeRvCallbackKey(placement);
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdClosed();
                            removeIsCallbackKey(placement);
                        }
                        break;
                    case 6:
                        AdLog.getSingleton().LogD("Om-Tapjoy", "Tapjoy video ad end");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdEnded();
                            videoCallback.onRewardedVideoAdRewarded();
                        }
                        break;
                    case 8:
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoInitSuccess();
                        }

                        if (isCallback != null) {
                            isCallback.onInterstitialAdInitSuccess();
                        }
                        break;
                    case 9:
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoInitFailed(error);
                        }

                        if (isCallback != null) {
                            isCallback.onInterstitialAdInitFailed(error);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        mHandler.post(runnable);
    }

    private void removeRvCallbackKey(TJPlacement tjPlacement) {
        if (mVideoCallbacks != null) {
            mVideoCallbacks.remove(tjPlacement);
            mVideoCallbacks.keySet().remove(tjPlacement);
        }
    }

    private void removeIsCallbackKey(TJPlacement tjPlacement) {
        if (mInterstitialAdCallbacks != null) {
            mInterstitialAdCallbacks.remove(tjPlacement);
            mInterstitialAdCallbacks.keySet().remove(tjPlacement);
        }
    }

    /**
     * Vungle sdk init state
     */
    private enum InitState {
        /**
         *
         */
        NOT_INIT,
        /**
         *
         */
        INIT_PENDING,
        /**
         *
         */
        INIT_SUCCESS,
        /**
         *
         */
        INIT_FAIL
    }
}
