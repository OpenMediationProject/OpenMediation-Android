package com.openmediation.sdk.splash;

public interface SplashAdListener {

    /**
     * called when SplashAd loaded
     */
    void onSplashAdLoad();

    /**
     * called when SplashAd load error
     */
    void onSplashAdFailed(String error);

    /**
     * called when SplashAd clicked
     */
    void onSplashAdClicked();

    /**
     * called when SplashAd showed
     */
    void onSplashAdShowed();

    /**
     * called when SplashAd show failed
     *
     * @param error SplashAd show error reason
     */
    void onSplashAdShowFailed(String error);

    /**
     * called when SplashAd countdown
     * @param millisUntilFinished The time until the end of the countdown，ms
     */
    void onSplashAdTick(long millisUntilFinished);

    /**
     * called when SplashAd dismissed
     */
    void onSplashAdDismissed();
}
