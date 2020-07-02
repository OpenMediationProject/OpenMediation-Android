// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;


import android.content.Context;

import java.util.Map;

public interface BannerAdApi {
    void initBannerAd(Context activity, Map<String, Object> dataMap, BannerAdCallback callback);

    void loadBannerAd(Context activity, String adUnitId, BannerAdCallback callback);

    void destroyBannerAd();
}
