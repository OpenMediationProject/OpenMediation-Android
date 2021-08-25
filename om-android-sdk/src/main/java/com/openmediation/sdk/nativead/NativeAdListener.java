// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.nativead;

import com.openmediation.sdk.utils.error.Error;

/**
 * Lifecycle to receive native ads events
 */
public interface NativeAdListener {
    /**
     * called when native ad is prepared
     *
     * @param info An object {@link AdInfo}include base elements of ads {#title,#description,#CTA,#rate}
     */
    void onNativeAdLoaded(String placementId, AdInfo info);

    /**
     * called when native ads preparation failed
     *
     * @param error failure reason
     */
    void onNativeAdLoadFailed(String placementId, Error error);

    /**
     * called when native ad is impression
     *
     * @param info An object {@link AdInfo}include base elements of ads {#title,#description,#CTA,#rate}
     */
    void onNativeAdImpression(String placementId, AdInfo info);

    /**
     * called when native ad is clicked
     */
    void onNativeAdClicked(String placementId, AdInfo info);
}
