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
import com.openmediation.sdk.mobileads.unity.BuildConfig;
import com.openmediation.sdk.utils.AdLog;
import com.unity3d.ads.IUnityAdsInitializationListener;
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

    private synchronized void initSDK(Activity activity, IUnityAdsInitializationListener listener) {
        UnityAds.addListener(this);
        UnitySingleTon.getInstance().init(activity, mAppKey, listener);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity, new IUnityAdsInitializationListener() {
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
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                mRvCallbacks.put(adUnitId, callback);
            }
            if (UnityAds.isReady(adUnitId)) {
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
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (UnityAds.isReady(adUnitId)) {
            UnityAds.show(activity, adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "ad not ready"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return UnityAds.isReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity, new IUnityAdsInitializationListener() {
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
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            if (UnityAds.isReady(adUnitId)) {
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
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return UnityAds.isReady(adUnitId);
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (UnityAds.isReady(adUnitId)) {
            UnityAds.show(activity, adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad not ready"));
            }
        }
    }

    @Override
    public void onUnityAdsClick(String placementId) {
        AdLog.getSingleton().LogD(TAG, "onUnityAdsClick : " + placementId);
        if (!TextUtils.isEmpty(placementId)) {
            RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdClicked();
            } else {
                InterstitialAdCallback isCallback = mIsCallbacks.get(placementId);
                if (isCallback != null) {
                    isCallback.onInterstitialAdClick();
                }
            }
        }
    }

    @Override
    public void onUnityAdsPlacementStateChanged(String placementId, UnityAds.PlacementState oldState,
                                                UnityAds.PlacementState newState) {
        AdLog.getSingleton().LogD(TAG, "onUnityAdsPlacementStateChanged : " + placementId
                + " oldState : " + oldState.name() + " newState: " + newState.name());
        if (newState.equals(oldState) || newState.equals(UnityAds.PlacementState.WAITING)) {
            return;
        }

        if (!TextUtils.isEmpty(placementId)) {
            if (mRvLoadTrigerIds.contains(placementId)) {
                RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
                if (rvCallback != null) {
                    if (UnityAds.isReady(placementId)) {
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
                        if (UnityAds.isReady(placementId)) {
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
        AdLog.getSingleton().LogD(TAG, "onUnityAdsStart : " + placementId);
        if (!TextUtils.isEmpty(placementId)) {
            RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdShowSuccess();
                rvCallback.onRewardedVideoAdStarted();
            } else {
                InterstitialAdCallback isCallback = mIsCallbacks.get(placementId);
                if (isCallback != null) {
                    isCallback.onInterstitialAdShowSuccess();
                }
            }
        }
    }

    @Override
    public void onUnityAdsFinish(String placementId, UnityAds.FinishState result) {
        AdLog.getSingleton().LogD(TAG, "onUnityAdsFinish : " + placementId + " result : " + result.name());
        if (!TextUtils.isEmpty(placementId)) {
            RewardedVideoCallback rvCallback = mRvCallbacks.get(placementId);
            if (rvCallback != null) {
                if (result.equals(UnityAds.FinishState.COMPLETED)) {
                    rvCallback.onRewardedVideoAdEnded();
                    rvCallback.onRewardedVideoAdRewarded();
                }
                rvCallback.onRewardedVideoAdClosed();
            } else {
                InterstitialAdCallback isCallback = mIsCallbacks.get(placementId);
                if (isCallback != null) {
                    isCallback.onInterstitialAdClosed();
                }
            }
        }
    }

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
    }
}
