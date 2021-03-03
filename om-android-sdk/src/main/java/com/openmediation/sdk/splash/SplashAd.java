package com.openmediation.sdk.splash;


import android.app.Activity;
import android.view.ViewGroup;

import com.openmediation.sdk.core.imp.splash.SplashAdManager;

public class SplashAd {

    /**
     * load timeout
     *
     * @param timeout ms
     */
    public static void setLoadTimeout(String placementId, long timeout) {
        SplashAdManager.getInstance().setLoadTimeout(placementId, timeout);
    }

    public static void loadAd(String placementId) {
        SplashAdManager.getInstance().load(placementId);
    }

    public static void setSize(String placementId, int width, int height) {
        SplashAdManager.getInstance().setSize(placementId, width, height);
    }

    public static void setSplashAdListener(String placementId, SplashAdListener listener) {
        SplashAdManager.getInstance().setSplashAdListener(placementId, listener);
    }

    public static boolean isReady(String placementId) {
        return SplashAdManager.getInstance().isReady(placementId);
    }

    public static void showAd(Activity activity, String placementId, ViewGroup container) {
        SplashAdManager.getInstance().show(activity, placementId, container);
    }

    public static void showAd(Activity activity, String placementId) {
        SplashAdManager.getInstance().show(activity, placementId);
    }
}
