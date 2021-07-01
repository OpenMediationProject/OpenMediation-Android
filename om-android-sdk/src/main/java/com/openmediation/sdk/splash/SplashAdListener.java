// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.splash;

import com.openmediation.sdk.utils.error.Error;

public interface SplashAdListener {

    /**
     * called when SplashAd loaded
     */
    void onSplashAdLoaded(String placementId);

    /**
     * called when SplashAd load error
     * @param error error message
     */
    void onSplashAdFailed(String placementId, Error error);

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
    void onSplashAdShowFailed(String placementId, Error error);

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
