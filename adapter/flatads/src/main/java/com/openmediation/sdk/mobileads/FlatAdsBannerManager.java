// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;

import java.util.Map;

import static com.openmediation.sdk.mobileads.FlatAdsAdapter.BID;

public class FlatAdsBannerManager {

    private static class Holder {
        private static final FlatAdsBannerManager INSTANCE = new FlatAdsBannerManager();
    }

    private FlatAdsBannerManager() {
    }

    public static FlatAdsBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(String appKey, BannerAdCallback callback) {
        FlatAdsSingleTon.getInstance().init(appKey, new FlatAdsSingleTon.InitListener() {
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
                            AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", error));
                }
            }
        });
    }

    public void loadAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        boolean bid = false;
        if (extras.containsKey(BID) && extras.get(BID) instanceof Integer) {
            bid = ((int) extras.get(BID)) == 1;
        }
        if (bid) {
            FlatAdsSingleTon.getInstance().loadAndShowBanner(adUnitId, callback);
        } else {
            FlatAdsSingleTon.getInstance().loadBannerAd(activity, adUnitId, extras, callback);
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        return FlatAdsSingleTon.getInstance().isBannerAdReady(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        FlatAdsSingleTon.getInstance().destroyBannerAd(adUnitId);
    }

}
