// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.AdTimingAds;
import com.adtiming.mediationsdk.InitCallback;
import com.adtiming.mediationsdk.adt.nativead.Ad;
import com.adtiming.mediationsdk.adt.nativead.NativeAd;
import com.adtiming.mediationsdk.adt.nativead.NativeAdListener;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
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
        AdTimingSingleTon.getInstance().initAdTiming(activity);
        if (!AdTimingAds.isInit()) {
            String appKey = config.get("AppKey");
            AdTimingAds.init(activity, appKey, new InitCallback() {
                @Override
                public void onSuccess() {
                    loadNativeAd(activity, config);
                }

                @Override
                public void onError(AdTimingError error) {
                    onInsError(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error.getErrorCode(), error.getErrorMessage()));
                }
            }, AdTimingAds.AD_TYPE.NONE);
            return;
        }

        loadNativeAd(activity, config);
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        try {
            if (isDestroyed || mAd == null) {
                return;
            }
            com.adtiming.mediationsdk.nativead.NativeAdView nativeAdView =
                    new com.adtiming.mediationsdk.nativead.NativeAdView(adView.getContext());
            if (adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();

                if (mAd.getContent() != null) {
                    mediaView.removeAllViews();
                    ImageView imageView = new ImageView(adView.getContext());
                    mediaView.addView(imageView);
                    imageView.setImageBitmap(mAd.getContent());
                    imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    imageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                }
                adView.setMediaView(mediaView);
            }

            if (adView.getAdIconView() != null) {
                AdIconView adIconView = adView.getAdIconView();
                if (mAd.getIcon() != null) {
                    adIconView.removeAllViews();
                    ImageView iconImageView = new ImageView(adView.getContext());
                    adIconView.addView(iconImageView);
                    iconImageView.setImageBitmap(mAd.getIcon());
                    iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                }
                adView.setAdIconView(adIconView);
            }
            nativeAdView.setCallToActionView(adView.getCallToActionView());
            nativeAdView.setTitleView(adView.getTitleView());
            nativeAdView.setDescView(adView.getDescView());
            mNativeAd.registerNativeAdView(nativeAdView);
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
    public void onNativeAdFailed(String placementId, com.adtiming.mediationsdk.adt.utils.error.AdTimingError error) {
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
    public void onNativeAdShowFailed(String s, com.adtiming.mediationsdk.adt.utils.error.AdTimingError adTimingError) {

    }

}
