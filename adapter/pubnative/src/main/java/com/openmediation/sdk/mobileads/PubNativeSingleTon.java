// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.utils.ResDownloader;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterError;
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
import com.openmediation.sdk.utils.WorkExecutor;

import net.pubnative.lite.sdk.HyBid;
import net.pubnative.lite.sdk.interstitial.HyBidInterstitialAd;
import net.pubnative.lite.sdk.interstitial.PNInterstitialAd;
import net.pubnative.lite.sdk.models.AdSize;
import net.pubnative.lite.sdk.models.NativeAd;
import net.pubnative.lite.sdk.request.HyBidNativeAdRequest;
import net.pubnative.lite.sdk.rewarded.HyBidRewardedAd;
import net.pubnative.lite.sdk.utils.PrebidUtils;
import net.pubnative.lite.sdk.views.HyBidAdView;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PubNativeSingleTon {

    private static final String TAG = "PubNativeSingleTon: ";
    private final ConcurrentHashMap<String, HyBidRewardedAd> mRvAds;
    private final ConcurrentHashMap<String, HyBidInterstitialAd> mIsAds;
    private final ConcurrentHashMap<String, HyBidAdView> mBannerAds;
    private final ConcurrentHashMap<String, PubNativeBannerListener> mBannerListeners;

    private PubNativeVideoCallback mVideoAdCallback;
    private PubNativeInterstitialCallback mInterstitialAdCallback;

    private volatile InitState mInitState = InitState.NOT_INIT;
    private final List<InitListener> mListeners = new CopyOnWriteArrayList<>();

    private static class HyBidHolder {
        private static final PubNativeSingleTon INSTANCE = new PubNativeSingleTon();
    }

    private PubNativeSingleTon() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mBannerListeners = new ConcurrentHashMap<>();
    }

    public static PubNativeSingleTon getInstance() {
        return HyBidHolder.INSTANCE;
    }

    public boolean isInit() {
        return mInitState == InitState.INIT_SUCCESS;
    }

    public synchronized void init(final Application application, final String appKey, InitListener listener) {
        if (application == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                listener.initFailed("Context is null or AppKey is empty");
            }
            AdLog.getSingleton().LogE("Init Failed: Context is null or AppKey is empty!");
            return;
        }
        if (InitState.INIT_SUCCESS == mInitState) {
            if (listener != null) {
                listener.initSuccess();
            }
            return;
        }
        if (listener != null) {
            mListeners.add(listener);
        }
        if (InitState.INIT_PENDING == mInitState) {
            return;
        }
        mInitState = InitState.INIT_PENDING;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                initOnMainThread(application, appKey);
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private void initOnMainThread(Application application, final String appKey) {
        HyBid.initialize(appKey, application, new HyBid.InitialisationListener() {
            @Override
            public void onInitialisationFinished(boolean success) {
                if (success) {
                    mInitState = InitState.INIT_SUCCESS;
                    AdLog.getSingleton().LogD("HyBid SDK initialized successfully");
                    for (InitListener listener : mListeners) {
                        if (listener != null) {
                            listener.initSuccess();
                        }
                    }
                } else {
                    mInitState = InitState.NOT_INIT;
                    AdLog.getSingleton().LogD("HyBid SDK initialized failed");
                    for (InitListener listener : mListeners) {
                        if (listener != null) {
                            listener.initFailed("HyBid SDK initialized failed");
                        }
                    }
                }
                mListeners.clear();
            }
        });
    }

    void getBidResponse(final Map<String, Object> dataMap, final BidCallback callback) {
        if (isInit()) {
            executeBid(dataMap, callback);
            return;
        }
        if (dataMap == null) {
            if (callback != null) {
                callback.onBidFailed("Bid Failed : DataMap is null");
            }
            return;
        }
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        if (TextUtils.isEmpty(adUnitId)) {
            if (callback != null) {
                callback.onBidFailed("Bid Failed : AdUnitId is null");
            }
            return;
        }
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        init(MediationUtil.getApplication(), appKey, new InitListener() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onBidFailed("PubNative SDK init error: " + error);
                }
            }
        });
    }

    void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        if (adType == BidConstance.BANNER) {
            String desc = "";
            Object size = dataMap.get(BidConstance.BID_BANNER_SIZE);
            if (size != null) {
                com.openmediation.sdk.banner.AdSize adSize = (com.openmediation.sdk.banner.AdSize) size;
                desc = adSize.getDescription();
            }
            PubNativeSingleTon.getInstance().loadBanner(adUnitId, getAdSize(desc), null, callback);
        } else if (adType == BidConstance.NATIVE) {
            PubNativeSingleTon.getInstance().loadNative(adUnitId, null, callback);
        } else if (adType == BidConstance.INTERSTITIAL) {
            PubNativeSingleTon.getInstance().loadInterstitial(adUnitId, null, callback);
        } else if (adType == BidConstance.VIDEO) {
            PubNativeSingleTon.getInstance().loadRewardedVideo(adUnitId, null, callback);
        } else {
            if (callback != null) {
                callback.onBidFailed("unSupport bid type");
            }
        }
    }

    void setVideoAdCallback(PubNativeVideoCallback callback) {
        mVideoAdCallback = callback;
    }

    void setInterstitialAdCallback(PubNativeInterstitialCallback callback) {
        mInterstitialAdCallback = callback;
    }

    void loadRewardedVideo(final String adUnitId, final RewardedVideoCallback adCallback, final BidCallback bidCallback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HyBidRewardedAdListener listener = new HyBidRewardedAdListener(adCallback, bidCallback);
                    HyBidRewardedAd rewarded = new HyBidRewardedAd(MediationUtil.getContext(), adUnitId, listener);
                    listener.setParameters(rewarded, adUnitId);
                    rewarded.load();
                } catch (Throwable e) {
                    String error = "PubNative RewardedVideo LoadFailed : " + e.getMessage();
                    AdLog.getSingleton().LogE(TAG, error);
                    if (adCallback != null) {
                        onBidFailed(error, adCallback);
                        AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "PubNativeAdapter", error);
                        adCallback.onRewardedVideoLoadFailed(adapterError);
                    }
                    if (bidCallback != null) {
                        onBidFailed(error, bidCallback);
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    boolean isRewardedVideoReady(final String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        HyBidRewardedAd rewardedAd = mRvAds.get(adUnitId);
        return rewardedAd != null && rewardedAd.isReady();
    }

    void showRewardedVideo(final String adUnitId) {
        HyBidRewardedAd rewardedAd = mRvAds.get(adUnitId);
        if (rewardedAd != null) {
            rewardedAd.show();
            mRvAds.remove(adUnitId);
        }
    }

    void loadInterstitial(final String adUnitId, final InterstitialAdCallback adCallback, final BidCallback bidCallback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HyBidInterstitialAdListener listener = new HyBidInterstitialAdListener(adCallback, bidCallback);
                    HyBidInterstitialAd ad = new HyBidInterstitialAd(MediationUtil.getContext(), adUnitId, listener);
                    listener.setParameters(ad, adUnitId);
                    ad.load();
                } catch (Throwable e) {
                    String error = "PubNative InterstitialAd LoadFailed : " + e.getMessage();
                    AdLog.getSingleton().LogE(TAG, error);
                    if (adCallback != null) {
                        onBidFailed(error, adCallback);
                        AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "PubNativeAdapter", error);
                        adCallback.onInterstitialAdLoadFailed(adapterError);
                    }
                    if (bidCallback != null) {
                        onBidFailed(error, bidCallback);
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    boolean isInterstitialReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        HyBidInterstitialAd interstitialAd = mIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isReady();
    }

    void showInterstitial(String adUnitId) {
        HyBidInterstitialAd interstitialAd = mIsAds.get(adUnitId);
        if (interstitialAd != null) {
            interstitialAd.show();
            mIsAds.remove(adUnitId);
        }
    }

    private class HyBidRewardedAdListener implements HyBidRewardedAd.Listener {
        HyBidRewardedAd mRewarded;
        String mAdUnitId;
        RewardedVideoCallback mAdCallback;
        BidCallback mBidCallback;

        public HyBidRewardedAdListener(RewardedVideoCallback adCallback, BidCallback bidCallback) {
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        public void setParameters(HyBidRewardedAd rewarded, String adUnitId) {
            mRewarded = rewarded;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onRewardedLoaded() {
            AdLog.getSingleton().LogD(TAG, "PubNative onRewardedLoaded PlacementId : " + mAdUnitId);
            mRvAds.put(mAdUnitId, mRewarded);
            if (mAdCallback != null) {
                onBidSuccess(mRewarded.getBidPoints(), null, mAdCallback);
                mAdCallback.onRewardedVideoLoadSuccess();
            }
            if (mBidCallback != null) {
                onBidSuccess(mRewarded.getBidPoints(), null, mBidCallback);
            }
        }

        @Override
        public void onRewardedLoadFailed(Throwable e) {
            String error = "PubNative RewardedVideo LoadFailed : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            if (mAdCallback != null) {
                onBidFailed(error, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "PubNativeAdapter", error);
                mAdCallback.onRewardedVideoLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed(error, mBidCallback);
            }
        }

        @Override
        public void onRewardedOpened() {
            if (getInstance().mVideoAdCallback == null) {
                return;
            }
            AdLog.getSingleton().LogD(TAG, "PubNative RewardedAd Opened");
            getInstance().mVideoAdCallback.onRewardedOpened(mAdUnitId);
        }

        @Override
        public void onRewardedClosed() {
            AdLog.getSingleton().LogD(TAG, "PubNative RewardedAd Closed");
            if (getInstance().mVideoAdCallback != null) {
                getInstance().mVideoAdCallback.onRewardedClosed(mAdUnitId);
            }
        }

        @Override
        public void onRewardedClick() {
            AdLog.getSingleton().LogD(TAG, "PubNative RewardedAd Click");
            if (getInstance().mVideoAdCallback != null) {
                getInstance().mVideoAdCallback.onRewardedClick(mAdUnitId);
            }
        }

        @Override
        public void onReward() {
            AdLog.getSingleton().LogD(TAG, "PubNative RewardedAd onReward");
            if (getInstance().mVideoAdCallback != null) {
                getInstance().mVideoAdCallback.onReward(mAdUnitId);
            }
        }
    }

    private class HyBidInterstitialAdListener implements PNInterstitialAd.Listener {
        HyBidInterstitialAd mInterstitialAd;
        String mAdUnitId;
        InterstitialAdCallback mAdCallback;
        BidCallback mBidCallback;

        public HyBidInterstitialAdListener(InterstitialAdCallback adCallback, BidCallback bidCallback) {
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        public void setParameters(HyBidInterstitialAd interstitialAd, String adUnitId) {
            mInterstitialAd = interstitialAd;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onInterstitialLoaded() {
            AdLog.getSingleton().LogD(TAG, "PubNative InterstitialAd onInterstitialLoaded PlacementId : " + mAdUnitId);
            mIsAds.put(mAdUnitId, mInterstitialAd);
            if (mAdCallback != null) {
                onBidSuccess(mInterstitialAd.getBidPoints(), null, mAdCallback);
                mAdCallback.onInterstitialAdLoadSuccess();
            }
            if (mBidCallback != null) {
                onBidSuccess(mInterstitialAd.getBidPoints(), null, mBidCallback);
            }
        }

        @Override
        public void onInterstitialLoadFailed(Throwable e) {
            String error = "PubNative InterstitialAd LoadFailed : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            if (mAdCallback != null) {
                onBidFailed(error, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "PubNativeAdapter", error);
                mAdCallback.onInterstitialAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed(error, mBidCallback);
            }
        }

        @Override
        public void onInterstitialImpression() {
            AdLog.getSingleton().LogD(TAG, "PubNative InterstitialAd Impression");
            if (getInstance().mInterstitialAdCallback != null) {
                getInstance().mInterstitialAdCallback.onInterstitialImpression(mAdUnitId);
            }
        }

        @Override
        public void onInterstitialDismissed() {
            AdLog.getSingleton().LogD(TAG, "PubNative InterstitialAd close");
            if (getInstance().mInterstitialAdCallback != null) {
                getInstance().mInterstitialAdCallback.onInterstitialDismissed(mAdUnitId);
            }
        }

        @Override
        public void onInterstitialClick() {
            AdLog.getSingleton().LogD(TAG, "PubNative InterstitialAd Click");
            if (getInstance().mInterstitialAdCallback != null) {
                getInstance().mInterstitialAdCallback.onInterstitialClick(mAdUnitId);
            }
        }
    }

    void loadBanner(final String adUnitId, final AdSize adSize, final BannerAdCallback adCallback, final BidCallback bidCallback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = MediationUtil.getContext();
                    final HyBidAdView bannerView = new HyBidAdView(context);
                    bannerView.setAdSize(adSize);
                    HyBidBannerAdListener listener = new HyBidBannerAdListener(adCallback, bidCallback);
                    listener.setParameters(adUnitId, bannerView);
                    bannerView.setAutoShowOnLoad(false);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            MediationUtil.dip2px(context, adSize.getWidth()),
                            MediationUtil.dip2px(context, adSize.getHeight()));
                    bannerView.setLayoutParams(layoutParams);
                    bannerView.load(adUnitId, listener);
                } catch (Throwable e) {
                    String error = "PubNative Banner LoadFailed : " + e.getMessage();
                    AdLog.getSingleton().LogE(TAG, error);
                    if (adCallback != null) {
                        onBidFailed(error, adCallback);
                        AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "PubNativeAdapter", error);
                        adCallback.onBannerAdLoadFailed(adapterError);
                    }
                    if (bidCallback != null) {
                        onBidFailed(error, bidCallback);
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    HyBidAdView getBannerAd(String adUnitId) {
        return mBannerAds.get(adUnitId);
    }

    HyBidAdView removeBannerAd(String adUnitId) {
        return mBannerAds.remove(adUnitId);
    }

    void destroyBannerAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            removeBannerListener(adUnitId);
            HyBidAdView adView = mBannerAds.remove(adUnitId);
            if (adView != null) {
                adView.destroy();
            }
        }
    }

    void addBannerListener(String adUnitId, PubNativeBannerListener listener) {
        if (!TextUtils.isEmpty(adUnitId) && listener != null) {
            mBannerListeners.put(adUnitId, listener);
        }
    }

    void removeBannerListener(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            mBannerListeners.remove(adUnitId);
        }
    }

    private class HyBidBannerAdListener implements HyBidAdView.Listener {
        HyBidAdView mBannerAdView;
        String mAdUnitId;
        BannerAdCallback mAdCallback;
        BidCallback mBidCallback;

        public HyBidBannerAdListener(BannerAdCallback adCallback, BidCallback bidCallback) {
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        public void setParameters(String adUnitId, HyBidAdView adView) {
            mBannerAdView = adView;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoaded() {
            AdLog.getSingleton().LogD(TAG, "PubNative Banner onAdLoaded : " + mAdUnitId);
            if (mAdCallback != null) {
                mBannerAdView.show();
                onBidSuccess(mBannerAdView.getBidPoints(), mBannerAdView, mAdCallback);
                mAdCallback.onBannerAdLoadSuccess(mBannerAdView);
            }
            if (mBidCallback != null) {
                mBannerAds.put(mAdUnitId, mBannerAdView);
                onBidSuccess(mBannerAdView.getBidPoints(), mBannerAdView, mBidCallback);
            }
        }

        @Override
        public void onAdLoadFailed(Throwable e) {
            String error = "PubNative Banner LoadFailed : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            if (mAdCallback != null) {
                onBidFailed(error, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "PubNativeAdapter", error);
                mAdCallback.onBannerAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed(error, mBidCallback);
            }
        }

        @Override
        public void onAdImpression() {
            AdLog.getSingleton().LogD(TAG, "PubNative Banner onAdImpression : " + mAdUnitId);
            PubNativeBannerListener listener = mBannerListeners.get(mAdUnitId);
            if (listener != null) {
                listener.onAdImpression(mAdUnitId);
            }
        }

        @Override
        public void onAdClick() {
            AdLog.getSingleton().LogD(TAG, "PubNative Banner onAdClick : " + mAdUnitId);
            PubNativeBannerListener listener = mBannerListeners.get(mAdUnitId);
            if (listener != null) {
                listener.onAdClick(mAdUnitId);
            }
        }
    }

    void loadNative(final String adUnitId, final NativeAdCallback adCallback, final BidCallback bidCallback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HyBidNativeAdListener listener = new HyBidNativeAdListener(adUnitId, adCallback, bidCallback);
                    HyBidNativeAdRequest nativeAdRequest = new HyBidNativeAdRequest();
                    nativeAdRequest.load(adUnitId, listener);
                } catch (Throwable e) {
                    String error = "PubNative Native LoadFailed : " + e.getMessage();
                    AdLog.getSingleton().LogE(TAG, error);
                    if (adCallback != null) {
                        onBidFailed(error, adCallback);
                        AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, "PubNativeAdapter", error);
                        adCallback.onNativeAdLoadFailed(adapterError);
                    }
                    if (bidCallback != null) {
                        onBidFailed(error, bidCallback);
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private class HyBidNativeAdListener implements HyBidNativeAdRequest.RequestListener {

        String mAdUnitId;
        NativeAdCallback mAdCallback;
        BidCallback mBidCallback;

        public HyBidNativeAdListener(String adUnitId, NativeAdCallback adCallback, BidCallback bidCallback) {
            mAdUnitId = adUnitId;
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        @Override
        public void onRequestSuccess(NativeAd nativeAd) {
            AdLog.getSingleton().LogD(TAG, "PubNative Native onAdLoaded : " + mAdUnitId);
            AdnAdInfo adInfo = new AdnAdInfo();
            adInfo.setAdnNativeAd(nativeAd);
            adInfo.setDesc(nativeAd.getDescription());
            adInfo.setType(MediationInfo.MEDIATION_ID_23);
            adInfo.setTitle(nativeAd.getTitle());
            adInfo.setCallToActionText(nativeAd.getCallToActionText());
            adInfo.setStarRating(nativeAd.getRating());
            if (mAdCallback != null) {
                onBidSuccess(nativeAd.getBidPoints(), adInfo, mAdCallback);
                downloadRes(adInfo, nativeAd, mAdCallback);
            }
            if (mBidCallback != null) {
                onBidSuccess(nativeAd.getBidPoints(), adInfo, mBidCallback);
            }
        }

        @Override
        public void onRequestFail(Throwable e) {
            String error = "PubNative Native LoadFailed : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            if (mAdCallback != null) {
                onBidFailed(error, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "PubNativeAdapter", error);
                mAdCallback.onNativeAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed(error, mBidCallback);
            }
        }
    }

    void downloadRes(final AdnAdInfo adInfo, final NativeAd ad, final NativeAdCallback callback) {
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!TextUtils.isEmpty(ad.getBannerUrl())) {
                        File file = ResDownloader.downloadFile(ad.getBannerUrl());
                        if (file == null || !file.exists()) {
                            if (callback != null) {
                                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                        AdapterErrorBuilder.AD_UNIT_NATIVE, "PubNativeAdapter", "NativeAd Load Failed"));
                            }
                            return;
                        }
                        AdLog.getSingleton().LogD("PubNativeNative", "Content File = " + file);
                    }
                    if (!TextUtils.isEmpty(ad.getIconUrl())) {
                        File file = ResDownloader.downloadFile(ad.getIconUrl());
                        if (file == null || !file.exists()) {
                            if (callback != null) {
                                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                        AdapterErrorBuilder.AD_UNIT_NATIVE, "PubNativeAdapter", "NativeAd Load Failed"));
                            }
                            return;
                        }
                        AdLog.getSingleton().LogD("PubNativeNative", "Icon File = " + file);
                    }
                    if (callback != null) {
                        callback.onNativeAdLoadSuccess(adInfo);
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, "PubNativeAdapter", "NativeAd Load Failed: " + e.getMessage()));
                    }
                }
            }
        });
    }

    void onBidSuccess(Integer points, Object object, BidCallback callback) {
        String price = "0";
        if (points != null) {
            price = PrebidUtils.getBidFromPoints(points, PrebidUtils.KeywordMode.THREE_DECIMALS);
        }
        BidResponse bidResponse = new BidResponse();
        bidResponse.setPrice(Double.parseDouble(price));
        bidResponse.setObject(object);
        callback.onBidSuccess(bidResponse);
    }

    public void onBidFailed(String error, BidCallback callback) {
        callback.onBidFailed(error);
    }

    AdSize getAdSize(String bannerDesc) {
        switch (bannerDesc) {
            case MediationUtil.DESC_LEADERBOARD:
                return AdSize.SIZE_728x90;
            case MediationUtil.DESC_RECTANGLE:
                return AdSize.SIZE_300x250;
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(MediationUtil.getContext())) {
                    return AdSize.SIZE_728x90;
                } else {
                    return AdSize.SIZE_320x50;
                }
            default:
                return AdSize.SIZE_320x50;
        }
    }

    enum InitState {
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
        INIT_SUCCESS,
    }

    interface InitListener {
        void initSuccess();

        void initFailed(String error);
    }

}
