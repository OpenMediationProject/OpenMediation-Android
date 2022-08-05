package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;
import com.vungle.warren.AdConfig;
import com.vungle.warren.BannerAdConfig;
import com.vungle.warren.Banners;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.VungleBanner;
import com.vungle.warren.error.VungleException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VungleBannerManager {

    private final ConcurrentHashMap<String, com.vungle.warren.VungleBanner> mBannerAds;

    private static class Holder {
        private static final VungleBannerManager INSTANCE = new VungleBannerManager();
    }

    private VungleBannerManager() {
        mBannerAds = new ConcurrentHashMap<>();
    }

    public static VungleBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void loadAd(Context context, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        AdConfig.AdSize adSize = getAdSize(context, extras);
        com.vungle.warren.VungleBanner banner = mBannerAds.remove(adUnitId);
        if (banner != null) {
            // Playing or Loading operation ongoing. Playing true Loading: false
            banner.finishAd();
        }
        InnerBannerAdListener listener = new InnerBannerAdListener(adSize, adUnitId, callback);
        Banners.loadBanner(adUnitId, new BannerAdConfig(adSize), listener);
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId)) {
            com.vungle.warren.VungleBanner banner = mBannerAds.remove(adUnitId);
            banner.destroyAd();
        }
    }

    private class InnerBannerAdListener implements LoadAdCallback {
        private AdConfig.AdSize mAdSize;
        private String mAdUnitId;
        private BannerAdCallback mAdCallback;

        private InnerBannerAdListener(AdConfig.AdSize adSize, String adUnitId, BannerAdCallback callback) {
            this.mAdSize = adSize;
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onAdLoad(String id) {
            VungleBanner bannerRemove = mBannerAds.remove(mAdUnitId);
            if (bannerRemove != null) {
                bannerRemove.destroyAd();
            }
            InnerPlayAdCallback adCallback = new InnerPlayAdCallback(mAdCallback);
            com.vungle.warren.VungleBanner banner = Banners.getBanner(id, new BannerAdConfig(mAdSize), adCallback);
            if (banner != null) {
                ViewGroup.LayoutParams layoutParams = banner.getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                banner.setLayoutParams(layoutParams);
                mBannerAds.put(mAdUnitId, banner);
                banner.disableLifeCycleManagement(true);
                banner.renderAd();
                if (mAdCallback != null) {
                    mAdCallback.onBannerAdLoadSuccess(banner);
                }
            } else {
                if (mAdCallback != null) {
                    mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "VungleAdapter", "Load Vungle banner error"));
                }
            }
        }

        @Override
        public void onError(String id, VungleException exception) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "VungleAdapter", exception.getExceptionCode(), exception.getLocalizedMessage()));
            }
        }
    }

    private static class InnerPlayAdCallback implements PlayAdCallback {
        private final BannerAdCallback mAdCallback;

        private InnerPlayAdCallback(BannerAdCallback callback) {
            this.mAdCallback = callback;
        }

        @Override
        public void creativeId(String creativeId) {

        }

        @Override
        public void onAdStart(String id) {
            AdLog.getSingleton().LogD("VungleBannerManager", "onAdStart: " + id);
        }

        @Override
        public void onAdEnd(String id, boolean completed, boolean isCTAClicked) {

        }

        @Override
        public void onAdEnd(String id) {
        }

        @Override
        public void onAdClick(String id) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onAdRewarded(String id) {

        }

        @Override
        public void onAdLeftApplication(String id) {

        }

        @Override
        public void onError(String id, VungleException exception) {

        }

        @Override
        public void onAdViewed(String id) {
            AdLog.getSingleton().LogD("VungleBannerManager", "onAdViewed: " + id);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }
    }

    private AdConfig.AdSize getAdSize(Context context, Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        if (TextUtils.isEmpty(bannerDesc)) {
            return AdConfig.AdSize.BANNER;
        }
        switch (bannerDesc) {
            case MediationUtil.DESC_BANNER:
                return AdConfig.AdSize.BANNER;
            case MediationUtil.DESC_LEADERBOARD:
                return AdConfig.AdSize.BANNER_LEADERBOARD;
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(context)) {
                    return AdConfig.AdSize.BANNER_LEADERBOARD;
                } else {
                    return AdConfig.AdSize.BANNER;
                }
            default:
                return null;
        }
    }
}
