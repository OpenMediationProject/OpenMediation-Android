// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnityBannerManager {
    private static final String TAG = "UnityBanner: ";

    private ConcurrentHashMap<String, BannerView> mBannerViews;

    private static class Holder {
        private static final UnityBannerManager INSTANCE = new UnityBannerManager();
    }

    private UnityBannerManager() {
        mBannerViews = new ConcurrentHashMap<>();
    }

    public static UnityBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final BannerAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        UnitySingleTon.getInstance().init(context, appKey, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "UnityAdapter", message));
                }
            }
        });
    }

    public void loadAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        AdLog.getSingleton().LogD(TAG + "Unity Banner Ad Start Load : " + adUnitId);
        UnityBannerSize bannerSize = getAdSize(activity, extras);
        BannerView bannerView = new BannerView(activity, adUnitId, bannerSize);
        InnerBannerAdListener listener = new InnerBannerAdListener(adUnitId, callback);
        bannerView.setListener(listener);
        bannerView.load();
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId)) {
            BannerView bannerView = mBannerViews.remove(adUnitId);
            if (bannerView != null) {
                bannerView.destroy();
                bannerView = null;
            }
        }
    }

    private class InnerBannerAdListener implements BannerView.IListener {
        private String mAdUnitId;
        private BannerAdCallback mAdCallback;

        private InnerBannerAdListener(String adUnitId, BannerAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onBannerLoaded(BannerView bannerAdView) {
            destroyAd(mAdUnitId);
            mBannerViews.put(mAdUnitId, bannerAdView);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadSuccess(bannerAdView);
            }
        }

        @Override
        public void onBannerClick(BannerView bannerAdView) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "UnityAdapter",
                        errorInfo.errorCode.name() + " : " + errorInfo.errorMessage));
            }
        }

        @Override
        public void onBannerLeftApplication(BannerView bannerView) {

        }
    }

    private UnityBannerSize getAdSize(Context context, Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_BANNER:
                return new UnityBannerSize(320, 50);
            case MediationUtil.DESC_LEADERBOARD:
                return new UnityBannerSize(728, 90);
            case MediationUtil.DESC_RECTANGLE:
                return new UnityBannerSize(300, 250);
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(context)) {
                    return new UnityBannerSize(728, 90);
                } else {
                    return new UnityBannerSize(320, 50);
                }
            default:
                return UnityBannerSize.getDynamicSize(context);
        }
    }

}
