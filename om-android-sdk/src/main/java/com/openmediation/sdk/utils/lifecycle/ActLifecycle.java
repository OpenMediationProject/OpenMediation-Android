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

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class ActLifecycle implements Application.ActivityLifecycleCallbacks {

    private ConcurrentHashMap<Integer, Activity> mRefActivities = new ConcurrentHashMap<>();

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
        mRefActivities.put(activity.hashCode(), activity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (isAdActivity(activity)) {
            return;
        }
        mRefActivities.put(activity.hashCode(), activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (isAdActivity(activity)) {
            return;
        }
        mRefActivities.put(activity.hashCode(), activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        DeveloperLog.LogD("onActivityResumed: " + activity.toString());
        if (isAdActivity(activity)) {
            return;
        }
        mRefActivities.put(activity.hashCode(), activity);
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
        mRefActivities.remove(activity.hashCode());
    }

    public Activity getActivity() {
        Enumeration<Activity> elements = mRefActivities.elements();
        Activity act;
        while (elements.hasMoreElements()) {
            act = elements.nextElement();
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
