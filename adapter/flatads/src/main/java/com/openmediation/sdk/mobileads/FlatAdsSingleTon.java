// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.flatads.sdk.FlatAdSDK;
import com.flatads.sdk.builder.InterstitialAd;
import com.flatads.sdk.builder.NativeAd;
import com.flatads.sdk.builder.RewardedAd;
import com.flatads.sdk.callback.AdBiddingListener;
import com.flatads.sdk.callback.InitListener;
import com.flatads.sdk.callback.InterstitialAdListener;
import com.flatads.sdk.callback.NativeAdListener;
import com.flatads.sdk.callback.RewardedAdListener;
import com.flatads.sdk.response.Ad;
import com.flatads.sdk.statics.ErrorCode;
import com.flatads.sdk.ui.view.BannerAdView;
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BidCallback;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class FlatAdsSingleTon {

    public static final String TAG = "OM-FlatAds ";

    private boolean isInit = false;

    private final ConcurrentHashMap<String, RewardedAd> mRvAds;
    private final ConcurrentHashMap<String, InterstitialAd> mIsAds;
    private final ConcurrentHashMap<String, BannerAdView> mBannerAds;
    private final CopyOnWriteArraySet<String> mBidAd;

    private static class Holder {
        private static final FlatAdsSingleTon INSTANCE = new FlatAdsSingleTon();
    }

    private FlatAdsSingleTon() {
        mBannerAds = new ConcurrentHashMap<>();
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBidAd = new CopyOnWriteArraySet<>();
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
            bannerAdView.stop();
        }
    }

    public boolean isInit() {
        return isInit;
    }

    public synchronized void init(String appKey, InitListener listener) {
        if (MediationUtil.getContext() == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                listener.onFailure(-1, "Context is null or AppKey is empty");
            }
            AdLog.getSingleton().LogE(TAG, "Init Failed: Context is null or AppKey is empty!");
            return;
        }
        if (isInit()) {
            if (listener != null) {
                listener.onSuccess();
            }
            return;
        }
        try {
            String[] tmp = appKey.split("#");
            String appId = tmp[0];
            String token = tmp[1];
            FlatAdSDK.initialize(MediationUtil.getApplication(), appId, token, new InitListener() {
                @Override
                public void onSuccess() {
                    AdLog.getSingleton().LogD(TAG, "Init Success");
                    isInit = true;
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }

                @Override
                public void onFailure(int code, String msg) {
                    isInit = false;
                    if (listener != null) {
                        listener.onFailure(code, msg);
                    }
                }
            });
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "Init Error: " + e.getMessage());
            if (listener != null) {
                listener.onFailure(-1, "Init Error" + e.getMessage());
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
            onBidFailed("FlatAds Bid Failed : AdUnitId is null", callback);
            return;
        }
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        init(appKey, new InitListener() {
            @Override
            public void onSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void onFailure(int code, String msg) {
                onBidFailed("FlatAds SDK init code: " + code + ", error: " + msg, callback);
            }
        });
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
                    bannerAdView.bidding((isGetAd, price) -> {
                        AdLog.getSingleton().LogD(TAG, "BannerAd getBidding price: " + price + ", isGetAd : " + isGetAd);
                        if (isGetAd) {
                            mBannerAds.put(adUnitId, bannerAdView);
                            onBidSuccess(adUnitId, price, null, callback);
                        } else {
                            onBidFailed("No Fill", callback);
                        }
                    });
                } catch (Throwable e) {
                    String error = "BannerAd bid error : " + e.getMessage();
                    AdLog.getSingleton().LogE(TAG, error);
                    onBidFailed(error, callback);
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    public BannerAdView getBannerAdView(String adUnitId) {
        return mBannerAds.get(adUnitId);
    }

    public void petBannerAdView(String adUnitId, BannerAdView adView) {
        mBannerAds.put(adUnitId, adView);
    }

    public void bidNative(String adUnitId, BidCallback callback) {
        try {
            NativeAd nativeAd = new NativeAd(MediationUtil.getContext(), adUnitId);
            nativeAd.setAdListener(new NativeAdListener() {
                @Override
                public void onAdLoadSuc(Ad ad) {
                    AdLog.getSingleton().LogD(TAG, "NativeAd onAdSucLoad adUnitId : " + adUnitId);
                }

                @Override
                public void onAdLoadFail(ErrorCode errorCode) {
                    String error = "NativeAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg();
                    AdLog.getSingleton().LogE(TAG, error);
                    onBidFailed(error, callback);
                }

                @Override
                public void onAdExposure() {
                    AdLog.getSingleton().LogD(TAG, "NativeAd onAdExposure adUnitId : " + adUnitId);
                }

                @Override
                public void onAdClick() {
                    AdLog.getSingleton().LogD(TAG, "NativeAd onAdClick adUnitId : " + adUnitId);
                }

                @Override
                public void onAdDestroy() {
                    AdLog.getSingleton().LogD(TAG, "NativeAd onAdDestroy adUnitId : " + adUnitId);
                }
            });
            nativeAd.bidding(new AdBiddingListener() {
                @Override
                public void getBidding(boolean isGetAd, float price) {
                    AdLog.getSingleton().LogD(TAG, "NativeAd getBidding price: " + price + ", token : " + isGetAd);
                    if (isGetAd) {
                        AdnAdInfo info = new AdnAdInfo();
                        info.setAdnNativeAd(nativeAd);
                        onBidSuccess(adUnitId, price, info, callback);
                    } else {
                        onBidFailed("No Fill", callback);
                    }
                }
            });
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd bid error : " + e.getMessage());
            onBidFailed(e.getMessage(), callback);
        }
    }

    public void bidInterstitial(String adUnitId, BidCallback callback) {
        try {
            InterstitialAd interstitialAd = new InterstitialAd(MediationUtil.getContext(), adUnitId);
            interstitialAd.bidding(new AdBiddingListener() {
                @Override
                public void getBidding(boolean isGetAd, float price) {
                    AdLog.getSingleton().LogD(TAG, "InterstitialAd getBidding isGetAd: " + isGetAd + ", price : " + price);
                    if (isGetAd) {
                        mBidAd.add(adUnitId);
                        mIsAds.put(adUnitId, interstitialAd);
                        onBidSuccess(adUnitId, price, null, callback);
                    } else {
                        onBidFailed("No Fill", callback);
                    }
                }
            });
        } catch (Throwable e) {
            String error = "InterstitialAd bid error : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            onBidFailed(error, callback);
        }
    }

    void loadInterstitialAdWithBid(final String adUnitId, InterstitialAdCallback callback) {
        try {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd Load bid adUnitId : " + adUnitId);
            InterstitialAd interstitialAd = mIsAds.get(adUnitId);
            if (interstitialAd != null) {
//                interstitialAd.winBidding();
                interstitialAd.loadAd();
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
        return interstitialAd != null && interstitialAd.isReady();
    }

    void showInterstitialAd(String adUnitId, InterstitialAdCallback callback) {
        try {
            InterstitialAd interstitialAd = mIsAds.remove(adUnitId);
            interstitialAd.setAdListener(new InnerInterstitialAdListener(adUnitId, interstitialAd, callback));
            if (mBidAd.remove(adUnitId)) {
                interstitialAd.winBidding();
            }
            interstitialAd.show();
        } catch (Throwable e) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd show failed: " + e.getMessage());
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "FlatAdsAdapter", e.getMessage()));
            }
        }
    }

    private class InnerInterstitialAdListener implements InterstitialAdListener {

        String adUnitId;
        InterstitialAd interstitialAd;
        InterstitialAdCallback callback;

        InnerInterstitialAdListener(String adUnitId, InterstitialAd interstitialAd, InterstitialAdCallback callback) {
            this.adUnitId = adUnitId;
            this.interstitialAd = interstitialAd;
            this.callback = callback;
        }

        @Override
        public void onAdExposure() {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdExposure adUnitId: " + adUnitId);
            if (callback != null) {
                callback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onAdClick() {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdClick adUnitId : " + adUnitId);
            if (callback != null) {
                callback.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdClose() {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdClose adUnitId : " + adUnitId);
            if (callback != null) {
                callback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onAdLoadSuc() {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdSucLoad adUnitId : " + adUnitId);
            mIsAds.put(adUnitId, interstitialAd);
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onAdLoadFail(ErrorCode errorCode) {
            String error = "InterstitialAd onAdLoadFail: " + errorCode.getCode() + ", " + errorCode.getMsg();
            AdLog.getSingleton().LogE(TAG, error);
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", error));
            }
        }
    }

    void bidVideo(String adUnitId, BidCallback callback) {
        try {
            RewardedAd rewardedAd = new RewardedAd(MediationUtil.getContext(), adUnitId);
            rewardedAd.bidding((isGetAd, price) -> {
                AdLog.getSingleton().LogD(TAG, "RewardedVideoAd getBidding isGetAd: " + isGetAd + ", price : " + price);
                if (isGetAd) {
                    mBidAd.add(adUnitId);
                    mRvAds.put(adUnitId, rewardedAd);
                    onBidSuccess(adUnitId, price, null, callback);
                } else {
                    onBidFailed("No Fill", callback);
                }
            });
        } catch (Throwable e) {
            String error = "RewardedVideoAd bid error : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            onBidFailed(error, callback);
        }
    }

    void loadRewardedVideoWithBid(final String adUnitId, RewardedVideoCallback callback) {
        RewardedAd rewardedAd = mRvAds.get(adUnitId);
        if (rewardedAd != null) {
//            rewardedAd.winBidding();
            rewardedAd.setAdListener(new InnerRewardedVideoListener(adUnitId, rewardedAd, callback));
            rewardedAd.loadAd();
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
        return rewardedAd != null && rewardedAd.isReady();
    }

    void showRewardedVideo(String adUnitId, RewardedVideoCallback callback) {
        try {
            RewardedAd rewardedAd = mRvAds.remove(adUnitId);
            if (rewardedAd != null) {
                rewardedAd.setAdListener(new InnerRewardedVideoListener(adUnitId, rewardedAd, callback));
                if (mBidAd.remove(adUnitId)) {
                    rewardedAd.winBidding();
                }
                rewardedAd.show();
            }
        } catch (Throwable e) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd show failed: " + e.getMessage());
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", e.getMessage()));
            }
        }
    }

    private class InnerRewardedVideoListener implements RewardedAdListener {
        RewardedVideoCallback callback;
        String adUnitId;
        RewardedAd rewardedAd;

        InnerRewardedVideoListener(String adUnitId, RewardedAd rewardedAd, RewardedVideoCallback callback) {
            this.callback = callback;
            this.adUnitId = adUnitId;
            this.rewardedAd = rewardedAd;
        }

        @Override
        public void onUserEarnedReward() {
            AdLog.getSingleton().LogD(TAG, "RewardedVideo onUserEarnedReward adUnitId: " + adUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onAdFailedToShow() {
            AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdFailedToShow adUnitId: " + adUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", "onAdFailedToShow"));
            }
        }

        @Override
        public void onAdExposure() {
            AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdExposure adUnitId: " + adUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onAdClick() {
            AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdClick adUnitId: " + adUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onAdClose() {
            AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdClose adUnitId: " + adUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onAdLoadSuc() {
            AdLog.getSingleton().LogD(TAG, "RewardedVideo onAdLoadSuc adUnitId: " + adUnitId);
            mRvAds.put(adUnitId, rewardedAd);
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onAdLoadFail(ErrorCode errorCode) {
            String error = "RewardedVideo LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg();
            AdLog.getSingleton().LogD(TAG, error);
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
            }
        }
    }

    public int getAdSize(com.openmediation.sdk.banner.AdSize adSize) {
        String bannerDesc = "";
        if (adSize != null) {
            bannerDesc = adSize.getDescription();
        }
        return getAdSize(bannerDesc);
    }

    public int getAdSize(String desc) {
        if (MediationUtil.DESC_RECTANGLE.equals(desc)) {
            return 1;
        }
        return 2;
    }

    void loadInterstitialAd(final String adUnitId, InterstitialAdCallback callback) {
        try {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd Load adUnitId : " + adUnitId);
            InterstitialAd interstitialAd = new InterstitialAd(MediationUtil.getContext(), adUnitId);
            interstitialAd.setAdListener(new InnerInterstitialAdListener(adUnitId, interstitialAd, callback));
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
            RewardedAd rewardedAd = new RewardedAd(MediationUtil.getContext(), adUnitId);
            rewardedAd.setAdListener(new InnerRewardedVideoListener(adUnitId, rewardedAd, callback));
            rewardedAd.loadAd();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd bid error : " + e.getMessage());
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
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

    public void onBidSuccess(String adUnitId, float ecpm, Object object, BidCallback callback) {
        if (callback != null) {
            BidResponse bidResponse = new BidResponse();
            bidResponse.setPrice(ecpm);
            bidResponse.setObject(object);
//            bidResponse.setPayLoad(token);
            callback.onBidSuccess(bidResponse);
        }
    }

    public void onBidFailed(String error, BidCallback callback) {
        if (callback != null) {
            callback.onBidFailed(error);
        }
    }

}
