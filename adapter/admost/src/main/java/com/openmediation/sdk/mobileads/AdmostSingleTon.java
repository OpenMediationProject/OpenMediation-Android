// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.mediation.BidCallback;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import admost.sdk.AdMostInterstitial;
import admost.sdk.AdMostView;
import admost.sdk.AdMostViewBinder;
import admost.sdk.base.AdMost;
import admost.sdk.base.AdMostConfiguration;
import admost.sdk.listener.AdMostAdListener;
import admost.sdk.listener.AdMostInitListener;
import admost.sdk.listener.AdMostViewListener;

public class AdmostSingleTon {

    private static final String TAG = "OM-AdMost";
    private Boolean mUserConsent = null;
    private Boolean mAgeRestricted = null;
    private Integer mUserAge = null;
    private String mUserGender = null;
    private Boolean mUSPrivacyLimit = null;

    private final ConcurrentHashMap<String, AdMostInterstitial> mRvAds;
    private final ConcurrentHashMap<String, AdMostInterstitial> mIsAds;
    private final ConcurrentHashMap<String, AdmostBannerAdsConfig> mBannerAds;
    private final List<AdmostBannerAdsConfig> mReadyNativeAds;
    private final ConcurrentHashMap<String, AdmostBannerCallback> mAdListeners;
    private final ConcurrentHashMap<AdmostBannerAdsConfig, AdmostBannerCallback> mNativeAdListeners;

    private AdmostVideoCallback mVideoAdCallback;
    private AdmostInterstitialCallback mInterstitialAdCallback;

    private final List<InitListener> mListeners = new CopyOnWriteArrayList<>();

    private static class Holder {
        private static final AdmostSingleTon INSTANCE = new AdmostSingleTon();
    }

    private AdmostSingleTon() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mReadyNativeAds = new CopyOnWriteArrayList<>();
        mAdListeners = new ConcurrentHashMap<>();
        mNativeAdListeners = new ConcurrentHashMap<>();
    }

    static AdmostSingleTon getInstance() {
        return Holder.INSTANCE;
    }

    void onResume() {
        try {
            for (AdmostBannerAdsConfig config : mBannerAds.values()) {
                if (config != null && config.getAdMostView() != null) {
                    config.getAdMostView().resume();
                }
            }
            for (AdmostBannerAdsConfig config : mReadyNativeAds) {
                if (config != null && config.getAdMostView() != null) {
                    config.getAdMostView().resume();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    void onPause() {
        try {
            for (AdmostBannerAdsConfig config : mBannerAds.values()) {
                if (config != null && config.getAdMostView() != null) {
                    config.getAdMostView().pause();
                }
            }
            for (AdmostBannerAdsConfig config : mReadyNativeAds) {
                if (config != null && config.getAdMostView() != null) {
                    config.getAdMostView().pause();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    boolean isInit() {
        return AdMost.getInstance().isInitCompleted();
    }

    synchronized void init(final Activity activity, final String appKey, InitListener listener) {
        if (isInit()) {
            if (listener != null) {
                listener.initSuccess();
            }
            return;
        }
        if (activity == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                listener.initFailed(0, "Context is null or AppKey is empty");
            }
            AdLog.getSingleton().LogE(TAG, "Init Failed: Context is null or AppKey is empty!");
            return;
        }
        if (listener != null) {
            mListeners.add(listener);
        }
        init(activity, appKey);
    }

    void setCustomParam() {
        Map<String, Object> policySettings = MediationUtil.getPolicySettings();
        if (policySettings == null || policySettings.isEmpty()) {
            return;
        }
        if (policySettings.containsKey("age")) {
            mUserAge = (Integer) policySettings.get("age");
        }
        if (policySettings.containsKey("gender")) {
            mUserGender = (String) policySettings.get("gender");
        }
        if (policySettings.containsKey("consent")) {
            mUserConsent = (Boolean) policySettings.get("consent");
        }
        if (policySettings.containsKey("restricted")) {
            mAgeRestricted = (Boolean) policySettings.get("restricted");
        }
        if (policySettings.containsKey("usPrivacyLimit")) {
            mUSPrivacyLimit = (Boolean) policySettings.get("usPrivacyLimit");
        }
    }

    private synchronized void init(Activity activity, String appKey) {
        AdMostConfiguration.Builder builder = new AdMostConfiguration.Builder(activity, appKey);
        setCustomParam();
        if (mUserAge != null) {
            builder.age(mUserAge);
        }
        if (mAgeRestricted != null) {
            builder.setUserChild(mAgeRestricted);
        }
        if (mUserGender != null) {
            if (TextUtils.equals(mUserGender, "female")) {
                builder.gender(AdMost.GENDER_FEMALE);
            } else if (TextUtils.equals(mUserGender, "male")) {
                builder.gender(AdMost.GENDER_MALE);
            } else {
                builder.gender(AdMost.GENDER_UNKNOWN);
            }
        }

        if (mUserConsent != null) {
            builder.setSubjectToGDPR(!mUserConsent);
            builder.setUserConsent(mUserConsent);
        }
        if (mUSPrivacyLimit != null) {
            builder.setSubjectToCCPA(mUSPrivacyLimit);
        }

        AdMost.getInstance().init(builder.build(), new AdMostInitListener() {
            @Override
            public void onInitCompleted() {
                AdLog.getSingleton().LogD(TAG, "AdMost SDK initialized successfully");
                for (InitListener listener : mListeners) {
                    if (listener != null) {
                        listener.initSuccess();
                    }
                }
                mListeners.clear();
            }

            @Override
            public void onInitFailed(int err) {
                AdLog.getSingleton().LogD(TAG, "AdMost SDK initialized failed, err code = " + err);
                for (InitListener listener : mListeners) {
                    if (listener != null) {
                        listener.initFailed(err, "AdMost init failed, err code = " + err);
                    }
                }
                mListeners.clear();
            }
        });
    }

    void getBidResponse(Context context, Map<String, Object> dataMap, BidCallback callback) {
        if (isInit()) {
            executeBid(dataMap, callback);
            return;
        }

        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            activity = MediationUtil.getActivity();
        }
        init(activity, appKey, new AdmostSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(int code, String error) {
                if (callback != null) {
                    callback.onBidFailed("AdMost SDK init error: " + error + ", code: " + code);
                }
            }
        });
    }

    private void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
        String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
        if (adType == BidConstance.INTERSTITIAL) {
            loadInterstitial(adUnitId, null, callback);
        } else if (adType == BidConstance.VIDEO) {
            loadRewardedVideo(adUnitId, null, callback);
        } else if (adType == BidConstance.BANNER) {
            AdSize adSize = (AdSize) dataMap.get(BidConstance.BID_BANNER_SIZE);
            int height = 50;
            if (adSize != null) {
                height = getAdSizeHeight(adSize.getDescription());
            }
            loadBanner(adUnitId, height, null, callback);
        } else if (adType == BidConstance.NATIVE) {
            String pid = "";
            Object placementId = dataMap.get(BidConstance.BID_OM_PLACEMENT_ID);
            if (placementId != null) {
                pid = String.valueOf(placementId);
            }
            loadNative(pid, adUnitId, null, callback);
        } else {
            if (callback != null) {
                callback.onBidFailed("unSupport bid type");
            }
        }
    }

    void setVideoAdCallback(AdmostVideoCallback callback) {
        mVideoAdCallback = callback;
    }

    void setInterstitialAdCallback(AdmostInterstitialCallback callback) {
        mInterstitialAdCallback = callback;
    }

    void loadRewardedVideo(final String adUnitId, RewardedVideoCallback adCallback, BidCallback bidCallback) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            String error = "Activity is null";
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd Load Error : " + error);
            if (adCallback != null) {
                onBidFailed(error, adCallback);
                adCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "AdmostAdapter", error));
            }
            if (bidCallback != null) {
                onBidFailed(error, bidCallback);
            }
            return;
        }
        try {
            InnerRewardedAdListener listener = new InnerRewardedAdListener(adCallback, bidCallback);
            AdMostInterstitial interstitial = new AdMostInterstitial(activity, adUnitId, listener);
            listener.setParameters(interstitial, adUnitId);
            interstitial.refreshAd(false);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd Load Error : " + e.getMessage());
            if (adCallback != null) {
                onBidFailed(e.getMessage(), adCallback);
                adCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "AdmostAdapter", "Unknown Error, " + e.getMessage()));
            }
            if (bidCallback != null) {
                onBidFailed(e.getMessage(), bidCallback);
            }
        }
    }

    boolean isRewardedVideoReady(final String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        AdMostInterstitial rewardedAd = mRvAds.get(adUnitId);
        return rewardedAd != null && rewardedAd.isLoaded();
    }

    void showRewardedVideo(final String adUnitId) {
        AdMostInterstitial rewardedAd = mRvAds.get(adUnitId);
        if (rewardedAd != null) {
            rewardedAd.show();
            mRvAds.remove(adUnitId);
        }
    }

    void loadInterstitial(String adUnitId, InterstitialAdCallback adCallback, BidCallback bidCallback) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            String error = "Activity is null";
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : " + error);
            if (adCallback != null) {
                onBidFailed(error, adCallback);
                adCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "AdmostAdapter", error));
            }
            if (bidCallback != null) {
                onBidFailed(error, bidCallback);
            }
            return;
        }
        try {
            InnerInterstitialAdListener listener = new InnerInterstitialAdListener(adCallback, bidCallback);
            AdMostInterstitial interstitial = new AdMostInterstitial(activity, adUnitId, listener);
            listener.setParameters(interstitial, adUnitId);
            interstitial.refreshAd(false);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : " + e.getMessage());
            if (adCallback != null) {
                onBidFailed(e.getMessage(), adCallback);
                adCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "AdmostAdapter", "Unknown Error, " + e.getMessage()));
            }
            if (bidCallback != null) {
                onBidFailed(e.getMessage(), bidCallback);
            }
        }
    }

    boolean isInterstitialReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        AdMostInterstitial interstitialAd = mIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isLoaded();
    }

    void showInterstitial(String adUnitId) {
        AdMostInterstitial interstitialAd = mIsAds.get(adUnitId);
        if (interstitialAd != null) {
            interstitialAd.show();
            mIsAds.remove(adUnitId);
        }
    }

    private class InnerRewardedAdListener implements AdMostAdListener {
        AdMostInterstitial mRewarded;
        String mAdUnitId;
        BidCallback mBidCallback;
        RewardedVideoCallback mAdCallback;

        public InnerRewardedAdListener(RewardedVideoCallback adCallback, BidCallback callback) {
            mBidCallback = callback;
            mAdCallback = adCallback;
        }

        public void setParameters(AdMostInterstitial rewarded, String adUnitId) {
            mRewarded = rewarded;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onReady(String network, int ecpm) {
            AdLog.getSingleton().LogD(TAG, "AdMost onRewardedLoaded PlacementId : " + mAdUnitId);
            mRvAds.put(mAdUnitId, mRewarded);
            if (mAdCallback != null) {
                onBidSuccess(ecpm, null, mAdCallback);
                mAdCallback.onRewardedVideoLoadSuccess();
            }
            if (mBidCallback != null) {
                onBidSuccess(ecpm, null, mBidCallback);
            }
        }

        @Override
        public void onFail(int errorCode) {
            AdLog.getSingleton().LogE(TAG, "AdMost RewardedAd LoadFailed : " + errorCode);
            mRvAds.remove(mAdUnitId);

            if (mAdCallback != null) {
                onBidFailed("RewardedAd LoadFailed : " + errorCode, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "AdmostAdapter", errorCode + "");
                mAdCallback.onRewardedVideoLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed("RewardedAd LoadFailed : " + errorCode, mBidCallback);
            }
        }

        @Override
        public void onDismiss(String message) {
            AdLog.getSingleton().LogD(TAG, "AdMost RewardedAd Closed");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onRewardedClosed(mAdUnitId);
            }
        }

        @Override
        public void onComplete(String network) {
            AdLog.getSingleton().LogD(TAG, "AdMost RewardedAd onReward");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onRewardedComplete(mAdUnitId);
            }
        }

        @Override
        public void onShown(String network) {
            AdLog.getSingleton().LogD(TAG, "AdMost RewardedAd Opened");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onRewardedOpened(mAdUnitId);
            }
        }

        @Override
        public void onClicked(String s) {
            AdLog.getSingleton().LogD(TAG, "AdMost RewardedAd Click");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.onRewardedClick(mAdUnitId);
            }
        }

        @Override
        public void onStatusChanged(int statusCode) {
        }
    }

    private class InnerInterstitialAdListener implements AdMostAdListener {
        AdMostInterstitial mInterstitialAd;
        String mAdUnitId;
        BidCallback mBidCallback;
        InterstitialAdCallback mAdCallback;

        public InnerInterstitialAdListener(InterstitialAdCallback adCallback, BidCallback callback) {
            this.mBidCallback = callback;
            this.mAdCallback = adCallback;
        }

        public void setParameters(AdMostInterstitial interstitialAd, String adUnitId) {
            mInterstitialAd = interstitialAd;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onReady(String network, int ecpm) {
            AdLog.getSingleton().LogD(TAG, "AdMost InterstitialAd onInterstitialLoaded PlacementId : " + mAdUnitId);
            mIsAds.put(mAdUnitId, mInterstitialAd);
            if (mAdCallback != null) {
                onBidSuccess(ecpm, null, mAdCallback);
                mAdCallback.onInterstitialAdLoadSuccess();
            }
            if (mBidCallback != null) {
                onBidSuccess(ecpm, null, mBidCallback);
            }
        }

        @Override
        public void onFail(int errorCode) {
            AdLog.getSingleton().LogE(TAG, "AdMost InterstitialAd LoadFailed : " + errorCode);
            mIsAds.remove(mAdUnitId);
            if (mAdCallback != null) {
                onBidFailed("InterstitialAd LoadFailed : " + errorCode, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "AdmostAdapter", errorCode + "");
                mAdCallback.onInterstitialAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed("InterstitialAd LoadFailed : " + errorCode, mBidCallback);
            }
        }

        @Override
        public void onDismiss(String message) {
            AdLog.getSingleton().LogD(TAG, "AdMost InterstitialAd close");
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.onInterstitialDismissed(mAdUnitId);
            }
        }

        @Override
        public void onComplete(String network) {
        }

        @Override
        public void onShown(String network) {
            AdLog.getSingleton().LogD(TAG, "AdMost InterstitialAd Open");
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.onInterstitialOpened(mAdUnitId);
            }
        }

        @Override
        public void onClicked(String s) {
            AdLog.getSingleton().LogD(TAG, "AdMost InterstitialAd Click");
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.onInterstitialClick(mAdUnitId);
            }
        }

        @Override
        public void onStatusChanged(int statusCode) {
        }
    }

    void loadBanner(final String adUnitId, int height, BannerAdCallback adCallback, BidCallback bidCallback) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            String error = " Activity is null";
            AdLog.getSingleton().LogE(TAG, "BannerAd Load Error : " + error);
            if (adCallback != null) {
                onBidFailed(error, adCallback);
                adCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "AdmostAdapter", error));
            }
            if (bidCallback != null) {
                onBidFailed(error, bidCallback);
            }
            return;
        }
        try {
            InnerBannerAdListener listener = new InnerBannerAdListener(adCallback, bidCallback);
            AdMostView bannerAd = new AdMostView(activity, adUnitId, height, listener, null);
            listener.setParameters(adUnitId, bannerAd);
            bannerAd.load();
        } catch (Throwable e) {
            String error = "BannerAd Load Error : " + e.getMessage();
            AdLog.getSingleton().LogE(TAG, error);
            if (adCallback != null) {
                onBidFailed(e.getMessage(), adCallback);
                adCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "AdmostAdapter", error));
            }
            if (bidCallback != null) {
                onBidFailed(e.getMessage(), bidCallback);
            }
        }
    }

    AdmostBannerAdsConfig getBannerAd(String adUnitId) {
        return mBannerAds.get(adUnitId);
    }
    AdmostBannerAdsConfig removeBannerAd(String adUnitId) {
        return mBannerAds.remove(adUnitId);
    }

    void addNativeAd(AdmostBannerAdsConfig config) {
        if (!mReadyNativeAds.contains(config)) {
            mReadyNativeAds.add(config);
        }
    }

    void destroyNativeAd(AdmostBannerAdsConfig config) {
        mReadyNativeAds.remove(config);
        removeNativeAdListener(config);
    }

    boolean isBannerAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId) && !mBannerAds.containsKey(adUnitId)) {
            return false;
        }
        AdmostBannerAdsConfig config = mBannerAds.get(adUnitId);
        AdMostView adMostView = config.getAdMostView();
        return adMostView != null && adMostView.isAdLoaded();
    }

    void destroyBannerAd(String adUnitId) {
        try {
            removeBannerListener(adUnitId);
            if (getBannerAd(adUnitId) != null) {
                AdmostBannerAdsConfig adView = mBannerAds.remove(adUnitId);
                adView.getAdMostView().destroy();
                adView = null;
            }
        } catch (Throwable ignored) {
        }
    }

    void addBannerListener(String adUnitId, AdmostBannerCallback listener) {
        if (!TextUtils.isEmpty(adUnitId) && listener != null) {
            mAdListeners.put(adUnitId, listener);
        }
    }

    void removeBannerListener(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            mAdListeners.remove(adUnitId);
        }
    }

    void addNativeAdListener(AdmostBannerAdsConfig config, AdmostBannerCallback listener) {
        if (config != null && listener != null) {
            mNativeAdListeners.put(config, listener);
        }
    }

    void removeNativeAdListener(AdmostBannerAdsConfig config) {
        if (config != null) {
            mNativeAdListeners.remove(config);
        }
    }

    void loadNative(String pid, String adUnitId, NativeAdCallback adCallback, BidCallback bidCallback) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            String error = " Activity is null";
            AdLog.getSingleton().LogE(TAG, "BannerAd Load Error : " + error);
            if (adCallback != null) {
                onBidFailed(error, adCallback);
                adCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "AdmostAdapter", error));
            }
            if (bidCallback != null) {
                onBidFailed(error, bidCallback);
            }
            return;
        }
        try {
            AdMostViewBinder binder = null;
            String packageName = activity.getPackageName();
            Resources resources = activity.getResources();
            int layoutId = activity.getResources().getIdentifier("admost_custom_native_layout_" + pid, "layout", packageName);
            if (layoutId > 0) {
                AdMostViewBinder.Builder builder = new AdMostViewBinder.Builder(layoutId);
                int titleId = resources.getIdentifier("admost_ad_headline", "id", packageName);
                if (titleId > 0) {
                    builder.titleId(titleId);
                }
                int callToActionId = resources.getIdentifier("admost_ad_call_to_action", "id", packageName);
                if (callToActionId > 0) {
                    builder.callToActionId(callToActionId);
                }
                int textId = resources.getIdentifier("admost_ad_body", "id", packageName);
                if (textId > 0) {
                    builder.textId(textId);
                }
                int attributionId = resources.getIdentifier("admost_ad_attribution", "id", packageName);
                if (attributionId > 0) {
                    builder.attributionId(attributionId);
                }
                int mainImageId = resources.getIdentifier("admost_ad_image", "id", packageName);
                if (mainImageId > 0) {
                    builder.mainImageId(mainImageId);
                }
                int backImageId = resources.getIdentifier("admost_ad_back", "id", packageName);
                if (backImageId > 0) {
                    builder.backImageId(backImageId);
                }
                int privacyIconId = resources.getIdentifier("admost_ad_privacy_icon", "id", packageName);
                if (privacyIconId > 0) {
                    builder.privacyIconId(privacyIconId);
                }
                int iconImageId = resources.getIdentifier("admost_ad_icon", "id", packageName);
                if (iconImageId > 0) {
                    builder.iconImageId(iconImageId);
                }
                binder = builder.build();
            }
            InnerNativeAdListener listener = new InnerNativeAdListener(adCallback, bidCallback);
            AdMostView nativeAd = new AdMostView(activity, adUnitId, listener, binder);
            AdmostBannerAdsConfig nativeConfig = new AdmostBannerAdsConfig();
            nativeConfig.setAdMostView(nativeAd);
            listener.setParameters(adUnitId, nativeAd);
            nativeAd.load();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd Load Error : " + e.getMessage());
            if (adCallback != null) {
                onBidFailed(e.getMessage(), adCallback);
                adCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "AdmostAdapter", e.getMessage()));
            }
            if (bidCallback != null) {
                onBidFailed(e.getMessage(), bidCallback);
            }
        }
    }

    private class InnerBannerAdListener implements AdMostViewListener {

        AdMostView mBannerAdView;
        String mAdUnitId;
        BidCallback mBidCallback;
        BannerAdCallback mAdCallback;

        public InnerBannerAdListener(BannerAdCallback adCallback, BidCallback callback) {
            mAdCallback = adCallback;
            mBidCallback = callback;
        }

        public void setParameters(String adUnitId, AdMostView adView) {
            mBannerAdView = adView;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onReady(String network, int ecpm, View adView) {
            AdLog.getSingleton().LogD(TAG, "AdMost onAdLoaded network: " + network + ", ecpm: " + ecpm + ", adUnit: " + mAdUnitId);
            if (mAdCallback != null) {
                onBidSuccess(ecpm, adView, mAdCallback);
                mAdCallback.onBannerAdLoadSuccess(adView);
            }
            if (mBidCallback != null) {
                AdmostBannerAdsConfig config = new AdmostBannerAdsConfig();
                config.setAdMostView(mBannerAdView);
                config.setAdView(adView);
                mBannerAds.put(mAdUnitId, config);
                onBidSuccess(ecpm, adView, mBidCallback);
            }
        }

        @Override
        public void onFail(int errorCode) {
            mBannerAds.remove(mAdUnitId);
            AdLog.getSingleton().LogE(TAG, "AdMost LoadFailed : " + errorCode);
            if (mAdCallback != null) {
                onBidFailed("AdMost LoadFailed, adUnit: " + mAdUnitId + ", code: " + errorCode, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "AdmostAdapter", errorCode + "");
                mAdCallback.onBannerAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed("AdMost LoadFailed, adUnit: " + mAdUnitId + ", code: " + errorCode, mBidCallback);
            }
        }

        @Override
        public void onClick(String network) {
            AdLog.getSingleton().LogD(TAG, "AdMost onAdClick : " + mAdUnitId);
            AdmostBannerCallback listener = mAdListeners.get(mAdUnitId);
            if (listener != null) {
                listener.onBannerAdClick(mAdUnitId);
            }
        }
    }

    private class InnerNativeAdListener implements AdMostViewListener {

        AdMostView mBannerAdView;
        String mAdUnitId;
        NativeAdCallback mAdCallback;
        BidCallback mBidCallback;
        AdmostBannerAdsConfig config = new AdmostBannerAdsConfig();

        public InnerNativeAdListener(NativeAdCallback adCallback, BidCallback callback) {
            mAdCallback = adCallback;
            mBidCallback = callback;
        }

        public void setParameters(String adUnitId, AdMostView adView) {
            mBannerAdView = adView;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onReady(String network, int ecpm, View adView) {
            AdLog.getSingleton().LogD(TAG, "AdMost onAdLoaded network: " + network + ", ecpm: " + ecpm + ", adUnit: " + mAdUnitId);
            config.setAdMostView(mBannerAdView);
            config.setAdView(adView);
            AdnAdInfo info = new AdnAdInfo();
            info.setAdnNativeAd(config);
            info.setType(MediationInfo.MEDIATION_ID_24);
            info.setView(adView);
            info.setTemplateRender(true);
            mReadyNativeAds.add(config);

            if (mAdCallback != null) {
                onBidSuccess(ecpm, info, mAdCallback);
                mAdCallback.onNativeAdLoadSuccess(info);
            }
            if (mBidCallback != null) {
                onBidSuccess(ecpm, info, mBidCallback);
            }
        }

        @Override
        public void onFail(int errorCode) {
            AdLog.getSingleton().LogE(TAG, "AdMost LoadFailed : " + errorCode);
            if (mAdCallback != null) {
                onBidFailed("AdMost LoadFailed, adUnit: " + mAdUnitId + ", code: " + errorCode, mAdCallback);
                AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "AdmostAdapter", errorCode + "");
                mAdCallback.onNativeAdLoadFailed(adapterError);
            }
            if (mBidCallback != null) {
                onBidFailed("AdMost LoadFailed, adUnit: " + mAdUnitId + ", code: " + errorCode, mBidCallback);
            }
        }

        @Override
        public void onClick(String network) {
            AdLog.getSingleton().LogD(TAG, "AdMost onAdClick : " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onNativeAdAdClicked();
            }
            AdmostBannerCallback listener = mNativeAdListeners.get(config);
            if (listener != null) {
                listener.onBannerAdClick(mAdUnitId);
            }
        }
    }

    private void onBidSuccess(int ecpm, Object object, BidCallback callback) {
        BidResponse bidResponse = new BidResponse();
        double price = ecpm / 100.0;
        bidResponse.setPrice(price);
        bidResponse.setObject(object);
        callback.onBidSuccess(bidResponse);
    }

    private void onBidFailed(String error, BidCallback callback) {
        callback.onBidFailed(error);
    }

    static int getAdSizeHeight(String desc) {
        switch (desc) {
            case MediationUtil.DESC_LEADERBOARD:
                return 90;
            case MediationUtil.DESC_RECTANGLE:
                return 250;
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(MediationUtil.getContext())) {
                    return 90;
                } else {
                    return 50;
                }
            default:
                return 50;
        }
    }

    interface InitListener {
        void initSuccess();

        void initFailed(int code, String error);
    }
}
