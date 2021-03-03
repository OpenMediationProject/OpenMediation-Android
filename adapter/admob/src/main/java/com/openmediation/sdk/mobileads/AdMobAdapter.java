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

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdMobAdapter extends CustomAdsAdapter {

    private final ConcurrentMap<String, RewardedAd> mRewardedAds;
    private final ConcurrentMap<String, InterstitialAd> mInterstitialAds;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvInitCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsInitCallbacks;
    private volatile InitState mInitState = InitState.NOT_INIT;

    public AdMobAdapter() {
        mRewardedAds = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
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
    public void loadRewardedVideo(final Activity activity, final String adUnitId, Map<String, Object> extras, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
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
                    if (isRewardedVideoAvailable(adUnitId)) {
                        callback.onRewardedVideoLoadSuccess();
                        return;
                    }
                    RewardedAd.load(activity.getApplicationContext(), adUnitId, createAdRequest(), createRvLoadListener(adUnitId, callback));
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
                    showAdMobVideo(activity, adUnitId, callback);
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error"));
                    }
                }
            }
        });
    }

    private void showAdMobVideo(Activity activity, String adUnitId, final RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (!isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Ad Not Ready"));
            }
            return;
        }
        RewardedAd rewardedAd = mRewardedAds.get(adUnitId);
        if (rewardedAd == null) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Ad Not Ready"));
            }
            return;
        }
        rewardedAd.setFullScreenContentCallback(createRvCallback(callback));
        rewardedAd.show(activity, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                AdLog.getSingleton().LogE("AdMobAdapter", "----onUserEarnedReward");
                if (callback != null) {
                    callback.onRewardedVideoAdRewarded();
                }
            }
        });
        mRewardedAds.remove(adUnitId);
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mRewardedAds.containsKey(adUnitId);
    }

    private RewardedAdLoadCallback createRvLoadListener(final String adUnitId, final RewardedVideoCallback callback) {
        return new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd ad) {
                super.onAdLoaded(ad);
                mRewardedAds.put(adUnitId, ad);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                super.onAdFailedToLoad(error);
                mRewardedAds.remove(adUnitId);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getCode(), error.getMessage()));
                }
            }
        };
    }

    private FullScreenContentCallback createRvCallback(final RewardedVideoCallback callback) {
        return new FullScreenContentCallback() {

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                AdLog.getSingleton().LogE("AdMobAdapter", "----RewardedAd onAdFailedToShowFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, adError.getCode(), adError.getMessage()));
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AdLog.getSingleton().LogE("AdMobAdapter", "----RewardedAd onAdShowedFullScreenContent");
                if (callback != null) {
                    callback.onRewardedVideoAdShowSuccess();
                    callback.onRewardedVideoAdStarted();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AdLog.getSingleton().LogE("AdMobAdapter", "----RewardedAd onAdDismissedFullScreenContent");
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
    public void loadInterstitialAd(final Activity activity, final String adUnitId, Map<String, Object> extras, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
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
                    if (isInterstitialAdAvailable(adUnitId)) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadSuccess();
                        }
                        return;
                    }
                    InterstitialAd.load(activity.getApplicationContext(), adUnitId, createAdRequest(), createInterstitialListener(adUnitId, callback));
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
                    showAdMobInterstitial(activity, adUnitId, callback);
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error"));
                    }
                }
            }
        });
    }

    private void showAdMobInterstitial(Activity activity, String adUnitId, InterstitialAdCallback callback) {
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
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Ad Not Ready"));
            }
            return;
        }
        InterstitialAd ad = mInterstitialAds.get(adUnitId);
        if (ad != null) {
            ad.setFullScreenContentCallback(createIsCallback(callback));
            ad.show(activity);
        }
        mInterstitialAds.remove(adUnitId);
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mInterstitialAds.containsKey(adUnitId);
    }

    private InterstitialAdLoadCallback createInterstitialListener(final String adUnitId, final InterstitialAdCallback callback) {
        return new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAds.put(adUnitId, interstitialAd);
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAds.remove(adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
                }
            }
        };
    }

    private FullScreenContentCallback createIsCallback(final InterstitialAdCallback callback) {
        return new FullScreenContentCallback() {

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                AdLog.getSingleton().LogE("AdMobAdapter", "----InterstitialAd onAdFailedToShowFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, adError.getCode(), adError.getMessage()));
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AdLog.getSingleton().LogE("AdMobAdapter", "----InterstitialAd onAdShowedFullScreenContent");
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AdLog.getSingleton().LogE("AdMobAdapter", "----InterstitialAd onAdDismissedFullScreenContent");
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
