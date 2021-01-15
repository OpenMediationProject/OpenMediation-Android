// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.crosspromotion.sdk.banner.AdSize;
import com.crosspromotion.sdk.banner.BannerAd;
import com.crosspromotion.sdk.banner.BannerAdListener;
import com.crosspromotion.sdk.utils.error.Error;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.util.Map;

public class CrossPromotionBanner extends CustomBannerEvent implements BannerAdListener {
    private static final String PAY_LOAD = "pay_load";
    private BannerAd mBannerAd;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        String payload = "";
        if (config.containsKey(PAY_LOAD)) {
            payload = config.get(PAY_LOAD);
        }
        if (mBannerAd != null) {
            mBannerAd.loadAdWithPayload(payload, config);
            return;
        }
        mBannerAd = new BannerAd(activity, mInstancesKey);
        mBannerAd.setAdListener(this);
        AdSize adSize = getAdSize(activity, config);
        mBannerAd.setAdSize(adSize);
        mBannerAd.loadAdWithPayload(payload, config);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_19;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerAd != null) {
            mBannerAd.destroy();
            mBannerAd = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onBannerAdReady(String placementId, View view) {
        if (!isDestroyed) {
            onInsReady(view);
        }
    }

    @Override
    public void onBannerAdFailed(String placementId, Error error) {
        if (!isDestroyed) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error.getCode(), error.getMessage()));
        }
    }

    @Override
    public void onBannerAdClicked(String placementId) {
        if (!isDestroyed) {
            onInsClicked();
        }
    }

    @Override
    public void onBannerAdShowFailed(String placementId, Error error) {

    }

    private AdSize getAdSize(Context context, Map<String, String> config) {
        String bannerDesc = getBannerDesc(config);
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return AdSize.LEADERBOARD;
            case DESC_RECTANGLE:
                return AdSize.MEDIUM_RECTANGLE;
            case DESC_SMART:
                return AdSize.SMART;
            default:
                return AdSize.BANNER;
        }
    }
}
