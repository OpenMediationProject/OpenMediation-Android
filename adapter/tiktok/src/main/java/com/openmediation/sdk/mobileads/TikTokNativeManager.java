// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.api.nativeAd.PAGImageItem;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGMediaView;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAd;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdData;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdLoadListener;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeRequest;
import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.ImageUtils;
import com.crosspromotion.sdk.utils.ResDownloader;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.WorkExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TikTokNativeManager {

    private static class Holder {
        private static final TikTokNativeManager INSTANCE = new TikTokNativeManager();
    }

    private TikTokNativeManager() {
    }

    public static TikTokNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, Boolean consent, Boolean ageRestricted, Boolean privacyLimit, final NativeAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, consent, ageRestricted, privacyLimit, new TTAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onNativeAdInitSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", code, msg));
                }
            }
        });
    }

    public void loadAd(final Context context, final String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        try {
            PAGNativeRequest request = new PAGNativeRequest();
            PAGNativeAd.loadAd(adUnitId, request, new PAGNativeAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    AdLog.getSingleton().LogD("TikTokAdapter, NativeAd load onError code: " + code + ", message: " + message);
                    if (callback != null) {
                        callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", code, message));
                    }
                }

                @Override
                public void onAdLoaded(PAGNativeAd pagNativeAd) {
                    AdLog.getSingleton().LogD("TikTokAdapter, NativeAd onAdLoaded: " + pagNativeAd);
                    if (pagNativeAd == null) {
                        if (callback != null) {
                            callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "No Fill"));
                        }
                        return;
                    }
                    WorkExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            downloadRes(pagNativeAd, callback);
                        }
                    });
                }
            });
        } catch (Throwable e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "Unknown Error"));
            }
        }
    }

    public void registerView(final String adUnitId, final NativeAdView adView, final AdnAdInfo adInfo, final NativeAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                innerRegisterView(adUnitId, adView, adInfo, callback);
            }
        });
    }

    private void innerRegisterView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, final NativeAdCallback callback) {
        try {
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof PAGNativeAd)) {
                return;
            }
            PAGNativeAd nativeAd = (PAGNativeAd) adInfo.getAdnNativeAd();
            PAGNativeAdData adData = nativeAd.getNativeAdData();
            MediaView mediaView = adView.getMediaView();
            ArrayList<View> images = new ArrayList<>();
            if (mediaView != null) {
                PAGMediaView adnMediaView = adData.getMediaView();
//                MediationAdapterUtil.addNativeFeedMainView(adView.getContext(), nativeAd.getImageMode(), adnMediaView, feedAd.getAdView(), feedAd.getImageList());
                mediaView.removeAllViews();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adnMediaView.setLayoutParams(layoutParams);
                mediaView.addView(adnMediaView);
                images.add(adnMediaView);
            }
            PAGImageItem icon = adData.getIcon();
            if (icon != null && icon.getImageUrl() != null && adView.getAdIconView() != null) {
                AdIconView iconView = adView.getAdIconView();
                iconView.removeAllViews();
                ImageView adnIconView = new ImageView(adView.getContext());
                iconView.addView(adnIconView);
                adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                Bitmap content = ImageUtils.getBitmap(IOUtil.getFileInputStream(Cache.getCacheFile(adView.getContext(),
                        icon.getImageUrl(), null)));
                adnIconView.setImageBitmap(content);
            }
            List<View> clickViewList = new ArrayList<>();
            clickViewList.add(adView);
            //The views that can trigger the creative action (like download app)
            List<View> creativeViewList = new ArrayList<>();
            creativeViewList.add(adView);
            if (adView.getCallToActionView() != null) {
                creativeViewList.add(adView.getCallToActionView());
            }
            nativeAd.registerViewForInteraction(adView, clickViewList, creativeViewList, null, new PAGNativeAdInteractionListener() {
                @Override
                public void onAdShowed() {
                    AdLog.getSingleton().LogD("TikTokAdapter NativeAd onAdShowed");
                    if (callback != null) {
                        callback.onNativeAdImpression();
                    }
                }

                @Override
                public void onAdClicked() {
                    AdLog.getSingleton().LogD("TikTokAdapter NativeAd onAdClicked");
                    if (callback != null) {
                        callback.onNativeAdAdClicked();
                    }
                }

                @Override
                public void onAdDismissed() {
                    AdLog.getSingleton().LogD("TikTokAdapter, NativeAd onAdDismissed");
                }
            });
        } catch (Throwable e) {
            AdLog.getSingleton().LogE("TikTokNativeManager", "Native register error: " + e.getMessage());
        }
    }

    private void downloadRes(final PAGNativeAd ad, final NativeAdCallback callback) {
        try {
            PAGNativeAdData adData = ad.getNativeAdData();
            PAGImageItem icon = adData.getIcon();
            if (icon != null && icon.getImageUrl() != null) {
                File file = ResDownloader.downloadFile(icon.getImageUrl());
                if (file == null || !file.exists()) {
                    if (callback != null) {
                        callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "NativeAd Load Failed"));
                    }
                    return;
                }
                AdLog.getSingleton().LogD("TikTokAdapter", "Content File = " + file);
            }
            AdnAdInfo adInfo = new AdnAdInfo();
            adInfo.setAdnNativeAd(ad);
            adInfo.setDesc(adData.getDescription());
            adInfo.setType(MediationInfo.MEDIATION_ID_13);
            adInfo.setTitle(adData.getTitle());
            adInfo.setCallToActionText(adData.getButtonText());
            if (callback != null) {
                callback.onNativeAdLoadSuccess(adInfo);
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "NativeAd Load Failed: " + e.getMessage()));
            }
        }
    }

    public void destroyAd(String adUnitId, AdnAdInfo adInfo) {
    }

}
