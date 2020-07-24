// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
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

    private AtomicBoolean hasInit = new AtomicBoolean(false);

    private ConcurrentLinkedQueue<String> mRvLoadTriggerIds;
    private ConcurrentLinkedQueue<String> mIsLoadTriggerIds;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    private CbCallback mCbDelegate;

    public ChartboostAdapter() {
        mIsLoadTriggerIds = new ConcurrentLinkedQueue<>();
        mRvLoadTriggerIds = new ConcurrentLinkedQueue<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    private void initSDK(final Activity activity) {
        AdLog.getSingleton().LogD("init chartboost sdk");
        if (!hasInit.get()) {
            mCbDelegate = new CbCallback();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        hasInit.set(true);
                        String[] tmp = mAppKey.split("#");
                        String appId = tmp[0];
                        String signature = tmp[1];
                        Chartboost.setPIDataUseConsent(activity, Chartboost.CBPIDataUseConsent.YES_BEHAVIORAL);
                        Chartboost.startWithAppId(activity.getApplication(), appId, signature);
                        Chartboost.setDelegate(mCbDelegate);
                        Chartboost.setMediation(Chartboost.CBMediation.CBMediationOther, getAdapterVersion(), "");
                        Chartboost.setShouldRequestInterstitialsInFirstSession(false);
                        Chartboost.setShouldPrefetchVideoContent(false);
                        Chartboost.setAutoCacheAds(true);

                    } catch (Exception e) {
                        AdLog.getSingleton().LogE("OM-Chartboost", e.getMessage());
                    }
                }
            });
        }
    }

    private void onInitCallback() {
        for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallbacks.entrySet()) {
            videoCallbackEntry.getValue().onRewardedVideoInitSuccess();
        }
        for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallbacks.entrySet()) {
            interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
        }
    }

    @Override
    public String getMediationVersion() {
        if (hasInit.get()) {
            return Chartboost.getSDKVersion();
        } else {
            return "";
        }
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
        if (consent) {
            Chartboost.setPIDataUseConsent(context, Chartboost.CBPIDataUseConsent.YES_BEHAVIORAL);
        } else {
            Chartboost.setPIDataUseConsent(context, Chartboost.CBPIDataUseConsent.NO_BEHAVIORAL);
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String checkError = check(activity);
        if (TextUtils.isEmpty(checkError)) {
            mRvCallbacks.put((String) dataMap.get("pid"), callback);
            if (!hasInit.get()) {
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
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
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
            mIsCallbacks.put((String) dataMap.get("pid"), callback);
            if (!hasInit.get()) {
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
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
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
