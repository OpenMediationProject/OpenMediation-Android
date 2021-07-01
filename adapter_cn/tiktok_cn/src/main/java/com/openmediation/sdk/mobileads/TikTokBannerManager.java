// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TikTokBannerManager {
    private TTAdNative mTTAdNative;

    private final ConcurrentHashMap<String, TTNativeExpressAd> mBannerAds;
    private final ConcurrentHashMap<String, View> mBannerViews;

    private static class BannerHolder {
        private static final TikTokBannerManager INSTANCE = new TikTokBannerManager();
    }

    private TikTokBannerManager() {
        mBannerAds = new ConcurrentHashMap<>();
        mBannerViews = new ConcurrentHashMap<>();
    }

    public static TikTokBannerManager getInstance() {
        return BannerHolder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final BannerAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, new TTAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", code, msg));
                }
            }
        });
    }

    public void loadAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        int[] size = getAdSize(activity, extras);
        int width = size[0], height = size[1];
        loadBannerAd(activity, adUnitId, width, height, callback);
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId)) {
            mBannerAds.get(adUnitId).destroy();
            mBannerAds.remove(adUnitId);
            mBannerViews.remove(adUnitId);
        }
    }

    private void loadBannerAd(Activity activity, String adUnitId, int width, int height, BannerAdCallback callback) {
        try {
            if (mTTAdNative == null) {
                mTTAdNative = TTAdManagerHolder.getInstance().getAdManager().createAdNative(activity);
            }
            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(adUnitId)
                    .setSupportDeepLink(true)
                    .setAdCount(1)
                    .setExpressViewAcceptedSize(width, height)
                    .build();
            mTTAdNative.loadBannerExpressAd(adSlot, new InnerBannerAdListener(new WeakReference<>(activity), adUnitId, callback));
        } catch(Exception e) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", "Unknown Error"));
            }
        }
    }

    private class InnerBannerAdListener implements TTAdNative.NativeExpressAdListener {

        private String mAdUnitId;
        private BannerAdCallback mAdCallback;
        private WeakReference<Activity> weakReference;

        private InnerBannerAdListener(WeakReference<Activity> weakReference, String adUnitId, BannerAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
            this.weakReference = weakReference;
        }

        @Override
        public void onError(int i, String s) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", i, s));
            }
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
            if (list == null || list.size() == 0) {
                if (mAdCallback != null) {
                    mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", "No Fill"));
                }
                return;
            }
            TTNativeExpressAd ttNativeExpressAd = list.get(0);
            if (weakReference != null && weakReference.get() != null && !weakReference.get().isFinishing()) {
                bindDislike(weakReference.get(), mAdUnitId, ttNativeExpressAd);
            }
            ttNativeExpressAd.setExpressInteractionListener(new InnerAdInteractionListener(mAdUnitId, mAdCallback));
            ttNativeExpressAd.render();
            mBannerAds.put(mAdUnitId, ttNativeExpressAd);
        }
    }

    private class InnerAdInteractionListener implements TTNativeExpressAd.ExpressAdInteractionListener {

        private BannerAdCallback mAdCallback;
        private String mAdUnitId;

        private InnerAdInteractionListener(String adUnit, BannerAdCallback callback) {
            mAdUnitId = adUnit;
            this.mAdCallback = callback;
        }

        @Override
        public void onAdClicked(View view, int type) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onAdShow(View view, int type) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", code, msg));
            }
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            mBannerViews.put(mAdUnitId, view);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadSuccess(view);
            }
        }

    }

    private void bindDislike(Activity activity, final String adUnitId, TTNativeExpressAd ad) {
        if (activity == null || ad == null) {
            return;
        }
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int i, String s, boolean b) {
                if (mBannerViews.containsKey(adUnitId)) {
                    View view = mBannerViews.get(adUnitId);
                    if (view != null && view.getParent() instanceof ViewGroup) {
                        ((ViewGroup) view.getParent()).removeView(view);
                        view = null;
                    }
                    mBannerViews.remove(adUnitId);
                }
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private int[] getAdSize(Activity activity, Map<String, Object> config) {
        String desc = MediationUtil.getBannerDesc(config);
        int widthDp = 320;
        int heightDp = 50;

        if (MediationUtil.DESC_RECTANGLE.equals(desc)) {
            widthDp = 300;
            heightDp = 250;
        } else if (MediationUtil.DESC_SMART.equals(desc) && MediationUtil.isLargeScreen(activity)) {
            widthDp = 728;
            heightDp = 90;
        }
        return new int[]{widthDp, heightDp};
    }

}
