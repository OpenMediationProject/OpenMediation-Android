/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.admost.BuildConfig;
import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdmostAdapter extends CustomAdsAdapter implements AdmostInterstitialCallback, AdmostVideoCallback {

    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    public AdmostAdapter() {
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return admost.sdk.BuildConfig.VERSION_NAME;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_24;
    }

    @Override
    public boolean isAdNetworkInit() {
        return AdmostSingleTon.getInstance().isInit();
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        AdmostSingleTon.getInstance().onResume();
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        AdmostSingleTon.getInstance().onPause();
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, extras, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            init(activity, mAppKey, new AdmostSingleTon.InitListener() {
                @Override
                public void initSuccess() {
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                }

                @Override
                public void initFailed(int code, String error) {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, code, error));
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
            if (callback != null) {
                String error = AdmostSingleTon.getInstance().getError(adUnitId);
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return AdmostSingleTon.getInstance().isInterstitialReady(adUnitId);
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            AdmostSingleTon.getInstance().setInterstitialAdCallback(this);
            AdmostSingleTon.getInstance().showInterstitial(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "AdMost interstitial is not ready"));
            }
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, extras, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        init(activity, mAppKey, new AdmostSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }

            @Override
            public void initFailed(int code, String error) {
                if (callback != null) {
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                }
            }
        });
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String checkError = check(adUnitId);
        if (!TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
            return;
        }
        if (isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            if (callback != null) {
                String error = AdmostSingleTon.getInstance().getError(adUnitId);
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return AdmostSingleTon.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                mRvCallbacks.put(adUnitId, callback);
            }
            AdmostSingleTon.getInstance().setVideoAdCallback(this);
            AdmostSingleTon.getInstance().showRewardedVideo(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "AdMost RewardedVideo is not ready"));
            }
        }
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            AdmostBannerManager.getInstance().init(activity, mAppKey, callback);
        } else {
            callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
        }
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        AdmostBannerManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return AdmostSingleTon.getInstance().isBannerAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        AdmostSingleTon.getInstance().destroyBannerAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            AdmostNativeManager.getInstance().init(activity, mAppKey, callback);
        } else {
            callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
        }
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            return;
        }
        AdmostNativeManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, callback);
        AdmostNativeManager.getInstance().registerView(adUnitId, adView, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId) {
        super.destroyNativeAd(adUnitId);
        AdmostSingleTon.getInstance().destroyBannerAd(adUnitId);
    }

    private synchronized void init(Activity activity, String appKey, AdmostSingleTon.InitListener listener) {
        AdmostSingleTon.getInstance().init(activity, appKey, listener);
    }

    @Override
    public void onInterstitialOpened(String placementId) {
        InterstitialAdCallback listener = mIsCallbacks.get(placementId);
        if (listener == null) {
            return;
        }
        listener.onInterstitialAdShowSuccess();
    }

    @Override
    public void onInterstitialDismissed(String placementId) {
        InterstitialAdCallback listener = mIsCallbacks.get(placementId);
        if (listener == null) {
            return;
        }
        listener.onInterstitialAdClosed();
    }

    @Override
    public void onInterstitialClick(String placementId) {
        InterstitialAdCallback listener = mIsCallbacks.get(placementId);
        if (listener == null) {
            return;
        }
        listener.onInterstitialAdClicked();
    }

    @Override
    public void onRewardedOpened(String placementId) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener == null) {
            return;
        }

        listener.onRewardedVideoAdShowSuccess();
        listener.onRewardedVideoAdStarted();
    }

    @Override
    public void onRewardedClosed(String placementId) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener == null) {
            return;
        }

        listener.onRewardedVideoAdClosed();
    }

    @Override
    public void onRewardedComplete(String placementId) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener != null) {
            listener.onRewardedVideoAdRewarded();
            listener.onRewardedVideoAdEnded();
        }
    }

    @Override
    public void onRewardedClick(String placementId) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener != null) {
            listener.onRewardedVideoAdClicked();
        }
    }
}
