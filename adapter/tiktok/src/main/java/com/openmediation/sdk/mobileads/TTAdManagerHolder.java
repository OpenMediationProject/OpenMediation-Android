// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.api.init.PAGConfig;
import com.bytedance.sdk.openadsdk.api.init.PAGSdk;
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

    public void init(Context context, String appId, Boolean consent, Boolean ageRestricted, Boolean privacyLimit, InitCallback callback) {
        if (context == null || TextUtils.isEmpty(appId)) {
            if (callback != null) {
                callback.onFailed(-1, "Context or AppId is null");
            }
            return;
        }
        doInit(context, appId, consent, ageRestricted, privacyLimit, callback);
    }

    private void doInit(final Context context, final String appId, final Boolean consent, final Boolean ageRestricted, final Boolean privacyLimit, final InitCallback callback) {
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
        final PAGSdk.PAGInitCallback initCallback = new PAGSdk.PAGInitCallback() {

            @Override
            public void success() {
                onInitFinish();
            }

            @Override
            public void fail(int code, String message) {
                onInitFailed(code, message);
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PAGSdk.init(context, buildConfig(appId, consent, ageRestricted, privacyLimit), initCallback);
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private static PAGConfig buildConfig(String appId, Boolean consent, Boolean ageRestricted, Boolean privacyLimit) {
        PAGConfig.Builder builder = new PAGConfig.Builder()
                .appId(appId);
        if (consent != null) {
            // 0:User doesn't grant consent, 1: User has granted the consent
            builder.setGDPRConsent(consent ? 1 : 0);
        }
        if (ageRestricted != null) {
            // 0:adult ，1:child
            builder.setChildDirected(ageRestricted ? 1 : 0);
        }
        if (privacyLimit != null) {
            // 0: "sale" of personal information is permitted, 1: user has opted out of "sale" of personal information
            builder.setDoNotSell(privacyLimit ? 1 : 0);
        }
        return builder.build();
    }

    private void onInitFinish() {
        AdLog.getSingleton().LogD("TikTokAdapter SDK Init Success");
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
        AdLog.getSingleton().LogE("TikTokAdapter SDK Init Failed, code: " + code + ", msg: " + msg);
        for (InitCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onFailed(code, msg);
            }
        }
        mCallbacks.clear();
    }

    public interface InitCallback {
        void onSuccess();

        void onFailed(int code, String msg);
    }

    enum InitState {
        NOT_INIT,
        INIT_PENDING,
        INIT_SUCCESS
    }
}
