// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.os.SystemClock;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class TencentAdSplash extends CustomSplashEvent implements SplashADListener {

    private SplashAD mSplashAD;

    private long mExpireTimestamp;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!check(activity)) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Activity is null"));
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
        } catch (Exception ignored) {
        }
        if (fetchDelay <= 0) {
            fetchDelay = 0;
        }
        mSplashAD = new SplashAD(activity, codeId, this, fetchDelay);
        mSplashAD.fetchAdOnly();
        mExpireTimestamp = 0;
    }

    @Override
    public void show(Activity activity) {
        super.show(activity);
        showSplashAd(null);
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        super.show(activity, container);
        showSplashAd(container);
    }

    private void showSplashAd(ViewGroup container) {
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
        onInsDismissed();
    }

    @Override
    public void onNoAD(AdError adError) {
        if (isDestroyed) {
            return;
        }
        mExpireTimestamp = 0;
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, adError.getErrorCode(), adError.getErrorMsg()));
    }

    @Override
    public void onADPresent() {
        if (isDestroyed) {
            return;
        }
        onInsShowSuccess();
    }

    @Override
    public void onADClicked() {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    @Override
    public void onADTick(long millisUntilFinished) {
        if (isDestroyed) {
            return;
        }
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
        onInsReady(null);
    }

}
