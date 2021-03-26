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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomSplashEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.util.Map;

public class AdMobSplash extends CustomSplashEvent {
    private AppOpenAd mAppOpenAd;
    private Long mAdLoadTime;

    private static final long mTimeTimeLess = 4 * 3600000;

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

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_2;
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!check(activity)) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Activity is null"));
            return;
        }
        loadSplashAd(activity, mInstancesKey, config);
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        mAppOpenAd = null;
        mAdLoadTime = 0L;
    }

    /**
     * Request an ad
     */
    public void loadSplashAd(final Activity activity, final String adUnit, Map<String, String> config) {
        final int orientation;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                super.onAdLoaded(ad);
                if (isDestroyed) {
                    return;
                }
                if (ad != null) {
                    mAppOpenAd = ad;
                    mAdLoadTime = SystemClock.elapsedRealtime();
                    onInsReady(null);
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
                if (isDestroyed) {
                    return;
                }
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
            }
        };
        final AdRequest request = createAdRequest();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                AppOpenAd.load(activity.getApplicationContext(), adUnit, request, orientation, loadCallback);
            }
        });
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        super.show(activity, container);
        showSplashAd(activity, container);
    }

    @Override
    public void show(final Activity activity) {
        showSplashAd(activity, null);
    }

    private void showSplashAd(final Activity activity, ViewGroup container) {
        if (!check(activity)) {
            onInsError(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "Activity is null"));
            return;
        }
        if (!isReady()) {
            onInsShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, "SplashAd not ready"));
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    showSplashAd(activity);
                } catch (Exception e) {
                    onInsShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, e.getMessage()));
                }
            }
        });
    }

    private void showSplashAd(Activity activity) {
        FullScreenContentCallback fullScreenContentCallback =
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        if (isDestroyed) {
                            return;
                        }
                        onInsDismissed();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        if (isDestroyed) {
                            return;
                        }
                        onInsError(AdapterErrorBuilder.buildShowError(
                                AdapterErrorBuilder.AD_UNIT_SPLASH, mAdapterName, adError.getCode(), adError.getMessage()));
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        if (isDestroyed) {
                            return;
                        }
                        onInsShowSuccess();
                    }
                };
        mAppOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
        mAppOpenAd.show(activity);
        mAppOpenAd = null;
        mAdLoadTime = 0L;
    }

    @Override
    public boolean isReady() {
        if (isDestroyed || TextUtils.isEmpty(mInstancesKey)) {
            return false;
        }
        return mAppOpenAd != null && mAdLoadTime > 0
                && wasLoadTimeLessThanNHoursAgo(mAdLoadTime);
    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long loadTime) {
        long dateDifference = SystemClock.elapsedRealtime() - loadTime;
        return dateDifference < mTimeTimeLess;
    }

}
