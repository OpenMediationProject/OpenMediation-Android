// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.utils.AdLog;
import com.vungle.warren.BuildConfig;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class VungleAdapter extends CustomAdsAdapter implements PlayAdCallback {

    private InitState mInitState = InitState.NOT_INIT;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallback;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallback;

    public VungleAdapter() {
        mIsCallback = new ConcurrentHashMap<>();
        mRvCallback = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getAdapterVersion() {
        return com.nbmediation.sdk.mobileads.vungle.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_5;
    }

    private void initSDK(Context activity) {
        mInitState = InitState.INIT_PENDING;
        Vungle.init(mAppKey, activity.getApplicationContext(), new InitCallback() {
            @Override
            public void onSuccess() {
                mInitState = InitState.INIT_SUCCESS;
                AdLog.getSingleton().LogD("Om-Vungle", "Vungle init success ");
                if (!mRvCallback.isEmpty()) {
                    for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallback.entrySet()) {
                        videoCallbackEntry.getValue().onRewardedVideoInitSuccess();
                    }
                }
                if (!mIsCallback.isEmpty()) {
                    for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                        interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
                    }
                }
            }

            @Override
            public void onError(VungleException error) {
                mInitState = InitState.INIT_FAIL;
                AdLog.getSingleton().LogE("Om-Vungle: Vungle init failed " + error.getLocalizedMessage());
                if (!mRvCallback.isEmpty()) {
                    for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallback.entrySet()) {
                        videoCallbackEntry.getValue().onRewardedVideoInitFailed("Vungle init failed " + error.getLocalizedMessage());
                    }
                }
                if (!mIsCallback.isEmpty()) {
                    for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                        interstitialAdCallbackEntry.getValue().onInterstitialAdInitFailed("Vungle init failed " + error.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {
            }
        });
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity, (String) dataMap.get("pid"));
        if (TextUtils.isEmpty(error)) {
            String pid = (String) dataMap.get("pid");
            switch (mInitState) {
                case NOT_INIT:
                    mRvCallback.put(pid, callback);
                    initSDK(activity);
                    break;
                case INIT_PENDING:
                    //waiting
                    mRvCallback.put(pid, callback);
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                    break;
                case INIT_FAIL:
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed("Vungle init failed ");
                    }
                    break;
                default:
                    break;
            }
        } else {
            AdLog.getSingleton().LogE("Om-Vungle: Vungle init failed ");
            callback.onRewardedVideoInitFailed(error);
        }
    }

    @Override
    public void loadRewardedVideo(Context activity, final String adUnitId, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!mRvCallback.containsKey(adUnitId)) {
                mRvCallback.put(adUnitId, callback);
            }
            if (Vungle.isInitialized()) {
                if (isRewardedVideoAvailable(adUnitId)) {
                    callback.onRewardedVideoLoadSuccess();
                } else {
                    Vungle.loadAd(adUnitId, new LoadCallback());
                }
            } else {
                callback.onRewardedVideoLoadFailed("Vungle load failed cause vungle not initialized " + adUnitId);
            }
        } else {
            AdLog.getSingleton().LogE("Om-Vungle: Vungle load failed, error: " + error);
            callback.onRewardedVideoLoadFailed(error);
        }
    }

    @Override
    public void showRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!mRvCallback.containsKey(adUnitId)) {
                mRvCallback.put(adUnitId, callback);
            }
            if (isRewardedVideoAvailable(adUnitId)) {
                Vungle.playAd(adUnitId, null, this);
            } else {
                callback.onRewardedVideoAdShowFailed("Vungle show video failed no ready");
            }
        } else {
            AdLog.getSingleton().LogE("Om-Vungle: Vungle show video failed, error:" + error);
            callback.onRewardedVideoAdShowFailed(error);
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && Vungle.canPlayAd(adUnitId);
    }

    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity, (String) dataMap.get("pid"));
        if (TextUtils.isEmpty(error)) {
            String pid = (String) dataMap.get("pid");
            switch (mInitState) {
                case NOT_INIT:
                    mIsCallback.put(pid, callback);
                    initSDK(activity);
                    break;
                case INIT_PENDING:
                    //waiting
                    mIsCallback.put(pid, callback);
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                    break;
                case INIT_FAIL:
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed("Vungle init failed ");
                    }
                    break;
                default:
                    break;
            }
        } else {
            AdLog.getSingleton().LogE("Om-Vungle: Vungle init failed ");
            callback.onInterstitialAdInitFailed(error);
        }
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!mIsCallback.containsKey(adUnitId)) {
                mIsCallback.put(adUnitId, callback);
            }
            if (Vungle.isInitialized()) {
                if (isRewardedVideoAvailable(adUnitId)) {
                    callback.onInterstitialAdLoadSuccess();
                } else {
                    Vungle.loadAd(adUnitId, new LoadCallback());
                }
            } else {
                callback.onInterstitialAdLoadFailed("Vungle load failed cause vungle not initialized " + adUnitId);
            }
        } else {
            AdLog.getSingleton().LogE("Om-Vungle: Vungle load interstitial ad failed, error: " + error);
            callback.onInterstitialAdLoadFailed(error);
        }
    }

    @Override
    public void showInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!mIsCallback.containsKey(adUnitId)) {
                mIsCallback.put(adUnitId, callback);
            }
            if (isInterstitialAdAvailable(adUnitId)) {
                Vungle.playAd(adUnitId, null, this);
            } else {
                callback.onInterstitialAdShowFailed("Vungle show interstitial failed no ready");
            }
        } else {
            AdLog.getSingleton().LogE("Om-Vungle: Vungle show interstitial failed, error:" + error);
            callback.onInterstitialAdShowFailed(error);
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && Vungle.canPlayAd(adUnitId);
    }

    @Override
    public void onAdStart(String id) {
        AdLog.getSingleton().LogD("Om-Vungle", "Vungle onAdStart, id:" + id);
        if (mRvCallback.containsKey(id)) {
            RewardedVideoCallback callback = mRvCallback.get(id);
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        } else {
            InterstitialAdCallback callback = mIsCallback.get(id);
            if (callback != null) {
                callback.onInterstitialAdShowSuccess();
            }
        }
    }

    @Override
    public void onAdEnd(String id, boolean completed, boolean isCTAClicked) {
        AdLog.getSingleton().LogD("Om-Vungle", "Vungle onAdEnd, id:"
                + id + ", completed:" + completed + ", isCtaClicked:" + isCTAClicked);
        if (mRvCallback.containsKey(id)) {
            RewardedVideoCallback callback = mRvCallback.get(id);
            if (callback != null) {
                //
                if (isCTAClicked) {
                    callback.onRewardedVideoAdClicked();
                }
                if (completed) {
                    callback.onRewardedVideoAdRewarded();
                }
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdClosed();
            }
        } else {
            InterstitialAdCallback callback = mIsCallback.get(id);
            if (callback != null) {
                //
                if (isCTAClicked) {
                    callback.onInterstitialAdClick();
                }
                callback.onInterstitialAdClosed();
            }
        }
    }

    @Override
    public void onError(String id, VungleException error) {
        AdLog.getSingleton().LogE("Om-Vungle: Vungle ad play failed, id: "
                + id + ", message: " + error.getLocalizedMessage());
        if (mRvCallback.containsKey(id)) {
            RewardedVideoCallback callback = mRvCallback.get(id);
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("Vungle video play failed, s:"
                        + id + ", message:" + error.getLocalizedMessage());
            }
        } else {
            InterstitialAdCallback callback = mIsCallback.get(id);
            if (callback != null) {
                callback.onInterstitialAdShowFailed("Vungle interstitial play failed, s:"
                        + id + ", message:" + error.getLocalizedMessage());
            }
        }
    }

    private class LoadCallback implements LoadAdCallback {

        @Override
        public void onAdLoad(String id) {
            if (mRvCallback.containsKey(id)) {
                RewardedVideoCallback callback = mRvCallback.get(id);
                if (callback != null) {
                    AdLog.getSingleton().LogD("Om-Vungle", "Vungle load video success ");
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                InterstitialAdCallback callback = mIsCallback.get(id);
                if (callback != null) {
                    AdLog.getSingleton().LogD("Om-Vungle", "Vungle load interstitial success ");
                    callback.onInterstitialAdLoadSuccess();
                }
            }
        }

        @Override
        public void onError(String id, VungleException cause) {
            AdLog.getSingleton().LogE("Om-Vungle: Vungle load failed, message:" + cause.getMessage());
            if (mRvCallback.containsKey(id)) {
                RewardedVideoCallback callback = mRvCallback.get(id);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed("Vungle load video failed, message:" + cause.getLocalizedMessage());
                }
            } else {
                InterstitialAdCallback callback = mIsCallback.get(id);
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed("Vungle load interstitial failed, message:" + cause.getMessage());
                }
            }
        }
    }

    /**
     * Vungle sdk Init State
     */
    private enum InitState {
        /**
         *
         */
        NOT_INIT,
        /**
         *
         */
        INIT_PENDING,
        /**
         *
         */
        INIT_SUCCESS,
        /**
         *
         */
        INIT_FAIL
    }
}
