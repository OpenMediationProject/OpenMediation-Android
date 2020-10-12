// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import com.chartboost.heliumsdk.ad.HeliumAdError;

interface CbtInterstitialAdCallback {

    void didInterstitialShowed(String placementId);

    void didInterstitialShowFailed(String placementId, HeliumAdError error);

    void didInterstitialClosed(String placementId);
}
