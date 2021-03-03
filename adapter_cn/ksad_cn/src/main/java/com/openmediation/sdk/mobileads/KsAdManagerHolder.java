// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.SdkConfig;

import java.util.concurrent.atomic.AtomicBoolean;

public class KsAdManagerHolder {

    private static final AtomicBoolean sInit = new AtomicBoolean(false);

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
            boolean success = KsAdSDK.init(context.getApplicationContext(), getConfig(context, appId));
            onInitFinish(success, callback);
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                boolean success = KsAdSDK.init(context.getApplicationContext(), getConfig(context, appId));
                onInitFinish(success, callback);
            }
        });
    }

    private static void onInitFinish(boolean result, final InitCallback callback) {
        if (result) {
            sInit.set(true);
            if (callback != null) {
                callback.onSuccess();
            }
        } else {
            if (callback != null) {
                callback.onFailed();
            }
        }
    }

    private static SdkConfig getConfig(Context context, String appId) {
        return new SdkConfig.Builder()
                .appId(appId)
                .appName(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString())
                .showNotification(true)
                .build();
    }

    public static boolean isInit() {
        return sInit.get();
    }

    public interface InitCallback {
        void onSuccess();

        void onFailed();
    }
}
