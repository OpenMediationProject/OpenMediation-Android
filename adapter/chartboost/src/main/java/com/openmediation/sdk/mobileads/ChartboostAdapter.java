// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.privacy.model.CCPA;
import com.chartboost.sdk.privacy.model.COPPA;
import com.chartboost.sdk.privacy.model.DataUseConsent;
import com.chartboost.sdk.privacy.model.GDPR;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.chartboost.BuildConfig;

import java.util.Map;

public class ChartboostAdapter extends CustomAdsAdapter {

    public ChartboostAdapter() {
    }

    private void initSDK(ChartboostSingleTon.InitListener initListener) {
        ChartboostSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, initListener);
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

    // Needs to be set before SDK init
    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (context != null) {
            try {
                DataUseConsent dataUseConsent;
                if (consent) {
                    dataUseConsent = new GDPR(GDPR.GDPR_CONSENT.BEHAVIORAL);
                } else {
                    dataUseConsent = new GDPR(GDPR.GDPR_CONSENT.NON_BEHAVIORAL);
                }
                Chartboost.addDataUseConsent(context, dataUseConsent);
            } catch (Throwable ignored) {
            }
        }
    }

    // Needs to be set before SDK init
    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        if (context != null) {
            try {
                DataUseConsent dataUseConsent;
                if (value) {
                    dataUseConsent = new CCPA(CCPA.CCPA_CONSENT.OPT_OUT_SALE);
                } else {
                    dataUseConsent = new CCPA(CCPA.CCPA_CONSENT.OPT_IN_SALE);
                }
                Chartboost.addDataUseConsent(context, dataUseConsent);
            } catch (Throwable ignored) {
            }
        }
    }

    // Needs to be set before SDK init
    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        if (context != null) {
            try {
                DataUseConsent dataUseConsent = new COPPA(restricted);
                Chartboost.addDataUseConsent(context, dataUseConsent);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String checkError = check();
        if (TextUtils.isEmpty(checkError)) {
            initSDK(new ChartboostSingleTon.InitListener() {
                @Override
                public void initSuccess() {
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                }

                @Override
                public void initFailed(String error) {
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String checkError = check(adUnitId);
        if (!TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
            return;
        }
        if (isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
            return;
        }
        ChartboostSingleTon.getInstance().loadRewardedVideo(adUnitId, callback);
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
            if (isRewardedVideoAvailable(adUnitId)) {
                ChartboostSingleTon.getInstance().showRewardedVideo(adUnitId);
            } else {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return ChartboostSingleTon.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String checkError = check();
        if (TextUtils.isEmpty(checkError)) {
            initSDK(new ChartboostSingleTon.InitListener() {
                @Override
                public void initSuccess() {
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                }

                @Override
                public void initFailed(String error) {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String checkError = check(adUnitId);
        if (!TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
            return;
        }
        ChartboostSingleTon.getInstance().loadInterstitialAd(adUnitId, callback);
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
            if (isInterstitialAdAvailable(adUnitId)) {
                ChartboostSingleTon.getInstance().showInterstitialAd(adUnitId);
            } else {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Not Ready"));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return ChartboostSingleTon.getInstance().isInterstitialAdReady(adUnitId);
    }

}
