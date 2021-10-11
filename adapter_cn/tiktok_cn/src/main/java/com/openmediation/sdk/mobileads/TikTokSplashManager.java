// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TikTokSplashManager {
    private static final String TAG = "TikTok: ";

    private static final String CONFIG_TIMEOUT = "Timeout";
    private static final String CONFIG_WIDTH = "Width";
    private static final String CONFIG_HEIGHT = "Height";

    private TTAdNative mTTAdNative;

    private final ConcurrentHashMap<String, TTSplashAd> mSplashAds;

    private static class Holder {
        private static final TikTokSplashManager INSTANCE = new TikTokSplashManager();
    }

    private TikTokSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static TikTokSplashManager getInstance() {
        return TikTokSplashManager.Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final SplashAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, new TTAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSplashAdInitSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", code, msg));
                }
            }
        });
    }

    public void loadAd(final Context context, String adUnitId, final Map<String, Object> config, SplashAdCallback callback) {
        int fetchDelay;
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT).toString());
        } catch(Exception ignored) {
            fetchDelay = 0;
        }
        int width = 0;
        try {
            width = Integer.parseInt(config.get(CONFIG_WIDTH).toString());
        } catch(Exception ignored) {
        }
        if (width <= 0) {
            width = TTAdManagerHolder.getScreenWidth(context);
        }
        int height = 0;
        try {
            height = Integer.parseInt(config.get(CONFIG_HEIGHT).toString());
        } catch(Exception ignored) {
        }
        if (height <= 0) {
            height = TTAdManagerHolder.getScreenHeight(context);
        }
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(adUnitId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(width, height)
                .build();
        if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.getInstance().getAdManager().createAdNative(context);
        }
        InnerSplashAdListener listener = new InnerSplashAdListener(adUnitId, callback);
        if (fetchDelay <= 0) {
            mTTAdNative.loadSplashAd(adSlot, listener);
        } else {
            mTTAdNative.loadSplashAd(adSlot, listener, fetchDelay);
        }
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            TTSplashAd ttSplashAd = mSplashAds.remove(adUnitId);
            ttSplashAd = null;
        }
    }

    public void showAd(final String adUnitId, final ViewGroup container, final SplashAdCallback callback) {
        if (container == null) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", "Splash container is null, please use \"SplashAd.showAd(ViewGroup)\""));
            }
            return;
        }
        if (!isAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", "SplashAd not ready"));
            }
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    TTSplashAd ttSplashAd = mSplashAds.remove(adUnitId);
                    SplashAdAdInteractionListener listener = new SplashAdAdInteractionListener(adUnitId, callback);
                    ttSplashAd.setSplashInteractionListener(listener);
                    View splashView = ttSplashAd.getSplashView();
                    if (splashView.getParent() instanceof ViewGroup) {
                        ViewGroup viewGroup = (ViewGroup) splashView.getParent();
                        viewGroup.removeView(splashView);
                    }
                    container.removeAllViews();
                    container.addView(splashView);
                } catch(Throwable e) {
                    if (callback != null) {
                        callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mSplashAds.containsKey(adUnitId);
    }

    private class InnerSplashAdListener implements TTAdNative.SplashAdListener {

        private final String mAdUnitId;
        private final SplashAdCallback mAdCallback;

        private InnerSplashAdListener(String adUnitId, SplashAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onError(int code, String message) {
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", code, message));
            }
        }

        @Override
        public void onTimeout() {
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", "Splash ad load failed: timeout"));
            }
        }

        @Override
        public void onSplashAdLoad(com.bytedance.sdk.openadsdk.TTSplashAd ttSplashAd) {
            if (ttSplashAd == null) {
                if (mAdCallback != null) {
                    mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", "Splash ad Load Failed: TTSplashAd is null"));
                }
                return;
            }
            AdLog.getSingleton().LogD(TAG + "Splash ad onSplashAdLoad: " + mAdUnitId);
            mSplashAds.put(mAdUnitId, ttSplashAd);
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadSuccess(null);
            }
        }
    }

    private static class SplashAdAdInteractionListener implements TTSplashAd.AdInteractionListener {
        private final String mAdUnitId;
        private final SplashAdCallback mAdCallback;

        private SplashAdAdInteractionListener(String adUnitId, SplashAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onAdClicked(View view, int i) {
            AdLog.getSingleton().LogD(TAG + "Splash ad onADClicked");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdAdClicked();
            }
        }

        @Override
        public void onAdShow(View view, int i) {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdShow");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdShowSuccess();
            }
        }

        @Override
        public void onAdSkip() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdSkip");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdDismissed();
            }
        }

        @Override
        public void onAdTimeOver() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdTimeOver");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdDismissed();
            }
        }
    }

}
