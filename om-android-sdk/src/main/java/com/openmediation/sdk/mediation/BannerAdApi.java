// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.app.Activity;

import java.util.Map;

public interface BannerAdApi {
    void initBannerAd(Activity activity, Map<String, Object> dataMap, BannerAdCallback callback);

    void loadBannerAd(Activity activity, String adUnitId, BannerAdCallback callback);

    void destroyBannerAd();
}
