// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TikTokSplash extends CustomSplashEvent implements TTAdNative.SplashAdListener, TTSplashAd.AdInteractionListener {
    private static String TAG = "OM-TikTok: ";

    private static final String CONFIG_TIMEOUT = "Timeout";
    private static final String CONFIG_WIDTH = "Width";
    private static final String CONFIG_HEIGHT = "Height";

    private TTAdNative mTTAdNative;
    private ConcurrentMap<String, TTSplashAd> mSplashAdMap;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        if (!check(activity, config)) {
            return;
        }
        if (mSplashAdMap == null) {
            mSplashAdMap = new ConcurrentHashMap<>();
        }
        initTTSDKConfig(activity, config);
        loadSplashAd(activity, mInstancesKey, config);
    }

    private void initTTSDKConfig(Activity activity, Map<String, String> config) {
        TTAdManagerHolder.init(activity.getApplication(), config.get("AppKey"));
        if (mTTAdNative == null) {
            mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
        }
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        mSplashAdMap.clear();
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
            width = getScreenWidth(activity);
        }
        int height = 0;
        try {
            height = Integer.parseInt(config.get(CONFIG_HEIGHT));
        } catch (Exception ignored) {
        }
        if (height <= 0) {
            height = getScreenHeight(activity);
        }
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(width, height)
                .build();
        if (fetchDelay <= 0) {
            mTTAdNative.loadSplashAd(adSlot, this);
        } else {
            mTTAdNative.loadSplashAd(adSlot, this, fetchDelay);
        }
    }

    private static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    private static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public void show(ViewGroup container) {
        if (!isReady()) {
            onInsShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "SplashAd not ready"));
            return;
        }
        try {
            TTSplashAd splashAd = mSplashAdMap.get(mInstancesKey);
            mSplashAdMap.remove(mInstancesKey);
            View splashView = splashAd.getSplashView();
            if (splashView.getParent() instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) splashView.getParent();
                viewGroup.removeView(splashView);
            }
            container.removeAllViews();
            container.addView(splashView);
            splashAd.setSplashInteractionListener(this);
        } catch (Exception e) {
            onInsShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, e.getMessage()));
        }
    }

    @Override
    public boolean isReady() {
        return !isDestroyed && !TextUtils.isEmpty(mInstancesKey) && mSplashAdMap != null && mSplashAdMap.containsKey(mInstancesKey);
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
        mSplashAdMap.put(mInstancesKey, ttSplashAd);
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
