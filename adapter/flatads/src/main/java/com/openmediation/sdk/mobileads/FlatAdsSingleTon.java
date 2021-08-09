// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.flatads.sdk.FlatAdSDK;
import com.flatads.sdk.builder.InterstitialAd;
import com.flatads.sdk.builder.NativeAd;
import com.flatads.sdk.builder.RewardedAd;
import com.flatads.sdk.callback.AdBiddingListener;
import com.flatads.sdk.callback.AdLoadListener;
import com.flatads.sdk.callback.AdShowListener;
import com.flatads.sdk.callback.RewardedAdCallback;
import com.flatads.sdk.response.AdContent;
import com.flatads.sdk.statics.ErrorCode;
import com.flatads.sdk.ui.BannerAdView;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlatAdsSingleTon {

    private static final String TAG = "OM-FlatAds ";

    private final ConcurrentHashMap<String, RewardedAd> mRvAds;
    private final ConcurrentHashMap<String, InterstitialAd> mIsAds;
    private final ConcurrentHashMap<String, BannerAdView> mBannerAds;
    private final ConcurrentHashMap<String, FlatAdsNativeAdsConfig> mNativeAds;
    private final ConcurrentHashMap<String, String> mAdToken;

    private final ConcurrentMap<String, FlatAdsBidCallback> mBidCallbacks;
    private final ConcurrentMap<String, String> mBidError;

    private static class Holder {
        private static final FlatAdsSingleTon INSTANCE = new FlatAdsSingleTon();
    }

    private FlatAdsSingleTon() {
        mAdToken = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mNativeAds = new ConcurrentHashMap<>();
        mBidCallbacks = new ConcurrentHashMap<>();
        mBidError = new ConcurrentHashMap<>();
    }

    public static FlatAdsSingleTon getInstance() {
        return Holder.INSTANCE;
    }

    public void onResume() {
        for (BannerAdView bannerAdView : mBannerAds.values()) {
            bannerAdView.resume();
        }
    }

    public void onPause() {
        for (BannerAdView bannerAdView : mBannerAds.values()) {
            bannerAdView.pause();
        }
    }

    public boolean isInit() {
        return FlatAdSDK.getInitStatus();
    }

    public synchronized void init(String appKey, InitListener listener) {
        if (MediationUtil.getContext() == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                listener.initFailed("Context is null or AppKey is empty");
            }
            AdLog.getSingleton().LogE(TAG, "Init Failed: Context is null or AppKey is empty!");
            return;
        }
        if (isInit()) {
            if (listener != null) {
                listener.initSuccess();
            }
            return;
        }
        try {
            String[] tmp = appKey.split("#");
            String appId = tmp[0];
            String token = tmp[1];
            FlatAdSDK.initialize(MediationUtil.getApplication(), appId, token);
            AdLog.getSingleton().LogD(TAG, "Init Success");
            if (listener != null) {
                listener.initSuccess();
            }
        } catch (Exception e) {
            AdLog.getSingleton().LogE(TAG, "Init Error: " + e.getMessage());
            if (listener != null) {
                listener.initFailed("Init Error");
            }
        }
    }

    void addBidCallback(String placementId, FlatAdsBidCallback callback) {
        if (!TextUtils.isEmpty(placementId) && callback != null) {
            mBidCallbacks.put(placementId, callback);
        }
    }

    void removeBidCallback(String placementId) {
        if (!TextUtils.isEmpty(placementId)) {
            mBidCallbacks.remove(placementId);
        }
    }

    String getError(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            return mBidError.get(adUnitId);
        }
        return "No Fill";
    }

    String getToken(String adUnitId) {
        return mAdToken.get(adUnitId);
    }

    void bidBanner(String adUnitId, com.openmediation.sdk.banner.AdSize adSize) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    BannerAdView bannerAdView = new BannerAdView(MediationUtil.getContext());
                    bannerAdView.setAdUnitId(adUnitId);
                    bannerAdView.setBannerSize(getAdSize(adSize));
                    bannerAdView.setAdLoadListener(new AdLoadListener() {
                        @Override
                        public void onAdSucLoad(AdContent adContent) {
                            AdLog.getSingleton().LogD(TAG, "BannerAd onAdSucLoad adUnitId : " + adUnitId);
                        }

                        @Override
                        public void onAdFailLoad(ErrorCode errorCode) {
                            AdLog.getSingleton().LogE(TAG, "BannerAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                            bidFailed(adUnitId, "BannerAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                        }
                    });
                    bannerAdView.bidding(adUnitId, new AdBiddingListener() {
                        @Override
                        public void getBidding(float price, String token) {
                            AdLog.getSingleton().LogD(TAG, "BannerAd getBidding price: " + price + ", token : " + token);
                            mAdToken.put(adUnitId, token);
                            mBannerAds.put(adUnitId, bannerAdView);
                            bidSuccess(adUnitId, price);
                        }
                    });
                } catch (Throwable e) {
                    AdLog.getSingleton().LogE(TAG, "BannerAd bid error : " + e.getMessage());
                    bidFailed(adUnitId, e.getMessage());
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    void loadAndShowBanner(String adUnitId, BannerAdCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    BannerAdView bannerAdView = mBannerAds.get(adUnitId);
                    // remove token
                    String token = mAdToken.remove(adUnitId);
                    if (bannerAdView == null || TextUtils.isEmpty(token)) {
                        if (callback != null) {
                            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "No Fill"));
                        }
                        return;
                    }
                    bannerAdView.setAdLoadListener(new AdLoadListener() {
                        @Override
                        public void onAdSucLoad(AdContent adContent) {
                            AdLog.getSingleton().LogD(TAG, "BannerAd onAdSucLoad adUnitId : " + adUnitId);
                            mBannerAds.put(adUnitId, bannerAdView);
                            if (callback != null) {
                                callback.onBannerAdLoadSuccess(bannerAdView);
                            }
                        }

                        @Override
                        public void onAdFailLoad(ErrorCode errorCode) {
                            AdLog.getSingleton().LogE(TAG, "BannerAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                            destroyBannerAd(adUnitId);
                            if (callback != null) {
                                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                        AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
                            }
                        }
                    });
                    bannerAdView.setAdShowListener(new InnerBannerListener(callback));
                    bannerAdView.winBidding(mAdToken.get(adUnitId));
                    bannerAdView.loadAd(token);
                } catch (Throwable e) {
                    destroyBannerAd(adUnitId);
                    AdLog.getSingleton().LogD(TAG, "BannerAd Load Failed: " + e.getMessage());
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "No Fill"));
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private class InnerBannerListener implements AdShowListener {
        BannerAdCallback callback;

        private InnerBannerListener(BannerAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdShowed() {
            AdLog.getSingleton().LogD(TAG, "BannerAd onAdShowed");
            if (callback != null) {
                callback.onBannerAdImpression();
            }
        }

        @Override
        public boolean onAdClicked() {
            AdLog.getSingleton().LogD(TAG, "BannerAd onAdClicked");
            if (callback != null) {
                callback.onBannerAdAdClicked();
            }
            return false;
        }

        @Override
        public void onAdClosed() {
            AdLog.getSingleton().LogD(TAG, "BannerAd onAdClosed");
        }
    }


    boolean isBannerAdReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId) || TextUtils.isEmpty(getToken(adUnitId))) {
            return false;
        }
        BannerAdView bannerAdView = mBannerAds.get(adUnitId);
        return bannerAdView != null;
    }

    void destroyBannerAd(String adUnitId) {
        BannerAdView bannerAdView = mBannerAds.remove(adUnitId);
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
    }

    public void bidNative(String adUnitId) {
        try {
            //native
            NativeAd nativeAd = new NativeAd(adUnitId, MediationUtil.getContext());
            AdLoadListener loadListener = new AdLoadListener() {
                @Override
                public void onAdSucLoad(AdContent adContent) {
                    AdLog.getSingleton().LogD(TAG, "NativeAd onAdSucLoad adUnitId : " + adUnitId);
                }

                @Override
                public void onAdFailLoad(ErrorCode errorCode) {
                    AdLog.getSingleton().LogE(TAG, "NativeAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                    bidFailed(adUnitId, "NativeAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                }

            };
            nativeAd.setAdListener(loadListener);
            nativeAd.bidding(new AdBiddingListener() {
                @Override
                public void getBidding(float price, String token) {
                    AdLog.getSingleton().LogD(TAG, "NativeAd getBidding price: " + price + ", token : " + token);
                    mAdToken.put(adUnitId, token);
                    FlatAdsNativeAdsConfig adsConfig = new FlatAdsNativeAdsConfig();
                    adsConfig.setNativeAd(nativeAd);
                    mNativeAds.put(adUnitId, adsConfig);
                    bidSuccess(adUnitId, price);
                }
            });
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd bid error : " + e.getMessage());
            bidFailed(adUnitId, e.getMessage());
        }
    }

    void loadNativeAdWithBid(String adUnitId, NativeAdCallback callback) {
        try {
            FlatAdsNativeAdsConfig adsConfig = mNativeAds.get(adUnitId);
            String token = mAdToken.remove(adUnitId);
            if (adsConfig == null || adsConfig.getNativeAd() == null || TextUtils.isEmpty(token)) {
                AdLog.getSingleton().LogE(TAG, "NativeAd Load Failed : No Fill");
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "No Fill"));
                }
                return;
            }
            NativeAd nativeAd = adsConfig.getNativeAd();
            nativeAd.setAdListener(new InnerNativeListener(adUnitId, nativeAd, callback));
            nativeAd.winBidding(token);
            nativeAd.loadAd(token);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd LoadFailed");
            destroyNativeAd(adUnitId);
        }
    }

    private class InnerNativeListener implements AdLoadListener {
        String adUnitId;
        NativeAd nativeAd;
        NativeAdCallback callback;

        private InnerNativeListener(String adUnitId, NativeAd nativeAd, NativeAdCallback callback) {
            this.adUnitId = adUnitId;
            this.nativeAd = nativeAd;
            this.callback = callback;
        }

        @Override
        public void onAdSucLoad(AdContent adContent) {
            AdLog.getSingleton().LogD(TAG, "NativeAd onAdSucLoad adUnitId : " + adUnitId);
            FlatAdsNativeAdsConfig adsConfig = new FlatAdsNativeAdsConfig();
            adsConfig.setNativeAd(nativeAd);
            adsConfig.setAdContent(adContent);
            mNativeAds.put(adUnitId, adsConfig);
            AdInfo adInfo = new AdInfo();
            adInfo.setDesc(adContent.desc);
            adInfo.setType(MediationInfo.MEDIATION_ID_25);
            adInfo.setTitle(adContent.title);
            adInfo.setCallToActionText(adContent.ad_btn);
            if (callback != null) {
                callback.onNativeAdLoadSuccess(adInfo);
            }
        }

        @Override
        public void onAdFailLoad(ErrorCode errorCode) {
            AdLog.getSingleton().LogE(TAG, "NativeAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
            destroyNativeAd(adUnitId);
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
            }
        }
    }

    FlatAdsNativeAdsConfig getNativeAd(String adUnitId) {
        return mNativeAds.get(adUnitId);
    }

    public void destroyNativeAd(String adUnitId) {
        mNativeAds.remove(adUnitId);
        mAdToken.remove(adUnitId);
    }

    public void bidInterstitial(String adUnitId) {
        try {
            InterstitialAd interstitialAd = new InterstitialAd(MediationUtil.getContext());
            interstitialAd.setUnitId(adUnitId);
            interstitialAd.setAdListener(new AdLoadListener() {
                @Override
                public void onAdSucLoad(AdContent adContent) {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdSucLoad adUnitId : " + adUnitId);
                }

                @Override
                public void onAdFailLoad(ErrorCode errorCode) {
                    AdLog.getSingleton().LogE(TAG, "InterstitialAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                    bidFailed(adUnitId, "InterstitialAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                }
            });
            interstitialAd.bidding(adUnitId, new AdBiddingListener() {
                @Override
                public void getBidding(float price, String token) {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd getBidding price: " + price + ", token : " + token);
                    mAdToken.put(adUnitId, token);
                    mIsAds.put(adUnitId, interstitialAd);
                    bidSuccess(adUnitId, 0);
                }
            });
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd bid error : " + e.getMessage());
            bidFailed(adUnitId, e.getMessage());
        }
    }

    void loadInterstitialAdWithBid(final String adUnitId, InterstitialAdCallback callback) {
        try {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd Load bid adUnitId : " + adUnitId);
            InterstitialAd interstitialAd = mIsAds.get(adUnitId);
            if (interstitialAd != null) {
                interstitialAd.winBidding(mAdToken.get(adUnitId));
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FlatAdsAdapter", "No Fill"));
                }
            }
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : " + e.getMessage());
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FlatAdsAdapter", e.getMessage()));
            }
        }
    }

    boolean isInterstitialAdReady(final String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        InterstitialAd interstitialAd = mIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isLoaded();
    }

    void showInterstitialAd(String adUnitId, FlatAdsInterstitialCallback callback) {
        try {
            InterstitialAd interstitialAd = mIsAds.remove(adUnitId);
            interstitialAd.setAdShowListener(new AdShowListener() {

                @Override
                public void onAdShowed() {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdShowed");
                    if (callback != null) {
                        callback.onInterstitialOpened(adUnitId);
                    }
                }

                @Override
                public boolean onAdClicked() {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdClicked");
                    if (callback != null) {
                        callback.onInterstitialClick(adUnitId);
                    }
                    return false;
                }

                @Override
                public void onAdClosed() {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdClosed");
                    if (callback != null) {
                        callback.onInterstitialDismissed(adUnitId);
                    }
                }
            });
            String token = mAdToken.remove(adUnitId);
            if (!TextUtils.isEmpty(token)) {
                interstitialAd.loadAd(token);
            } else {
                interstitialAd.showAd(MediationUtil.getContext());
            }
        } catch (Throwable e) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd show failed: " + e.getMessage());
        }
    }

    void bidVideo(String adUnitId) {
        try {
            RewardedAd rewardedAd = new RewardedAd(MediationUtil.getContext());
            rewardedAd.setAdUnitId(adUnitId);
            rewardedAd.setAdListener(new AdLoadListener() {
                @Override
                public void onAdSucLoad(AdContent adContent) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onRewardedLoaded adUnitId : " + adUnitId);
                }

                @Override
                public void onAdFailLoad(ErrorCode errorCode) {
                    AdLog.getSingleton().LogE(TAG, "RewardedVideoAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                    bidFailed(adUnitId, "RewardedVideoAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                }
            });
            rewardedAd.bidding(adUnitId, new AdBiddingListener() {
                @Override
                public void getBidding(float price, String token) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd getBidding price: " + price + ", token : " + token);
                    mAdToken.put(adUnitId, token);
                    mRvAds.put(adUnitId, rewardedAd);
                    bidSuccess(adUnitId, price);
                }
            });
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd bid error : " + e.getMessage());
            bidFailed(adUnitId, e.getMessage());
        }
    }

    void loadRewardedVideoWithBid(final String adUnitId, RewardedVideoCallback callback) {
        RewardedAd rewardedAd = mRvAds.get(adUnitId);
        String token = mAdToken.get(adUnitId);
        if (rewardedAd != null && !TextUtils.isEmpty(token)) {
            rewardedAd.winBidding(token);
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", "No Fill"));
            }
        }
    }

    boolean isRewardedVideoReady(final String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        RewardedAd rewardedAd = mRvAds.get(adUnitId);
        return rewardedAd != null && rewardedAd.isLoaded();
    }

    void showRewardedVideo(String adUnitId, FlatAdsVideoCallback callback) {
        try {
            RewardedAd rewardedAd = mRvAds.remove(adUnitId);
            if (rewardedAd != null) {
                rewardedAd.setRewardedAdCallback(new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdOpened() {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideoAd opened");
                        if (callback != null) {
                            callback.onRewardedOpened(adUnitId);
                        }
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideoAd closed");
                        if (callback != null) {
                            callback.onRewardedClosed(adUnitId);
                        }
                    }

                    @Override
                    public void onUserEarnedReward() {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onUserEarnedReward");
                        if (callback != null) {
                            callback.onRewardedReward(adUnitId);
                        }
                    }

                    @Override
                    public void onRewardedAdFailedToShow() {
                        AdLog.getSingleton().LogE(TAG, "RewardedVideoAd onRewardedAdFailedToShow");
                        if (callback != null) {
                            callback.onRewardedShowFailed(adUnitId);
                        }
                    }

                    @Override
                    public boolean onAdClicked() {
                        AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onAdClicked");
                        if (callback != null) {
                            callback.onRewardedClicked(adUnitId);
                        }
                        return false;
                    }
                });
                String token = mAdToken.remove(adUnitId);
                if (!TextUtils.isEmpty(token)) {
                    rewardedAd.loadAd(token);
                } else {
                    rewardedAd.showAd(MediationUtil.getContext());
                }
            }
        } catch (Throwable e) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd show failed: " + e.getMessage());
        }
    }

    private void bidSuccess(String adUnitId, float ecpm) {
        if (mBidCallbacks.containsKey(adUnitId)) {
            mBidCallbacks.get(adUnitId).onBidSuccess(adUnitId, ecpm);
        }
        mBidError.remove(adUnitId);
    }

    private void bidFailed(String adUnitId, String error) {
        if (mBidCallbacks.containsKey(adUnitId)) {
            mBidCallbacks.get(adUnitId).onBidFailed(adUnitId, error);
        }
        mBidError.put(adUnitId, error);
    }

    private int getAdSize(com.openmediation.sdk.banner.AdSize adSize) {
        String bannerDesc = "";
        if (adSize != null) {
            bannerDesc = adSize.getDescription();
        }
        return getAdSize(bannerDesc);
    }

    private int getAdSize(String desc) {
        if (MediationUtil.DESC_RECTANGLE.equals(desc)) {
            return 1;
        }
        return 2;
    }

    interface InitListener {
        void initSuccess();

        void initFailed(String error);
    }

    /* no bid */
    void loadInterstitialAd(final String adUnitId, InterstitialAdCallback callback) {
        try {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd Load adUnitId : " + adUnitId);
            InterstitialAd interstitialAd = new InterstitialAd(MediationUtil.getContext());
            interstitialAd.setUnitId(adUnitId);
            interstitialAd.setAdListener(new AdLoadListener() {
                @Override
                public void onAdSucLoad(AdContent adContent) {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdSucLoad adUnitId : " + adUnitId);
                    mIsAds.put(adUnitId, interstitialAd);
                    if (callback != null) {
                        callback.onInterstitialAdLoadSuccess();
                    }
                }

                @Override
                public void onAdFailLoad(ErrorCode errorCode) {
                    AdLog.getSingleton().LogE(TAG, "InterstitialAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
                    }
                }
            });
            interstitialAd.loadAd();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : " + e.getMessage());
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FlatAdsAdapter", e.getMessage()));
            }
        }
    }

    void loadRewardedVideo(final String adUnitId, RewardedVideoCallback callback) {
        try {
            RewardedAd rewardedAd = new RewardedAd(MediationUtil.getContext());
            rewardedAd.setAdUnitId(adUnitId);
            rewardedAd.setAdListener(new AdLoadListener() {
                @Override
                public void onAdSucLoad(AdContent adContent) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd onRewardedLoaded adUnitId : " + adUnitId);
                    mRvAds.put(adUnitId, rewardedAd);
                    if (callback != null) {
                        callback.onRewardedVideoLoadSuccess();
                    }
                }

                @Override
                public void onAdFailLoad(ErrorCode errorCode) {
                    AdLog.getSingleton().LogE(TAG, "RewardedVideoAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
                    }
                }
            });
            rewardedAd.loadAd();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd bid error : " + e.getMessage());
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", "No Fill"));
            }
        }
    }

    void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    BannerAdView bannerAdView = new BannerAdView(activity);
                    bannerAdView.setAdUnitId(adUnitId);
                    bannerAdView.setBannerSize(getAdSize(MediationUtil.getBannerDesc(extras)));
                    bannerAdView.setAdLoadListener(new AdLoadListener() {
                        @Override
                        public void onAdSucLoad(AdContent adContent) {
                            AdLog.getSingleton().LogD(TAG, "BannerAd onAdSucLoad adUnitId : " + adUnitId);
                            mBannerAds.put(adUnitId, bannerAdView);
                            if (callback != null) {
                                callback.onBannerAdLoadSuccess(bannerAdView);
                            }
                        }

                        @Override
                        public void onAdFailLoad(ErrorCode errorCode) {
                            AdLog.getSingleton().LogE(TAG, "BannerAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
                            if (callback != null) {
                                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                        AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
                            }
                        }
                    });
                    bannerAdView.setAdShowListener(new InnerBannerListener(callback));
                    bannerAdView.loadAd();
                } catch (Throwable e) {
                    AdLog.getSingleton().LogE(TAG, "BannerAd load error : " + e.getMessage());
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "No Fill"));
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    void loadNativeAd(String adUnitId, NativeAdCallback callback) {
        try {
            //native
            NativeAd nativeAd = new NativeAd(adUnitId, MediationUtil.getContext());
            nativeAd.setAdListener(new InnerNativeListener(adUnitId, nativeAd, callback));
            nativeAd.loadAd();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd Load error : " + e.getMessage());
            destroyNativeAd(adUnitId);
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "No Fill"));
            }
        }
    }

}
