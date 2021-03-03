// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.ogury.core.OguryError;
import com.ogury.ed.OguryBannerAdListener;
import com.ogury.ed.OguryBannerAdSize;
import com.ogury.ed.OguryBannerAdView;
import com.ogury.sdk.Ogury;
import com.ogury.sdk.OguryConfiguration;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

public class OguryBanner extends CustomBannerEvent implements OguryBannerAdListener {
    private static final String TAG = "OguryBanner ";
    private OguryBannerAdView mBannerAd;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        OguryBannerAdSize size = getAdSize(config);
        if (size == null) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Unsupported Banner Size: " + getBannerDesc(config)));
        }

        initSdk(activity, config);
        mBannerAd = new OguryBannerAdView(activity);
        mBannerAd.setAdUnit(mInstancesKey);
        mBannerAd.setAdSize(OguryBannerAdSize.SMALL_BANNER_320x50);
        mBannerAd.setListener(this);
        mBannerAd.loadAd();
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_13;
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        if (mBannerAd != null) {
            mBannerAd.destroy();
        }
    }

    @Override
    public void onAdLoaded() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogE(TAG + "Banner Load Success");
        onInsReady(mBannerAd);
    }

    @Override
    public void onAdDisplayed() {
        AdLog.getSingleton().LogE(TAG + "Banner onAdDisplayed");
    }

    @Override
    public void onAdClicked() {
        AdLog.getSingleton().LogE(TAG + "Banner onAdClicked");
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    @Override
    public void onAdClosed() {
        AdLog.getSingleton().LogE(TAG + "Banner onAdClosed");
    }

    @Override
    public void onAdError(OguryError oguryError) {
        AdLog.getSingleton().LogE(TAG + "Banner onAdError: " + oguryError);
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, oguryError.getErrorCode(), oguryError.getMessage()));
    }

    private void initSdk(Activity activity, Map<String, String> config) {
        String appKey = config.get("AppKey");
        OguryConfiguration.Builder configurationBuilder = new OguryConfiguration.Builder(activity.getApplicationContext(), appKey);
        Ogury.start(configurationBuilder.build());
    }

    private OguryBannerAdSize getAdSize(Map<String, String> config) {
        String desc = getBannerDesc(config);
        if (DESC_BANNER.equals(desc)) {
            return OguryBannerAdSize.SMALL_BANNER_320x50;
        }
        if (DESC_RECTANGLE.equals(desc)) {
            return OguryBannerAdSize.MPU_300x250;
        }
        if (DESC_LEADERBOARD.equals(desc)) {
            return null;
        }
        return OguryBannerAdSize.SMALL_BANNER_320x50;
    }

}
