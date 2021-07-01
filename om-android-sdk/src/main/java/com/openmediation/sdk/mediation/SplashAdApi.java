// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.app.Activity;
import android.view.ViewGroup;

import java.util.Map;

public interface SplashAdApi {
    void initSplashAd(Activity activity, Map<String, Object> dataMap, SplashAdCallback callback);

    void loadSplashAd(Activity activity, String placementId, Map<String, Object> extras, SplashAdCallback callback);

    void showSplashAd(Activity activity, String placementId, ViewGroup viewGroup, SplashAdCallback callback);

    boolean isSplashAdAvailable(String placementId);

    void destroySplashAd(String placementId);
}
