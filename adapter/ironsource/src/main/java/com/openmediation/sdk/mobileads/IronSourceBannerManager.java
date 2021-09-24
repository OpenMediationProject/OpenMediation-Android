package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class IronSourceBannerManager {

    private AtomicBoolean mDidBannerInited = new AtomicBoolean(false);
    private ConcurrentHashMap<String, IronSourceBannerLayout> mIrBannerLayouts;
    private ConcurrentHashMap<String, Boolean> mBannerLoaded;

    public boolean isInit() {
        return mDidBannerInited.get();
    }

    private static class BannerHolder {
        private static final IronSourceBannerManager INSTANCE = new IronSourceBannerManager();
    }

    private IronSourceBannerManager() {
        mIrBannerLayouts = new ConcurrentHashMap<>();
        mBannerLoaded = new ConcurrentHashMap<>();
    }

    public static IronSourceBannerManager getInstance() {
        return BannerHolder.INSTANCE;
    }

    public void initAd(Activity activity, Map<String, Object> extras, final BannerAdCallback callback) {
        try {
            String appKey = extras.get("AppKey").toString();
            IronSource.init(activity, appKey, IronSource.AD_UNIT.BANNER);
            mDidBannerInited.set(true);
            if (callback != null) {
                callback.onBannerAdInitSuccess();
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "IronSourceAdapter", e.getLocalizedMessage()));
            }
        }
    }

    public void loadAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        // ProgBannerManager loadBanner - can't load banner - loadBanner already called and still in progress
        destroyAd(adUnitId);
        ISBannerSize bannerSize = getAdSize(extras);
        IronSourceBannerLayout bannerLayout = IronSource.createBanner(activity, bannerSize);
        InnerBannerAdListener listener = new InnerBannerAdListener(bannerLayout, adUnitId, callback);
        bannerLayout.setBannerListener(listener);
        mIrBannerLayouts.put(adUnitId, bannerLayout);
        IronSource.loadBanner(bannerLayout, adUnitId);
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mIrBannerLayouts.containsKey(adUnitId) && mBannerLoaded.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mIrBannerLayouts.containsKey(adUnitId)) {
            IronSourceBannerLayout bannerLayout = mIrBannerLayouts.remove(adUnitId);
            mBannerLoaded.remove(adUnitId);
            IronSource.destroyBanner(bannerLayout);
        }
    }

    private class InnerBannerAdListener implements BannerListener {

        private String mAdUnitId;
        private BannerAdCallback mAdCallback;
        private IronSourceBannerLayout mBannerLayout;

        private InnerBannerAdListener(IronSourceBannerLayout bannerLayout, String adUnitId, BannerAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
            this.mBannerLayout = bannerLayout;
        }

        @Override
        public void onBannerAdLoaded() {
            mBannerLoaded.put(mAdUnitId, true);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadSuccess(mBannerLayout);
            }
        }

        @Override
        public void onBannerAdLoadFailed(IronSourceError error) {
            mBannerLoaded.put(mAdUnitId, false);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "IronSourceAdapter", error.getErrorCode(), error.getErrorMessage()));
            }
        }

        @Override
        public void onBannerAdClicked() {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onBannerAdScreenPresented() {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }

        @Override
        public void onBannerAdScreenDismissed() {

        }

        @Override
        public void onBannerAdLeftApplication() {

        }
    }

    private ISBannerSize getAdSize(Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_LEADERBOARD:
                return new ISBannerSize(728, 90);
            case MediationUtil.DESC_RECTANGLE:
                return ISBannerSize.RECTANGLE;
            case MediationUtil.DESC_SMART:
                return ISBannerSize.SMART;
            default:
                return ISBannerSize.BANNER;
        }
    }
}
