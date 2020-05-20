// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import java.util.Map;

public abstract class CustomBannerEvent extends CustomAdEvent {

    protected int[] getBannerSize(Map<String, String> config) {
        int width = 320, height = 50;
        if (config != null && config.containsKey("width") && config.containsKey("height")) {
            try {
                width = Integer.parseInt(config.get("width"));
                height = Integer.parseInt(config.get("height"));
            } catch (Exception e) {
                width = 320;
                height = 50;
            }
        }
        return new int[]{width, height};
    }
}
