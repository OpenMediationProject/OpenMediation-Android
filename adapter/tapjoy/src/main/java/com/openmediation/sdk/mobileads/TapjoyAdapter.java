// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
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
import java.util.concurrent.CopyOnWriteArraySet;

public class TapjoyAdapter extends CustomAdsAdapter implements TJConnectListener {

    public static final String TAG = "Om-Tapjoy";
    private static final String MEDIATION_NAME = "OM";
    private InitState mInitState = InitState.NOT_INIT;
    private CopyOnWriteArraySet<RewardedVideoCallback> mVideoCallbacks;
    private CopyOnWriteArraySet<InterstitialAdCallback> mInterstitialAdCallbacks;
    private ConcurrentMap<String, TJPlacement> mVideos;
    private ConcurrentMap<String, TJPlacement> mInterstitialAds;

    private static final int AGE_RESTRICTION = 13;

    public TapjoyAdapter() {
        mVideoCallbacks = new CopyOnWriteArraySet<>();
        mInterstitialAdCallbacks = new CopyOnWriteArraySet<>();
        mVideos = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
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

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        try {
            Tapjoy.getPrivacyPolicy().setUserConsent(consent ? "1" : "0");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        try {
            Tapjoy.getPrivacyPolicy().setBelowConsentAge(restricted);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        setAgeRestricted(context, age < AGE_RESTRICTION);
    }

    private synchronized void initSDK(final Activity activity) {
        mInitState = InitState.INIT_PENDING;
        Tapjoy.limitedConnect(activity.getApplicationContext(), mAppKey, this);
    }

    private synchronized TJPlacement requestVideoAd(final String placementId, final RewardedVideoCallback callback) {
        TJPlacement placement = Tapjoy.getLimitedPlacement(placementId, new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onRequestSuccess " + placementId);
            }

            @Override
            public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onRequestFailure " + placementId);
                if (tjError == null) {
                    tjError = new TJError(0, "No Fill");
                }
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, tjError.code, tjError.message));
                }
            }

            @Override
            public void onContentReady(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onContentReady " + placementId);
                mVideos.put(placementId, tjPlacement);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            @Override
            public void onContentShow(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onContentShow " + placementId);
                if (callback != null) {
                    callback.onRewardedVideoAdShowSuccess();
                }
            }

            @Override
            public void onContentDismiss(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onContentDismiss " + placementId);
                if (callback != null) {
                    callback.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

            }

            @Override
            public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onRewardRequest " + placementId);
            }

            @Override
            public void onClick(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onClick " + placementId);
                if (callback != null) {
                    callback.onRewardedVideoAdClicked();
                }
            }
        });
        placement.setVideoListener(new TJPlacementVideoListener() {
            @Override
            public void onVideoStart(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onVideoStart " + placementId);
                if (callback != null) {
                    callback.onRewardedVideoAdStarted();
                }
            }

            @Override
            public void onVideoError(TJPlacement tjPlacement, String message) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onVideoStart " + placementId);
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, message));
                }
            }

            @Override
            public void onVideoComplete(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy RewardedVideo onVideoComplete " + placementId);
                if (callback != null) {
                    callback.onRewardedVideoAdEnded();
                    callback.onRewardedVideoAdRewarded();
                }
            }
        });
        placement.setMediationName(MEDIATION_NAME);
        placement.setAdapterVersion(getAdapterVersion());
        return placement;
    }

    private synchronized TJPlacement requestInterstitialAd(final String placementId, final InterstitialAdCallback callback) {
        TJPlacement placement = Tapjoy.getLimitedPlacement(placementId, new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy InterstitialAd onRequestSuccess " + placementId);
            }

            @Override
            public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy InterstitialAd onRequestFailure " + placementId);
                if (tjError == null) {
                    tjError = new TJError(0, "No Fill");
                }
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, tjError.code, tjError.message));
                }
            }

            @Override
            public void onContentReady(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy InterstitialAd onContentReady " + placementId);
                mInterstitialAds.put(placementId, tjPlacement);
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            }

            @Override
            public void onContentShow(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy InterstitialAd onContentShow " + placementId);
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            public void onContentDismiss(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy InterstitialAd onContentDismiss " + placementId);
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }

            @Override
            public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

            }

            @Override
            public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {

            }

            @Override
            public void onClick(TJPlacement tjPlacement) {
                AdLog.getSingleton().LogD(TAG, "Tapjoy InterstitialAd onClick " + placementId);
                if (callback != null) {
                    callback.onInterstitialAdClicked();
                }
            }
        });
        placement.setMediationName(MEDIATION_NAME);
        placement.setAdapterVersion(getAdapterVersion());
        return placement;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            Tapjoy.setActivity(activity);
            switch (mInitState) {
                case NOT_INIT:
                    try {
                        if (callback != null) {
                            mVideoCallbacks.add(callback);
                        }
                        initSDK(activity);
                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                        }
                    }
                    break;
                case INIT_PENDING:
                    if (callback != null) {
                        mVideoCallbacks.add(callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        try {
            String error = check(activity, adUnitId);
            if (TextUtils.isEmpty(error)) {
                Tapjoy.setActivity(activity);
                if (!Tapjoy.isLimitedConnected()) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Tapjoy video load failed cause not init"));
                    }
                } else {
                    TJPlacement placement = requestVideoAd(adUnitId, callback);
                    if (placement.isContentReady()) {
                        if (callback != null) {
                            callback.onRewardedVideoLoadSuccess();
                        }
                    } else {
                        placement.requestContent();
                    }
                }
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (TextUtils.isEmpty(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Tapjoy Video ad placementId is null"));
            }
            return;
        }
        try {
            TJPlacement placement = mVideos.remove(adUnitId);
            if (placement != null && placement.isContentReady()) {
                placement.showContent();
            } else {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Tapjoy Video ad not ready"));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        try {
            TJPlacement placement = mVideos.get(adUnitId);
            return placement != null && placement.isContentReady();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            Tapjoy.setActivity(activity);
            switch (mInitState) {
                case NOT_INIT:
                    try {
                        if (callback != null) {
                            mInterstitialAdCallbacks.add(callback);
                        }
                        initSDK(activity);
                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                        }
                    }
                    break;
                case INIT_PENDING:
                    if (callback != null) {
                        mInterstitialAdCallbacks.add(callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        try {
            String error = check(activity, adUnitId);
            if (TextUtils.isEmpty(error)) {
                Tapjoy.setActivity(activity);
                if (!Tapjoy.isLimitedConnected()) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Tapjoy interstitial load failed cause not init"));
                    }
                } else {
                    TJPlacement placement = requestInterstitialAd(adUnitId, callback);
                    if (placement.isContentReady()) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadSuccess();
                        }
                    } else {
                        placement.requestContent();
                    }
                }
            } else {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (TextUtils.isEmpty(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Tapjoy Interstitial ad placementId is null"));
            }
            return;
        }
        try {
            TJPlacement placement = mInterstitialAds.remove(adUnitId);
            if (placement != null && placement.isContentReady()) {
                placement.showContent();
            } else {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Tapjoy Interstitial ad not ready"));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        try {
            TJPlacement placement = mInterstitialAds.get(adUnitId);
            return placement != null && placement.isContentReady();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void onConnectSuccess() {
        AdLog.getSingleton().LogD(TAG, "Tapjoy init success ");
        mInitState = InitState.INIT_SUCCESS;
        for (RewardedVideoCallback callback : mVideoCallbacks) {
            callback.onRewardedVideoInitSuccess();
        }
        mVideoCallbacks.clear();
        for (InterstitialAdCallback callback : mInterstitialAdCallbacks) {
            callback.onInterstitialAdInitSuccess();
        }
        mInterstitialAdCallbacks.clear();
    }

    @Override
    public void onConnectFailure() {
        mInitState = InitState.NOT_INIT;
        for (RewardedVideoCallback callback : mVideoCallbacks) {
            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Tapjoy init failed"));
        }
        mVideoCallbacks.clear();

        for (InterstitialAdCallback callback : mInterstitialAdCallbacks) {
            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Tapjoy init failed"));
        }
        mInterstitialAdCallbacks.clear();
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
    }
}
