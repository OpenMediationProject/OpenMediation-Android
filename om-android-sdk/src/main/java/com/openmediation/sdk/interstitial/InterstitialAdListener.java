// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.interstitial;

import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;

/**
 * Listener for interstitial events. Implementers of this interface will receive events for interstitial ads
 */
public interface InterstitialAdListener {

    /**
     * called when interstitialAd availability changed
     *
     * @param available represent interstitialAd available status
     */
    void onInterstitialAdAvailabilityChanged(boolean available);

    /**
     * called when interstitialAd is shown
     *
     * @param scene the scene
     */
    void onInterstitialAdShowed(Scene scene);

    /**
     * called when interstitialAd show failed
     *
     * @param scene the scene
     * @param error Interstitial ads show failed reason
     */
    void onInterstitialAdShowFailed(Scene scene, Error error);

    /**
     * called when interstitialAd closes
     *
     * @param scene the scene
     */
    void onInterstitialAdClosed(Scene scene);

    /**
     * called when interstitialAd is clicked
     *
     * @param scene the scene
     */
    void onInterstitialAdClicked(Scene scene);

}
