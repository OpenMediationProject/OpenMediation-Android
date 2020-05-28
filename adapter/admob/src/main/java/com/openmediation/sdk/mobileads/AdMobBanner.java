// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Map;

public class AdMobBanner extends CustomBannerEvent {

    private AdView mBannerView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (mBannerView != null) {
            mBannerView.loadAd(new AdRequest.Builder().build());
            return;
        }
        mBannerView = new AdView(activity.getApplicationContext());
        mBannerView.setAdUnitId(mInstancesKey);
        int[] size = getBannerSize(config);
        AdSize adSize;
        if (size[0] < 0 || size[1] < 0) {
            adSize = new AdSize(320, 50);
        } else {
            adSize = new AdSize(size[0], size[1]);
        }
        mBannerView.setAdSize(adSize);
        mBannerView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                if (!isDestroyed) {
                    onInsReady(mBannerView);
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (!isDestroyed) {
                    onInsClicked();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                AdLog.getSingleton().LogE("Om-AdMob: AdMob banner failed to load, error code is : " + i);
                if (!isDestroyed) {
                    onInsError("Admob onAdFailedToLoad:" + i);
                }
            }
        });
        mBannerView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_2;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
        isDestroyed = true;
    }
}
