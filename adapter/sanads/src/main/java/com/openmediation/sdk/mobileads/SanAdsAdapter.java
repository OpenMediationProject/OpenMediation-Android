// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.sanads.BuildConfig;
import com.openmediation.sdk.nativead.NativeAdView;
import com.san.api.SanAdSdk;

import java.util.Map;

/**
 * https://github.com/san-sdk/sample/wiki
 */
public class SanAdsAdapter extends CustomAdsAdapter {
    private static final String TAG = "OM-SanAds: ";

    public SanAdsAdapter() {
    }

    @Override
    public String getMediationVersion() {
        return SanAdSdk.getSdkVerName();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_27;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        SanAdSdk.notifyConsentStatus(context, consent);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            return;
        }
        SanAdsSingleTon.getInstance().init(new SanAdsSingleTon.InitListener() {
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
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            return;
        }
        SanAdsSingleTon.getInstance().loadRewardedVideo(adUnitId, callback);
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
            SanAdsSingleTon.getInstance().showRewardedVideo(adUnitId, callback);
        } else {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "No Fill"));
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return SanAdsSingleTon.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        SanAdsSingleTon.getInstance().init(new SanAdsSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        SanAdsSingleTon.getInstance().loadInterstitial(adUnitId, callback);
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
            SanAdsSingleTon.getInstance().showInterstitial(adUnitId, callback);
        } else {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "No Fill"));
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return SanAdsSingleTon.getInstance().isInterstitialReady(adUnitId);
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, final BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        SanAdsSingleTon.getInstance().init(new SanAdsSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
                }
            }
        });
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
        SanAdsSingleTon.getInstance().loadBanner(adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return SanAdsSingleTon.getInstance().isBannerAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        SanAdsSingleTon.getInstance().destroyBannerAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, final NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            return;
        }
        SanAdsSingleTon.getInstance().init(new SanAdsSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onNativeAdInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "SanAdsAdapter", error));
                }
            }
        });
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
        SanAdsSingleTon.getInstance().loadNative(adUnitId, callback);
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adInfo, callback);
        SanAdsSingleTon.getInstance().registerNativeView(adUnitId, adView, adInfo, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        super.destroyNativeAd(adUnitId, adInfo);
        SanAdsSingleTon.getInstance().destroyNativeAd(adUnitId, adInfo);
    }

}
