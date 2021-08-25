/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.inmobi.ads.InMobiBanner;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.Map;

public class InMobiBannerManager {

    private static final String TAG = "OM-InMobi: ";

    private static class Holder {
        private static final InMobiBannerManager INSTANCE = new InMobiBannerManager();
    }

    private InMobiBannerManager() {
    }

    public static InMobiBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(Activity activity, String appKey, BannerAdCallback callback) {
        InMobiSingleTon.getInstance().init(activity, appKey, new InMobiInitCallback() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "AdmostAdapter", error));
                }
            }
        });
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InMobiBanner banner = InMobiSingleTon.getInstance().getBannerAd(adUnitId);
                if (banner == null) {
                    String error = InMobiSingleTon.getInstance().getError(adUnitId);
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "AdmostAdapter", error));
                    }
                    return;
                }
                InMobiSingleTon.getInstance().addBannerListener(adUnitId, new InMobiBannerCallback() {

                    @Override
                    public void onAdDisplayed(String placementId) {
                        if (callback != null) {
                            callback.onBannerAdImpression();
                        }
                    }

                    @Override
                    public void onAdClick(String placementId) {
                        if (callback != null) {
                            callback.onBannerAdAdClicked();
                        }
                    }
                });
                if (callback != null) {
                    callback.onBannerAdLoadSuccess(banner);
                }
            }
        });
    }

}
