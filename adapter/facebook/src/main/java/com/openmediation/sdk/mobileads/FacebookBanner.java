// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FacebookBanner extends CustomBannerEvent implements AdListener {
    private static final String PAY_LOAD = "pay_load";
    private AtomicBoolean mDidCallInit = new AtomicBoolean(false);
    private AdView adView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }
        initSdk(activity);
        if (adView != null) {
            adView.loadAd();
            return;
        }
        AdSize adSize = getAdSize(activity, config);
        adView = new AdView(activity.getApplicationContext(), mInstancesKey, adSize);
        AdView.AdViewLoadConfigBuilder loadConfigBuilder = adView.buildLoadAdConfig();
        if (config.containsKey(PAY_LOAD)) {
            loadConfigBuilder.withBid(config.get(PAY_LOAD));
        }
        loadConfigBuilder.withAdListener(this);
        adView.loadAd(loadConfigBuilder.build());
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_3;
    }

    @Override
    public void destroy(Activity activity) {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, adError.getErrorCode(), adError.getErrorMessage()));
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD("OM-Facebook", "Facebook Banner ad load success ");
        onInsReady(adView);
    }

    @Override
    public void onAdClicked(Ad ad) {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    @Override
    public void onLoggingImpression(Ad ad) {

    }

    private AdSize getAdSize(Context context, Map<String, String> config) {
        String bannerDesc = getBannerDesc(config);
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return AdSize.BANNER_HEIGHT_90;
            case DESC_RECTANGLE:
                return AdSize.RECTANGLE_HEIGHT_250;
            case DESC_SMART:
                if (isLargeScreen(context)) {
                    return AdSize.BANNER_HEIGHT_90;
                } else {
                    return AdSize.BANNER_HEIGHT_50;
                }
            default:
                return AdSize.BANNER_HEIGHT_50;
        }
    }

    private void initSdk(Activity activity) {
        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);
        if (mDidCallInit.compareAndSet(false, true)) {
            AudienceNetworkAds.buildInitSettings(activity.getApplicationContext())
                    .withInitListener(new AudienceNetworkAds.InitListener() {
                        @Override
                        public void onInitialized(final AudienceNetworkAds.InitResult result) {
                        }
                    }).initialize();
        }
    }
}
