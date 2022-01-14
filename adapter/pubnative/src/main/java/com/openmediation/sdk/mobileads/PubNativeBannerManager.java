// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Application;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;

import net.pubnative.lite.sdk.models.AdSize;
import net.pubnative.lite.sdk.views.HyBidAdView;

import java.util.Map;

public class PubNativeBannerManager {

    private static class Holder {
        private static final PubNativeBannerManager INSTANCE = new PubNativeBannerManager();
    }

    private PubNativeBannerManager() {
    }

    public static PubNativeBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Application application, Map<String, Object> extras, final BannerAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        PubNativeSingleTon.getInstance().init(application, appKey, new PubNativeSingleTon.InitListener() {
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
                            AdapterErrorBuilder.AD_UNIT_BANNER, "PubNativeAdapter", error));
                }
            }
        });
    }

    public void loadAd(final String adUnitId, final Map<String, Object> extras, final BannerAdCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PubNativeSingleTon.getInstance().removeBannerListener(adUnitId);
                PubNativeSingleTon.getInstance().addBannerListener(adUnitId, new PubNativeBannerListener() {
                    @Override
                    public void onAdImpression(String placementId) {
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
                HyBidAdView adView = PubNativeSingleTon.getInstance().removeBannerAd(adUnitId);
                if (adView != null) {
                    adView.show();
                    if (callback != null) {
                        callback.onBannerAdLoadSuccess(adView);
                    }
                    return;
                }

                // load waterfall banner
                AdSize adSize = PubNativeSingleTon.getInstance().getAdSize(MediationUtil.getBannerDesc(extras));
                PubNativeSingleTon.getInstance().loadBanner(adUnitId, adSize, callback, null);
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && PubNativeSingleTon.getInstance().getBannerAd(adUnitId) != null;
    }

    public void destroyAd(String adUnitId) {
        PubNativeSingleTon.getInstance().destroyBannerAd(adUnitId);
    }

}
