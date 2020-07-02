// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

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
public abstract class CustomAdsAdapter implements RewardedVideoApi, InterstitialAdApi, BannerAdApi {

    protected String mAppKey;

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        initData(activity, dataMap);
    }

    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        initData(activity, dataMap);
    }

    @Override
    public void initBannerAd(Context activity, Map<String, Object> dataMap, BannerAdCallback callback) {
        initData(activity, dataMap);
    }

    public void onResume(Context activity) {

    }

    public void onPause(Context activity) {

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

    public String getPartKey(){
        return "";
    }


    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {

    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {

    }

    @Override
    public void showRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {

    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return false;
    }


    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {

    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {

    }

    @Override
    public void showInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {

    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return false;
    }


    @Override
    public void loadBannerAd(Context activity, String adUnitId, BannerAdCallback callback) {

    }

    @Override
    public void destroyBannerAd() {

    }

    protected String check(Context activity, String adUnitId) {
        if (activity == null) {
            return "activity is null";
        }
        if (isDestroyed(activity)) {
            return "activity is destroyed";
        }
        if (TextUtils.isEmpty(mAppKey)) {
            return "app key is null";
        }
        if (TextUtils.isEmpty(adUnitId)) {
            return "instanceKey is null";
        }
        return "";
    }

    protected String check(Context activity) {
        if (activity == null) {
            return "activity is null";
        }
        if (isDestroyed(activity)) {
            return "activity is destroyed";
        }
        if (TextUtils.isEmpty(mAppKey)) {
            return "app key is empty";
        }
        return "";
    }

    private void initData(Context activity, Map<String, Object> dataMap) {
        if (!TextUtils.isEmpty(mAppKey)) {
            return;
        }
        mAppKey = (String) dataMap.get("AppKey");
    }

    /**
     * Checks if an Context is available
     *
     * @param context the given activity
     * @return activity availability
     */
    private boolean isDestroyed(Context context) {
        boolean flage = false;
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 17) {
            if (activity.isDestroyed()) {
                flage = true;
            }
        } else {
            if (activity.isFinishing()) {
                flage = true;
            }
        }
        return flage;
    }

}
