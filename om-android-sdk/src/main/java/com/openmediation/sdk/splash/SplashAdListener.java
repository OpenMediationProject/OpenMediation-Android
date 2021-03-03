package com.openmediation.sdk.splash;

public interface SplashAdListener {

    /**
     * called when SplashAd loaded
     */
    void onSplashAdLoad(String placementId);

    /**
     * called when SplashAd load error
     * @param error error message
     */
    void onSplashAdFailed(String placementId, String error);

    /**
     * called when SplashAd clicked
     */
    void onSplashAdClicked(String placementId);

    /**
     * called when SplashAd showed
     */
    void onSplashAdShowed(String placementId);

    /**
     * called when SplashAd show failed
     *
     * @param error SplashAd show error reason
     */
    void onSplashAdShowFailed(String placementId, String error);

    /**
     * called when SplashAd countdown
     *
     * @param millisUntilFinished The time until the end of the countdown,ms
     */
    void onSplashAdTick(String placementId, long millisUntilFinished);

    /**
     * called when SplashAd dismissed
     */
    void onSplashAdDismissed(String placementId);
}
