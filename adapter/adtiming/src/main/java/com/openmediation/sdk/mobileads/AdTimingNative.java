// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adtbid.sdk.AdTimingAds;
import com.adtbid.sdk.InitCallback;
import com.adtbid.sdk.nativead.Ad;
import com.adtbid.sdk.nativead.NativeAd;
import com.adtbid.sdk.nativead.NativeAdListener;
import com.adtbid.sdk.utils.error.AdTimingError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomNativeEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public class AdTimingNative extends CustomNativeEvent implements NativeAdListener {
    private static final String PAY_LOAD = "pay_load";
    private NativeAd mNativeAd;
    private Ad mAd;

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
        String appKey = config.get("AppKey");
        AdTimingSingleTon.getInstance().initAdTiming(activity, appKey, new AdTimingSingleTon.AdTimingInitCallback() {
            @Override
            public void onSuccess() {
                loadNativeAd(activity, config);
            }

            @Override
            public void onError(AdTimingError error) {
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error.getCode(), error.getMessage()));
            }
        });
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        try {
            if (isDestroyed || mAd == null) {
                return;
            }
            com.adtbid.sdk.nativead.NativeAdView adnNativeAdView =
                    new com.adtbid.sdk.nativead.NativeAdView(adView.getContext());

            if (adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();
                mediaView.removeAllViews();
                com.adtbid.sdk.nativead.MediaView adnMediaView = new com.adtbid.sdk.nativead.MediaView(adView.getContext());
                mediaView.addView(adnMediaView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adnMediaView.setLayoutParams(layoutParams);
                adnNativeAdView.setMediaView(adnMediaView);
            }

            if (adView.getAdIconView() != null) {
                AdIconView iconView = adView.getAdIconView();
                iconView.removeAllViews();
                com.adtbid.sdk.nativead.AdIconView adnIconView = new com.adtbid.sdk.nativead.AdIconView(adView.getContext());
                iconView.addView(adnIconView);
                adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                adnNativeAdView.setAdIconView(adnIconView);
            }

            adnNativeAdView.setCallToActionView(adView.getCallToActionView());
            adnNativeAdView.setTitleView(adView.getTitleView());
            adnNativeAdView.setDescView(adView.getDescView());
            adView.addView(adnNativeAdView);
            mNativeAd.registerNativeAdView(adnNativeAdView);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_1;
    }

    @Override
    public void destroy(Activity activity) {
        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
        isDestroyed = true;
    }

    private void loadNativeAd(Activity activity, Map<String, String> config) {
        String payload = "";
        if (config.containsKey(PAY_LOAD)) {
            payload = config.get(PAY_LOAD);
        }
        if (mNativeAd != null) {
            mNativeAd.loadAdWithPayload(payload);
            return;
        }
        mNativeAd = new NativeAd(activity, mInstancesKey);
        mNativeAd.setAdListener(this);
        mNativeAd.loadAdWithPayload(payload);
    }

    @Override
    public void onNativeAdReady(String placementId, Ad ad) {
        if (!isDestroyed) {
            if (ad == null) {
                onInsError(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "NativeAd Load Failed"));
                return;
            }
            mAd = ad;
            AdInfo adInfo = new AdInfo();
            adInfo.setDesc(ad.getDescription());
            adInfo.setType(getMediation());
            adInfo.setTitle(ad.getTitle());
            adInfo.setCallToActionText(ad.getCTA());
            onInsReady(adInfo);
        }
    }

    @Override
    public void onNativeAdFailed(String placementId, AdTimingError error) {
        if (!isDestroyed) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error.getCode(), error.getMessage()));
        }
    }

    @Override
    public void onNativeAdClicked(String s) {
        if (!isDestroyed) {
            onInsClicked();
        }
    }

    @Override
    public void onNativeAdShowFailed(String s, AdTimingError adTimingError) {

    }

}
