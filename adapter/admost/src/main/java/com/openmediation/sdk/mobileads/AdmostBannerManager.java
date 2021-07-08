/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;

import java.util.Map;

public class AdmostBannerManager {

    private static final String TAG = "OM-AdMost: ";

    private static class Holder {
        private static final AdmostBannerManager INSTANCE = new AdmostBannerManager();
    }

    private AdmostBannerManager() {
    }

    public static AdmostBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(Activity activity, String appKey, BannerAdCallback callback) {
        AdmostSingleTon.getInstance().init(activity, appKey, new AdmostSingleTon.InitListener() {
            @Override
            public void initSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void initFailed(int code, String error) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "AdmostAdapter", code, error));
                }
            }
        });
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        AdmostBannerAdsConfig config = AdmostSingleTon.getInstance().getBannerAd(adUnitId);
        if (config == null) {
            String error = AdmostSingleTon.getInstance().getError(adUnitId);
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "AdmostAdapter", error));
            }
            return;
        }
        AdmostSingleTon.getInstance().addBannerListener(adUnitId, new AdmostBannerCallback() {

            @Override
            public void onBannerAdClick(String adUnitId) {
                if (callback != null) {
                    callback.onBannerAdAdClicked();
                }
            }
        });
        if (callback != null) {
            callback.onBannerAdLoadSuccess(config.getAdView());
        }
    }

}
