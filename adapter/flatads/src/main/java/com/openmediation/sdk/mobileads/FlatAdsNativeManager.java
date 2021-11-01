// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flatads.sdk.callback.AdShowListener;
import com.flatads.sdk.response.AdContent;
import com.flatads.sdk.ui.NativeAdLayout;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.openmediation.sdk.mobileads.FlatAdsAdapter.BID;

public class FlatAdsNativeManager {
    private static final String TAG = "FlatAdsNativeManager";
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
        FlatAdsSingleTon.getInstance().init(appKey, new FlatAdsSingleTon.InitListener() {
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
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", error));
                }
            }
        });
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        boolean bid = false;
        if (extras.containsKey(BID) && extras.get(BID) instanceof Integer) {
            bid = ((int) extras.get(BID)) == 1;
        }
        if (bid) {
            final FlatAdsNativeAdsConfig adsConfig = FlatAdsSingleTon.getInstance().getNativeAd(adUnitId);
            if (adsConfig == null || adsConfig.getNativeAd() == null) {
                String error = FlatAdsSingleTon.getInstance().getError(adUnitId);
                if (TextUtils.isEmpty(error)) {
                    error = "No Fill";
                }
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "FlatAdsAdapter", error));
                }
                return;
            }
            FlatAdsSingleTon.getInstance().loadNativeAdWithBid(adUnitId, callback);
        } else {
            FlatAdsSingleTon.getInstance().loadNativeAd(adUnitId, callback);
        }
    }

    void registerNativeView(String adUnitId, NativeAdView adView, final NativeAdCallback callback) {
        try {
            final FlatAdsNativeAdsConfig adsConfig = FlatAdsSingleTon.getInstance().getNativeAd(adUnitId);
            if (adsConfig == null) {
                return;
            }
            AdContent adContent = adsConfig.getAdContent();
            NativeAdLayout nativeAdLayout = new NativeAdLayout(adView.getContext());

//            if (adView.getTitleView() instanceof TextView) {
//                nativeAdLayout.setTitle((TextView) adView.getTitleView());
//            }
//            if (adView.getDescView() instanceof TextView) {
//                nativeAdLayout.setDescribe((TextView) adView.getDescView());
//            }
            if (adView.getCallToActionView() instanceof TextView) {
                nativeAdLayout.setButton((TextView) adView.getCallToActionView());
            }
            nativeAdLayout.setContainer(adView);
            if (adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();
                com.flatads.sdk.ui.MediaView adnMediaView = new com.flatads.sdk.ui.MediaView(adView.getContext());
                nativeAdLayout.setMedia(adnMediaView);
                // TODO
                mediaView.removeAllViews();
                mediaView.addView(adnMediaView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adnMediaView.setLayoutParams(layoutParams);
            }
            if (adView.getAdIconView() != null) {
                AdIconView iconView = adView.getAdIconView();
                iconView.removeAllViews();
                ImageView adnIconView = new ImageView(adView.getContext());
                iconView.addView(adnIconView);
                adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                nativeAdLayout.setIcon(adnIconView);
            }
            nativeAdLayout.setAdShowListener(new AdShowListener() {
                @Override
                public void onAdShowed() {
                    AdLog.getSingleton().LogD("FlatAdsNativeManager", "NativeAd onAdShowed");
                }

                @Override
                public boolean onAdClicked() {
                    AdLog.getSingleton().LogD("FlatAdsNativeManager", "NativeAd onAdClicked");
                    if (callback != null) {
                        callback.onNativeAdAdClicked();
                    }
                    return false;
                }

                @Override
                public void onAdClosed() {

                }
            });
            nativeAdLayout.showAd(adContent);
            mNativeAdLayoutMap.put(adUnitId, nativeAdLayout);
        } catch (Throwable ignored) {
        }
    }

    void destroyAd(String adUnitId) {
        FlatAdsSingleTon.getInstance().destroyNativeAd(adUnitId);
        NativeAdLayout nativeAdLayout = mNativeAdLayoutMap.remove(adUnitId);
        if (nativeAdLayout != null) {
            nativeAdLayout.destroy();
        }
    }

    void onResume() {
        for (NativeAdLayout adLayout : mNativeAdLayoutMap.values()) {
            adLayout.resume();
        }
    }

    void onPause() {
        for (NativeAdLayout adLayout : mNativeAdLayoutMap.values()) {
            adLayout.pause();
//            adLayout.stop();
        }
    }

}
