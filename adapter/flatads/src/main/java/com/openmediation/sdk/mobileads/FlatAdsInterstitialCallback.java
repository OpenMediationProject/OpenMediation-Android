// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

interface FlatAdsInterstitialCallback {

    void onInterstitialOpened(String adUnitId);

    void onInterstitialShowFailed(String adUnitId, String error);

    void onInterstitialDismissed(String adUnitId);

    void onInterstitialClick(String adUnitId);
}