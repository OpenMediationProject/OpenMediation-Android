// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

interface InMobiVideoCallback {
    void onAdDisplayed(String placementId);

    void onAdDisplayFailed(String placementId);

    void onAdClicked(String placementId);

    void onAdDismissed(String placementId);

    void onAdRewarded(String placementId);
}
