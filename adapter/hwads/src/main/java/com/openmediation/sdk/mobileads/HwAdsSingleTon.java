// Copyright 2022 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.reward.Reward;
import com.huawei.hms.ads.reward.RewardAd;
import com.huawei.hms.ads.reward.RewardAdLoadListener;
import com.huawei.hms.ads.reward.RewardAdStatusListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.concurrent.ConcurrentHashMap;

public class HwAdsSingleTon {
    private final ConcurrentHashMap<String, RewardAd> mRvAds;
    private final ConcurrentHashMap<String, InterstitialAd> mIsAds;

    private static class Holder {
        private static final HwAdsSingleTon INSTANCE = new HwAdsSingleTon();
    }

    private HwAdsSingleTon() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
    }

    public static HwAdsSingleTon getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void initSDK(final Context context, final InitCallback listener) {
        if (context == null) {
            AdLog.getSingleton().LogE("Init Failed: Context is null");
            if (listener != null) {
                listener.onFailed("Init Failed: Context is null");
            }
            return;
        }
        HwAds.init(context.getApplicationContext());
        if (listener != null) {
            listener.onSuccess();
        }
    }

    public interface InitCallback {
        void onSuccess();

        void onFailed(String msg);
    }

    public void loadRewardedVideo(Context context, final String adUnitId, final RewardedVideoCallback callback) {
        try {
            final RewardAd rewardAd = new RewardAd(context, adUnitId);
            RewardAdLoadListener listener = new RewardAdLoadListener() {
                @Override
                public void onRewardAdFailedToLoad(int code) {
                    AdLog.getSingleton().LogE("HwAdsAdapter onRewardAdFailedToLoad: " + code + ", adUnit: " + adUnitId);
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", code, ""));
                    }
                }

                @Override
                public void onRewardedLoaded() {
                    AdLog.getSingleton().LogD("HwAdsAdapter onRewardedLoaded: adUnit: " + adUnitId);
                    mRvAds.put(adUnitId, rewardAd);
                    if (callback != null) {
                        callback.onRewardedVideoLoadSuccess();
                    }
                }
            };
            rewardAd.loadAd(new AdParam.Builder().build(), listener);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        RewardAd rewardAd = mRvAds.get(adUnitId);
        return rewardAd != null && rewardAd.isLoaded();
    }

    public void showRewardedVideo(Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        try {
            if (!isRewardedVideoAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", "Not Ready"));
                }
                return;
            }
            RewardAd rewardAd = mRvAds.remove(adUnitId);
            rewardAd.show(activity, new RewardAdStatusListener() {
                @Override
                public void onRewardAdClosed() {
                    super.onRewardAdClosed();
                    AdLog.getSingleton().LogD("HwAdsAdapter onRewardAdClosed: adUnit: " + adUnitId);
                    if (callback != null) {
                        callback.onRewardedVideoAdEnded();
                        callback.onRewardedVideoAdClosed();
                    }
                }

                @Override
                public void onRewardAdFailedToShow(int code) {
                    super.onRewardAdFailedToShow(code);
                    AdLog.getSingleton().LogD("HwAdsAdapter onRewardAdFailedToShow, code: " + code + ", adUnitId: " + adUnitId);
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", code, ""));
                    }
                }

                @Override
                public void onRewardAdOpened() {
                    super.onRewardAdOpened();
                    AdLog.getSingleton().LogD("HwAdsAdapter onRewardAdOpened, adUnitId: " + adUnitId);
                    if (callback != null) {
                        callback.onRewardedVideoAdShowSuccess();
                        callback.onRewardedVideoAdStarted();
                    }
                }

                @Override
                public void onRewarded(Reward reward) {
                    super.onRewarded(reward);
                    AdLog.getSingleton().LogD("HwAdsAdapter onRewarded, adUnitId: " + adUnitId);
                    if (callback != null) {
                        callback.onRewardedVideoAdRewarded();
                    }
                }
            });
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HwAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    public void loadInterstitialAd(Context context, final String adUnitId, final InterstitialAdCallback callback) {
        final InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdId(adUnitId);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                AdLog.getSingleton().LogD("HwAdsAdapter InterstitialAd onAdLoaded, adUnit: " + adUnitId);
                mIsAds.put(adUnitId, interstitialAd);
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            }

            @Override
            public void onAdFailed(int errorCode) {
                AdLog.getSingleton().LogE("HwAdsAdapter InterstitialAd onAdFailed, code: " + errorCode + ", adUnitId: " + adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HwAdsAdapter", errorCode, ""));
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdLog.getSingleton().LogD("HwAdsAdapter InterstitialAd onAdClosed, adUnitId: " + adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AdLog.getSingleton().LogD("HwAdsAdapter InterstitialAd onAdClicked, adUnitId: " + adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdClicked();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                AdLog.getSingleton().LogD("HwAdsAdapter InterstitialAd onAdOpened, adUnitId: " + adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }
        });
        AdParam adParam = new AdParam.Builder().build();
        interstitialAd.loadAd(adParam);
    }

    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        InterstitialAd interstitialAd = mIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isLoaded();
    }

    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        try {
            if (!isInterstitialAdAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HwAdsAdapter", "Not Ready"));
                }
                return;
            }
            InterstitialAd interstitialAd = mIsAds.remove(adUnitId);
            interstitialAd.show(activity);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HwAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }
}
