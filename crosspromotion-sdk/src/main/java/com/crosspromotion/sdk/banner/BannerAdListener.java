// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.banner;

import android.view.View;

import com.crosspromotion.sdk.utils.error.Error;

public interface BannerAdListener {
    void onBannerAdReady(String placementId, View view);

    void onBannerAdFailed(String placementId, Error error);

    void onBannerAdClicked(String placementId);

    void onBannerAdShowFailed(String placementId, Error error);
}
