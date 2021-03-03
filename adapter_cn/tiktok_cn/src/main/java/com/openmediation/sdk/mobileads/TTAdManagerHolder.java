// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.util.concurrent.atomic.AtomicBoolean;

public class TTAdManagerHolder {

    private static final AtomicBoolean sInit = new AtomicBoolean(false);

    static TTAdManager get() {
        if (!sInit.get()) {
            throw new RuntimeException("TTAdSdk is not init, please check.");
        }
        return TTAdSdk.getAdManager();
    }

    public static void init(Context context, String appId, InitCallback callback) {
        if (context == null) {
            return;
        }
        doInit(context, appId, callback);
    }

    private static void doInit(final Context context, final String appId, final InitCallback callback) {
        if (sInit.get()) {
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            TTAdSdk.init(context, buildConfig(context, appId));
            onInitFinish(callback);
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                TTAdSdk.init(context, buildConfig(context, appId));
                onInitFinish(callback);
            }
        });
    }

    private static TTAdConfig buildConfig(Context context, String appId) {
        return new TTAdConfig.Builder()
                .appId(appId)
                .appName(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString())
                .build();
    }

    private static void onInitFinish(InitCallback callback) {
        sInit.set(true);
        if (callback != null) {
            callback.onSuccess();
        }
    }

    public interface InitCallback {
        void onSuccess();
    }

    public static int[] getScreenPx(Context context) {
        return new int[] {context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels};
    }

    public static float[] getScreenDp(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        int[] screenPx = getScreenPx(context);
        return new float[]{screenPx[0] / density, screenPx[1] / density};
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}
