// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TikTokSplash extends CustomSplashEvent implements TTAdNative.SplashAdListener, TTSplashAd.AdInteractionListener {
    private static String TAG = "OM-TikTok: ";

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
        loadSplashAd(mInstancesKey, config.get("Timeout"));
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

    private void loadSplashAd(String codeId, String timeout) {
        int fetchDelay;
        try {
            fetchDelay = Integer.parseInt(timeout);
        } catch (Exception e) {
            fetchDelay = 0;
        }
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .build();
        if (fetchDelay <= 0) {
            mTTAdNative.loadSplashAd(adSlot, this);
        } else {
            mTTAdNative.loadSplashAd(adSlot, this, fetchDelay);
        }
    }

    @Override
    public void show(ViewGroup container) {
        if (!isReady()) {
            onInsShowFailed("SplashAd not ready");
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
            onInsShowFailed("SplashAd not ready");
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
        AdLog.getSingleton().LogD(TAG + "Splash ad load failed: code " + code + " " + message);
        onInsError(message);
    }

    @Override
    public void onTimeout() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad load failed: timeout");
        onInsError("Splash ad load failed: timeout");
    }

    @Override
    public void onSplashAdLoad(TTSplashAd ttSplashAd) {
        if (isDestroyed) {
            return;
        }
        if (ttSplashAd == null) {
            onInsError("Splash ad Load Failed");
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
        AdLog.getSingleton().LogD(TAG + "Splash ad onAdShow");
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
