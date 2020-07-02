// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.admob.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.HandlerUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdMobAdapter extends CustomAdsAdapter {

    private ConcurrentMap<String, RewardedAd> mRewardedAds;
    private ConcurrentMap<String, InterstitialAd> mInterstitialAds;
    private ConcurrentHashMap<String, Boolean> mAdUnitReadyStatus;
    private ConcurrentMap<String, RewardedVideoCallback> mRvInitCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsInitCallbacks;
    private volatile InitState mInitState = InitState.NOT_INIT;
    private WeakReference<Context> mRefAct;

    public AdMobAdapter() {
        mRewardedAds = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mAdUnitReadyStatus = new ConcurrentHashMap<>();
        mRvInitCallbacks = new ConcurrentHashMap<>();
        mIsInitCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        if (InitState.INIT_SUCCESS == mInitState) {
            return MobileAds.getVersionString();
        }
        return "";
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_2;
    }

    // All calls to MobileAds must be on the main thread --> run all calls to initSDK in a thread.
    private synchronized void initSDK(final Context activity) {
        mInitState = InitState.INIT_PENDING;
        String adMobAppKey = null;
        try {
            ApplicationInfo appInfo = activity.getPackageManager().getApplicationInfo(activity.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            adMobAppKey = bundle.getString("com.google.android.gms.ads.APPLICATION_ID");
        } catch (Exception e) {
            AdLog.getSingleton().LogE("AdMob can't find APPLICATION_ID in manifest.xml ");
        }

        if (TextUtils.isEmpty(adMobAppKey)) {
            adMobAppKey = mAppKey;
        }

        if (TextUtils.isEmpty(adMobAppKey)) {
            MobileAds.initialize(activity.getApplicationContext());
            onInitSuccess();
        } else {
            MobileAds.initialize(activity.getApplicationContext(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    HandlerUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onInitSuccess();
                        }
                    });
                }
            });
        }
    }

    private void onInitSuccess() {
        mInitState = InitState.INIT_SUCCESS;
        for (InterstitialAdCallback callback : mIsInitCallbacks.values()) {
            callback.onInterstitialAdInitSuccess();
        }
        mIsInitCallbacks.clear();
        for (RewardedVideoCallback callback : mRvInitCallbacks.values()) {
            callback.onRewardedVideoInitSuccess();
        }
        mRvInitCallbacks.clear();
    }

    /*********************************RewardedVideoAd***********************************/
    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed("Must be called on the main UI thread. ");
            }
            return;
        }
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    if (dataMap.get("pid") != null && callback != null) {
                        mRvInitCallbacks.put((String) dataMap.get("pid"), callback);
                    }
                    initSDK(activity);
                    break;
                case INIT_PENDING:
                    if (dataMap.get("pid") != null && callback != null) {
                        mRvInitCallbacks.put((String) dataMap.get("pid"), callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed("Must be called on the main UI thread. ");
            }
            return;
        }

        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
            return;
        }
        RewardedAd rewardedAd = getRewardedAd(activity, adUnitId);
        AdLog.getSingleton().LogD("load RewardedVideoAd : " + getAdNetworkId() + " key : " + adUnitId);
        if (rewardedAd != null) {
            if (!rewardedAd.isLoaded()) {
                rewardedAd.loadAd(new AdRequest.Builder().build(), createRvLoadListener(adUnitId, callback));
            } else {
                mAdUnitReadyStatus.put(adUnitId, true);
                callback.onRewardedVideoLoadSuccess();
            }
        }
    }

    @Override
    public void showRewardedVideo(final Context activity, final String adUnitId
            , final RewardedVideoCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String error = check(activity, adUnitId);
                if (!TextUtils.isEmpty(error)) {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(error);
                    }
                    return;
                }
                RewardedAd rewardedAd = mRewardedAds.get(adUnitId);
                if (rewardedAd != null && rewardedAd.isLoaded()) {
                    mAdUnitReadyStatus.remove(adUnitId);
                    mRefAct = new WeakReference<>(activity);
                    rewardedAd.show((Activity)mRefAct.get(), createRvCallback(adUnitId, callback));
                } else {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed("");
                    }
                }
            }
        });
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mAdUnitReadyStatus.containsKey(adUnitId);
    }

    /**
     * Creates a new one everytime for it can't be re-used.
     */
    private RewardedAd getRewardedAd(Context activity, String adUnitId) {
        mRefAct = new WeakReference<>(activity);
        RewardedAd rewardedAd = new RewardedAd(mRefAct.get(), adUnitId);
        mRewardedAds.put(adUnitId, rewardedAd);
        return rewardedAd;
    }

    private RewardedAdLoadCallback createRvLoadListener(final String adUnitId, final RewardedVideoCallback callback) {
        return new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                mAdUnitReadyStatus.put(adUnitId, true);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            @Override
            public void onRewardedAdFailedToLoad(int i) {
                super.onRewardedAdFailedToLoad(i);
                mRewardedAds.remove(adUnitId);
                mRefAct.clear();
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed("onRewardedAdFailedToLoad" + i);
                }
            }
        };
    }

    private RewardedAdCallback createRvCallback(final String adUnitId, final RewardedVideoCallback callback) {
        return new RewardedAdCallback() {
            @Override
            public void onRewardedAdOpened() {
                super.onRewardedAdOpened();
                if (callback != null) {
                    callback.onRewardedVideoAdShowSuccess();
                }
            }

            @Override
            public void onRewardedAdFailedToShow(int i) {
                super.onRewardedAdFailedToShow(i);
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed("onRewardedAdFailedToShow" + i);
                }
            }

            @Override
            public void onUserEarnedReward(com.google.android.gms.ads.rewarded.RewardItem rewardItem) {
                if (callback != null) {
                    callback.onRewardedVideoAdRewarded();
                }
            }

            @Override
            public void onRewardedAdClosed() {
                super.onRewardedAdClosed();
                mRewardedAds.remove(adUnitId);
                mRefAct.clear();
                if (callback != null) {
                    callback.onRewardedVideoAdClosed();
                }
            }
        };

    }

    /*********************************Interstitial***********************************/
    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed("Must be called on the main UI thread. ");
            }
            return;
        }
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    if (dataMap.get("pid") != null && callback != null) {
                        mIsInitCallbacks.put((String) dataMap.get("pid"), callback);
                    }
                    initSDK(activity);
                    break;
                case INIT_PENDING:
                    if (dataMap.get("pid") != null && callback != null) {
                        mIsInitCallbacks.put((String) dataMap.get("pid"), callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
        }
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed("Must be called on the main UI thread. ");
            }
            return;
        }

        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
            return;
        }
        InterstitialAd interstitialAd = getInterstitialAd(activity, adUnitId);
        if (interstitialAd.isLoaded()) {
            mAdUnitReadyStatus.put(adUnitId, true);
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            interstitialAd.setAdListener(createInterstitialListener(adUnitId, callback));
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    public void showInterstitialAd(final Context activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String error = check(activity, adUnitId);
                if (!TextUtils.isEmpty(error)) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(error);
                    }
                    return;
                }
                if (!isInterstitialAdAvailable(adUnitId)) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed("ad not ready");
                    }
                    return;
                }
                mAdUnitReadyStatus.remove(adUnitId);
                InterstitialAd ad = mInterstitialAds.get(adUnitId);
                if (ad != null) {
                    ad.show();
                } else {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed("ad not ready");
                    }
                }
            }
        });
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mAdUnitReadyStatus.containsKey(adUnitId);
    }

    private InterstitialAd getInterstitialAd(Context activity, String adUnitId) {
        mRefAct = new WeakReference<>(activity);
        InterstitialAd interstitialAd = new InterstitialAd(mRefAct.get());
        interstitialAd.setAdUnitId(adUnitId);
        mInterstitialAds.put(adUnitId, interstitialAd);
        return interstitialAd;
    }

    private AdListener createInterstitialListener(final String adUnitId, final InterstitialAdCallback callback) {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdUnitReadyStatus.put(adUnitId, true);
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                mInterstitialAds.remove(adUnitId);
                mRefAct.clear();
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed("AdMob load failed " + i);
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (callback != null) {
                    callback.onInterstitialAdClick();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAds.remove(adUnitId);
                mRefAct.clear();
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }
        };
    }


    private enum InitState {
        /**
         *
         */
        NOT_INIT,
        /**
         *
         */
        INIT_PENDING,
        /**
         *
         */
        INIT_SUCCESS,
    }
}
