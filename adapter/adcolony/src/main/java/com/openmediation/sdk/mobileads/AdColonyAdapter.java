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
import com.adcolony.sdk.AdColonyUserMetadata;
import com.adcolony.sdk.AdColonyZone;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.adcolony.BuildConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdColonyAdapter extends CustomAdsAdapter implements AdColonyRewardListener {
    private boolean mDidInited = false;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallback;
    private ConcurrentHashMap<String, AdColonyInterstitial> mAdColonyAds;
    private AdColonyAppOptions mAdColonyOptions;

    public AdColonyAdapter() {
        mRvCallback = new ConcurrentHashMap<>();
        mAdColonyAds = new ConcurrentHashMap<>();
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
        mAdColonyOptions.setGDPRConsentString(consent ? "1" : "0");
        mAdColonyOptions.setGDPRRequired(true);
        AdColony.setAppOptions(mAdColonyOptions);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        try {
            AdColonyUserMetadata userMetadata = mAdColonyOptions.getUserMetadata();
            if (userMetadata == null) {
                userMetadata = new AdColonyUserMetadata();
            }
            userMetadata.setUserAge(age);
            mAdColonyOptions.setUserMetadata(userMetadata);
            AdColony.setAppOptions(mAdColonyOptions);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setUserGender(Context context, String gender) {
        try {
            AdColonyUserMetadata userMetadata = mAdColonyOptions.getUserMetadata();
            if (userMetadata == null) {
                userMetadata = new AdColonyUserMetadata();
            }
            userMetadata.setUserGender(gender);
            mAdColonyOptions.setUserMetadata(userMetadata);
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
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
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
            AdColonyInterstitial interstitial = mAdColonyAds.get(adUnitId);
            if (interstitial != null) {
                interstitial.show();
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

            AdColonyInterstitial interstitial = mAdColonyAds.get(adUnitId);
            return interstitial != null && !interstitial.isExpired();
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

    private class AdColonyAdListener extends AdColonyInterstitialListener {
        @Override
        public void onRequestFilled(AdColonyInterstitial var1) {
            mAdColonyAds.put(var1.getZoneID(), var1);
            RewardedVideoCallback callback = mRvCallback.get(var1.getZoneID());
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
}
