// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.utils.AdLog;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TencentBannerManager {

    private final ConcurrentHashMap<String, UnifiedBannerView> mBannerViews;

    private static class BannerHolder {
        private static final TencentBannerManager INSTANCE = new TencentBannerManager();
    }

    private TencentBannerManager() {
        mBannerViews = new ConcurrentHashMap<>();
    }

    public static TencentBannerManager getInstance() {
        return BannerHolder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final BannerAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        boolean result = TencentAdManagerHolder.init(context.getApplicationContext(), appKey);
        if (result) {
            if (callback != null) {
                callback.onBannerAdInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "TencentAdAdapter", "Init Failed"));
            }
        }
    }

    public void loadAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        try {
            InnerBannerAdListener listener = new InnerBannerAdListener(adUnitId, callback);
            UnifiedBannerView mBannerView = new UnifiedBannerView(activity, adUnitId, listener);
            // Disable Auto Refresh
            mBannerView.setRefresh(0);
            listener.setBannerView(mBannerView);
            mBannerView.loadAD();
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "TencentAdAdapter","Unknown Error, " + e.getMessage()));
            }
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        try {
            if (!TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId)) {
                UnifiedBannerView bannerView = mBannerViews.remove(adUnitId);
                if (bannerView != null) {
                    bannerView.destroy();
                    bannerView = null;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private class InnerBannerAdListener implements UnifiedBannerADListener {

        private String mAdUnitId;
        private BannerAdCallback mAdCallback;
        private UnifiedBannerView mBannerView;

        private InnerBannerAdListener(String adUnitId, BannerAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        public void setBannerView(UnifiedBannerView bannerView) {
            this.mBannerView = bannerView;
        }

        @Override
        public void onNoAD(AdError adError) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "TencentAdAdapter", adError.getErrorCode(), adError.getErrorMsg()));
            }
        }

        @Override
        public void onADReceive() {
            if (mBannerView != null) {
                mBannerViews.put(mAdUnitId, mBannerView);
                if (mAdCallback != null) {
                    mAdCallback.onBannerAdLoadSuccess(mBannerView);
                }
            }
        }

        @Override
        public void onADExposure() {
            AdLog.getSingleton().LogD("TencentAdAdapter Banner onADExposure");
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }

        @Override
        public void onADClosed() {

        }

        @Override
        public void onADClicked() {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onADLeftApplication() {

        }

        @Override
        public void onADOpenOverlay() {

        }

        @Override
        public void onADCloseOverlay() {

        }

    }

}
