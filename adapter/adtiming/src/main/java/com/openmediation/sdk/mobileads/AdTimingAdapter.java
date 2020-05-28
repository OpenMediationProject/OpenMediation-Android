// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.AdTimingAds;
import com.adtiming.mediationsdk.InitCallback;
import com.adtiming.mediationsdk.core.AdTimingManager;
import com.adtiming.mediationsdk.mediation.MediationInterstitialListener;
import com.adtiming.mediationsdk.mediation.MediationRewardVideoListener;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.adtiming.BuildConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdTimingAdapter extends CustomAdsAdapter {

    private volatile InitState mInitState = InitState.NOT_INIT;

    private ConcurrentMap<String, RewardedVideoCallback> mRvCallback;
    private ConcurrentMap<String, RvListener> mRvListeners;

    private ConcurrentMap<String, InterstitialAdCallback> mIsCallback;
    private ConcurrentMap<String, IsListener> mIsListeners;

    public AdTimingAdapter() {
        mRvCallback = new ConcurrentHashMap<>();
        mRvListeners = new ConcurrentHashMap<>();

        mIsCallback = new ConcurrentHashMap<>();
        mIsListeners = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return Constants.SDK_V;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_1;
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        AdTimingAds.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        AdTimingAds.onPause(activity);
        super.onPause(activity);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            callback.onRewardedVideoInitFailed(error);
            return;
        }
        String pid = (String) dataMap.get("pid");
        switch (mInitState) {
            case NOT_INIT:
                mRvCallback.put(pid, callback);
                String appKey = (String) dataMap.get("AppKey");
                initSDK(activity, appKey);
                break;
            case INIT_PENDING:
                mRvCallback.put(pid, callback);
                break;
            case INIT_SUCCESS:
                callback.onRewardedVideoInitSuccess();
                break;
            case INIT_FAIL:
                callback.onRewardedVideoInitFailed("AdTiming initRewardedVideo failed");
                break;
            default:
                break;
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
            return;
        }
        if (!mRvCallback.containsKey(adUnitId)) {
            mRvCallback.put(adUnitId, callback);
        }

        if (!mRvListeners.containsKey(adUnitId)) {
            mRvListeners.put(adUnitId, new RvListener(adUnitId));
        }
        AdTimingManager.getInstance().setMediationRewardedVideoListener(adUnitId, mRvListeners.get(adUnitId));
        if (AdTimingManager.getInstance().isRewardedVideoReady(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            AdTimingManager.getInstance().loadRewardedVideo(adUnitId);
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (!mRvCallback.containsKey(adUnitId)) {
            mRvCallback.put(adUnitId, callback);
        }

        if (!mRvListeners.containsKey(adUnitId)) {
            mRvListeners.put(adUnitId, new RvListener(adUnitId));
        }
        if (!isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("AdTiming RewardedVideo is not ready for " + adUnitId);
            }
            return;
        }
        AdTimingManager.getInstance().setMediationRewardedVideoListener(adUnitId, mRvListeners.get(adUnitId));
        AdTimingManager.getInstance().showRewardedVideo(adUnitId, "");
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return AdTimingManager.getInstance().isRewardedVideoReady(adUnitId);
    }


    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdInitFailed(error);
            return;
        }
        String pid = (String) dataMap.get("pid");
        switch (mInitState) {
            case NOT_INIT:
                mIsCallback.put(pid, callback);
                String appKey = (String) dataMap.get("AppKey");
                initSDK(activity, appKey);
                break;
            case INIT_PENDING:
                mIsCallback.put(pid, callback);
                break;
            case INIT_SUCCESS:
                callback.onInterstitialAdInitSuccess();
                break;
            case INIT_FAIL:
                callback.onInterstitialAdInitFailed("AdTiming initInterstitialAd failed");
                break;
            default:
                break;
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
            return;
        }
        if (!mIsCallback.containsKey(adUnitId)) {
            mIsCallback.put(adUnitId, callback);
        }

        if (!mIsListeners.containsKey(adUnitId)) {
            mIsListeners.put(adUnitId, new IsListener(adUnitId));
        }
        AdTimingManager.getInstance().setMediationInterstitialAdListener(adUnitId, mIsListeners.get(adUnitId));
        if (AdTimingManager.getInstance().isInterstitialAdReady(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            AdTimingManager.getInstance().loadInterstitialAd(adUnitId);
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (!mIsCallback.containsKey(adUnitId)) {
            mIsCallback.put(adUnitId, callback);
        }

        if (!mIsListeners.containsKey(adUnitId)) {
            mIsListeners.put(adUnitId, new IsListener(adUnitId));
        }
        if (!isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("AdTiming InterstitialAd is not ready for " + adUnitId);
                return;
            }
        }
        AdTimingManager.getInstance().setMediationInterstitialAdListener(adUnitId, mIsListeners.get(adUnitId));
        AdTimingManager.getInstance().showInterstitialAd(adUnitId, "");
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return AdTimingManager.getInstance().isInterstitialAdReady(adUnitId);
    }

    private void initSDK(Activity activity, String appKey) {
        mInitState = InitState.INIT_PENDING;
        AdTimingAds.init(activity, appKey, new InitCallback() {
            @Override
            public void onSuccess() {
                mInitState = InitState.INIT_SUCCESS;
                if (!mRvCallback.isEmpty()) {
                    for (Map.Entry<String, RewardedVideoCallback> rewardedVideoCallbackEntry : mRvCallback.entrySet()) {
                        if (rewardedVideoCallbackEntry != null) {
                            rewardedVideoCallbackEntry.getValue().onRewardedVideoInitSuccess();
                        }
                    }
                }

                if (!mIsCallback.isEmpty()) {
                    for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                        if (interstitialAdCallbackEntry != null) {
                            interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
                        }
                    }
                }
            }

            @Override
            public void onError(AdTimingError adTimingError) {
                mInitState = InitState.INIT_FAIL;
                if (!mRvCallback.isEmpty()) {
                    for (Map.Entry<String, RewardedVideoCallback> rewardedVideoCallbackEntry : mRvCallback.entrySet()) {
                        if (rewardedVideoCallbackEntry != null) {
                            rewardedVideoCallbackEntry.getValue().onRewardedVideoInitFailed(adTimingError.toString());
                        }
                    }
                }

                if (!mIsCallback.isEmpty()) {
                    for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                        if (interstitialAdCallbackEntry != null) {
                            interstitialAdCallbackEntry.getValue().onInterstitialAdInitFailed(adTimingError.toString());
                        }
                    }
                }
            }
        });
    }

    private class RvListener implements MediationRewardVideoListener {

        private String mAdUnitId;

        private RvListener(String adUnitId) {
            mAdUnitId = adUnitId;
        }

        @Override
        public void onRewardedVideoLoadSuccess() {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onRewardedVideoLoadFailed(AdTimingError adTimingError) {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(adTimingError.toString());
            }
        }

        @Override
        public void onRewardedVideoAdShowed() {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
            }
        }

        @Override
        public void onRewardedVideoAdShowFailed(AdTimingError adTimingError) {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(adTimingError.toString());
            }
        }

        @Override
        public void onRewardedVideoAdClicked() {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onRewardedVideoAdClosed() {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onRewardedVideoAdStarted() {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onRewardedVideoAdEnded() {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onRewardedVideoAdRewarded() {
            RewardedVideoCallback callback = mRvCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        }
    }

    private class IsListener implements MediationInterstitialListener {

        private String mAdUnitId;

        private IsListener(String adUnitId) {
            mAdUnitId = adUnitId;
        }

        @Override
        public void onInterstitialAdLoadSuccess() {
            InterstitialAdCallback callback = mIsCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onInterstitialAdLoadFailed(AdTimingError adTimingError) {
            InterstitialAdCallback callback = mIsCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(adTimingError.toString());
            }
        }

        @Override
        public void onInterstitialAdShowed() {
            InterstitialAdCallback callback = mIsCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onInterstitialAdShowFailed(AdTimingError adTimingError) {
            InterstitialAdCallback callback = mIsCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdShowFailed(adTimingError.toString());
            }
        }

        @Override
        public void onInterstitialAdClosed() {
            InterstitialAdCallback callback = mIsCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onInterstitialAdClicked() {
            InterstitialAdCallback callback = mIsCallback.get(mAdUnitId);
            if (callback != null) {
                callback.onInterstitialAdClick();
            }
        }
    }

    /**
     * AdTiming sdk init state
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
