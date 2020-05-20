// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class InsExecutor {
    private final static ScheduledThreadPoolExecutor POOL;

    static {
        POOL = new ScheduledThreadPoolExecutor(12);
        POOL.setKeepAliveTime(60, TimeUnit.SECONDS);
        POOL.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                DeveloperLog.LogD("execute rejected");
            }
        });
    }

    private InsExecutor() {
    }


    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return POOL.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public static void execute(Runnable command) {
        POOL.execute(command);
    }

    public static ScheduledFuture<?> execute(Runnable command, long delay, TimeUnit unit) {
        return POOL.schedule(command, delay, unit);
    }

    public static void remove(Runnable runnable) {
        POOL.remove(runnable);
    }
}
