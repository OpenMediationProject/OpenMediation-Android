// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.utils.AdLog;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.MediationMetaData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UnitySingleTon {
    private InitState mInitState = InitState.NOT_INIT;
    private List<IUnityAdsInitializationListener> mCallbacks = new CopyOnWriteArrayList<>();

    private static class UnityHolder {
        private static final UnitySingleTon INSTANCE = new UnitySingleTon();
    }

    private UnitySingleTon() {
    }

    public static UnitySingleTon getInstance() {
        return UnityHolder.INSTANCE;
    }

    public void init(final Context context, String appKey, final IUnityAdsInitializationListener listener) {
        if (context == null || TextUtils.isEmpty(appKey)) {
            AdLog.getSingleton().LogE("Init Failed: Context is null or AppKey is empty!");
            if (listener != null) {
                listener.onInitializationFailed(UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT, UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT.name() + ", Context is null or AppKey is empty");
            }
            return;
        }
        if (InitState.INIT_SUCCESS == mInitState) {
            if (listener != null) {
                listener.onInitializationComplete();
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
            MediationMetaData mediationMetaData = new MediationMetaData(context);
            mediationMetaData.setName("AdTiming");
            mediationMetaData.setVersion(com.openmediation.sdk.mobileads.unity.BuildConfig.VERSION_NAME);
            mediationMetaData.commit();

            UnityAds.initialize(context.getApplicationContext(), appKey, new IUnityAdsInitializationListener() {

                @Override
                public void onInitializationComplete() {
                    AdLog.getSingleton().LogD("UnityAds SDK Init Success");
                    mInitState = InitState.INIT_SUCCESS;
                    for (IUnityAdsInitializationListener callback : mCallbacks) {
                        if (callback != null) {
                            callback.onInitializationComplete();
                        }
                    }
                    mCallbacks.clear();
                }

                @Override
                public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                    mInitState = InitState.INIT_FAIL;
                    String errorMsg = message;
                    if (error != null) {
                        errorMsg = error.name() + ", " + message;
                    }
                    AdLog.getSingleton().LogD("UnityAds SDK Init Failed: " + message);
                    for (IUnityAdsInitializationListener callback : mCallbacks) {
                        if (callback != null) {
                            callback.onInitializationFailed(error, errorMsg);
                        }
                    }
                    mCallbacks.clear();
                }
            });
        } catch (Exception e) {
            mInitState = InitState.INIT_FAIL;
            if (listener != null) {
                String errorMsg = UnityAds.UnityAdsInitializationError.INTERNAL_ERROR.name() + ", " + e.getMessage();
                listener.onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, errorMsg);
            }
        }
    }

    public InitState getInitState() {
        return mInitState;
    }

    /**
     * sdk Init State
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
