// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerSize;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TikTokBannerManager {

    private final ConcurrentHashMap<String, PAGBannerAd> mBannerAds;

    private static class BannerHolder {
        private static final TikTokBannerManager INSTANCE = new TikTokBannerManager();
    }

    private TikTokBannerManager() {
        mBannerAds = new ConcurrentHashMap<>();
    }

    public static TikTokBannerManager getInstance() {
        return BannerHolder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, Boolean consent, Boolean ageRestricted, Boolean privacyLimit, final BannerAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        TTAdManagerHolder.getInstance().init(context, appKey, consent, ageRestricted, privacyLimit, new TTAdManagerHolder.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", code, msg));
                }
            }
        });
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        try {
            PAGBannerSize bannerSize = getAdSize(extras);
            PAGBannerRequest bannerRequest = new PAGBannerRequest(bannerSize);
            PAGBannerAd.loadAd(adUnitId, bannerRequest, new PAGBannerAdLoadListener() {
                @Override
                public void onError(int code, String message) {
                    AdLog.getSingleton().LogD("TikTokAdapter, BannerAd load onError code: " + code + ", message: " + message);
                    if (callback != null) {
                        callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", code, message));
                    }
                }

                @Override
                public void onAdLoaded(PAGBannerAd bannerAd) {
                    AdLog.getSingleton().LogD("TikTokAdapter, BannerAd onAdLoaded");
                    if (bannerAd == null || bannerAd.getBannerView() == null) {
                        if (callback != null) {
                            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", "No Fill"));
                        }
                        return;
                    }
                    bannerAd.setAdInteractionListener(new PAGBannerAdInteractionListener() {
                        @Override
                        public void onAdShowed() {
                            AdLog.getSingleton().LogD("TikTokAdapter, BannerAd onAdShowed");
                            if (callback != null) {
                                callback.onBannerAdImpression();
                            }
                        }

                        @Override
                        public void onAdClicked() {
                            AdLog.getSingleton().LogD("TikTokAdapter, BannerAd onAdClicked");
                            if (callback != null) {
                                callback.onBannerAdAdClicked();
                            }
                        }

                        @Override
                        public void onAdDismissed() {
                            AdLog.getSingleton().LogD("TikTokAdapter, BannerAd onAdDismissed");
                        }
                    });
                    if (callback != null) {
                        callback.onBannerAdLoadSuccess(bannerAd.getBannerView());
                    }
                }
            });
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "TikTokAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId)) {
            mBannerAds.remove(adUnitId).destroy();
        }
    }

    private PAGBannerSize getAdSize(Map<String, Object> config) {
        String desc = MediationUtil.getBannerDesc(config);
        PAGBannerSize bannerSize = PAGBannerSize.BANNER_W_320_H_50;
        if (MediationUtil.DESC_RECTANGLE.equals(desc)) {
            bannerSize = PAGBannerSize.BANNER_W_300_H_250;
        } else if (MediationUtil.DESC_LEADERBOARD.equals(desc)) {
            bannerSize = PAGBannerSize.BANNER_W_728_H_90;
        } else if (MediationUtil.DESC_SMART.equals(desc) && MediationUtil.isLargeScreen(MediationUtil.getContext())) {
            bannerSize = PAGBannerSize.BANNER_W_728_H_90;
        }
        return bannerSize;
    }

}
