/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.utils.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActLifecycle implements Application.ActivityLifecycleCallbacks {

    private List<Activity> mRefActivities = new CopyOnWriteArrayList<>();

    private static final class DKLifecycleHolder {
        private static final ActLifecycle INSTANCE = new ActLifecycle();
    }

    private ActLifecycle() {
    }

    public static ActLifecycle getInstance() {
        return DKLifecycleHolder.INSTANCE;
    }

    public void init() {
        AdtUtil.getInstance().getApplicationContext().registerActivityLifecycleCallbacks(this);
    }

    public void setActivity(Activity activity) {
        if (!mRefActivities.contains(activity)) {
            mRefActivities.add(activity);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (isAdActivity(activity)) {
            return;
        }
        if (!mRefActivities.contains(activity)) {
            mRefActivities.add(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (isAdActivity(activity)) {
            return;
        }
        if (!mRefActivities.contains(activity)) {
            mRefActivities.add(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        DeveloperLog.LogD("onActivityResumed: " + activity.toString());
        if (isAdActivity(activity)) {
            return;
        }
        if (!mRefActivities.contains(activity)) {
            mRefActivities.add(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        DeveloperLog.LogD("onActivityDestroyed: " + activity.toString());
        mRefActivities.remove(activity);
        DeveloperLog.LogD("after onActivityDestroyed: " + mRefActivities.size());
    }

    public Activity getActivity() {
        if (mRefActivities == null || mRefActivities.isEmpty()) {
            return null;
        }
        int size = mRefActivities.size();
        for (int i = size - 1; i >=0; i --) {
            Activity act = mRefActivities.get(i);
            if (act.isFinishing() || (Build.VERSION.SDK_INT >= 17 && act.isDestroyed())) {
                continue;
            }
            return act;
        }
        return null;
    }

    private boolean isAdActivity(Activity activity) {
        String address = activity.toString();
        for (String s : TypeConstant.ADS_ACT) {
            if (address.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
