// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.splash;

import android.app.Activity;
import android.view.ViewGroup;

import com.openmediation.sdk.core.imp.splash.SpAdManager;

public class SplashAd {

    /**
     * load timeout
     *
     * @param timeout ms
     */
    public static void setLoadTimeout(String placementId, long timeout) {
        SpAdManager.getInstance().setLoadTimeout(placementId, timeout);
    }

    public static void loadAd(String placementId) {
        SpAdManager.getInstance().load(placementId);
    }

    public static void setSize(String placementId, int width, int height) {
        SpAdManager.getInstance().setSize(placementId, width, height);
    }

    public static void setSplashAdListener(String placementId, SplashAdListener listener) {
        SpAdManager.getInstance().setSplashAdListener(placementId, listener);
    }

    public static boolean isReady(String placementId) {
        return SpAdManager.getInstance().isReady(placementId);
    }

    @Deprecated
    public static void showAd(Activity activity, String placementId, ViewGroup container) {
        SpAdManager.getInstance().show(placementId, container);
    }

    @Deprecated
    public static void showAd(Activity activity, String placementId) {
        SpAdManager.getInstance().show(placementId);
    }

    public static void showAd(String placementId, ViewGroup container) {
        SpAdManager.getInstance().show(placementId, container);
    }

    public static void showAd(String placementId) {
        SpAdManager.getInstance().show(placementId);
    }
}
