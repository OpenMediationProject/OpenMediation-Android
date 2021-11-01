// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.widget.RelativeLayout;

import com.adtbid.sdk.nativead.Ad;
import com.adtbid.sdk.nativead.NativeAd;
import com.adtbid.sdk.nativead.NativeAdListener;
import com.adtbid.sdk.utils.error.AdTimingError;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class AdTimingNativeManager {
    private static final String PAY_LOAD = "pay_load";
    private final List<NativeAdCallback> mNaCallback;

    private static class Holder {
        private static final AdTimingNativeManager INSTANCE = new AdTimingNativeManager();
    }

    private AdTimingNativeManager() {
        mNaCallback = new CopyOnWriteArrayList<>();
    }

    public static AdTimingNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void addNativeAdCallback(NativeAdCallback callback) {
        if (callback != null) {
            mNaCallback.add(callback);
        }
    }

    public void onInitSuccess() {
        for (NativeAdCallback callback : mNaCallback) {
            callback.onNativeAdInitSuccess();
        }
        mNaCallback.clear();
    }

    public void onInitFailed(AdapterError error) {
        for (NativeAdCallback callback : mNaCallback) {
            callback.onNativeAdInitFailed(error);
        }
        mNaCallback.clear();
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        String payload = "";
        if (extras.containsKey(PAY_LOAD)) {
            payload = extras.get(PAY_LOAD).toString();
        }
        NativeAd nativeAd = new NativeAd(MediationUtil.getContext(), adUnitId);
        InnerNativeAdListener listener = new InnerNativeAdListener(nativeAd, adUnitId, callback);
        nativeAd.setAdListener(listener);
        nativeAd.loadAdWithPayload(payload);
    }

    public void registerNativeView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        try {
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof NativeAd)) {
                AdLog.getSingleton().LogE("AdTiming NativeAd Not Ready! " + adUnitId);
                return;
            }
            NativeAd nativeAd = (NativeAd) adInfo.getAdnNativeAd();
            if (nativeAd == null) {
                AdLog.getSingleton().LogE("AdTiming NativeAd Not Ready! " + adUnitId);
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
            nativeAd.registerNativeAdView(adnNativeAdView);
        } catch (Throwable ignored) {
        }
    }

    public void destroyAd(String adUnitId, AdnAdInfo adInfo) {
        if (adInfo != null && adInfo.getAdnNativeAd() instanceof NativeAd) {
            NativeAd nativeAd = (NativeAd) adInfo.getAdnNativeAd();
            nativeAd.destroy();
            nativeAd = null;
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
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "AdTimingAdapter", "NativeAd Load Failed: No Fill"));
                }
                return;
            }
            if (mAdCallback != null) {
                AdnAdInfo adInfo = new AdnAdInfo();
                adInfo.setAdnNativeAd(mNativeAd);
                adInfo.setDesc(ad.getDescription());
                adInfo.setType(MediationInfo.MEDIATION_ID_1);
                adInfo.setTitle(ad.getTitle());
                adInfo.setCallToActionText(ad.getCTA());
                mAdCallback.onNativeAdLoadSuccess(adInfo);
            }
        }

        @Override
        public void onNativeAdFailed(String s, AdTimingError error) {
            mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, "AdTimingAdapter", error.getCode(), error.getMessage()));
        }

        @Override
        public void onNativeAdClicked(String s) {
            if (mAdCallback != null) {
                mAdCallback.onNativeAdAdClicked();
            }
        }

        @Override
        public void onNativeAdShowed(String s) {
            if (mAdCallback != null) {
                mAdCallback.onNativeAdImpression();
            }
        }

        @Override
        public void onNativeAdShowFailed(String s, AdTimingError adTimingError) {

        }
    }
}
