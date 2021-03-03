// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

public class KsAdSplash extends CustomSplashEvent implements KsLoadManager.SplashScreenAdListener, KsSplashScreenAd.SplashScreenAdInteractionListener {
    private static final String TAG = "KsAdSplash: ";

    private static final String CONFIG_WIDTH = "Width";
    private static final String CONFIG_HEIGHT = "Height";
    private KsSplashScreenAd mSplashAd;

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!check(activity)) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Activity is null"));
            return;
        }
        initTTSDKConfig(activity, config, new KsAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                try {
                    loadSplashAd(activity, mInstancesKey, config);
                } catch (Exception e) {
                    onInsError(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Unknown Error"));
                }
            }

            @Override
            public void onFailed() {
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Init Error"));
            }
        });
    }

    private void initTTSDKConfig(Activity activity, Map<String, String> config, KsAdManagerHolder.InitCallback callback) {
        KsAdManagerHolder.init(activity.getApplication(), config.get("AppKey"), callback);
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        mSplashAd = null;
    }

    private void loadSplashAd(Activity activity, String adUnitId, Map<String, String> config) {
        KsLoadManager loadManager = KsAdSDK.getLoadManager();
        if (loadManager == null) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "KsLoadManager is null"));
            return;
        }
        long posId;
        try {
            posId = Long.parseLong(adUnitId);
        } catch (Exception e) {
            posId = 0L;
        }
        int width = 0;
        try {
            width = Integer.parseInt(config.get(CONFIG_WIDTH));
        } catch (Exception ignored) {
        }
        int height = 0;
        try {
            height = Integer.parseInt(config.get(CONFIG_HEIGHT));
        } catch (Exception ignored) {
        }
        KsScene scene;
        if (width <= 0 || height <= 0) {
            scene = new KsScene.Builder(posId).build();
        } else {
            scene = new KsScene.Builder(posId).width(width).height(height).build();
        }
        KsAdSDK.getLoadManager().loadSplashScreenAd(scene, this);
    }

    @Override
    public void show(Activity activity) {
        super.show(activity);
        showSplashAd(activity, null);
    }

    @Override
    public void show(Activity activity, final ViewGroup container) {
        super.show(activity, container);
        showSplashAd(activity, container);
    }

    private void showSplashAd(final Activity activity, final ViewGroup container) {
        if (!check(activity)) {
            onInsError(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Activity is null"));
            return;
        }
        if (container == null) {
            onInsShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Splash container is null, please use \"SplashAd.showAd(Activity, ViewGroup)\""));
            return;
        }
        if (!isReady()) {
            onInsShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "SplashAd not ready"));
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    View splashView = mSplashAd.getView(activity, KsAdSplash.this);
                    if (splashView.getParent() instanceof ViewGroup) {
                        ViewGroup viewGroup = (ViewGroup) splashView.getParent();
                        viewGroup.removeView(splashView);
                    }
                    container.removeAllViews();
                    container.addView(splashView);
                } catch (Exception e) {
                    onInsShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, e.getMessage()));
                }
            }
        });
    }

    @Override
    public boolean isReady() {
        return !isDestroyed && !TextUtils.isEmpty(mInstancesKey) && mSplashAd != null && mSplashAd.isAdEnable();
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_21;
    }

    @Override
    public void onError(int code, String msg) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad LoadError: code = " + code + ", msg = " + msg);
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, code, msg));
    }

    @Override
    public void onSplashScreenAdLoad(KsSplashScreenAd ksSplashScreenAd) {
        if (isDestroyed) {
            return;
        }
        if (ksSplashScreenAd == null) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Splash ad Load Failed: KsSplashScreenAd is null"));
            return;
        }
        mSplashAd = ksSplashScreenAd;
        AdLog.getSingleton().LogD(TAG + "Splash ad onSplashAdLoad");
        onInsReady(null);
    }

    @Override
    public void onAdClicked() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onAdClicked");
        onInsClicked();
    }

    @Override
    public void onAdShowError(int code, String msg) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onAdShowError: code = " + code + ", msg = " + msg);
        onInsShowFailed(AdapterErrorBuilder.buildShowError(
                AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, code, msg));
    }

    @Override
    public void onAdShowEnd() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onAdShowEnd");
        onInsDismissed();
    }

    @Override
    public void onAdShowStart() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onAdShowStart");
        onInsShowSuccess();
    }

    @Override
    public void onSkippedAd() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onSkippedAd");
        onInsDismissed();
    }
}
