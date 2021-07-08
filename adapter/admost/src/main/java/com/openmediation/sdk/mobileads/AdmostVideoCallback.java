// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

interface AdmostVideoCallback {

    void onRewardedOpened(String adUnitId);

    void onRewardedClosed(String adUnitId);

    void onRewardedComplete(String adUnitId);

    void onRewardedClick(String adUnitId);
}
