// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.os.SystemClock;
import android.text.TextUtils;

import com.nbmediation.sdk.mobileads.tencentad.BuildConfig;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.utils.AdLog;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.managers.status.SDKStatus;
import com.qq.e.comm.util.AdError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TencentAdAdapter extends CustomAdsAdapter
{
    private static String TAG = "OM-TencentAd: ";
    private ConcurrentMap<String, RewardVideoAD> mRvAds;
    private ConcurrentMap<String, UnifiedInterstitialAD> mIsAds;

    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    public TencentAdAdapter() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return SDKStatus.getIntegrationSDKVersion();
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
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
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
            if (isRewardedVideoAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                realLoadRvAd(activity, adUnitId, callback);
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
                callback.onRewardedVideoAdShowFailed("TencentAds ad not ready");
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
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
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
            if (isInterstitialAdAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                realLoadInterstitial(activity, adUnitId, callback);
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
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            UnifiedInterstitialAD ad = mIsAds.get(adUnitId);
            if (ad != null) {
                ad.show(activity);
            }
            mIsAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("TencentAds InterstitialAd not ready");
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

    private void realLoadInterstitial(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        if (callback != null) {
            mIsCallbacks.put(adUnitId, callback);
        }

        InnerIsAdListener listener = new InnerIsAdListener(adUnitId);
        UnifiedInterstitialAD ad = new UnifiedInterstitialAD(activity, mAppKey, adUnitId, listener);
        listener.setAdView(ad);
        ad.loadAD();
    }

    private void realLoadRvAd(Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        if (callback != null) {
            mRvCallbacks.put(adUnitId, callback);
        }
        InnerRvAdListener listener = new InnerRvAdListener(adUnitId);
        RewardVideoAD rewardVideoAD = new RewardVideoAD(activity, mAppKey, adUnitId, listener);
        listener.setAdView(rewardVideoAD);
        rewardVideoAD.loadAD();
    }

    private class InnerIsAdListener implements UnifiedInterstitialADListener {

        private String mAdUnitId;
        private UnifiedInterstitialAD mAd;

        void setAdView(UnifiedInterstitialAD ad) {
            this.mAd = ad;
        }

        InnerIsAdListener(String adUnitId) {
            this.mAdUnitId = adUnitId;
        }

        @Override
        public void onADReceive() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd onADReceive : " + mAdUnitId);
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
            AdLog.getSingleton().LogE(TAG + "RewardedVideo  onError: " + adError.getErrorCode() + ", " + adError.getErrorMsg());
            InterstitialAdCallback callback = mIsCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(TAG + "RewardedVideo load failed : " + adError.getErrorCode() + ", " + adError.getErrorMsg());
            }
        }

        @Override
        public void onADOpened() {
            AdLog.getSingleton().LogD(TAG + "InterstitialAd show onDisplay : " + mAdUnitId);
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
                callback.onInterstitialAdClick();
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
    }

    private class InnerRvAdListener implements RewardVideoADListener {

        private String mAdUnitId;
        private RewardVideoAD mRewardVideoAD;

        private InnerRvAdListener(String adUnitId) {
            this.mAdUnitId = adUnitId;
        }

        void setAdView(RewardVideoAD rewardVideoAD) {
            this.mRewardVideoAD = rewardVideoAD;
        }

        @Override
        public void onADLoad() {
            AdLog.getSingleton().LogD(TAG + "RewardedVideo onRewardVideoAdLoad : " + mAdUnitId);
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
            AdLog.getSingleton().LogD(TAG + "RewardVideoAd show onDisplay : " + mAdUnitId);
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onADExpose() {
            AdLog.getSingleton().LogD(TAG + "RewardVideoAd show onADExpose : " + mAdUnitId);
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
            }
        }

        @Override
        public void onReward() {
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
            AdLog.getSingleton().LogD(TAG + "RewardVideoAd close : " + mAdUnitId);
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onError(AdError adError) {
            AdLog.getSingleton().LogE(TAG + "RewardedVideo  onError: " + adError.getErrorCode() + ", " + adError.getErrorMsg());
            RewardedVideoCallback callback = mRvCallbacks.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed : " + adError.getErrorCode() + ", " + adError.getErrorMsg());
            }
        }
    }

}
