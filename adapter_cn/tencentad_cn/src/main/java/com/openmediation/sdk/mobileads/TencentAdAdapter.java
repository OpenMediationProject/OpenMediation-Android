// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.mobileads.tencentad.BuildConfig;
import com.openmediation.sdk.utils.AdLog;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.managers.status.SDKStatus;
import com.qq.e.comm.util.AdError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TencentAdAdapter extends CustomAdsAdapter {
    private static final String TAG = "OM-TencentAd: ";
    private final ConcurrentMap<String, RewardVideoAD> mRvAds;
    private final ConcurrentMap<String, UnifiedInterstitialAD> mIsAds;

    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    public TencentAdAdapter() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return SDKStatus.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_6;
    }

    @Override
    public boolean isAdNetworkInit() {
        return TencentAdManagerHolder.isInit();
    }

    private synchronized boolean initSDK(String appKey) {
        if (TencentAdManagerHolder.isInit()) {
            return true;
        }
        return TencentAdManagerHolder.init(MediationUtil.getContext().getApplicationContext(), appKey);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (initSDK(mAppKey)) {
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Init Failed"));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(adUnitId, callback);
    }

    private void loadRvAd(String adUnitId, RewardedVideoCallback callback) {
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            realLoadRvAd(MediationUtil.getContext(), adUnitId, callback);
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                mRvCallbacks.put(adUnitId, callback);
            }
            RewardVideoAD rewardedVideoAd = mRvAds.get(adUnitId);
            if (rewardedVideoAd != null) {
                rewardedVideoAd.showAD(activity);
            }
            mRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Tencent RewardedVideoAd not ready"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        RewardVideoAD videoAD = mRvAds.get(adUnitId);
        return videoAD != null && !videoAD.hasShown() && SystemClock.elapsedRealtime() < (videoAD.getExpireTimestamp() - 1000);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (initSDK(mAppKey)) {
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Init Failed"));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras,
                                   InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadInterstitial(activity, adUnitId, callback);
    }

    private void loadInterstitial(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            realLoadInterstitial(activity, adUnitId, callback);
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            UnifiedInterstitialAD ad = mIsAds.get(adUnitId);
            if (ad != null) {
                ad.showFullScreenAD(activity);
            }
            mIsAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "TencentAds InterstitialAd not ready"));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mIsAds.get(adUnitId) != null;
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        TencentBannerManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        TencentBannerManager.getInstance().loadAd(activity, adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return TencentBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        TencentBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
            return;
        }
        TencentNativeManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
            return;
        }
        TencentNativeManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        super.destroyNativeAd(adUnitId, adInfo);
        TencentNativeManager.getInstance().destroyAd(adUnitId, adInfo);
    }

    @Override
    public void initSplashAd(Activity activity, Map<String, Object> extras, SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        TencentSplashManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        super.loadSplashAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        TencentSplashManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return TencentSplashManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        TencentSplashManager.getInstance().showAd(adUnitId, viewGroup, callback);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        TencentSplashManager.getInstance().destroyAd(adUnitId);
    }

    private void realLoadInterstitial(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        if (callback != null) {
            mIsCallbacks.put(adUnitId, callback);
        }

        InnerIsAdListener listener = new InnerIsAdListener(adUnitId);
        UnifiedInterstitialAD ad = new UnifiedInterstitialAD(activity, adUnitId, listener);
        listener.setAdView(ad);
        ad.loadFullScreenAD();
    }

    private void realLoadRvAd(Context context, final String adUnitId, final RewardedVideoCallback callback) {
        if (callback != null) {
            mRvCallbacks.put(adUnitId, callback);
        }
        InnerRvAdListener listener = new InnerRvAdListener(adUnitId);
        RewardVideoAD rewardVideoAD = new RewardVideoAD(context, adUnitId, listener);
        listener.setAdView(rewardVideoAD);
        rewardVideoAD.loadAD();
    }

    private class InnerIsAdListener implements UnifiedInterstitialADListener {

        private final String mAdUnitId;
        private UnifiedInterstitialAD mAd;

        void setAdView(UnifiedInterstitialAD ad) {
            this.mAd = ad;
        }

        InnerIsAdListener(String adUnitId) {
            this.mAdUnitId = adUnitId;
        }

        @Override
        public void onADReceive() {
            if (mAd != null) {
                mIsAds.put(mAdUnitId, mAd);
            }
            InterstitialAdCallback callback = mIsCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onVideoCached() {

        }

        @Override
        public void onNoAD(AdError adError) {
            InterstitialAdCallback callback = mIsCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, adError.getErrorCode(), adError.getErrorMsg()));
            }
        }

        @Override
        public void onADOpened() {
            InterstitialAdCallback callback = mIsCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onADExposure() {

        }

        @Override
        public void onADClicked() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd click : " + mAdUnitId);
            InterstitialAdCallback callback = mIsCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdClicked();
            }
        }

        @Override
        public void onADLeftApplication() {

        }

        @Override
        public void onADClosed() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd close : " + mAdUnitId);
            InterstitialAdCallback callback = mIsCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onRenderSuccess() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onRenderSuccess : " + mAdUnitId);
        }

        @Override
        public void onRenderFail() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onRenderFail : " + mAdUnitId);
        }
    }

    private class InnerRvAdListener implements RewardVideoADListener {

        private final String mAdUnitId;
        private RewardVideoAD mRewardVideoAD;

        private InnerRvAdListener(String adUnitId) {
            this.mAdUnitId = adUnitId;
        }

        void setAdView(RewardVideoAD rewardVideoAD) {
            this.mRewardVideoAD = rewardVideoAD;
        }

        @Override
        public void onADLoad() {
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (mRewardVideoAD != null) {
                mRvAds.put(mAdUnitId, mRewardVideoAD);
            }
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onVideoCached() {

        }

        @Override
        public void onADShow() {
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onADExpose() {
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
            }
        }

        @Override
        public void onReward(Map<String, Object> map) {
            AdLog.getSingleton().LogD(TAG + "RewardVideoAd onReward : " + mAdUnitId);
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onADClick() {
            AdLog.getSingleton().LogD(TAG + "RewardVideoAd click : " + mAdUnitId);
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onVideoComplete() {
            AdLog.getSingleton().LogD(TAG + "RewardVideoAd complete : " + mAdUnitId);
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onADClose() {
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onError(AdError adError) {
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, adError.getErrorCode(), adError.getErrorMsg()));
            }
        }
    }

}
