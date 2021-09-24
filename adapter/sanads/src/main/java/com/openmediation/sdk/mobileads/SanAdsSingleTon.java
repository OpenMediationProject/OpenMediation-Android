// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;
import com.san.ads.AdError;
import com.san.ads.AdSize;
import com.san.ads.SANBanner;
import com.san.ads.SANInterstitial;
import com.san.ads.SANNativeAd;
import com.san.ads.SANReward;
import com.san.ads.base.IAdListener;
import com.san.ads.core.SANAd;
import com.san.ads.render.AdViewRenderHelper;
import com.san.api.SanAdSdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class SanAdsSingleTon {
    private static final String TAG = "OM-SanAds";

    private final ConcurrentHashMap<String, SANReward> mRvAds;
    private final ConcurrentHashMap<String, SANInterstitial> mIsAds;
    private final ConcurrentHashMap<String, SANBanner> mBannerAds;
    private final List<InitListener> mListeners;

    private static class Holder {
        private static final SanAdsSingleTon INSTANCE = new SanAdsSingleTon();
    }

    private SanAdsSingleTon() {
        mListeners = new CopyOnWriteArrayList<>();
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
    }

    static SanAdsSingleTon getInstance() {
        return Holder.INSTANCE;
    }

    boolean isInit() {
        return SanAdSdk.hasInitialized();
    }

    void init(InitListener listener) {
        if (isInit()) {
            if (listener != null) {
                listener.initSuccess();
            }
            return;
        }
        Application context = MediationUtil.getApplication();
        if (context == null) {
            if (listener != null) {
                listener.initFailed("Init Failed: Context is null");
            }
            AdLog.getSingleton().LogE(TAG, "Init Failed: Context is null");
            return;
        }
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String appKey = appInfo.metaData.getString("com.san.APP_KEY");
            if (TextUtils.isEmpty(appKey)) {
                String error = "Init Failed: Please add a <meta-data> tag with android:name=\"com.san.APP_KEY\" inside the AndroidManifest.";
                if (listener != null) {
                    listener.initFailed(error);
                }
                AdLog.getSingleton().LogE(TAG, error);
                return;
            }
        } catch (Throwable e) {
            if (listener != null) {
                listener.initFailed("Init Failed");
            }
            AdLog.getSingleton().LogE(TAG, "Init Failed: " + e.getMessage());
        }
        if (listener != null) {
            mListeners.add(listener);
        }
        init(context);
    }

    private void init(Context context) {
        try {
            SanAdSdk.init(context);
            AdLog.getSingleton().LogD(TAG, "SDK initialized successfully");
            for (InitListener listener : mListeners) {
                if (listener != null) {
                    listener.initSuccess();
                }
            }
        } catch (Throwable e) {
            AdLog.getSingleton().LogD(TAG, "SDK initialized failed: " + e.getMessage());
            for (InitListener listener : mListeners) {
                if (listener != null) {
                    listener.initFailed("SanAds SDK initialized failed: " + e.getMessage());
                }
            }
        }
        mListeners.clear();
    }

    void loadBanner(final String adUnitId, final Map<String, Object> extras, final BannerAdCallback adCallback) {
        final Context context = MediationUtil.getContext();
        if (context == null) {
            String error = "Context is null";
            AdLog.getSingleton().LogE(TAG, "BannerAd Load Error : " + error);
            if (adCallback != null) {
                adCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "SanAdsAdapter", error));
            }
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SANBanner bannerAd = new SANBanner(context, adUnitId);
                    InnerBannerAdListener listener = new InnerBannerAdListener(adCallback);
                    listener.setParameters(adUnitId, bannerAd);
                    AdSize adSize = getAdSize(extras);
                    bannerAd.setAdSize(adSize);
                    bannerAd.setAdLoadListener(listener);
                    bannerAd.setAdActionListener(listener);
                    bannerAd.load();
                } catch (Throwable e) {
                    String error = "BannerAd Load Error : " + e.getMessage();
                    AdLog.getSingleton().LogE(TAG, error);
                    if (adCallback != null) {
                        adCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "SanAdsAdapter", error));
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    boolean isBannerAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId) && !mBannerAds.containsKey(adUnitId)) {
            return false;
        }
        SANBanner bannerAd = mBannerAds.get(adUnitId);
        return bannerAd != null;
    }

    void destroyBannerAd(String adUnitId) {
        try {
            SANBanner bannerAd = mBannerAds.get(adUnitId);
            if (bannerAd != null) {
                bannerAd.destroy();
            }
        } catch (Throwable ignored) {
        }
    }


    void loadNative(String adUnitId, NativeAdCallback adCallback) {
        Context context = MediationUtil.getContext();
        if (context == null) {
            String error = "Context is null";
            AdLog.getSingleton().LogE(TAG, "NativeAd Load Error : " + error);
            if (adCallback != null) {
                adCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "SanAdsAdapter", error));
            }
            return;
        }
        try {
            InnerNativeAdListener listener = new InnerNativeAdListener(adCallback);
            SANNativeAd nativeAd = new SANNativeAd(context, adUnitId);
            nativeAd.setAdLoadListener(listener);
            listener.setParameters(adUnitId, nativeAd);
            nativeAd.load();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd Load Error : " + e.getMessage());
            if (adCallback != null) {
                adCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "SanAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    void registerNativeView(final String adUnitId, NativeAdView adView, AdnAdInfo adInfo, final NativeAdCallback callback) {
        try {
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof SANNativeAd)) {
                AdLog.getSingleton().LogE(TAG, "NativeAd Not Ready! " + adUnitId);
                return;
            }
            SANNativeAd nativeAd = (SANNativeAd) adInfo.getAdnNativeAd();
            if (nativeAd == null) {
                AdLog.getSingleton().LogE(TAG, "NativeAd Not Ready! " + adUnitId);
                return;
            }
            nativeAd.setAdActionListener(new IAdListener.AdActionListener() {
                @Override
                public void onAdImpressionError(AdError error) {
                    AdLog.getSingleton().LogE(TAG, "NativeAd onAdImpressionError: " + error);
                }

                @Override
                public void onAdImpression() {
                    AdLog.getSingleton().LogD(TAG, "NativeAd onAdImpression " + adUnitId);
                    if (callback != null) {
                        callback.onNativeAdImpression();
                    }
                }

                @Override
                public void onAdClicked() {
                    AdLog.getSingleton().LogD(TAG, "NativeAd onAdClicked " + adUnitId);
                    if (callback != null) {
                        callback.onNativeAdAdClicked();
                    }
                }

                @Override
                public void onAdCompleted() {

                }

                @Override
                public void onAdClosed(boolean hasRewarded) {

                }
            });
            //click list
            List<View> clickViews = new ArrayList<>();
            if (adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();
                mediaView.removeAllViews();

                com.san.ads.MediaView adnMediaView = new com.san.ads.MediaView(adView.getContext());
                mediaView.addView(adnMediaView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adnMediaView.setLayoutParams(layoutParams);
                //media view
                adnMediaView.loadMadsMediaView(nativeAd.getNativeAd());

                clickViews.add(adnMediaView);
            }
            String iconUrl = nativeAd.getIconUrl();
            if (!TextUtils.isEmpty(iconUrl) && adView.getAdIconView() != null) {
                AdIconView iconView = adView.getAdIconView();
                iconView.removeAllViews();
                ImageView adnIconView = new ImageView(adView.getContext());
                iconView.addView(adnIconView);
                adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                AdViewRenderHelper.loadImage(adView.getContext(), nativeAd.getIconUrl(), adnIconView);
                clickViews.add(iconView);
            }

            if (adView.getTitleView() != null) {
                clickViews.add(adView.getTitleView());
            }
            if (adView.getCallToActionView() != null) {
                clickViews.add(adView.getCallToActionView());
            }
            if (adView.getDescView() != null) {
                clickViews.add(adView.getDescView());
            }
            clickViews.add(adView);
            //prepare
            nativeAd.prepare(adView, clickViews, null);
        } catch (Throwable ignored) {
        }
    }

    void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        if (adInfo != null && adInfo.getAdnNativeAd() instanceof SANNativeAd) {
            try {
                SANNativeAd nativeAd = (SANNativeAd) adInfo.getAdnNativeAd();
                nativeAd.destroy();
            } catch (Throwable ignored) {
            }
        }
    }

    void loadRewardedVideo(final String adUnitId, RewardedVideoCallback adCallback) {
        Context context = MediationUtil.getContext();
        if (context == null) {
            String error = "Context is null";
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd Load Error : " + error);
            if (adCallback != null) {
                adCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "SanAdsAdapter", error));
            }
            return;
        }
        try {
            SANReward rewardAd = new SANReward(context, adUnitId);
            InnerRewardedAdListener listener = new InnerRewardedAdListener(adCallback);
            listener.setParameters(rewardAd, adUnitId);
            rewardAd.setAdLoadListener(listener);
            rewardAd.load();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd Load Error : " + e.getMessage());
            if (adCallback != null) {
                adCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "SanAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    boolean isRewardedVideoReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        SANReward rewardedAd = mRvAds.get(adUnitId);
        return rewardedAd != null && rewardedAd.isAdReady();
    }

    void showRewardedVideo(final String adUnitId, final RewardedVideoCallback callback) {
        try {
            final SANReward rewardedAd = mRvAds.get(adUnitId);
            if (rewardedAd != null) {
                rewardedAd.setAdActionListener(new IAdListener.AdActionListener() {
                    @Override
                    public void onAdImpressionError(AdError error) {
                        AdLog.getSingleton().LogE(TAG, "RewardedVideo onAdImpressionError: " + error);
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "SanAdsAdapter", error.getErrorCode(), error.getErrorMessage()));
                        }
                    }

                    @Override
                    public void onAdImpression() {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdImpression");
                        if (callback != null) {
                            callback.onRewardedVideoAdShowSuccess();
                            callback.onRewardedVideoAdStarted();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdClicked");
                        if (callback != null) {
                            callback.onRewardedVideoAdClicked();
                        }
                    }

                    @Override
                    public void onAdCompleted() {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdCompleted");
                        if (callback != null) {
                            callback.onRewardedVideoAdRewarded();
                            callback.onRewardedVideoAdEnded();
                        }
                    }

                    @Override
                    public void onAdClosed(boolean hasRewarded) {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdClosed, hasRewarded: " + hasRewarded);
                        try {
                            mRvAds.remove(adUnitId);
                            rewardedAd.destroy();
                        } catch (Throwable ignored) {
                        }
                        if (callback != null) {
                            callback.onRewardedVideoAdClosed();
                        }
                    }
                });
                rewardedAd.show();
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "SanAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    void loadInterstitial(String adUnitId, InterstitialAdCallback adCallback) {
        Context context = MediationUtil.getContext();
        if (context == null) {
            String error = "Context is null";
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : " + error);
            if (adCallback != null) {
                adCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "SanAdsAdapter", error));
            }
            return;
        }
        try {
            InnerInterstitialAdListener listener = new InnerInterstitialAdListener(adCallback);
            SANInterstitial interstitial = new SANInterstitial(context, adUnitId);
            listener.setParameters(interstitial, adUnitId);
            interstitial.setAdLoadListener(listener);
            interstitial.load();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : " + e.getMessage());
            if (adCallback != null) {
                adCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "SanAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    boolean isInterstitialReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        SANInterstitial interstitialAd = mIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isAdReady();
    }

    void showInterstitial(final String adUnitId, final InterstitialAdCallback callback) {
        try {
            final SANInterstitial interstitialAd = mIsAds.get(adUnitId);
            if (interstitialAd != null) {
                interstitialAd.setAdActionListener(new IAdListener.AdActionListener() {
                    @Override
                    public void onAdImpressionError(AdError error) {
                        AdLog.getSingleton().LogE(TAG, "InterstitialAd onAdImpressionError : " + error);
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "SanAdsAdapter", error.getErrorCode(), error.getErrorMessage()));
                        }
                    }

                    @Override
                    public void onAdImpression() {
                        AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdImpression");
                        if (callback != null) {
                            callback.onInterstitialAdShowSuccess();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdClicked");
                        if (callback != null) {
                            callback.onInterstitialAdClicked();
                        }
                    }

                    @Override
                    public void onAdCompleted() {
                    }

                    @Override
                    public void onAdClosed(boolean hasRewarded) {
                        AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdClosed");
                        try {
                            mIsAds.remove(adUnitId);
                            interstitialAd.destroy();
                        } catch (Throwable ignored) {
                        }
                        if (callback != null) {
                            callback.onInterstitialAdClosed();
                        }
                    }
                });
                interstitialAd.show();
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "SanAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private class InnerRewardedAdListener implements IAdListener.AdLoadListener {
        SANReward mRewarded;
        String mAdUnitId;
        RewardedVideoCallback mAdCallback;

        public InnerRewardedAdListener(RewardedVideoCallback adCallback) {
            mAdCallback = adCallback;
        }

        public void setParameters(SANReward rewarded, String adUnitId) {
            mRewarded = rewarded;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoaded(SANAd adWrapper) {
            AdLog.getSingleton().LogD(TAG, "onRewardedLoaded PlacementId : " + mAdUnitId);
            mRvAds.put(mAdUnitId, mRewarded);
            if (mAdCallback != null) {
                mAdCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onAdLoadError(AdError adError) {
            AdLog.getSingleton().LogE(TAG, "RewardedAd LoadFailed : " + adError);
            if (mAdCallback != null) {
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "SanAdsAdapter",
                        adError.getErrorCode(), adError.getErrorMessage());
                mAdCallback.onRewardedVideoLoadFailed(adapterError);
            }
        }
    }

    private class InnerInterstitialAdListener implements IAdListener.AdLoadListener {
        SANInterstitial mInterstitialAd;
        String mAdUnitId;
        InterstitialAdCallback mAdCallback;

        public InnerInterstitialAdListener(InterstitialAdCallback adCallback) {
            this.mAdCallback = adCallback;
        }

        public void setParameters(SANInterstitial interstitialAd, String adUnitId) {
            mInterstitialAd = interstitialAd;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoaded(SANAd adWrapper) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onInterstitialLoaded PlacementId : " + mAdUnitId);
            mIsAds.put(mAdUnitId, mInterstitialAd);
            if (mAdCallback != null) {
                mAdCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onAdLoadError(AdError adError) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd LoadFailed : " + adError);
            mIsAds.remove(mAdUnitId);
            if (mAdCallback != null) {
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "SanAdsAdapter",
                        adError.getErrorCode(), adError.getErrorMessage());
                mAdCallback.onInterstitialAdLoadFailed(adapterError);
            }
        }
    }

    private class InnerBannerAdListener implements IAdListener.AdLoadListener, IAdListener.AdActionListener {
        SANBanner mBannerAd;
        String mAdUnitId;
        BannerAdCallback mAdCallback;

        public InnerBannerAdListener(BannerAdCallback adCallback) {
            mAdCallback = adCallback;
        }

        public void setParameters(String adUnitId, SANBanner adView) {
            mBannerAd = adView;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoaded(SANAd adWrapper) {
            AdLog.getSingleton().LogD(TAG, "BannerAd onAdLoaded : " + mAdUnitId);
            getInstance().mBannerAds.put(mAdUnitId, mBannerAd);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadSuccess(mBannerAd.getAdView());
            }
        }

        @Override
        public void onAdLoadError(AdError adError) {
            AdLog.getSingleton().LogE(TAG, "BannerAd LoadFailed : " + adError);
            mRvAds.remove(mAdUnitId);
            if (mAdCallback != null) {
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "SanAdsAdapter",
                        adError.getErrorCode(), adError.getErrorMessage());
                mAdCallback.onBannerAdLoadFailed(adapterError);
            }
        }

        @Override
        public void onAdImpressionError(AdError error) {
            AdLog.getSingleton().LogE(TAG, "BannerAd onAdImpressionError : " + error);
            if (mAdCallback != null) {
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "SanAdsAdapter",
                        error.getErrorCode(), error.getErrorMessage());
                mAdCallback.onBannerAdLoadFailed(adapterError);
            }
        }

        @Override
        public void onAdImpression() {
            AdLog.getSingleton().LogD(TAG, "BannerAd onAdImpression");
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }

        @Override
        public void onAdClicked() {
            AdLog.getSingleton().LogD(TAG, "BannerAd onAdClicked");
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onAdCompleted() {
        }

        @Override
        public void onAdClosed(boolean hasRewarded) {

        }
    }

    private static class InnerNativeAdListener implements IAdListener.AdLoadListener {

        SANNativeAd mNativeAd;
        String mAdUnitId;
        NativeAdCallback mAdCallback;

        public InnerNativeAdListener(NativeAdCallback adCallback) {
            mAdCallback = adCallback;
        }

        public void setParameters(String adUnitId, SANNativeAd nativeAd) {
            mNativeAd = nativeAd;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoaded(SANAd adWrapper) {
            AdLog.getSingleton().LogD(TAG, "NativeAd onAdLoaded: " + mAdUnitId);
            AdnAdInfo adInfo = new AdnAdInfo();
            adInfo.setAdnNativeAd(mNativeAd);
            adInfo.setDesc(mNativeAd.getContent());
            adInfo.setType(MediationInfo.MEDIATION_ID_27);
            adInfo.setTitle(mNativeAd.getTitle());
            adInfo.setCallToActionText(mNativeAd.getCallToAction());
            if (mAdCallback != null) {
                mAdCallback.onNativeAdLoadSuccess(adInfo);
            }
        }

        @Override
        public void onAdLoadError(AdError adError) {
            AdLog.getSingleton().LogE(TAG, "NativeAd LoadFailed : " + adError);
            if (mAdCallback != null) {
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "SanAdsAdapter",
                        adError.getErrorCode(), adError.getErrorMessage());
                mAdCallback.onNativeAdLoadFailed(adapterError);
            }
        }
    }

    static AdSize getAdSize(Map<String, Object> extras) {
        String desc = MediationUtil.getBannerDesc(extras);
        switch (desc) {
            case MediationUtil.DESC_RECTANGLE:
                return AdSize.MEDIUM_RECTANGLE;
            case MediationUtil.DESC_SMART:
                if (!MediationUtil.isLargeScreen(MediationUtil.getContext())) {
                    return AdSize.BANNER;
                }
            default:
                return AdSize.BANNER;
        }
    }

    interface InitListener {
        void initSuccess();

        void initFailed(String error);
    }
}
