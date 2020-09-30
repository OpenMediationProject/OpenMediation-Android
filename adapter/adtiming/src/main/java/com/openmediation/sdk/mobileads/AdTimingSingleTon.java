package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.adtiming.mediationsdk.adt.AdTimingAds;

public class AdTimingSingleTon {

    private boolean isDidInit;

    private static class AdTimingHolder {
        private static final AdTimingSingleTon INSTANCE = new AdTimingSingleTon();
    }

    private AdTimingSingleTon() {
    }

    public static AdTimingSingleTon getInstance() {
        return AdTimingHolder.INSTANCE;
    }

    public synchronized boolean initAdTiming(Activity activity) {
        if (!isDidInit) {
            if (activity == null) {
                return false;
            }
            AdTimingAds.init(activity);
            isDidInit = true;
            return true;
        }
        return true;
    }
}
