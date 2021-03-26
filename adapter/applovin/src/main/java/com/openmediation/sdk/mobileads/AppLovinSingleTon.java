// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppLovinSingleTon {
    private InitState mInitState = InitState.NOT_INIT;
    private AppLovinSdk mAppLovinSdk;
    private final List<InitCallback> mCallbacks = new CopyOnWriteArrayList<>();

    private static class AppLovinHolder {
        private static final AppLovinSingleTon INSTANCE = new AppLovinSingleTon();
    }

    private AppLovinSingleTon() {
    }

    public static AppLovinSingleTon getInstance() {
        return AppLovinHolder.INSTANCE;
    }

    public void init(final Context context, String appKey, final InitCallback listener) {
        if (context == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                listener.onFailed("Init Failed: AppKey is empty");
            }
            return;
        }
        if (InitState.INIT_SUCCESS == mInitState) {
            if (listener != null) {
                listener.onSuccess();
            }
            return;
        }
        if (listener != null) {
            mCallbacks.add(listener);
        }
        if (InitState.INIT_PENDING == mInitState) {
            return;
        }
        mInitState = InitState.INIT_PENDING;
        try {
            AppLovinSdkSettings settings = new AppLovinSdkSettings(context);
            mAppLovinSdk = AppLovinSdk.getInstance(appKey, settings,
                    context);
            mAppLovinSdk.initializeSdk(new AppLovinSdk.SdkInitializationListener() {
                @Override
                public void onSdkInitialized(AppLovinSdkConfiguration config) {
                    AdLog.getSingleton().LogD("AppLovin SDK Init Success");
                    mInitState = InitState.INIT_SUCCESS;
                    for (InitCallback callback : mCallbacks) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                    mCallbacks.clear();
                }
            });
        } catch (Exception e) {
            mInitState = InitState.INIT_FAIL;
            for (InitCallback callback : mCallbacks) {
                if (callback != null) {
                    listener.onFailed("Init Failed: " + e.getMessage());
                }
            }
            mCallbacks.clear();
        }
    }

    public AppLovinSdk getAppLovinSdk() {
        return mAppLovinSdk;
    }

    public enum InitState {
        /**
         *
         */
        NOT_INIT,
        /**
         *
         */
        INIT_PENDING,
        /**
         *
         */
        INIT_SUCCESS,
        /**
         *
         */
        INIT_FAIL
    }

    public interface InitCallback {
        void onSuccess();

        void onFailed(String msg);
    }
}
