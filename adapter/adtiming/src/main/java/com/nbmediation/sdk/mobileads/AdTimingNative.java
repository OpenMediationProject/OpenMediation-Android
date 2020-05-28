// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;

import com.adtiming.mediationsdk.AdTimingAds;
import com.adtiming.mediationsdk.InitCallback;
import com.adtiming.mediationsdk.nativead.AdInfo;
import com.adtiming.mediationsdk.nativead.NativeAd;
import com.adtiming.mediationsdk.nativead.NativeAdListener;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdIconView;
import com.nbmediation.sdk.nativead.MediaView;
import com.nbmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public class AdTimingNative extends CustomNativeEvent implements NativeAdListener {
    private NativeAd mNativeAd;
    private MediaView mMediaView;
    private AdIconView mIconView;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }

        if (!AdTimingAds.isInit()) {
            String appKey = config.get("AppKey");
            AdTimingAds.init(activity, appKey, new InitCallback() {
                @Override
                public void onSuccess() {
                    loadNativeAd(activity);
                }

                @Override
                public void onError(AdTimingError adTimingError) {
                    onInsError(adTimingError.toString());
                }
            });
            return;
        }

        loadNativeAd(activity);
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        com.adtiming.mediationsdk.nativead.NativeAdView nativeAdView =
                new com.adtiming.mediationsdk.nativead.NativeAdView(adView.getContext());
        if (adView.getMediaView() != null) {
            mMediaView = adView.getMediaView();
        }

        if (adView.getAdIconView() != null) {
            mIconView = adView.getAdIconView();
        }
        if (mMediaView != null) {
            com.adtiming.mediationsdk.nativead.MediaView mediaView = new com.adtiming.mediationsdk.nativead.MediaView(adView.getContext());
            mMediaView.removeAllViews();
            mMediaView.addView(mediaView);
            nativeAdView.setMediaView(mediaView);
        }

        if (mIconView != null) {
            com.adtiming.mediationsdk.nativead.AdIconView iconView = new com.adtiming.mediationsdk.nativead.AdIconView(adView.getContext());
            mIconView.removeAllViews();
            mIconView.addView(iconView);
            nativeAdView.setAdIconView(iconView);
        }

        nativeAdView.setCallToActionView(adView.getCallToActionView());
        nativeAdView.setTitleView(adView.getTitleView());
        nativeAdView.setDescView(adView.getDescView());

        mNativeAd.registerNativeAdView(nativeAdView);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_1;
    }

    @Override
    public void destroy(Activity activity) {
        if (mNativeAd != null) {
            mNativeAd.destroy();
        }
    }

    private void loadNativeAd(Activity activity) {
        if (mNativeAd != null) {
            mNativeAd.loadAd();
            return;
        }
        mNativeAd = new NativeAd(activity, mInstancesKey, this);
        mNativeAd.loadAd();
    }

    @Override
    public void onAdReady(AdInfo adInfo) {
        com.nbmediation.sdk.nativead.AdInfo info = new com.nbmediation.sdk.nativead.AdInfo();
        info.setCallToActionText(adInfo.getCallToActionText());
        info.setDesc(adInfo.getDesc());
        info.setStarRating(adInfo.getStarRating());
        info.setTitle(adInfo.getTitle());
        info.setType(adInfo.getType());
        onInsReady(info);
    }

    @Override
    public void onAdFailed(String s) {
        onInsError(s);
    }

    @Override
    public void onAdClicked() {
        onInsClicked();
    }
}
