// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.banner;

import android.view.View;

import com.openmediation.sdk.utils.error.Error;

/**
 * Listener for banner ad events. Implementing this interface to receive events for banner ads
 */
public interface BannerAdListener {
    /**
     * called when a banner ad is prepared
     *
     * @param view A view filled with ads. Add this view into a layout container to display
     */
    void onBannerAdLoaded(String placementId, View view);

    /**
     * called when a banner ad failed to prepare
     *
     * @param error failure reasons
     */
    void onBannerAdLoadFailed(String placementId, Error error);

    /**
     * called when a banner ad is clicked
     */
    void onBannerAdClicked(String placementId);
}
