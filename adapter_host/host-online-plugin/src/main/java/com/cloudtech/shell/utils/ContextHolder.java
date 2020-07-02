package com.cloudtech.shell.utils;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Keep;

public class ContextHolder {

    private static Context globalAppContext;

    public synchronized static void init(Context context) {
        if (context != null) {
            if (context instanceof Application) {
                globalAppContext = context;
            } else {
                globalAppContext = context.getApplicationContext();
            }
        }
    }

    public synchronized static Context getGlobalAppContext() {
        if (globalAppContext == null) {
            try {
                Application application = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication").invoke(null, (Object[]) null);
                if (application != null) {
                    globalAppContext = application;
                    return application;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Application application = (Application) Class.forName("android.app.AppGlobals")
                        .getMethod("getInitialApplication").invoke(null, (Object[]) null);
                if (application != null) {
                    globalAppContext = application;
                    return application;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new IllegalStateException("ContextHolder is not initialed, it is recommend to init with application context.");
        }
        return globalAppContext;
    }

    public static void clean(){
        globalAppContext=null;
    }
}
