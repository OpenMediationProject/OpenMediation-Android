// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.banner;

import android.app.Activity;

import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.core.imp.banner.BnManager;

/**
 * <p>
 * In general you should get instances of {@link BannerAd}
 * <p>
 * When you have a {@link BannerAd} instance and wish to show a view you should:
 * 1. Call {@link #loadAd()} to prepare the ad.
 * 2. When the ad is no longer shown to the user, call {@link #destroy()}. You can later
 * call {@link #loadAd()} again if the ad will be shown.
 */
public class BannerAd {

    private BnManager mBanner;

    /**
     * Instantiates the BannerAd
     *
     * @param activity    Must be a non-null, effective Activity.
     * @param placementId Current placement id
     * @param adListener  A lifecycle listener to receive native ad events
     */
    @Deprecated
    public BannerAd(Activity activity, String placementId, BannerAdListener adListener) {
        this.mBanner = new BnManager(placementId, adListener);
    }

    public BannerAd(String placementId, BannerAdListener adListener) {
        this.mBanner = new BnManager(placementId, adListener);
    }

    public void loadAd() {
        if (mBanner != null) {
            mBanner.loadAds(OmManager.LOAD_TYPE.MANUAL);
        }
    }

    public void setAdSize(AdSize adSize) {
        if (mBanner != null) {
            mBanner.setAdSize(adSize);
        }
    }

    /**
     * Cleans up all {@link BannerAd} state. Call this method when the {@link BannerAd} will never be shown to a
     * user again.
     */
    public void destroy() {
        if (mBanner != null) {
            mBanner.destroy();
        }
    }
}
