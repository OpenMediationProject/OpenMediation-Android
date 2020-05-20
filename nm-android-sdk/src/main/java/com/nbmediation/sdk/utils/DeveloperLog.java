// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * 
 */
public class DeveloperLog {
    private static final String TAG = "OmDev";
    private static boolean debug = false;

    public static void enableDebug(Context context, boolean enable) {
        debug = enable || (context != null && new File(context.getFilesDir(), "log.txt").exists());
    }

    public static void LogD(String info) {
        if (!debug) {
            return;
        }
        Log.d(TAG, info);
    }

    public static void LogD(String tag, String info) {
        if (!debug) {
            return;
        }
        Log.d(TAG + ":" + tag, info);
    }

    public static void LogD(String info, Throwable t) {
        if (!debug) {
            return;
        }
        Log.d(TAG, info, t);
    }

    public static void LogE(String info) {
        if (!debug) {
            return;
        }
        Log.e(TAG, info);
    }

    public static void LogE(String info, Throwable t) {
        if (!debug) {
            return;
        }
        Log.e(TAG, info, t);
    }
}
