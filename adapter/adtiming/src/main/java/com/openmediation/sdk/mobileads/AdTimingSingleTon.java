package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.adtbid.sdk.AdTimingAds;
import com.adtbid.sdk.InitCallback;
import com.adtbid.sdk.utils.error.AdTimingError;
import com.adtbid.sdk.utils.error.ErrorCode;

import java.util.HashSet;
import java.util.Set;

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
        mInitState = InitState.INIT_FAIL;
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
        initCallbacks = new HashSet<>();
    }

    public static AdTimingSingleTon getInstance() {
        return AdTimingHolder.INSTANCE;
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
            case INIT_FAIL:
                if (callback != null) {
                    callback.onError(new AdTimingError(ErrorCode.CODE_INIT_UNKNOWN_ERROR));
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

    /**
     * MoPub sdk init state
     */
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
        /**
         *
         */
        INIT_FAIL
    }
}
