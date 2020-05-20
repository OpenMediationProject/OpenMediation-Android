// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.applovin.adview.AppLovinAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.applovin.sdk.AppLovinSdkUtils;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;

public class AppLovinBanner extends CustomBannerEvent implements AppLovinAdLoadListener, AppLovinAdClickListener, AppLovinAdDisplayListener {
    private boolean mDidInited = false;
    private AppLovinAdView mAppLovinAdView;
    private AppLovinSdk mAppLovinSdk;
    private static final String TAG = "OM-AppLovin";

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }
        if (!mDidInited) {
            AppLovinSdkSettings settings = new AppLovinSdkSettings(activity.getApplicationContext());
            mAppLovinSdk = AppLovinSdk.getInstance(config.get("AppKey"), settings,
                    activity.getApplicationContext());
            mAppLovinSdk.initializeSdk();
            mDidInited = true;
        }
        if (mAppLovinAdView != null) {
            mAppLovinAdView.loadNextAd();
            return;
        }

        AppLovinAdSize adSize = getAdSize(config);
        mAppLovinAdView = new AppLovinAdView(mAppLovinSdk, adSize, mInstancesKey, activity);
        int heightPx = AppLovinSdkUtils.dpToPx(activity.getApplication(), adSize.getHeight());
        mAppLovinAdView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, heightPx));
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
        AdLog.getSingleton().LogD(TAG, "AppLovin Banner ad load failedToReceiveAd: " + errorCode);
        onInsError("Banner ad load failed");
    }

    @Override
    public void adDisplayed(AppLovinAd ad) {
        AdLog.getSingleton().LogD(TAG, "AppLovin Banner adDisplayed ");
    }

    @Override
    public void adHidden(AppLovinAd ad) {
        AdLog.getSingleton().LogD(TAG, "AppLovin Banner adHidden");
    }

    private AppLovinAdSize getAdSize(Map<String, String> config) {
        int[] size = getBannerSize(config);
        int height = size[1];
        if (height == AppLovinAdSize.LEADER.getHeight()) {
            return AppLovinAdSize.LEADER;
        } else if (height == AppLovinAdSize.MREC.getHeight()) {
            return AppLovinAdSize.MREC;
        }
        return AppLovinAdSize.BANNER;
    }
}
