// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsFullScreenVideoAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsScene;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.mobileads.ksad.BuildConfig;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KsAdAdapter extends CustomAdsAdapter {

    private static final String TAG = "KsAdAdapter ";

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

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        AdLog.getSingleton().LogD(TAG + " initInterstitialAd...");
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        initSDK(mAppKey, new KsAdManagerHolder.InitCallback() {
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
    }

    @Override
    public void loadInterstitialAd(Activity activity, final String adUnitId, Map<String, Object> extras, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        long adValue;
        try {
            adValue = Long.parseLong(adUnitId);
        } catch(Exception e) {
            adValue = 0L;
        }
        KsLoadManager loadManager = KsAdSDK.getLoadManager();
        if (loadManager == null) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "KsAd Load Error"));
            }
            return;
        }
        try {
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
                public void onRequestResult(int i) {

                }

                @Override
                public void onFullScreenVideoAdLoad(List<KsFullScreenVideoAd> adList) {
                    if (adList != null && adList.size() > 0) {
                        AdLog.getSingleton().LogE(TAG + " onFullScreenVideoAdLoad.....");
                        mInterstitialAds.put(adUnitId, adList.get(0));
                        if (callback != null) {
                            callback.onInterstitialAdLoadSuccess();
                        }
                    }
                }
            });
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (!isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Not Ready"));
            }
            return;
        }
        try {
            KsFullScreenVideoAd ksFullScreenVideoAd = mInterstitialAds.get(adUnitId);
            if (ksFullScreenVideoAd != null) {
                ksFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new KsFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                    @Override
                    public void onAdClicked() {
                        AdLog.getSingleton().LogD(TAG + "ksFullScreenVideoAd onAdClicked.....");
                        if (callback != null) {
                            callback.onInterstitialAdClicked();
                        }
                    }

                    @Override
                    public void onPageDismiss() {
                        AdLog.getSingleton().LogD(TAG + "ksFullScreenVideoAd onPageDismiss.....");
                        mInterstitialAds.remove(adUnitId);
                        if (callback != null) {
                            callback.onInterstitialAdClosed();
                        }
                    }

                    @Override
                    public void onVideoPlayError(int code, int extra) {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ksFullScreenVideoAd onVideoPlayError code= " + code));
                        }
                    }

                    @Override
                    public void onVideoPlayEnd() {
                        AdLog.getSingleton().LogD(TAG + "ksFullScreenVideoAd onVideoPlayEnd.....");
                    }

                    @Override
                    public void onVideoPlayStart() {
                        AdLog.getSingleton().LogD(TAG + "ksFullScreenVideoAd onVideoPlayStart.....");
                        if (callback != null) {
                            callback.onInterstitialAdShowSuccess();
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
                        AdLog.getSingleton().LogD(TAG + "ksFullScreenVideoAd onSkippedVideo.....");
                    }
                });
                ksFullScreenVideoAd.showFullScreenVideoAd(activity, null);
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
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
        AdLog.getSingleton().LogD(TAG + "initRewardedVideo...");
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        initSDK(mAppKey, new KsAdManagerHolder.InitCallback() {
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
    }

    @Override
    public void loadRewardedVideo(Activity activity, final String adUnitId, Map<String, Object> extras, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        AdLog.getSingleton().LogD(TAG + "loadRewardedVideo...");
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        long adValue;
        try {
            adValue = Long.parseLong(adUnitId);
        } catch(Exception e) {
            adValue = 0L;
        }
        KsLoadManager loadManager = KsAdSDK.getLoadManager();
        if (loadManager == null) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "KsAd Load Error"));
            }
            return;
        }
        try {
            KsScene scene = new KsScene.Builder(adValue).build();
            loadManager.loadRewardVideoAd(scene, new KsLoadManager.RewardVideoAdListener() {
                @Override
                public void onError(int code, String msg) {
                    if (callback != null) {
                        AdLog.getSingleton().LogE(TAG + "RewardedVideo Load Error....." + msg);
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, code, msg));
                    }
                    if (mRewardedAds.size() > 0) {
                        mRewardedAds.remove(adUnitId);
                    }
                }

                @Override
                public void onRequestResult(int i) {

                }

                @Override
                public void onRewardVideoAdLoad(List<KsRewardVideoAd> adList) {
                    if (adList != null && adList.size() > 0) {
                        AdLog.getSingleton().LogD(TAG + "onRewardVideoAdLoad.....");
                        mRewardedAds.put(adUnitId, adList.get(0));
                        if (callback != null) {
                            callback.onRewardedVideoLoadSuccess();
                        }
                    }
                }
            });
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void showRewardedVideo(final Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (!isRewardedVideoAvailable(adUnitId)) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
            return;
        }
        try {
            KsRewardVideoAd rewardVideoAd = mRewardedAds.get(adUnitId);
            if (rewardVideoAd != null) {
                rewardVideoAd.setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdClicked() {
                        AdLog.getSingleton().LogD(TAG + "RewardedVideo onAdClicked.....");
                        if (callback != null) {
                            callback.onRewardedVideoAdClicked();
                        }
                    }

                    @Override
                    public void onPageDismiss() {
                        AdLog.getSingleton().LogD(TAG + "RewardedVideo onPageDismiss......");
                        mRewardedAds.remove(adUnitId);
                        if (callback != null) {
                            callback.onRewardedVideoAdClosed();
                        }
                    }

                    @Override
                    public void onVideoPlayError(int code, int extra) {
                        AdLog.getSingleton().LogD(TAG + "RewardedVideo onVideoPlayError......");
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "onVideoError code = " + code));
                        }
                    }

                    @Override
                    public void onVideoPlayEnd() {
                        AdLog.getSingleton().LogD(TAG + "RewardedVideo onVideoPlayEnd......");
                        if (callback != null) {
                            callback.onRewardedVideoAdEnded();
                        }
                    }

                    @Override
                    public void onVideoSkipToEnd(long l) {

                    }

                    @Override
                    public void onVideoPlayStart() {
                        AdLog.getSingleton().LogD(TAG + "RewardedVideo onVideoPlayStart.....");
                        if (callback != null) {
                            callback.onRewardedVideoAdShowSuccess();
                            callback.onRewardedVideoAdStarted();
                        }
                    }

                    @Override
                    public void onRewardVerify() {
                        AdLog.getSingleton().LogD(TAG + "RewardedVideo onRewardVerify.....");
                        if (callback != null) {
                            callback.onRewardedVideoAdRewarded();
                        }
                    }
                });
                rewardVideoAd.showRewardVideoAd(activity, null);
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
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
        KsAdSplashManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        super.loadSplashAd(activity, adUnitId, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        KsAdSplashManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return KsAdSplashManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        KsAdSplashManager.getInstance().showAd(adUnitId, viewGroup, callback);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        KsAdSplashManager.getInstance().destroyAd(adUnitId);
    }

    private synchronized void initSDK(String appKey, KsAdManagerHolder.InitCallback callback) {
        KsAdManagerHolder.init(MediationUtil.getContext(), appKey, callback);
    }

}
