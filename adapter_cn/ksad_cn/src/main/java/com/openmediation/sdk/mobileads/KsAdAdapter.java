// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsFullScreenVideoAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsScene;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.ksad.BuildConfig;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KsAdAdapter extends CustomAdsAdapter {

    private static final String TAG = "KsAdAdapter";

    private final ConcurrentMap<String, KsFullScreenVideoAd> mInterstitialAds;
    private final ConcurrentMap<String, KsRewardVideoAd> mRewardedAds;

    public KsAdAdapter() {
        mInterstitialAds = new ConcurrentHashMap<>();
        mRewardedAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return KsAdSDK.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_21;
    }

    /*********************************Interstitial***********************************/
    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        AdLog.getSingleton().LogE(TAG + " 插屏初始化...");
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            String appKey = (String) dataMap.get("AppKey");
            initSDK(activity, appKey, new KsAdManagerHolder.InitCallback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                }

                @Override
                public void onFailed() {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Init Error"));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, final String adUnitId, Map<String, Object> extras, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        AdLog.getSingleton().LogE(TAG + "插屏广告加载中...");
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            long adValue;
            try {
                adValue = Long.parseLong(adUnitId);
            } catch (Exception e) {
                adValue = 0L;
            }
            KsLoadManager loadManager = KsAdSDK.getLoadManager();
            if (loadManager == null) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "KsAd Init Error"));
                }
                return;
            }
            KsScene scene = new KsScene.Builder(adValue).build();
            loadManager.loadFullScreenVideoAd(scene, new KsLoadManager.FullScreenVideoAdListener() {

                @Override
                public void onError(int code, String msg) {
                    if (mInterstitialAds.size() > 0) {
                        mInterstitialAds.remove(adUnitId);
                    }
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, code, msg));
                    }
                }

                @Override
                public void onFullScreenVideoAdLoad(List<KsFullScreenVideoAd> adList) {
                    if (adList != null && adList.size() > 0) {
                        AdLog.getSingleton().LogE(TAG + " 插屏加载成功.....");
                        mInterstitialAds.put(adUnitId, adList.get(0));
                        if (callback != null) {
                            callback.onInterstitialAdLoadSuccess();
                        }
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (!isInterstitialAdAvailable(adUnitId)) {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Not Ready"));
            return;
        }
        KsFullScreenVideoAd ksFullScreenVideoAd = mInterstitialAds.get(adUnitId);
        if (ksFullScreenVideoAd != null) {
            ksFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new KsFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                @Override
                public void onAdClicked() {
                    AdLog.getSingleton().LogE(TAG + "插屏广告点击.....");
                    if (callback != null) {
                        callback.onInterstitialAdClick();
                    }
                }

                @Override
                public void onPageDismiss() {
                    AdLog.getSingleton().LogE(TAG + "插屏广告关闭.....");
                    mInterstitialAds.remove(adUnitId);
                    if (callback != null) {
                        callback.onInterstitialAdClosed();
                    }
                }

                @Override
                public void onVideoPlayError(int code, int extra) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "播放出错 code= " + code));
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    AdLog.getSingleton().LogE(TAG + "插屏广告播放完成.....");
                }

                @Override
                public void onVideoPlayStart() {
                    AdLog.getSingleton().LogE(TAG + "插屏广告显示.....");
                    if (callback != null) {
                        callback.onInterstitialAdShowSuccess();
                    }
                }

                @Override
                public void onSkippedVideo() {
                    AdLog.getSingleton().LogE(TAG + "插屏广告跳过.....");
                }
            });
            ksFullScreenVideoAd.showFullScreenVideoAd(activity, null);
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        KsFullScreenVideoAd screenVideoAd = mInterstitialAds.get(adUnitId);
        if (screenVideoAd == null) {
            return false;
        }
        return screenVideoAd.isAdEnable();
    }

    /*********************************RewardedVideoAd***********************************/
    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        AdLog.getSingleton().LogE(TAG + "激励广告初始化...");
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            String appKey = (String) dataMap.get("AppKey");
            initSDK(activity, appKey, new KsAdManagerHolder.InitCallback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                }

                @Override
                public void onFailed() {
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Init Error"));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, final String adUnitId, Map<String, Object> extras, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        AdLog.getSingleton().LogE(TAG + "激励广告加载中...");
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            long adValue;
            try {
                adValue = Long.parseLong(adUnitId);
            } catch (Exception e) {
                adValue = 0L;
            }
            KsLoadManager loadManager = KsAdSDK.getLoadManager();
            if (loadManager == null) {
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "KsAd Init Error"));
                }
                return;
            }
            KsScene scene = new KsScene.Builder(adValue).build();
            loadManager.loadRewardVideoAd(scene, new KsLoadManager.RewardVideoAdListener() {
                @Override
                public void onError(int code, String msg) {
                    if (callback != null) {
                        AdLog.getSingleton().LogE(TAG + "激励加载失败....." + msg);
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, code, msg));
                    }
                    if (mRewardedAds.size() > 0) {
                        mRewardedAds.remove(adUnitId);
                    }
                }

                @Override
                public void onRewardVideoAdLoad(List<KsRewardVideoAd> adList) {
                    if (adList != null && adList.size() > 0) {
                        AdLog.getSingleton().LogE(TAG + "激励加载成功.....");
                        mRewardedAds.put(adUnitId, adList.get(0));
                        if (callback != null) {
                            callback.onRewardedVideoLoadSuccess();
                        }
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void showRewardedVideo(final Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        if (!isRewardedVideoAvailable(adUnitId)) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
            return;
        }
        KsRewardVideoAd rewardVideoAd = mRewardedAds.get(adUnitId);
        if (rewardVideoAd != null) {
            rewardVideoAd.setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {

                @Override
                public void onAdClicked() {
                    AdLog.getSingleton().LogE(TAG + "激励广告点击.....");
                    if (callback != null) {
                        callback.onRewardedVideoAdClicked();
                    }
                }

                @Override
                public void onPageDismiss() {
                    AdLog.getSingleton().LogE(TAG + "激励广告关闭......");
                    mRewardedAds.remove(adUnitId);
                    if (callback != null) {
                        callback.onRewardedVideoAdClosed();
                    }
                }

                @Override
                public void onVideoPlayError(int code, int extra) {
                    AdLog.getSingleton().LogE(TAG + "激励广告播放出错......");
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "onVideoError code = " + code));
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    AdLog.getSingleton().LogE(TAG + "激励广告播放完成......");
                    if (callback != null) {
                        callback.onRewardedVideoAdEnded();
                    }
                }

                @Override
                public void onVideoPlayStart() {
                    AdLog.getSingleton().LogE(TAG + "激励广告展示.....");
                    if (callback != null) {
                        callback.onRewardedVideoAdShowSuccess();
                        callback.onRewardedVideoAdStarted();
                    }
                }

                @Override
                public void onRewardVerify() {
                    AdLog.getSingleton().LogE(TAG + "激励广告奖励回调.....");
                    if (callback != null) {
                        callback.onRewardedVideoAdRewarded();
                    }
                }
            });
            rewardVideoAd.showRewardVideoAd(activity, null);
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        KsRewardVideoAd rewardVideoAd = mRewardedAds.get(adUnitId);
        if (rewardVideoAd == null) {
            return false;
        }
        return rewardVideoAd.isAdEnable();
    }

    private synchronized void initSDK(Activity context, String appKey, KsAdManagerHolder.InitCallback callback) {
        KsAdManagerHolder.init(context, appKey, callback);
    }

}
