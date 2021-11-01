// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.nativead;

import com.openmediation.sdk.core.OmManager;

/**
 * <p>
 * In general you should get instances of {@link NativeAd}
 * <p>
 * When you have a {@link NativeAd} instance and wish to show a view you should:
 * 1. Call {@link #loadAd(String)} to prepare ad.
 * 2. Call {@link #registerNativeAdView(String, NativeAdView, AdInfo)} with a compatible {@link NativeAdView} to render the ad data into the view.
 * 3. When the ad view is no longer shown to the user, call {@link #destroy(String, AdInfo)}. You can later
 * call {@link #loadAd(String)} again if the ad will be shown to users.
 */
public class NativeAd {

    private NativeAd() {
    }

    public static void addAdListener(String placementId, NativeAdListener listener) {
        OmManager.getInstance().addNativeAdListener(placementId, listener);
    }

    public static void removeAdListener(String placementId, NativeAdListener listener) {
        OmManager.getInstance().removeNativeAdListener(placementId, listener);
    }

    public static void setDisplayParams(String placementId, int width, int height) {
        OmManager.getInstance().setDisplayParams(placementId, width, height);
    }

    /**
     * Load ad.
     */
    public static void loadAd(String placementId) {
        OmManager.getInstance().loadNativeAd(placementId);
    }

    /**
     * Registers a {@link NativeAdView} by filling it with ad data.
     *
     * @param adView The ad {@link NativeAdView}
     */
    public static void registerNativeAdView(String placementId, NativeAdView adView, AdInfo info) {
        OmManager.getInstance().registerNativeAdView(placementId, adView, info);
    }

    /**
     * Cleans up all {@link NativeAd} state. Call this method when the {@link NativeAd} will never be shown to a
     * user again.
     */
    public static void destroy(String placementId, AdInfo info) {
        OmManager.getInstance().destroyNativeAd(placementId, info);
    }
}
