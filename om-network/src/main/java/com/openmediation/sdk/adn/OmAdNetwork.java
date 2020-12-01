// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.adn;

import android.content.Context;

import com.openmediation.sdk.adn.core.OmAdNetworkManager;

public final class OmAdNetwork {

    public static void init(Context context) {
        OmAdNetworkManager.getInstance().init(context);
    }

    /**
     * Returns the SDk version
     *
     * @return the sdk version
     */
    public static String getSDKVersion() {
        return com.openmediation.sdk.adn.BuildConfig.VERSION_NAME;
    }
}
