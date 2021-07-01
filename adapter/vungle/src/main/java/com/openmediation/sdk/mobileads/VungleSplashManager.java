package com.openmediation.sdk.mobileads;

import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.SplashAdCallback;
import com.openmediation.sdk.utils.AdLog;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleSplashManager {

    private static class Holder {
        private static final VungleSplashManager INSTANCE = new VungleSplashManager();
    }

    private VungleSplashManager() {
    }

    public static VungleSplashManager getInstance() {
        return Holder.INSTANCE;
    }

    public void loadAd(String adUnitId, final Map<String, Object> config, final SplashAdCallback callback) {
        Vungle.loadAd(adUnitId, new LoadAdCallback() {
            @Override
            public void onAdLoad(String id) {
                if (callback != null) {
                    callback.onSplashAdLoadSuccess(null);
                }
            }

            @Override
            public void onError(String id, VungleException exception) {
                if (callback != null) {
                    callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "VungleAdapter", exception.getExceptionCode(), exception.getLocalizedMessage()));
                }
            }
        });
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && Vungle.canPlayAd(adUnitId);
    }

    public void showAd(final String adUnitId, final SplashAdCallback callback) {
        if (!isAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "VungleAdapter", "SplashAd not ready"));
            }
            return;
        }

        Vungle.playAd(adUnitId, null, new PlayAdCallback() {
            @Override
            public void onAdStart(String id) {
                AdLog.getSingleton().LogD("onAdStart");
                if (callback != null) {
                    callback.onSplashAdShowSuccess();
                }
            }

            @Override
            public void onAdEnd(String id, boolean completed, boolean isCTAClicked) {

            }

            @Override
            public void onAdEnd(String id) {
                AdLog.getSingleton().LogD("VungleSplash onAdEnd");
                if (callback != null) {
                    callback.onSplashAdDismissed();
                }
            }

            @Override
            public void onAdClick(String id) {
                AdLog.getSingleton().LogD("VungleSplash onAdClick");
                if (callback != null) {
                    callback.onSplashAdAdClicked();
                }
            }

            @Override
            public void onAdRewarded(String id) {

            }

            @Override
            public void onAdLeftApplication(String id) {

            }

            @Override
            public void onError(String id, VungleException exception) {
                AdLog.getSingleton().LogE("VungleSplash onError" + exception.getMessage());
                if (callback != null) {
                    callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "VungleAdapter", exception.getExceptionCode(), exception.getLocalizedMessage()));
                }
            }

            @Override
            public void onAdViewed(String id) {
                AdLog.getSingleton().LogD("VungleSplash onAdViewed");
            }
        });
    }

    public void destroyAd(String adUnitId) {
    }

}
