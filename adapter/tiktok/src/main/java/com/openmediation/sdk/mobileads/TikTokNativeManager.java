// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.bytedance.sdk.openadsdk.adapter.MediationAdapterUtil;
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
    private TTAdNative mTTAdNative;

    private static class Holder {
        private static final TikTokNativeManager INSTANCE = new TikTokNativeManager();
    }

    private TikTokNativeManager() {
    }

    public static TikTokNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, Boolean consent, Boolean ageRestricted, final NativeAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, consent, ageRestricted, new TTAdManagerHolder.InitCallback() {
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
            if (mTTAdNative == null) {
                mTTAdNative = TTAdManagerHolder.getInstance().getAdManager().createAdNative(context);
            }
            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(adUnitId)
                    .setAdCount(1) //ad count from 1 to 3
                    .build();
            mTTAdNative.loadFeedAd(adSlot, new InnerAdListener(adUnitId, callback));
        } catch(Exception e) {
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
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof TTFeedAd)) {
                return;
            }
            TTFeedAd feedAd = (TTFeedAd) adInfo.getAdnNativeAd();
            MediaView mediaView = adView.getMediaView();
            ArrayList<View> images = new ArrayList<>();
            if (mediaView != null) {
                com.bytedance.sdk.openadsdk.adapter.MediaView adnMediaView = new com.bytedance.sdk.openadsdk.adapter.MediaView(adView.getContext());
                /** Add Native Feed Main View */
                MediationAdapterUtil.addNativeFeedMainView(adView.getContext(), feedAd.getImageMode(), adnMediaView, feedAd.getAdView(), feedAd.getImageList());
                mediaView.removeAllViews();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adnMediaView.setLayoutParams(layoutParams);
                mediaView.addView(adnMediaView);
                images.add(adnMediaView);
            }
            TTImage icon = feedAd.getIcon();
            if (icon != null && icon.isValid() && adView.getAdIconView() != null) {
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
            //notice! This involves advertising billing and must be called correctly. convertView must use ViewGroup.
            feedAd.registerViewForInteraction(adView, images, clickViewList, creativeViewList, null, new TTNativeAd.AdInteractionListener() {

                @Override
                public void onAdClicked(View view, TTNativeAd ttNativeAd) {
                }

                @Override
                public void onAdCreativeClick(View view, TTNativeAd ttNativeAd) {
                    if (callback != null) {
                        callback.onNativeAdAdClicked();
                    }
                }

                @Override
                public void onAdShow(TTNativeAd ttNativeAd) {
                    AdLog.getSingleton().LogD("TikTok NativeAd onAdShow");
                    if (callback != null) {
                        callback.onNativeAdImpression();
                    }
                }
            });
        } catch(Throwable e) {
            AdLog.getSingleton().LogE("TikTokNativeManager", "Native register error: " + e.getMessage());
        }
    }

    public void destroyAd(String adUnitId, AdnAdInfo adInfo) {
    }

    private class InnerAdListener implements TTAdNative.FeedAdListener {

        private String mAdUnitId;
        private NativeAdCallback mAdCallback;

        private InnerAdListener(String adUnitId, NativeAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onError(int i, String s) {
            if (mAdCallback != null) {
                mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", i, s));
            }
        }

        @Override
        public void onFeedAdLoad(final List<TTFeedAd> list) {
            if (list == null || list.isEmpty() || list.get(0) == null) {
                if (mAdCallback != null) {
                    mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "No Fill"));
                }
                return;
            }
            WorkExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    downloadRes(mAdUnitId, list.get(0), mAdCallback);
                }
            });
        }
    }

    private void downloadRes(String adUnitId, final TTFeedAd ad, final NativeAdCallback callback) {
        try {
            TTImage icon = ad.getIcon();
            if (icon != null && icon.isValid()) {
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
            adInfo.setDesc(ad.getDescription());
            adInfo.setType(MediationInfo.MEDIATION_ID_13);
            adInfo.setTitle(ad.getTitle());
            adInfo.setCallToActionText(ad.getButtonText());
            if (callback != null) {
                callback.onNativeAdLoadSuccess(adInfo);
            }
        } catch(Exception e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "NativeAd Load Failed: " + e.getMessage()));
            }
        }
    }

}
