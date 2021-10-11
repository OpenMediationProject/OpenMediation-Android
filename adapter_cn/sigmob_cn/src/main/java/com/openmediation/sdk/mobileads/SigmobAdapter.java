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
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.interstitial.WindInterstitialAd;
import com.sigmob.windad.interstitial.WindInterstitialAdListener;
import com.sigmob.windad.interstitial.WindInterstitialAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardInfo;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAdListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SigmobAdapter extends CustomAdsAdapter implements WindRewardedVideoAdListener, WindInterstitialAdListener {
    private final ConcurrentMap<String, WindRewardedVideoAd> mRvAds;
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
        String error = check(activity, adUnitId);
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
            realLoadRvAd(activity, adUnitId, callback);
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
                mRvAds.remove(adUnitId).show(activity, null);
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
            return;
        }
        try {
            realLoadFullScreenVideoAd(activity, adUnitId, callback);
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
                mFvAds.remove(adUnitId).show(activity, null);
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
            ads.startWithOptions(activity, new WindAdOptions(tmp[0], tmp[1], false));
        } catch (Throwable ignored) {
        }
    }

    private void realLoadRvAd(final Activity activity, final String placementId, final RewardedVideoCallback rvCallback) {
        WindRewardAdRequest rewardAdRequest = new WindRewardAdRequest(placementId, null, null);
        WindRewardedVideoAd windRewardedVideoAd = new WindRewardedVideoAd(activity, rewardAdRequest);
        windRewardedVideoAd.setWindRewardedVideoAdListener(this);
        mRvAds.put(placementId, windRewardedVideoAd);
        mRvCallbacks.put(placementId, rvCallback);
        windRewardedVideoAd.loadAd();
    }

    private void realLoadFullScreenVideoAd(final Activity activity, final String placementId, final InterstitialAdCallback rvCallback) {
        WindInterstitialAdRequest request = new WindInterstitialAdRequest(placementId, "", null);
        WindInterstitialAd windInterstitialAd = new WindInterstitialAd(activity, request);
        windInterstitialAd.setWindInterstitialAdListener(this);
        mFvAds.put(placementId, windInterstitialAd);
        mFvCallbacks.put(placementId, rvCallback);
        windInterstitialAd.loadAd();
    }

    @Override
    public void onVideoAdPreLoadSuccess(String placementId) {
    }

    @Override
    public void onVideoAdPreLoadFail(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "No Fill"));
        }
    }

    @Override
    public void onVideoAdLoadSuccess(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    @Override
    public void onVideoAdPlayStart(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
            callback.onRewardedVideoAdStarted();
        }
    }

    @Override
    public void onVideoAdPlayEnd(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdEnded();
        }
    }

    @Override
    public void onVideoAdClicked(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    @Override
    public void onVideoAdClosed(WindRewardInfo windRewardInfo, String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            if (windRewardInfo != null && windRewardInfo.isComplete()) {
                callback.onRewardedVideoAdRewarded();
            }
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onVideoAdLoadError(WindAdError windAdError, String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, windAdError.getErrorCode(), windAdError.getMessage()));
        }
    }

    @Override
    public void onVideoAdPlayError(WindAdError windAdError, String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "RewardedVideo play failed"));
        }
    }

    @Override
    public void onInterstitialAdPreLoadSuccess(String placementId) {
    }

    @Override
    public void onInterstitialAdPreLoadFail(String placementId) {
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "InterstitialAd ad load failed"));
        }
    }

    @Override
    public void onInterstitialAdLoadSuccess(String placementId) {
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    @Override
    public void onInterstitialAdPlayStart(String placementId) {
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    @Override
    public void onInterstitialAdPlayEnd(String placementId) {
    }

    @Override
    public void onInterstitialAdClicked(String placementId) {
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClicked();
        }
    }

    @Override
    public void onInterstitialAdClosed(String placementId) {
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    @Override
    public void onInterstitialAdLoadError(WindAdError windAdError, String placementId) {
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, windAdError.getErrorCode(), windAdError.getMessage());
            callback.onInterstitialAdLoadFailed(adapterError);
        }
    }

    @Override
    public void onInterstitialAdPlayError(WindAdError windAdError, String placementId) {
        InterstitialAdCallback callback = mFvCallbacks.get(placementId);
        if (callback != null) {
            AdapterError adapterError = AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, windAdError.getErrorCode(), windAdError.getMessage());
            callback.onInterstitialAdShowFailed(adapterError);
        }
    }

}
