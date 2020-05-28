// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.os.SystemClock;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class TencentAdSplash extends CustomSplashEvent implements SplashADListener {
    private static String TAG = "OM-TencentAd: ";

    private SplashAD mSplashAD;

    private long mExpireTimestamp;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        if (!check(activity, config)) {
            return;
        }
        GDTADManager.getInstance().initWith(activity.getApplicationContext(), config.get("AppKey"));
        loadSplashAd(activity, mInstancesKey, config.get("Timeout"));
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        mSplashAD = null;
        mExpireTimestamp = 0;
    }

    private void loadSplashAd(Activity activity, String codeId, String timeout) {
        int fetchDelay = 0;
        try {
            fetchDelay = Integer.parseInt(timeout);
        } catch (Exception e) {
        }
        if (fetchDelay <= 0) {
            fetchDelay = 0;
        }
        mSplashAD = new SplashAD(activity, codeId, this, fetchDelay);
        mSplashAD.fetchAdOnly();
        mExpireTimestamp = 0;
    }

    @Override
    public void show(ViewGroup container) {
        if (isDestroyed) {
            return;
        }
        if (mSplashAD == null) {
            onInsShowFailed("SplashAd not ready");
            return;
        }
        mSplashAD.showAd(container);
        mExpireTimestamp = 0;
    }

    @Override
    public boolean isReady() {
        return !isDestroyed && (mExpireTimestamp - SystemClock.elapsedRealtime()) / 1000 > 0;
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_6;
    }

    @Override
    public void onADDismissed() {
        if (isDestroyed) {
            return;
        }
        mExpireTimestamp = 0;
        AdLog.getSingleton().LogD(TAG + "Splash ad onADDismissed");
        onInsDismissed();
    }

    @Override
    public void onNoAD(AdError adError) {
        if (isDestroyed) {
            return;
        }
        mExpireTimestamp = 0;
        AdLog.getSingleton().LogD(TAG + "Splash ad load failed: code " + adError.getErrorCode() + " " + adError.getErrorMsg());
        onInsError(adError.getErrorMsg());
    }

    @Override
    public void onADPresent() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onADPresent");
        onInsShowSuccess();
    }

    @Override
    public void onADClicked() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onADClicked");
        onInsClicked();
    }

    @Override
    public void onADTick(long millisUntilFinished) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Splash ad onADTick " + millisUntilFinished);
        onInsTick(millisUntilFinished);
    }

    @Override
    public void onADExposure() {
    }

    @Override
    public void onADLoaded(long expireTimestamp) {
        if (isDestroyed) {
            return;
        }
        mExpireTimestamp = expireTimestamp;
        AdLog.getSingleton().LogD(TAG + "Splash ad onADLoaded");
        onInsReady(null);
    }

}
