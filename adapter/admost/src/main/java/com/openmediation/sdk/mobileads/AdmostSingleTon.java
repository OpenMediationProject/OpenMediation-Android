// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    private final List<AdmostBannerAdsConfig> mLoadedNativeAds;
    private final ConcurrentHashMap<String, AdmostBannerCallback> mAdListeners;
    private final ConcurrentHashMap<AdmostBannerAdsConfig, AdmostBannerCallback> mNativeAdListeners;

    private final ConcurrentMap<String, AdmostBidCallback> mBidCallbacks;
    private final ConcurrentMap<String, String> mBidError;

    private AdmostVideoCallback mVideoAdCallback;
    private AdmostInterstitialCallback mInterstitialAdCallback;

    private final List<InitListener> mListeners = new CopyOnWriteArrayList<>();

    private static class Holder {
        private static final AdmostSingleTon INSTANCE = new AdmostSingleTon();
    }

    private AdmostSingleTon() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBidCallbacks = new ConcurrentHashMap<>();
        mBidError = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mReadyNativeAds = new CopyOnWriteArrayList<>();
        mLoadedNativeAds = new CopyOnWriteArrayList<>();
        mAdListeners = new ConcurrentHashMap<>();
        mNativeAdListeners = new ConcurrentHashMap<>();
    }

    public static AdmostSingleTon getInstance() {
        return Holder.INSTANCE;
    }

    public void onResume() {
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
            for (AdmostBannerAdsConfig config : mLoadedNativeAds) {
                if (config != null && config.getAdMostView() != null) {
                    config.getAdMostView().resume();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void onPause() {
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
            for (AdmostBannerAdsConfig config : mLoadedNativeAds) {
                if (config != null && config.getAdMostView() != null) {
                    config.getAdMostView().pause();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isInit() {
        return AdMost.getInstance().isInitCompleted();
    }

    public synchronized void init(final Activity activity, final String appKey, InitListener listener) {
        if (activity == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                listener.initFailed(0, "Context is null or AppKey is empty");
            }
            AdLog.getSingleton().LogE(TAG, "Init Failed: Context is null or AppKey is empty!");
            return;
        }
        if (isInit()) {
            if (listener != null) {
                listener.initSuccess();
            }
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
                AdLog.getSingleton().LogD(TAG, "AdMost SDK initialized failed");
                for (InitListener listener : mListeners) {
                    if (listener != null) {
                        listener.initFailed(err, "AdMost init failed");
                    }
                }
                mListeners.clear();
            }
        });
    }

    void addBidCallback(String placementId, AdmostBidCallback callback) {
        if (!TextUtils.isEmpty(placementId) && callback != null) {
            mBidCallbacks.put(placementId, callback);
        }
    }

    void removeBidCallback(String placementId) {
        if (!TextUtils.isEmpty(placementId)) {
            mBidCallbacks.remove(placementId);
        }
    }

    String getError(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            return mBidError.get(adUnitId);
        }
        return "No Fill";
    }

    void setVideoAdCallback(AdmostVideoCallback callback) {
        mVideoAdCallback = callback;
    }

    void setInterstitialAdCallback(AdmostInterstitialCallback callback) {
        mInterstitialAdCallback = callback;
    }

    void loadRewardedVideo(final String adUnitId) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd Load Error : Activity is null");
            bidFailed(adUnitId, "Activity is null");
            return;
        }
        try {
            InnerRewardedAdListener listener = new InnerRewardedAdListener();
            AdMostInterstitial interstitial = mRvAds.get(adUnitId);
            if (interstitial == null) {
                interstitial = new AdMostInterstitial(activity, adUnitId, listener);
                mRvAds.put(adUnitId, interstitial);
            }
            listener.setParameters(interstitial, adUnitId);
            interstitial.refreshAd(false);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "RewardedVideoAd Load Error : " + e.getMessage());
            bidFailed(adUnitId, e.getMessage());
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

    void loadInterstitial(String adUnitId) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : Activity is null");
            bidFailed(adUnitId, "Activity is null");
            return;
        }
        try {
            InnerInterstitialAdListener listener = new InnerInterstitialAdListener();
            AdMostInterstitial interstitial = mIsAds.get(adUnitId);
            if (interstitial == null) {
                interstitial = new AdMostInterstitial(activity, adUnitId, listener);
                mIsAds.put(adUnitId, interstitial);
            }
            listener.setParameters(interstitial, adUnitId);
            interstitial.refreshAd(false);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "InterstitialAd Load Error : " + e.getMessage());
            bidFailed(adUnitId, e.getMessage());
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

        public InnerRewardedAdListener() {
        }

        public void setParameters(AdMostInterstitial rewarded, String adUnitId) {
            mRewarded = rewarded;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onReady(String network, int ecpm) {
            AdLog.getSingleton().LogD(TAG, "AdMost onRewardedLoaded PlacementId : " + mAdUnitId);
            mRvAds.put(mAdUnitId, mRewarded);
            bidSuccess(mAdUnitId, network, ecpm);
        }

        @Override
        public void onFail(int errorCode) {
            AdLog.getSingleton().LogE(TAG, "AdMost RewardedAd LoadFailed : " + errorCode);
            mRvAds.remove(mAdUnitId);
            bidFailed(mAdUnitId, "RewardedAd LoadFailed : " + errorCode);
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
            if (mVideoAdCallback == null) {
                return;
            }
            AdLog.getSingleton().LogD(TAG, "AdMost RewardedAd Opened");
            mVideoAdCallback.onRewardedOpened(mAdUnitId);
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
            // This callback will be triggered only when frequency cap ended.
            // status code
            // 1 - AdMost.AD_STATUS_CHANGE_FREQ_CAP_ENDED
        }
    }

    private class InnerInterstitialAdListener implements AdMostAdListener {
        AdMostInterstitial mInterstitialAd;
        String mAdUnitId;

        public InnerInterstitialAdListener() {
        }

        public void setParameters(AdMostInterstitial interstitialAd, String adUnitId) {
            mInterstitialAd = interstitialAd;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onReady(String network, int ecpm) {
            AdLog.getSingleton().LogD(TAG, "AdMost InterstitialAd onInterstitialLoaded PlacementId : " + mAdUnitId);
            mIsAds.put(mAdUnitId, mInterstitialAd);
            bidSuccess(mAdUnitId, network, ecpm);
        }

        @Override
        public void onFail(int errorCode) {
            AdLog.getSingleton().LogE(TAG, "AdMost InterstitialAd LoadFailed : " + errorCode);
            mIsAds.remove(mAdUnitId);
            bidFailed(mAdUnitId, "InterstitialAd LoadFailed : " + errorCode);
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
            // This callback will be triggered only when frequency cap ended.
            // status code
            // 1 - AdMost.AD_STATUS_CHANGE_FREQ_CAP_ENDED
        }
    }

    void loadBanner(final String adUnitId) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            AdLog.getSingleton().LogE(TAG, "BannerAd Load Error : Activity is null");
            bidFailed(adUnitId, "Activity is null");
            return;
        }
        try {
            InnerBannerAdListener listener = new InnerBannerAdListener();
            AdMostView bannerAd = new AdMostView(activity, adUnitId, listener, null);
            listener.setParameters(adUnitId, bannerAd);
            bannerAd.load();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "BannerAd Load Error : " + e.getMessage());
            bidFailed(adUnitId, e.getMessage());
        }
    }

    AdmostBannerAdsConfig getBannerAd(String adUnitId) {
        return mBannerAds.get(adUnitId);
    }

    AdmostBannerAdsConfig getNativeAd() {
        if (!mReadyNativeAds.isEmpty()) {
            AdmostBannerAdsConfig remove = mReadyNativeAds.remove(0);
            mLoadedNativeAds.add(remove);
            return remove;
        }
        return null;
    }

    void destroyNativeAd(AdmostBannerAdsConfig config) {
        mReadyNativeAds.remove(config);
        mLoadedNativeAds.remove(config);
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
        } catch (Exception ignored) {
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

    void loadNative(String pid, String adUnitId) {
        Activity activity = MediationUtil.getActivity();
        if (activity == null || activity.isFinishing()) {
            AdLog.getSingleton().LogE(TAG, "NativeAd Load Error : Activity is null");
            bidFailed(adUnitId, "Activity is null");
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
//            AdMostViewBinder.Builder builder = new AdMostViewBinder.Builder(R.layout.admost_custom_native_layout);
//            AdMostViewBinder build = builder.iconImageId(R.id.ad_app_icon)
//                    .titleId(R.id.ad_headline)
//                    .callToActionId(R.id.ad_call_to_action)
//                    .textId(R.id.ad_body)
//                    .attributionId(R.id.ad_attribution)
//                    .mainImageId(R.id.ad_image)
//                    .backImageId(R.id.ad_back)
//                    .privacyIconId(R.id.ad_privacy_icon)
//                    .iconImageId(R.id.ad_app_icon)
//                    .build();
            InnerNativeAdListener listener = new InnerNativeAdListener();
            AdMostView nativeAd = new AdMostView(activity, adUnitId, listener, binder);
            AdmostBannerAdsConfig nativeConfig = new AdmostBannerAdsConfig();
            nativeConfig.setAdMostView(nativeAd);
            listener.setParameters(adUnitId, nativeAd);
            nativeAd.load();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd Load Error : " + e.getMessage());
            bidFailed(adUnitId, e.getMessage());
        }
    }

    private class InnerBannerAdListener implements AdMostViewListener {

        AdMostView mBannerAdView;
        String mAdUnitId;

        public InnerBannerAdListener() {
        }

        public void setParameters(String adUnitId, AdMostView adView) {
            mBannerAdView = adView;
            mAdUnitId = adUnitId;
        }

        @Override
        public void onReady(String network, int ecpm, View adView) {
            AdLog.getSingleton().LogD(TAG, "AdMost onAdLoaded network: " + network + ", ecpm: " + ecpm + ", adUnit: " + mAdUnitId);
            AdmostBannerAdsConfig config = new AdmostBannerAdsConfig();
            config.setAdMostView(mBannerAdView);
            config.setAdView(adView);
            mBannerAds.put(mAdUnitId, config);
            bidSuccess(mAdUnitId, network, ecpm);
        }

        @Override
        public void onFail(int errorCode) {
            mBannerAds.remove(mAdUnitId);
            AdLog.getSingleton().LogE(TAG, "AdMost LoadFailed : " + errorCode);
            bidFailed(mAdUnitId, "AdMost LoadFailed, adUnit: " + mAdUnitId + ", code: " + errorCode);
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
        AdmostBannerAdsConfig config = new AdmostBannerAdsConfig();
        public InnerNativeAdListener() {
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
            mReadyNativeAds.add(config);
            bidSuccess(mAdUnitId, network, ecpm);
        }

        @Override
        public void onFail(int errorCode) {
            AdLog.getSingleton().LogE(TAG, "AdMost LoadFailed : " + errorCode);
            bidFailed(mAdUnitId, "AdMost LoadFailed, adUnit: " + mAdUnitId + ", code: " + errorCode);
        }

        @Override
        public void onClick(String network) {
            AdLog.getSingleton().LogD(TAG, "AdMost onAdClick : " + mAdUnitId);
            AdmostBannerCallback listener = mNativeAdListeners.get(config);
            if (listener != null) {
                listener.onBannerAdClick(mAdUnitId);
            }
        }
    }

    private void bidSuccess(String adUnitId, String network, int ecpm) {
        if (mBidCallbacks.containsKey(adUnitId)) {
            mBidCallbacks.get(adUnitId).onBidSuccess(adUnitId, network, ecpm);
        }
        mBidError.remove(adUnitId);
    }

    private void bidFailed(String adUnitId, String error) {
        if (mBidCallbacks.containsKey(adUnitId)) {
            mBidCallbacks.get(adUnitId).onBidFailed(adUnitId, error);
        }
        mBidError.put(adUnitId, error);
    }

    interface InitListener {
        void initSuccess();

        void initFailed(int code, String error);
    }
}
