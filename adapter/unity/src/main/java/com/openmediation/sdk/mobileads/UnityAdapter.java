// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.unity.BuildConfig;
import com.openmediation.sdk.utils.AdLog;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.metadata.MediationMetaData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnityAdapter extends CustomAdsAdapter implements IUnityAdsExtendedListener {

    private static final String TAG = "Om-Unity";

    private ConcurrentLinkedQueue<String> mRvLoadTrigerIds;
    private ConcurrentLinkedQueue<String> mIsLoadTrigerIds;
    private ConcurrentHashMap<String, InterstitialAdCallback> mIsCallbacks;
    private ConcurrentHashMap<String, RewardedVideoCallback> mRvCallbacks;

    private boolean mDidInit = false;

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

    private synchronized void initSDK(Activity activity) {
        if (!mDidInit) {
            AdLog.getSingleton().LogD("UnityAdapter", "initSDK, appkey:" + mAppKey);
            MediationMetaData mediationMetaData = new MediationMetaData(activity);
            mediationMetaData.setName("Om");
            mediationMetaData.setVersion(BuildConfig.VERSION_NAME);
            mediationMetaData.commit();

            UnityAds.initialize(activity, mAppKey, this);
            mDidInit = true;
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        AdLog.getSingleton().LogD("UnityAdapter", "initRewardedVideo, appkey:" + mAppKey);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity);
            if (mDidInit) {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
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
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            mRvCallbacks.put(adUnitId, callback);
            if (UnityAds.isReady(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                if (UnityAds.isInitialized() && UnityAds.getPlacementState(adUnitId) != UnityAds.PlacementState.WAITING) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed("No Fill");
                    }
                } else {
                    mRvLoadTrigerIds.add(adUnitId);
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed("loadRewardedVideo error cause " + checkError);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        if (UnityAds.isReady(adUnitId)) {
            UnityAds.show(activity, adUnitId);
        } else {
            AdLog.getSingleton().LogE(TAG + ": Unity Video show() called but ad not ready");
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("Unity Video show() called but ad not ready");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return UnityAds.isReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        AdLog.getSingleton().LogD("UnityAdapter", "initInterstitialAd, appkey:" + mAppKey);
        initSDK(activity);
        if (mDidInit) {
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            mIsCallbacks.put(adUnitId, callback);
            if (UnityAds.isReady(adUnitId)) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                if (UnityAds.isInitialized() && UnityAds.getPlacementState(adUnitId) != UnityAds.PlacementState.WAITING) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed("No Fill");
                    }
                } else {
                    mIsLoadTrigerIds.add(adUnitId);
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed("loadInterstitialAd error cause " + checkError);
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
        if (UnityAds.isReady(adUnitId)) {
            UnityAds.show(activity, adUnitId);
        } else {
            AdLog.getSingleton().LogE(TAG + ": Unity interstitial show() called but ad not ready");
            if (callback != null) {
                callback.onInterstitialAdShowFailed("Unity interstitial show() called but ad not ready");
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
                        rvCallback.onRewardedVideoLoadFailed(placementId + " placement state: " + newState.toString());
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
                            isCallback.onInterstitialAdLoadFailed(placementId + " placement state: " + newState.toString());
                        }
                        mIsLoadTrigerIds.remove(placementId);
                    }
                }
            }
        }
    }

    @Override
    public void onUnityAdsReady(String placementId) {
        AdLog.getSingleton().LogD(TAG, "onUnityAdsReady : " + placementId);
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
        AdLog.getSingleton().LogE(TAG + ":onUnityAdsError, error:" + error + ", message:"
                + message);
    }
}
