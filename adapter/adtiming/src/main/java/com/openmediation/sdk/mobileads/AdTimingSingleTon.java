package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.adtbid.sdk.AdTimingAds;
import com.adtbid.sdk.InitCallback;
import com.adtbid.sdk.utils.error.AdTimingError;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class AdTimingSingleTon implements InitCallback {

    private volatile InitState mInitState = InitState.NOT_INIT;
    private volatile Set<AdTimingInitCallback> initCallbacks;

    @Override
    public void onSuccess() {
        mInitState = InitState.INIT_SUCCESS;
        for (AdTimingInitCallback initCallback : initCallbacks) {
            if (initCallback != null) {
                initCallback.onSuccess();
            }
        }
        initCallbacks.clear();
    }

    @Override
    public void onError(AdTimingError adTimingError) {
        mInitState = InitState.NOT_INIT;
        for (AdTimingInitCallback initCallback : initCallbacks) {
            if (initCallback != null) {
                initCallback.onError(adTimingError);
            }
        }
        initCallbacks.clear();
    }

    private static class AdTimingHolder {
        private static final AdTimingSingleTon INSTANCE = new AdTimingSingleTon();
    }

    private AdTimingSingleTon() {
        initCallbacks = new CopyOnWriteArraySet<>();
    }

    public static AdTimingSingleTon getInstance() {
        return AdTimingHolder.INSTANCE;
    }

    public boolean isInit() {
        return mInitState == InitState.INIT_SUCCESS;
    }

    public synchronized void initAdTiming(Context context, String appKey, AdTimingInitCallback callback) {
        switch (mInitState) {
            case NOT_INIT:
                initCallbacks.add(callback);
                init(context, appKey);
                break;
            case INIT_PENDING:
                initCallbacks.add(callback);
                break;
            case INIT_SUCCESS:
                if (callback != null) {
                    callback.onSuccess();
                }
                break;
        }
    }

    private void init(Context context, String appKey) {
        mInitState = InitState.INIT_PENDING;
        AdTimingAds.init(context, appKey, this);
    }

    interface AdTimingInitCallback {
        void onSuccess();

        void onError(AdTimingError adTimingError);
    }

    enum InitState {
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
    }
}
