// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.utils.AdLog;

import net.pubnative.lite.sdk.HyBid;
import net.pubnative.lite.sdk.interstitial.HyBidInterstitialAd;
import net.pubnative.lite.sdk.interstitial.PNInterstitialAd;
import net.pubnative.lite.sdk.models.AdSize;
import net.pubnative.lite.sdk.models.NativeAd;
import net.pubnative.lite.sdk.request.HyBidNativeAdRequest;
import net.pubnative.lite.sdk.rewarded.HyBidRewardedAd;
import net.pubnative.lite.sdk.utils.PrebidUtils;
import net.pubnative.lite.sdk.views.HyBidAdView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PubNativeSingleTon {

    private static final String TAG = "PubNativeSingleTon: ";

    protected static final String DESC_BANNER = "BANNER";
    protected static final String DESC_LEADERBOARD = "LEADERBOARD";
    protected static final String DESC_RECTANGLE = "RECTANGLE";
    protected static final String DESC_SMART = "SMART";

    private final ConcurrentHashMap<String, HyBidRewardedAd> mRvAds;
    private final ConcurrentHashMap<String, HyBidInterstitialAd> mIsAds;
    private final ConcurrentHashMap<String, HyBidAdView> mBannerAds;
    private final ConcurrentHashMap<String, NativeAd> mNativeAds;
    private final ConcurrentHashMap<String, PubNativeBannerListener> mBannerListeners;

    private final ConcurrentMap<String, PubNativeCallback> mBidCallbacks;
    private final ConcurrentMap<String, String> mBidError;

    private PubNativeVideoCallback mVideoAdCallback;
    private PubNativeInterstitialCallback mInterstitialAdCallback;

    private volatile InitState mInitState = InitState.NOT_INIT;
    private final List<InitListener> mListeners = new CopyOnWriteArrayList<>();
    private final Handler mMainHandler;

    private static class HyBidHolder {
        private static final PubNativeSingleTon INSTANCE = new PubNativeSingleTon();
    }

    private PubNativeSingleTon() {
        mMainHandler = new Handler(Looper.getMainLooper());
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBidCallbacks = new ConcurrentHashMap<>();
        mBidError = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mBannerListeners = new ConcurrentHashMap<>();
        mNativeAds = new ConcurrentHashMap<>();
    }

    public static PubNativeSingleTon getInstance() {
        return HyBidHolder.INSTANCE;
    }

    public synchronized void init(final Activity activity, final String appKey, InitListener listener) {
        if (activity == null || TextUtils.isEmpty(appKey)) {
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
        if (Looper.myLooper() == Looper.getMainLooper()) {
            initOnMainThread(activity, appKey);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    initOnMainThread(activity, appKey);
                }
            });
        }
    }

    private void initOnMainThread(Activity activity, final String appKey) {
        HyBid.initialize(appKey, activity.getApplication(), new HyBid.InitialisationListener() {
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

    void addBidCallback(String placementId, PubNativeCallback callback) {
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
        return null;
    }

    void setVideoAdCallback(PubNativeVideoCallback callback) {
        mVideoAdCallback = callback;
    }

    void setInterstitialAdCallback(PubNativeInterstitialCallback callback) {
        mInterstitialAdCallback = callback;
    }

    void loadRewardedVideo(final Activity activity, String appKey, final String adUnitId) {
        PubNativeSingleTon.getInstance().init(activity, appKey, new InitListener() {
            @Override
            public void initSuccess() {
                loadRewardedVideoOnMainThread(activity, adUnitId);
            }

            @Override
            public void initFailed(String error) {
                AdLog.getSingleton().LogE(TAG, "PubNative RewardedVideo InitFailed : " + error);
                bidFailed(adUnitId, error);
            }
        });
    }

    private void loadRewardedVideoOnMainThread(final Activity activity, final String adUnitId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HyBidRewardedAdListener listener = new HyBidRewardedAdListener();
                    HyBidRewardedAd rewarded = new HyBidRewardedAd(activity, adUnitId, listener);
                    listener.setParameters(rewarded, adUnitId);
                    rewarded.load();
                } catch (Exception e) {
                    AdLog.getSingleton().LogE(TAG, "loadRewardedVideo Error : " + e.getMessage());
                    bidFailed(adUnitId, e.getMessage());
                }
            }
        };
        mMainHandler.post(runnable);
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

    void loadInterstitial(final Activity activity, String appKey, final String adUnitId) {
        PubNativeSingleTon.getInstance().init(activity, appKey, new InitListener() {
            @Override
            public void initSuccess() {
                loadInterstitialOnMainThread(activity, adUnitId);
            }

            @Override
            public void initFailed(String error) {
                AdLog.getSingleton().LogE(TAG, "PubNative Interstitial InitFailed : " + error);
                bidFailed(adUnitId, error);
            }
        });
    }

    private void loadInterstitialOnMainThread(final Activity activity, final String adUnitId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HyBidInterstitialAdListener listener = new HyBidInterstitialAdListener();
                    HyBidInterstitialAd ad = new HyBidInterstitialAd(activity, adUnitId, listener);
                    listener.setParameters(ad, adUnitId);
                    ad.load();
                } catch (Exception e) {
                    AdLog.getSingleton().LogE(TAG, "loadInterstitial Error : " + e.getMessage());
                    bidFailed(adUnitId, e.getMessage());
                }
            }
        };
        mMainHandler.post(runnable);
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

        public HyBidRewardedAdListener() {
        }

        public void setParameters(HyBidRewardedAd rewarded, String adUnitId) {
            mRewarded = rewarded;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onRewardedLoaded() {
            AdLog.getSingleton().LogD(TAG, "PubNative onRewardedLoaded PlacementId : " + mAdUnitId);
            mRvAds.put(mAdUnitId, mRewarded);
            bidSuccess(mAdUnitId, mRewarded.getBidPoints());
        }

        @Override
        public void onRewardedLoadFailed(Throwable error) {
            AdLog.getSingleton().LogE(TAG, "PubNative RewardedAd LoadFailed : " + error.getMessage());
            bidFailed(mAdUnitId, error.getMessage());
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

        public HyBidInterstitialAdListener() {
        }

        public void setParameters(HyBidInterstitialAd interstitialAd, String adUnitId) {
            mInterstitialAd = interstitialAd;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onInterstitialLoaded() {
            AdLog.getSingleton().LogD(TAG, "PubNative InterstitialAd onInterstitialLoaded PlacementId : " + mAdUnitId);
            mIsAds.put(mAdUnitId, mInterstitialAd);
            bidSuccess(mAdUnitId, mInterstitialAd.getBidPoints());
        }

        @Override
        public void onInterstitialLoadFailed(Throwable error) {
            AdLog.getSingleton().LogE(TAG, "PubNative InterstitialAd LoadFailed : " + error.getMessage());
            bidFailed(mAdUnitId, error.getMessage());
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

    void loadBanner(final Activity activity, String appKey, final String adUnitId, final com.openmediation.sdk.banner.AdSize adSize) {
        PubNativeSingleTon.getInstance().init(activity, appKey, new InitListener() {
            @Override
            public void initSuccess() {
                loadBannerOnMainThread(activity, adUnitId, adSize);
            }

            @Override
            public void initFailed(String error) {
                AdLog.getSingleton().LogE(TAG, "PubNative Banner InitFailed : " + error);
                bidFailed(adUnitId, error);
            }
        });
    }

    private void loadBannerOnMainThread(final Activity activity, final String adUnitId, final com.openmediation.sdk.banner.AdSize adSize) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    AdSize bannerSize = getAdSize(activity, adSize);
                    final HyBidAdView bannerView = new HyBidAdView(activity.getApplicationContext());
                    bannerView.setAdSize(bannerSize);
                    HyBidBannerAdListener listener = new HyBidBannerAdListener();
                    listener.setParameters(adUnitId, bannerView);
                    bannerView.setAutoShowOnLoad(false);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            dpToPixels(activity, bannerSize.getWidth()),
                            dpToPixels(activity, bannerSize.getHeight()));
                    bannerView.setLayoutParams(layoutParams);
                    bannerView.load(adUnitId, listener);
                } catch (Exception e) {
                    AdLog.getSingleton().LogE(TAG, "loadBanner Error : " + e.getMessage());
                    bidFailed(adUnitId, e.getMessage());
                }
            }
        };
        mMainHandler.post(runnable);
    }

    HyBidAdView getBannerAd(String adUnitId) {
        return mBannerAds.remove(adUnitId);
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

        public HyBidBannerAdListener() {
        }

        public void setParameters(String adUnitId, HyBidAdView adView) {
            mBannerAdView = adView;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoaded() {
            AdLog.getSingleton().LogD(TAG, "PubNative Banner onAdLoaded : " + mAdUnitId);
            mBannerAds.put(mAdUnitId, mBannerAdView);
            bidSuccess(mAdUnitId, mBannerAdView.getBidPoints());
        }

        @Override
        public void onAdLoadFailed(Throwable error) {
            AdLog.getSingleton().LogE(TAG, "PubNative Banner LoadFailed : " + error.getMessage());
            bidFailed(mAdUnitId, error.getMessage());
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

    void loadNative(final Activity activity, String appKey, final String adUnitId) {
        PubNativeSingleTon.getInstance().init(activity, appKey, new InitListener() {
            @Override
            public void initSuccess() {
                loadNativeOnMainThread(adUnitId);
            }

            @Override
            public void initFailed(String error) {
                AdLog.getSingleton().LogE(TAG, "PubNative Banner InitFailed : " + error);
                bidFailed(adUnitId, error);
            }
        });
    }

    private void loadNativeOnMainThread(final String adUnitId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HyBidNativeAdListener listener = new HyBidNativeAdListener(adUnitId);
                    HyBidNativeAdRequest nativeAdRequest = new HyBidNativeAdRequest();
                    nativeAdRequest.load(adUnitId, listener);
                } catch (Exception e) {
                    AdLog.getSingleton().LogE(TAG, "loadNative Error : " + e.getMessage());
                    bidFailed(adUnitId, e.getMessage());
                }
            }
        };
        mMainHandler.post(runnable);
    }

    NativeAd getNativeAd(String adUnitId) {
        return mNativeAds.remove(adUnitId);
    }

    private class HyBidNativeAdListener implements HyBidNativeAdRequest.RequestListener {

        String mAdUnitId;

        public HyBidNativeAdListener(String adUnitId) {
            mAdUnitId = adUnitId;
        }

        @Override
        public void onRequestSuccess(NativeAd nativeAd) {
            if (nativeAd == null) {
                bidFailed(mAdUnitId, "No Fill");
                return;
            }
            AdLog.getSingleton().LogD(TAG, "PubNative Native onAdLoaded : " + mAdUnitId);
            mNativeAds.put(mAdUnitId, nativeAd);
            bidSuccess(mAdUnitId, nativeAd.getBidPoints());
        }

        @Override
        public void onRequestFail(Throwable throwable) {
            AdLog.getSingleton().LogE(TAG, "PubNative Native LoadFailed : " + throwable.getMessage());
            bidFailed(mAdUnitId, throwable.getMessage());
        }
    }

    private void bidSuccess(String adUnitId, Integer points) {
        if (getInstance().mBidCallbacks.containsKey(adUnitId)) {
            Map<String, String> map = new HashMap<>();
            // 1 point = $ 0.001
            String price = "0";
            if (points != null) {
                price = PrebidUtils.getBidFromPoints(points, PrebidUtils.KeywordMode.THREE_DECIMALS);
            }
            map.put(PubNativeBidAdapter.PRICE, price);
            getInstance().mBidCallbacks.get(adUnitId).onBidSuccess(adUnitId, map);
        }
        getInstance().mBidError.remove(adUnitId);
    }

    private void bidFailed(String adUnitId, String error) {
        if (getInstance().mBidCallbacks.containsKey(adUnitId)) {
            getInstance().mBidCallbacks.get(adUnitId).onBidFailed(adUnitId, error);
        }
        getInstance().mBidError.put(adUnitId, error);
    }

    private AdSize getAdSize(Context context, com.openmediation.sdk.banner.AdSize adSize) {
        String bannerDesc = "";
        if (adSize != null) {
            bannerDesc = adSize.getDescription();
        }
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return AdSize.SIZE_728x90;
            case DESC_RECTANGLE:
                return AdSize.SIZE_300x250;
            case DESC_SMART:
                if (CustomBannerEvent.isLargeScreen(context)) {
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

    private static int dpToPixels(Context context, int dpSize) {
        if (context == null) {
            return dpSize;
        } else {
            return (int) TypedValue.applyDimension(1, (float) dpSize, context.getResources().getDisplayMetrics());
        }
    }

}
