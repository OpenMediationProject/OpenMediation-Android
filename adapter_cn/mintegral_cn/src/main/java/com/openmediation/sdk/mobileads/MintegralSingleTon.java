// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.mbridge.msdk.MBridgeConstans;
import com.mbridge.msdk.MBridgeSDK;
import com.mbridge.msdk.out.MBridgeSDKFactory;
import com.mbridge.msdk.out.SDKInitStatusListener;
import com.openmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MintegralSingleTon {

    private InitState mInitState = InitState.NOT_INIT;
    private final List<InitCallback> mCallbacks = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<String, String> mBidAdUnits;

    public String removeBidAdUnit(String adUnitId) {
        return mBidAdUnits.remove(adUnitId);
    }

    public void putBidAdUnit(String adUnitId, String token) {
        mBidAdUnits.put(adUnitId, token);
    }

    public boolean containsKeyBidAdUnit(String adUnitId) {
        return mBidAdUnits.containsKey(adUnitId);
    }

    public String getBidToken(String adUnitId) {
        return mBidAdUnits.get(adUnitId);
    }

    private static class MintegralHolder {
        private static final MintegralSingleTon INSTANCE = new MintegralSingleTon();
    }

    private MintegralSingleTon() {
        mBidAdUnits = new ConcurrentHashMap<>();
    }

    public static MintegralSingleTon getInstance() {
        return MintegralHolder.INSTANCE;
    }

    public synchronized void initSDK(final Context context, final String appKey, final InitCallback listener, final Boolean consent, final Boolean ageRestricted) {
        if (context == null || TextUtils.isEmpty(appKey)) {
            if (listener != null) {
                AdLog.getSingleton().LogE("Init Failed: Context or AppKey is null");
                listener.onFailed("Init Failed: Context or AppKey is null");
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
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                initSDK(context, appKey, consent, ageRestricted);
            }
        };
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
            return;
        }
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void initSDK(Context context, String appKey, final Boolean consent, final Boolean ageRestricted) {
        try {
            String[] tmp = appKey.split("#");
            String appId = tmp[0];
            String key = tmp[1];
            final MBridgeSDK sdk = MBridgeSDKFactory.getMBridgeSDK();
            final Map<String, String> map = sdk.getMBConfigurationMap(appId, key);
            if (consent != null) {
                sdk.setConsentStatus(context, consent ? MBridgeConstans.IS_SWITCH_ON : MBridgeConstans.IS_SWITCH_OFF);
            }
            if (ageRestricted != null) {
                sdk.setDoNotTrackStatus(ageRestricted);
            }

            sdk.init(map, context.getApplicationContext(), new SDKInitStatusListener() {
                @Override
                public void onInitSuccess() {
                    initSuccess();
                }

                @Override
                public void onInitFail(String err) {
                    initError("Mintegral SDK Init Failed: " + err);
                }

            });
        } catch (Throwable e) {
            initError("Mintegral SDK Init Failed: " + e.getMessage());
        }
    }

    private void initSuccess() {
        AdLog.getSingleton().LogD("Mintegral SDK Init Success");
        mInitState = InitState.INIT_SUCCESS;
        for (InitCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onSuccess();
            }
        }
        mCallbacks.clear();
    }

    private void initError(String error) {
        AdLog.getSingleton().LogE(error);
        mInitState = InitState.NOT_INIT;
        for (InitCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onFailed(error);
            }
        }
        mCallbacks.clear();
    }

    public InitState getInitState() {
        return mInitState;
    }

    public boolean isInit() {
        return InitState.INIT_SUCCESS == mInitState;
    }

    /**
     * sdk Init State
     */
    public enum InitState {
        NOT_INIT,
        INIT_PENDING,
        INIT_SUCCESS
    }

    public interface InitCallback {
        void onSuccess();

        void onFailed(String msg);
    }
}
