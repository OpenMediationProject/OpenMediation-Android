// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.constant;

public interface CommonConstants {
    String INIT_URL = "https://omtest.adtiming.com/init";

    String CHARTSET_UTF8 = "UTF-8";
    int PLAT_FORM_ANDROID = 1;
    int API_VERSION = 1;

    String SDK_VERSION_NAME = "1.2.0";

    String ADTYPE_BANNER = "Banner";
    String ADTYPE_NATIVE = "Native";
    String ADTYPE_SPLASH = "Splash";

    int WATERFALL_READY = 3;
    int INSTANCE_LOAD = 4;
    int INSTANCE_READY = 5;
    int INSTANCE_IMPR = 6;
    int INSTANCE_CLICK = 7;
    String PKG_GP = "com.android.vending";
    String PKG_FB = "com.facebook.katana";
    String PKG_SDK = "com.openmediation.sdk";
    String PKG_ADAPTER = "com.openmediation.sdk.mobileads.";

    //AdType
    int BANNER = 0;
    int NATIVE = 1;
    int VIDEO = 2;
    int INTERSTITIAL = 3;
    int SPLASH = 4;

    String DB_NAME = "omDB.db";
    int DB_VERSION = 1;
}
