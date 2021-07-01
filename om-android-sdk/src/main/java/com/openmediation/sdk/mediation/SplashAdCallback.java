// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

public interface SplashAdCallback {
    /**
     * called when third-party ad network ads init success
     */
    void onSplashAdInitSuccess();

    /**
     * called when third-party ad network ads init failed
     *
     * @param error init failed reason
     */
    void onSplashAdInitFailed(AdapterError error);

    /**
     * called when third-party ad network ads load success
     */
    void onSplashAdLoadSuccess(Object view);

    /**
     * called when third-party ad network load failed
     *
     * @param error load failed reason
     */
    void onSplashAdLoadFailed(AdapterError error);

    /**
     * called when third-party ad network shows an ads
     */
    void onSplashAdShowSuccess();

    /**
     * called when third-party ad network show failed
     *
     * @param error show failed reason
     */
    void onSplashAdShowFailed(AdapterError error);

    /**
     * called when SplashAd countdown
     *
     * @param millisUntilFinished The time until the end of the countdown,ms
     */
    void onSplashAdTick(long millisUntilFinished);

    /**
     * called when third-party ad network ads are clicked
     */
    void onSplashAdAdClicked();

    /**
     * called when SplashAd dismissed
     */
    void onSplashAdDismissed();
}
