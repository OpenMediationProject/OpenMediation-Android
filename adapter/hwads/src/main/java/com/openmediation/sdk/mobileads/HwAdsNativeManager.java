// Copyright 2022 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.Image;
import com.huawei.hms.ads.nativead.NativeAd;
import com.huawei.hms.ads.nativead.NativeAdConfiguration;
import com.huawei.hms.ads.nativead.NativeAdLoader;
import com.huawei.hms.ads.nativead.NativeView;
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

public class HwAdsNativeManager {

    private static class Holder {
        private static final HwAdsNativeManager INSTANCE = new HwAdsNativeManager();
    }

    private HwAdsNativeManager() {
    }

    public static HwAdsNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final NativeAdCallback callback) {
        HwAdsSingleTon.getInstance().initSDK(context, new HwAdsSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onNativeAdInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "HwAdsAdapter", msg));
                }
            }
        });
    }

    public void loadAd(final Context context, final String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        try {
            NativeAdConfiguration adConfiguration = new NativeAdConfiguration.Builder()
                    .setChoicesPosition(NativeAdConfiguration.ChoicesPosition.BOTTOM_RIGHT)
                    .build();
            NativeAdLoader.Builder builder = new NativeAdLoader.Builder(context, adUnitId);
            builder.setNativeAdLoadedListener(new NativeAd.NativeAdLoadedListener() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    AdLog.getSingleton().LogD("HwAdsNativeManager NativeAd onNativeAdLoaded, adUnitId: " + adUnitId);
                    if (nativeAd == null) {
                        if (callback != null) {
                            callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_NATIVE, "HwAdsAdapter", "NativeAd Load Failed: No Fill"));
                        }
                        return;
                    }
                    if (callback != null) {
                        AdnAdInfo adInfo = new AdnAdInfo();
                        adInfo.setAdnNativeAd(nativeAd);
                        adInfo.setDesc(nativeAd.getDescription());
                        adInfo.setType(MediationInfo.MEDIATION_ID_28);
                        adInfo.setTitle(nativeAd.getTitle());
                        adInfo.setCallToActionText(nativeAd.getCallToAction());
                        Double rating = nativeAd.getRating();
                        if (rating != null) {
                            adInfo.setStarRating(rating);
                        }
                        callback.onNativeAdLoadSuccess(adInfo);
                    }
                }
            }).setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    AdLog.getSingleton().LogD("HwAdsNativeManager NativeAd onAdLoaded, adUnitId: " + adUnitId);
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    AdLog.getSingleton().LogD("HwAdsNativeManager Native Ad onAdClicked: " + adUnitId);
                    if (callback != null) {
                        callback.onNativeAdAdClicked();
                    }
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    AdLog.getSingleton().LogD("HwAdsNativeManager Native Ad onAdImpression: " + adUnitId);
                    if (callback != null) {
                        callback.onNativeAdImpression();
                    }
                }

                @Override
                public void onAdFailed(int errorCode) {
                    AdLog.getSingleton().LogE("HwAdsNativeManager NativeAd onAdFailed, errorCode: " + errorCode + ", adUnitId: " + adUnitId);
                    if (callback != null) {
                        callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, "HwAdsAdapter", errorCode, ""));
                    }
                }
            });
            NativeAdLoader nativeAdLoader = builder.setNativeAdOptions(adConfiguration).build();
            nativeAdLoader.loadAd(new AdParam.Builder().build());
        } catch (Throwable e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "HwAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    public void registerNativeView(final String adUnitId, final NativeAdView adView, final AdnAdInfo adInfo,
                                   final NativeAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof NativeAd)) {
                        AdLog.getSingleton().LogE("HwAdsAdapter NativeAd not ready");
                        return;
                    }
                    final NativeAd nativeAd = (NativeAd) adInfo.getAdnNativeAd();
                    if (nativeAd == null) {
                        return;
                    }
                    // 注册和填充标题素材视图
                    com.huawei.hms.ads.nativead.NativeView hwNativeView = new NativeView(adView.getContext());
                    MediaView mediaView = adView.getMediaView();
                    if (mediaView != null) {
                        com.huawei.hms.ads.nativead.MediaView hwMediaView = new com.huawei.hms.ads.nativead.MediaView(adView.getContext());
                        // 注册和填充多媒体素材视图
                        hwMediaView.setMediaContent(nativeAd.getMediaContent());
                        mediaView.removeAllViews();
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        mediaView.addView(hwMediaView, layoutParams);
                        hwNativeView.setMediaView(hwMediaView);
                    }
                    if (adView.getTitleView() != null) {
                        hwNativeView.setTitleView(adView.getTitleView());
                    }
                    if (adView.getDescView() != null) {
                        hwNativeView.setDescriptionView(adView.getDescView());
                    }
                    if (adView.getCallToActionView() != null) {
                        hwNativeView.setCallToActionView(adView.getCallToActionView());
                    }
                    AdIconView iconView = adView.getAdIconView();
                    if (iconView != null && nativeAd.getIcon() != null) {
                        iconView.removeAllViews();
                        final ImageView adnIconView = new ImageView(adView.getContext());
                        iconView.addView(adnIconView);
                        adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                        adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                        Image icon = nativeAd.getIcon();
                        adnIconView.setImageDrawable(icon.getDrawable());
                        hwNativeView.setIconView(iconView);
                    }
                    // 注册原生广告对象
                    hwNativeView.setNativeAd(nativeAd);
                    int count = adView.getChildCount();
                    if (count > 0) {
                        View actualView = adView.getChildAt(0);
                        adView.removeView(actualView);
                        hwNativeView.addView(actualView);
                        adView.addView(hwNativeView);
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    public void destroyAd(String adUnitId, final AdnAdInfo adInfo) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof NativeAd)) {
                        AdLog.getSingleton().LogE("HwAdsNativeManager NativeAd destroyAd failed");
                        return;
                    }
                    NativeAd nativeAd = (NativeAd) adInfo.getAdnNativeAd();
                    if (nativeAd == null) {
                        return;
                    }
                    nativeAd.destroy();
                } catch (Throwable ignored) {
                }
            }
        });
    }

}
