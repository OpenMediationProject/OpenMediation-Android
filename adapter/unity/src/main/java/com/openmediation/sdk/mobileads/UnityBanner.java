// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mobileads.unity.BuildConfig;
import com.openmediation.sdk.utils.AdLog;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.Map;


public class UnityBanner extends CustomBannerEvent implements BannerView.IListener {
    private static final String TAG = "OM-Unity: ";
    private boolean mDidInit = false;

    private BannerView mBannerView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Unity Banner Ad Start Load : " + mInstancesKey);
        if (!mDidInit) {
            initSDK(activity, config.get("AppKey"));
            mDidInit = true;
        }

        BannerView bannerView = new BannerView(activity, mInstancesKey, getAdSize(config));
        bannerView.setListener(this);
        bannerView.load();
    }

    private synchronized void initSDK(Activity activity, String appKey) {
        AdLog.getSingleton().LogD(TAG, "initSDK, appkey:" + appKey);
        MediationMetaData mediationMetaData = new MediationMetaData(activity);
        mediationMetaData.setName("AdTiming");
        mediationMetaData.setVersion(BuildConfig.VERSION_NAME);
        mediationMetaData.commit();
        UnityAds.initialize(activity, appKey);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_4;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onBannerLoaded(BannerView bannerAdView) {
        if (isDestroyed) {
            return;
        }
        if (mBannerView != null) {
            mBannerView.destroy();
        }
        mBannerView = bannerAdView;
        AdLog.getSingleton().LogD(TAG + "Unity Banner Ad Load Success : " + mInstancesKey);
        onInsReady(bannerAdView);
    }

    @Override
    public void onBannerClick(BannerView bannerAdView) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Unity Banner Ad Click : " + mInstancesKey);
        onInsClicked();
    }

    @Override
    public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Unity Banner Ad Load Failed " + errorInfo.errorMessage + "  " + mInstancesKey);
        onInsError(errorInfo.errorMessage);
    }

    @Override
    public void onBannerLeftApplication(BannerView bannerView) {

    }

    private UnityBannerSize getAdSize(Map<String, String> config) {
        int[] size = getBannerSize(config);
        int width = size[0];
        int height = size[1];
        return new UnityBannerSize(width, height);
    }

}
