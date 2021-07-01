// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TTAdManagerHolder {

    private InitState mInitState = InitState.NOT_INIT;
    private List<InitCallback> mCallbacks = new CopyOnWriteArrayList<>();

    private static class Holder {
        private static final TTAdManagerHolder INSTANCE = new TTAdManagerHolder();
    }

    private TTAdManagerHolder() {
    }

    public static TTAdManagerHolder getInstance() {
        return Holder.INSTANCE;
    }

    TTAdManager getAdManager() {
        return TTAdSdk.getAdManager();
    }

    public void init(Context context, String appId, InitCallback callback) {
        if (context == null || TextUtils.isEmpty(appId)) {
            if (callback != null) {
                callback.onFailed(-1, "Context or AppId is null");
            }
            return;
        }
        doInit(context, appId, callback);
    }

    private void doInit(final Context context, final String appId, final InitCallback callback) {
        if (InitState.INIT_SUCCESS == mInitState) {
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }
        if (callback != null) {
            mCallbacks.add(callback);
        }
        if (InitState.INIT_PENDING == mInitState) {
            return;
        }
        mInitState = InitState.INIT_PENDING;
        final TTAdSdk.InitCallback initCallback = new TTAdSdk.InitCallback() {

            @Override
            public void success() {
                onInitFinish();
            }

            @Override
            public void fail(int code, String msg) {
                onInitFailed(code, msg);
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                TTAdSdk.init(context, buildConfig(context, appId), initCallback);
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private static TTAdConfig buildConfig(Context context, String appId) {
        return new TTAdConfig.Builder()
                .appId(appId)
                .appName(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString())
                .build();
    }

    private void onInitFinish() {
        mInitState = InitState.INIT_SUCCESS;
        AdLog.getSingleton().LogD("TikTok SDK Init Success");
        mInitState = InitState.INIT_SUCCESS;
        for (InitCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onSuccess();
            }
        }
        mCallbacks.clear();
    }

    private void onInitFailed(int code, String msg) {
        mInitState = InitState.NOT_INIT;
        AdLog.getSingleton().LogE("TikTok SDK Init Failed, code: " + code + ", msg: " + msg);
        for (InitCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onFailed(code, msg);
            }
        }
        mCallbacks.clear();
    }

    boolean isInit() {
        return mInitState == InitState.INIT_SUCCESS;
    }

    public interface InitCallback {
        void onSuccess();

        void onFailed(int code, String msg);
    }

    public static int[] getScreenPx(Context context) {
        return new int[]{context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels};
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

    enum InitState {
        NOT_INIT,
        INIT_PENDING,
        INIT_SUCCESS,
    }
}
