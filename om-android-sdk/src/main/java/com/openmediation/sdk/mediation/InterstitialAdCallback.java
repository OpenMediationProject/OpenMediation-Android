// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

/**
 * This interface is used to notify adapter's lifecycle to mediation SDK
 */
public interface InterstitialAdCallback {
    /**
     * called t third-party ad network ads init success
     */
    void onInterstitialAdInitSuccess();

    /**
     * called when third-party ad networks ads init failed
     *
     * @param error init failed reason
     */
    void onInterstitialAdInitFailed(AdapterError error);

    /**
     * called when third-party ad networks ads closed
     */
    void onInterstitialAdClosed();

    /**
     * called at third-party ad networks ads load success
     */
    void onInterstitialAdLoadSuccess();

    /**
     * called when third-party ad networks ads load failed
     *
     * @param error load failure reason
     */
    void onInterstitialAdLoadFailed(AdapterError error);

    /**
     * called when third-party ad networks show ads
     */
    void onInterstitialAdShowSuccess();

    /**
     * called when third-party ad networks ads show failed
     *
     * @param error show failure reason
     */
    void onInterstitialAdShowFailed(AdapterError error);

    /**
     * called when third-party ad networks ads are clicked
     */
    void onInterstitialAdClick();
}
