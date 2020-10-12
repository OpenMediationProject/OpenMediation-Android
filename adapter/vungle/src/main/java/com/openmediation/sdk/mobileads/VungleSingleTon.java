// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.utils.AdLog;
import com.vungle.warren.InitCallback;
import com.vungle.warren.Plugin;
import com.vungle.warren.Vungle;
import com.vungle.warren.VungleApiClient;
import com.vungle.warren.error.VungleException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VungleSingleTon {
    private InitState mInitState = InitState.NOT_INIT;
    private List<InitCallback> mCallbacks = new CopyOnWriteArrayList<>();

    private static class VungleHolder {
        private static final VungleSingleTon INSTANCE = new VungleSingleTon();
    }

    private VungleSingleTon() {
    }

    public static VungleSingleTon getInstance() {
        return VungleHolder.INSTANCE;
    }

    public void init(final Context context, String appKey, final InitCallback listener) {
        if (context == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                listener.onError(new VungleException(VungleException.APPLICATION_CONTEXT_REQUIRED));
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
        try {
            mInitState = InitState.INIT_PENDING;
            Plugin.addWrapperInfo(VungleApiClient.WrapperFramework.vunglehbs, "1.0.0");
            Vungle.init(appKey, context.getApplicationContext(), new InitCallback() {
                @Override
                public void onSuccess() {
                    AdLog.getSingleton().LogD("Vungle SDK Init Success");
                    mInitState = InitState.INIT_SUCCESS;
                    for (InitCallback callback : mCallbacks) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                    mCallbacks.clear();
                }

                @Override
                public void onError(VungleException exception) {
                    AdLog.getSingleton().LogD("Vungle SDK Init Failed: " + exception.getLocalizedMessage());
                    mInitState = InitState.INIT_FAIL;
                    for (InitCallback callback : mCallbacks) {
                        if (callback != null) {
                            callback.onError(exception);
                        }
                    }
                    mCallbacks.clear();
                }

                @Override
                public void onAutoCacheAdAvailable(String placementId) {

                }
            });
        } catch (Exception e) {
            mInitState = InitState.INIT_FAIL;
            if (listener != null) {
                listener.onError(new VungleException(VungleException.UNKNOWN_ERROR));
            }
        }
    }

    public InitState getInitState() {
        return mInitState;
    }

    /**
     * Vungle sdk Init State
     */
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
}
