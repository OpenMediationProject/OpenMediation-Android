// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.adtbid.sdk.banner.AdSize;
import com.adtbid.sdk.banner.BannerAd;
import com.adtbid.sdk.banner.BannerAdListener;
import com.adtbid.sdk.utils.error.AdTimingError;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AdTimingBannerManager {
    private static final String TAG = "OM-AdTiming: ";
    private static final String PAY_LOAD = "pay_load";

    private final ConcurrentMap<String, BannerAd> mBannerAds;
    private final List<BannerAdCallback> mBnCallback;

    private static class Holder {
        private static final AdTimingBannerManager INSTANCE = new AdTimingBannerManager();
    }

    private AdTimingBannerManager() {
        mBannerAds = new ConcurrentHashMap<>();
        mBnCallback = new CopyOnWriteArrayList<>();
    }

    public static AdTimingBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    public void addBannerAdCallback(BannerAdCallback callback) {
        if (callback != null) {
            mBnCallback.add(callback);
        }
    }

    public void onInitSuccess() {
        for (BannerAdCallback callback : mBnCallback) {
            callback.onBannerAdInitSuccess();
        }
        mBnCallback.clear();
    }

    public void onInitFailed(AdapterError error) {
        for (BannerAdCallback callback : mBnCallback) {
            callback.onBannerAdInitFailed(error);
        }
        mBnCallback.clear();
    }

    public void loadAd(String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        String payload = "";
        if (extras.containsKey(PAY_LOAD)) {
            payload = extras.get(PAY_LOAD).toString();
        }
        BannerAd bannerAd = new BannerAd(MediationUtil.getContext(), adUnitId);
        bannerAd.setAdListener(new InnerBannerAdListener(bannerAd, adUnitId, callback));
        AdSize adSize = getAdSize(MediationUtil.getContext(), extras);
        bannerAd.setAdSize(adSize);
        bannerAd.loadAdWithPayload(payload);
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerAds.containsKey(adUnitId)) {
            BannerAd bannerAd = mBannerAds.remove(adUnitId);
            bannerAd.destroy();
            bannerAd = null;
        }
    }

    private class InnerBannerAdListener implements BannerAdListener {

        private String mAdUnitId;
        private BannerAdCallback mAdCallback;
        private BannerAd mBannerAd;

        private InnerBannerAdListener(BannerAd bannerAd, String adUnitId, BannerAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
            this.mBannerAd = bannerAd;
        }

        @Override
        public void onBannerAdReady(String s, View view) {
            mBannerAds.put(mAdUnitId, mBannerAd);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadSuccess(view);
            }
        }

        @Override
        public void onBannerAdFailed(String s, AdTimingError error) {
            mBannerAds.remove(mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "AdTimingAdapter", error.getCode(), error.getMessage()));
            }
        }

        @Override
        public void onBannerAdClicked(String s) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onBannerAdShowFailed(String s, AdTimingError adTimingError) {

        }
    }

    private AdSize getAdSize(Context context, Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_LEADERBOARD:
                return AdSize.LEADERBOARD;
            case MediationUtil.DESC_RECTANGLE:
                return AdSize.MEDIUM_RECTANGLE;
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(context)) {
                    return AdSize.LEADERBOARD;
                } else {
                    return AdSize.BANNER;
                }
            default:
                return AdSize.BANNER;
        }
    }
}
