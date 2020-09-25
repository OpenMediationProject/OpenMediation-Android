// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.adtiming.mediationsdk.AdTimingAds;
import com.adtiming.mediationsdk.InitCallback;
import com.adtiming.mediationsdk.adt.banner.AdtAdSize;
import com.adtiming.mediationsdk.adt.banner.BannerAd;
import com.adtiming.mediationsdk.adt.banner.BannerAdListener;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.util.Map;

public class AdTimingBanner extends CustomBannerEvent implements BannerAdListener {
    private static final String TAG = "OM-AdTiming: ";
    private static final String PAY_LOAD = "pay_load";
    private BannerAd mBannerAd;

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_1;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        AdTimingAds.setGDPRConsent(consent);
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        AdTimingAds.setAgeRestricted(value);
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        AdTimingAds.setAgeRestricted(restricted);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        AdTimingAds.setUserAge(age);
    }

    @Override
    public void setUserGender(Context context, String gender) {
        super.setUserGender(context, gender);
        AdTimingAds.setUserGender(gender);
    }

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }

        if (!AdTimingAds.isInit()) {
            String appKey = config.get("AppKey");
            AdTimingAds.init(activity, appKey, new InitCallback() {
                @Override
                public void onSuccess() {
                    loadBanner(activity, config);
                }

                @Override
                public void onError(AdTimingError error) {
                    onInsError(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error.getErrorCode(), error.getErrorMessage()));
                }
            }, AdTimingAds.AD_TYPE.INTERACTIVE);
            return;
        }
        loadBanner(activity, config);
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerAd != null) {
            mBannerAd.destroy();
            mBannerAd = null;
        }
        isDestroyed = true;
    }

    private void loadBanner(Activity activity, Map<String, String> config) {
        String payload = "";
        if (config.containsKey(PAY_LOAD)) {
            payload = config.get(PAY_LOAD);
        }
        if (mBannerAd != null) {
            mBannerAd.loadAdWithPayload(payload);
            return;
        }
        mBannerAd = new BannerAd(activity, mInstancesKey);
        mBannerAd.setAdListener(this);
        AdtAdSize adSize = getAdSize(activity, config);
        mBannerAd.setAdSize(adSize);
        mBannerAd.loadAdWithPayload(payload);
    }

    @Override
    public void onBannerAdReady(String placementId, View view) {
        if (!isDestroyed) {
            onInsReady(view);
        }
    }

    @Override
    public void onBannerAdFailed(String placementId, com.adtiming.mediationsdk.adt.utils.error.AdTimingError error) {
        if (!isDestroyed) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error.getCode(), error.getMessage()));
        }
    }

    @Override
    public void onBannerAdClicked(String placementId) {
        if (!isDestroyed) {
            onInsClicked();
        }
    }

    @Override
    public void onBannerAdShowFailed(String placementId, com.adtiming.mediationsdk.adt.utils.error.AdTimingError error) {

    }

    @Override
    public void onBannerAdEvent(String placementId, String event) {
    }

    private AdtAdSize getAdSize(Context context, Map<String, String> config) {
        String bannerDesc = getBannerDesc(config);
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return AdtAdSize.LEADERBOARD;
            case DESC_RECTANGLE:
                return AdtAdSize.MEDIUM_RECTANGLE;
            case DESC_SMART:
                if (isLargeScreen(context)) {
                    return AdtAdSize.LEADERBOARD;
                } else {
                    return AdtAdSize.BANNER;
                }
            default:
                return AdtAdSize.BANNER;
        }
    }
}
