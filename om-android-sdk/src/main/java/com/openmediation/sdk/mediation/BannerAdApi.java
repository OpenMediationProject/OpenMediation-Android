// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.app.Activity;

import java.util.Map;

public interface BannerAdApi {
    void initBannerAd(Activity activity, Map<String, Object> dataMap, BannerAdCallback callback);

    void loadBannerAd(Activity activity, String placementId, Map<String, Object> extras, BannerAdCallback callback);

    /**
     * Checks if third-party ad networks have available ads
     *
     * @param adUnitId ad unit id on third-party ad networks
     * @return third-party ad networks have available ads or not
     */
    boolean isBannerAdAvailable(String adUnitId);

    void destroyBannerAd(String placementId);
}
