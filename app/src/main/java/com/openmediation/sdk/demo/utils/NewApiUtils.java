// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.demo.utils;

import android.util.Log;

public class NewApiUtils {

    public static final String TAG = "AdtDebug";
    public static boolean ENABLE_LOG = false;
    public static final String APPKEY = "SfQ10FmY4hOlIoXrngL15lbTc0SYavdA";

    public static final String P_BANNER = "6042";
    public static final String P_NATIVE = "6041";

    public static void printLog(String msg) {
        if (ENABLE_LOG) {
            Log.e(TAG, msg);
        }
    }
}
