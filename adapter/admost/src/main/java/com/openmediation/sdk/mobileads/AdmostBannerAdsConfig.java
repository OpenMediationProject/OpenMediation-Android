/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.view.View;

import admost.sdk.AdMostView;

public class AdmostBannerAdsConfig {
    private AdMostView mAdMostView;
    private View mAdView;

    public AdMostView getAdMostView() {
        return mAdMostView;
    }

    public void setAdMostView(AdMostView adMostView) {
        this.mAdMostView = adMostView;
    }

    public View getAdView() {
        return mAdView;
    }

    public void setAdView(View adView) {
        this.mAdView = adView;
    }
}
