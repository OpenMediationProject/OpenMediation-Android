// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.app.Application;
import android.content.Context;

import com.openmediation.sdk.utils.crash.CrashUtil;

/**
 * 
 */
public class AdtUtil {
    private static Application application;

    public static void init(Context context) {
        application = (Application) context.getApplicationContext();

        DeveloperLog.enableDebug(application, false);
        CrashUtil.getSingleton().init();
    }

    public static void setApplication(Context context) {
        application = (Application) context.getApplicationContext();
    }

    public static Application getApplication() {
        if (application != null) {
            return application;
        } else {
            try {
                return (Application) Class.forName("android.app.ActivityThread").
                        getMethod("currentApplication").invoke(null);
            } catch (Exception e) {
                DeveloperLog.LogD("getApplication error", e);
            }

            try {
                return (Application) Class.forName("android.app.AppGlobals").
                        getMethod("getInitialApplication").invoke(null);
            } catch (Exception e) {
                DeveloperLog.LogD("getApplication error", e);
            }
            return null;
        }
    }
}
