/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.inmobi.sdk.InMobiSdk;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class InMobiAdapter extends CustomAdsAdapter {
    @Override
    public String getMediationVersion() {
        return InMobiSdk.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.inmobi.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_26;
    }

    @Override
    public boolean isAdNetworkInit() {
        return InMobiSingleTon.getInstance().isInit();
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        try {
            JSONObject consentObject = new JSONObject();
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, consent);
            InMobiSdk.updateGDPRConsent(consentObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUserGender(Context context, String gender) {
        super.setUserGender(context, gender);
        if (gender.equalsIgnoreCase("female")) {
            InMobiSdk.setGender(InMobiSdk.Gender.FEMALE);
        } else if (gender.equalsIgnoreCase("male")) {
            InMobiSdk.setGender(InMobiSdk.Gender.MALE);
        }
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        InMobiSdk.setAge(age);
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            InMobiBannerManager.getInstance().init(activity, mAppKey, callback);
        } else {
            callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
        }
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        InMobiBannerManager.getInstance().loadAd(adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return InMobiSingleTon.getInstance().isBannerAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        InMobiSingleTon.getInstance().destroyBanner(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }

        InMobiSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, new InMobiInitCallback() {
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
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                }
            }
        });
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
        } else {
            if (callback != null) {
                String error = InMobiSingleTon.getInstance().getError(adUnitId);
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return InMobiSingleTon.getInstance().isInterstitialAvailable(adUnitId);
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (isInterstitialAdAvailable(adUnitId)) {
            InMobiSingleTon.getInstance().setInterstitialAdCallback(new InnerIsCallback(callback));
            InMobiSingleTon.getInstance().showInterstitial(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "InMobi interstitial is not ready"));
            }
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }

        InMobiSingleTon.getInstance().init(MediationUtil.getContext(), mAppKey, new InMobiInitCallback() {
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
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                }
            }
        });
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
        if (callback != null) {
            String error = InMobiSingleTon.getInstance().getError(adUnitId);
            if (TextUtils.isEmpty(error)) {
                error = "No Fill";
            }
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return InMobiSingleTon.getInstance().isRewardedVideoAvailable(adUnitId);
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
        if (isRewardedVideoAvailable(adUnitId)) {
            InMobiSingleTon.getInstance().setVideoAdCallback(new InnerRvCallback(callback));
            InMobiSingleTon.getInstance().showRewardedVideo(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
            }
        }
    }

    private class InnerIsCallback implements InMobiInterstitialCallback {

        private InterstitialAdCallback mCallback;

        private InnerIsCallback(InterstitialAdCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onAdDisplayed(String placementId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onAdDisplayFailed(String placementId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Show Error"));
            }
        }

        @Override
        public void onAdClicked(String placementId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdDismissed(String placementId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClosed();
            }
        }
    }
    private class InnerRvCallback implements InMobiVideoCallback {

        private RewardedVideoCallback mCallback;

        private InnerRvCallback(RewardedVideoCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onAdDisplayed(String placementId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowSuccess();
            }
        }

        @Override
        public void onAdDisplayFailed(String placementId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Show Error"));
            }
        }

        @Override
        public void onAdClicked(String placementId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onAdDismissed(String placementId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onAdRewarded(String placementId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdRewarded();
            }
        }
    }
}
