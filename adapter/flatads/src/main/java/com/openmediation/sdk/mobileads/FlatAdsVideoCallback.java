// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

interface FlatAdsVideoCallback {

    void onRewardedOpened(String adUnitId);

    void onRewardedShowFailed(String adUnitId, String error);

    void onRewardedClosed(String adUnitId);

    void onRewardedReward(String adUnitId);

    void onRewardedClicked(String adUnitId);
}
