// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.openmediation.sdk.utils.error.Error;

/**
 * 
 */
public class AdLog {
    private static class AdLogHolder {
        private static final AdLog INSTANCE = new AdLog();
    }

    public static AdLog getSingleton() {
        return AdLogHolder.INSTANCE;
    }

    private boolean isDebug = false;
    private static final String TAG = "OmAds";

    private AdLog() {

    }

    public void isDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void LogD(String info) {
        if (isDebug) {
            Log.d(TAG, info);
        }
    }

    public void LogD(String tag, String info) {
        if (isDebug) {
            Log.d(TAG + ":" + tag, info);
        }
    }

    public void LogD(String info, Throwable t) {
        if (isDebug) {
            Log.d(TAG, info, t);
        }
    }


    public void LogE(String tag, String info) {
        if (isDebug) {
            Log.e(TAG + ":" + tag, info);
        }
    }

    public void LogE(String info) {
        if (isDebug) {
            Log.e(TAG, info);
        }
    }

    public void LogE(Error error) {
        if (isDebug && error != null) {
            Log.e(TAG, error.toString());
        }
    }

    public void LogE(String info, Throwable throwable) {
        if (isDebug) {
            Log.e(TAG, info, throwable);
        }
    }
}
