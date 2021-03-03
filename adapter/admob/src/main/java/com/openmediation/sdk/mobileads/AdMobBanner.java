// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.util.Map;

public class AdMobBanner extends CustomBannerEvent {

    private AdView mBannerView;

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        int value = restricted ? MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE : MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;
        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
                .setTagForChildDirectedTreatment(value)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        setAgeRestricted(context, age < 13);
    }

    private AdRequest createAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        if (mUserConsent != null || mUSPrivacyLimit != null) {
            Bundle extras = new Bundle();
            if (mUserConsent != null && !mUserConsent) {
                extras.putString("npa", "1");
            }
            if (mUSPrivacyLimit != null) {
                extras.putInt("rdp", mUSPrivacyLimit ? 1 : 0);
            }
            builder.addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter.class, extras);
        }
        return builder.build();
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (mBannerView != null) {
            mBannerView.loadAd(createAdRequest());
            return;
        }
        mBannerView = new AdView(activity.getApplicationContext());
        mBannerView.setAdUnitId(mInstancesKey);
        AdSize adSize = getAdSize(config);
        if (adSize != null) {
            mBannerView.setAdSize(adSize);
        }
        mBannerView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                if (!isDestroyed) {
                    onInsReady(mBannerView);
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (!isDestroyed) {
                    onInsClicked();
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (!isDestroyed) {
                    onInsError(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
                }
            }
        });
        mBannerView.loadAd(createAdRequest());
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_2;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
        isDestroyed = true;
    }

    private AdSize getAdSize(Map<String, String> config) {
        String bannerDesc = getBannerDesc(config);
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return AdSize.LEADERBOARD;
            case DESC_RECTANGLE:
                return AdSize.MEDIUM_RECTANGLE;
            case DESC_SMART:
                return AdSize.SMART_BANNER;
            default:
                return AdSize.BANNER;
        }
    }
}
