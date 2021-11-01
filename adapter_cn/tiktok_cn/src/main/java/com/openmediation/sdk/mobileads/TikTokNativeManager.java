// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TikTokNativeManager {
    private TTAdNative mTTAdNative;

    private final ConcurrentHashMap<TTNativeExpressAd, View> mNativeView;

    private static class Holder {
        private static final TikTokNativeManager INSTANCE = new TikTokNativeManager();
    }

    private TikTokNativeManager() {
        mNativeView = new ConcurrentHashMap<>();
    }

    public static TikTokNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final NativeAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, new TTAdManagerHolder.InitCallback() {
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

    public void loadAd(final String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        try {
            if (mTTAdNative == null) {
                mTTAdNative = TTAdManagerHolder.getInstance().getAdManager().createAdNative(MediationUtil.getContext());
            }
            int width = 0;
            int height = 0;
            if (extras != null) {
                try {
                    width = Integer.parseInt(extras.get("width").toString());
                } catch (Exception ignored) {
                }
                try {
                    height = Integer.parseInt(extras.get("height").toString());
                } catch (Exception ignored) {
                }
            }
            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(adUnitId)
                    .setAdCount(1) //ad count from 1 to 3
                    .setExpressViewAcceptedSize(width, height) // dp
                    .build();
            mTTAdNative.loadNativeExpressAd(adSlot, new InnerAdListener(adUnitId, callback));
        } catch (Exception e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "Unknown Error"));
            }
        }
    }

    public void destroyAd(String adUnitId, AdnAdInfo adInfo) {
        if (adInfo != null && adInfo.getAdnNativeAd() instanceof TTNativeExpressAd) {
            TTNativeExpressAd ad = (TTNativeExpressAd) adInfo.getAdnNativeAd();
            ad.destroy();
            mNativeView.remove(ad);
        }
    }

    private class InnerAdListener implements TTAdNative.NativeExpressAdListener {

        private String mAdUnitId;
        private NativeAdCallback mAdCallback;

        private InnerAdListener(String adUnitId, NativeAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onError(int code, String msg) {
            if (mAdCallback != null) {
                mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", code, msg));
            }
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
            if (list == null || list.isEmpty() || list.get(0) == null) {
                if (mAdCallback != null) {
                    mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "No Fill"));
                }
                return;
            }
            final TTNativeExpressAd ad = list.get(0);
            ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                @Override
                public void onAdClicked(View view, int type) {
                    if (mAdCallback != null) {
                        mAdCallback.onNativeAdAdClicked();
                    }
                }

                @Override
                public void onAdShow(View view, int type) {
                    AdLog.getSingleton().LogD("TikTok NativeAd onAdShow");
                    bindDislike(MediationUtil.getActivity(), mAdUnitId, ad);
                    if (mAdCallback != null) {
                        mAdCallback.onNativeAdImpression();
                    }
                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    if (mAdCallback != null) {
                        mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_NATIVE, "TikTokAdapter", "No Fill"));
                    }
                }

                @Override
                public void onRenderSuccess(View view, float width, float height) {
                    mNativeView.put(ad, view);
                    AdnAdInfo adInfo = new AdnAdInfo();
                    adInfo.setType(MediationInfo.MEDIATION_ID_13);
                    adInfo.setTemplateRender(true);
                    adInfo.setView(view);
                    adInfo.setAdnNativeAd(ad);
                    if (mAdCallback != null) {
                        mAdCallback.onNativeAdLoadSuccess(adInfo);
                    }
                }
            });
            ad.render();
        }
    }

    private void bindDislike(Activity activity, final String adUnitId, final TTNativeExpressAd ad) {
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {
            }

            @Override
            public void onSelected(int position, String value, boolean enforce) {
                if (mNativeView.containsKey(ad)) {
                    View view = mNativeView.remove(ad);
                    if (view != null && view.getParent() instanceof ViewGroup) {
                        ((ViewGroup) view.getParent()).removeView(view);
                        view = null;
                    }
                }
            }

            @Override
            public void onCancel() {
            }

        });
    }

}
