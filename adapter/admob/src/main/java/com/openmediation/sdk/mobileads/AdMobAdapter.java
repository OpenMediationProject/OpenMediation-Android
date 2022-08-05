// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.google.android.gms.ads.nativead.NativeAd;
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

public class AdMobAdapter extends CustomAdsAdapter {

    private final ConcurrentMap<String, RewardedAd> mRewardedAds;
    private final ConcurrentMap<String, InterstitialAd> mInterstitialAds;
    private final ConcurrentMap<String, AdView> mBannerAds;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvInitCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsInitCallbacks;
    private final ConcurrentMap<String, BannerAdCallback> mBnInitCallbacks;
    private final ConcurrentMap<String, NativeAdCallback> mNaInitCallbacks;
    private volatile InitState mInitState = InitState.NOT_INIT;

    public AdMobAdapter() {
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
        return com.openmediation.sdk.mobileads.admob.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_2;
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

    private AdRequest createAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
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

    private synchronized void initSDK() {
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

        AdMobSplashManager.getInstance().onInitSuccess();
    }

    @Override
    public void initRewardedVideo(final Activity activity, final Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check();
                    if (TextUtils.isEmpty(error)) {
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
                    } else {
                        if (callback != null) {
                            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                        }
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
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
                AdLog.getSingleton().LogD("AdMobAdapter", "RewardedAd onUserEarnedReward");
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
                AdLog.getSingleton().LogE("AdMobAdapter", "RewardedAd onAdFailedToShowFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, adError.getCode(), adError.getMessage()));
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AdLog.getSingleton().LogD("AdMobAdapter", "RewardedAd onAdShowedFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdShowSuccess();
                    callback.onRewardedVideoAdStarted();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AdLog.getSingleton().LogD("AdMobAdapter", "RewardedAd onAdDismissedFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdEnded();
                    callback.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AdLog.getSingleton().LogD("AdMobAdapter", "RewardedAd onAdClicked");
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check();
                    if (TextUtils.isEmpty(error)) {
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
                    } else {
                        if (callback != null) {
                            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                        }
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
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
                    InterstitialAd.load(MediationUtil.getContext(), adUnitId, createAdRequest(), createInterstitialListener(adUnitId, callback));
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check();
                    if (TextUtils.isEmpty(error)) {
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
                    } else {
                        if (callback != null) {
                            callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
                        }
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
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
                    AdView adView = new AdView(MediationUtil.getContext());
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
            AdView view = mBannerAds.remove(adUnitId);
            if (view != null) {
                view.destroy();
            }
        } catch (Throwable e) {
        }
    }

    @Override
    public void initNativeAd(final Activity activity, final Map<String, Object> dataMap, final NativeAdCallback callback) {
        super.initNativeAd(activity, dataMap, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check();
                    if (TextUtils.isEmpty(error)) {
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
                    } else {
                        if (callback != null) {
                            callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
                        }
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
                        }
                        return;
                    }
                    final AdMobNativeAdsConfig config = new AdMobNativeAdsConfig();
                    AdLoader.Builder builder = new AdLoader.Builder(MediationUtil.getContext(), adUnitId);
                    builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
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
                                            AdapterErrorBuilder.AD_UNIT_NATIVE, "AdMobAdapter", e.getMessage()));
                                }
                            }
                        }
                    });
                    NativeAdOptions.Builder nativeAdOptionsBuilder = new NativeAdOptions.Builder();
                    //single image
                    nativeAdOptionsBuilder.setRequestMultipleImages(false);
                    AdLoader loader = builder.withNativeAdOptions(nativeAdOptionsBuilder.build())
                            .withAdListener(createNativeAdListener(callback)).build();
                    config.setAdLoader(loader);
                    loader.loadAd(createAdRequest());
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adnAdInfo) {
        super.destroyNativeAd(adUnitId, adnAdInfo);
        if (adnAdInfo == null || !(adnAdInfo.getAdnNativeAd() instanceof AdMobNativeAdsConfig)) {
            return;
        }
        AdMobNativeAdsConfig config = (AdMobNativeAdsConfig) adnAdInfo.getAdnNativeAd();
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
        if (adnAdInfo == null || !(adnAdInfo.getAdnNativeAd() instanceof AdMobNativeAdsConfig)) {
            return;
        }
        AdMobNativeAdsConfig config = (AdMobNativeAdsConfig) adnAdInfo.getAdnNativeAd();
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

            TextView textView = new TextView(adView.getContext());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(50, 35);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            textView.setLayoutParams(layoutParams);
            textView.setBackgroundColor(Color.argb(255, 234, 234, 234));
            textView.setGravity(Gravity.CENTER);
            textView.setText("Ad");
            textView.setTextSize(10);
            textView.setTextColor(Color.argb(255, 45, 174, 201));
            googleAdView.setAdvertiserView(textView);
            googleAdView.setNativeAd(config.getAdMobNativeAd());

            textView.bringToFront();
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
            AdLog.getSingleton().LogE("AdMobAdapter registerNativeAdView error: " + e.getMessage());
        }
    }

    @Override
    public void initSplashAd(final Activity activity, final Map<String, Object> extras, final SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check();
                    if (TextUtils.isEmpty(error)) {
                        switch (mInitState) {
                            case NOT_INIT:
                                AdMobSplashManager.getInstance().addAdCallback(callback);
                                initSDK();
                                break;
                            case INIT_PENDING:
                                AdMobSplashManager.getInstance().addAdCallback(callback);
                                break;
                            case INIT_SUCCESS:
                                if (callback != null) {
                                    callback.onSplashAdInitSuccess();
                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (callback != null) {
                            callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
                        }
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
        AdMobSplashManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, mUserConsent, mUSPrivacyLimit, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return AdMobSplashManager.getInstance().isAdAvailable(adUnitId);
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
        AdMobSplashManager.getInstance().showAd(activity, adUnitId, viewGroup, callback);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        AdMobSplashManager.getInstance().destroyAd(adUnitId);
    }

    private InterstitialAdLoadCallback createInterstitialListener(final String adUnitId, final InterstitialAdCallback callback) {
        return new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
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
                AdLog.getSingleton().LogE("AdMobAdapter", "InterstitialAd onAdFailedToShowFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, adError.getCode(), adError.getMessage()));
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AdLog.getSingleton().LogD("AdMobAdapter", "InterstitialAd onAdShowedFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AdLog.getSingleton().LogD("AdMobAdapter", "InterstitialAd onAdDismissedFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AdLog.getSingleton().LogD("AdMobAdapter", "InterstitialAd onAdClicked");
                if (callback != null) {
                    callback.onInterstitialAdClicked();
                }
            }
        };

    }

    private AdListener createBannerAdListener(final AdView adView, final BannerAdCallback callback) {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                AdLog.getSingleton().LogD("AdMobAdapter", "BannerAd onAdLoaded");
                if (callback != null) {
                    callback.onBannerAdLoadSuccess(adView);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                AdLog.getSingleton().LogE("AdMobAdapter", "BannerAd onAdFailedToLoad : " + loadAdError.toString());
                if (callback != null) {
                    callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AdLog.getSingleton().LogD("AdMobAdapter", "BannerAd onAdClicked");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                AdLog.getSingleton().LogD("AdMobAdapter", "BannerAd onAdImpression");
                if (callback != null) {
                    callback.onBannerAdImpression();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                AdLog.getSingleton().LogD("AdMobAdapter", "BannerAd onAdOpened");
                if (callback != null) {
                    callback.onBannerAdAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdLog.getSingleton().LogD("AdMobAdapter", "BannerAd onAdClosed");
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
