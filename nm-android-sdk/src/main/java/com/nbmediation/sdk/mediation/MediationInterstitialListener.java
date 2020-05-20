// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import com.nbmediation.sdk.utils.error.Error;

/**
 * The interface Mediation interstitial listener.
 */
public interface MediationInterstitialListener {

    /**
     * called when interstitialAd load success
     */
    void onInterstitialAdLoadSuccess();

    /**
     * called when interstitialAd load failed
     *
     * @param error the error
     */
    void onInterstitialAdLoadFailed(Error error);

    /**
     * called when interstitialAd is shown
     */
    void onInterstitialAdShowed();

    /**
     * called when interstitialAd show failed
     *
     * @param error Interstitial ads show failed reason
     */
    void onInterstitialAdShowFailed(Error error);

    /**
     * called when interstitialAd closes
     */
    void onInterstitialAdClosed();

    /**
     * called when interstitialAd is clicked
     */
    void onInterstitialAdClicked();
}
