// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAd;
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAdLoadListener;
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenRequest;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TikTokSplashManager {
    private static final String TAG = "TikTokAdapter: ";

    private static final String CONFIG_TIMEOUT = "Timeout";

    private final ConcurrentHashMap<String, PAGAppOpenAd> mSplashAds;

    private static class Holder {
        private static final TikTokSplashManager INSTANCE = new TikTokSplashManager();
    }

    private TikTokSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static TikTokSplashManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, Boolean consent, Boolean ageRestricted, Boolean privacyLimit, final SplashAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, consent, ageRestricted, privacyLimit, new TTAdManagerHolder.InitCallback() {
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
        PAGAppOpenRequest request = new PAGAppOpenRequest();
        int fetchDelay;
        try {
            //App Open ad timeout recommended >=3000ms
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT).toString());
            request.setTimeout(fetchDelay);
        } catch (Throwable ignored) {
        }

        PAGAppOpenAd.loadAd(adUnitId, request, new PAGAppOpenAdLoadListener() {
            @Override
            public void onError(int code, String message) {
                AdLog.getSingleton().LogD("TikTokAdapter, SplashAd load onError code: " + code + ", message: " + message);
                if (callback != null) {
                    callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", code, message));
                }
            }

            @Override
            public void onAdLoaded(PAGAppOpenAd appOpenAd) {
                AdLog.getSingleton().LogD("TikTokAdapter, SplashAd onAdLoaded: " + appOpenAd);
                if (appOpenAd == null) {
                    if (callback != null) {
                        callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, "TikTokAdapter", "Splash ad Load Failed: TTSplashAd is null"));
                    }
                    return;
                }
                AdLog.getSingleton().LogD(TAG + "Splash ad onSplashAdLoad: " + adUnitId);
                mSplashAds.put(adUnitId, appOpenAd);
                if (callback != null) {
                    callback.onSplashAdLoadSuccess(null);
                }
            }
        });
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            PAGAppOpenAd ttSplashAd = mSplashAds.remove(adUnitId);
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
                    PAGAppOpenAd ttSplashAd = mSplashAds.remove(adUnitId);
                    ttSplashAd.setAdInteractionListener(new PAGAppOpenAdInteractionListener() {
                        @Override
                        public void onAdShowed() {
                            AdLog.getSingleton().LogD(TAG + "Splash ad onAdShow");
                            if (callback != null) {
                                callback.onSplashAdShowSuccess();
                            }
                        }

                        @Override
                        public void onAdClicked() {
                            AdLog.getSingleton().LogD(TAG + "Splash ad onADClicked");
                            if (callback != null) {
                                callback.onSplashAdAdClicked();
                            }
                        }

                        @Override
                        public void onAdDismissed() {
                            AdLog.getSingleton().LogD(TAG + "Splash ad onAdDismissed");
                            if (callback != null) {
                                callback.onSplashAdDismissed();
                            }
                        }
                    });
                    ttSplashAd.show(activity);
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

}
