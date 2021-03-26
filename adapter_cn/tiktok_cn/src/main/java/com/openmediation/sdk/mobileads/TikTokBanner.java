// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class TikTokBanner extends CustomBannerEvent implements TTAdNative.NativeExpressAdListener {
    private TTAdNative mTTAdNative;
    private TTNativeExpressAd mTTAd;
    private View mBannerView;
    private Activity mActivity;

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        this.mActivity = activity;
        initTTSDKConfig(activity, config, new TTAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                int[] size = getAdSize(activity, config);
                int width = size[0], height = size[1];
                loadBannerAd(activity, mInstancesKey, width, height);
            }

            @Override
            public void onFailed(int code, String msg) {
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, code, msg));
            }
        });
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_13;
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        if (mTTAd != null) {
            mTTAd.destroy();
        }
    }

    private void loadBannerAd(Activity activity, String codeId, int width, int height) {
        try {
            if (mTTAdNative == null) {
                mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
            }
            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    .setSupportDeepLink(true)
                    .setAdCount(1)
                    .setExpressViewAcceptedSize(width, height)
                    .build();
            mTTAdNative.loadBannerExpressAd(adSlot, this);
        } catch (Exception e) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Unknown Error"));
        }
    }

    private void initTTSDKConfig(Activity activity, Map<String, String> config, TTAdManagerHolder.InitCallback callback) {
        TTAdManagerHolder.init(activity.getApplication(), config.get("AppKey"), callback);
    }

    @Override
    public void onError(int i, String s) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, i, s));
    }

    @Override
    public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
        if (isDestroyed) {
            return;
        }
        if (list == null || list.size() == 0) {
            return;
        }
        mTTAd = list.get(0);
        bindDislike(mActivity, mTTAd);
        mTTAd.setExpressInteractionListener(new InnerAdInteractionListener(TikTokBanner.this));
        mTTAd.render();
    }

    private static class InnerAdInteractionListener implements TTNativeExpressAd.ExpressAdInteractionListener {

        private final WeakReference<TikTokBanner> mReference;

        private InnerAdInteractionListener(TikTokBanner banner) {
            mReference = new WeakReference<>(banner);
        }

        @Override
        public void onAdClicked(View view, int type) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            TikTokBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            banner.onInsClicked();
        }

        @Override
        public void onAdShow(View view, int type) {
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            TikTokBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            banner.onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokBanner", code, msg));
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            TikTokBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            banner.mBannerView = view;
            banner.onInsReady(view);
        }

    }

    private void bindDislike(Activity activity, TTNativeExpressAd ad) {
        if (activity == null || ad == null) {
            return;
        }
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int position, String value) {
                if (mBannerView != null && mBannerView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) mBannerView.getParent()).removeView(mBannerView);
                    mBannerView = null;
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onRefuse() {

            }
        });
    }

    private int[] getAdSize(Activity activity, Map<String, String> config) {
        String desc = getBannerDesc(config);
        int widthDp = 320;
        int heightDp = 50;

        if (DESC_RECTANGLE.equals(desc)) {
            widthDp = 300;
            heightDp = 250;
        } else if (DESC_SMART.equals(desc) && isLargeScreen(activity)) {
            widthDp = 728;
            heightDp = 90;
        }
        return new int[]{widthDp, heightDp};
    }

}
