// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppOpenAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TikTokSplashManager {
    private static final String TAG = "TikTok: ";

    private static final String CONFIG_TIMEOUT = "Timeout";

    private TTAdNative mTTAdNative;

    private final ConcurrentHashMap<String, TTAppOpenAd> mSplashAds;

    private static class Holder {
        private static final TikTokSplashManager INSTANCE = new TikTokSplashManager();
    }

    private TikTokSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static TikTokSplashManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, Boolean consent, Boolean ageRestricted, final SplashAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, consent, ageRestricted, new TTAdManagerHolder.InitCallback() {
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
        } catch (Throwable ignored) {
            fetchDelay = 0;
        }
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(adUnitId)
                .build();
        if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.getInstance().getAdManager().createAdNative(context);
        }
        InnerSplashAdListener listener = new InnerSplashAdListener(adUnitId, callback);
        mTTAdNative.loadAppOpenAd(adSlot, listener, Math.max(fetchDelay, 0));
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            TTAppOpenAd ttSplashAd = mSplashAds.remove(adUnitId);
            ttSplashAd = null;
        }
    }

    public void showAd(final String adUnitId, final Activity activity, final SplashAdCallback callback) {
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
                    TTAppOpenAd ttSplashAd = mSplashAds.remove(adUnitId);
                    SplashAdAdInteractionListener listener = new SplashAdAdInteractionListener(callback);
                    ttSplashAd.setOpenAdInteractionListener(listener);
                    ttSplashAd.showAppOpenAd(activity);
                } catch (Throwable e) {
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

    private class InnerSplashAdListener implements TTAdNative.AppOpenAdListener {

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
        public void onAppOpenAdLoaded(TTAppOpenAd ttAppOpenAd) {
            if (ttAppOpenAd == null) {
                if (mAdCallback != null) {
                    mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", "Splash ad Load Failed: TTSplashAd is null"));
                }
                return;
            }
            AdLog.getSingleton().LogD(TAG + "Splash ad onSplashAdLoad: " + mAdUnitId);
            mSplashAds.put(mAdUnitId, ttAppOpenAd);
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadSuccess(null);
            }
        }
    }

    private static class SplashAdAdInteractionListener implements TTAppOpenAd.AppOpenAdInteractionListener {
        private final SplashAdCallback mAdCallback;

        private SplashAdAdInteractionListener(SplashAdCallback callback) {
            this.mAdCallback = callback;
        }

        @Override
        public void onAdShow() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdShow");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdShowSuccess();
            }
        }

        @Override
        public void onAdClicked() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onADClicked");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdAdClicked();
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
        public void onAdCountdownToZero() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdCountdownToZero");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdDismissed();
            }
        }

    }

}
