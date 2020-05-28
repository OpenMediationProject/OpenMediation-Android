// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public abstract class CustomNativeEvent extends CustomAdEvent {
    protected AdInfo mAdInfo = new AdInfo();


    protected int[] getNativeSize(Map<String, String> config) {
        int width = 350, height = 350;
        if (config != null && config.containsKey("width") && config.containsKey("height")) {
            try {
                width = Integer.parseInt(config.get("width"));
                height = Integer.parseInt(config.get("height"));
            } catch (Exception e) {
                width = 350;
                height = 350;
            }
        }
        return new int[]{width, height};
    }

    public abstract void registerNativeView(NativeAdView adView);
}
