// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.crosspromotion.sdk.CrossPromotionAds;
import com.crosspromotion.sdk.interstitial.InterstitialAd;
import com.crosspromotion.sdk.interstitial.InterstitialAdListener;
import com.crosspromotion.sdk.promotion.PromotionAd;
import com.crosspromotion.sdk.promotion.PromotionAdListener;
import com.crosspromotion.sdk.promotion.PromotionAdRect;
import com.crosspromotion.sdk.utils.error.Error;
import com.crosspromotion.sdk.video.RewardedVideo;
import com.crosspromotion.sdk.video.RewardedVideoListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.PromotionAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CrossPromotionAdapter extends CustomAdsAdapter implements RewardedVideoListener, InterstitialAdListener,
        PromotionAdListener {
    private static final String PAY_LOAD = "pay_load";
    private final ConcurrentMap<String, RewardedVideoCallback> mVideoListeners;
    private final ConcurrentMap<String, InterstitialAdCallback> mInterstitialListeners;
    private final ConcurrentMap<String, PromotionAdCallback> mPromotionListeners;

    public CrossPromotionAdapter() {
        mVideoListeners = new ConcurrentHashMap<>();
        mInterstitialListeners = new ConcurrentHashMap<>();
        mPromotionListeners = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return CrossPromotionAds.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return CrossPromotionAds.getSDKVersion();
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_19;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        boolean init = CrossPromotionSingleTon.getInstance().init(activity);
        if (callback != null) {
            if (init) {
                callback.onRewardedVideoInitSuccess();
            } else {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "InitRewardedVideo failed"));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRewardedVideoAd(activity, adUnitId, extras, callback);
    }

    private void loadRewardedVideoAd(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (callback != null) {
            mVideoListeners.put(adUnitId, callback);
        }
        RewardedVideo.setAdListener(adUnitId, this);
        String payload = "";
        if (extras != null && extras.containsKey(PAY_LOAD)) {
            payload = String.valueOf(extras.get(PAY_LOAD));
        }
        RewardedVideo.loadAdWithPayload(adUnitId, payload, extras);
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
                mVideoListeners.put(adUnitId, callback);
            }
            RewardedVideo.setAdListener(adUnitId, this);
            RewardedVideo.showAd(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "No reward ad or not ready"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return RewardedVideo.isReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        boolean init = CrossPromotionSingleTon.getInstance().init(activity);
        if (callback != null) {
            if (init) {
                callback.onInterstitialAdInitSuccess();
            } else {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "InitInterstitialAd failed"));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadIsAd(activity, adUnitId, extras, callback);
    }

    private void loadIsAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        String payload = "";
        if (extras != null && extras.containsKey(PAY_LOAD)) {
            payload = String.valueOf(extras.get(PAY_LOAD));
        }
        if (callback != null) {
            mInterstitialListeners.put(adUnitId, callback);
        }
        InterstitialAd.setAdListener(adUnitId, this);
        InterstitialAd.loadAdWithPayload(adUnitId, payload, extras);
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                mInterstitialListeners.put(adUnitId, callback);
            }
            InterstitialAd.setAdListener(adUnitId, this);
            InterstitialAd.showAd(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "No interstitial ad or not ready"));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return InterstitialAd.isReady(adUnitId);
    }

    @Override
    public void initPromotionAd(Activity activity, Map<String, Object> dataMap, PromotionAdCallback callback) {
        super.initPromotionAd(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onPromotionAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapterName, error));
            }
            return;
        }
        boolean init = CrossPromotionSingleTon.getInstance().init(activity);
        if (callback != null) {
            if (init) {
                callback.onPromotionAdInitSuccess();
            } else {
                callback.onPromotionAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapterName, "InitPromotionAd failed"));
            }
        }
    }

    @Override
    public void loadPromotionAd(Activity activity, String adUnitId, Map<String, Object> extras, PromotionAdCallback callback) {
        super.loadPromotionAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onPromotionAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapterName, error));
            }
            return;
        }
        if (callback != null) {
            mPromotionListeners.put(adUnitId, callback);
        }
        PromotionAd.setAdListener(adUnitId, this);
        PromotionAd.loadAd(adUnitId, extras);
    }

    @Override
    public void showPromotionAd(Activity activity, String adUnitId, Map<String, Object> extras, PromotionAdCallback callback) {
        super.showPromotionAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onPromotionAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapterName, error));
            }
            return;
        }
        if (isPromotionAdAvailable(adUnitId)) {
            if (callback != null) {
                mPromotionListeners.put(adUnitId, callback);
            }
            PromotionAd.setAdListener(adUnitId, this);
            PromotionAd.showAd(activity, PromotionAdRect.getAdRect(extras), adUnitId);
        } else {
            if (callback != null) {
                callback.onPromotionAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapterName, "PromotionAd not ready"));
            }
        }
    }

    @Override
    public void hidePromotionAd(String adUnitId, PromotionAdCallback callback) {
        super.hidePromotionAd(adUnitId, callback);
        if (TextUtils.isEmpty(adUnitId)) {
            AdLog.getSingleton().LogE("CrossPromotionAdapter", "HidePromotionAd Failed: AdUnitId is empty");
            return;
        }
        PromotionAd.setAdListener(adUnitId, this);
        PromotionAd.hideAd(adUnitId);
    }

    @Override
    public boolean isPromotionAdAvailable(String adUnitId) {
        return PromotionAd.isReady(adUnitId);
    }

    @Override
    public void onInterstitialAdLoadSuccess(String placementId) {
        try {
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdClosed(String placementId) {
        try {
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdClosed();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdShowed(String placementId) {
        try {
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdShowSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdLoadFailed(String placementId, Error error) {
        try {
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdClicked(String placementId) {
        try {
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdClick();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInterstitialAdEvent(String placementId, String event) {
    }

    @Override
    public void onInterstitialAdShowFailed(String placementId, Error error) {
        try {
            InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }


    @Override
    public void onRewardedVideoAdLoadSuccess(String placementId) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdClosed(String placementId) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdShowed(String placementId) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdRewarded(String placementId) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdLoadFailed(String placementId, Error error) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdClicked(String placementId) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onVideoAdEvent(String placementId, String event) {
    }

    @Override
    public void onRewardedVideoAdShowFailed(String placementId, Error error) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdStarted(String placementId) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdStarted();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRewardedVideoAdEnded(String placementId) {
        try {
            RewardedVideoCallback callback = mVideoListeners.get(placementId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPromotionAdLoadSuccess(String placementId) {
        try {
            PromotionAdCallback callback = null;
            if (mPromotionListeners.containsKey(placementId)) {
                callback = mPromotionListeners.get(placementId);
            }
            if (callback != null) {
                callback.onPromotionAdLoadSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPromotionAdLoadFailed(String placementId, Error error) {
        try {
            PromotionAdCallback callback = null;
            if (mPromotionListeners.containsKey(placementId)) {
                callback = mPromotionListeners.get(placementId);
            }
            if (callback != null) {
                callback.onPromotionAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPromotionAdShowed(String placementId) {
        try {
            PromotionAdCallback callback = mPromotionListeners.get(placementId);
            if (callback != null) {
                callback.onPromotionAdShowSuccess();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPromotionAdHidden(String placementId) {
        try {
            PromotionAdCallback callback = null;
            if (mPromotionListeners.containsKey(placementId)) {
                callback = mPromotionListeners.get(placementId);
            }
            if (callback != null) {
                callback.onPromotionAdHidden();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPromotionAdShowFailed(String placementId, Error error) {
        try {
            PromotionAdCallback callback = null;
            if (mPromotionListeners.containsKey(placementId)) {
                callback = mPromotionListeners.get(placementId);
            }
            if (callback != null) {
                callback.onPromotionAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_PROMOTION, mAdapterName, error.getCode(), error.getMessage()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPromotionAdClicked(String placementId) {
        try {
            PromotionAdCallback callback = null;
            if (mPromotionListeners.containsKey(placementId)) {
                callback = mPromotionListeners.get(placementId);
            }
            if (callback != null) {
                callback.onPromotionAdClicked();
            }
        } catch (Exception ignored) {
        }
    }
}
