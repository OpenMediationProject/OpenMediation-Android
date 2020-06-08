// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import static android.widget.ListPopupWindow.MATCH_PARENT;

public class TikTokNative extends CustomNativeEvent implements TTAdNative.NativeExpressAdListener {
    private static String TAG = "OM-TikTok: ";
    private Activity mActivity;

    private TTAdNative mTTAdNative;
    private TTNativeExpressAd mTTAd;

    private View mNativeView;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        this.mActivity = activity;
        initTTSDKConfig(activity, config);
        int[] size = getNativeSize(config);
        loadNativeAd(mInstancesKey, 640, 320);
    }


    private void loadNativeAd(String codeId, int width, int height) {
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setAdCount(1)
                .setExpressViewAcceptedSize(width, height)
                .build();
        mTTAdNative.loadNativeExpressAd(adSlot, this);
    }

    private void initTTSDKConfig(Activity activity, Map<String, String> config) {
        TTAdManagerHolder.init(activity.getApplication(), config.get("AppKey"));
        if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
        }
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        if (mNativeView == null) return;
        if (adView.getMediaView() != null) {
            adView.getMediaView().removeAllViews();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            adView.getMediaView().addView(mNativeView, lp);

        }
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_1;
    }

    @Override
    public void destroy(Activity activity) {
        if (mTTAd != null) {
            mTTAd.destroy();
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
        mTTAd.setExpressInteractionListener(new TikTokNative.InnerAdInteractionListener(TikTokNative.this));
        mTTAd.render();
        AdLog.getSingleton().LogD(TAG + "Banner ad load success ");
    }

    private static class InnerAdInteractionListener implements TTNativeExpressAd.ExpressAdInteractionListener {

        private WeakReference<TikTokNative> mReference;

        private InnerAdInteractionListener(TikTokNative banner) {
            mReference = new WeakReference<>(banner);
        }

        @Override
        public void onAdClicked(View view, int type) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            TikTokNative banner = mReference.get();
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
            TikTokNative banner = mReference.get();
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
            TikTokNative banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            AdLog.getSingleton().LogD(TAG + "onRenderSuccess");
            banner.mNativeView = view;
            banner.mAdInfo.setDesc("");
            banner.mAdInfo.setType(2);
            banner.mAdInfo.setCallToActionText("");
            banner.mAdInfo.setTitle("");
            banner.mAdInfo.setTemplate(true);
            banner.onInsReady(banner.mAdInfo);
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
                if (mNativeView != null && mNativeView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) mNativeView.getParent()).removeView(mNativeView);
                    mNativeView = null;
                }
            }

            @Override
            public void onCancel() {
            }
        });
    }
}
