// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.nativead;

import android.content.Context;

import com.crosspromotion.sdk.core.imp.nativeads.NativeAdImp;
import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public final class NativeAd {

    private NativeAdImp mNativeAd;

    public NativeAd(Context context, String placementId) {
        mNativeAd = new NativeAdImp(placementId);
    }

    public void loadAdWithPayload(String payload, Map extras) {
        mNativeAd.loadAdsWithPayload(payload, extras);
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
