// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

interface CbtAdsCallback {

    void didRewardedLoadSuccess(String placementId);

    void didRewardedLoadFailed(String placementId, String error);

    void didRewardedShowed(String placementId);

    void didRewardedShowFailed(String placementId, String error);

    void didRewardedClosed(String placementId);

    void didRewardedRewarded(String placementId);

    void didInterstitialLoadSuccess(String placementId);

    void didInterstitialLoadFailed(String placementId, String error);

    void didInterstitialShowed(String placementId);

    void didInterstitialShowFailed(String placementId, String error);

    void didInterstitialClosed(String placementId);
}
