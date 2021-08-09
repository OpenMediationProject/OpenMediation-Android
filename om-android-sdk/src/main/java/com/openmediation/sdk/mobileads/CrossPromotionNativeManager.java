// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.nativead.Ad;
import com.crosspromotion.sdk.nativead.NativeAd;
import com.crosspromotion.sdk.nativead.NativeAdListener;
import com.crosspromotion.sdk.utils.error.Error;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

public class CrossPromotionNativeManager {
    private static final String TAG = "OM-CrossPromotion: ";
    private static final String PAY_LOAD = "pay_load";

    private static class Holder {
        private static final CrossPromotionNativeManager INSTANCE = new CrossPromotionNativeManager();
    }

    private CrossPromotionNativeManager() {
    }

    public static CrossPromotionNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        String payload = "";
        if (extras.containsKey(PAY_LOAD)) {
            payload = extras.get(PAY_LOAD).toString();
        }
        if (TextUtils.isEmpty(payload)) {
            AdLog.getSingleton().LogD(TAG, "NativeAd load failed: payload is empty");
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "CrossPromotionAdapter", "payload is empty"));
            }
            return;
        }
        NativeAd nativeAd = new NativeAd(MediationUtil.getContext(), adUnitId);
        InnerNativeAdListener listener = new InnerNativeAdListener(nativeAd, adUnitId, callback);
        nativeAd.setAdListener(listener);
        nativeAd.loadAdWithPayload(payload, extras);
    }

    public void registerNativeView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof CrossPromotionNativeAdsConfig)) {
            return;
        }
        try {
            CrossPromotionNativeAdsConfig config = (CrossPromotionNativeAdsConfig) adInfo.getAdnNativeAd();
            Ad ad = config.getContent();
            if (adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();

                if (ad.getContent() != null) {
                    mediaView.removeAllViews();
                    ImageView imageView = new ImageView(adView.getContext());
                    mediaView.addView(imageView);
                    imageView.setImageBitmap(ad.getContent());
                    imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    imageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                }
            }

            if (adView.getAdIconView() != null) {
                AdIconView adIconView = adView.getAdIconView();
                if (ad.getIcon() != null) {
                    adIconView.removeAllViews();
                    ImageView iconImageView = new ImageView(adView.getContext());
                    adIconView.addView(iconImageView);
                    iconImageView.setImageBitmap(ad.getIcon());
                    iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                }
            }
            config.getNativeAd().registerNativeAdView(adView);
        } catch (Throwable ignored) {

        }
    }

    public void destroyAd(String adUnitId, AdnAdInfo adInfo) {
        if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof CrossPromotionNativeAdsConfig)) {
            return;
        }
        CrossPromotionNativeAdsConfig config = (CrossPromotionNativeAdsConfig) adInfo.getAdnNativeAd();
        if (config.getNativeAd() != null) {
            config.getNativeAd().destroy();
        }
    }

    private class InnerNativeAdListener implements NativeAdListener {
        private String mAdUnitId;
        private NativeAdCallback mAdCallback;
        private NativeAd mNativeAd;

        private InnerNativeAdListener(NativeAd nativeAd, String adUnitId, NativeAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
            this.mNativeAd = nativeAd;
        }

        @Override
        public void onNativeAdReady(String s, Ad ad) {
            if (ad == null) {
                if (mAdCallback != null) {
                    mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "CrossPromotionAdapter", "NativeAd Load Failed: No Fill"));
                }
                return;
            }
            if (mAdCallback != null) {
                AdnAdInfo adInfo = new AdnAdInfo();
                adInfo.setDesc(ad.getDescription());
                adInfo.setType(MediationInfo.MEDIATION_ID_1);
                adInfo.setTitle(ad.getTitle());
                adInfo.setCallToActionText(ad.getCTA());
                CrossPromotionNativeAdsConfig nativeAdsConfig = new CrossPromotionNativeAdsConfig();
                nativeAdsConfig.setContent(ad);
                nativeAdsConfig.setNativeAd(mNativeAd);
                adInfo.setAdnNativeAd(nativeAdsConfig);
                mAdCallback.onNativeAdLoadSuccess(adInfo);
            }
        }

        @Override
        public void onNativeAdFailed(String placementId, Error error) {
            mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, "CrossPromotionAdapter", error.getCode(), error.getMessage()));
        }

        @Override
        public void onNativeAdClicked(String s) {
            if (mAdCallback != null) {
                mAdCallback.onNativeAdAdClicked();
            }
        }

        @Override
        public void onNativeAdShowFailed(String placementId, Error error) {

        }
    }
}
