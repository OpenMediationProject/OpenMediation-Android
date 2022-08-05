// Copyright 2022 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class GAMReklamUpNativeAdsConfig {
    private NativeAd mAdMobNativeAd;
    private NativeAdView mUnifiedNativeAdView;

    public NativeAd getAdMobNativeAd() {
        return mAdMobNativeAd;
    }

    public void setAdMobNativeAd(NativeAd nativeAd) {
        this.mAdMobNativeAd = nativeAd;
    }

    public NativeAdView getUnifiedNativeAdView() {
        return mUnifiedNativeAdView;
    }

    public void setUnifiedNativeAdView(NativeAdView nativeAdView) {
        this.mUnifiedNativeAdView = nativeAdView;
    }
}
