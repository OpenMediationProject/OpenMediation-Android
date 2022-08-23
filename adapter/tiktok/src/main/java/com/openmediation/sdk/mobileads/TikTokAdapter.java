// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.api.init.PAGSdk;
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd;
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadListener;
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardItem;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAd;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdLoadListener;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedRequest;
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

public class TikTokAdapter extends CustomAdsAdapter {
    private static String TAG = "OM-TikTok: ";
    private ConcurrentMap<String, PAGRewardedAd> mTTRvAds;
    private ConcurrentMap<String, PAGInterstitialAd> mTTFvAds;

    public TikTokAdapter() {
        mTTRvAds = new ConcurrentHashMap<>();
        mTTFvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return PAGSdk.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.tiktok.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_13;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        initSdk(new TTAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, code, msg));
                }
            }
        });
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        try {
            loadRvAd(adUnitId, callback);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private void loadRvAd(String adUnitId, RewardedVideoCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            PAGRewardedRequest request = new PAGRewardedRequest();
            PAGRewardedAd.loadAd(adUnitId, request, new PAGRewardedAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    AdLog.getSingleton().LogD("TikTokAdapter, Rewarded load onError code: " + code + ", message: " + message);
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "TikTokAdapter", code, message));
                    }
                }

                @Override
                public void onAdLoaded(PAGRewardedAd ad) {
                    AdLog.getSingleton().LogD("TikTokAdapter, Rewarded onAdLoaded: " + ad);
                    if (ad == null) {
                        if (callback != null) {
                            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "TikTokAdapter", "TikTok RewardedVideo load failed"));
                        }
                        return;
                    }
                    mTTRvAds.put(adUnitId, ad);
                    if (callback != null) {
                        callback.onRewardedVideoLoadSuccess();
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void showRewardedVideo(final Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                        }
                        return;
                    }
                    PAGRewardedAd rewardedVideoAd = mTTRvAds.get(adUnitId);
                    if (rewardedVideoAd != null) {
                        rewardedVideoAd.setAdInteractionListener(new InnerRvAdShowListener(callback));
                        rewardedVideoAd.show(activity);
                        mTTRvAds.remove(adUnitId);
                    } else {
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "TikTok RewardedVideo is not ready"));
                        }
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mTTRvAds.get(adUnitId) != null;
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        initSdk(new TTAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, code, msg));
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras,
                                   InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        try {
            loadInterstitial(MediationUtil.getContext(), adUnitId, callback);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private void loadInterstitial(Context context, String adUnitId, InterstitialAdCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            PAGInterstitialRequest request = new PAGInterstitialRequest();
            PAGInterstitialAd.loadAd(adUnitId, request, new PAGInterstitialAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    AdLog.getSingleton().LogD("TikTokAdapter, Interstitial load onError code: " + code + ", message: " + message);
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, code, message));
                    }
                }

                @Override
                public void onAdLoaded(PAGInterstitialAd ad) {
                    AdLog.getSingleton().LogD("TikTokAdapter, Interstitial onAdLoaded: " + ad);
                    if (ad == null) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "TikTok InterstitialAd ad load failed"));
                        }
                        return;
                    }
                    mTTFvAds.put(adUnitId, ad);
                    if (callback != null) {
                        callback.onInterstitialAdLoadSuccess();
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void showInterstitialAd(final Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                        }
                        return;
                    }
                    PAGInterstitialAd ad = mTTFvAds.get(adUnitId);
                    if (ad != null) {
                        ad.setAdInteractionListener(new PAGInterstitialAdInteractionListener() {
                            @Override
                            public void onAdShowed() {
                                AdLog.getSingleton().LogD("TikTokAdapter, Interstitial onAdShowed");
                                if (callback != null) {
                                    callback.onInterstitialAdShowSuccess();
                                }
                            }

                            @Override
                            public void onAdClicked() {
                                AdLog.getSingleton().LogD("TikTokAdapter, Interstitial onAdClicked");
                                if (callback != null) {
                                    callback.onInterstitialAdClicked();
                                }
                            }

                            @Override
                            public void onAdDismissed() {
                                AdLog.getSingleton().LogD("TikTokAdapter, Interstitial onAdDismissed");
                                if (callback != null) {
                                    callback.onInterstitialAdClosed();
                                }
                            }
                        });
                        ad.show(activity);
                        mTTFvAds.remove(adUnitId);
                    } else {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "TikTok InterstitialAd is not ready"));
                        }
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mTTFvAds.get(adUnitId) != null;
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
        TikTokBannerManager.getInstance().initAd(MediationUtil.getContext(), extras, mUserConsent, mAgeRestricted, mUSPrivacyLimit, callback);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        TikTokBannerManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return TikTokBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        TikTokBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
            return;
        }
        TikTokNativeManager.getInstance().initAd(MediationUtil.getContext(), extras, mUserConsent, mAgeRestricted, mUSPrivacyLimit, callback);
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
            return;
        }
        TikTokNativeManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adInfo, callback);
        TikTokNativeManager.getInstance().registerView(adUnitId, adView, adInfo, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        super.destroyNativeAd(adUnitId, adInfo);
        TikTokNativeManager.getInstance().destroyAd(adUnitId, adInfo);
    }

    @Override
    public void initSplashAd(Activity activity, Map<String, Object> extras, SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        TikTokSplashManager.getInstance().initAd(MediationUtil.getContext(), extras, mUserConsent, mAgeRestricted, mUSPrivacyLimit, callback);
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        super.loadSplashAd(activity, adUnitId, extras, callback);
        TikTokSplashManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        TikTokSplashManager.getInstance().showAd(adUnitId, activity, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return TikTokSplashManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        TikTokSplashManager.getInstance().destroyAd(adUnitId);
    }

    private void initSdk(TTAdManagerHolder.InitCallback callback) {
        TTAdManagerHolder.getInstance().init(MediationUtil.getContext(), mAppKey, mUserConsent, mAgeRestricted, mUSPrivacyLimit, callback);
    }

    private static class InnerRvAdShowListener implements PAGRewardedAdInteractionListener {

        private RewardedVideoCallback callback;

        private InnerRvAdShowListener(RewardedVideoCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onUserEarnedReward(PAGRewardItem pagRewardItem) {
            AdLog.getSingleton().LogD("TikTokAdapter, Rewarded onUserEarnedReward");
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onUserEarnedRewardFail(int i, String s) {
            AdLog.getSingleton().LogD("TikTokAdapter, Rewarded onUserEarnedRewardFail, code: " + i + ", message: " + s);
        }

        @Override
        public void onAdShowed() {
            AdLog.getSingleton().LogD("TikTokAdapter, Rewarded onAdShowed");

            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onAdClicked() {
            AdLog.getSingleton().LogD("TikTokAdapter, Rewarded onAdClicked");
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onAdDismissed() {
            AdLog.getSingleton().LogD("TikTokAdapter, Rewarded onAdDismissed");
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdClosed();
            }
        }
    }

}
