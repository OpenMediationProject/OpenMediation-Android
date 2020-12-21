// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import com.chartboost.heliumsdk.ad.HeliumAdError;

interface HeliumVideoCallback {

    void didRewardedShowed(String placementId);

    void didRewardedShowFailed(String placementId, HeliumAdError error);

    void didRewardedClosed(String placementId);

    void didRewardedRewarded(String placementId);
}
