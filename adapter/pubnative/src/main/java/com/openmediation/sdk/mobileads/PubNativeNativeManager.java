// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Application;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.ImageUtils;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.IOUtil;

import net.pubnative.lite.sdk.models.NativeAd;

import java.util.Map;

public class PubNativeNativeManager {

    private static final String ADN_OBJECT = "AdnObject";

    private static class Holder {
        private static final PubNativeNativeManager INSTANCE = new PubNativeNativeManager();
    }

    private PubNativeNativeManager() {
    }

    public static PubNativeNativeManager getInstance() {
        return PubNativeNativeManager.Holder.INSTANCE;
    }

    public void initAd(Application application, Map<String, Object> extras, final NativeAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        PubNativeSingleTon.getInstance().init(application, appKey, new PubNativeSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onNativeAdInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "PubNativeAdapter", error));
                }
            }
        });
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        AdnAdInfo info = null;
        NativeAd nativeAd = null;
        if (extras.get(ADN_OBJECT) instanceof AdnAdInfo) {
            info = (AdnAdInfo) extras.get(ADN_OBJECT);
            if (info != null && info.getAdnNativeAd() instanceof NativeAd) {
                nativeAd = (NativeAd) info.getAdnNativeAd();
            }
        }
        if (nativeAd != null) {
            PubNativeSingleTon.getInstance().downloadRes(info, nativeAd, callback);
            return;
        }
        PubNativeSingleTon.getInstance().loadNative(adUnitId, callback, null);
    }

    public void registerNativeView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, final NativeAdCallback callback) {
        try {
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof NativeAd)) {
                AdLog.getSingleton().LogE("PubNativeAdapter NativeAd not ready");
                return;
            }
            final NativeAd nativeAd = (NativeAd) adInfo.getAdnNativeAd();
            if (nativeAd == null) {
                return;
            }
            if (!TextUtils.isEmpty(nativeAd.getBannerUrl()) && adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();
                mediaView.removeAllViews();

                ImageView adnMediaView = new ImageView(adView.getContext());
                mediaView.addView(adnMediaView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adnMediaView.setLayoutParams(layoutParams);
                Bitmap content = ImageUtils.getBitmap(IOUtil.getFileInputStream(Cache.getCacheFile(adView.getContext(),
                        nativeAd.getBannerUrl(), null)));
                adnMediaView.setImageBitmap(content);
            }

            if (!TextUtils.isEmpty(nativeAd.getIconUrl()) && adView.getAdIconView() != null) {
                AdIconView iconView = adView.getAdIconView();
                iconView.removeAllViews();
                ImageView adnIconView = new ImageView(adView.getContext());
                iconView.addView(adnIconView);
                adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                Bitmap content = ImageUtils.getBitmap(IOUtil.getFileInputStream(Cache.getCacheFile(adView.getContext(),
                        nativeAd.getIconUrl(), null)));
                adnIconView.setImageBitmap(content);
            }
            NativeAd.Listener listener = new NativeAd.Listener() {
                @Override
                public void onAdImpression(NativeAd ad, View view) {
                    AdLog.getSingleton().LogD("PubNative NativeAd onAdImpression");
                    if (callback != null) {
                        callback.onNativeAdImpression();
                    }
                }

                @Override
                public void onAdClick(NativeAd ad, View view) {
                    AdLog.getSingleton().LogD("PubNative NativeAd onAdClick");
                    if (callback != null) {
                        callback.onNativeAdAdClicked();
                    }
                }
            };
            if (adView.getCallToActionView() != null) {
                nativeAd.startTracking(adView.getCallToActionView(), listener);
            } else {
                nativeAd.startTracking(adView, listener);
            }
        } catch (Throwable ignored) {
        }
    }

    public void destroyAd(String adUnitId, AdnAdInfo adInfo) {
        try {
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof NativeAd)) {
                AdLog.getSingleton().LogE("PubNativeAdapter NativeAd destroyAd failed");
                return;
            }
            NativeAd nativeAd = (NativeAd) adInfo.getAdnNativeAd();
            nativeAd.stopTracking();
        } catch (Throwable ignored) {
        }
    }

}
