// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class TikTokBanner extends CustomBannerEvent implements TTAdNative.NativeExpressAdListener {
    private static String TAG = "OM-TikTok: ";
    private TTAdNative mTTAdNative;
    private TTNativeExpressAd mTTAd;
    private View mBannerView;
    private Activity mActivity;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        this.mActivity = activity;
        initTTSDKConfig(activity, config);
        int[] size = getBannerSize(config);
        loadBannerAd(mInstancesKey, size[0], size[1]);
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

    private void loadBannerAd(String codeId, int width, int height) {
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setAdCount(1)
                .setExpressViewAcceptedSize(width, height)
                .build();
        mTTAdNative.loadBannerExpressAd(adSlot, this);
    }

    private void initTTSDKConfig(Activity activity, Map<String, String> config) {
        TTAdManagerHolder.init(activity.getApplication(), config.get("AppKey"));
        if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
        }
    }

    @Override
    public void onError(int i, String s) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Banner ad load failed " + s);
        onInsError(s);
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
        AdLog.getSingleton().LogD(TAG + "Banner ad load success ");
    }

    private static class InnerAdInteractionListener implements TTNativeExpressAd.ExpressAdInteractionListener {

        private WeakReference<TikTokBanner> mReference;

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
            AdLog.getSingleton().LogD(TAG + "onAdClicked");
        }

        @Override
        public void onAdShow(View view, int type) {
            AdLog.getSingleton().LogD(TAG + "onAdShow");
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            TikTokBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            AdLog.getSingleton().LogD(TAG + "onRenderFail " + msg + " code:" + code);
            banner.onInsError(msg);
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
            AdLog.getSingleton().LogD(TAG + "onRenderSuccess");
            banner.mBannerView = view;
            banner.onInsReady(view);
        }
    }

    /**
     * 设置广告的不喜欢，注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     */
    private void bindDislike(Activity activity, TTNativeExpressAd ad) {
        if (activity == null || ad == null) {
            return;
        }
        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                //用户选择不喜欢原因后，移除广告展示
                if (mBannerView != null && mBannerView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) mBannerView.getParent()).removeView(mBannerView);
                    mBannerView = null;
                }
            }

            @Override
            public void onCancel() {
            }
        });
    }

}
