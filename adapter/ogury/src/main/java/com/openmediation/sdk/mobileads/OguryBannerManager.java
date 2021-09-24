// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.ogury.core.OguryError;
import com.ogury.ed.OguryBannerAdListener;
import com.ogury.ed.OguryBannerAdSize;
import com.ogury.ed.OguryBannerAdView;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OguryBannerManager {
    private static final String TAG = "OguryBanner: ";

    private final ConcurrentHashMap<String, OguryBannerAdView> mBannerAds;

    private static class Holder {
        private static final OguryBannerManager INSTANCE = new OguryBannerManager();
    }

    private OguryBannerManager() {
        mBannerAds = new ConcurrentHashMap<>();
    }

    public static OguryBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void loadAd(Context context, String adUnitId, Map<String, Object> extras, final BannerAdCallback callback) {
        try {
            OguryBannerAdSize size = getAdSize(extras);
            if (size == null) {
                if (callback != null) {
                    callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "OguryAdapter", "Unsupported Banner Size: " + MediationUtil.getBannerDesc(extras)));
                }
                return;
            }
            OguryBannerAdView bannerAdView = new OguryBannerAdView(context);
            bannerAdView.setAdUnit(adUnitId);
            bannerAdView.setAdSize(OguryBannerAdSize.SMALL_BANNER_320x50);
            InnerBannerAdListener listener = new InnerBannerAdListener(bannerAdView, adUnitId, callback);
            bannerAdView.setListener(listener);
            bannerAdView.loadAd();
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "OguryAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        OguryBannerAdView bannerAdView = mBannerAds.remove(adUnitId);
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
    }

    private class InnerBannerAdListener implements OguryBannerAdListener {
        private String mAdUnitId;
        private BannerAdCallback mAdCallback;
        private OguryBannerAdView mAdView;

        private InnerBannerAdListener(OguryBannerAdView adView, String adUnitId, BannerAdCallback callback) {
            this.mAdView = adView;
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onAdLoaded() {
            AdLog.getSingleton().LogE(TAG + "Banner Load Success");
            mBannerAds.put(mAdUnitId, mAdView);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadSuccess(mAdView);
            }
        }

        @Override
        public void onAdDisplayed() {
            AdLog.getSingleton().LogE(TAG + "BannerAd onAdDisplayed");
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }

        @Override
        public void onAdClicked() {
            AdLog.getSingleton().LogE(TAG + "BannerAd onAdClicked");
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onAdClosed() {

        }

        @Override
        public void onAdError(OguryError oguryError) {
            AdLog.getSingleton().LogE(TAG + "Banner onAdError: " + oguryError);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "OguryAdapter", oguryError.getErrorCode(), oguryError.getMessage()));
            }
        }
    }

    private OguryBannerAdSize getAdSize(Map<String, Object> config) {
        String desc = MediationUtil.getBannerDesc(config);
        if (MediationUtil.DESC_BANNER.equals(desc)) {
            return OguryBannerAdSize.SMALL_BANNER_320x50;
        }
        if (MediationUtil.DESC_RECTANGLE.equals(desc)) {
            return OguryBannerAdSize.MPU_300x250;
        }
        if (MediationUtil.DESC_LEADERBOARD.equals(desc)) {
            return null;
        }
        return OguryBannerAdSize.SMALL_BANNER_320x50;
    }

}
