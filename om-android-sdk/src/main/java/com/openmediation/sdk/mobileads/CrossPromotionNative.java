// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.nativead.Ad;
import com.crosspromotion.sdk.nativead.NativeAd;
import com.crosspromotion.sdk.nativead.NativeAdListener;
import com.crosspromotion.sdk.utils.error.Error;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomNativeEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public class CrossPromotionNative extends CustomNativeEvent implements NativeAdListener {
    private static final String PAY_LOAD = "pay_load";
    private NativeAd mNativeAd;
    private Ad mAd;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        String payload = "";
        if (config.containsKey(PAY_LOAD)) {
            payload = config.get(PAY_LOAD);
        }
        if (mNativeAd != null) {
            mNativeAd.loadAdWithPayload(payload, config);
            return;
        }
        mNativeAd = new NativeAd(activity, mInstancesKey);
        mNativeAd.setAdListener(this);
        mNativeAd.loadAdWithPayload(payload, config);
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        try {
            if (isDestroyed || mAd == null) {
                return;
            }
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
            }

            mNativeAd.registerNativeAdView(adView);
        } catch (Throwable ignored) {

        }
    }

    @Override
    public void onNativeAdReady(String placementId, Ad ad) {
        if (!isDestroyed) {
            if (ad == null) {
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "NativeAd Load Failed: return ad is null"));
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
    public void onNativeAdFailed(String placementId, Error error) {
        if (!isDestroyed) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error.getCode(), error.getMessage()));
        }
    }

    @Override
    public void onNativeAdClicked(String placementId) {
        if (!isDestroyed) {
            onInsClicked();
        }
    }

    @Override
    public void onNativeAdShowFailed(String placementId, Error error) {

    }

    @Override
    public void destroy(Activity activity) {
        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
        isDestroyed = true;
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_19;
    }

}
