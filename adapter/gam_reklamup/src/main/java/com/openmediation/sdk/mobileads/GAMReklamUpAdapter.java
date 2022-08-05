// Copyright 2022 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
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
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GAMReklamUpAdapter extends CustomAdsAdapter {

    private final ConcurrentMap<String, RewardedAd> mRewardedAds;
    private final ConcurrentMap<String, AdManagerInterstitialAd> mInterstitialAds;
    private final ConcurrentMap<String, AdManagerAdView> mBannerAds;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvInitCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsInitCallbacks;
    private final ConcurrentMap<String, BannerAdCallback> mBnInitCallbacks;
    private final ConcurrentMap<String, NativeAdCallback> mNaInitCallbacks;
    private volatile InitState mInitState = InitState.NOT_INIT;

    public GAMReklamUpAdapter() {
        mRewardedAds = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mRvInitCallbacks = new ConcurrentHashMap<>();
        mIsInitCallbacks = new ConcurrentHashMap<>();
        mBnInitCallbacks = new ConcurrentHashMap<>();
        mNaInitCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return MobileAds.getVersion().toString();
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.gamreklamup.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_31;
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        int value = restricted ? MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE : MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;
        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
                .setTagForChildDirectedTreatment(value)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        setAgeRestricted(context, age < 13);
    }

    private AdManagerAdRequest createAdRequest() {
        AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        if (mUserConsent != null || mUSPrivacyLimit != null) {
            Bundle extras = new Bundle();
            if (mUserConsent != null && !mUserConsent) {
                extras.putString("npa", "1");
            }
            if (mUSPrivacyLimit != null) {
                extras.putInt("rdp", mUSPrivacyLimit ? 1 : 0);
            }
            builder.addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter.class, extras);
        }
        return builder.build();
    }

    private void initSDK() {
        mInitState = InitState.INIT_PENDING;
        MobileAds.initialize(MediationUtil.getContext());
        onInitSuccess();
    }

    private void onInitSuccess() {
        mInitState = InitState.INIT_SUCCESS;
        for (InterstitialAdCallback callback : mIsInitCallbacks.values()) {
            callback.onInterstitialAdInitSuccess();
        }
        mIsInitCallbacks.clear();
        for (RewardedVideoCallback callback : mRvInitCallbacks.values()) {
            callback.onRewardedVideoInitSuccess();
        }
        mRvInitCallbacks.clear();
        for (BannerAdCallback callback : mBnInitCallbacks.values()) {
            callback.onBannerAdInitSuccess();
        }
        mBnInitCallbacks.clear();
        for (NativeAdCallback callback : mNaInitCallbacks.values()) {
            callback.onNativeAdInitSuccess();
        }
        mNaInitCallbacks.clear();

        GAMReklamUpSplashManager.getInstance().onInitSuccess();
    }

    /*********************************RewardedVideoAd***********************************/
    @Override
    public void initRewardedVideo(final Activity activity, final Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (mInitState) {
                        case NOT_INIT:
                            if (dataMap.get("pid") != null && callback != null) {
                                mRvInitCallbacks.put((String) dataMap.get("pid"), callback);
                            }
                            initSDK();
                            break;
                        case INIT_PENDING:
                            if (dataMap.get("pid") != null && callback != null) {
                                mRvInitCallbacks.put((String) dataMap.get("pid"), callback);
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
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Init Failed: Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void loadRewardedVideo(final Activity activity, final String adUnitId, Map<String, Object> extras, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                        }
                        return;
                    }
                    if (isRewardedVideoAvailable(adUnitId)) {
                        callback.onRewardedVideoLoadSuccess();
                        return;
                    }
                    RewardedAd.load(MediationUtil.getContext(), adUnitId, createAdRequest(), createRvLoadListener(adUnitId, callback));
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void showRewardedVideo(final Activity activity, final String adUnitId
            , final RewardedVideoCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    showAdMobVideo(activity, adUnitId, callback);
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    private void showAdMobVideo(Activity activity, String adUnitId, final RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (!isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Ad Not Ready"));
            }
            return;
        }
        RewardedAd rewardedAd = mRewardedAds.get(adUnitId);
        if (rewardedAd == null) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Ad Not Ready"));
            }
            return;
        }
        rewardedAd.setFullScreenContentCallback(createRvCallback(callback));
        rewardedAd.show(activity, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "RewardedAd onUserEarnedReward");
                if (callback != null) {
                    callback.onRewardedVideoAdRewarded();
                }
            }
        });
        mRewardedAds.remove(adUnitId);
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mRewardedAds.containsKey(adUnitId);
    }

    private RewardedAdLoadCallback createRvLoadListener(final String adUnitId, final RewardedVideoCallback callback) {
        return new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd ad) {
                super.onAdLoaded(ad);
                mRewardedAds.put(adUnitId, ad);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                super.onAdFailedToLoad(error);
                mRewardedAds.remove(adUnitId);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getCode(), error.getMessage()));
                }
            }
        };
    }

    private FullScreenContentCallback createRvCallback(final RewardedVideoCallback callback) {
        return new FullScreenContentCallback() {

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                AdLog.getSingleton().LogE("GAMReklamUpAdapter", "RewardedAd onAdFailedToShowFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, adError.getCode(), adError.getMessage()));
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "RewardedAd onAdShowedFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdShowSuccess();
                    callback.onRewardedVideoAdStarted();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "RewardedAd onAdDismissedFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdEnded();
                    callback.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "RewardedAd onAdClicked");
                if (callback != null) {
                    callback.onRewardedVideoAdClicked();
                }
            }
        };
    }

    /*********************************Interstitial***********************************/
    @Override
    public void initInterstitialAd(final Activity activity, final Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (mInitState) {
                        case NOT_INIT:
                            if (dataMap.get("pid") != null && callback != null) {
                                mIsInitCallbacks.put((String) dataMap.get("pid"), callback);
                            }
                            initSDK();
                            break;
                        case INIT_PENDING:
                            if (dataMap.get("pid") != null && callback != null) {
                                mIsInitCallbacks.put((String) dataMap.get("pid"), callback);
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
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(final Activity activity, final String adUnitId, Map<String, Object> extras, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                        }
                        return;
                    }
                    if (isInterstitialAdAvailable(adUnitId)) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadSuccess();
                        }
                        return;
                    }
                    AdManagerInterstitialAd.load(MediationUtil.getContext(), adUnitId, createAdRequest(), createInterstitialListener(adUnitId, callback));
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void showInterstitialAd(final Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    showAdMobInterstitial(activity, adUnitId, callback);
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    private void showAdMobInterstitial(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (!isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Ad Not Ready"));
            }
            return;
        }
        InterstitialAd ad = mInterstitialAds.get(adUnitId);
        if (ad != null) {
            ad.setFullScreenContentCallback(createIsCallback(callback));
            ad.show(activity);
        }
        mInterstitialAds.remove(adUnitId);
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mInterstitialAds.containsKey(adUnitId);
    }

    @Override
    public void initBannerAd(final Activity activity, final Map<String, Object> dataMap, final BannerAdCallback callback) {
        super.initBannerAd(activity, dataMap, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (mInitState) {
                        case NOT_INIT:
                            if (dataMap.get("pid") != null && callback != null) {
                                mBnInitCallbacks.put((String) dataMap.get("pid"), callback);
                            }
                            initSDK();
                            break;
                        case INIT_PENDING:
                            if (dataMap.get("pid") != null && callback != null) {
                                mBnInitCallbacks.put((String) dataMap.get("pid"), callback);
                            }
                            break;
                        case INIT_SUCCESS:
                            if (callback != null) {
                                callback.onBannerAdInitSuccess();
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void loadBannerAd(final Activity activity, final String adUnitId, final Map<String, Object> extras, final BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
                        }
                        return;
                    }
                    AdManagerAdView adView = new AdManagerAdView(MediationUtil.getContext());
                    adView.setAdUnitId(adUnitId);
                    AdSize adSize = getAdSize(extras);
                    if (adSize != null) {
                        adView.setAdSize(adSize);
                    }
                    adView.setAdListener(createBannerAdListener(adView, callback));
                    mBannerAds.put(adUnitId, adView);
                    adView.loadAd(createAdRequest());
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        if (!mBannerAds.containsKey(adUnitId)) {
            return;
        }
        try {
            AdManagerAdView view = mBannerAds.remove(adUnitId);
            if (view != null) {
                view.destroy();
            }
        } catch (Throwable e) {
        }
    }

    @Override
    public void initNativeAd(final Activity activity, final Map<String, Object> dataMap, final NativeAdCallback callback) {
        super.initNativeAd(activity, dataMap, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (mInitState) {
                        case NOT_INIT:
                            if (dataMap.get("pid") != null && callback != null) {
                                mNaInitCallbacks.put((String) dataMap.get("pid"), callback);
                            }
                            initSDK();
                            break;
                        case INIT_PENDING:
                            if (dataMap.get("pid") != null && callback != null) {
                                mNaInitCallbacks.put((String) dataMap.get("pid"), callback);
                            }
                            break;
                        case INIT_SUCCESS:
                            if (callback != null) {
                                callback.onNativeAdInitSuccess();
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void loadNativeAd(final Activity activity, final String adUnitId, final Map<String, Object> extras, final NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        MediationUtil.runOnUiThread(() -> {
            try {
                String error = check(adUnitId);
                if (!TextUtils.isEmpty(error)) {
                    if (callback != null) {
                        callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
                    }
                    return;
                }
                final GAMReklamUpNativeAdsConfig config = new GAMReklamUpNativeAdsConfig();
                AdLoader.Builder builder = new AdLoader.Builder(MediationUtil.getContext(), adUnitId);
                builder.forNativeAd(nativeAd -> {
                    try {
                        config.setAdMobNativeAd(nativeAd);
                        AdnAdInfo info = new AdnAdInfo();
                        info.setAdnNativeAd(config);
                        info.setType(getAdNetworkId());
                        info.setTitle(nativeAd.getHeadline());
                        info.setDesc(nativeAd.getBody());
                        info.setCallToActionText(nativeAd.getCallToAction());
                        if (callback != null) {
                            callback.onNativeAdLoadSuccess(info);
                        }
                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_NATIVE, "GAMReklamUpAdapter", e.getMessage()));
                        }
                    }
                });
                NativeAdOptions.Builder nativeAdOptionsBuilder = new NativeAdOptions.Builder();
                //single image
                nativeAdOptionsBuilder.setRequestMultipleImages(false);
                AdLoader loader = builder.withNativeAdOptions(nativeAdOptionsBuilder.build())
                        .withAdListener(createNativeAdListener(callback)).build();
                loader.loadAd(createAdRequest());
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "Unknown Error, " + e.getMessage()));
                }
            }
        });
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adnAdInfo) {
        super.destroyNativeAd(adUnitId, adnAdInfo);
        if (adnAdInfo == null || !(adnAdInfo.getAdnNativeAd() instanceof GAMReklamUpNativeAdsConfig)) {
            return;
        }
        GAMReklamUpNativeAdsConfig config = (GAMReklamUpNativeAdsConfig) adnAdInfo.getAdnNativeAd();
        if (config == null) {
            return;
        }
        try {
            if (config.getAdMobNativeAd() != null) {
                config.getAdMobNativeAd().destroy();
            }
            if (config.getUnifiedNativeAdView() != null) {
                config.getUnifiedNativeAdView().removeAllViews();
                config.getUnifiedNativeAdView().destroy();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adnAdInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adnAdInfo, callback);
        if (adnAdInfo == null || !(adnAdInfo.getAdnNativeAd() instanceof GAMReklamUpNativeAdsConfig)) {
            return;
        }
        GAMReklamUpNativeAdsConfig config = (GAMReklamUpNativeAdsConfig) adnAdInfo.getAdnNativeAd();
        if (config == null) {
            return;
        }
        if (config.getAdMobNativeAd() == null) {
            return;
        }

        try {
            com.google.android.gms.ads.nativead.NativeAdView googleAdView =
                    new com.google.android.gms.ads.nativead.NativeAdView(adView.getContext());
            if (adView.getTitleView() != null) {
                googleAdView.setHeadlineView(adView.getTitleView());
            }
            if (adView.getDescView() != null) {
                googleAdView.setBodyView(adView.getDescView());
            }
            if (adView.getCallToActionView() != null) {
                googleAdView.setCallToActionView(adView.getCallToActionView());
            }
            if (adView.getMediaView() != null) {
                adView.getMediaView().removeAllViews();
                com.google.android.gms.ads.nativead.MediaView adMobMediaView = new
                        com.google.android.gms.ads.nativead.MediaView(adView.getContext());
                adView.getMediaView().addView(adMobMediaView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adMobMediaView.setLayoutParams(layoutParams);
                googleAdView.setMediaView(adMobMediaView);
            }

            if (adView.getAdIconView() != null && config.getAdMobNativeAd().getIcon() != null
                    && config.getAdMobNativeAd().getIcon().getDrawable() != null) {
                adView.getAdIconView().removeAllViews();
                ImageView iconImageView = new ImageView(adView.getContext());
                adView.getAdIconView().addView(iconImageView);
                iconImageView.setImageDrawable(config.getAdMobNativeAd().getIcon().getDrawable());
                iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                googleAdView.setIconView(adView.getAdIconView());
            }

            googleAdView.setNativeAd(config.getAdMobNativeAd());
            if (googleAdView.getAdChoicesView() != null) {
                googleAdView.getAdChoicesView().bringToFront();
            }
            config.setUnifiedNativeAdView(googleAdView);
            int count = adView.getChildCount();
            if (count > 0) {
                View actualView = adView.getChildAt(0);
                adView.removeView(actualView);
                googleAdView.addView(actualView);
                adView.addView(googleAdView);
            }
        } catch (Throwable e) {
            AdLog.getSingleton().LogE("GAMReklamUpAdapter registerNativeAdView error: " + e.getMessage());
        }
    }

    @Override
    public void initSplashAd(final Activity activity, final Map<String, Object> extras, final SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (mInitState) {
                        case NOT_INIT:
                            GAMReklamUpSplashManager.getInstance().addAdCallback(callback);
                            initSDK();
                            break;
                        case INIT_PENDING:
                            GAMReklamUpSplashManager.getInstance().addAdCallback(callback);
                            break;
                        case INIT_SUCCESS:
                            if (callback != null) {
                                callback.onSplashAdInitSuccess();
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        super.loadSplashAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        GAMReklamUpSplashManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, mUserConsent, mUSPrivacyLimit, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return GAMReklamUpSplashManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        GAMReklamUpSplashManager.getInstance().showAd(activity, adUnitId, viewGroup, callback);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        GAMReklamUpSplashManager.getInstance().destroyAd(adUnitId);
    }

    private AdManagerInterstitialAdLoadCallback createInterstitialListener(final String adUnitId, final InterstitialAdCallback callback) {
        return new AdManagerInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAds.put(adUnitId, interstitialAd);
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAds.remove(adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
                }
            }
        };
    }

    private FullScreenContentCallback createIsCallback(final InterstitialAdCallback callback) {
        return new FullScreenContentCallback() {

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                AdLog.getSingleton().LogE("GAMReklamUpAdapter", "InterstitialAd onAdFailedToShowFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, adError.getCode(), adError.getMessage()));
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "InterstitialAd onAdShowedFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "InterstitialAd onAdDismissedFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "InterstitialAd onAdClicked");
                if (callback != null) {
                    callback.onInterstitialAdClicked();
                }
            }
        };

    }

    private AdListener createBannerAdListener(final AdManagerAdView adView, final BannerAdCallback callback) {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "BannerAd onAdLoaded");
                if (callback != null) {
                    callback.onBannerAdLoadSuccess(adView);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                AdLog.getSingleton().LogE("GAMReklamUpAdapter", "BannerAd onAdFailedToLoad : " + loadAdError.toString());
                if (callback != null) {
                    callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "BannerAd onAdClicked");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "BannerAd onAdImpression");
                if (callback != null) {
                    callback.onBannerAdImpression();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "BannerAd onAdOpened");
                if (callback != null) {
                    callback.onBannerAdAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdLog.getSingleton().LogD("GAMReklamUpAdapter", "BannerAd onAdClosed");
            }
        };
    }

    private AdListener createNativeAdListener(final NativeAdCallback callback) {
        return new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (callback != null) {
                    callback.onNativeAdAdClicked();
                }
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                if (callback != null) {
                    callback.onNativeAdImpression();
                }
            }
        };
    }

    private AdSize getAdSize(Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_LEADERBOARD:
                return AdSize.LEADERBOARD;
            case MediationUtil.DESC_RECTANGLE:
                return AdSize.MEDIUM_RECTANGLE;
            case MediationUtil.DESC_SMART:
                return AdSize.SMART_BANNER;
            default:
                return AdSize.BANNER;
        }
    }

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
        INIT_SUCCESS
    }
}
