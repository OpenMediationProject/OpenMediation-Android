// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TikTokAdapter extends CustomAdsAdapter {
    private static String TAG = "TikTok: ";
    private TTAdNative mTTAdNative;
    private ConcurrentMap<String, TTRewardVideoAd> mTTRvAds;
    private ConcurrentMap<String, TTFullScreenVideoAd> mTTFvAds;

    public TikTokAdapter() {
        mTTRvAds = new ConcurrentHashMap<>();
        mTTFvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return TTAdSdk.getAdManager().getSDKVersion();
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
    public boolean isAdNetworkInit() {
        return TTAdManagerHolder.getInstance().isInit();
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
        } catch(Exception e) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error"));
            }
        }
    }

    private void loadRvAd(String adUnitId, RewardedVideoCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            TTRewardVideoAd rewardedVideoAd = mTTRvAds.get(adUnitId);
            if (rewardedVideoAd == null) {
                AdSlot adSlot = buildAdSlotReq(MediationUtil.getContext(), adUnitId);
                mTTAdNative.loadRewardVideoAd(adSlot, new InnerLoadRvAdListener(callback, adUnitId, mTTRvAds));
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }
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
                    TTRewardVideoAd rewardedVideoAd = mTTRvAds.get(adUnitId);
                    if (rewardedVideoAd != null) {
                        rewardedVideoAd.setRewardAdInteractionListener(new InnerRvAdShowListener(callback));
                        rewardedVideoAd.showRewardVideoAd(activity);
                        mTTRvAds.remove(adUnitId);
                    } else {
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "TikTok RewardedVideo is not ready"));
                        }
                    }
                } catch(Exception e) {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "TikTok RewardedVideo is not ready"));
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
        } catch(Exception e) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error"));
            }
        }
    }

    private void loadInterstitial(Context context, String adUnitId, InterstitialAdCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            TTFullScreenVideoAd ad = mTTFvAds.get(adUnitId);
            if (ad != null) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                realLoadFullScreenVideoAd(context, adUnitId, callback);
            }
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
                    TTFullScreenVideoAd ad = mTTFvAds.get(adUnitId);
                    if (ad != null) {
                        ad.setFullScreenVideoAdInteractionListener(new InnerAdInteractionListener(callback));
                        ad.showFullScreenVideoAd(activity);
                        mTTFvAds.remove(adUnitId);
                    } else {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "TikTok InterstitialAd is not ready"));
                        }
                    }
                } catch(Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, e.getMessage()));
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
    public void initBannerAd(Activity activity, Map<String, Object> extras, final BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        TikTokBannerManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
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
        TikTokNativeManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
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
        TikTokNativeManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId) {
        super.destroyNativeAd(adUnitId);
        TikTokNativeManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initSplashAd(Activity activity, Map<String, Object> extras, SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        TikTokSplashManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
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
        TikTokSplashManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        TikTokSplashManager.getInstance().showAd(adUnitId, viewGroup, callback);
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
        TTAdManagerHolder.getInstance().init(MediationUtil.getContext(), mAppKey, callback);
    }

    private class InnerIsAdListener implements TTAdNative.FullScreenVideoAdListener {

        private InterstitialAdCallback mCallback;
        private String mAdUnitId;

        InnerIsAdListener(InterstitialAdCallback callback, String adUnitId) {
            this.mCallback = callback;
            this.mAdUnitId = adUnitId;
        }

        @Override
        public void onError(int i, String s) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, i, s));
            }
        }

        @Override
        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
            if (ad == null) {
                if (mCallback != null) {
                    mCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "TikTok loadInterstitialAd ad load failed"));
                }
                return;
            }
            AdLog.getSingleton().LogD(TAG + "loadInterstitialAd onFullScreenVideoAdLoad");
            mTTFvAds.put(mAdUnitId, ad);
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onFullScreenVideoCached() {
        }

        @Override
        public void onFullScreenVideoCached(TTFullScreenVideoAd ttFullScreenVideoAd) {

        }
    }

    private void realLoadFullScreenVideoAd(Context context, String adUnitId, InterstitialAdCallback callback) {
        AdSlot adSlot = buildAdSlotReq(context, adUnitId);
        InnerIsAdListener listener = new InnerIsAdListener(callback, adUnitId);
        if (mTTAdNative != null) {
            mTTAdNative.loadFullScreenVideoAd(adSlot, listener);
        }
    }

    private static class InnerAdInteractionListener implements TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {

        private InterstitialAdCallback mCallback;

        private InnerAdInteractionListener(InterstitialAdCallback callback) {
            this.mCallback = callback;
        }

        @Override
        public void onAdShow() {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onAdVideoBarClick() {
            if (mCallback != null) {
                mCallback.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdClose() {
            if (mCallback != null) {
                mCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onVideoComplete() {
        }

        @Override
        public void onSkippedVideo() {
        }
    }

    private AdSlot buildAdSlotReq(Context context, final String adUnitId) {
        if (mTTAdNative == null) {
            TTAdManager adManager = TTAdManagerHolder.getInstance().getAdManager();
            mTTAdNative = adManager.createAdNative(context.getApplicationContext());
        }
        int orientation = TTAdConstant.HORIZONTAL;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = TTAdConstant.VERTICAL;
        }
        int[] screenPx = TTAdManagerHolder.getScreenPx(context);
        float[] screenDp = TTAdManagerHolder.getScreenDp(context);
        return new AdSlot.Builder()
                .setCodeId(adUnitId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(screenPx[0], screenPx[1])
                .setExpressViewAcceptedSize(screenDp[0], screenDp[1])
                .setOrientation(orientation)
                .build();
    }

    private static class InnerLoadRvAdListener implements TTAdNative.RewardVideoAdListener {

        private RewardedVideoCallback mCallback;
        private String mCodeId;
        private ConcurrentMap<String, TTRewardVideoAd> mTTRvAds;

        private InnerLoadRvAdListener(RewardedVideoCallback callback, String codeId, ConcurrentMap<String, TTRewardVideoAd> tTRvAds) {
            this.mCallback = callback;
            this.mCodeId = codeId;
            this.mTTRvAds = tTRvAds;
        }

        @Override
        public void onError(int code, String message) {
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "TikTokAdapter", code, message));
            }
        }

        @Override
        public void onRewardVideoCached() {
        }

        @Override
        public void onRewardVideoCached(TTRewardVideoAd ttRewardVideoAd) {

        }

        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
            if (ad == null) {
                if (mCallback != null) {
                    mCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "TikTokAdapter", "TikTok RewardedVideo load failed"));
                }
                return;
            }
            mTTRvAds.put(mCodeId, ad);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadSuccess();
            }
        }
    }

    private static class InnerRvAdShowListener implements TTRewardVideoAd.RewardAdInteractionListener {

        private RewardedVideoCallback callback;

        private InnerRvAdShowListener(RewardedVideoCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdShow() {
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onAdVideoBarClick() {
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onAdClose() {
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onVideoComplete() {
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onVideoError() {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "TikTokAdapter", "TikTok rewardedVideo play failed"));
            }
        }

        @Override
        public void onRewardVerify(boolean rewardVerify, int i, String s, int i1, String s1) {
            if (callback != null && rewardVerify) {
                callback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onSkippedVideo() {
        }

    }

}
