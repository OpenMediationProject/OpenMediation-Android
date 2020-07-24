// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.util.Map;

public class MoPubBanner extends CustomBannerEvent implements MoPubView.BannerAdListener {

    private MoPubView adView;

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (!MoPub.isSdkInitialized()) {
            return;
        }
        PersonalInfoManager manager = MoPub.getPersonalInformationManager();
        if (manager == null) {
            return;
        }
        if (consent) {
            manager.grantConsent();
        } else {
            manager.revokeConsent();
        }
    }

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!MoPub.isSdkInitialized()) {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(mInstancesKey).build();
            MoPub.initializeSdk(activity, sdkConfiguration, new SdkInitializationListener() {
                @Override
                public void onInitializationFinished() {
                    if (mUserConsent != null) {
                        setGDPRConsent(activity, mUserConsent);
                    }
                    loadBannerAd(activity, config);
                }
            });
        } else {
            loadBannerAd(activity, config);
        }
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_9;
    }

    @Override
    public void destroy(Activity activity) {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        if (isDestroyed) {
            return;
        }
        onInsReady(banner);
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, errorCode.name()));
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {

    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {

    }

    private void loadBannerAd(Activity activity, Map<String, String> config) {
        if (adView == null) {
            adView = new MoPubView(activity);
            MoPubView.MoPubAdSize adSize = getAdSize(config, adView.getAdSize());
            adView.setAdSize(adSize);
            adView.setAdUnitId(mInstancesKey);
            adView.setBannerAdListener(this);
        }

        adView.loadAd();
    }

    private MoPubView.MoPubAdSize getAdSize(Map<String, String> config, MoPubView.MoPubAdSize defaultSize) {
        String bannerDesc = getBannerDesc(config);
        switch (bannerDesc) {
            case DESC_BANNER:
                return MoPubView.MoPubAdSize.HEIGHT_50;
            case DESC_LEADERBOARD:
                return MoPubView.MoPubAdSize.HEIGHT_90;
            case DESC_RECTANGLE:
                return MoPubView.MoPubAdSize.HEIGHT_250;
            default:
                return defaultSize;
        }
    }
}
