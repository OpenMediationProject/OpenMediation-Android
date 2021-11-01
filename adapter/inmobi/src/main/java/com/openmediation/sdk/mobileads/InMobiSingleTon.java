/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMobiSingleTon {
    private static final String TAG = "InMobiSingleTon";
    private static final String DESC_LEADERBOARD = "LEADERBOARD";
    private static final String DESC_RECTANGLE = "RECTANGLE";
    private static final String DESC_SMART = "SMART";

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

    private volatile InitState mInitState = InitState.NOT_INIT;
    private final List<InMobiInitCallback> mInitCallbacks = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, InMobiBidCallback> mBidCallbacks;
    private final ConcurrentMap<String, String> mBidError;

    private final ConcurrentMap<String, InMobiInterstitial> mRvAds;
    private final ConcurrentMap<String, InMobiInterstitial> mIsAds;
    private final ConcurrentMap<String, InMobiBanner> mBnAds;
    private final ConcurrentMap<String, InMobiBanner> mBnViewAds;
    private final ConcurrentMap<String, InMobiBannerCallback> mBannerCallbacks;

    private InMobiInterstitialCallback mInterstitialAdCallback;
    private InMobiVideoCallback mVideoAdCallback;

    private static final class ImHolder {
        private static final InMobiSingleTon INSTANCE = new InMobiSingleTon();
    }

    private InMobiSingleTon() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBnAds = new ConcurrentHashMap<>();
        mBnViewAds = new ConcurrentHashMap<>();
        mBidCallbacks = new ConcurrentHashMap<>();
        mBidError = new ConcurrentHashMap<>();
        mBannerCallbacks = new ConcurrentHashMap<>();
    }

    public static InMobiSingleTon getInstance() {
        return ImHolder.INSTANCE;
    }

    public void init(Context context, String appKey, final InMobiInitCallback initCallback) {
        try {
            if (TextUtils.isEmpty(appKey)) {
                if (initCallback != null) {
                    initCallback.initFailed("app key is empty");
                }
                return;
            }

            if (InitState.INIT_SUCCESS == mInitState) {
                if (initCallback != null) {
                    initCallback.initSuccess();
                }
                return;
            }
            if (initCallback != null) {
                mInitCallbacks.add(initCallback);
            }
            if (InitState.INIT_PENDING == mInitState) {
                return;
            }
            mInitState = InitState.INIT_PENDING;
            String[] tmp = appKey.split("#");
            String accountId = tmp[0];
            InMobiSdk.init(context, accountId, null, new SdkInitializationListener() {
                @Override
                public void onInitializationComplete(Error error) {
                    if (error == null) {
                        mInitState = InitState.INIT_SUCCESS;
                        AdLog.getSingleton().LogD("InMobi SDK initialized successfully");
                        for (InMobiInitCallback callback : mInitCallbacks) {
                            if (callback != null) {
                                callback.initSuccess();
                            }
                        }
                    } else {
                        mInitState = InitState.NOT_INIT;
                        AdLog.getSingleton().LogD("InMobi SDK initialized failed: " + error);
                        for (InMobiInitCallback callback : mInitCallbacks) {
                            if (callback != null) {
                                callback.initFailed(error.getMessage());
                            }
                        }
                    }
                    mInitCallbacks.clear();
                }
            });
        } catch (Exception e) {
            mInitState = InitState.NOT_INIT;
            AdLog.getSingleton().LogE("OM-InMobi", e.getMessage());
            for (InMobiInitCallback callback : mInitCallbacks) {
                if (callback != null) {
                    callback.initFailed(e.getMessage());
                }
            }
            mInitCallbacks.clear();
        }
    }

    boolean isInit() {
        return mInitState == InitState.INIT_SUCCESS;
    }

    void setInterstitialAdCallback(InMobiInterstitialCallback callback) {
        mInterstitialAdCallback = callback;
    }

    void setVideoAdCallback(InMobiVideoCallback callback) {
        mVideoAdCallback = callback;
    }

    void addBidCallback(String placementId, InMobiBidCallback callback) {
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

    void loadBanner(String adUnitId, com.openmediation.sdk.banner.AdSize adSize) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InMobiBanner banner = new InMobiBanner(MediationUtil.getContext(), Long.parseLong(adUnitId));
                int[] size = getAdSize(adSize);
                BnListener listener = new BnListener(adUnitId);
                banner.setListener(listener);
                banner.setBannerSize(size[0], size[1]);
                banner.setRefreshInterval(0);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        MediationUtil.dip2px(MediationUtil.getContext(), size[0]),
                        MediationUtil.dip2px(MediationUtil.getContext(), size[1]));
                banner.setLayoutParams(layoutParams);
                banner.load();
                mBnAds.put(adUnitId, banner);
            }
        });
    }

    InMobiBanner getBannerAd(String adUnitId) {
        return mBnViewAds.get(adUnitId);
    }

    boolean isBannerAdAvailable(String adUnitId) {
       return !TextUtils.isEmpty(adUnitId) && mBnViewAds.containsKey(adUnitId);
    }

    void destroyBanner(String adUnitId) {
        removeBannerListener(adUnitId);
        if (mBnAds.containsKey(adUnitId)) {
            mBnAds.get(adUnitId).destroy();
        }
        mBnViewAds.remove(adUnitId);
    }

    void loadRewardedVideo(String adUnitId) {
        RvListener listener = new RvListener(adUnitId);
        InMobiInterstitial interstitialAd = new InMobiInterstitial(MediationUtil.getContext(), Long.parseLong(adUnitId),
                listener);
        interstitialAd.load();
    }

    boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        InMobiInterstitial interstitialAd = mRvAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isReady();
    }

    void showRewardedVideo(String adUnitId) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InMobiInterstitial interstitialAd = mRvAds.remove(adUnitId);
                if (interstitialAd != null) {
                    interstitialAd.show();
                }
            }
        });
    }

    void loadInterstitial(String adUnitId) {
        IsListener listener = new IsListener(adUnitId);
        InMobiInterstitial interstitialAd = new InMobiInterstitial(MediationUtil.getContext(), Long.parseLong(adUnitId),
                listener);
        interstitialAd.load();
    }

    boolean isInterstitialAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        InMobiInterstitial interstitialAd = mIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isReady();
    }

    void showInterstitial(String adUnitId) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InMobiInterstitial interstitialAd = mIsAds.remove(adUnitId);
                if (interstitialAd != null) {
                    interstitialAd.show();
                }
            }
        });
    }

    void addBannerListener(String adUnitId, InMobiBannerCallback listener) {
        if (!TextUtils.isEmpty(adUnitId) && listener != null) {
            mBannerCallbacks.put(adUnitId, listener);
        }
    }

    void removeBannerListener(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            mBannerCallbacks.remove(adUnitId);
        }
    }

    private class BnListener extends BannerAdEventListener {
        private String mAdUnitId;

        private BnListener(String adUnitId) {
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoadSucceeded(InMobiBanner inMobiBanner, AdMetaInfo adMetaInfo) {
            super.onAdLoadSucceeded(inMobiBanner, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi Banner onAdLoaded : " + mAdUnitId);
            if (inMobiBanner != null) {
                mBnViewAds.put(mAdUnitId, inMobiBanner);
            }
            bidSuccess(mAdUnitId, adMetaInfo);
        }

        @Override
        public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus inMobiAdRequestStatus) {
            super.onAdLoadFailed(inMobiBanner, inMobiAdRequestStatus);
            AdLog.getSingleton().LogE(TAG, "InMobi Banner LoadFailed : " + inMobiAdRequestStatus.getMessage());
            bidFailed(mAdUnitId, inMobiAdRequestStatus.getMessage());
        }

        @Override
        public void onAdDisplayed(@NonNull InMobiBanner inMobiBanner) {
            super.onAdDisplayed(inMobiBanner);
            AdLog.getSingleton().LogD(TAG, "InMobi Banner onAdDisplayed : " + mAdUnitId);
            InMobiBannerCallback callback = mBannerCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onAdDisplayed(mAdUnitId);
            }
        }

        @Override
        public void onAdClicked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
            super.onAdClicked(inMobiBanner, map);
            AdLog.getSingleton().LogD(TAG, "InMobi onAdClick : " + mAdUnitId);
            InMobiBannerCallback callback = mBannerCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onAdClick(mAdUnitId);
            }
        }
    }

    private class IsListener extends InterstitialAdEventListener {
        private String mAdUnitId;

        private IsListener(String adUnitId) {
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
            super.onAdLoadSucceeded(inMobiInterstitial, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi InterstitialAd onAdLoadSucceeded PlacementId : " + mAdUnitId);
            mIsAds.put(mAdUnitId, inMobiInterstitial);
            bidSuccess(mAdUnitId, adMetaInfo);
        }

        @Override
        public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus status) {
            super.onAdLoadFailed(inMobiInterstitial, status);
            AdLog.getSingleton().LogE(TAG, "InMobi InterstitialAd onAdLoadFailed : " + status.getMessage());
            mIsAds.remove(mAdUnitId);
            bidFailed(mAdUnitId, "InterstitialAd onAdLoadFailed : " + status.getMessage());
        }

        @Override
        public void onAdDisplayed(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
            super.onAdDisplayed(inMobiInterstitial, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi InterstitialAd onAdDisplayed");
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.onAdDisplayed(mAdUnitId);
            }
        }

        @Override
        public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
            super.onAdDisplayFailed(inMobiInterstitial);
            AdLog.getSingleton().LogD(TAG, "InMobi InterstitialAd onAdDisplayFailed");
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.onAdDisplayFailed(mAdUnitId);
            }
        }

        @Override
        public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
            super.onAdClicked(inMobiInterstitial, map);
            AdLog.getSingleton().LogD(TAG, "InMobi InterstitialAd onAdClicked");
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.onAdClicked(mAdUnitId);
            }
        }

        @Override
        public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
            super.onAdDismissed(inMobiInterstitial);
            AdLog.getSingleton().LogD(TAG, "InMobi InterstitialAd onAdDismissed");
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.onAdDismissed(mAdUnitId);
            }
        }
    }

    private class RvListener extends InterstitialAdEventListener {
        private String mAdUnitId;

        private RvListener(String adUnitId) {
            mAdUnitId = adUnitId;
        }

        @Override
        public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
            super.onAdLoadSucceeded(inMobiInterstitial, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi RewardedVideo onAdLoadSucceeded PlacementId : " + mAdUnitId);
            mRvAds.put(mAdUnitId, inMobiInterstitial);
            bidSuccess(mAdUnitId, adMetaInfo);
        }

        @Override
        public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus status) {
            super.onAdLoadFailed(inMobiInterstitial, status);
            AdLog.getSingleton().LogE(TAG, "InMobi RewardedVideo onAdLoadFailed : " + status.getMessage());
            mRvAds.remove(mAdUnitId);
            bidFailed(mAdUnitId, "RewardedVideo onAdLoadFailed : " + status.getMessage());
        }

        @Override
        public void onAdDisplayed(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
            super.onAdDisplayed(inMobiInterstitial, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi RewardedVideo onAdDisplayed");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onAdDisplayed(mAdUnitId);
            }
        }

        @Override
        public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
            super.onAdDisplayFailed(inMobiInterstitial);
            AdLog.getSingleton().LogD(TAG, "InMobi RewardedVideo onAdDisplayFailed");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onAdDisplayFailed(mAdUnitId);
            }
        }

        @Override
        public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
            super.onAdClicked(inMobiInterstitial, map);
            AdLog.getSingleton().LogD(TAG, "InMobi RewardedVideo onAdClicked");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onAdClicked(mAdUnitId);
            }
        }

        @Override
        public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
            super.onAdDismissed(inMobiInterstitial);
            AdLog.getSingleton().LogD(TAG, "InMobi RewardedVideo onAdDismissed");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onAdDismissed(mAdUnitId);
            }
        }

        @Override
        public void onRewardsUnlocked(@NonNull InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
            super.onRewardsUnlocked(inMobiInterstitial, map);
            AdLog.getSingleton().LogD(TAG, "InMobi RewardedVideo onRewardsUnlocked: " + map);
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onAdRewarded(mAdUnitId);
            }
        }
    }

    private void bidSuccess(String adUnitId, AdMetaInfo info) {
        if (mBidCallbacks.containsKey(adUnitId)) {
            mBidCallbacks.get(adUnitId).onBidSuccess(adUnitId, info.getBid());
        }
        mBidError.remove(adUnitId);
    }

    private void bidFailed(String adUnitId, String error) {
        if (mBidCallbacks.containsKey(adUnitId)) {
            mBidCallbacks.get(adUnitId).onBidFailed(adUnitId, error);
        }
        mBidError.put(adUnitId, error);
    }

    private int[] getAdSize(com.openmediation.sdk.banner.AdSize adSize) {
        if (adSize == null) {
            return new int[] {320, 50};
        }
        String bannerDesc = adSize.getDescription();
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return new int[] {728, 90};
            case DESC_RECTANGLE:
                return new int[] {300, 250};
            case DESC_SMART:
                if (MediationUtil.isLargeScreen(MediationUtil.getContext())) {
                    return new int[] {728, 90};
                } else {
                    return new int[] {320, 50};
                }
            default:
                return new int[] {320, 50};
        }
    }
}
