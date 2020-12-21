// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import java.util.Map;

public interface HeliumBidCallback {
    void onBidSuccess(String placementId, Map<String, String> map);

    void onBidFailed(String placementId, String error);
}
