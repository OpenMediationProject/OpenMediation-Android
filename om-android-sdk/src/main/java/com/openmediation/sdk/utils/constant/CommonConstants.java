// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.constant;

public interface CommonConstants {
    String INIT_URL = "https://s.openmediation.com/init";

    String CHARTSET_UTF8 = "UTF-8";
    int PLAT_FORM_ANDROID = 1;
    int API_VERSION = 1;
    int API_VERSION_V2 = 2;

    String SDK_VERSION_NAME = "2.1.1";

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
    int PROMOTION = 5;

    String DB_NAME = "omDB.db";
    int DB_VERSION = 1;

    int HEAD_BIDDING_TIMEOUT = 5000;


    String FILE_HEADER_SUFFIX = "-header";
    String KEY_REQUEST_TIME = "request_time";
    String KEY_CONTENT_TYPE = "Content-Type";
    String KEY_CACHE_CONTROL = "Cache-Control";
    String KEY_ETAG = "ETag";
    String KEY_LAST_MODIFIED = "Last-Modified";
    String KEY_MAX_AGE = "max-age";
    String KEY_IF_NONE_MATCH = "If-None-Match";
    String KEY_IF_MODIFIED_SINCE = "If-Modified-Since";
    String KEY_LOCATION = "Location";
    String KEY_CONTENT_LENGTH = "Content-Length";
}
