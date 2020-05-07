package com.openmediation.sdk.core.runnable;

import com.openmediation.sdk.core.AbstractAdsManager;

public class AdsScheduleTask implements Runnable {
    private AbstractAdsManager adsManager;

    public AdsScheduleTask(AbstractAdsManager manager) {
        adsManager = manager;
    }

    @Override
    public void run() {
        if (adsManager != null) {
            adsManager.loadAdWithInterval();
        }
    }
}
