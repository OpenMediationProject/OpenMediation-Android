// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.RelativeLayout;

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
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.BidCallback;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlatAdsSingleTon {

    private static final String TAG = "OM-FlatAds ";

    private final ConcurrentHashMap<String, RewardedAd> mRvAds;
    private final ConcurrentHashMap<String, InterstitialAd> mIsAds;
    private final ConcurrentHashMap<String, BannerAdView> mBannerAds;
    private final ConcurrentHashMap<FlatAdsNativeAdsConfig, String> mNativeAdToken;
    private final ConcurrentHashMap<String, String> mAdToken;

    private static class Holder {
        private static final FlatAdsSingleTon INSTANCE = new FlatAdsSingleTon();
    }

    private FlatAdsSingleTon() {
        mAdToken = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mNativeAdToken = new ConcurrentHashMap<>();
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
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "Init Error: " + e.getMessage());
            if (listener != null) {
                listener.initFailed("Init Error" + e.getMessage());
            }
        }
    }

    public void getBidResponse(Map<String, Object> dataMap, BidCallback callback) {
        if (isInit()) {
            executeBid(dataMap, callback);
            return;
        }
        if (dataMap == null) {
            if (callback != null) {
                callback.onBidFailed("FlatAds Bid Failed : DataMap is null");
            }
            return;
        }
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        if (TextUtils.isEmpty(adUnitId)) {
            onBidFailed(adUnitId, "FlatAds Bid Failed : AdUnitId is null", callback);
            return;
        }
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        init(appKey, new InitListener() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(String error) {
                onBidFailed(adUnitId, "FlatAds SDK init error: " + error, callback);
            }
        });
    }

    String getToken(String adUnitId) {
        return mAdToken.get(adUnitId);
    }

    void bidBanner(String adUnitId, com.openmediation.sdk.banner.AdSize adSize, BidCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    BannerAdView bannerAdView = new BannerAdView(MediationUtil.getContext());
                    bannerAdView.setAdUnitId(adUnitId);
                    int size = getAdSize(adSize);
                    RelativeLayout.LayoutParams layoutParams;
                    if (size == 1) {
                        layoutParams = new RelativeLayout.LayoutParams(
                                MediationUtil.dip2px(MediationUtil.getContext(), 300),
                                MediationUtil.dip2px(MediationUtil.getContext(), 250));
                    } else {
                        layoutParams = new RelativeLayout.LayoutParams(
                                MediationUtil.dip2px(MediationUtil.getContext(), 320),
                                MediationUtil.dip2px(MediationUtil.getContext(), 50));
                    }
                    bannerAdView.setLayoutParams(layoutParams);
                    bannerAdView.setBannerSize(size);
                    bannerAdView.setAdLoadListener(new AdLoadListener() {
                        @Override
                        public void onAdSucLoad(AdContent adContent) {
                            AdLog.getSingleton().LogD(TAG, "FlatAds BannerAd onAdSucLoad adUnitId : " + adUnitId);
                        }

                        @Override
                        public void onAdFailLoad(ErrorCode errorCode) {
                            String error = "FlatAds BannerAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg();
                            AdLog.getSingleton().LogE(TAG, error);
                            onBidFailed(adUnitId, error, callback);
                        }
                    });
                    bannerAdView.bidding(adUnitId, new AdBiddingListener() {
                        @Override
                        public void getBidding(float price, String token) {
                            AdLog.getSingleton().LogD(TAG, "BannerAd getBidding price: " + price + ", token : " + token);
                            mAdToken.put(adUnitId, token);
                            mBannerAds.put(adUnitId, bannerAdView);
                            onBidSuccess(adUnitId, price, token, null, callback);
                        }
                    });
                } catch (Throwable e) {
                    String error = "BannerAd bid error : " + e.getMessage();
                    AdLog.getSingleton().LogE(TAG, error);
                    onBidFailed(adUnitId, error, callback);
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
                                AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private static class InnerBannerListener implements AdShowListener {
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

    public void bidNative(String adUnitId, BidCallback callback) {
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
                    String error = "NativeAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg();
                    AdLog.getSingleton().LogE(TAG, error);
                    onBidFailed(adUnitId, error, callback);
                }

            };
            nativeAd.setAdListener(loadListener);
            nativeAd.bidding(new AdBiddingListener() {
                @Override
                public void getBidding(float price, String token) {
                    AdLog.getSingleton().LogD(TAG, "NativeAd getBidding price: " + price + ", token : " + token);
                    AdnAdInfo info = new AdnAdInfo();
                    FlatAdsNativeAdsConfig adsConfig = new FlatAdsNativeAdsConfig();
                    adsConfig.setNativeAd(nativeAd);
                    info.setAdnNativeAd(adsConfig);
                    mNativeAdToken.put(adsConfig, token);
                    onBidSuccess(adUnitId, price, token, info, callback);
                }
            });
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd bid error : " + e.getMessage());
            onBidFailed(adUnitId, e.getMessage(), callback);
        }
    }

    void loadNativeAdWithBid(String adUnitId, AdnAdInfo info, FlatAdsNativeAdsConfig config, NativeAdCallback callback) {
        try {
            String token = mNativeAdToken.remove(config);
            if (config.getNativeAd() == null || TextUtils.isEmpty(token)) {
                AdLog.getSingleton().LogE(TAG, "NativeAd Load Failed : No Fill");
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "No Fill"));
                }
                return;
            }
            NativeAd nativeAd = config.getNativeAd();
            nativeAd.setAdListener(new InnerNativeListener(adUnitId, info, config, nativeAd, callback));
            nativeAd.winBidding(token);
            nativeAd.loadAd(token);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd LoadFailed");
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private static class InnerNativeListener implements AdLoadListener {
        String adUnitId;
        NativeAd nativeAd;
        NativeAdCallback callback;
        FlatAdsNativeAdsConfig config;
        AdnAdInfo adInfo;

        private InnerNativeListener(String adUnitId, AdnAdInfo info, FlatAdsNativeAdsConfig config, NativeAd nativeAd, NativeAdCallback callback) {
            this.adUnitId = adUnitId;
            this.nativeAd = nativeAd;
            this.callback = callback;
            this.config = config;
            this.adInfo = info;
        }

        @Override
        public void onAdSucLoad(AdContent adContent) {
            AdLog.getSingleton().LogD(TAG, "NativeAd onAdSucLoad adUnitId : " + adUnitId);
            if (config == null) {
                config = new FlatAdsNativeAdsConfig();
            }
            config.setNativeAd(nativeAd);
            config.setAdContent(adContent);
            if (adInfo == null) {
                adInfo = new AdnAdInfo();
            }
            adInfo.setAdnNativeAd(config);
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
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
            }
        }
    }

    public void bidInterstitial(String adUnitId, BidCallback callback) {
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
                    String error = "InterstitialAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg();
                    AdLog.getSingleton().LogE(TAG, error);
                    onBidFailed(adUnitId, error, callback);
                }
            });
            interstitialAd.bidding(adUnitId, new AdBiddingListener() {
                @Override
                public void getBidding(float price, String token) {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd getBidding price: " + price + ", token : " + token);
                    mAdToken.put(adUnitId, token);
                    mIsAds.put(adUnitId, interstitialAd);
                    onBidSuccess(adUnitId, price, token, null, callback);
                }
            });
        } catch (Throwable e) {
            String error = "InterstitialAd bid error : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            onBidFailed(adUnitId, error, callback);
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
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
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
            if (callback != null) {
                callback.onInterstitialShowFailed(adUnitId, "InterstitialAd show failed: " + e.getMessage());
            }
        }
    }

    void bidVideo(String adUnitId, BidCallback callback) {
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
                    String error = "RewardedVideoAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg();
                    AdLog.getSingleton().LogE(TAG, error);
                    onBidFailed(adUnitId, error, callback);
                }
            });
            rewardedAd.bidding(adUnitId, new AdBiddingListener() {
                @Override
                public void getBidding(float price, String token) {
                    AdLog.getSingleton().LogD(TAG, "RewardedVideoAd getBidding price: " + price + ", token : " + token);
                    mAdToken.put(adUnitId, token);
                    mRvAds.put(adUnitId, rewardedAd);
                    onBidSuccess(adUnitId, price, token, null, callback);
                }
            });
        } catch (Throwable e) {
            String error = "RewardedVideoAd bid error : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            onBidFailed(adUnitId, error, callback);
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
                            callback.onRewardedShowFailed(adUnitId, "RewardedVideoAd onRewardedAdFailedToShow");
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
            if (callback != null) {
                callback.onRewardedShowFailed(adUnitId, "Unknown Error, " + e.getMessage());
            }
        }
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
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
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
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
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
                    int adSize = getAdSize(MediationUtil.getBannerDesc(extras));
                    RelativeLayout.LayoutParams layoutParams;
                    if (adSize == 1) {
                        layoutParams = new RelativeLayout.LayoutParams(
                                MediationUtil.dip2px(MediationUtil.getContext(), 300),
                                MediationUtil.dip2px(MediationUtil.getContext(), 250));
                    } else {
                        layoutParams = new RelativeLayout.LayoutParams(
                                MediationUtil.dip2px(MediationUtil.getContext(), 320),
                                MediationUtil.dip2px(MediationUtil.getContext(), 50));
                    }
                    bannerAdView.setLayoutParams(layoutParams);
                    bannerAdView.setBannerSize(adSize);
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
                                AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
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
            nativeAd.setAdListener(new InnerNativeListener(adUnitId, null, null, nativeAd, callback));
            nativeAd.loadAd();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd Load error : " + e.getMessage());
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        if (adType == BidConstance.INTERSTITIAL) {
            FlatAdsSingleTon.getInstance().bidInterstitial(adUnitId, callback);
        } else if (adType == BidConstance.VIDEO) {
            FlatAdsSingleTon.getInstance().bidVideo(adUnitId, callback);
        } else if (adType == BidConstance.NATIVE) {
            FlatAdsSingleTon.getInstance().bidNative(adUnitId, callback);
        } else if (adType == BidConstance.BANNER) {
            AdSize adSize = AdSize.BANNER;
            if (dataMap.containsKey(BidConstance.BID_BANNER_SIZE)) {
                adSize = (AdSize) dataMap.get(BidConstance.BID_BANNER_SIZE);
            }
            FlatAdsSingleTon.getInstance().bidBanner(adUnitId, adSize, callback);
        } else {
            if (callback != null) {
                callback.onBidFailed("unSupport bid type");
            }
        }
    }

    public void onBidSuccess(String adUnitId, float ecpm, String token, Object object, BidCallback callback) {
        if (callback != null) {
            BidResponse bidResponse = new BidResponse();
            bidResponse.setPrice(ecpm);
            bidResponse.setObject(object);
            bidResponse.setPayLoad(token);
            callback.onBidSuccess(bidResponse);
        }
    }

    public void onBidFailed(String adUnitId, String error, BidCallback callback) {
        if (callback != null) {
            callback.onBidFailed(error);
        }
    }

}
