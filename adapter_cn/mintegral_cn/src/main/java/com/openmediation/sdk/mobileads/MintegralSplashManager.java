// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.mbridge.msdk.out.MBSplashHandler;
import com.mbridge.msdk.out.MBSplashLoadListener;
import com.mbridge.msdk.out.MBSplashShowListener;
import com.mbridge.msdk.out.MBridgeIds;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralSplashManager {

    private final ConcurrentHashMap<String, MBSplashHandler> mSplashAds;

    private static class Holder {
        private static final MintegralSplashManager INSTANCE = new MintegralSplashManager();
    }

    private MintegralSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static MintegralSplashManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final SplashAdCallback callback, Boolean userConsent, Boolean ageRestricted) {
        String appKey = (String) extras.get("AppKey");
        MintegralSingleTon.getInstance().initSDK(context.getApplicationContext(), appKey, new MintegralSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSplashAdInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onSplashAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "MintegralAdapter", msg));
                }
            }
        }, userConsent, ageRestricted);
    }

    public void loadAd(Context context, final String adUnitId, final Map<String, Object> extras, final SplashAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String payload = "";
                    if (extras != null && extras.containsKey(MintegralAdapter.PAY_LOAD)) {
                        payload = String.valueOf(extras.get(MintegralAdapter.PAY_LOAD));
                    }
                    int fetchDelay = 0;
                    try {
                        fetchDelay = Integer.parseInt(extras.get("Timeout").toString());
                    } catch (Exception ignored) {
                    }
                    MBSplashHandler splashHandler = new MBSplashHandler("", adUnitId);
                    if (fetchDelay > 0) {
                        splashHandler.setLoadTimeOut(fetchDelay / 1000);
                    }
                    InnerSplashAdListener listener = new InnerSplashAdListener(adUnitId, callback);
                    splashHandler.setSplashLoadListener(listener);
                    listener.setSplashAD(splashHandler);
                    if (TextUtils.isEmpty(payload)) {
                        MintegralSingleTon.getInstance().removeBidAdUnit(adUnitId);
                        splashHandler.preLoad();
                    } else {
                        MintegralSingleTon.getInstance().putBidAdUnit(adUnitId, payload);
                        splashHandler.preLoadByToken(payload);
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, "MintegralAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    public void destroyAd(final String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return;
        }
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MBSplashHandler splashHandler = mSplashAds.remove(adUnitId);
                    if (splashHandler != null) {
                        splashHandler.onDestroy();
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    public void onPause() {
        try {
            for (MBSplashHandler splashHandler : mSplashAds.values()) {
                if (splashHandler != null) {
                    splashHandler.onPause();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public void onResume() {
        try {
            for (MBSplashHandler splashHandler : mSplashAds.values()) {
                if (splashHandler != null) {
                    splashHandler.onResume();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mSplashAds.containsKey(adUnitId)) {
            MBSplashHandler splashHandler = mSplashAds.get(adUnitId);
            if (splashHandler == null) {
                return false;
            }
            if (MintegralSingleTon.getInstance().containsKeyBidAdUnit(adUnitId)) {
                String token = MintegralSingleTon.getInstance().getBidToken(adUnitId);
                return splashHandler.isReady(token);
            } else {
                return splashHandler.isReady();
            }
        }
        return false;
    }

    public void showAd(final String adUnitId, final ViewGroup container, final SplashAdCallback callback) {
        if (container == null) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "MintegralAdapter", "Splash container is null, please use \"SplashAd.showAd(String placementId, ViewGroup container)\""));
            }
            return;
        }
//        if (!isAdAvailable(adUnitId)) {
//            if (callback != null) {
//                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
//                        AdapterErrorBuilder.AD_UNIT_SPLASH, "MintegralAdapter", "SplashAd not ready"));
//            }
//            return;
//        }

        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MBSplashHandler splashHandler = mSplashAds.get(adUnitId);
                    splashHandler.setSplashShowListener(new MBSplashShowListener() {
                        @Override
                        public void onShowSuccessed(MBridgeIds mBridgeIds) {
                            AdLog.getSingleton().LogD("MintegralSplash ad onShowSuccessed: " + adUnitId);
                            if (callback != null) {
                                callback.onSplashAdShowSuccess();
                            }
                        }

                        @Override
                        public void onShowFailed(MBridgeIds mBridgeIds, String msg) {
                            if (callback != null) {
                                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                                        AdapterErrorBuilder.AD_UNIT_SPLASH, "MintegralAdapter", msg));
                            }
                            destroyAd(adUnitId);
                        }

                        @Override
                        public void onAdClicked(MBridgeIds mBridgeIds) {
                            AdLog.getSingleton().LogD("MintegralSplash ad onAdClicked: " + adUnitId);
                            if (callback != null) {
                                callback.onSplashAdAdClicked();
                            }
                        }

                        @Override
                        public void onDismiss(MBridgeIds mBridgeIds, int type) {
                            AdLog.getSingleton().LogD("MintegralSplash ad onDismiss: " + adUnitId);
                            if (callback != null) {
                                callback.onSplashAdDismissed();
                            }
                            destroyAd(adUnitId);
                        }

                        @Override
                        public void onAdTick(MBridgeIds mBridgeIds, long millisUntilFinished) {
                            if (callback != null) {
                                callback.onSplashAdTick(millisUntilFinished);
                            }
                        }

                        @Override
                        public void onZoomOutPlayStart(MBridgeIds mBridgeIds) {

                        }

                        @Override
                        public void onZoomOutPlayFinish(MBridgeIds mBridgeIds) {

                        }
                    });
                    if (MintegralSingleTon.getInstance().containsKeyBidAdUnit(adUnitId)) {
                        String token = MintegralSingleTon.getInstance().removeBidAdUnit(adUnitId);
                        splashHandler.show(container, token);
                    } else {
                        splashHandler.show(container);
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, "MintegralAdapter", e.getMessage()));
                    }
                }
            }
        });
    }

    private class InnerSplashAdListener implements MBSplashLoadListener {

        private String mAdUnitId;
        private SplashAdCallback mAdCallback;
        private MBSplashHandler mSplashAD;

        private InnerSplashAdListener(String adUnitId, SplashAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        public void setSplashAD(MBSplashHandler splashAD) {
            this.mSplashAD = splashAD;
        }

        @Override
        public void onLoadSuccessed(MBridgeIds mBridgeIds, int reqType) {
            AdLog.getSingleton().LogD("MintegralSplash ad onLoadSuccessed: " + mAdUnitId);
            mSplashAds.put(mAdUnitId, mSplashAD);
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadSuccess(null);
            }
        }

        @Override
        public void onLoadFailed(MBridgeIds mBridgeIds, String msg, int reqType) {
            if (mAdCallback != null) {
                mAdCallback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "MintegralAdapter", msg));
            }
        }

        @Override
        public void isSupportZoomOut(MBridgeIds mBridgeIds, boolean b) {

        }
    }

}
