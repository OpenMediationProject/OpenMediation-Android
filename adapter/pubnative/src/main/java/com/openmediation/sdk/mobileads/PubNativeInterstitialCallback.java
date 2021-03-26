// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

interface PubNativeInterstitialCallback {

    void onInterstitialImpression(String placementId);

    void onInterstitialDismissed(String placementId);

    void onInterstitialClick(String placementId);
}