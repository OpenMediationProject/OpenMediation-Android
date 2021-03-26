package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleSplash extends CustomSplashEvent {

    private static final String CONSENT_MESSAGE_VERSION = "1.0.0";

    @Override
    public boolean isReady() {
        if (isDestroyed || TextUtils.isEmpty(mInstancesKey)) {
            return false;
        }
        return Vungle.canPlayAd(mInstancesKey);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_5;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        Vungle.updateConsentStatus(consent ? Vungle.Consent.OPTED_IN : Vungle.Consent.OPTED_OUT, CONSENT_MESSAGE_VERSION);
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        Vungle.updateCCPAStatus(value ? Vungle.Consent.OPTED_OUT : Vungle.Consent.OPTED_IN);
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (Vungle.isInitialized()) {
            if (Vungle.canPlayAd(mInstancesKey)) {
                onInsReady(null);
            } else {
                loadSplash();
            }
        } else {
            String appKey = config.get("AppKey");
            VungleSingleTon.getInstance().init(activity.getApplicationContext(), appKey, new InitCallback() {
                @Override
                public void onSuccess() {
                    loadSplash();
                }

                @Override
                public void onError(VungleException exception) {
                    onInsError(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, exception.getExceptionCode(), exception.getMessage()));
                }

                @Override
                public void onAutoCacheAdAvailable(String placementId) {

                }
            });
        }
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        super.show(activity, container);
        showSplash(activity, container);
    }

    @Override
    public void show(Activity activity) {
        super.show(activity);
        showSplash(activity, null);
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
    }

    private void loadSplash(){
        Vungle.loadAd(mInstancesKey, new LoadAdCallback() {
            @Override
            public void onAdLoad(String id) {
                if (isDestroyed) {
                    return;
                }
                onInsReady(null);
            }

            @Override
            public void onError(String id, VungleException exception) {
                if (isDestroyed) {
                    return;
                }
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, exception.getExceptionCode(), exception.getMessage()));
            }
        });
    }

    private void showSplash(Activity activity, ViewGroup container) {
        if (!check(activity)) {
            onInsError(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Activity is null"));
            return;
        }
        if (!isReady()) {
            onInsShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "SplashAd not ready"));
            return;
        }

        Vungle.playAd(mInstancesKey, null, new PlayAdCallback() {
            @Override
            public void onAdStart(String id) {
                onInsShowSuccess();
            }

            @Override
            public void onAdEnd(String id, boolean completed, boolean isCTAClicked) {

            }

            @Override
            public void onAdEnd(String id) {
                onInsDismissed();
            }

            @Override
            public void onAdClick(String id) {
                onInsClicked();
            }

            @Override
            public void onAdRewarded(String id) {

            }

            @Override
            public void onAdLeftApplication(String id) {

            }

            @Override
            public void onError(String id, VungleException exception) {
                onInsError(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, exception.getExceptionCode(), exception.getMessage()));
            }

            @Override
            public void onAdViewed(String id) {

            }
        });
    }
}
