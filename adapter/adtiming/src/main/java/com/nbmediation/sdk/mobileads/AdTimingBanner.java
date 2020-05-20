// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;

import com.adtiming.mediationsdk.AdTimingAds;
import com.adtiming.mediationsdk.InitCallback;
import com.adtiming.mediationsdk.banner.BannerAd;
import com.adtiming.mediationsdk.banner.BannerAdListener;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;

import java.util.Map;

public class AdTimingBanner extends CustomBannerEvent implements BannerAdListener {

    private BannerAd mBannerAd;


    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_1;
    }

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }

        if (!AdTimingAds.isInit()) {
            String appKey = config.get("AppKey");
            AdTimingAds.init(activity, appKey, new InitCallback() {
                @Override
                public void onSuccess() {
                    loadBanner(activity);
                }

                @Override
                public void onError(AdTimingError adTimingError) {
                    onInsError(adTimingError.toString());
                }
            });
            return;
        }
        loadBanner(activity);
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerAd != null) {
            mBannerAd.destroy();
        }
    }

    private void loadBanner(Activity activity) {
        if (mBannerAd != null) {
            mBannerAd.loadAd();
            return;
        }
        mBannerAd = new BannerAd(activity, mInstancesKey, this);
        mBannerAd.loadAd();
    }

    @Override
    public void onAdReady(View view) {
        onInsReady(view);
    }

    @Override
    public void onAdFailed(String s) {
        onInsError(s);
    }

    @Override
    public void onAdClicked() {
        onInsClicked();
    }
}
