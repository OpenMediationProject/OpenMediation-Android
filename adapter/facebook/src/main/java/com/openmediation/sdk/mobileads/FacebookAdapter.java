// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.BidderTokenProvider;
import com.facebook.ads.BuildConfig;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdExtendedListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdExtendedListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FacebookAdapter extends CustomAdsAdapter {

    private static final String PAY_LOAD = "pay_load";
    private Boolean mDidInitSuccess = null;
    private AtomicBoolean mDidCallInit;

    private ConcurrentMap<String, RewardedVideoAd> mFbRvAds;
    private ConcurrentMap<String, InterstitialAd> mFbIsAds;
    private ConcurrentMap<String, AdView> mBannerAds;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;
    private ConcurrentMap<String, BannerAdCallback> mBnCallbacks;
    private ConcurrentMap<String, NativeAdCallback> mNaCallbacks;

    public FacebookAdapter() {
        mFbRvAds = new ConcurrentHashMap<>();
        mFbIsAds = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
        mBnCallbacks = new ConcurrentHashMap<>();
        mNaCallbacks = new ConcurrentHashMap<>();
        mDidCallInit = new AtomicBoolean(false);
    }

    @Override
    public String getMediationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.facebook.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_3;
    }

    @Override
    public boolean isS2S() {
        return true;
    }

    @Override
    public boolean needPayload() {
        return true;
    }

    @Override
    public String getBiddingToken(Context context) {
        return BidderTokenProvider.getBidderToken(MediationUtil.getContext());
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check();
        if (TextUtils.isEmpty(error)) {
            mRvCallbacks.put((String) dataMap.get("pid"), callback);
            initSdk();
            if (mDidInitSuccess != null) {
                if (mDidInitSuccess) {
                    callback.onRewardedVideoInitSuccess();
                } else {
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Init facebook sdk failed"));
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRv(adUnitId, extras, callback);
    }

    private void loadRv(String adUnitId, Map<String, Object> extras,
                        RewardedVideoCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            RewardedVideoAd rewardedVideoAd = getRv(adUnitId);
            RewardedVideoAd.RewardedVideoAdLoadConfigBuilder configBuilder = rewardedVideoAd.buildLoadAdConfig();
            configBuilder.withAdListener(new FbRvListener(mRvCallbacks.get(adUnitId)));

            if (rewardedVideoAd.isAdLoaded()) {
                try {
                    MediationUtil.event(701, extras, getAdNetworkId());
                } catch (Throwable ignored) {
                }
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                if (extras != null && extras.containsKey(PAY_LOAD)) {
                    try {
                        MediationUtil.event(702, extras, getAdNetworkId());
                    } catch (Throwable ignored) {
                    }
                    configBuilder.withBid(String.valueOf(extras.get(PAY_LOAD)));
                } else {
                    try {
                        MediationUtil.event(703, extras, getAdNetworkId());
                    } catch (Throwable ignored) {
                    }
                }
                rewardedVideoAd.loadAd(configBuilder.build());
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (isRewardedVideoAvailable(adUnitId)) {
            RewardedVideoAd rewardedVideoAd = mFbRvAds.get(adUnitId);
            rewardedVideoAd.show();
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Facebook rewardedVideo is not ready"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        RewardedVideoAd rewardedVideoAd = mFbRvAds.get(adUnitId);
        return rewardedVideoAd != null && rewardedVideoAd.isAdLoaded();
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (TextUtils.isEmpty(error)) {
            mIsCallbacks.put((String) dataMap.get("pid"), callback);
            initSdk();
            if (mDidInitSuccess != null) {
                if (mDidInitSuccess) {
                    callback.onInterstitialAdInitSuccess();
                } else {
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Init facebook sdk failed"));
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras,
                                   InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadInterstitial(adUnitId, extras, callback);
    }

    private void loadInterstitial(String adUnitId, Map<String, Object> extras,
                                  InterstitialAdCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            InterstitialAd interstitialAd = getIs(adUnitId);
            InterstitialAd.InterstitialAdLoadConfigBuilder configBuilder = interstitialAd.buildLoadAdConfig();
            configBuilder.withAdListener(new FbIsAdListener(mIsCallbacks.get(adUnitId)));
            if (interstitialAd.isAdLoaded()) {
                try {
                    MediationUtil.event(701, extras, getAdNetworkId());
                } catch (Throwable ignored) {
                }
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                if (extras != null && extras.containsKey(PAY_LOAD)) {
                    try {
                        MediationUtil.event(702, extras, getAdNetworkId());
                    } catch (Throwable ignored) {
                    }
                    configBuilder.withBid(String.valueOf(extras.get(PAY_LOAD)));
                } else {
                    try {
                        MediationUtil.event(703, extras, getAdNetworkId());
                    } catch (Throwable ignored) {
                    }
                }
                interstitialAd.loadAd(configBuilder.build());
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (isInterstitialAdAvailable(adUnitId)) {
            InterstitialAd interstitialAd = mFbIsAds.get(adUnitId);
            interstitialAd.show();
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Facebook interstitial is not ready"));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        InterstitialAd interstitialAd = mFbIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isAdLoaded();
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (TextUtils.isEmpty(error)) {
            mBnCallbacks.put((String) extras.get("pid"), callback);
            initSdk();
            if (mDidInitSuccess != null) {
                if (mDidInitSuccess) {
                    callback.onBannerAdInitSuccess();
                } else {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Init facebook sdk failed"));
                }
            }
        } else {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            AdSize adSize = getAdSize(MediationUtil.getContext(), extras);
            AdView adView = new AdView(MediationUtil.getContext(), adUnitId, adSize);
            AdView.AdViewLoadConfigBuilder loadConfigBuilder = adView.buildLoadAdConfig();
            if (extras.containsKey(PAY_LOAD)) {
                loadConfigBuilder.withBid(String.valueOf(extras.get(PAY_LOAD)));
            }
            loadConfigBuilder.withAdListener(new FbBnAdListener(adView, callback));
            adView.loadAd(loadConfigBuilder.build());
            mBannerAds.put(adUnitId, adView);
        } else {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
        }
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        if (!mBannerAds.containsKey(adUnitId)) {
            return;
        }
        mBannerAds.get(adUnitId).destroy();
        mBannerAds.remove(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check();
        if (TextUtils.isEmpty(error)) {
            mNaCallbacks.put((String) extras.get("pid"), callback);
            initSdk();
            if (mDidInitSuccess != null) {
                if (mDidInitSuccess) {
                    callback.onNativeAdInitSuccess();
                } else {
                    callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "Init facebook sdk failed"));
                }
            }
        } else {
            if (callback != null) {
                callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            NativeAd nativeAd = new NativeAd(MediationUtil.getContext(), adUnitId);
            NativeAd.NativeAdLoadConfigBuilder loadConfigBuilder = nativeAd.buildLoadAdConfig();
            if (extras.containsKey(PAY_LOAD)) {
                loadConfigBuilder.withBid(String.valueOf(extras.get(PAY_LOAD)));
            }
            loadConfigBuilder.withAdListener(new FbNaAdListener(nativeAd, callback));
            nativeAd.loadAd(loadConfigBuilder.build());
        } else {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
        }
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        super.destroyNativeAd(adUnitId, adInfo);
        if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof FacebookNativeAdsConfig)) {
            AdLog.getSingleton().LogE("FacebookAdapter NativeAd destroyNativeAd failed, AdnAdInfo is null");
            return;
        }
        try {
            FacebookNativeAdsConfig config = (FacebookNativeAdsConfig) adInfo.getAdnNativeAd();
            if (config.getMediaView() != null) {
                config.getMediaView().destroy();
            }
            if (config.getIconView() != null) {
                config.getIconView().destroy();
            }
            if (config.getNativeAd() != null) {
                config.getNativeAd().unregisterView();
                config.getNativeAd().destroy();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adInfo, callback);
        try {
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof FacebookNativeAdsConfig)) {
                AdLog.getSingleton().LogE("FacebookAdapter NativeAd not ready, AdnAdInfo is null");
                return;
            }
            FacebookNativeAdsConfig config = (FacebookNativeAdsConfig) adInfo.getAdnNativeAd();
            if (config == null || config.getNativeAd() == null) {
                return;
            }
            NativeAdLayout fbNativeAdLayout = new NativeAdLayout(adView.getContext());
            List<View> views = new ArrayList<>();
            if (adView.getMediaView() != null) {
                adView.getMediaView().removeAllViews();
                views.add(adView.getMediaView());
            }

            if (adView.getAdIconView() != null) {
                adView.getAdIconView().removeAllViews();
                views.add(adView.getAdIconView());
            }

            if (adView.getTitleView() != null) {
                views.add(adView.getTitleView());
            }

            if (adView.getDescView() != null) {
                views.add(adView.getDescView());
            }

            if (adView.getCallToActionView() != null) {
                views.add(adView.getCallToActionView());
            }

            if (config.getAdOptionsView() == null) {
                AdOptionsView adOptionsView = new AdOptionsView(adView.getContext(), config.getNativeAd(), fbNativeAdLayout);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                adView.addView(adOptionsView, layoutParams);
                config.setAdOptionsView(adOptionsView);
            }

            if (adView.getMediaView() != null) {
                MediaView mediaView = new MediaView(adView.getContext());
                adView.getMediaView().addView(mediaView);
                config.setMediaView(mediaView);
            }
            if (adView.getAdIconView() != null) {
                MediaView iconView = new MediaView(adView.getContext());
                adView.getAdIconView().addView(iconView);
                config.setIconView(iconView);
            }
            //pay attention to the order of fb_mediaView and adIconView here
            config.getNativeAd().registerViewForInteraction(fbNativeAdLayout, config.getMediaView(), config.getIconView(), views);

            if (config.getAdOptionsView() != null) {
                config.getAdOptionsView().bringToFront();
            }
        } catch (Throwable e) {
            // ignore
        }
    }

    private RewardedVideoAd getRv(String adUnitId) {
        RewardedVideoAd rewardedVideoAd = mFbRvAds.get(adUnitId);
        if (rewardedVideoAd == null) {
            rewardedVideoAd = new RewardedVideoAd(MediationUtil.getContext(), adUnitId);
            mFbRvAds.put(adUnitId, rewardedVideoAd);
        }
        return rewardedVideoAd;
    }

    private InterstitialAd getIs(String adUnitId) {
        InterstitialAd interstitialAd = mFbIsAds.get(adUnitId);
        if (interstitialAd == null) {
            interstitialAd = new InterstitialAd(MediationUtil.getContext(), adUnitId);
            mFbIsAds.put(adUnitId, interstitialAd);
        }
        return interstitialAd;
    }

    private void initSdk() {
        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);
        if (mDidCallInit.compareAndSet(false, true)) {

            AudienceNetworkAds.buildInitSettings(MediationUtil.getContext())
                    .withInitListener(new AudienceNetworkAds.InitListener() {
                        @Override
                        public void onInitialized(final AudienceNetworkAds.InitResult result) {
                            MediationUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result.isSuccess()) {
                                        mDidInitSuccess = true;
                                        for (InterstitialAdCallback callback : mIsCallbacks.values()) {
                                            callback.onInterstitialAdInitSuccess();
                                        }
                                        for (RewardedVideoCallback callback : mRvCallbacks.values()) {
                                            callback.onRewardedVideoInitSuccess();
                                        }

                                        for (BannerAdCallback callback : mBnCallbacks.values()) {
                                            callback.onBannerAdInitSuccess();
                                        }
                                        for (NativeAdCallback callback : mNaCallbacks.values()) {
                                            callback.onNativeAdInitSuccess();
                                        }
                                    } else {
                                        mDidInitSuccess = false;
                                        String message = "Facebook init failed:" + result.getMessage();

                                        for (InterstitialAdCallback callback : mIsCallbacks.values()) {
                                            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, message));
                                        }
                                        for (RewardedVideoCallback callback : mRvCallbacks.values()) {
                                            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, message));
                                        }

                                        for (BannerAdCallback callback : mBnCallbacks.values()) {
                                            callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                                                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, message));
                                        }
                                        for (NativeAdCallback callback : mNaCallbacks.values()) {
                                            callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                                                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, message));
                                        }
                                    }
                                }
                            });
                        }
                    }).initialize();
        }
    }

    private AdSize getAdSize(Context context, Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_LEADERBOARD:
                return AdSize.BANNER_HEIGHT_90;
            case MediationUtil.DESC_RECTANGLE:
                return AdSize.RECTANGLE_HEIGHT_250;
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(context)) {
                    return AdSize.BANNER_HEIGHT_90;
                } else {
                    return AdSize.BANNER_HEIGHT_50;
                }
            default:
                return AdSize.BANNER_HEIGHT_50;
        }
    }

    private static class FbRvListener implements RewardedVideoAdExtendedListener {

        private RewardedVideoCallback rvCallback;
        private AtomicBoolean mDidRvCloseCallbacked;

        FbRvListener(RewardedVideoCallback callback) {
            rvCallback = callback;
            mDidRvCloseCallbacked = new AtomicBoolean(false);
        }

        @Override
        public void onRewardedVideoCompleted() {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdRewarded();
                rvCallback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FacebookAdapter", adError.getErrorCode(), adError.getErrorMessage()));
            }
        }

        @Override
        public void onAdLoaded(Ad ad) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onAdClicked(Ad ad) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            mDidRvCloseCallbacked.set(false);
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdShowSuccess();
                rvCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onRewardedVideoClosed() {
            if (rvCallback != null && !mDidRvCloseCallbacked.get()) {
                mDidRvCloseCallbacked.set(true);
                rvCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onRewardedVideoActivityDestroyed() {
            if (rvCallback != null && !mDidRvCloseCallbacked.get()) {
                mDidRvCloseCallbacked.set(true);
                rvCallback.onRewardedVideoAdClosed();
            }
        }
    }

    private static class FbIsAdListener implements InterstitialAdExtendedListener {

        private InterstitialAdCallback isCallback;

        FbIsAdListener(InterstitialAdCallback callback) {
            isCallback = callback;
        }

        @Override
        public void onInterstitialDisplayed(Ad ad) {

        }

        @Override
        public void onInterstitialDismissed(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            if (isCallback != null) {
                isCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FacebookAdapter", adError.getErrorCode(), adError.getErrorMessage()));
            }
        }

        @Override
        public void onAdLoaded(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onAdClicked(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdClicked();
            }
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onInterstitialActivityDestroyed() {
            if (isCallback != null) {
                isCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onRewardedAdCompleted() {

        }

        @Override
        public void onRewardedAdServerSucceeded() {

        }

        @Override
        public void onRewardedAdServerFailed() {

        }
    }

    private static class FbBnAdListener implements AdListener {

        private AdView adView;
        private BannerAdCallback callback;

        FbBnAdListener(AdView view, BannerAdCallback callback) {
            this.adView = view;
            this.callback = callback;
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "FacebookAdapter", adError.getErrorCode(), adError.getErrorMessage()));
            }
        }

        @Override
        public void onAdLoaded(Ad ad) {
            if (callback != null) {
                callback.onBannerAdLoadSuccess(adView);
            }
        }

        @Override
        public void onAdClicked(Ad ad) {
            if (callback != null) {
                callback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            if (callback != null) {
                callback.onBannerAdImpression();
            }
        }
    }

    private static class FbNaAdListener implements NativeAdListener {

        private NativeAdCallback callback;
        private NativeAd nativeAd;

        FbNaAdListener(NativeAd nativeAd, NativeAdCallback callback) {
            this.callback = callback;
            this.nativeAd = nativeAd;
        }

        @Override
        public void onMediaDownloaded(Ad ad) {

        }

        @Override
        public void onError(Ad ad, AdError adError) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FacebookAdapter", adError.getErrorCode(), adError.getErrorMessage()));
            }
        }

        @Override
        public void onAdLoaded(Ad ad) {
            if (callback != null) {
                FacebookNativeAdsConfig config = new FacebookNativeAdsConfig();
                config.setNativeAd(nativeAd);
                AdnAdInfo info = new AdnAdInfo();
                info.setAdnNativeAd(config);
                info.setDesc(nativeAd.getAdBodyText());
                info.setType(MediationInfo.MEDIATION_ID_3);
                info.setCallToActionText(nativeAd.getAdCallToAction());
                info.setTitle(nativeAd.getAdHeadline());
                callback.onNativeAdLoadSuccess(info);
            }
        }

        @Override
        public void onAdClicked(Ad ad) {
            if (callback != null) {
                callback.onNativeAdAdClicked();
            }
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            AdLog.getSingleton().LogD("FacebookAdapter", "NativeAd onLoggingImpression");
            if (callback != null) {
                callback.onNativeAdImpression();
            }
        }
    }
}
