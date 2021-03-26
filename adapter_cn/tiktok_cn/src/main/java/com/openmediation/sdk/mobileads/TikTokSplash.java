// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

public class TikTokSplash extends CustomSplashEvent implements TTAdNative.SplashAdListener, TTSplashAd.AdInteractionListener {
    private static String TAG = "TikTok: ";

    private static final String CONFIG_TIMEOUT = "Timeout";
    private static final String CONFIG_WIDTH = "Width";
    private static final String CONFIG_HEIGHT = "Height";

    private TTAdNative mTTAdNative;
    private TTSplashAd mSplashAd;

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
        initTTSDKConfig(activity, config, new TTAdManagerHolder.InitCallback() {
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
            public void onFailed(int code, String msg) {
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, code, msg));
            }
        });
    }

    private void initTTSDKConfig(Activity activity, Map<String, String> config, TTAdManagerHolder.InitCallback callback) {
        TTAdManagerHolder.init(activity.getApplication(), config.get("AppKey"), callback);
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        mSplashAd = null;
        mTTAdNative = null;
    }

    private void loadSplashAd(Activity activity, String codeId, Map<String, String> config) {
        int fetchDelay;
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT));
        } catch (Exception e) {
            fetchDelay = 0;
        }
        int width = 0;
        try {
            width = Integer.parseInt(config.get(CONFIG_WIDTH));
        } catch (Exception ignored) {
        }
        if (width <= 0) {
            width = TTAdManagerHolder.getScreenWidth(activity);
        }
        int height = 0;
        try {
            height = Integer.parseInt(config.get(CONFIG_HEIGHT));
        } catch (Exception ignored) {
        }
        if (height <= 0) {
            height = TTAdManagerHolder.getScreenHeight(activity);
        }
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(width, height)
                .build();
        if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
        }
        if (fetchDelay <= 0) {
            mTTAdNative.loadSplashAd(adSlot, this);
        } else {
            mTTAdNative.loadSplashAd(adSlot, this, fetchDelay);
        }
    }

    @Override
    public void show(Activity activity) {
        super.show(activity);
        showSplashAd(null);
    }

    @Override
    public void show(Activity activity, final ViewGroup container) {
        super.show(activity, container);
        showSplashAd(container);
    }

    private void showSplashAd(final ViewGroup container) {
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
                    mSplashAd.setSplashInteractionListener(TikTokSplash.this);
                    View splashView = mSplashAd.getSplashView();
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
        return !isDestroyed && !TextUtils.isEmpty(mInstancesKey) && mSplashAd != null;
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_13;
    }

    @Override
    public void onError(int code, String message) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, code, message));
    }

    @Override
    public void onTimeout() {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Splash ad load failed: timeout"));
    }

    @Override
    public void onSplashAdLoad(TTSplashAd ttSplashAd) {
        if (isDestroyed) {
            return;
        }
        if (ttSplashAd == null) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Splash ad Load Failed: TTSplashAd is null"));
            return;
        }
        mSplashAd = ttSplashAd;
        AdLog.getSingleton().LogD(TAG + "Splash ad onSplashAdLoad");
        onInsReady(null);
    }

    @Override
    public void onAdClicked(View view, int type) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onADClicked");
        onInsClicked();
    }

    @Override
    public void onAdShow(View view, int type) {
        if (isDestroyed) {
            return;
        }
        onInsShowSuccess();
    }

    @Override
    public void onAdSkip() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onAdSkip");
        onInsDismissed();
    }

    @Override
    public void onAdTimeOver() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onAdTimeOver");
        onInsDismissed();
    }
}
