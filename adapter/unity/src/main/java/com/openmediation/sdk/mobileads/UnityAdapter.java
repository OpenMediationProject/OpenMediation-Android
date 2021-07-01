// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.unity.BuildConfig;
import com.openmediation.sdk.utils.AdLog;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.metadata.MetaData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnityAdapter extends CustomAdsAdapter implements IUnityAdsExtendedListener {

    private static final String TAG = "Unity";
    private ConcurrentLinkedQueue<String> mRvLoadTrigerIds;
    private ConcurrentLinkedQueue<String> mIsLoadTrigerIds;
    private ConcurrentHashMap<String, InterstitialAdCallback> mIsCallbacks;
    private ConcurrentHashMap<String, RewardedVideoCallback> mRvCallbacks;

    public UnityAdapter() {
        mIsLoadTrigerIds = new ConcurrentLinkedQueue<>();
        mRvLoadTrigerIds = new ConcurrentLinkedQueue<>();

        mIsCallbacks = new ConcurrentHashMap<>();
        mRvCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return UnityAds.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_4;
    }

    @Override
    public boolean isAdNetworkInit() {
        return UnitySingleTon.getInstance().isInit();
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (context != null) {
            MetaData gdprMetaData = new MetaData(context);
            gdprMetaData.set("gdpr.consent", consent);
            gdprMetaData.commit();
        }
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        if (context != null) {
            MetaData ageGateMetaData = new MetaData(context);
            ageGateMetaData.set("privacy.useroveragelimit", restricted);
            ageGateMetaData.commit();
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        if (context != null) {
            MetaData privacyMetaData = new MetaData(context);
            privacyMetaData.set("privacy.consent", !value);
            privacyMetaData.commit();
        }
    }

    private synchronized void initSDK(IUnityAdsInitializationListener listener) {
        UnityAds.addListener(this);
        UnitySingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, listener);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check();
        if (TextUtils.isEmpty(error)) {
            initSDK(new IUnityAdsInitializationListener() {
                @Override
                public void onInitializationComplete() {
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                }

                @Override
                public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, message));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String checkError = check(adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                mRvCallbacks.put(adUnitId, callback);
            }
            if (isRewardedVideoAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                if (UnityAds.isInitialized() && UnityAds.getPlacementState(adUnitId) != UnityAds.PlacementState.WAITING) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, UnityAds.getPlacementState(adUnitId).name()));
                    }
                } else {
                    mRvLoadTrigerIds.add(adUnitId);
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, final RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (isRewardedVideoAvailable(adUnitId)) {
            UnityAds.show(activity, adUnitId, new IUnityAdsShowListener() {
                @Override
                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onUnityAdsShowFailure : " + placementId);
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.name() + ", " + message));
                    }
                }

                @Override
                public void onUnityAdsShowStart(String placementId) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onUnityAdsShowStart : " + placementId);
                    if (callback != null) {
                        callback.onRewardedVideoAdShowSuccess();
                        callback.onRewardedVideoAdStarted();
                    }
                }

                @Override
                public void onUnityAdsShowClick(String placementId) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onUnityAdsShowClick : " + placementId);
                    if (callback != null) {
                        callback.onRewardedVideoAdClicked();
                    }
                }

                @Override
                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onUnityAdsShowComplete : " + placementId);
                    if (callback != null) {
                        if (state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
                            callback.onRewardedVideoAdEnded();
                            callback.onRewardedVideoAdRewarded();
                        }
                        callback.onRewardedVideoAdClosed();
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "ad not ready"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return UnityAds.getPlacementState(adUnitId) == UnityAds.PlacementState.READY;
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (TextUtils.isEmpty(error)) {
            initSDK(new IUnityAdsInitializationListener() {
                @Override
                public void onInitializationComplete() {
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                }

                @Override
                public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, message));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, final String adUnitId, Map<String, Object> extras, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String checkError = check(adUnitId);
        if (!TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            if (UnityAds.isInitialized() && UnityAds.getPlacementState(adUnitId) != UnityAds.PlacementState.WAITING) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, UnityAds.getPlacementState(adUnitId).name()));
                }
            } else {
                mIsLoadTrigerIds.add(adUnitId);
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return UnityAds.getPlacementState(adUnitId) == UnityAds.PlacementState.READY;
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        UnityAds.show(activity, adUnitId, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                AdLog.getSingleton().LogD(TAG, "InterstitialAd onUnityAdsShowFailure : " + placementId);
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.name() + ", " + message));
                }
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {
                AdLog.getSingleton().LogD(TAG, "InterstitialAd onUnityAdsShowStart : " + placementId);
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
                AdLog.getSingleton().LogD(TAG, "InterstitialAd onUnityAdsShowClick : " + placementId);
                if (callback != null) {
                    callback.onInterstitialAdClicked();
                }
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                AdLog.getSingleton().LogD(TAG, "InterstitialAd onUnityAdsShowComplete : " + placementId);
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }
        });
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        UnityBannerManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        UnityBannerManager.getInstance().loadAd(activity, adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return UnityBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        UnityBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void onUnityAdsClick(String placementId) {
//        AdLog.getSingleton().LogD(TAG, "onUnityAdsClick : " + placementId);
//        if (!TextUtils.isEmpty(placementId)) {
//            RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
//            if (rvCallback != null) {
//                rvCallback.onRewardedVideoAdClicked();
//            } else {
//                InterstitialAdCallback isCallback = mIsCallbacks.get(placementId);
//                if (isCallback != null) {
//                    isCallback.onInterstitialAdClick();
//                }
//            }
//        }
    }

    @Override
    public void onUnityAdsPlacementStateChanged(String placementId, UnityAds.PlacementState oldState, UnityAds.PlacementState newState) {
        AdLog.getSingleton().LogD(TAG, "onUnityAdsPlacementStateChanged : " + placementId
                + " oldState : " + oldState.name() + " newState: " + newState.name());
        if (newState.equals(oldState) || newState.equals(UnityAds.PlacementState.WAITING)) {
            return;
        }

        if (!TextUtils.isEmpty(placementId)) {
            if (mRvLoadTrigerIds.contains(placementId)) {
                RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
                if (rvCallback != null) {
                    if (isRewardedVideoAvailable(placementId)) {
                        rvCallback.onRewardedVideoLoadSuccess();
                    } else {
                        rvCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, newState.name()));
                    }
                    mRvLoadTrigerIds.remove(placementId);
                }
            } else {
                if (mIsLoadTrigerIds.contains(placementId)) {
                    InterstitialAdCallback isCallback = mIsCallbacks.get(placementId);
                    if (isCallback != null) {
                        if (isInterstitialAdAvailable(placementId)) {
                            isCallback.onInterstitialAdLoadSuccess();
                        } else {
                            isCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, newState.name()));
                        }
                        mIsLoadTrigerIds.remove(placementId);
                    }
                }
            }
        }
    }

    @Override
    public void onUnityAdsReady(String placementId) {
    }

    @Override
    public void onUnityAdsStart(String placementId) {
//        AdLog.getSingleton().LogD(TAG, "onUnityAdsStart : " + placementId);
//        if (!TextUtils.isEmpty(placementId)) {
//            RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
//            if (rvCallback != null) {
//                rvCallback.onRewardedVideoAdShowSuccess();
//                rvCallback.onRewardedVideoAdStarted();
//            } else {
//                InterstitialAdCallback isCallback = mIsCallbacks.get(placementId);
//                if (isCallback != null) {
//                    isCallback.onInterstitialAdShowSuccess();
//                }
//            }
//        }
    }

    @Override
    public void onUnityAdsFinish(String placementId, UnityAds.FinishState result) {
//        AdLog.getSingleton().LogD(TAG, "onUnityAdsFinish : " + placementId + " result : " + result.name());
//        if (!TextUtils.isEmpty(placementId)) {
//            RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
//            if (rvCallback != null) {
//                if (result.equals(UnityAds.FinishState.COMPLETED)) {
//                    rvCallback.onRewardedVideoAdEnded();
//                    rvCallback.onRewardedVideoAdRewarded();
//                }
//                rvCallback.onRewardedVideoAdClosed();
//            } else {
//                InterstitialAdCallback isCallback = mIsCallbacks.get(placementId);
//                if (isCallback != null) {
//                    isCallback.onInterstitialAdClosed();
//                }
//            }
//        }
    }

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {

    }
}
