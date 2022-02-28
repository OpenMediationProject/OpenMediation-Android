// Copyright 2022 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.RequestOptions;
import com.huawei.hms.ads.TagForChild;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.mobileads.hwads.BuildConfig;
import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public class HwAdsAdapter extends CustomAdsAdapter {

    public HwAdsAdapter() {
    }

    @Override
    public String getMediationVersion() {
        return HwAds.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_28;
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
//        HwAdsSplashManager.getInstance().onPause();
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
//        HwAdsSplashManager.getInstance().onResume();
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        int tag = restricted ? TagForChild.TAG_FOR_CHILD_PROTECTION_TRUE : TagForChild.TAG_FOR_CHILD_PROTECTION_FALSE;
        RequestOptions requestOptions = HwAds.getRequestOptions().toBuilder().setTagForChildProtection(tag).build();
        HwAds.setRequestOptions(requestOptions);
    }

    @Override
    public void initRewardedVideo(final Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        final String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSingleTon.getInstance().initSDK(MediationUtil.getContext(), new HwAdsSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", msg));
                }
            }
        });
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSingleTon.getInstance().loadRewardedVideo(MediationUtil.getContext(), adUnitId, callback);
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return HwAdsSingleTon.getInstance().isRewardedVideoAvailable(adUnitId);
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, final RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSingleTon.getInstance().showRewardedVideo(activity, adUnitId, callback);
    }

    @Override
    public void initInterstitialAd(final Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSingleTon.getInstance().initSDK(MediationUtil.getContext(), new HwAdsSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HwAdsAdapter", msg));
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSingleTon.getInstance().loadInterstitialAd(MediationUtil.getContext(), adUnitId, callback);
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return HwAdsSingleTon.getInstance().isInterstitialAdAvailable(adUnitId);
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSingleTon.getInstance().showInterstitialAd(activity, adUnitId, callback);
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsBannerManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsBannerManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return HwAdsBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        HwAdsBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initSplashAd(Activity activity, Map<String, Object> extras, SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSplashManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        super.loadSplashAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSplashManager.getInstance().loadAd(activity, adUnitId, extras, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return HwAdsSplashManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsSplashManager.getInstance().showAd(adUnitId, viewGroup, callback);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        HwAdsSplashManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsNativeManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "HwAdsAdapter", error));
            }
            return;
        }
        HwAdsNativeManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adnAdInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adnAdInfo, callback);
        HwAdsNativeManager.getInstance().registerNativeView(adUnitId, adView, adnAdInfo, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adnAdInfo) {
        super.destroyNativeAd(adUnitId, adnAdInfo);
        HwAdsNativeManager.getInstance().destroyAd(adUnitId, adnAdInfo);
    }

}
