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
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.BidCallback;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMobiSingleTon {
    private static final String TAG = "InMobiSingleTon";

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
        } catch (Throwable e) {
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

    public void getBidResponse(Map<String, Object> dataMap, BidCallback callback) {
        if (isInit()) {
            executeBid(dataMap, callback);
            return;
        }
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        init(MediationUtil.getContext(), appKey, new InMobiInitCallback() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onBidFailed("InMobi SDK init error: " + error);
                }
            }
        });
    }

    /**
     * execute c2s bid
     *
     * @param dataMap PlacementId
     * @param dataMap BidCallback
     */
    void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        try {
            int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
            String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
            if (adType == BidConstance.INTERSTITIAL) {
                loadInterstitial(adUnitId, null, callback);
            } else if (adType == BidConstance.VIDEO) {
                loadRewardedVideo(adUnitId, null, callback);
            } else if (adType == BidConstance.BANNER) {
                AdSize adSize = (AdSize) dataMap.get(BidConstance.BID_BANNER_SIZE);
                int[] size;
                if (adSize == null) {
                    size = new int[]{320, 50};
                } else {
                    String bannerDesc = adSize.getDescription();
                    size = getAdSize(bannerDesc);
                }
                loadBanner(adUnitId, size, null, callback);
            } else {
                if (callback != null) {
                    callback.onBidFailed("unSupport bid type");
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBidFailed("Bid Failed: " + e.getMessage());
            }
        }
    }

    void setInterstitialAdCallback(InMobiInterstitialCallback callback) {
        mInterstitialAdCallback = callback;
    }

    void setVideoAdCallback(InMobiVideoCallback callback) {
        mVideoAdCallback = callback;
    }

    void loadBanner(String adUnitId, int[] size, BannerAdCallback adCallback, BidCallback bidCallback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InMobiBanner banner = new InMobiBanner(MediationUtil.getContext(), Long.parseLong(adUnitId));
                BnListener listener = new BnListener(adUnitId, adCallback, bidCallback);
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

    InMobiBanner removeBannerAd(String adUnitId) {
        return mBnViewAds.remove(adUnitId);
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

    void loadRewardedVideo(String adUnitId, RewardedVideoCallback adCallback, BidCallback bidCallback) {
        RvListener listener = new RvListener(adUnitId, adCallback, bidCallback);
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

    void showRewardedVideo(String adUnitId, RewardedVideoCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    InMobiInterstitial interstitialAd = mRvAds.remove(adUnitId);
                    if (interstitialAd != null) {
                        interstitialAd.show();
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "InMobiAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    void loadInterstitial(String adUnitId, InterstitialAdCallback adCallback, BidCallback bidCallback) {
        IsListener listener = new IsListener(adUnitId, adCallback, bidCallback);
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

    void showInterstitial(String adUnitId, InterstitialAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    InMobiInterstitial interstitialAd = mIsAds.remove(adUnitId);
                    if (interstitialAd != null) {
                        interstitialAd.show();
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "InMobiAdapter", "Unknown Error, " + e.getMessage()));
                    }
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
        private final String mAdUnitId;
        private final BannerAdCallback mAdCallback;
        private final BidCallback mBidCallback;

        private BnListener(String adUnitId, BannerAdCallback adCallback, BidCallback bidCallback) {
            mAdUnitId = adUnitId;
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        @Override
        public void onAdLoadSucceeded(InMobiBanner inMobiBanner, AdMetaInfo adMetaInfo) {
            super.onAdLoadSucceeded(inMobiBanner, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi Banner onAdLoaded : " + mAdUnitId);
            if (mAdCallback != null) {
                onBidSuccess(adMetaInfo, mAdCallback);
                mAdCallback.onBannerAdLoadSuccess(inMobiBanner);
            }
            if (mBidCallback != null) {
                mBnViewAds.put(mAdUnitId, inMobiBanner);
                onBidSuccess(adMetaInfo, mBidCallback);
            }
        }

        @Override
        public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus status) {
            super.onAdLoadFailed(inMobiBanner, status);
            String error = "InMobi Banner LoadFailed : " + status.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            if (mAdCallback != null) {
                onBidFailed(error, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "InMobiAdapter", error);
                mAdCallback.onBannerAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed(error, mBidCallback);
            }
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

//        @Override
//        public void onAdImpression(@NonNull InMobiBanner inMobiBanner) {
//            super.onAdImpression(inMobiBanner);
//            AdLog.getSingleton().LogD(TAG, "InMobi Banner onAdImpression : " + mAdUnitId);
//            InMobiBannerCallback callback = mBannerCallbacks.get(mAdUnitId);
//            if (callback != null) {
//                callback.onAdImpression(mAdUnitId);
//            }
//        }

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
        private final String mAdUnitId;
        private final InterstitialAdCallback mAdCallback;
        private final BidCallback mBidCallback;

        private IsListener(String adUnitId, InterstitialAdCallback adCallback, BidCallback bidCallback) {
            mAdUnitId = adUnitId;
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        @Override
        public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
            super.onAdLoadSucceeded(inMobiInterstitial, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi InterstitialAd onAdLoadSucceeded PlacementId : " + mAdUnitId);
            mIsAds.put(mAdUnitId, inMobiInterstitial);
            if (mAdCallback != null) {
                onBidSuccess(adMetaInfo, mAdCallback);
                mAdCallback.onInterstitialAdLoadSuccess();
            }
            if (mBidCallback != null) {
                onBidSuccess(adMetaInfo, mBidCallback);
            }
        }

        @Override
        public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus status) {
            super.onAdLoadFailed(inMobiInterstitial, status);
            AdLog.getSingleton().LogE(TAG, "InMobi InterstitialAd onAdLoadFailed : " + status.getMessage());
            mIsAds.remove(mAdUnitId);
            String error = "InterstitialAd onAdLoadFailed : " + status.getMessage();
            if (mAdCallback != null) {
                onBidFailed(error, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "InMobiAdapter", status.getMessage());
                mAdCallback.onInterstitialAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed(error, mBidCallback);
            }
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
        private final String mAdUnitId;
        private final RewardedVideoCallback mAdCallback;
        private final BidCallback mBidCallback;

        private RvListener(String adUnitId, RewardedVideoCallback adCallback, BidCallback bidCallback) {
            mAdUnitId = adUnitId;
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        @Override
        public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
            super.onAdLoadSucceeded(inMobiInterstitial, adMetaInfo);
            AdLog.getSingleton().LogD(TAG, "InMobi RewardedVideo onAdLoadSucceeded PlacementId : " + mAdUnitId);
            mRvAds.put(mAdUnitId, inMobiInterstitial);
            if (mAdCallback != null) {
                onBidSuccess(adMetaInfo, mAdCallback);
                mAdCallback.onRewardedVideoLoadSuccess();
            }
            if (mBidCallback != null) {
                onBidSuccess(adMetaInfo, mBidCallback);
            }
        }

        @Override
        public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus status) {
            super.onAdLoadFailed(inMobiInterstitial, status);
            AdLog.getSingleton().LogE(TAG, "InMobi RewardedVideo onAdLoadFailed : " + status.getMessage());
            mRvAds.remove(mAdUnitId);
            String error = "RewardedVideo onAdLoadFailed : " + status.getMessage();
            if (mAdCallback != null) {
                onBidFailed(error, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "InMobiAdapter", status.getMessage());
                mAdCallback.onRewardedVideoLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed(error, mBidCallback);
            }
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

    private void onBidSuccess(AdMetaInfo info, BidCallback callback) {
        BidResponse bidResponse = new BidResponse();
        bidResponse.setPrice(info.getBid());
        callback.onBidSuccess(bidResponse);
    }

    private void onBidFailed(String error, BidCallback callback) {
        callback.onBidFailed(error);
    }

    int[] getAdSize(String desc) {
        switch (desc) {
            case MediationUtil.DESC_LEADERBOARD:
                return new int[]{728, 90};
            case MediationUtil.DESC_RECTANGLE:
                return new int[]{300, 250};
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(MediationUtil.getContext())) {
                    return new int[]{728, 90};
                } else {
                    return new int[]{320, 50};
                }
            default:
                return new int[]{320, 50};
        }
    }
}
