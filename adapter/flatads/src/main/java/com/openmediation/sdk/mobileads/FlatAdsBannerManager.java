// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import static com.openmediation.sdk.mobileads.FlatAdsAdapter.BID;
import static com.openmediation.sdk.mobileads.FlatAdsSingleTon.TAG;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.flatads.sdk.callback.BannerAdListener;
import com.flatads.sdk.callback.InitListener;
import com.flatads.sdk.statics.ErrorCode;
import com.flatads.sdk.ui.view.BannerAdView;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

public class FlatAdsBannerManager {

    private static class Holder {
        private static final FlatAdsBannerManager INSTANCE = new FlatAdsBannerManager();
    }

    private FlatAdsBannerManager() {
    }

    public static FlatAdsBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(String appKey, BannerAdCallback callback) {
        FlatAdsSingleTon.getInstance().init(appKey, new InitListener() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void onFailure(int code, String msg) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", code, msg));
                }
            }

        });
    }

    public void loadAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        boolean bid = false;
        if (extras.containsKey(BID) && extras.get(BID) instanceof Integer) {
            bid = ((int) extras.get(BID)) == 1;
        }
        if (bid) {
            loadAndShowBanner(adUnitId, callback);
        } else {
            loadBannerAd(activity, adUnitId, extras, callback);
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        BannerAdView bannerAdView = FlatAdsSingleTon.getInstance().getBannerAdView(adUnitId);
        return bannerAdView != null;
    }

    private void putBannerView(String adUnitId, BannerAdView bannerAdView) {
        FlatAdsSingleTon.getInstance().petBannerAdView(adUnitId, bannerAdView);
    }

    public void destroyAd(String adUnitId) {
        BannerAdView bannerAdView = FlatAdsSingleTon.getInstance().getBannerAdView(adUnitId);
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
    }

    void loadAndShowBanner(String adUnitId, BannerAdCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    BannerAdView bannerAdView = FlatAdsSingleTon.getInstance().getBannerAdView(adUnitId);
                    if (bannerAdView == null) {
                        if (callback != null) {
                            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "No Fill"));
                        }
                        return;
                    }
                    bannerAdView.setAdListener(new InnerBannerListener(bannerAdView, adUnitId, callback));
                    bannerAdView.winBidding();
                    bannerAdView.loadAd();
                } catch (Throwable e) {
                    destroyAd(adUnitId);
                    AdLog.getSingleton().LogD(TAG, "BannerAd Load Failed: " + e.getMessage());
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    BannerAdView bannerAdView = new BannerAdView(activity);
                    bannerAdView.setAdUnitId(adUnitId);
                    int adSize = FlatAdsSingleTon.getInstance().getAdSize(MediationUtil.getBannerDesc(extras));
                    RelativeLayout.LayoutParams layoutParams;
                    if (adSize == 1) {
                        layoutParams = new RelativeLayout.LayoutParams(
                                MediationUtil.dip2px(MediationUtil.getContext(), 300),
                                MediationUtil.dip2px(MediationUtil.getContext(), 250));
                    } else {
                        layoutParams = new RelativeLayout.LayoutParams(
                                MediationUtil.dip2px(MediationUtil.getContext(), 320),
                                MediationUtil.dip2px(MediationUtil.getContext(), 50));
                    }
                    bannerAdView.setLayoutParams(layoutParams);
                    bannerAdView.setBannerSize(adSize);
                    bannerAdView.setAdListener(new InnerBannerListener(bannerAdView, adUnitId, callback));
                    bannerAdView.loadAd();
                } catch (Throwable e) {
                    AdLog.getSingleton().LogE(TAG, "FlatAds BannerAd load error : " + e.getMessage());
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", "Unknown Error, " + e.getMessage()));
                    }
                }
            }
        };
        MediationUtil.runOnUiThread(runnable);
    }

    private class InnerBannerListener implements BannerAdListener {
        BannerAdCallback callback;
        String adUnitId;
        BannerAdView bannerAdView;

        private InnerBannerListener(BannerAdView bannerAdView, String adUnitId, BannerAdCallback callback) {
            this.bannerAdView = bannerAdView;
            this.adUnitId = adUnitId;
            this.callback = callback;
        }

        @Override
        public void onAdExposure() {
            AdLog.getSingleton().LogD(TAG, "FlatAds BannerAd onAdExposure adUnitId : " + adUnitId);
            if (callback != null) {
                callback.onBannerAdImpression();
            }
        }

        @Override
        public void onAdClick() {
            AdLog.getSingleton().LogD(TAG, "FlatAds BannerAd onAdClick adUnitId : " + adUnitId);
            if (callback != null) {
                callback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onAdClose() {
            AdLog.getSingleton().LogD(TAG, "FlatAds BannerAd onAdClose adUnitId : " + adUnitId);
        }

        @Override
        public void onAdLoadSuc() {
            AdLog.getSingleton().LogD(TAG, "BannerAd onAdSucLoad adUnitId : " + adUnitId);
            putBannerView(adUnitId, bannerAdView);
            if (callback != null) {
                callback.onBannerAdLoadSuccess(bannerAdView);
            }
        }

        @Override
        public void onAdLoadFail(ErrorCode errorCode) {
            AdLog.getSingleton().LogE(TAG, "BannerAd LoadFailed: " + errorCode.getCode() + ", " + errorCode.getMsg());
            destroyAd(adUnitId);
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "FlatAdsAdapter", errorCode.getCode(), errorCode.getMsg()));
            }
        }
    }

}
