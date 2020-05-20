// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;

import com.facebook.ads.AdSettings;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;

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
        AdSize adSize = getAdSize(config);
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
        AdLog.getSingleton().LogE("Om-Facebook: Facebook Banner ad load failed " + adError.getErrorMessage());
        onInsError(adError.getErrorMessage());
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD("Om-Facebook", "Facebook Banner ad load success ");
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

    private AdSize getAdSize(Map<String, String> config) {
        int[] size = getBannerSize(config);
        int height = size[1];
        if (height == AdSize.BANNER_HEIGHT_90.getHeight()) {
            return AdSize.BANNER_HEIGHT_90;
        } else if (height == AdSize.RECTANGLE_HEIGHT_250.getHeight()) {
            return AdSize.RECTANGLE_HEIGHT_250;
        }
        return AdSize.BANNER_HEIGHT_50;
    }

    private void initSdk(Activity activity) {
        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);
        if (mDidCallInit.compareAndSet(false, true)) {
            if (AudienceNetworkAds.isInAdsProcess(activity.getApplicationContext())) {
                // According to Xabi from facebook (29/4/19) - the meaning of isInAdsProcess==true is that
                // another process has already initialized Facebook's SDK and in this case there's no need to init it again.
                // Without this check an error will appear in the log.
                return;
            }

            AudienceNetworkAds.buildInitSettings(activity.getApplicationContext())
                    .withInitListener(new AudienceNetworkAds.InitListener() {
                        @Override
                        public void onInitialized(final AudienceNetworkAds.InitResult result) {
                        }
                    }).initialize();
        }
    }
}
