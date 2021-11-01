// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

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
import com.openmediation.sdk.mobileads.flatads.BuildConfig;
import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public class FlatAdsAdapter extends CustomAdsAdapter {

    public static String BID = "Bid";

    public FlatAdsAdapter() {
    }

    @Override
    public String getMediationVersion() {
        return "";
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_25;
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        FlatAdsSingleTon.getInstance().onResume();
        FlatAdsNativeManager.getInstance().onResume();
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        FlatAdsSingleTon.getInstance().onPause();
        FlatAdsNativeManager.getInstance().onPause();
    }

    @Override
    public boolean isAdNetworkInit() {
        return FlatAdsSingleTon.getInstance().isInit();
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        FlatAdsBannerManager.getInstance().init(mAppKey, callback);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        FlatAdsBannerManager.getInstance().loadAd(activity, adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return FlatAdsBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        FlatAdsBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        FlatAdsNativeManager.getInstance().initAd(extras, callback);
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
        FlatAdsNativeManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, callback);
        FlatAdsNativeManager.getInstance().registerNativeView(adUnitId, adView, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId) {
        super.destroyNativeAd(adUnitId);
        FlatAdsNativeManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, extras, callback);
        FlatAdsSingleTon.getInstance().init(mAppKey, new FlatAdsSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                }
            }
        });
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
            return;
        }
        boolean bid = false;
        if (extras.containsKey(BID) && extras.get(BID) instanceof Integer) {
            bid = ((int) extras.get(BID)) == 1;
        }
        if (bid) {
            FlatAdsSingleTon.getInstance().loadInterstitialAdWithBid(adUnitId, callback);
        } else {
            FlatAdsSingleTon.getInstance().loadInterstitialAd(adUnitId, callback);
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return FlatAdsSingleTon.getInstance().isInterstitialAdReady(adUnitId);
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        FlatAdsSingleTon.getInstance().showInterstitialAd(adUnitId, new InnerInterstitialCallback(callback));
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, extras, callback);
        FlatAdsSingleTon.getInstance().init(mAppKey, new FlatAdsSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
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
            return;
        }

        boolean bid = false;
        if (extras.containsKey(BID) && extras.get(BID) instanceof Integer) {
            bid = ((int) extras.get(BID)) == 1;
        }
        if (bid) {
            FlatAdsSingleTon.getInstance().loadRewardedVideoWithBid(adUnitId, callback);
        } else {
            FlatAdsSingleTon.getInstance().loadRewardedVideo(adUnitId, callback);
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return FlatAdsSingleTon.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (isRewardedVideoAvailable(adUnitId)) {
            InnerVideoCallback videoCallback = new InnerVideoCallback(callback);
            FlatAdsSingleTon.getInstance().showRewardedVideo(adUnitId, videoCallback);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "FlatAds RewardedVideo is not ready"));
            }
        }
    }

    private class InnerInterstitialCallback implements FlatAdsInterstitialCallback {

        InterstitialAdCallback mCallback;

        InnerInterstitialCallback(InterstitialAdCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onInterstitialOpened(String adUnitId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onInterstitialDismissed(String adUnitId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onInterstitialClick(String adUnitId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClicked();
            }
        }
    }

    private class InnerVideoCallback implements FlatAdsVideoCallback {

        RewardedVideoCallback mCallback;

        InnerVideoCallback(RewardedVideoCallback videoCallback) {
            mCallback = videoCallback;
        }

        @Override
        public void onRewardedOpened(String adUnitId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowSuccess();
                mCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onRewardedClosed(String adUnitId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdEnded();
                mCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onRewardedReward(String adUnitId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onRewardedShowFailed(String adUnitId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Show Failed"));
            }
        }

        @Override
        public void onRewardedClicked(String adUnitId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClicked();
            }
        }
    }
}
