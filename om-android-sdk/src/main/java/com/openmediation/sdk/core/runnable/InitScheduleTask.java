package com.openmediation.sdk.core.runnable;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.core.InitImp;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Configurations;

import java.util.concurrent.TimeUnit;

public class InitScheduleTask implements Runnable {

    static InitScheduleTask mTask;

    public static void startTask(Configurations config) {
        if (mTask != null) {
            WorkExecutor.remove(mTask);
            mTask = null;
        }
        if (config == null || config.getRi() <= 0) {
            return;
        }
        int delay = config.getRi();
        DeveloperLog.LogD("Execute Re Init SDK Delay : " + delay + " min");
        mTask = new InitScheduleTask();
        WorkExecutor.execute(mTask, delay, TimeUnit.MINUTES);
    }

    public InitScheduleTask() {
    }

    @Override
    public void run() {
        try {
            DeveloperLog.LogD("Execute Re Init SDK Task");
            InitImp.startReInitSDK(new InitCallback() {
                @Override
                public void onSuccess() {
                    DeveloperLog.LogD("Execute Re Init SDK Success");
                    mTask = null;
                }

                @Override
                public void onError(Error result) {
                    DeveloperLog.LogD("Execute Re Init SDK Task Failed: " + result);
                    mTask = null;
                }
            });
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }
}
