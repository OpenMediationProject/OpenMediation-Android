// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

interface PubNativeVideoCallback {

    void onRewardedOpened(String placementId);

    void onRewardedClosed(String placementId);

    void onRewardedClick(String placementId);

    void onReward(String placementId);
}
