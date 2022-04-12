// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import static com.openmediation.sdk.mobileads.FlatAdsAdapter.BID;
import static com.openmediation.sdk.mobileads.FlatAdsSingleTon.TAG;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flatads.sdk.builder.NativeAd;
import com.flatads.sdk.callback.InitListener;
import com.flatads.sdk.callback.NativeAdListener;
import com.flatads.sdk.response.Ad;
import com.flatads.sdk.statics.ErrorCode;
import com.flatads.sdk.ui.view.NativeAdLayout;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlatAdsNativeManager {
    private static final String ADN_OBJECT = "AdnObject";
    private final ConcurrentHashMap<String, NativeAdLayout> mNativeAdLayoutMap;

    private static class Holder {
        private static final FlatAdsNativeManager INSTANCE = new FlatAdsNativeManager();
    }

    private FlatAdsNativeManager() {
        mNativeAdLayoutMap = new ConcurrentHashMap<>();
    }

    public static FlatAdsNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Map<String, Object> extras, final NativeAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        FlatAdsSingleTon.getInstance().init(appKey, new InitListener() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onNativeAdInitSuccess();
                }
            }

            @Override
            public void onFailure(int code, String msg) {
                if (callback != null) {
                    callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", code, msg));
                }
            }
        });
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        boolean bid = false;
        if (extras != null && extras.containsKey(BID) && extras.get(BID) instanceof Integer) {
            bid = ((int) extras.get(BID)) == 1;
        }
        if (bid) {
            AdnAdInfo info = null;
            NativeAd nativeAd = null;
            if (extras.get(ADN_OBJECT) instanceof AdnAdInfo) {
                info = (AdnAdInfo) extras.get(ADN_OBJECT);
                if (info != null && info.getAdnNativeAd() instanceof NativeAd) {
                    nativeAd = (NativeAd) info.getAdnNativeAd();
                }
            }
            if (nativeAd == null) {
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "No Fill"));
                }
                return;
            }
            loadNativeAdWithBid(adUnitId, info, nativeAd, callback);
        } else {
            loadNativeAd(adUnitId, callback);
        }
    }

    void loadNativeAdWithBid(String adUnitId, AdnAdInfo info, NativeAd nativeAd, NativeAdCallback callback) {
        try {
            nativeAd.setAdListener(new InnerNativeListener(adUnitId, info, nativeAd, callback));
            nativeAd.winBidding();
            nativeAd.loadAd();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd LoadFailed");
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    void loadNativeAd(String adUnitId, NativeAdCallback callback) {
        try {
            NativeAd nativeAd = new NativeAd(MediationUtil.getContext(), adUnitId);
            nativeAd.setAdListener(new InnerNativeListener(adUnitId, null, nativeAd, callback));
            nativeAd.loadAd();
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "NativeAd Load error : " + e.getMessage());
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    private static class InnerNativeListener implements NativeAdListener {
        String adUnitId;
        NativeAd nativeAd;
        NativeAdCallback callback;
        AdnAdInfo adInfo;

        private InnerNativeListener(String adUnitId, AdnAdInfo info, NativeAd nativeAd, NativeAdCallback callback) {
            this.adUnitId = adUnitId;
            this.nativeAd = nativeAd;
            this.callback = callback;
            this.adInfo = info;
        }

        @Override
        public void onAdLoadSuc(Ad ad) {
            AdLog.getSingleton().LogD(TAG, "NativeAd onAdSucLoad adUnitId : " + adUnitId);
            if (adInfo == null) {
                adInfo = new AdnAdInfo();
            }
            adInfo.setAdnNativeAd(nativeAd);
            adInfo.setDesc(ad.getDesc());
            adInfo.setType(MediationInfo.MEDIATION_ID_25);
            adInfo.setTitle(ad.getTitle());
            adInfo.setCallToActionText(ad.getAdBtn());
            if (callback != null) {
                callback.onNativeAdLoadSuccess(adInfo);
            }
        }

        @Override
        public void onAdLoadFail(ErrorCode errorCode) {
            String error = "NativeAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg();
            AdLog.getSingleton().LogE(TAG, error);
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
            }
        }

        @Override
        public void onAdExposure() {
            AdLog.getSingleton().LogD(TAG, "NativeAd onAdExposure adUnitId : " + adUnitId);
            if (callback != null) {
                callback.onNativeAdImpression();
            }
        }

        @Override
        public void onAdClick() {
            AdLog.getSingleton().LogD(TAG, "NativeAd onAdClick adUnitId : " + adUnitId);
            if (callback != null) {
                callback.onNativeAdAdClicked();
            }
        }

        @Override
        public void onAdDestroy() {
            AdLog.getSingleton().LogD(TAG, "NativeAd onAdDestroy adUnitId : " + adUnitId);
        }
    }

    void registerNativeView(String adUnitId, NativeAdView adView, AdnAdInfo adInfo, final NativeAdCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof NativeAd)) {
                        return;
                    }
                    NativeAd nativeAd = (NativeAd) adInfo.getAdnNativeAd();
                    NativeAdLayout nativeAdLayout = new NativeAdLayout(adView.getContext());
                    MediaView mediaView = adView.getMediaView();
                    com.flatads.sdk.ui.view.MediaView adnMediaView = null;
                    if (mediaView != null) {
                        adnMediaView = new com.flatads.sdk.ui.view.MediaView(adView.getContext());
                        mediaView.removeAllViews();
                        mediaView.addView(adnMediaView);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        adnMediaView.setLayoutParams(layoutParams);
                    }
                    AdIconView iconView = adView.getAdIconView();
                    ImageView adnIconView = null;
                    if (iconView != null) {
                        iconView.removeAllViews();
                        adnIconView = new ImageView(adView.getContext());
                        iconView.addView(adnIconView);
                        adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                        adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                    }
                    int count = adView.getChildCount();
                    if (count > 0) {
                        View actualView = adView.getChildAt(0);
                        adView.removeView(actualView);
                        nativeAdLayout.addView(actualView);
                        adView.addView(nativeAdLayout);
                    }
                    List<View> clickableViews = new ArrayList<>();
                    View actionView = adView.getCallToActionView();
                    if (actionView != null) {
                        clickableViews.add(actionView);
                    }
                    clickableViews.add(mediaView);
                    nativeAd.registerViewForInteraction(nativeAdLayout, adnMediaView, adnIconView, clickableViews);
                    mNativeAdLayoutMap.put(adUnitId, nativeAdLayout);
                } catch (Throwable ignored) {
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        try {
            NativeAdLayout nativeAdLayout = mNativeAdLayoutMap.remove(adUnitId);
            if (nativeAdLayout != null) {
                nativeAdLayout.destroy();
            }
        } catch (Throwable ignored) {
        }
    }

}
