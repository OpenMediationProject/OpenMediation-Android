// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;

import com.openmediation.sdk.utils.DeveloperLog;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class ReqExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Request #" + mCount.getAndIncrement());
        }
    };

    private static ThreadPoolExecutor mPoolExecutor;

    static {
        mPoolExecutor = new ThreadPoolExecutor(
                Math.max(2, Math.min(CPU_COUNT - 1, 4)),
                CPU_COUNT * 2 + 1,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(128),
                THREAD_FACTORY);
        mPoolExecutor.allowCoreThreadTimeOut(true);
        mPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                DeveloperLog.LogD("ReqExecutor", "execute rejected");
            }
        });
    }

    static void execute(Runnable command) {
        mPoolExecutor.execute(command);
    }
}