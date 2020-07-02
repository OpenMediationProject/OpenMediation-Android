package com.cloudtech.shell;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.cloudtech.shell.utils.YeLog;

/**
 * Created by jiantao.tu on 2018/4/18.
 */
@Deprecated
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TimingJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        YeLog.i("JobScheduler execute.");
        if(SdkImpl.request()) YeLog.i("JobScheduler execute OK.");
        jobFinished(params, true);
        return true;//这个方法如果另外起线程做一些费事的事情的话，最好返回为true，
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


}
