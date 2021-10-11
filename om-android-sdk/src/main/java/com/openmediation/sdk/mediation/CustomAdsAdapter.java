// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

/**
 * CustomAdsAdapter is a base class for custom events that support RewardedVideoAd,Interstitial. By
 * implementing subclasses of CustomAdsAdapter, you can enable the mediation SDK to natively
 * support a wider variety of third-party ad networks, or execute any of your application code on
 * demand.
 * <p>
 * At runtime, the mediation SDK will find and instantiate a CustomAdsAdapter subclass as needed
 * and invoke its methods.
 */
public abstract class CustomAdsAdapter extends CustomAdParams implements RewardedVideoApi,
        InterstitialAdApi, PromotionAdApi, BannerAdApi, NativeAdApi, SplashAdApi, BidApi {

    protected String mAppKey;

    @Override
    public void initBid(Context context, Map<String, Object> dataMap) {

    }

    @Override
    public String getBiddingToken(Context context) {
        return null;
    }

    @Override
    public void getBidResponse(Context context, Map<String, Object> dataMap, BidCallback callback) {

    }

    @Override
    public boolean isS2S() {
        return false;
    }

    @Override
    public boolean needPayload() {
        return false;
    }

    @Override
    public void notifyWin(String placementId, Map<String, Object> dataMap) {

    }

    @Override
    public void notifyLose(String placementId, Map<String, Object> dataMap) {

    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> extras
            , RewardedVideoCallback callback) {
        initData(extras);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> extras, InterstitialAdCallback callback) {
        initData(extras);
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        initData(extras);
    }

    @Override
    public void initPromotionAd(Activity activity, Map<String, Object> extras, PromotionAdCallback callback) {
        initData(extras);
    }

    public void onResume(Activity activity) {

    }

    public void onPause(Activity activity) {

    }

    /**
     * Gets current third-part ad network's version
     *
     * @return third-part ad network's version
     */
    public abstract String getMediationVersion();

    /**
     * Gets current adapter's version
     *
     * @return adapter's version
     */
    public abstract String getAdapterVersion();

    /**
     * Get current third-part ad network's id
     *
     * @return third-part ad network's id
     */
    public abstract int getAdNetworkId();

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {

    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {

    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return false;
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {

    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {

    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return false;
    }


    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        initData(extras);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return false;
    }

    @Override
    public void destroyBannerAd(String adUnitId) {

    }

    @Override
    public void loadPromotionAd(Activity activity, String adUnitId, Map<String, Object> extras, PromotionAdCallback callback) {
    }

    @Override
    public void showPromotionAd(Activity activity, String adUnitId, Map<String, Object> extras, PromotionAdCallback callback) {
    }

    @Override
    public void hidePromotionAd(String adUnitId, PromotionAdCallback callback) {
    }

    @Override
    public boolean isPromotionAdAvailable(String adUnitId) {
        return false;
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        initData(extras);
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        initData(extras);
    }

    @Deprecated
    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, NativeAdCallback callback) {

    }

    @Override
    public void registerNativeAdView(String adUnitId, NativeAdView adView, AdnAdInfo adnAdInfo, NativeAdCallback callback) {

    }

    @Deprecated
    @Override
    public void destroyNativeAd(String adUnitId) {

    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adnAdInfo) {

    }

    @Override
    public void initSplashAd(Activity activity, Map<String, Object> extras, SplashAdCallback callback) {
        initData(extras);
    }

    @Override
    public void loadSplashAd(Activity activity, String adUnitId, Map<String, Object> extras, SplashAdCallback callback) {
        initData(extras);
    }

    @Override
    public void showSplashAd(Activity activity, String adUnitId, ViewGroup viewGroup, SplashAdCallback callback) {

    }

    @Override
    public boolean isSplashAdAvailable(String adUnitId) {
        return false;
    }

    @Override
    public void destroySplashAd(String adUnitId) {

    }

    protected String check() {
        if (TextUtils.isEmpty(mAppKey)) {
            return "app key is empty";
        }
        return "";
    }

    protected String check(Activity activity) {
        if (activity == null) {
            return "activity is null";
        }
        if (isDestroyed(activity)) {
            return "activity is destroyed";
        }
        return "";
    }

    protected String check(String adUnitId) {
        if (TextUtils.isEmpty(mAppKey)) {
            return "app key is null";
        }
        if (TextUtils.isEmpty(adUnitId)) {
            return "instanceKey is null";
        }
        return "";
    }

    protected String check(Activity activity, String adUnitId) {
        String str = check(activity);
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        if (TextUtils.isEmpty(mAppKey)) {
            return "app key is null";
        }
        if (TextUtils.isEmpty(adUnitId)) {
            return "instanceKey is null";
        }
        return "";
    }

    private void initData(Map<String, Object> extras) {
        if (!TextUtils.isEmpty(mAppKey)) {
            return;
        }
        mAppKey = (String) extras.get("AppKey");
    }

    /**
     * Checks if an Activity is available
     *
     * @param activity the given activity
     * @return activity availability
     */
    private boolean isDestroyed(Activity activity) {
        boolean flage = false;
        if (Build.VERSION.SDK_INT >= 17) {
            if (activity == null || activity.isDestroyed()) {
                flage = true;
            }
        } else {
            if (activity == null || activity.isFinishing()) {
                flage = true;
            }
        }
        return flage;
    }

}
