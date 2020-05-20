// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.tiktok.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TikTokAdapter extends CustomAdsAdapter
{
    private static String TAG = "OM-TikTok: ";
    private TTAdNative mTTAdNative;
    private ConcurrentMap<String, TTRewardVideoAd> mTTRvAds;
    private ConcurrentMap<String, TTFullScreenVideoAd> mTTFvAds;

    public TikTokAdapter() {
        mTTRvAds = new ConcurrentHashMap<>();
        mTTFvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return TTAdSdk.getAdManager().getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_13;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSdk(activity);
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    private void loadRvAd(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            TTRewardVideoAd rewardedVideoAd = mTTRvAds.get(adUnitId);
            if (rewardedVideoAd == null) {
                realLoadRvAd(activity, adUnitId, callback);
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(error);
            }
            return;
        }
        TTRewardVideoAd rewardedVideoAd = mTTRvAds.get(adUnitId);
        if (rewardedVideoAd != null) {
            rewardedVideoAd.setRewardAdInteractionListener(new InnerRvAdShowListener(callback));
            rewardedVideoAd.showRewardVideoAd(activity);
            mTTRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("TikTok RewardedVideo is not ready");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mTTRvAds.get(adUnitId) != null;
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSdk(activity);
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        loadInterstitial(activity, adUnitId, callback);
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras,
                                   InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadInterstitial(activity, adUnitId, callback);
    }

    private void loadInterstitial(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            TTFullScreenVideoAd ad = mTTFvAds.get(adUnitId);
            if (ad != null) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                realLoadFullScreenVideoAd(activity, adUnitId, callback);
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(error);
            }
            return;
        }
        TTFullScreenVideoAd ad = mTTFvAds.get(adUnitId);
        if (ad != null) {
            ad.setFullScreenVideoAdInteractionListener(new InnerAdInteractionListener(callback));
            ad.showFullScreenVideoAd(activity);
            mTTFvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("TikTok InterstitialAd is not ready");
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mTTFvAds.get(adUnitId) != null;
    }

    private void initSdk(final Activity activity) {
        TTAdManagerHolder.init(activity.getApplicationContext(), mAppKey);
    }

    private class InnerIsAdListener implements TTAdNative.FullScreenVideoAdListener {

        private InterstitialAdCallback mCallback;
        private String mAdUnitId;

        InnerIsAdListener(InterstitialAdCallback callback, String adUnitId) {
            this.mCallback = callback;
            this.mAdUnitId = adUnitId;
        }

        @Override
        public void onError(int i, String s) {
            AdLog.getSingleton().LogD(TAG + "loadInterstitialAd onError : code " + i + " msg " + s);
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadFailed("TikTok loadInterstitialAd ad load failed : code = " + i + " message = " + s);
            }
        }

        @Override
        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
            AdLog.getSingleton().LogD(TAG + "loadInterstitialAd onFullScreenVideoAdLoad");
            if (ad == null) {
                if (mCallback != null) {
                    mCallback.onInterstitialAdLoadFailed("TikTok loadInterstitialAd ad load failed");
                }
                return;
            }
            mTTFvAds.put(mAdUnitId, ad);
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onFullScreenVideoCached() {
            AdLog.getSingleton().LogD(TAG + "loadInterstitialAd onFullScreenVideoCached");
        }
    }

    private void realLoadFullScreenVideoAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        AdSlot adSlot = buildAdSlotReq(activity, adUnitId);
        InnerIsAdListener listener = new InnerIsAdListener(callback, adUnitId);
        mTTAdNative.loadFullScreenVideoAd(adSlot, listener);
    }

    private static class InnerAdInteractionListener implements TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {

        private InterstitialAdCallback mCallback;

        private InnerAdInteractionListener(InterstitialAdCallback callback) {
            this.mCallback = callback;
        }

        @Override
        public void onAdShow() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onAdShow");
            if (mCallback != null) {
                mCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onAdVideoBarClick() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onAdClicked");
            if (mCallback != null) {
                mCallback.onInterstitialAdClick();
            }
        }

        @Override
        public void onAdClose() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onAdClose");
            if (mCallback != null) {
                mCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onVideoComplete() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onVideoComplete");
        }

        @Override
        public void onSkippedVideo() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onSkippedVideo");
        }
    }

    private void realLoadRvAd(Activity activity, final String adUnitId, final RewardedVideoCallback rvCallback) {
        AdSlot adSlot = buildAdSlotReq(activity, adUnitId);
        mTTAdNative.loadRewardVideoAd(adSlot, new InnerLoadRvAdListener(rvCallback, adUnitId, mTTRvAds));
    }

    private AdSlot buildAdSlotReq(Activity activity, final String adUnitId) {
        if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(activity.getApplicationContext());
        }
        int orientation = TTAdConstant.HORIZONTAL;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = TTAdConstant.VERTICAL;
        }
        return new AdSlot.Builder()
                .setCodeId(adUnitId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setExpressViewAcceptedSize(1080, 1920)
                .setOrientation(orientation)
                .build();
    }

    private static class InnerLoadRvAdListener implements TTAdNative.RewardVideoAdListener {

        private RewardedVideoCallback mCallback;
        private String mCodeId;
        private ConcurrentMap<String, TTRewardVideoAd> mTTRvAds;

        private InnerLoadRvAdListener(RewardedVideoCallback callback, String codeId, ConcurrentMap<String, TTRewardVideoAd> tTRvAds) {
            this.mCallback = callback;
            this.mCodeId = codeId;
            this.mTTRvAds = tTRvAds;
        }

        @Override
        public void onError(int code, String message) {
            AdLog.getSingleton().LogD(TAG + "RewardedVideo  onError: " + code + ", " + message);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed("TikTok RewardedVideo load failed : " + code + ", " + message);
            }
        }

        @Override
        public void onRewardVideoCached() {
            AdLog.getSingleton().LogD(TAG + "RewardedVideo onRewardVideoCached");
        }

        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
            if (ad == null) {
                if (mCallback != null) {
                    mCallback.onRewardedVideoLoadFailed("TikTok RewardedVideo load failed");
                }
                return;
            }
            mTTRvAds.put(mCodeId, ad);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadSuccess();
            }
            AdLog.getSingleton().LogD(TAG + "rewardedVideo  onRewardVideoAdLoad");
        }
    }

    private static class InnerRvAdShowListener implements TTRewardVideoAd.RewardAdInteractionListener {

        private RewardedVideoCallback callback;

        private InnerRvAdShowListener(RewardedVideoCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdShow() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd show");
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onAdVideoBarClick() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd bar click");
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onAdClose() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd close");
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onVideoComplete() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd complete");
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
                callback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onVideoError() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd error");
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("TikTok rewardedVideo play failed");
            }
        }

        @Override
        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
            AdLog.getSingleton().LogD(TAG + "verify:" + rewardVerify + " amount:" + rewardAmount +
                    " name:" + rewardName);
        }

        @Override
        public void onSkippedVideo() {

        }

    }

}
