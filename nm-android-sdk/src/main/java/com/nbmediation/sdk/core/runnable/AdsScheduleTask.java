package com.nbmediation.sdk.core.runnable;

import com.nbmediation.sdk.core.AbstractAdsManager;

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
