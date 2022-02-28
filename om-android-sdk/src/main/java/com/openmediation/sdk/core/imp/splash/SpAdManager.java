package com.openmediation.sdk.core.imp.splash;

import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.splash.SplashAdListener;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.model.Placement;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SpAdManager {

    private ConcurrentMap<String, SpManager> mSplashAds;

    private ConcurrentMap<String, SplashAdListener> mSplashListeners = new ConcurrentHashMap<>();

    public void setSize(String placementId, int width, int height) {
        SpManager splashManager = getSplashAd(placementId);
        if (splashManager == null) {
            return;
        }
        splashManager.setSize(width, height);
    }

    private static final class SplashHolder {
        private static final SpAdManager INSTANCE = new SpAdManager();
    }

    private SpAdManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static SpAdManager getInstance() {
        return SplashHolder.INSTANCE;
    }

    public void initSplashAd(String placementId) {
        if (mSplashAds != null && !mSplashAds.containsKey(placementId)) {
            SpManager splashManager = new SpManager(placementId);
            mSplashAds.put(placementId, splashManager);
        }
    }

    public void setLoadTimeout(String placementId, long timeout) {
        SpManager splashManager = getSplashAd(placementId);
        if (splashManager == null) {
            return;
        }
        splashManager.setLoadTimeout(timeout);
    }

    public void load(String placementId, ViewGroup container) {
        AdsUtil.callActionReport(placementId, 0, EventId.CALLED_LOAD);
        SpManager splashManager = getSplashAd(placementId);
        if (splashManager == null) {
            if (placementId != null && mSplashListeners.containsKey(placementId)) {
                Error error = new Error(ErrorCode.CODE_LOAD_SDK_UNINITIALIZED,
                        "SplashAd Load Failed: Placement Not Found", -1);
                mSplashListeners.get(placementId).onSplashAdFailed(placementId, error);
            }
            return;
        }
        splashManager.loadAds(OmManager.LOAD_TYPE.MANUAL, container);
    }

    public boolean isReady(String placementId) {
        SpManager splashManager = getSplashAd(placementId);
        if (splashManager == null) {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.SPLASH);
            return false;
        }
        boolean result = splashManager.isReady();
        if (result) {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_TRUE, placementId, null, CommonConstants.SPLASH);
        } else {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.SPLASH);
        }
        return result;
    }

    public void setSplashAdListener(String placementId, SplashAdListener listener) {
        SpManager splashManager = getSplashAd(placementId);
        if (splashManager == null) {
            if (placementId != null) {
                if (listener == null) {
                    mSplashListeners.remove(placementId);
                } else {
                    mSplashListeners.put(placementId, listener);
                }
            }
            return;
        }
        splashManager.setAdListener(listener);
    }

    public void show(String placementId, ViewGroup container) {
        AdsUtil.callActionReport(EventId.CALLED_SHOW, placementId, null, CommonConstants.SPLASH);
        SpManager splashManager = getSplashAd(placementId);
        if (splashManager == null) {
            if (placementId != null && mSplashListeners.containsKey(placementId)) {
                Error error = new Error(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                        "SplashAd Show Failed: Placement Not Found", -1);
                mSplashListeners.get(placementId).onSplashAdShowFailed(placementId, error);
            }
            return;
        }
        splashManager.showSplashAd(container);
    }

    public void show(String placementId) {
        AdsUtil.callActionReport(EventId.CALLED_SHOW, placementId, null, CommonConstants.SPLASH);
        SpManager splashManager = getSplashAd(placementId);
        if (splashManager == null) {
            if (placementId != null && mSplashListeners.containsKey(placementId)) {
                Error error = new Error(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                        "SplashAd Show Failed: Placement Not Found", -1);
                mSplashListeners.get(placementId).onSplashAdShowFailed(placementId, error);
            }
            return;
        }
        splashManager.showSplashAd();
    }

    private SpManager getSplashAd(String placementId) {
        if (mSplashAds == null) {
            return null;
        }
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = PlacementUtils.getPlacement(CommonConstants.SPLASH);
            if (placement == null) {
                return null;
            }
            placementId = placement.getId();
        }
        return mSplashAds.get(placementId);
    }
}
