// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

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
    private WeakReference<Activity> mRefAct;

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
        return com.openmediation.sdk.mobileads.admob.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_2;
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        int value = restricted ? MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE : MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;
        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
                .setTagForChildDirectedTreatment(value)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        setAgeRestricted(context, age < 13);
    }

    private AdRequest createAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        if (mUserConsent != null || mUSPrivacyLimit != null) {
            Bundle extras = new Bundle();
            if (mUserConsent != null && !mUserConsent) {
                extras.putString("npa", "1");
            }
            if (mUSPrivacyLimit != null) {
                extras.putInt("rdp", mUSPrivacyLimit ? 1 : 0);
            }
            builder.addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter.class, extras);
        }
        return builder.build();
    }

    // All calls to MobileAds must be on the main thread --> run all calls to initSDK in a thread.
    private synchronized void initSDK(final Activity activity) {
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
                    activity.runOnUiThread(new Runnable() {
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
    public void initRewardedVideo(final Activity activity, final Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
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
                            callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Init Failed: Unknown Error"));
                    }
                }
            }
        });
    }

    @Override
    public void loadRewardedVideo(final Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                        }
                        return;
                    }
                    RewardedAd rewardedAd = getRewardedAd(activity, adUnitId);
                    if (!rewardedAd.isLoaded()) {
                        rewardedAd.loadAd(createAdRequest(), createRvLoadListener(adUnitId, callback));
                    } else {
                        mAdUnitReadyStatus.put(adUnitId, true);
                        callback.onRewardedVideoLoadSuccess();
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void showRewardedVideo(final Activity activity, final String adUnitId
            , final RewardedVideoCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                        }
                        return;
                    }
                    RewardedAd rewardedAd = mRewardedAds.get(adUnitId);
                    if (rewardedAd != null && rewardedAd.isLoaded()) {
                        mAdUnitReadyStatus.remove(adUnitId);
                        mRefAct = new WeakReference<>(activity);
                        rewardedAd.show(mRefAct.get(), createRvCallback(adUnitId, callback));
                    } else {
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error"));
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
    private RewardedAd getRewardedAd(Activity activity, String adUnitId) {
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
            public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                super.onRewardedAdFailedToLoad(loadAdError);
                mRewardedAds.remove(adUnitId);
                mRefAct.clear();
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
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
                    callback.onRewardedVideoAdStarted();
                }
            }

            @Override
            public void onRewardedAdFailedToShow(AdError adError) {
                super.onRewardedAdFailedToShow(adError);
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, adError.getCode(), adError.getMessage()));
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
                    callback.onRewardedVideoAdEnded();
                    callback.onRewardedVideoAdClosed();
                }
            }
        };

    }

    /*********************************Interstitial***********************************/
    @Override
    public void initInterstitialAd(final Activity activity, final Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
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
                            callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error"));
                    }
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(final Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
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
                        interstitialAd.loadAd(createAdRequest());
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    public void showInterstitialAd(final Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                        }
                        return;
                    }
                    if (!isInterstitialAdAvailable(adUnitId)) {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad not ready"));
                        }
                        return;
                    }
                    mAdUnitReadyStatus.remove(adUnitId);
                    InterstitialAd ad = mInterstitialAds.get(adUnitId);
                    if (ad != null) {
                        ad.show();
                    } else {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad not ready"));
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error"));
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

    private InterstitialAd getInterstitialAd(Activity activity, String adUnitId) {
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
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAds.remove(adUnitId);
                mRefAct.clear();
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
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
