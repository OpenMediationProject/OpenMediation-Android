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
import com.openmediation.sdk.utils.AdLog;
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
    private static final String CONSENT_MESSAGE_VERSION = "1.0.0";

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
        return com.openmediation.sdk.mobileads.vungle.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_5;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (mInitState == InitState.INIT_SUCCESS) {
            Vungle.updateConsentStatus(consent ? Vungle.Consent.OPTED_IN : Vungle.Consent.OPTED_OUT, CONSENT_MESSAGE_VERSION);
        }
    }

    private void initSDK(final Activity activity) {
        mInitState = InitState.INIT_PENDING;
        Vungle.init(mAppKey, activity.getApplicationContext(), new InitCallback() {
            @Override
            public void onSuccess() {
                mInitState = InitState.INIT_SUCCESS;
                if (mUserConsent != null) {
                    setGDPRConsent(activity, mUserConsent);
                }

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
                if (!mRvCallback.isEmpty()) {
                    for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallback.entrySet()) {
                        videoCallbackEntry.getValue().onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
                    }
                }
                if (!mIsCallback.isEmpty()) {
                    for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                        interstitialAdCallbackEntry.getValue().onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                              AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
                    }
                }
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {
            }
        });
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
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
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Vungle init failed"));
                    }
                    break;
                default:
                    break;
            }
        } else {
            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
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
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Vungle load failed cause vungle not initialized " + adUnitId));
            }
        } else {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!mRvCallback.containsKey(adUnitId)) {
                mRvCallback.put(adUnitId, callback);
            }
            if (isRewardedVideoAvailable(adUnitId)) {
                Vungle.playAd(adUnitId, null, this);
            } else {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Vungle show video failed no ready"));
            }
        } else {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && Vungle.canPlayAd(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
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
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Vungle init failed"));
                    }
                    break;
                default:
                    break;
            }
        } else {
            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
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
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Vungle load failed cause vungle not initialized " + adUnitId));
            }
        } else {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!mIsCallback.containsKey(adUnitId)) {
                mIsCallback.put(adUnitId, callback);
            }
            if (isInterstitialAdAvailable(adUnitId)) {
                Vungle.playAd(adUnitId, null, this);
            } else {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad no ready"));
            }
        } else {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
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
                if (isCTAClicked) {
                    callback.onInterstitialAdClick();
                }
                callback.onInterstitialAdClosed();
            }
        }
    }

    @Override
    public void onError(String id, VungleException error) {
        if (mRvCallback.containsKey(id)) {
            RewardedVideoCallback callback = mRvCallback.get(id);
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
            }
        } else {
            InterstitialAdCallback callback = mIsCallback.get(id);
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
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
            if (mRvCallback.containsKey(id)) {
                RewardedVideoCallback callback = mRvCallback.get(id);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, cause.getExceptionCode(), cause.getLocalizedMessage()));
                }
            } else {
                InterstitialAdCallback callback = mIsCallback.get(id);
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, cause.getExceptionCode(), cause.getLocalizedMessage()));
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
