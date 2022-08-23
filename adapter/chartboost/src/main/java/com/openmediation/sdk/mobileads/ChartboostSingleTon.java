// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ads.Interstitial;
import com.chartboost.sdk.ads.Rewarded;
import com.chartboost.sdk.callbacks.InterstitialCallback;
import com.chartboost.sdk.callbacks.RewardedCallback;
import com.chartboost.sdk.callbacks.StartCallback;
import com.chartboost.sdk.events.CacheError;
import com.chartboost.sdk.events.CacheEvent;
import com.chartboost.sdk.events.ClickError;
import com.chartboost.sdk.events.ClickEvent;
import com.chartboost.sdk.events.DismissEvent;
import com.chartboost.sdk.events.ImpressionEvent;
import com.chartboost.sdk.events.RewardEvent;
import com.chartboost.sdk.events.ShowError;
import com.chartboost.sdk.events.ShowEvent;
import com.chartboost.sdk.events.StartError;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChartboostSingleTon {

    private static final String TAG = "ChartboostSingleTon ";

    private volatile InitState mInitState = InitState.NOT_INIT;
    private final List<InitListener> mListeners = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, Rewarded> mRewardedAds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Interstitial> mInterstitialAds = new ConcurrentHashMap<>();

    private static class Holder {
        private static final ChartboostSingleTon INSTANCE = new ChartboostSingleTon();
    }

    private ChartboostSingleTon() {
    }

    public static ChartboostSingleTon getInstance() {
        return Holder.INSTANCE;
    }

    public boolean isInit() {
        return mInitState == InitState.INIT_SUCCESS;
    }

    public synchronized void init(final Context context, final String appKey, InitListener listener) {
        if (context == null || TextUtils.isEmpty(appKey)) {
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
                initOnMainThread(context, appKey);
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private void initOnMainThread(Context context, final String appKey) {
        String[] tmp = appKey.split("#");
        String appId = tmp[0];
        String signature = tmp[1];
        Chartboost.startWithAppId(context, appId, signature, new StartCallback() {
            @Override
            public void onStartCompleted(StartError startError) {
                if (startError == null) {
                    mInitState = InitState.INIT_SUCCESS;
                    AdLog.getSingleton().LogD("Chartboost SDK initialized successfully");
                    for (InitListener listener : mListeners) {
                        if (listener != null) {
                            listener.initSuccess();
                        }
                    }
                } else {
                    mInitState = InitState.NOT_INIT;
                    AdLog.getSingleton().LogD("Chartboost SDK initialized failed: " + startError.getCode().name());
                    for (InitListener listener : mListeners) {
                        if (listener != null) {
                            listener.initFailed("Chartboost SDK initialized failed: " + startError.getCode().name());
                        }
                    }
                }
                mListeners.clear();
            }
        });
    }

    public void loadRewardedVideo(String adUnitId, RewardedVideoCallback callback) {
        AdLog.getSingleton().LogD(TAG, "loadRewardedVideo: " + adUnitId);
//        InnerRewardedCallback innerRewardedCallback = mRvCallbacks.get(adUnitId);
//        if (innerRewardedCallback == null) {
//            innerRewardedCallback = new InnerRewardedCallback();
//            mRvCallbacks.put(adUnitId, innerRewardedCallback);
//        }
//        Rewarded rewarded = mRewardedAds.get(adUnitId);
//        if (rewarded == null) {
//            rewarded = new Rewarded(adUnitId, innerRewardedCallback, null);
//            mRewardedAds.put(adUnitId, rewarded);
//        }
//        innerRewardedCallback.setRewardedAd(adUnitId, callback);
//        rewarded.cache();


        InnerRewardedCallback rewardedCallback = new InnerRewardedCallback();
        Rewarded rewarded = new Rewarded(adUnitId, rewardedCallback, null);
        rewardedCallback.setRewardedAd(adUnitId, rewarded, callback);
        rewarded.cache();
    }

    boolean isRewardedVideoReady(final String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        Rewarded rewardedAd = mRewardedAds.get(adUnitId);
        return rewardedAd != null && rewardedAd.isCached();
    }

    public void showRewardedVideo(String adUnitId) {
        Rewarded rewarded = mRewardedAds.get(adUnitId);
        if (rewarded != null) {
            rewarded.show();
        }
    }

    public void loadInterstitialAd(String adUnitId, InterstitialAdCallback callback) {
        AdLog.getSingleton().LogD(TAG, "loadInterstitialAd: " + adUnitId);
//        InnerInterstitialCallback interstitialCallback = mIsCallbacks.get(adUnitId);
//        if (interstitialCallback == null) {
//            interstitialCallback = new InnerInterstitialCallback();
//            mIsCallbacks.put(adUnitId, interstitialCallback);
//        }
//        Interstitial interstitial = mInterstitialAds.get(adUnitId);
//        if (interstitial == null) {
//            interstitial = new Interstitial(adUnitId, interstitialCallback, null);
//            mInterstitialAds.put(adUnitId, interstitial);
//        }
//        interstitialCallback.setInterstitialAd(adUnitId, callback);
//        interstitial.cache();

        InnerInterstitialCallback interstitialCallback = new InnerInterstitialCallback();
        Interstitial interstitial = new Interstitial(adUnitId, interstitialCallback, null);
        interstitialCallback.setInterstitialAd(adUnitId, interstitial, callback);
        interstitial.cache();
    }

    public boolean isInterstitialAdReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        Interstitial interstitial = mInterstitialAds.get(adUnitId);
        return interstitial != null && interstitial.isCached();
    }

    public void showInterstitialAd(String adUnitId) {
        Interstitial interstitial = mInterstitialAds.get(adUnitId);
        if (interstitial != null) {
            interstitial.show();
        }
    }

    private class InnerRewardedCallback implements RewardedCallback {
        String mAdUnitId;
        RewardedVideoCallback mAdCallback;
        Rewarded mRewarded;

        public InnerRewardedCallback() {
        }

        public void setRewardedAd(String adUnitId, Rewarded rewarded, RewardedVideoCallback adCallback) {
            mAdUnitId = adUnitId;
            mRewarded = rewarded;
            mAdCallback = adCallback;
        }

        @Override
        public void onAdClicked(ClickEvent clickEvent, ClickError clickError) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd onAdClicked: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onAdLoaded(CacheEvent cacheEvent, CacheError cacheError) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd onAdLoaded: " + mAdUnitId);
            if (mRewarded != null) {
                mRewardedAds.put(mAdUnitId, mRewarded);
            }
            if (mAdCallback != null) {
                mAdCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onAdRequestedToShow(ShowEvent showEvent) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd onAdRequestedToShow: " + mAdUnitId);
        }

        @Override
        public void onAdShown(ShowEvent showEvent, ShowError showError) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd onAdShown: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onRewardedVideoAdShowSuccess();
                mAdCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onImpressionRecorded(ImpressionEvent impressionEvent) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd onImpressionRecorded: " + mAdUnitId);
        }

        @Override
        public void onAdDismiss(DismissEvent dismissEvent) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd onAdDismiss: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onRewardedVideoAdEnded();
                mAdCallback.onRewardedVideoAdClosed();
            }
            mRewardedAds.remove(mAdUnitId);
        }

        @Override
        public void onRewardEarned(RewardEvent rewardEvent) {
            AdLog.getSingleton().LogD(TAG, "RewardedAd onRewardEarned: " + mAdUnitId + ", rewardEvent: " + rewardEvent.getReward());
            if (mAdCallback != null) {
                mAdCallback.onRewardedVideoAdRewarded();
            }
        }
    }

    private class InnerInterstitialCallback implements InterstitialCallback {
        String mAdUnitId;
        InterstitialAdCallback mAdCallback;
        Interstitial mInterstitial;

        public InnerInterstitialCallback() {

        }

        public void setInterstitialAd(String adUnitId, Interstitial interstitial, InterstitialAdCallback adCallback) {
            mAdUnitId = adUnitId;
            mInterstitial = interstitial;
            mAdCallback = adCallback;
        }

        @Override
        public void onAdClicked(@NotNull ClickEvent clickEvent, @Nullable ClickError clickError) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdClicked: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdLoaded(@NotNull CacheEvent cacheEvent, @Nullable CacheError cacheError) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdLoaded: " + mAdUnitId);
            if (mInterstitial != null) {
                mInterstitialAds.put(mAdUnitId, mInterstitial);
            }
            if (mAdCallback != null) {
                mAdCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onAdRequestedToShow(@NotNull ShowEvent showEvent) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdRequestedToShow: " + mAdUnitId);
        }

        @Override
        public void onAdShown(@NotNull ShowEvent showEvent, @Nullable ShowError showError) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdShown: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onImpressionRecorded(@NotNull ImpressionEvent impressionEvent) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onImpressionRecorded: " + mAdUnitId);
        }

        @Override
        public void onAdDismiss(@NotNull DismissEvent dismissEvent) {
            AdLog.getSingleton().LogD(TAG, "InterstitialAd onAdDismiss: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onInterstitialAdClosed();
            }
            mInterstitialAds.remove(mAdUnitId);
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
