// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.adcolony.sdk.AdColonyZone;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.adcolony.BuildConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdColonyAdapter extends CustomAdsAdapter implements AdColonyRewardListener {
    private boolean mDidInited = false;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallback;
    private final ConcurrentHashMap<String, AdColonyInterstitial> mAdColonyAds;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsCallback;
    private final ConcurrentHashMap<String, AdColonyInterstitial> mIsAdColonyAds;
    private final AdColonyAppOptions mAdColonyOptions;

    public AdColonyAdapter() {
        mRvCallback = new ConcurrentHashMap<>();
        mAdColonyAds = new ConcurrentHashMap<>();
        mIsCallback = new ConcurrentHashMap<>();
        mIsAdColonyAds = new ConcurrentHashMap<>();
        mAdColonyOptions = new AdColonyAppOptions();
    }

    @Override
    public String getMediationVersion() {
        return AdColony.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_7;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        try {
            mAdColonyOptions.setPrivacyConsentString(AdColonyAppOptions.GDPR, consent ? "1" : "0");
            mAdColonyOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.GDPR, true);
            AdColony.setAppOptions(mAdColonyOptions);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        try {
            mAdColonyOptions.setPrivacyConsentString(AdColonyAppOptions.CCPA, value ? "1" : "0");
            mAdColonyOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.CCPA, true);
            AdColony.setAppOptions(mAdColonyOptions);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initAdColony(activity, dataMap);
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            AdColonyInterstitial rvAd = mAdColonyAds.get(adUnitId);
            mRvCallback.put(adUnitId, callback);
            if (rvAd == null || rvAd.isExpired()) {
                AdColony.setRewardListener(this);
                AdColony.requestInterstitial(adUnitId, new AdColonyAdListener());
            } else if (!rvAd.isExpired()) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (isRewardedVideoAvailable(adUnitId)) {
            AdColonyInterstitial ad = mAdColonyAds.get(adUnitId);
            if (ad != null) {
                ad.show();
            } else {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, AdColonyAdapter.this.mAdapterName, "AdColony ad not ready"));
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, AdColonyAdapter.this.mAdapterName, "AdColony ad not ready"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        try {
            if (TextUtils.isEmpty(adUnitId)) {
                return false;
            }
            AdColonyInterstitial ad = mAdColonyAds.get(adUnitId);
            return ad != null && !ad.isExpired();
        } catch (Exception ex) {
            return false;
        }
    }

    private synchronized void initAdColony(Activity activity, Map<String, Object> dataMap) {
        if (!mDidInited) {
            List<String> idList = null;
            if (dataMap.get("zoneIds") instanceof List) {
                idList = (List<String>) dataMap.get("zoneIds");
            }
            String[] zoneIds;
            if (idList != null) {
                zoneIds = idList.toArray(new String[idList.size()]);
                AdColony.configure(activity.getApplication(), mAdColonyOptions, mAppKey, zoneIds);
            } else {
                AdColony.configure(activity.getApplication(), mAdColonyOptions, mAppKey);
            }
            mDidInited = true;
        }
    }

    @Override
    public void onReward(AdColonyReward adColonyReward) {
        RewardedVideoCallback callback = mRvCallback.get(adColonyReward.getZoneID());
        if (adColonyReward.success() && callback != null) {
            callback.onRewardedVideoAdRewarded();
        }
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initAdColony(activity, dataMap);
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            mIsCallback.put(adUnitId, callback);
            if (isInterstitialAdAvailable(adUnitId)) {
                callback.onInterstitialAdLoadSuccess();
            } else {
                AdColony.requestInterstitial(adUnitId, new AdIsColonyAdListener());
            }
        } else {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (isInterstitialAdAvailable(adUnitId)) {
            AdColonyInterstitial ad = mIsAdColonyAds.get(adUnitId);
            if (ad != null) {
                ad.show();
            } else {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, AdColonyAdapter.this.mAdapterName, "AdColony ad not ready"));
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, AdColonyAdapter.this.mAdapterName, "AdColony ad not ready"));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        AdColonyInterstitial ad = mIsAdColonyAds.get(adUnitId);
        return ad != null && !ad.isExpired();
    }

    private class AdColonyAdListener extends AdColonyInterstitialListener {
        @Override
        public void onRequestFilled(AdColonyInterstitial ad) {
            mAdColonyAds.put(ad.getZoneID(), ad);
            RewardedVideoCallback callback = mRvCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onRequestNotFilled(AdColonyZone zone) {
            RewardedVideoCallback callback = mRvCallback.get(zone.getZoneID());
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Request Not Filled"));
            }
        }

        @Override
        public void onOpened(AdColonyInterstitial ad) {
            RewardedVideoCallback callback = mRvCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onClosed(AdColonyInterstitial ad) {
            RewardedVideoCallback callback = mRvCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onExpiring(AdColonyInterstitial ad) {
            //re-requests if expired
            AdColony.requestInterstitial(ad.getZoneID(), this);
        }

        @Override
        public void onClicked(AdColonyInterstitial ad) {
            RewardedVideoCallback callback = mRvCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }
    }

    private class AdIsColonyAdListener extends AdColonyInterstitialListener {
        @Override
        public void onRequestFilled(AdColonyInterstitial ad) {
            mIsAdColonyAds.put(ad.getZoneID(), ad);
            InterstitialAdCallback callback = mIsCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onRequestNotFilled(AdColonyZone zone) {
            InterstitialAdCallback callback = mIsCallback.get(zone.getZoneID());
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Request Not Filled"));
            }
        }

        @Override
        public void onOpened(AdColonyInterstitial ad) {
            InterstitialAdCallback callback = mIsCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onClosed(AdColonyInterstitial ad) {
            InterstitialAdCallback callback = mIsCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onExpiring(AdColonyInterstitial ad) {
            AdColony.requestInterstitial(ad.getZoneID(), this);
        }

        @Override
        public void onClicked(AdColonyInterstitial ad) {
            InterstitialAdCallback callback = mIsCallback.get(ad.getZoneID());
            if (callback != null) {
                callback.onInterstitialAdClick();
            }
        }
    }
}
