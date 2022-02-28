// Copyright 2022 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.splash.SplashAdDisplayListener;
import com.huawei.hms.ads.splash.SplashView;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HwAdsSplashManager {

    private final ConcurrentHashMap<String, SplashView> mSplashAds;

    private static class Holder {
        private static final HwAdsSplashManager INSTANCE = new HwAdsSplashManager();
    }

    private HwAdsSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static HwAdsSplashManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final SplashAdCallback callback) {
        HwAdsSingleTon.getInstance().initSDK(context.getApplicationContext(), new HwAdsSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSplashAdInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", msg));
                }
            }
        });
    }

    private int getScreenOrientation(Context context) {
        Configuration config = context.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    public void loadAd(final Activity context, final String adUnitId, final Map<String, Object> extras, final SplashAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ViewGroup container = null;
                    if (extras != null && extras.containsKey("ViewGroup")) {
                        container = (ViewGroup) extras.get("ViewGroup");
                    }
                    if (container == null) {
                        AdLog.getSingleton().LogD("HwAdsSplashManager SplashAD onAdFailedToLoad: " + adUnitId + ", No Container");
                        if (callback != null) {
                            callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter","No Container"));
                        }
                        return;
                    }
                    int orientation = getScreenOrientation(context);
                    AdParam adParam = new AdParam.Builder().build();
                    final SplashView splashView = new SplashView(context);
                    container.addView(splashView);

                    splashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);
                    splashView.setAdDisplayListener(new SplashAdDisplayListener() {
                        @Override
                        public void onAdShowed() {
//                            mSplashAds.put(adUnitId, splashView);
                            AdLog.getSingleton().LogD("HwAdsSplashManager onAdShowed: " + adUnitId);
                            if (callback != null) {
                                callback.onSplashAdShowSuccess();
                            }
                        }

                        @Override
                        public void onAdClick() {
                            AdLog.getSingleton().LogD("HwAdsSplashManager onAdClick: " + adUnitId);
                            if (callback != null) {
                                callback.onSplashAdAdClicked();
                            }
                        }
                    });
                    splashView.load(adUnitId, orientation, adParam, new SplashView.SplashAdLoadListener() {
                        @Override
                        public void onAdFailedToLoad(int i) {
                            super.onAdFailedToLoad(i);
                            AdLog.getSingleton().LogD("HwAdsSplashManager onAdFailedToLoad: " + adUnitId + ", code: " + i);
                            if (callback != null) {
                                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                        AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", i, ""));
                            }
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            AdLog.getSingleton().LogD("HwAdsSplashManager onAdLoaded: " + adUnitId);
//                            mSplashAds.put(adUnitId, splashView);
                            if (callback != null) {
                                callback.onSplashAdLoadSuccess(null);
                            }
                        }

                        @Override
                        public void onAdDismissed() {
                            super.onAdDismissed();
                            AdLog.getSingleton().LogD("HwAdsSplashManager onAdDismissed: " + adUnitId);
                            destroyAd(splashView, adUnitId);
                            if (callback != null) {
                                callback.onSplashAdDismissed();
                            }
                        }
                    });
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    public void destroyAd(final SplashView splashView, final String adUnitId) {
        try {
            if (splashView != null) {
                splashView.destroyView();
            }
            mSplashAds.remove(adUnitId);
        } catch (Throwable ignored) {
        }
    }

    public void destroyAd(final String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return;
        }
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    SplashView splashView = mSplashAds.remove(adUnitId);
                    if (splashView != null) {
                        splashView.destroyView();
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

//    public void onPause() {
//        try {
//            for (SplashView splashView : mSplashAds.values()) {
//                if (splashView != null) {
//                    splashView.pauseView();
//                }
//            }
//        } catch (Throwable ignored) {
//        }
//    }
//
//    public void onResume() {
//        try {
//            for (SplashView splashView : mSplashAds.values()) {
//                if (splashView != null) {
//                    splashView.resumeView();
//                }
//            }
//        } catch (Throwable ignored) {
//        }
//    }

    public boolean isAdAvailable(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mSplashAds.containsKey(adUnitId)) {
            SplashView splashView = mSplashAds.get(adUnitId);
            if (splashView == null) {
                return false;
            }
            return splashView.isLoaded();
        }
        return false;
    }

    public void showAd(final String adUnitId, final ViewGroup container, final SplashAdCallback callback) {
        if (container == null) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", "Splash container is null, please use \"SplashAd.showAd(ViewGroup)\""));
            }
            return;
        }
//        MediationUtil.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    SplashView splashView = mSplashAds.get(adUnitId);
//                    if (splashView.getParent() instanceof ViewGroup) {
//                        ViewGroup viewGroup = (ViewGroup) splashView.getParent();
//                        viewGroup.removeView(splashView);
//                    }
//                    container.removeAllViews();
//                    container.addView(splashView);
//                } catch (Throwable e) {
//                    if (callback != null) {
//                        callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
//                                AdapterErrorBuilder.AD_UNIT_SPLASH, "HwAdsAdapter", e.getMessage()));
//                    }
//                }
//            }
//        });
    }

}
