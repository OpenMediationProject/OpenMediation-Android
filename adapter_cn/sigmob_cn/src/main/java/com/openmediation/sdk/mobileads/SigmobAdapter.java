// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.interstitial.WindInterstitialAd;
import com.sigmob.windad.interstitial.WindInterstitialAdListener;
import com.sigmob.windad.interstitial.WindInterstitialAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardInfo;
import com.sigmob.windad.rewardVideo.WindRewardVideoAd;
import com.sigmob.windad.rewardVideo.WindRewardVideoAdListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SigmobAdapter extends CustomAdsAdapter implements WindRewardVideoAdListener, WindInterstitialAdListener {
    private static final String TAG = "SigmobAdapter ";
    private final ConcurrentMap<String, WindRewardVideoAd> mRvAds;
    private final ConcurrentMap<String, WindInterstitialAd> mFvAds;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mFvCallbacks;

    public SigmobAdapter() {
        mRvAds = new ConcurrentHashMap<>();
        mFvAds = new ConcurrentHashMap<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mFvCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return WindAds.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.sigmob.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_20;
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        WindAds.sharedAds().setUserAge(age);
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
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    private void loadRvAd(Activity activity, String adUnitId, RewardedVideoCallback callback) {
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
            return;
        }
        try {
            realLoadRvAd(adUnitId, callback);
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
        if (isRewardedVideoAvailable(adUnitId)) {
            try {
                if (callback != null) {
                    mRvCallbacks.put(adUnitId, callback);
                }
                mRvAds.remove(adUnitId).show(null);
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                }
            }
            return;
        }
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "RewardedVideo is not ready"));
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mRvAds.get(adUnitId) != null && mRvAds.get(adUnitId).isReady();
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
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
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
        String error = check(adUnitId);
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
            return;
        }
        try {
            realLoadFullScreenVideoAd(adUnitId, callback);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void showInterstitialAd(final Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
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
            try {
                if (callback != null) {
                    mFvCallbacks.put(adUnitId, callback);
                }
                mFvAds.remove(adUnitId).show(null);
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                }
            }
            return;
        }
        if (callback != null) {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "InterstitialAd is not ready"));
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        WindInterstitialAd request = mFvAds.get(adUnitId);
        return request != null && request.isReady();
    }

    private void initSdk(final Activity activity) {
        try {
            String[] tmp = mAppKey.split("#");
            WindAds ads = WindAds.sharedAds();
            ads.startWithOptions(activity, new WindAdOptions(tmp[0], tmp[1]));
        } catch (Throwable ignored) {
        }
    }

    private void realLoadRvAd(final String placementId, final RewardedVideoCallback rvCallback) {
        WindRewardAdRequest rewardAdRequest = new WindRewardAdRequest(placementId, null, null);
        WindRewardVideoAd windRewardedVideoAd = new WindRewardVideoAd(rewardAdRequest);
        windRewardedVideoAd.setWindRewardVideoAdListener(this);
        mRvAds.put(placementId, windRewardedVideoAd);
        mRvCallbacks.put(placementId, rvCallback);
        windRewardedVideoAd.loadAd();
    }

    private void realLoadFullScreenVideoAd(final String placementId, final InterstitialAdCallback rvCallback) {
        WindInterstitialAdRequest request = new WindInterstitialAdRequest(placementId, "", null);
        WindInterstitialAd windInterstitialAd = new WindInterstitialAd(request);
        windInterstitialAd.setWindInterstitialAdListener(this);
        mFvAds.put(placementId, windInterstitialAd);
        mFvCallbacks.put(placementId, rvCallback);
        windInterstitialAd.loadAd();
    }

    @Override
    public void onRewardAdPreLoadSuccess(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdPreLoadSuccess..." + placementId);
    }

    @Override
    public void onRewardAdPreLoadFail(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdPreLoadFail..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "No Fill"));
        }
    }

    @Override
    public void onRewardAdLoadSuccess(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdLoadSuccess..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    @Override
    public void onRewardAdPlayStart(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdPlayStart..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
            callback.onRewardedVideoAdStarted();
        }
    }

    @Override
    public void onRewardAdPlayEnd(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdPlayEnd..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdEnded();
        }
    }

    @Override
    public void onRewardAdClicked(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdClicked..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    @Override
    public void onRewardAdClosed(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdClosed..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onRewardAdRewarded(WindRewardInfo windRewardInfo, String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdRewarded..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdRewarded();
        }
    }

    @Override
    public void onRewardAdLoadError(WindAdError windAdError, String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdLoadError..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, windAdError.getErrorCode(), windAdError.getMessage()));
        }
    }

    @Override
    public void onRewardAdPlayError(WindAdError windAdError, String placementId) {
        AdLog.getSingleton().LogD(TAG + "onRewardAdPlayError..." + placementId);
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "RewardedVideo play failed"));
        }
    }

    @Override
    public void onInterstitialAdPreLoadSuccess(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdPreLoadSuccess..." + placementId);
    }

    @Override
    public void onInterstitialAdPreLoadFail(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdPreLoadFail..." + placementId);
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "InterstitialAd ad load failed"));
        }
    }

    @Override
    public void onInterstitialAdLoadSuccess(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdLoadSuccess..." + placementId);
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    @Override
    public void onInterstitialAdPlayStart(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdPlayStart..." + placementId);
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    @Override
    public void onInterstitialAdPlayEnd(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdPlayEnd..." + placementId);
    }

    @Override
    public void onInterstitialAdClicked(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdClicked..." + placementId);
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClicked();
        }
    }

    @Override
    public void onInterstitialAdClosed(String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdClosed..." + placementId);
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    @Override
    public void onInterstitialAdLoadError(WindAdError windAdError, String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdLoadError..." + placementId);
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, windAdError.getErrorCode(), windAdError.getMessage());
            callback.onInterstitialAdLoadFailed(adapterError);
        }
    }

    @Override
    public void onInterstitialAdPlayError(WindAdError windAdError, String placementId) {
        AdLog.getSingleton().LogD(TAG + "onInterstitialAdPlayError..." + placementId);
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            AdapterError adapterError = AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, windAdError.getErrorCode(), windAdError.getMessage());
            callback.onInterstitialAdShowFailed(adapterError);
        }
    }

}
