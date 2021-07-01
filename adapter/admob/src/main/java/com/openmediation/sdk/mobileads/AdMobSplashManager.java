// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.SplashAdCallback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AdMobSplashManager {
    private static final long TIME_LESS = 4 * 3600000;

    private final ConcurrentHashMap<String, AppOpenAd> mSplashAds;
    private final ConcurrentHashMap<String, Long> mAdLoadTimes;

    private final List<SplashAdCallback> mInitCallbacks;

    private static class Holder {
        private static final AdMobSplashManager INSTANCE = new AdMobSplashManager();
    }

    private AdMobSplashManager() {
        mSplashAds = new ConcurrentHashMap<>();
        mAdLoadTimes = new ConcurrentHashMap<>();
        mInitCallbacks = new CopyOnWriteArrayList<>();
    }

    public static AdMobSplashManager getInstance() {
        return AdMobSplashManager.Holder.INSTANCE;
    }

    public void addAdCallback(SplashAdCallback callback) {
        if (callback != null) {
            mInitCallbacks.add(callback);
        }
    }

    public void onInitSuccess() {
        for (SplashAdCallback callback : mInitCallbacks) {
            callback.onSplashAdInitSuccess();
        }
        mInitCallbacks.clear();
    }

    public void loadAd(final Context context, final String adUnitId, final Map<String, Object> config, Boolean userConsent, Boolean uSPrivacyLimit, final SplashAdCallback callback) {
        final int orientation;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE;
        } else {
            orientation = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT;
        }
        final AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {

            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            @Override
            public void onAdLoaded(AppOpenAd ad) {
                super.onAdLoaded(ad);
                if (ad != null) {
                    mSplashAds.put(adUnitId, ad);
                    mAdLoadTimes.put(adUnitId, SystemClock.elapsedRealtime());
                    if (callback != null) {
                        callback.onSplashAdLoadSuccess(null);
                    }
                }
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error.
                if (callback != null) {
                    callback.onSplashAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, "AdMobAdapter", loadAdError.getCode(), loadAdError.getMessage()));
                }
            }
        };
        final AdRequest request = createAdRequest(userConsent, uSPrivacyLimit);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                AppOpenAd.load(context, adUnitId, request, orientation, loadCallback);
            }
        });
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            AppOpenAd appOpenAd = mSplashAds.remove(adUnitId);
            appOpenAd = null;
            mAdLoadTimes.remove(adUnitId);
        }
    }

    public void showAd(final Activity activity, final String adUnitId, final ViewGroup container, final SplashAdCallback callback) {
        if (!isAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "AdMobAdapter", "SplashAd not ready"));
            }
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    showSplashAd(activity, adUnitId, callback);
                } catch(Exception e) {
                    if (callback != null) {
                        callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, "AdMobAdapter", e.getMessage()));
                    }
                }
            }
        });

    }

    public boolean isAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId) || !mAdLoadTimes.containsKey(adUnitId) || !mSplashAds.containsKey(adUnitId)) {
            return false;
        }
        Long adLoadTime = mAdLoadTimes.get(adUnitId);
        AppOpenAd appOpenAd = mSplashAds.get(adUnitId);
        return appOpenAd != null && adLoadTime > 0
                && wasLoadTimeLessThanNHoursAgo(adLoadTime);
    }


    private AdRequest createAdRequest(Boolean userConsent, Boolean uSPrivacyLimit) {
        AdRequest.Builder builder = new AdRequest.Builder();
        if (userConsent != null || uSPrivacyLimit != null) {
            Bundle extras = new Bundle();
            if (userConsent != null && !userConsent) {
                extras.putString("npa", "1");
            }
            if (uSPrivacyLimit != null) {
                extras.putInt("rdp", uSPrivacyLimit ? 1 : 0);
            }
            builder.addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter.class, extras);
        }
        return builder.build();
    }


    private void showSplashAd(Activity activity, String adUnitId, final SplashAdCallback callback) {
        AppOpenAd appOpenAd = mSplashAds.remove(adUnitId);
        if (appOpenAd == null) {
            if (callback != null) {
                callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, "AdMobAdapter", "Not Ready"));
            }
            return;
        }
        mAdLoadTimes.remove(adUnitId);
        FullScreenContentCallback fullScreenContentCallback =
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        if (callback != null) {
                            callback.onSplashAdDismissed();
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        if (callback != null) {
                            callback.onSplashAdShowFailed(AdapterErrorBuilder.buildShowError(
                                    AdapterErrorBuilder.AD_UNIT_SPLASH, "AdMobAdapter", adError.getCode(), adError.getMessage()));
                        }
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        if (callback != null) {
                            callback.onSplashAdShowSuccess();
                        }
                    }
                };
        appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
        appOpenAd.show(activity);
    }


    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long loadTime) {
        long dateDifference = SystemClock.elapsedRealtime() - loadTime;
        return dateDifference < TIME_LESS;
    }

}
