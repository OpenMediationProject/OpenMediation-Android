// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mediation.SplashAdCallback;
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
    public boolean isAdNetworkInit() {
        return VungleSingleTon.getInstance().getInitState() == VungleSingleTon.InitState.INIT_SUCCESS;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        Vungle.updateConsentStatus(consent ? Vungle.Consent.OPTED_IN : Vungle.Consent.OPTED_OUT, CONSENT_MESSAGE_VERSION);
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        Vungle.updateCCPAStatus(value ? Vungle.Consent.OPTED_OUT : Vungle.Consent.OPTED_IN);
    }

    private void setCustomParams() {
        if (mUserConsent != null) {
            setGDPRConsent(MediationUtil.getContext(), mUserConsent);
        }
        if (mUSPrivacyLimit != null) {
            setUSPrivacyLimit(MediationUtil.getContext(), mUSPrivacyLimit);
        }
    }

    @Override
    public void initRewardedVideo(final Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        VungleSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, new InitCallback() {
            @Override
            public void onSuccess() {
                setCustomParams();
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }

            @Override
            public void onError(VungleException error) {
                if (callback != null) {
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
                }
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {

            }
        });
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRv(adUnitId, extras, callback);
    }

    private void loadRv(String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            mRvCallback.put(adUnitId, callback);
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
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            mRvCallback.put(adUnitId, callback);
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
    public void initInterstitialAd(final Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        final String error = check();
        if (!TextUtils.isEmpty(error)) {
            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            return;
        }
        VungleSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, new InitCallback() {
            @Override
            public void onSuccess() {
                setCustomParams();
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }

            @Override
            public void onError(VungleException error) {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
                }
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {

            }
        });
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadIs(adUnitId, extras, callback);
    }

    private void loadIs(String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            mIsCallback.put(adUnitId, callback);
            if (Vungle.isInitialized()) {
                if (isInterstitialAdAvailable(adUnitId)) {
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
        String error = check(adUnitId);
        if (TextUtils.isEmpty(error)) {
            mIsCallback.put(adUnitId, callback);
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
    public void initBannerAd(final Activity activity, Map<String, Object> extras, final BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        final String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        VungleSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, new InitCallback() {
            @Override
            public void onSuccess() {
                setCustomParams();
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void onError(VungleException error) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
                }
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {

            }
        });
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
        VungleBannerManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return VungleBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        VungleBannerManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void initSplashAd(Activity activity, Map<String, Object> extras, final SplashAdCallback callback) {
        super.initSplashAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error));
            }
            return;
        }
        VungleSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, new InitCallback() {
            @Override
            public void onSuccess() {
                setCustomParams();
                if (callback != null) {
                    callback.onSplashAdInitSuccess();
                }
            }

            @Override
            public void onError(VungleException error) {
                if (callback != null) {
                    callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, error.getExceptionCode(), error.getLocalizedMessage()));
                }
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {

            }
        });
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        super.loadSplashAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        VungleSplashManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return VungleSplashManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {
        super.showSplashAd(activity, adUnitId, viewGroup, callback);
        VungleSplashManager.getInstance().showAd(adUnitId, callback);
    }

    @Override
    public void destroySplashAd(String adUnitId) {
        super.destroySplashAd(adUnitId);
        VungleSplashManager.getInstance().destroyAd(adUnitId);
    }

    @Override
    public void creativeId(String creativeId) {

    }

    @Override
    public void onAdStart(String id) {
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
    }

    @Override
    public void onAdEnd(String id) {
        if (mRvCallback.containsKey(id)) {
            RewardedVideoCallback callback = mRvCallback.get(id);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdClosed();
            }
        } else {
            InterstitialAdCallback callback = mIsCallback.get(id);
            if (callback != null) {
                callback.onInterstitialAdClosed();
            }
        }
    }

    @Override
    public void onAdClick(String id) {
        if (mRvCallback.containsKey(id)) {
            RewardedVideoCallback callback = mRvCallback.get(id);
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        } else {
            InterstitialAdCallback callback = mIsCallback.get(id);
            if (callback != null) {
                callback.onInterstitialAdClicked();
            }
        }
    }

    @Override
    public void onAdRewarded(String id) {
        if (mRvCallback.containsKey(id)) {
            RewardedVideoCallback callback = mRvCallback.get(id);
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        }
    }

    @Override
    public void onAdLeftApplication(String id) {

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

    @Override
    public void onAdViewed(String id) {
    }

    private class LoadCallback implements LoadAdCallback {

        @Override
        public void onAdLoad(String id) {
            if (mRvCallback.containsKey(id)) {
                RewardedVideoCallback callback = mRvCallback.get(id);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                InterstitialAdCallback callback = mIsCallback.get(id);
                if (callback != null) {
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

}
