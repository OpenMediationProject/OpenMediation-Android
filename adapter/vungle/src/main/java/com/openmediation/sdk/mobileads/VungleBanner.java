package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.vungle.warren.AdConfig;
import com.vungle.warren.Banners;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleBanner extends CustomBannerEvent implements LoadAdCallback, PlayAdCallback {

    private static final String CONSENT_MESSAGE_VERSION = "1.0.0";
    private com.vungle.warren.VungleBanner mBanner;
    private AdConfig.AdSize mAdSize;

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
        mAdSize = getAdSize(activity, config);
        checkInitAndLoad(activity, config);
    }

    @Override
    public void destroy(Activity activity) {
        if (mBanner != null) {
            mBanner.destroyAd();
        }
    }

    @Override
    public void onAdLoad(String id) {
        if (isDestroyed) {
            return;
        }
        if (mBanner != null) {
            mBanner.destroyAd();
        }
        mBanner = Banners.getBanner(id, mAdSize, this);
        if (mBanner != null) {
            mBanner.disableLifeCycleManagement(true);
            onInsReady(mBanner);
            mBanner.renderAd();
        } else {
            onInsError(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Load Vungle banner error"));
        }
    }

    @Override
    public void onAdStart(String id) {
    }

    @Override
    public void onAdEnd(String id, boolean completed, boolean isCTAClicked) {

    }

    @Override
    public void onAdEnd(String id) {

    }

    @Override
    public void onAdClick(String id) {
        if (isDestroyed) {
            return;
        }
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
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, exception.getExceptionCode(), exception.getMessage()));
    }

    @Override
    public void onAdViewed(String id) {
    }

    private void loadBanner() {
        if (mBanner != null) {
            mBanner.finishAd();
        }
        Banners.loadBanner(mInstancesKey, mAdSize, this);
    }

    private void checkInitAndLoad(final Activity activity, Map<String, String> config) {
        String appKey = config.get("AppKey");
        if (TextUtils.isEmpty(appKey)) {
            onInsError(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Load Vungle error When init Vungle SDK with empty appKey"));
            return;
        }
        VungleSingleTon.getInstance().init(activity, appKey, new InitCallback() {
            @Override
            public void onSuccess() {
                if (mUserConsent != null) {
                    setGDPRConsent(activity, mUserConsent);
                }
                if (mUSPrivacyLimit != null) {
                    setUSPrivacyLimit(activity, mUSPrivacyLimit);
                }
                loadBanner();
            }

            @Override
            public void onError(VungleException exception) {
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, exception.getExceptionCode(), exception.getLocalizedMessage()));
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {
            }
        });
    }

    private AdConfig.AdSize getAdSize(Context context, Map<String, String> config) {
        String bannerDesc = getBannerDesc(config);
        if (TextUtils.isEmpty(bannerDesc)) {
            return AdConfig.AdSize.BANNER;
        }
        switch (bannerDesc) {
            case DESC_BANNER:
                return AdConfig.AdSize.BANNER;
            case DESC_LEADERBOARD:
                return AdConfig.AdSize.BANNER_LEADERBOARD;
            case DESC_SMART:
                if (isLargeScreen(context)) {
                    return AdConfig.AdSize.BANNER_LEADERBOARD;
                } else {
                    return AdConfig.AdSize.BANNER;
                }
            default:
                return null;
        }
    }
}
