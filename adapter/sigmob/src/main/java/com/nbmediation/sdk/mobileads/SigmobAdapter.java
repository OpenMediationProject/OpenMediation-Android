// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.sigmob.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardInfo;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAdListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SigmobAdapter extends CustomAdsAdapter {

    private static String TAG = "OM-Sigmob: ";

    private Map<String, WindRewardAdRequest> mSigAds;

    public SigmobAdapter() {
        mSigAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_20;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        String appId = null;
        String appKey = null;
        try {
            appId = (String) dataMap.get("AppKey");
            appKey = (String) dataMap.get("pid");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(error) && !TextUtils.isEmpty(appId) && !TextUtils.isEmpty(appKey)) {
            initSdk(activity, appId, appKey);
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
            WindRewardAdRequest request = mSigAds.get(adUnitId);
            if (request == null) {
                realLoadRvAd(adUnitId, callback);
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
        WindRewardAdRequest request = mSigAds.get(adUnitId);
        if (request != null) {
            WindRewardedVideoAd windRewardedVideoAd = WindRewardedVideoAd.sharedInstance();
            windRewardedVideoAd.show(activity, request);
            mSigAds.remove(adUnitId);
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
        WindRewardedVideoAd windRewardedVideoAd = WindRewardedVideoAd.sharedInstance();
        return mSigAds.get(adUnitId) != null && windRewardedVideoAd.isReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
//        String error = check(activity);
//        if (TextUtils.isEmpty(error)) {
//            initSdk(activity);
//            if (callback != null) {
//                callback.onInterstitialAdInitSuccess();
//            }
//        } else {
//            if (callback != null) {
//                callback.onInterstitialAdInitFailed(error);
//            }
//        }
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
//        String error = check(activity, adUnitId);
//        if (TextUtils.isEmpty(error)) {
//            TTFullScreenVideoAd ad = mTTFvAds.get(adUnitId);
//            if (ad != null) {
//                if (callback != null) {
//                    callback.onInterstitialAdLoadSuccess();
//                }
//            } else {
//                realLoadFullScreenVideoAd(activity, adUnitId, callback);
//            }
//        } else {
//            if (callback != null) {
//                callback.onInterstitialAdLoadFailed(error);
//            }
//        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
//        String error = check(activity, adUnitId);
//        if (!TextUtils.isEmpty(error)) {
//            if (callback != null) {
//                callback.onInterstitialAdShowFailed(error);
//            }
//            return;
//        }
//        TTFullScreenVideoAd ad = mTTFvAds.get(adUnitId);
//        if (ad != null) {
//            ad.setFullScreenVideoAdInteractionListener(new InnerAdInteractionListener(callback));
//            ad.showFullScreenVideoAd(activity);
//            mTTFvAds.remove(adUnitId);
//        } else {
//            if (callback != null) {
//                callback.onInterstitialAdShowFailed("TikTok InterstitialAd is not ready");
//            }
//        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
//        if (TextUtils.isEmpty(adUnitId)) {
//            return false;
//        }
//        return mTTFvAds.get(adUnitId) != null;
        return false;

    }

    private void initSdk(final Activity activity, String appId, String appKey) {
        WindAds ads = WindAds.sharedAds();

        ads.startWithOptions(activity, new WindAdOptions(appId, appKey));
    }

    private void realLoadRvAd(final String adUnitId, final RewardedVideoCallback rvCallback) {
        WindRewardedVideoAd windRewardedVideoAd = WindRewardedVideoAd.sharedInstance();

        WindRewardAdRequest request = new WindRewardAdRequest(adUnitId, null, null);
        windRewardedVideoAd.setWindRewardedVideoAdListener(new InnerRvVideoAdListener(rvCallback, mSigAds, request));
        windRewardedVideoAd.loadAd(request);
    }

    private static class InnerRvVideoAdListener implements WindRewardedVideoAdListener {

        private RewardedVideoCallback mCallback;

        private Map<String, WindRewardAdRequest> mSigAds;

        private WindRewardAdRequest mRequest;

        InnerRvVideoAdListener(RewardedVideoCallback callback, Map<String, WindRewardAdRequest> sigAds, WindRewardAdRequest request) {
            mCallback = callback;
            mSigAds = sigAds;
            mRequest = request;
        }

        //仅sigmob渠道有回调，聚合其他平台无次回调
        @Override
        public void onVideoAdPreLoadSuccess(String placementId) {

        }

        //仅sigmob渠道有回调，聚合其他平台无次回调
        @Override
        public void onVideoAdPreLoadFail(String placementId) {
        }

        @Override
        public void onVideoAdLoadSuccess(String placementId) {
            if (placementId == null) {
                if (mCallback != null) {
                    mCallback.onRewardedVideoLoadFailed(TAG + " RewardedVideo load failed");
                }
                return;
            }
            mSigAds.put(placementId, mRequest);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadSuccess();
            }
            AdLog.getSingleton().LogD(TAG + "rewardedVideo  onVideoAdLoadSuccess");
        }

        @Override
        public void onVideoAdPlayStart(String placementId) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoAdPlayStart");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowSuccess();
                mCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onVideoAdPlayEnd(String s) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoAdPlayEnd s=" + s);
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowSuccess();
                mCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onVideoAdClicked(String placementId) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoAdClicked");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClicked();
            }
        }

        //WindRewardInfo中isComplete方法返回是否完整播放
        @Override
        public void onVideoAdClosed(WindRewardInfo windRewardInfo, String placementId) {
            if (windRewardInfo.isComplete()) {
                AdLog.getSingleton().LogD(TAG + "激励视频广告完整播放，给予奖励");
                if (mCallback != null) {
                    mCallback.onRewardedVideoAdRewarded();
                    mCallback.onRewardedVideoAdEnded();
                }
            } else {
                AdLog.getSingleton().LogD(TAG + "激励视频广告关闭");
                if (mCallback != null) {
                    mCallback.onRewardedVideoAdClosed();
                }
            }


        }

        /**
         * 加载广告错误回调
         * WindAdError 激励视频错误内容
         * placementId 广告位
         */
        @Override
        public void onVideoAdLoadError(WindAdError windAdError, String placementId) {
            AdLog.getSingleton().LogD(TAG + "RewardedVideo  onVideoAdLoadError: " + windAdError.getErrorCode() + ", " + windAdError.getMessage());
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed("TikTok RewardedVideo load failed : " + windAdError.getErrorCode() + ", " + windAdError.getMessage());
            }
        }


        /**
         * 播放错误回调
         * WindAdError 激励视频错误内容
         * placementId 广告位
         */
        @Override
        public void onVideoAdPlayError(WindAdError windAdError, String placementId) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoAdPlayError");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowFailed(TAG + "rewardedVideo playing failed");
            }
        }

    }
//
//    private static class InnerLoadRvAdListener implements TTAdNative.RewardVideoAdListener {
//
//        private RewardedVideoCallback mCallback;
//        private String mCodeId;
//        private ConcurrentMap<String, TTRewardVideoAd> mTTRvAds;
//
//        private InnerLoadRvAdListener(RewardedVideoCallback callback, String codeId, ConcurrentMap<String, TTRewardVideoAd> tTRvAds) {
//            this.mCallback = callback;
//            this.mCodeId = codeId;
//            this.mTTRvAds = tTRvAds;
//        }
//
//        @Override
//        public void onError(int code, String message) {
//            AdLog.getSingleton().LogE("OM-TikTok: RewardedVideo  onError: " + code + ", " + message);
//            if (mCallback != null) {
//                mCallback.onRewardedVideoLoadFailed("TikTok RewardedVideo load failed : " + code + ", " + message);
//            }
//        }
//
//        @Override
//        public void onRewardVideoCached() {
//            AdLog.getSingleton().LogD("OM-TikTok: RewardedVideo onRewardVideoCached");
//        }
//
//        @Override
//        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
//            if (ad == null) {
//                if (mCallback != null) {
//                    mCallback.onRewardedVideoLoadFailed("TikTok RewardedVideo load failed");
//                }
//                return;
//            }
//            mTTRvAds.put(mCodeId, ad);
//            if (mCallback != null) {
//                mCallback.onRewardedVideoLoadSuccess();
//            }
//            AdLog.getSingleton().LogD("OM-TikTok: rewardedVideo  onRewardVideoAdLoad");
//        }
//    }
//
//    private static class InnerRvAdShowListener implements TTRewardVideoAd.RewardAdInteractionListener {
//
//        private RewardedVideoCallback callback;
//
//        private InnerRvAdShowListener(RewardedVideoCallback callback) {
//            this.callback = callback;
//        }
//
//        @Override
//        public void onAdShow() {
//            AdLog.getSingleton().LogD("OM-TikTok: rewardVideoAd show");
//            if (callback != null) {
//                callback.onRewardedVideoAdShowSuccess();
//                callback.onRewardedVideoAdStarted();
//            }
//        }
//
//        @Override
//        public void onAdVideoBarClick() {
//            AdLog.getSingleton().LogD("OM-TikTok: rewardVideoAd bar click");
//            if (callback != null) {
//                callback.onRewardedVideoAdClicked();
//            }
//        }
//
//        @Override
//        public void onAdClose() {
//            AdLog.getSingleton().LogD("OM-TikTok: rewardVideoAd close");
//            if (callback != null) {
//                callback.onRewardedVideoAdClosed();
//            }
//        }
//
//        @Override
//        public void onVideoComplete() {
//            AdLog.getSingleton().LogD("OM-TikTok: rewardVideoAd complete");
//            if (callback != null) {
//                callback.onRewardedVideoAdRewarded();
//                callback.onRewardedVideoAdEnded();
//            }
//        }
//
//        @Override
//        public void onVideoError() {
//            AdLog.getSingleton().LogE("OM-TikTok: rewardVideoAd error");
//            if (callback != null) {
//                callback.onRewardedVideoAdShowFailed("TikTok rewardedVideo play failed");
//            }
//        }
//
//        @Override
//        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
//            AdLog.getSingleton().LogD("OM-TikTok:  verify:" + rewardVerify + " amount:" + rewardAmount +
//                    " name:" + rewardName);
//        }
//
//    }

}
