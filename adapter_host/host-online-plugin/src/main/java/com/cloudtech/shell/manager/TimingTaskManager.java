package com.cloudtech.shell.manager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.SystemClock;

import com.cloudtech.shell.Constants;
import com.cloudtech.shell.receiver.AlarmReceiver;
import com.cloudtech.shell.TimingJobService;
import com.cloudtech.shell.utils.Utils;
import com.cloudtech.shell.utils.YeLog;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiantao.tu on 2018/4/18.
 */
@Deprecated
public class TimingTaskManager {

    private final static String ALARM_ACTION = "alarm.receiver.intent.trigger";

    private final static String TAG = "TimingTaskManager";

    private static PendingIntent pi = null;
    private static AlarmReceiver receiver = null;

    private static final int jobId = Integer.MAX_VALUE - 1000;


    public static void setRepeating(Context context, int intervalSecond) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_ACTION);
//        intent.setClass(context, AlarmReceiver.class);
        if (receiver == null) {
            receiver = new AlarmReceiver();
            context.registerReceiver(receiver, new IntentFilter(ALARM_ACTION));
        }
        if (manager != null) {
            if (pi == null)
                pi = PendingIntent.getBroadcast(context, 0, intent, 0);
            else {
                manager.cancel(pi);
            }
            long triggerAtMillis = SystemClock.elapsedRealtime() + intervalSecond * 1000;
            if (Build.VERSION.SDK_INT >= 19) {
                manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi);
            } else {
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi);
            }
//            com.cloudtech.shell.manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock
//                    .elapsedRealtime(), intervalSecond * 1000, pi);
        }
    }


    public static void doService(Context context, int intervalSecond) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ComponentName cn = new ComponentName(context, TimingJobService.class);
            try {
                ServiceInfo info = context.getPackageManager().getServiceInfo(cn, 0);
                if (info != null) {
                    if (Constants.BIND_JOB_SERVICE.equals(info.permission)) {
                        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context
                                .JOB_SCHEDULER_SERVICE);
                        Objects.requireNonNull(jobScheduler).cancel(jobId);
                        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName
                                (context, TimingJobService.class));  //指定哪个JobService执行操作
                        if (Utils.checkPermission(context, Manifest.permission.RECEIVE_BOOT_COMPLETED))
                            builder.setPersisted(true);
                        Long time = TimeUnit.SECONDS.toMillis(intervalSecond);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            builder.setPeriodic(time, time);
//                        } else {
//                            builder.setPeriodic(time);
//                        }
                        builder.setMinimumLatency(time); //执行的最小延迟时间
                        builder.setOverrideDeadline(time);  //执行的最长延时时间
                        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);  //非漫游网络状态
                        builder.setBackoffCriteria(TimeUnit.SECONDS.toMillis(30), JobInfo
                                .BACKOFF_POLICY_LINEAR);
                        //线性重试方案
                        builder.setRequiresCharging(false); // 未充电状态
                        Objects.requireNonNull(jobScheduler).schedule(builder.build());
                    } else {
                        YeLog.e(TAG, "is not permission " + Constants.BIND_JOB_SERVICE + " " +
                                "statement.");
                    }

                } else {
                    YeLog.e(TAG, "manifest Non-existent TimingJobService.");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                YeLog.e(TAG, "manifest Non-existent TimingJobService.");
            }

        }
    }

    public static void clean(){
        pi=null;
        receiver=null;
    }

}
