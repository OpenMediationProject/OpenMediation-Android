/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

import admost.sdk.AdMostView;

public class AdmostNativeManager {

    private static final String TAG = "OM-AdMost: ";

    private static class Holder {
        private static final AdmostNativeManager INSTANCE = new AdmostNativeManager();
    }

    private AdmostNativeManager() {
    }

    public static AdmostNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(Activity activity, String appKey, NativeAdCallback callback) {
        AdmostSingleTon.getInstance().init(activity, appKey, new AdmostSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onNativeAdInitSuccess();
                }
            }

            @Override
            public void initFailed(int code, String error) {
                if (callback != null) {
                    callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "AdmostAdapter", code, error));
                }
            }
        });
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        AdmostBannerAdsConfig config = AdmostSingleTon.getInstance().getNativeAd();
        if (config == null || config.getAdMostView() == null || config.getAdView() == null) {
            String error = AdmostSingleTon.getInstance().getError(adUnitId);
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "AdmostAdapter", error));
            }
            return;
        }
        View adView = config.getAdView();
        AdmostSingleTon.getInstance().addNativeAdListener(config, new AdmostBannerCallback() {

            @Override
            public void onBannerAdClick(String adUnitId) {
                if (callback != null) {
                    callback.onNativeAdAdClicked();
                }
            }
        });
        AdnAdInfo info = new AdnAdInfo();
        info.setAdnNativeAd(config);
        info.setType(MediationInfo.MEDIATION_ID_24);
        info.setView(adView);
        info.setTemplateRender(true);
        if (callback != null) {
            callback.onNativeAdLoadSuccess(info);
        }
    }

    void registerView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof AdmostBannerAdsConfig)) {
            AdLog.getSingleton().LogE("Admost NativeAd Not Ready: AdnAdInfo is null, " + adUnitId);
            return;
        }
        AdmostBannerAdsConfig config = (AdmostBannerAdsConfig) adInfo.getAdnNativeAd();
        if (config == null || config.getAdMostView() == null || config.getAdView() == null) {
            AdLog.getSingleton().LogE("Admost NativeAd Not Ready: AdmostBannerAdsConfig is null, " + adUnitId);
            return;
        }
        try {
            View adMostView = config.getAdView();
            if (adMostView != null && adMostView.getParent() instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) adMostView.getParent();
                viewGroup.removeView(adMostView);
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            adView.addView(adMostView, layoutParams);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE("Admost NativeAd Render Failed: " + e.getMessage());
        }
    }

    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof AdmostBannerAdsConfig)) {
            return;
        }
        AdmostBannerAdsConfig config = (AdmostBannerAdsConfig) adInfo.getAdnNativeAd();
        if (config == null || config.getAdMostView() == null || config.getAdView() == null) {
            return;
        }
        try {
            AdmostSingleTon.getInstance().destroyNativeAd(config);
            AdMostView adMostView = config.getAdMostView();
            adMostView.destroy();
        } catch (Throwable ignored) {
        }
    }

}
