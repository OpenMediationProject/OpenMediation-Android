// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.chartboost.sdk.Privacy.model.CCPA;
import com.chartboost.sdk.Privacy.model.DataUseConsent;
import com.chartboost.sdk.Privacy.model.GDPR;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.chartboost.BuildConfig;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChartboostAdapter extends CustomAdsAdapter {

    private final AtomicBoolean hasInit = new AtomicBoolean(false);

    private final ConcurrentLinkedQueue<String> mRvLoadTriggerIds;
    private final ConcurrentLinkedQueue<String> mIsLoadTriggerIds;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvInitCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsInitCallbacks;

    private CbCallback mCbDelegate;

    public ChartboostAdapter() {
        mIsLoadTriggerIds = new ConcurrentLinkedQueue<>();
        mRvLoadTriggerIds = new ConcurrentLinkedQueue<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
        mRvInitCallbacks = new ConcurrentHashMap<>();
        mIsInitCallbacks = new ConcurrentHashMap<>();
    }

    private void initSDK(final Activity activity) {
        if (mCbDelegate == null) {
            mCbDelegate = new CbCallback();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (hasInit.get()) {
                    return;
                }
                try {
                    String[] tmp = mAppKey.split("#");
                    String appId = tmp[0];
                    String signature = tmp[1];
                    Chartboost.startWithAppId(activity.getApplication(), appId, signature);
                    Chartboost.setDelegate(mCbDelegate);
                    Chartboost.setMediation(Chartboost.CBMediation.CBMediationOther, getAdapterVersion(), "");
                    Chartboost.setShouldRequestInterstitialsInFirstSession(false);
                    Chartboost.setShouldPrefetchVideoContent(false);
                    Chartboost.setAutoCacheAds(true);
                    hasInit.set(true);
                } catch (Throwable e) {
                    AdLog.getSingleton().LogE("OM-Chartboost", e.getMessage());
                }
            }
        };
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    private void onInitCallback() {
        for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvInitCallbacks.entrySet()) {
            videoCallbackEntry.getValue().onRewardedVideoInitSuccess();
        }
        mRvInitCallbacks.clear();
        for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsInitCallbacks.entrySet()) {
            interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
        }
        mIsInitCallbacks.clear();
    }

    @Override
    public String getMediationVersion() {
        return Chartboost.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_12;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (context != null) {
            if (consent) {
                Chartboost.addDataUseConsent(context, new GDPR(GDPR.GDPR_CONSENT.BEHAVIORAL));
            } else {
                Chartboost.addDataUseConsent(context, new GDPR(GDPR.GDPR_CONSENT.NON_BEHAVIORAL));
            }
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        if (context != null) {
            DataUseConsent dataUseConsent;
            if (value) {
                dataUseConsent = new CCPA(CCPA.CCPA_CONSENT.OPT_OUT_SALE);
            } else {
                dataUseConsent = new CCPA(CCPA.CCPA_CONSENT.OPT_IN_SALE);
            }
            Chartboost.addDataUseConsent(context, dataUseConsent);
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String checkError = check(activity);
        if (TextUtils.isEmpty(checkError)) {
            if (!hasInit.get()) {
                mRvInitCallbacks.put((String) dataMap.get("pid"), callback);
                initSDK(activity);
            } else {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (Chartboost.hasRewardedVideo(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                mRvLoadTriggerIds.add(adUnitId);
                if (callback != null) {
                    mRvCallbacks.put(adUnitId, callback);
                }
                if (Chartboost.getDelegate() == null) {
                    Chartboost.setDelegate(mCbDelegate);
                }
                Chartboost.cacheRewardedVideo(adUnitId);
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (Chartboost.hasRewardedVideo(adUnitId)) {
                if (callback != null) {
                    mRvCallbacks.put(adUnitId, callback);
                }
                if (Chartboost.getDelegate() == null) {
                    Chartboost.setDelegate(mCbDelegate);
                }
                Chartboost.showRewardedVideo(adUnitId);
            } else {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "ad not ready"));
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return Chartboost.hasRewardedVideo(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String checkError = check(activity);
        if (TextUtils.isEmpty(checkError)) {
            if (!hasInit.get()) {
                mIsInitCallbacks.put((String) dataMap.get("pid"), callback);
                initSDK(activity);
            } else {
                callback.onInterstitialAdInitSuccess();
            }
        } else {
            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (Chartboost.hasInterstitial(adUnitId)) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                mIsLoadTriggerIds.add(adUnitId);
                if (callback != null) {
                    mIsCallbacks.put(adUnitId, callback);
                }
                if (Chartboost.getDelegate() == null) {
                    Chartboost.setDelegate(mCbDelegate);
                }
                Chartboost.cacheInterstitial(adUnitId);
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (Chartboost.hasInterstitial(adUnitId)) {
                if (callback != null) {
                    mIsCallbacks.put(adUnitId, callback);
                }
                if (Chartboost.getDelegate() == null) {
                    Chartboost.setDelegate(mCbDelegate);
                }
                Chartboost.showInterstitial(adUnitId);
            } else {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad not ready"));
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return Chartboost.hasInterstitial(adUnitId);
    }

    class CbCallback extends ChartboostDelegate {

        @Override
        public void didCacheInterstitial(String location) {
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            AdLog.getSingleton().LogD("OM-Chartboost Interstitial ad load success");
            if (listener != null && mIsLoadTriggerIds.contains(location)) {
                listener.onInterstitialAdLoadSuccess();
                mIsLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            String errorString = error != null ? error.name() : " error message ";
            if (listener != null && mIsLoadTriggerIds.contains(location)) {
                listener.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, errorString));
                mIsLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didClickInterstitial(String location) {
            AdLog.getSingleton().LogD("OM-Chartboost Interstitial ad click");
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            if (listener != null) {
                listener.onInterstitialAdClick();
            }
        }

        @Override
        public void didDisplayInterstitial(String location) {
            AdLog.getSingleton().LogD("OM-Chartboost Interstitial ad display");
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            if (listener != null) {
                listener.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void didDismissInterstitial(String location) {
            AdLog.getSingleton().LogD("OM-Chartboost Interstitial ad close");
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            if (listener != null) {
                listener.onInterstitialAdClosed();
            }
        }

        @Override
        public void didCacheRewardedVideo(String location) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            AdLog.getSingleton().LogD("OM-Chartboost RewardVideo ad load success");
            if (listener != null && mRvLoadTriggerIds.contains(location)) {
                listener.onRewardedVideoLoadSuccess();
                mRvLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            String errorString = error != null ? error.name() : " error message ";
            if (listener != null && mRvLoadTriggerIds.contains(location)) {
                listener.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, errorString));
                mRvLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didClickRewardedVideo(String location) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            AdLog.getSingleton().LogD("OM-Chartboost RewardVideo ad click");
            if (listener != null) {
                listener.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void didCompleteRewardedVideo(String location, int reward) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            AdLog.getSingleton().LogD("OM-Chartboost RewardVideo ad complete");
            if (listener != null) {
                listener.onRewardedVideoAdEnded();
                listener.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void didDismissRewardedVideo(String location) {
            AdLog.getSingleton().LogD("OM-Chartboost RewardVideo ad close");
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            if (listener != null) {
                listener.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void didDisplayRewardedVideo(String location) {
            AdLog.getSingleton().LogD("OM-Chartboost RewardVideo ad display");
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            if (listener != null) {
                listener.onRewardedVideoAdShowSuccess();
                listener.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void didInitialize() {
            super.didInitialize();
            AdLog.getSingleton().LogD("OM-Chartboost init success");
            onInitCallback();
        }
    }
}
