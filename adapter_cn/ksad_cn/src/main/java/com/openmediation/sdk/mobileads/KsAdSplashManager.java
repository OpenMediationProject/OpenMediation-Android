// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KsAdSplashManager {
    private static final String TAG = "KsAdSplash: ";
    private static final String CONFIG_WIDTH = "Width";
    private static final String CONFIG_HEIGHT = "Height";

    private final ConcurrentHashMap<String, KsSplashScreenAd> mSplashAds;

    private static class Holder {
        private static final KsAdSplashManager INSTANCE = new KsAdSplashManager();
    }

    private KsAdSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static KsAdSplashManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final SplashAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        KsAdManagerHolder.init(context.getApplicationContext(), appKey, new KsAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSplashAdInitSuccess();
                }
            }

            @Override
            public void onFailed() {
                if (callback != null) {
                    callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", "Init Failed"));
                }
            }
        });
    }

    public void loadAd(String adUnitId, final Map<String, Object> config, SplashAdCallback callback) {
        KsLoadManager loadManager = KsAdSDK.getLoadManager();
        if (loadManager == null) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", "KsLoadManager is null"));
            }
            return;
        }
        try {
            long posId;
            try {
                posId = Long.parseLong(adUnitId);
            } catch(Exception e) {
                posId = 0L;
            }
            int width = 0;
            try {
                width = Integer.parseInt(config.get(CONFIG_WIDTH).toString());
            } catch(Exception ignored) {
            }
            int height = 0;
            try {
                height = Integer.parseInt(config.get(CONFIG_HEIGHT).toString());
            } catch(Exception ignored) {
            }
            KsScene scene;
            if (width <= 0 || height <= 0) {
                scene = new KsScene.Builder(posId).build();
            } else {
                scene = new KsScene.Builder(posId).width(width).height(height).build();
            }
            InnerSplashAdListener listener = new InnerSplashAdListener(adUnitId, callback);
            KsAdSDK.getLoadManager().loadSplashScreenAd(scene, listener);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mSplashAds.containsKey(adUnitId) && mSplashAds.get(adUnitId).isAdEnable();
    }

    public void showAd(final String adUnitId, final ViewGroup container, final SplashAdCallback callback) {
        if (container == null) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", "Splash container is null, please use \"SplashAd.showAd(ViewGroup)\""));
            }
            return;
        }
        if (!isAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", "SplashAd not ready"));
            }
            return;
        }
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    KsSplashScreenAd splashScreenAd = mSplashAds.remove(adUnitId);
                    SplashAdInteractionListener listener = new SplashAdInteractionListener(adUnitId, callback);
                    View splashView = splashScreenAd.getView(MediationUtil.getContext(), listener);
                    if (splashView.getParent() instanceof ViewGroup) {
                        ViewGroup viewGroup = (ViewGroup) splashView.getParent();
                        viewGroup.removeView(splashView);
                    }
                    container.removeAllViews();
                    container.addView(splashView);
                } catch(Throwable e) {
                    if (callback != null) {
                        callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mSplashAds.containsKey(adUnitId)) {
            KsSplashScreenAd splashAD = mSplashAds.get(adUnitId);
            splashAD = null;
            mSplashAds.remove(adUnitId);
        }
    }

    private class InnerSplashAdListener implements KsLoadManager.SplashScreenAdListener {

        private String mAdUnitId;
        private SplashAdCallback mAdCallback;

        private InnerSplashAdListener(String adUnitId, SplashAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onError(int code, String msg) {
            AdLog.getSingleton().LogD(TAG + "Splash ad LoadError: code = " + code + ", msg = " + msg);
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", code, msg));
            }
        }

        @Override
        public void onRequestResult(int i) {

        }

        @Override
        public void onSplashScreenAdLoad(KsSplashScreenAd ksSplashScreenAd) {
            if (ksSplashScreenAd == null) {
                if (mAdCallback != null) {
                    mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", "Splash ad Load Failed: KsSplashScreenAd is null"));
                }
                return;
            }
            mSplashAds.put(mAdUnitId, ksSplashScreenAd);
            AdLog.getSingleton().LogD(TAG + "Splash ad onSplashAdLoad");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadSuccess(null);
            }
        }
    }

    private class SplashAdInteractionListener implements KsSplashScreenAd.SplashScreenAdInteractionListener {
        private String mAdUnitId;
        private SplashAdCallback mAdCallback;

        private SplashAdInteractionListener(String adUnitId, SplashAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onAdClicked() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdClicked");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdAdClicked();
            }
        }

        @Override
        public void onAdShowError(int code, String msg) {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdShowError: code = " + code + ", msg = " + msg);
            if (mAdCallback != null) {
                mAdCallback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "KsAdAdapter", code, msg));
            }
        }

        @Override
        public void onAdShowEnd() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdShowEnd");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdDismissed();
            }
        }

        @Override
        public void onAdShowStart() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onAdShowStart");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdShowSuccess();
            }
        }

        @Override
        public void onSkippedAd() {
            AdLog.getSingleton().LogD(TAG + "Splash ad onSkippedAd");
            if (mAdCallback != null) {
                mAdCallback.onSplashAdDismissed();
            }
        }

        @Override
        public void onDownloadTipsDialogShow() {

        }

        @Override
        public void onDownloadTipsDialogDismiss() {

        }

        @Override
        public void onDownloadTipsDialogCancel() {

        }
    }

}
