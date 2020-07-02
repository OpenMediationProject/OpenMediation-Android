// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.constant;

public interface CommonConstants {
    String INIT_URL = "http://omapi.adsgreat.cn:19015/init";//http://om-demo-lb-1458560685.cn-north-1.elb.amazonaws.com.cn:19015/init http://192.168.1.185:19011/init

    String CHARTSET_UTF8 = "UTF-8";
    int PLAT_FORM_ANDROID = 1;
    int API_VERSION = 1;

    String SDK_VERSION_NAME = "1.1.0";

    String ADTYPE_BANNER = "Banner";
    String ADTYPE_NATIVE = "Native";

    int WATERFALL_READY = 3;
    int INSTANCE_LOAD = 4;
    int INSTANCE_READY = 5;
    int INSTANCE_IMPR = 6;
    int INSTANCE_CLICK = 7;
    String PKG_GP = "com.android.vending";
    String PKG_FB = "com.facebook.katana";

    //AdType
    int BANNER = 0;
    int NATIVE = 1;
    int VIDEO = 2;
    int INTERSTITIAL = 3;

    String DB_NAME = "omDB.db";
    int DB_VERSION = 1;
}
