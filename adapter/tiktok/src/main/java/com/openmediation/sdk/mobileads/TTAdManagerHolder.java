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
        return TTAdSdk.getAdManager();
    }

    public static void init(Context context, String appId, Boolean consent, Boolean ageRestricted, InitCallback callback) {
        if (context == null) {
            return;
        }
        doInit(context, appId, consent, ageRestricted, callback);
    }

    private static void doInit(final Context context, final String appId, final Boolean consent, final Boolean ageRestricted, final InitCallback callback) {
        if (sInit.get()) {
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        final TTAdSdk.InitCallback initCallback = new TTAdSdk.InitCallback() {

            @Override
            public void success() {
                onInitFinish(callback);
            }

            @Override
            public void fail(int code, String msg) {
                onInitFailed(callback, code, msg);
            }
        };

        if (Looper.getMainLooper() == Looper.myLooper()) {
            TTAdSdk.init(context, buildConfig(context, appId, consent, ageRestricted), initCallback);
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                TTAdSdk.init(context, buildConfig(context, appId, consent, ageRestricted), initCallback);
            }
        });
    }

    private static void onInitFinish(InitCallback callback) {
        sInit.set(true);
        if (callback != null) {
            callback.onSuccess();
        }
    }

    private static void onInitFailed(InitCallback callback, int code, String msg) {
        sInit.set(false);
        if (callback != null) {
            callback.onFailed(code, msg);
        }
    }

    private static TTAdConfig buildConfig(Context context, String appId, Boolean consent, Boolean ageRestricted) {
        TTAdConfig.Builder builder = new TTAdConfig.Builder()
                .appId(appId)
                .appName(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString());
        if (consent != null) {
            // 0 close GDRP Privacy protection ，1: open GDRP Privacy protection
            builder.setGDPR(consent ? 1 : 0);
        }
        if (ageRestricted != null) {
            // 0:adult ，1:child
            builder.coppa(ageRestricted ? 1 : 0);
        }
        return builder.build();
    }

    public static int[] getScreenPx(Context context) {
        return new int[] {context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels};
    }

    public interface InitCallback {
        void onSuccess();
        void onFailed(int code, String msg);
    }
}
