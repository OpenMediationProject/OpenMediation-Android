// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.ImageUtils;
import com.crosspromotion.sdk.utils.ResDownloader;
import com.mbridge.msdk.MBridgeConstans;
import com.mbridge.msdk.nativex.view.MBMediaView;
import com.mbridge.msdk.out.Campaign;
import com.mbridge.msdk.out.Frame;
import com.mbridge.msdk.out.MBBidNativeHandler;
import com.mbridge.msdk.out.MBNativeHandler;
import com.mbridge.msdk.out.NativeListener;
import com.mbridge.msdk.out.OnMBMediaViewListenerPlus;
import com.mbridge.msdk.widget.MBAdChoice;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MintegralNativeManager {

    private static class Holder {
        private static final MintegralNativeManager INSTANCE = new MintegralNativeManager();
    }

    private MintegralNativeManager() {
    }

    public static MintegralNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final NativeAdCallback callback, Boolean userConsent, Boolean ageRestricted) {
        String appKey = (String) extras.get("AppKey");
        MintegralSingleTon.getInstance().initSDK(context, appKey, new MintegralSingleTon.InitCallback() {
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
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "MintegralAdapter", msg));
                }
            }
        }, userConsent, ageRestricted);
    }

    public void loadAd(final Context context, final String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        try {
            String payload = "";
            if (extras.containsKey(MintegralAdapter.PAY_LOAD)) {
                payload = String.valueOf(extras.get(MintegralAdapter.PAY_LOAD));
            }
            int width = 0;
            int height = 0;
            final Map<String, Object> preloadMap = new HashMap<>();
            preloadMap.put(MBridgeConstans.PROPERTIES_LAYOUT_TYPE, MBridgeConstans.LAYOUT_NATIVE);
            try {
                width = Integer.parseInt(String.valueOf(extras.get("width")));
            } catch (Exception ignored) {
            }
            if (width > 0) {
                preloadMap.put(MBridgeConstans.NATIVE_VIDEO_WIDTH, width);
            }
            try {
                height = Integer.parseInt(String.valueOf(extras.get("height")));
            } catch (Exception ignored) {
            }
            if (height > 0) {
                preloadMap.put(MBridgeConstans.NATIVE_VIDEO_HEIGHT, height);
            }
            preloadMap.put(MBridgeConstans.PROPERTIES_UNIT_ID, adUnitId);
            preloadMap.put(MBridgeConstans.PROPERTIES_AD_NUM, 1);
            preloadMap.put(MBridgeConstans.PREIMAGE, true);
            preloadMap.put(MBridgeConstans.NATIVE_VIDEO_SUPPORT, true);
            final String finalPayload = payload;
            MediationUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InnerAdListener listener = new InnerAdListener(adUnitId, callback, finalPayload);
                    if (TextUtils.isEmpty(finalPayload)) {
                        loadNative(context, adUnitId, preloadMap, listener);
                    } else {
                        loadNativeWithBid(context, adUnitId, finalPayload, preloadMap, listener);
                    }
                }
            });
        } catch (Throwable e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "MintegralAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private class InnerAdListener implements NativeListener.NativeAdListener {

        private String adUnitId;
        private NativeAdCallback callback;
        private MBNativeHandler nativeHandler;
        private MBBidNativeHandler bidNativeHandler;
        private String bidToken;

        private InnerAdListener(String adUnitId, NativeAdCallback callback, String payload) {
            this.adUnitId = adUnitId;
            this.callback = callback;
            this.bidToken = payload;
        }

        void setNativeAd(MBNativeHandler nativeHandler) {
            this.nativeHandler = nativeHandler;
        }

        void setBidNativeAd(MBBidNativeHandler bidNativeHandler) {
            this.bidNativeHandler = bidNativeHandler;
        }

        @Override
        public void onAdLoaded(List<Campaign> campaigns, int template) {
            AdLog.getSingleton().LogD("MintegralNative ad onAdLoaded: " + adUnitId);
            if (campaigns == null || campaigns.isEmpty() || campaigns.get(0) == null) {
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "MintegralAdapter", "NativeAd Load Failed: No Fill"));
                }
                return;
            }
            if (callback != null) {
                Campaign campaign = campaigns.get(0);
                MintegralNativeAdsConfig nativeAdsConfig = new MintegralNativeAdsConfig();
                nativeAdsConfig.setNativeHandler(nativeHandler);
                nativeAdsConfig.setBidNativeHandler(bidNativeHandler);
                nativeAdsConfig.setBidToken(bidToken);
                nativeAdsConfig.setCampaign(campaign);
                AdnAdInfo adInfo = new AdnAdInfo();
                adInfo.setAdnNativeAd(nativeAdsConfig);
                adInfo.setDesc(campaign.getAppDesc());
                adInfo.setType(MediationInfo.MEDIATION_ID_14);
                adInfo.setTitle(campaign.getAppName());
                adInfo.setCallToActionText(campaign.getAdCall());
                adInfo.setStarRating(campaign.getRating());
                String iconUrl = campaign.getIconUrl();
                if (!TextUtils.isEmpty(iconUrl)) {
                    downloadRes(adInfo, iconUrl, callback);
                } else {
                    callback.onNativeAdLoadSuccess(adInfo);
                }
            }
        }

        @Override
        public void onAdLoadError(String message) {
            AdLog.getSingleton().LogD("MintegralAdapter Native Ad onAdLoadError: " + adUnitId + ", error: " + message);
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "MintegralAdapter", message));
            }
        }

        @Override
        public void onAdClick(Campaign campaign) {
            AdLog.getSingleton().LogD("MintegralAdapter Native Ad onAdClick: " + adUnitId);
            if (callback != null) {
                callback.onNativeAdAdClicked();
            }
        }

        @Override
        public void onAdFramesLoaded(List<Frame> list) {
            AdLog.getSingleton().LogD("MintegralAdapter Native Ad onAdFramesLoaded: " + adUnitId);
        }

        @Override
        public void onLoggingImpression(int adsourceType) {
            AdLog.getSingleton().LogD("MintegralAdapter Native Ad onLoggingImpression: " + adUnitId);
            if (callback != null) {
                callback.onNativeAdImpression();
            }
        }
    }

    private void loadNative(Context context, String adUnitId, Map<String, Object> preloadMap, InnerAdListener listener) {
        MintegralSingleTon.getInstance().removeBidAdUnit(adUnitId);
        MBNativeHandler nativeHandler = new MBNativeHandler(preloadMap, context);
        nativeHandler.setAdListener(listener);
        listener.setNativeAd(nativeHandler);
        nativeHandler.load();
    }

    private void loadNativeWithBid(Context context, String adUnitId, String payload, Map<String, Object> preloadMap, InnerAdListener listener) {
        MintegralSingleTon.getInstance().putBidAdUnit(adUnitId, payload);
        MBBidNativeHandler nativeHandler = new MBBidNativeHandler(preloadMap, context);
        nativeHandler.setAdListener(listener);
        listener.setBidNativeAd(nativeHandler);
        nativeHandler.bidLoad(payload);
    }

    public void registerNativeView(final String adUnitId, final NativeAdView adView, final AdnAdInfo adInfo,
                                   final NativeAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof MintegralNativeAdsConfig)) {
                        AdLog.getSingleton().LogE("MintegralAdapter NativeAd not ready");
                        return;
                    }
                    final MintegralNativeAdsConfig nativeAdsConfig = (MintegralNativeAdsConfig) adInfo.getAdnNativeAd();
                    if (nativeAdsConfig == null) {
                        return;
                    }
                    Campaign campaign = nativeAdsConfig.getCampaign();
                    MediaView mediaView = adView.getMediaView();
                    if (mediaView != null) {
                        MBMediaView adnMediaView = new MBMediaView(adView.getContext());
                        adnMediaView.setOnMediaViewListener(new OnMBMediaViewListenerPlus() {
                            @Override
                            public void onEnterFullscreen() {
                            }

                            @Override
                            public void onExitFullscreen() {
                            }

                            @Override
                            public void onStartRedirection(Campaign campaign, String url) {
                            }

                            @Override
                            public void onFinishRedirection(Campaign campaign, String url) {
                            }

                            @Override
                            public void onRedirectionFailed(Campaign campaign, String url) {
                            }

                            @Override
                            public void onVideoAdClicked(Campaign campaign) {
                                AdLog.getSingleton().LogD("MintegralAdapter Native Ad onVideoAdClicked: " + adUnitId);
                                if (callback != null) {
                                    callback.onNativeAdAdClicked();
                                }
                            }

                            @Override
                            public void onVideoStart() {
                            }

                            @Override
                            public void onVideoComplete() {
                            }
                        });
                        adnMediaView.setNativeAd(campaign);
                        mediaView.removeAllViews();
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        mediaView.addView(adnMediaView, layoutParams);
                        nativeAdsConfig.setMBMediaView(adnMediaView);

                        MBAdChoice adChoice = new MBAdChoice(adView.getContext());
                        RelativeLayout.LayoutParams choiceParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        choiceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.ALIGN_PARENT_RIGHT);
                        adChoice.setLayoutParams(choiceParams);
                        adChoice.setCampaign(campaign);
                        mediaView.addView(adChoice);
                    }

                    String iconUrl = campaign.getIconUrl();
                    AdIconView iconView = adView.getAdIconView();
                    if (!TextUtils.isEmpty(iconUrl) && iconView != null) {
                        iconView.removeAllViews();
                        final ImageView adnIconView = new ImageView(adView.getContext());
                        iconView.addView(adnIconView);
                        adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                        adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                        Bitmap content = ImageUtils.getBitmap(IOUtil.getFileInputStream(Cache.getCacheFile(adView.getContext(),
                                iconUrl, null)));
                        adnIconView.setImageBitmap(content);
                    }
                    String bidToken = nativeAdsConfig.getBidToken();
                    if (TextUtils.isEmpty(bidToken)) {
                        MBNativeHandler nativeHandler = nativeAdsConfig.getNativeHandler();
                        if (mediaView != null) {
                            nativeHandler.registerView(mediaView, campaign);
                        }
                        if (iconView != null) {
                            nativeHandler.registerView(iconView, campaign);
                        }
                        View actionView = adView.getCallToActionView();
                        if (actionView != null) {
                            nativeHandler.registerView(actionView, campaign);
                        }
                    } else {
                        MBBidNativeHandler nativeHandler = nativeAdsConfig.getBidNativeHandler();
                        if (mediaView != null) {
                            nativeHandler.registerView(mediaView, campaign);
                        }
                        if (iconView != null) {
                            nativeHandler.registerView(iconView, campaign);
                        }
                        View actionView = adView.getCallToActionView();
                        if (actionView != null) {
                            nativeHandler.registerView(actionView, campaign);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    void downloadRes(final AdnAdInfo adInfo, final String iconUrl, final NativeAdCallback callback) {
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File cacheFile = Cache.getCacheFile(MediationUtil.getContext(), iconUrl, null);
                    if (cacheFile == null || !cacheFile.exists()) {
                        cacheFile = ResDownloader.downloadFile(iconUrl);
                    }
                    if (callback != null) {
                        callback.onNativeAdLoadSuccess(adInfo);
                    }
                    AdLog.getSingleton().LogD("MintegralAdapter Native Ad download icon: " + cacheFile);
                } catch (Throwable e) {
                    AdLog.getSingleton().LogD("MintegralAdapter Native Ad download icon error: " + e.getMessage());
                    if (callback != null) {
                        callback.onNativeAdLoadSuccess(adInfo);
                    }
                }
            }
        });
    }

    public void destroyAd(String adUnitId, final AdnAdInfo adInfo) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof MintegralNativeAdsConfig)) {
                        AdLog.getSingleton().LogE("MintegralAdapter NativeAd destroyAd failed");
                        return;
                    }
                    MintegralNativeAdsConfig nativeAdsConfig = (MintegralNativeAdsConfig) adInfo.getAdnNativeAd();
                    if (nativeAdsConfig == null) {
                        return;
                    }
                    MBNativeHandler nativeHandler = nativeAdsConfig.getNativeHandler();
                    if (nativeHandler != null) {
                        nativeHandler.release();
                    }
                    MBBidNativeHandler bidNativeHandler = nativeAdsConfig.getBidNativeHandler();
                    if (bidNativeHandler != null) {
                        bidNativeHandler.bidRelease();
                    }
                    MBMediaView mbMediaView = nativeAdsConfig.getMBMediaView();
                    if (mbMediaView != null) {
                        mbMediaView.destory();
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

}
