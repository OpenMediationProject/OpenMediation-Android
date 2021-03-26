// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;

import net.pubnative.lite.sdk.HyBid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PubNativeAdapter extends CustomAdsAdapter implements PubNativeVideoCallback, PubNativeInterstitialCallback {

    private final ConcurrentHashMap<String, RewardedVideoCallback> mRvCallbacks;
    private final ConcurrentHashMap<String, InterstitialAdCallback> mIsCallbacks;

    public PubNativeAdapter() {
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return HyBid.getHyBidVersion();
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.pubnative.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_23;
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        HyBid.setCoppaEnabled(restricted);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        HyBid.setAge(String.valueOf(age));
    }

    @Override
    public void setUserGender(Context context, String gender) {
        super.setUserGender(context, gender);
        HyBid.setGender(gender);
    }

    private synchronized void initSDK(Activity activity, String appKey, PubNativeSingleTon.InitListener listener) {
        PubNativeSingleTon.getInstance().init(activity, appKey, listener);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        initSDK(activity, mAppKey, new PubNativeSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                }
            }
        });
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (isRewardedVideoAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
                return;
            }
            if (callback != null) {
                String error = PubNativeSingleTon.getInstance().getError(adUnitId);
                if (TextUtils.isEmpty(error)) {
                    error = "No Fill";
                }
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
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
            PubNativeSingleTon.getInstance().setVideoAdCallback(this);
            PubNativeSingleTon.getInstance().showRewardedVideo(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return PubNativeSingleTon.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }

        initSDK(activity, mAppKey, new PubNativeSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (!TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
            return;
        }
        if (callback != null) {
            String error = PubNativeSingleTon.getInstance().getError(adUnitId);
            if (TextUtils.isEmpty(error)) {
                error = "No Fill";
            }
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return PubNativeSingleTon.getInstance().isInterstitialReady(adUnitId);
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
            PubNativeSingleTon.getInstance().setInterstitialAdCallback(this);
            PubNativeSingleTon.getInstance().showInterstitial(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad not ready"));
            }
        }
    }

    @Override
    public void onRewardedOpened(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
            callback.onRewardedVideoAdStarted();
        }
    }

    @Override
    public void onRewardedClosed(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdEnded();
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onRewardedClick(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    @Override
    public void onReward(String placementId) {
        RewardedVideoCallback callback = mRvCallbacks.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdRewarded();
        }
    }

    @Override
    public void onInterstitialImpression(String placementId) {
        InterstitialAdCallback callback = mIsCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    @Override
    public void onInterstitialDismissed(String placementId) {
        InterstitialAdCallback callback = mIsCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    @Override
    public void onInterstitialClick(String placementId) {
        InterstitialAdCallback callback = mIsCallbacks.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClick();
        }
    }

}
