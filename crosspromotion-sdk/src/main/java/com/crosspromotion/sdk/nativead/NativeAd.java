// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.nativead;

import android.content.Context;

import com.crosspromotion.sdk.core.imp.nativeads.NativeAdImp;
import com.openmediation.sdk.nativead.NativeAdView;

public final class NativeAd {

    private NativeAdImp mNativeAd;

    public NativeAd(Context context, String placementId) {
        mNativeAd = new NativeAdImp(placementId);
    }

    public void loadAd() {
        mNativeAd.loadAds();
    }

    public void loadAdWithPayload(String payload) {
        mNativeAd.loadAdsWithPayload(payload);
    }

    public void setAdListener(NativeAdListener listener) {
        mNativeAd.setListener(listener);
    }

    public void registerNativeAdView(NativeAdView view) {
        mNativeAd.registerNativeView(view);
    }

    public void destroy() {
        mNativeAd.destroy();
    }
}
