package com.openmediation.sdk.core.runnable;

import com.openmediation.sdk.core.AbstractAdsManager;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.crash.CrashUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AdsScheduleTask implements Runnable {
    private AbstractAdsManager adsManager;
    private int delay;

    public AdsScheduleTask(AbstractAdsManager manager, int initDelay) {
        adsManager = manager;
        delay = initDelay;
    }

    @Override
    public void run() {
        try {
            DeveloperLog.LogD("execute adsScheduleTask");
            if (adsManager == null) {
                return;
            }
            adsManager.loadAdWithInterval();
            int count = adsManager.getAllLoadFailedCount();
            Map<Integer, Integer> rfs = adsManager.getRfs();
            if (rfs == null || rfs.isEmpty()) {
                return;
            }
            Set<Integer> keys = rfs.keySet();
            Integer[] integers = keys.toArray(new Integer[keys.size()]);
            int maxCount = integers[integers.length - 1];
            if (count >= maxCount) {
                delay = rfs.get(maxCount);
            } else {
                for (Integer key : keys) {
                    if (count < key) {
                        delay = rfs.get(key);
                        break;
                    }
                }
            }
            if (delay > 0) {
                DeveloperLog.LogD("execute adsScheduleTask delay : " + delay + ", fail count = " + count);
                WorkExecutor.execute(this, delay, TimeUnit.SECONDS);
            } else {
                DeveloperLog.LogD("can't execute adsScheduleTask delay : " + delay);
            }
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }
}
