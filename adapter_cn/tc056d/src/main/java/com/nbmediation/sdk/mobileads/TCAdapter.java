// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.tc056d.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;

import test.android.com.new_lib.WorkInit;

public class TCAdapter extends CustomAdsAdapter {
    private static String TAG = "OM-TC: ";

    public TCAdapter() {
    }

    @Override
    public String getMediationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_19;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        initSdk(activity);
        if (callback != null) {
            callback.onRewardedVideoInitFailed("init rewarded error");
        }
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    private void loadRvAd(Context activity, String adUnitId, RewardedVideoCallback callback) {

        if (callback != null) {
            callback.onRewardedVideoLoadFailed("load error");
        }
    }

    @Override
    public void showRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return false;
    }

    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        initSdk(activity);
        if (callback != null) {
            callback.onInterstitialAdInitFailed("init interstitial error");
        }
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        loadInterstitial(activity, adUnitId, callback);
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, Map<String, Object> extras,
                                   InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadInterstitial(activity, adUnitId, callback);
    }

    private void loadInterstitial(Context activity, String adUnitId, InterstitialAdCallback callback) {
        if (callback != null) {
            callback.onInterstitialAdLoadFailed("load interstitial error");
        }
    }

    @Override
    public void showInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);

    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return false;
    }

    private void initSdk(final Context activity) {
        WorkInit.getInstance().init(activity.getApplicationContext());
        AdLog.getSingleton().LogD(TAG, "tc is init..");
    }


}
