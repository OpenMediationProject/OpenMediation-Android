// Copyright 2022 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.annotation.GlobalApi;
import com.huawei.hms.ads.banner.BannerView;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HwAdsBannerManager {

    private final ConcurrentHashMap<String, BannerView> mBannerViews;

    private static class BannerHolder {
        private static final HwAdsBannerManager INSTANCE = new HwAdsBannerManager();
    }

    private HwAdsBannerManager() {
        mBannerViews = new ConcurrentHashMap<>();
    }

    public static HwAdsBannerManager getInstance() {
        return BannerHolder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final BannerAdCallback callback) {
        HwAdsSingleTon.getInstance().initSDK(context, new HwAdsSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "HwAdsAdapter", msg));
                }
            }
        });
    }

    public void loadAd(final Context context, final String adUnitId, final Map<String, Object> extras, final BannerAdCallback callback) {
        MediationUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    BannerView bannerView = new BannerView(context.getApplicationContext());
                    bannerView.setAdId(adUnitId);
                    BannerAdSize adSize = getAdSize(context, extras);
                    bannerView.setBannerAdSize(adSize);
                    bannerView.setBannerRefresh(0);
                    InnerBannerAdListener listener = new InnerBannerAdListener(bannerView, adUnitId, callback);
                    bannerView.setAdListener(listener);
                    bannerView.loadAd(new AdParam.Builder().build());
                } catch (Throwable e) {
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "HwAdsAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        });
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId)) {
            BannerView bannerView = mBannerViews.remove(adUnitId);
            if (bannerView != null) {
                bannerView.destroy();
            }
        }
    }

    private class InnerBannerAdListener extends AdListener {

        private final BannerView mBannerView;
        private final String mAdUnitId;
        private final BannerAdCallback mAdCallback;

        private InnerBannerAdListener(BannerView bannerView, String adUnitId, BannerAdCallback callback) {
            this.mBannerView = bannerView;
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onAdFailed(int code) {
            AdLog.getSingleton().LogE("HwAdsBannerManager onAdFailed: " + code + ", adUnit: " + mAdUnitId);
            mBannerViews.remove(mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "HwAdsAdapter", code, ""));
            }
        }

        @GlobalApi
        public void onAdLoaded() {
            AdLog.getSingleton().LogD("HwAdsBannerManager onAdLoaded: " + mAdUnitId);
            if (mBannerView != null) {
                mBannerViews.put(mAdUnitId, mBannerView);
                if (mAdCallback != null) {
                    mAdCallback.onBannerAdLoadSuccess(mBannerView);
                }
            }
        }

        @GlobalApi
        public void onAdClicked() {
            AdLog.getSingleton().LogD("HwAdsBannerManager onAdClicked: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @GlobalApi
        public void onAdImpression() {
            AdLog.getSingleton().LogD("HwAdsBannerManager onAdImpression: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }
    }

    private BannerAdSize getAdSize(Context context, Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_LEADERBOARD:
                return BannerAdSize.BANNER_SIZE_728_90;
            case MediationUtil.DESC_RECTANGLE:
                return BannerAdSize.BANNER_SIZE_300_250;
            case MediationUtil.DESC_SMART:
                return BannerAdSize.BANNER_SIZE_SMART;
            default:
                return BannerAdSize.BANNER_SIZE_320_50;
        }
    }

}