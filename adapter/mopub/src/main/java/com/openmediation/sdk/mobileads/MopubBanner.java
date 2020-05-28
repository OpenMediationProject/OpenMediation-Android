// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import java.util.Map;

public class MoPubBanner extends CustomBannerEvent implements MoPubView.BannerAdListener {

    private MoPubView adView;

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
        AdLog.getSingleton().LogD("Om-Mopub", "Mopub Banner ad load success ");
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        if (isDestroyed) {
            return;
        }
        onInsError("load mopub banner error: " + errorCode.toString());
        AdLog.getSingleton().LogE("Om-Mopub: Mopub Banner ad load failed " + errorCode.name());
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
        int[] size = getBannerSize(config);
        int height = size[1];
        if (height == MoPubView.MoPubAdSize.HEIGHT_90_INT) {
            return MoPubView.MoPubAdSize.HEIGHT_90;
        } else if (height == MoPubView.MoPubAdSize.HEIGHT_250_INT) {
            return MoPubView.MoPubAdSize.HEIGHT_250;
        }
        return defaultSize;
    }
}
