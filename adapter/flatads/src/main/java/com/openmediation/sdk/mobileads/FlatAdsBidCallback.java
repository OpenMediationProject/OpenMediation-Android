// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

public interface FlatAdsBidCallback {
    void onBidSuccess(String adUnitId, float ecpm, Object object);

    void onBidFailed(String adUnitId, String error);
}
