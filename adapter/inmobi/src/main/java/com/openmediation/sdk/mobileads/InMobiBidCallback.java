// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

public interface InMobiBidCallback {
    void onBidSuccess(String placementId, double ecpm);

    void onBidFailed(String placementId, String error);
}
