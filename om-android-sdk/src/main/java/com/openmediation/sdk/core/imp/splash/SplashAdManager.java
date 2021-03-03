package com.openmediation.sdk.core.imp.splash;

import android.app.Activity;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.splash.SplashAdListener;
import com.openmediation.sdk.utils.ActLifecycle;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.model.Placement;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SplashAdManager {

    private ConcurrentMap<String, SplashAdImp> mSplashAds;

    private ConcurrentMap<String, SplashAdListener> mSplashListeners = new ConcurrentHashMap<>();

    public void setSize(String placementId, int width, int height) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            return;
        }
        splashAdImp.setSize(width, height);
    }

    private static final class SplashHolder {
        private static final SplashAdManager INSTANCE = new SplashAdManager();
    }

    private SplashAdManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static SplashAdManager getInstance() {
        return SplashHolder.INSTANCE;
    }

    public void initSplashAd(String placementId) {
        if (mSplashAds != null && !mSplashAds.containsKey(placementId)) {
            Activity activity = ActLifecycle.getInstance().getActivity();
            if (activity == null) {
                return;
            }
            SplashAdImp adImp = new SplashAdImp(activity, placementId);
            mSplashAds.put(placementId, adImp);
        }
    }

    public void setLoadTimeout(String placementId, long timeout) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            return;
        }
        splashAdImp.setLoadTimeout(timeout);
    }

    public void load(String placementId) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            if (placementId != null && mSplashListeners.containsKey(placementId)) {
                mSplashListeners.get(placementId).onSplashAdFailed(placementId, "Placement Not Found");
            }
            return;
        }
        splashAdImp.loadAd(OmManager.LOAD_TYPE.MANUAL);
    }

    public boolean isReady(String placementId) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.SPLASH);
            return false;
        }
        boolean result = splashAdImp.isReady();
        if (result) {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_TRUE, placementId, null, CommonConstants.SPLASH);
        } else {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.SPLASH);
        }
        return result;
    }

    public void setSplashAdListener(String placementId, SplashAdListener listener) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            if (placementId != null) {
                if (listener == null) {
                    mSplashListeners.remove(placementId);
                } else {
                    mSplashListeners.put(placementId, listener);
                }
            }
            return;
        }
        splashAdImp.setAdListener(listener);
    }

    public void show(Activity activity, String placementId, ViewGroup container) {
        AdsUtil.callActionReport(EventId.CALLED_SHOW, placementId, null, CommonConstants.SPLASH);
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            if (placementId != null && mSplashListeners.containsKey(placementId)) {
                mSplashListeners.get(placementId).onSplashAdShowFailed(placementId, "SplashAd Show Failed: Placement Not Found");
            }
            return;
        }
        splashAdImp.show(activity, container);
    }

    public void show(Activity activity, String placementId) {
        AdsUtil.callActionReport(EventId.CALLED_SHOW, placementId, null, CommonConstants.SPLASH);
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            if (placementId != null && mSplashListeners.containsKey(placementId)) {
                mSplashListeners.get(placementId).onSplashAdShowFailed(placementId, "SplashAd Show Failed: Placement Not Found");
            }
            return;
        }
        splashAdImp.show(activity);
    }

    private SplashAdImp getSplashAd(String placementId) {
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
