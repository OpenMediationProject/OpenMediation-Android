// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.adtbid.sdk.AdTimingAds;
import com.adtbid.sdk.interstitial.AdTimingInterstitialAd;
import com.adtbid.sdk.interstitial.InterstitialAdListener;
import com.adtbid.sdk.utils.error.AdTimingError;
import com.adtbid.sdk.video.AdTimingRewardedVideo;
import com.adtbid.sdk.video.RewardedVideoListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.adtiming.BuildConfig;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AdTimingAdapter extends CustomAdsAdapter implements RewardedVideoListener, InterstitialAdListener {
    private static final String TAG = "OM-AdTiming: ";
    private static final String PAY_LOAD = "pay_load";
    private List<RewardedVideoCallback> mInitVideoListeners;
    private List<InterstitialAdCallback> mInitInterstitialListeners;
    private ConcurrentMap<String, RewardedVideoCallback> mVideoListeners;
    private ConcurrentMap<String, InterstitialAdCallback> mInterstitialListeners;

    public AdTimingAdapter() {
        mInitVideoListeners = new CopyOnWriteArrayList<>();
        mInitInterstitialListeners = new CopyOnWriteArrayList<>();
        mVideoListeners = new ConcurrentHashMap<>();
        mInterstitialListeners = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return AdTimingAds.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_1;
    }

    @Override
    public boolean isAdNetworkInit() {
        return AdTimingSingleTon.getInstance().isInit();
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        AdTimingAds.setGDPRConsent(consent);
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        AdTimingAds.setUSPrivacyLimit(value);
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        AdTimingAds.setAgeRestricted(restricted);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        AdTimingAds.setUserAge(age);
    }

    @Override
    public void setUserGender(Context context, String gender) {
        super.setUserGender(context, gender);
        AdTimingAds.setUserGender(gender);
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        AdTimingAds.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        AdTimingAds.onPause(activity);
        super.onPause(activity);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            return;
        }
        if (callback != null) {
            mInitVideoListeners.add(callback);
        }
        String appKey = (String) dataMap.get("AppKey");
        initSDK(appKey);
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRewardedVideoAd(adUnitId, extras, callback);
    }

    private void loadRewardedVideoAd(String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            return;
        }
        if (!TextUtils.isEmpty(adUnitId) && callback != null) {
            mVideoListeners.put(adUnitId, callback);
        }
        AdTimingRewardedVideo.setAdListener(adUnitId, this);
        String payload = "";
        if (extras != null && extras.containsKey(PAY_LOAD)) {
            payload = String.valueOf(extras.get(PAY_LOAD));
        }
        AdTimingRewardedVideo.loadAdWithPayload(adUnitId, payload);
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            return;
        }
        if (isRewardedVideoAvailable(adUnitId)) {
            AdTimingRewardedVideo.setAdListener(adUnitId, this);
            AdTimingRewardedVideo.showAd(adUnitId);
        } else {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "no reward ad or not ready"));
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return AdTimingRewardedVideo.isReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        if (callback != null) {
            mInitInterstitialListeners.add(callback);
        }
        String appKey = (String) dataMap.get("AppKey");
        initSDK(appKey);
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadIsAd(adUnitId, extras, callback);
    }

    private void loadIsAd(String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        String payload = "";
        if (extras != null && extras.containsKey(PAY_LOAD)) {
            payload = String.valueOf(extras.get(PAY_LOAD));
        }
        if (!TextUtils.isEmpty(adUnitId) && callback != null) {
            mInterstitialListeners.put(adUnitId, callback);
        }
        AdTimingInterstitialAd.setAdListener(adUnitId, this);
        AdTimingInterstitialAd.loadAdWithPayload(adUnitId, payload);
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            AdTimingInterstitialAd.setAdListener(adUnitId, this);
            AdTimingInterstitialAd.showAd(adUnitId);
        } else {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "no interstitial ad or not ready"));
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return AdTimingInterstitialAd.isReady(adUnitId);
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        AdTimingBannerManager.getInstance().addBannerAdCallback(callback);
        String appKey = (String) extras.get("AppKey");
        initSDK(appKey);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        AdTimingBannerManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return AdTimingBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        AdTimingBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            return;
        }
        AdTimingNativeManager.getInstance().addNativeAdCallback(callback);
        String appKey = (String) extras.get("AppKey");
        initSDK(appKey);
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            return;
        }
        AdTimingNativeManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adInfo, callback);
        AdTimingNativeManager.getInstance().registerNativeView(adUnitId, adView, adInfo, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        super.destroyNativeAd(adUnitId, adInfo);
        AdTimingNativeManager.getInstance().destroyAd(adUnitId, adInfo);
    }

    private void initSDK(String appKey) {
        AdTimingSingleTon.getInstance().initAdTiming(MediationUtil.getContext(), appKey, new AdTimingSingleTon.AdTimingInitCallback() {
            @Override
            public void onSuccess() {
                if (!mInitVideoListeners.isEmpty()) {
                    for (RewardedVideoCallback videoCallback : mInitVideoListeners) {
                        videoCallback.onRewardedVideoInitSuccess();
                    }
                    mInitVideoListeners.clear();
                }

                if (!mInitInterstitialListeners.isEmpty()) {
                    for (InterstitialAdCallback adCallback : mInitInterstitialListeners) {
                        adCallback.onInterstitialAdInitSuccess();
                    }
                }
                mInitInterstitialListeners.clear();

                AdTimingBannerManager.getInstance().onInitSuccess();
                AdTimingNativeManager.getInstance().onInitSuccess();
            }

            @Override
            public void onError(AdTimingError adTimingError) {
                if (!mInitVideoListeners.isEmpty()) {
                    for (RewardedVideoCallback videoCallback : mInitVideoListeners) {
                        videoCallback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, adTimingError.getCode(), adTimingError.getMessage()));
                    }
                    mInitVideoListeners.clear();
                }

                if (!mInitInterstitialListeners.isEmpty()) {
                    for (InterstitialAdCallback adCallback : mInitInterstitialListeners) {
                        adCallback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, adTimingError.getCode(), adTimingError.getMessage()));
                    }
                    mInitInterstitialListeners.clear();
                }

                AdTimingBannerManager.getInstance().onInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, adTimingError.getCode(), adTimingError.getMessage()));

                AdTimingNativeManager.getInstance().onInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, adTimingError.getCode(), adTimingError.getMessage()));
            }
        });
    }

    @Override
    public void onInterstitialAdLoadSuccess(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onInterstitialAdReady : " + placementId);
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdClosed(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onInterstitialAdClose : " + placementId);
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdClosed();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdShowed(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onInterstitialAdShowed : " + placementId);
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdShowSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdLoadFailed(String placementId, AdTimingError error) {
        try {
            AdLog.getSingleton().LogE(TAG + "InterstitialAd Load Failed: " + error);
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdClicked(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onInterstitialAdClicked : " + placementId);
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdClicked();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdShowFailed(String placementId, AdTimingError error) {
        try {
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }


    @Override
    public void onRewardedVideoAdLoadSuccess(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onVideoAdReady : " + placementId);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdClosed(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onVideoAdClose : " + placementId);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdShowed(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onVideoAdShowed : " + placementId);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdRewarded(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onVideoAdRewarded : " + placementId);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdLoadFailed(String placementId, AdTimingError error) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdClicked(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onVideoAdClicked : " + placementId);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdShowFailed(String placementId, AdTimingError error) {
        try {
            AdLog.getSingleton().LogE(TAG + "onVideoAdShowFailed : " + placementId + " cause :" + error);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdStarted(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onVideoStarted : " + placementId);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdStarted();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdEnded(String placementId) {
        try {
            AdLog.getSingleton().LogD(TAG + "onVideoEnded : " + placementId);
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
            }
        } catch (Exception ignored) {
        }
    }
}
