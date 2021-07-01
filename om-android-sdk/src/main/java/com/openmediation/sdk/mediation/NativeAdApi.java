// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.app.Activity;

import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public interface NativeAdApi {
    void initNativeAd(Activity activity, Map<String, Object> dataMap, NativeAdCallback callback);

    void loadNativeAd(Activity activity, String placementId, Map<String, Object> extras, NativeAdCallback callback);

    void registerNativeAdView(String placementId, NativeAdView adView, NativeAdCallback callback);

    void destroyNativeAd(String placementId);
}
