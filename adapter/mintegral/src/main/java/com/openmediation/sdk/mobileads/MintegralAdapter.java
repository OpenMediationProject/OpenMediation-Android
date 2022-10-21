// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.mbridge.msdk.MBridgeConstans;
import com.mbridge.msdk.MBridgeSDK;
import com.mbridge.msdk.mbbid.out.BidManager;
import com.mbridge.msdk.newinterstitial.out.MBBidInterstitialVideoHandler;
import com.mbridge.msdk.newinterstitial.out.MBNewInterstitialHandler;
import com.mbridge.msdk.newinterstitial.out.NewInterstitialListener;
import com.mbridge.msdk.out.MBBidRewardVideoHandler;
import com.mbridge.msdk.out.MBConfiguration;
import com.mbridge.msdk.out.MBRewardVideoHandler;
import com.mbridge.msdk.out.MBridgeIds;
import com.mbridge.msdk.out.MBridgeSDKFactory;
import com.mbridge.msdk.out.RewardInfo;
import com.mbridge.msdk.out.RewardVideoListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.mobileads.mintegral.BuildConfig;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralAdapter extends CustomAdsAdapter {
    private static final String CLAZZ = "com.mbridge.msdk.mbbid.out.BidManager";
    public static final String PAY_LOAD = "pay_load";
    private ConcurrentHashMap<String, MBNewInterstitialHandler> mInterstitialAds;
    private ConcurrentHashMap<String, MBRewardVideoHandler> mRvAds;
    private ConcurrentHashMap<String, MBBidInterstitialVideoHandler> mInterstitialBidAds;
    private ConcurrentHashMap<String, MBBidRewardVideoHandler> mRvBidAds;

    public MintegralAdapter() {
        mInterstitialAds = new ConcurrentHashMap<>();
        mRvAds = new ConcurrentHashMap<>();
        mRvBidAds = new ConcurrentHashMap<>();
        mInterstitialBidAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return MBConfiguration.SDK_VERSION;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_14;
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        MintegralSplashManager.getInstance().onPause();
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        MintegralSplashManager.getInstance().onResume();
    }

    @Override
    public boolean isS2S() {
        return true;
    }

    @Override
    public boolean needPayload() {
        return true;
    }

    @Override
    public String getBiddingToken(Context context) {
        try {
            Class clazz = Class.forName(CLAZZ);
            return BidManager.getBuyerUid(MediationUtil.getContext());
        } catch (Throwable e) {
            AdLog.getSingleton().LogE("Mintegral getBuyerUid Error: " + e.getMessage());
        }
        return "";
    }

    @Override
    public void setGDPRConsent(final Context context, final boolean consent) {
        super.setGDPRConsent(context, consent);
        if (context != null) {
            MediationUtil.runOnUiThread(() -> {
                try {
                    MBridgeSDK sdk = MBridgeSDKFactory.getMBridgeSDK();
                    int consentStatus = consent ? MBridgeConstans.IS_SWITCH_ON : MBridgeConstans.IS_SWITCH_OFF;
                    sdk.setConsentStatus(context, consentStatus);
                } catch (Throwable ignored) {
                }
            });
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        MediationUtil.runOnUiThread(() -> {
            try {
                MBridgeSDK sdk = MBridgeSDKFactory.getMBridgeSDK();
                sdk.setDoNotTrackStatus(value);
            } catch (Throwable ignored) {
            }
        });
    }

    @Override
    public void initRewardedVideo(final Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        final String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        initSDK(new MintegralSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, msg));
                }
            }
        });
    }

    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        realLoadRvAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    private void realLoadRvAd(final Context context, final String adUnitId, final Map<String, Object> extras, final RewardedVideoCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String payload = "";
                    if (extras != null && extras.containsKey(PAY_LOAD)) {
                        payload = String.valueOf(extras.get(PAY_LOAD));
                    }
                    if (TextUtils.isEmpty(payload)) {
                        MintegralSingleTon.getInstance().removeBidAdUnit(adUnitId);
                        loadRvAd(context, adUnitId, callback);
                    } else {
                        MintegralSingleTon.getInstance().putBidAdUnit(adUnitId, payload);
                        loadRvAdWithBid(context, adUnitId, payload, callback);
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    private void loadRvAd(Context context, String adUnitId, RewardedVideoCallback callback) {
        MBRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        if (rewardVideoHandler == null) {
            rewardVideoHandler = new MBRewardVideoHandler(context, "", adUnitId);
            mRvAds.put(adUnitId, rewardVideoHandler);
            rewardVideoHandler.setRewardVideoListener(new MtgRvAdListener(callback));
        }
        if (rewardVideoHandler.isReady()) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            rewardVideoHandler.load();
        }
    }

    private void loadRvAdWithBid(Context context, String adUnitId, String payload, RewardedVideoCallback callback) {
        MBBidRewardVideoHandler rewardVideoHandler = mRvBidAds.get(adUnitId);
        if (rewardVideoHandler == null) {
            rewardVideoHandler = new MBBidRewardVideoHandler(context, "", adUnitId);
            mRvBidAds.put(adUnitId, rewardVideoHandler);
            rewardVideoHandler.setRewardVideoListener(new MtgRvAdListener(callback));
        }
        if (rewardVideoHandler.isBidReady()) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            rewardVideoHandler.loadFromBid(payload);
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        if (MintegralSingleTon.getInstance().containsKeyBidAdUnit(adUnitId)) {
            MBBidRewardVideoHandler videoHandler = mRvBidAds.get(adUnitId);
            return videoHandler != null && videoHandler.isBidReady();
        }
        MBRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        return rewardVideoHandler != null && rewardVideoHandler.isReady();
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        try {
            showRvAd(adUnitId, callback);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private void showRvAd(String adUnitId, RewardedVideoCallback callback) {
        if (MintegralSingleTon.getInstance().containsKeyBidAdUnit(adUnitId)) {
            MBBidRewardVideoHandler rewardVideoHandler = mRvBidAds.get(adUnitId);
            if (rewardVideoHandler == null || !rewardVideoHandler.isBidReady()) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MTGRewardedVideo Ad Not Ready"));
                }
                return;
            }
            rewardVideoHandler.showFromBid("1");
        } else {
            MBRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
            if (rewardVideoHandler == null || !rewardVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MTGRewardedVideo Ad Not Ready"));
                }
                return;
            }
            rewardVideoHandler.show("1");
        }
    }

    @Override
    public void initInterstitialAd(final Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        initSDK(new MintegralSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, msg));
                }
            }
        });
    }

    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        realLoadIsAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    private void realLoadIsAd(final Context context, final String adUnitId, final Map<String, Object> extras, final InterstitialAdCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String payload = "";
                    if (extras != null && extras.containsKey(PAY_LOAD)) {
                        payload = String.valueOf(extras.get(PAY_LOAD));
                    }
                    if (TextUtils.isEmpty(payload)) {
                        MintegralSingleTon.getInstance().removeBidAdUnit(adUnitId);
                        loadIsAd(context, adUnitId, callback);
                    } else {
                        MintegralSingleTon.getInstance().putBidAdUnit(adUnitId, payload);
                        loadIsAdWithBid(context, adUnitId, payload, callback);
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    private void loadIsAd(Context context, String adUnitId, InterstitialAdCallback callback) {
        MBNewInterstitialHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null) {
            mtgInterstitialVideoHandler = new MBNewInterstitialHandler(context, "", adUnitId);
            mInterstitialAds.put(adUnitId, mtgInterstitialVideoHandler);
            mtgInterstitialVideoHandler.setInterstitialVideoListener(new MtgInterstitialAdListener(callback));
        }
        if (mtgInterstitialVideoHandler.isReady()) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            mtgInterstitialVideoHandler.load();
        }
    }

    private void loadIsAdWithBid(Context context, String adUnitId, String payload, InterstitialAdCallback callback) {
        MBBidInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null) {
            mtgInterstitialVideoHandler = new MBBidInterstitialVideoHandler(context, "", adUnitId);
            mInterstitialBidAds.put(adUnitId, mtgInterstitialVideoHandler);
            mtgInterstitialVideoHandler.setInterstitialVideoListener(new MtgInterstitialAdListener(callback));
        }
        if (mtgInterstitialVideoHandler.isBidReady()) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            mtgInterstitialVideoHandler.loadFromBid(payload);
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        if (MintegralSingleTon.getInstance().containsKeyBidAdUnit(adUnitId)) {
            MBBidInterstitialVideoHandler bidInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
            return bidInterstitialVideoHandler != null && bidInterstitialVideoHandler.isBidReady();
        }
        MBNewInterstitialHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        return mtgInterstitialVideoHandler != null && mtgInterstitialVideoHandler.isReady();
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        try {
            showIsAd(adUnitId, callback);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private void showIsAd(String adUnitId, InterstitialAdCallback callback) {
        if (MintegralSingleTon.getInstance().containsKeyBidAdUnit(adUnitId)) {
            MBBidInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
            if (mtgInterstitialVideoHandler == null || !mtgInterstitialVideoHandler.isBidReady()) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Mintegral Interstitial Ad Not Ready"));
                }
                return;
            }
            mtgInterstitialVideoHandler.showFromBid();
        } else {
            MBNewInterstitialHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
            if (mtgInterstitialVideoHandler == null || !mtgInterstitialVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Mintegral Interstitial Ad Not Ready"));
                }
                return;
            }
            mtgInterstitialVideoHandler.show();
        }
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        MintegralBannerManager.getInstance().initAd(MediationUtil.getContext(), extras, callback, mUserConsent, mAgeRestricted);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        MintegralBannerManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return MintegralBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        MintegralBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initSplashAd(Activity activity, Map<String, Object> extras, SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        MintegralSplashManager.getInstance().initAd(MediationUtil.getContext(), extras, callback, mUserConsent, mAgeRestricted);
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        super.loadSplashAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        MintegralSplashManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return MintegralSplashManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        MintegralSplashManager.getInstance().showAd(adUnitId, viewGroup, callback);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        MintegralSplashManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
            return;
        }
        MintegralNativeManager.getInstance().initAd(MediationUtil.getContext(), extras, callback, mUserConsent, mAgeRestricted);
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
            return;
        }
        MintegralNativeManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adnAdInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adnAdInfo, callback);
        MintegralNativeManager.getInstance().registerNativeView(adUnitId, adView, adnAdInfo, callback);
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adnAdInfo) {
        super.destroyNativeAd(adUnitId, adnAdInfo);
        MintegralNativeManager.getInstance().destroyAd(adUnitId, adnAdInfo);
    }

    private void initSDK(MintegralSingleTon.InitCallback listener) {
        MintegralSingleTon.getInstance().initSDK(MediationUtil.getContext(), mAppKey, listener, mUserConsent, mAgeRestricted);
    }

    private static class MtgInterstitialAdListener implements NewInterstitialListener {

        private InterstitialAdCallback mCallback;

        MtgInterstitialAdListener(InterstitialAdCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onLoadCampaignSuccess(MBridgeIds mBridgeIds) {
        }

        @Override
        public void onResourceLoadSuccess(MBridgeIds mBridgeIds) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onResourceLoadFail(MBridgeIds mBridgeIds, String msg) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "MintegralAdapter", msg));
            }
        }

        @Override
        public void onAdShow(MBridgeIds mBridgeIds) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onAdClose(MBridgeIds mBridgeIds, RewardInfo rewardInfo) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onShowFail(MBridgeIds mBridgeIds, String msg) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "MintegralAdapter", msg));
            }
        }

        @Override
        public void onAdClicked(MBridgeIds mBridgeIds) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClicked();
            }
        }

        @Override
        public void onVideoComplete(MBridgeIds mBridgeIds) {
        }

        @Override
        public void onAdCloseWithNIReward(MBridgeIds mBridgeIds, RewardInfo rewardInfo) {
        }

        @Override
        public void onEndcardShow(MBridgeIds mBridgeIds) {
        }
    }

    private static class MtgRvAdListener implements RewardVideoListener {

        private RewardedVideoCallback mRvCallback;

        MtgRvAdListener(RewardedVideoCallback callback) {
            mRvCallback = callback;
        }

        @Override
        public void onVideoLoadSuccess(MBridgeIds mBridgeIds) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onLoadSuccess(MBridgeIds mBridgeIds) {

        }

        @Override
        public void onVideoLoadFail(MBridgeIds mBridgeIds, String msg) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "MintegralAdapter", msg));
            }
        }

        @Override
        public void onAdShow(MBridgeIds mBridgeIds) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdShowSuccess();
                mRvCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onAdClose(MBridgeIds mBridgeIds, RewardInfo rewardInfo) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onShowFail(MBridgeIds mBridgeIds, String msg) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "MintegralAdapter", msg));
            }
        }

        @Override
        public void onVideoAdClicked(MBridgeIds mBridgeIds) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onVideoComplete(MBridgeIds mBridgeIds) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdEnded();
                mRvCallback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onEndcardShow(MBridgeIds mBridgeIds) {

        }
    }
}
