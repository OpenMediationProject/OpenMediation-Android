package com.openmediation.sdk.splash;


import android.view.ViewGroup;

import com.openmediation.sdk.core.imp.splash.SplashAdManager;

public class SplashAd {

    /**
     * load timeout
     * @param timeout ms
     */
    public static void setLoadTimeout(long timeout) {
        SplashAdManager.getInstance().setLoadTimeout(timeout);
    }

    public static void loadAd() {
        SplashAdManager.getInstance().load();
    }

    public static void setSplashAdListener(SplashAdListener listener) {
        SplashAdManager.getInstance().setSplashAdListener(listener);
    }

    public static boolean isReady() {
        return SplashAdManager.getInstance().isReady();
    }

    public static void showAd(ViewGroup container) {
        SplashAdManager.getInstance().show(container);
    }
}
