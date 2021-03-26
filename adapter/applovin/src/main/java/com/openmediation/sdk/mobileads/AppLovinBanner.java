// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.applovin.adview.AppLovinAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

public class AppLovinBanner extends CustomBannerEvent implements AppLovinAdLoadListener, AppLovinAdClickListener, AppLovinAdDisplayListener {
    private AppLovinAdView mAppLovinAdView;
    private static final String TAG = "OM-AppLovin";

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (context != null) {
            AppLovinPrivacySettings.setHasUserConsent(consent, context);
        }
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        setAgeRestricted(context, age < AppLovinAdapter.AGE_RESTRICTION);
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        if (context != null) {
            AppLovinPrivacySettings.setIsAgeRestrictedUser(restricted, context);
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        if (context != null) {
            AppLovinPrivacySettings.setDoNotSell(value, context);
        }
    }

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }
        try {
            AppLovinSingleTon.getInstance().init(activity, config.get("AppKey"), new AppLovinSingleTon.InitCallback() {
                @Override
                public void onSuccess() {
                    loadBanner(activity, config);
                }

                @Override
                public void onFailed(String msg) {
                    onInsError(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, msg));
                }
            });
        } catch (Exception e) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, e.getMessage()));
        }
    }

    private void loadBanner(Activity activity, Map<String, String> config) {
        if (mAppLovinAdView != null) {
            mAppLovinAdView.loadNextAd();
            return;
        }
        AppLovinAdSize adSize = getAdSize(activity, config);
        if (adSize == null) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "unsupported banner size"));
            return;
        }
        AppLovinSdk sdk = AppLovinSingleTon.getInstance().getAppLovinSdk();
        mAppLovinAdView = new AppLovinAdView(sdk, adSize, mInstancesKey, activity);
        mAppLovinAdView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mAppLovinAdView.setAdLoadListener(this);
        mAppLovinAdView.setAdClickListener(this);
        mAppLovinAdView.setAdDisplayListener(this);
        mAppLovinAdView.loadNextAd();
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_8;
    }

    @Override
    public void destroy(Activity activity) {
        if (mAppLovinAdView != null) {
            mAppLovinAdView.destroy();
            mAppLovinAdView = null;
        }
        isDestroyed = true;
    }

    @Override
    public void adClicked(AppLovinAd ad) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG, "AppLovin Banner ad clicked ");
        onInsClicked();
    }

    @Override
    public void adReceived(AppLovinAd ad) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG, "AppLovin Banner ad load success ");
        onInsReady(mAppLovinAdView);
    }

    @Override
    public void failedToReceiveAd(int errorCode) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, errorCode, AppLovinAdapter.getErrorString(errorCode)));
    }

    @Override
    public void adDisplayed(AppLovinAd ad) {
    }

    @Override
    public void adHidden(AppLovinAd ad) {
    }

    private AppLovinAdSize getAdSize(Context context, Map<String, String> config) {
        String bannerDesc = getBannerDesc(config);
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return AppLovinAdSize.LEADER;
            case DESC_RECTANGLE:
                return null;
            case DESC_SMART:
                if (isLargeScreen(context)) {
                    return AppLovinAdSize.LEADER;
                } else {
                    return AppLovinAdSize.BANNER;
                }
            default:
                return AppLovinAdSize.BANNER;
        }
    }
}
