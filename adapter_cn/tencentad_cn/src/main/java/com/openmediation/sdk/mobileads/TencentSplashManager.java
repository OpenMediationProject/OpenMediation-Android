// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TencentSplashManager {

    private final ConcurrentHashMap<String, SplashAD> mSplashAds;
    private final ConcurrentHashMap<String, Long> mExpireTimestamps;

    private static class Holder {
        private static final TencentSplashManager INSTANCE = new TencentSplashManager();
    }

    private TencentSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
        mExpireTimestamps = new ConcurrentHashMap<>();
    }

    public static TencentSplashManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final SplashAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        boolean result = TencentAdManagerHolder.init(context.getApplicationContext(), appKey);
        if (result) {
            if (callback != null) {
                callback.onSplashAdInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TencentAdAdapter", "Init Failed"));
            }
        }
    }

    public void loadAd(Context context, String adUnitId, final Map<String, Object> config, SplashAdCallback callback) {
        int fetchDelay = 0;
        try {
            fetchDelay = Integer.parseInt(config.get("Timeout").toString());
        } catch(Exception ignored) {
        }
        if (fetchDelay <= 0) {
            fetchDelay = 0;
        }
        InnerSplashAdListener listener = new InnerSplashAdListener(adUnitId, callback);
        SplashAD splashAD = new SplashAD(context, adUnitId, listener, fetchDelay);
        listener.setSplashAD(splashAD);
        splashAD.fetchAdOnly();
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            mSplashAds.remove(adUnitId);
            mExpireTimestamps.remove(adUnitId);
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mSplashAds.containsKey(adUnitId) && mExpireTimestamps.containsKey(adUnitId)) {
            Long adExpireTime = mExpireTimestamps.get(adUnitId);
            return (adExpireTime - SystemClock.elapsedRealtime()) / 1000 > 0;
        }
        return false;
    }

    public void showAd(final String adUnitId, final ViewGroup container, final SplashAdCallback callback) {
        if (container == null) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TencentAdAdapter", "Splash container is null, please use \"SplashAd.showAd(ViewGroup)\""));
            }
            return;
        }
        if (!isAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TencentAdAdapter", "SplashAd not ready"));
            }
            return;
        }
        try {
            SplashAD ttSplashAd = mSplashAds.get(adUnitId);
            ttSplashAd.showAd(container);
            mSplashAds.remove(adUnitId);
            mExpireTimestamps.remove(adUnitId);
        } catch(Throwable e) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TencentAdAdapter", e.getMessage()));
            }
        }
    }

    private class InnerSplashAdListener implements SplashADListener {

        private String mAdUnitId;
        private SplashAdCallback mAdCallback;
        private SplashAD mSplashAD;

        private InnerSplashAdListener(String adUnitId, SplashAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        public void setSplashAD(SplashAD splashAD) {
            this.mSplashAD = splashAD;
        }

        @Override
        public void onADDismissed() {
            if (mAdCallback != null) {
                mAdCallback.onSplashAdDismissed();
            }
        }

        @Override
        public void onNoAD(AdError adError) {
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "TencentAdAdapter",
                        adError.getErrorCode(), adError.getErrorMsg()));
            }
        }

        @Override
        public void onADPresent() {
            AdLog.getSingleton().LogD("TencentAdSplash ad onADPresent: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onSplashAdShowSuccess();
            }
        }

        @Override
        public void onADClicked() {
            if (mAdCallback != null) {
                mAdCallback.onSplashAdAdClicked();
            }
        }

        @Override
        public void onADTick(long l) {
            if (mAdCallback != null) {
                mAdCallback.onSplashAdTick(l);
            }
        }

        @Override
        public void onADExposure() {
            AdLog.getSingleton().LogD("TencentAdSplash ad onADExposure: " + mAdUnitId);
        }

        @Override
        public void onADLoaded(long expireTimestamp) {
            mSplashAds.put(mAdUnitId, mSplashAD);
            mExpireTimestamps.put(mAdUnitId, expireTimestamp);
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadSuccess(null);
            }
            AdLog.getSingleton().LogD("TencentAdSplash ad onSplashAdLoad: " + mAdUnitId);
        }
    }


}
