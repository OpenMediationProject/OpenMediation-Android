// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomNativeEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FacebookNative extends CustomNativeEvent implements NativeAdListener {
    private static final String PAY_LOAD = "pay_load";
    private AtomicBoolean mDidCallInit = new AtomicBoolean(false);
    private NativeAd nativeAd;
    private AdOptionsView adOptionsView;
    private MediaView mediaView;
    private AdIconView adIconView;
    private com.facebook.ads.MediaView mFbMediaView;
    private com.facebook.ads.MediaView mFbIconView;

    @Override
    public void registerNativeView(NativeAdView adView) {
        if (isDestroyed || nativeAd == null) {
            return;
        }
        try {
            NativeAdLayout fbNativeAdLayout = new NativeAdLayout(adView.getContext());
            List<View> views = new ArrayList<>();
            if (adView.getMediaView() != null) {
                mediaView = adView.getMediaView();
                views.add(mediaView);
            }

            if (adView.getAdIconView() != null) {
                adIconView = adView.getAdIconView();
                views.add(adIconView);
            }

            if (adView.getTitleView() != null) {
                views.add(adView.getTitleView());
            }

            if (adView.getDescView() != null) {
                views.add(adView.getDescView());
            }

            if (adView.getCallToActionView() != null) {
                views.add(adView.getCallToActionView());
            }

            if (adOptionsView == null) {
                adOptionsView = new AdOptionsView(adView.getContext(), nativeAd, fbNativeAdLayout);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                adView.addView(adOptionsView, layoutParams);
            }

            if (mediaView != null) {
                mediaView.removeAllViews();
                mFbMediaView = new com.facebook.ads.MediaView(adView.getContext());
                mediaView.addView(mFbMediaView);
            }
            if (adIconView != null) {
                adIconView.removeAllViews();
                mFbIconView = new com.facebook.ads.MediaView(adView.getContext());
                adIconView.addView(mFbIconView);
            }
            //pay attention to the order of fb_mediaView and adIconView here
            this.nativeAd.registerViewForInteraction(fbNativeAdLayout, mFbMediaView, mFbIconView, views);

            if (adOptionsView != null) {
                adOptionsView.bringToFront();
            }
        } catch (Throwable e) {
            // ignore
        }
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        initSdk(activity);
        nativeAd = new NativeAd(activity, mInstancesKey);
        NativeAd.NativeAdLoadConfigBuilder loadConfigBuilder = nativeAd.buildLoadAdConfig();
        if (config.containsKey(PAY_LOAD)) {
            loadConfigBuilder.withBid(config.get(PAY_LOAD));
        }
        loadConfigBuilder.withAdListener(this);
        nativeAd.loadAd(loadConfigBuilder.build());
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_3;
    }

    @Override
    public void destroy(Activity activity) {
        if (mFbMediaView != null) {
            mFbMediaView.destroy();
        }
        if (mFbIconView != null) {
            mFbIconView.destroy();
        }
        if (nativeAd != null) {
            nativeAd.unregisterView();
            nativeAd.destroy();
            nativeAd = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onMediaDownloaded(Ad ad) {

    }

    @Override
    public void onError(Ad ad, AdError adError) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, adError.getErrorCode(), adError.getErrorMessage()));
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (isDestroyed) {
            return;
        }
        mAdInfo.setDesc(nativeAd.getAdBodyText());
        mAdInfo.setType(getMediation());
        mAdInfo.setCallToActionText(nativeAd.getAdCallToAction());
        mAdInfo.setTitle(nativeAd.getAdHeadline());

        onInsReady(mAdInfo);
        AdLog.getSingleton().LogD("OM-Facebook", "Facebook Native ad load success ");
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
