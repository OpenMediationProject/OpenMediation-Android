// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.google.android.gms.ads.nativead.NativeAd;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomNativeEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public class AdMobNative extends CustomNativeEvent {

    private AdLoader mAdLoader;
    private NativeAd mAdMobNativeAd;
    private com.google.android.gms.ads.nativead.NativeAdView mUnifiedNativeAdView;
    private MediaView mMediaView;
    private AdIconView mAdIconView;

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        int value = restricted? MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE : MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;
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
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }

        if (mAdLoader != null) {
            mAdLoader.loadAd(createAdRequest());
            return;
        }
        AdLoader.Builder builder = new AdLoader.Builder(activity.getApplicationContext(), mInstancesKey);
        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (!isDestroyed) {
                    mAdMobNativeAd = nativeAd;
                    mAdInfo.setType(getMediation());
                    mAdInfo.setTitle(nativeAd.getHeadline());
                    mAdInfo.setDesc(nativeAd.getBody());
                    mAdInfo.setCallToActionText(nativeAd.getCallToAction());
                    onInsReady(mAdInfo);
                }
            }
        });
        NativeAdOptions.Builder nativeAdOptionsBuilder = new NativeAdOptions.Builder();
        //single image
        nativeAdOptionsBuilder.setRequestMultipleImages(false);
        mAdLoader = builder.withNativeAdOptions(nativeAdOptionsBuilder.build()).withAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (!isDestroyed) {
                    onInsError(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, loadAdError.getCode(), loadAdError.getMessage()));
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (!isDestroyed) {
                    onInsClicked();
                }
            }
        }).build();
        mAdLoader.loadAd(createAdRequest());
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        if (isDestroyed) {
            return;
        }

        RelativeLayout relativeLayout = new RelativeLayout(adView.getContext());
        if (mAdMobNativeAd == null) {
            return;
        }

        if (adView.getMediaView() != null) {
            mMediaView = adView.getMediaView();
            adView.setMediaView(mMediaView);
        }

        if (adView.getAdIconView() != null) {
            mAdIconView = adView.getAdIconView();
            adView.setAdIconView(mAdIconView);
        }
        mUnifiedNativeAdView = new com.google.android.gms.ads.nativead.NativeAdView(adView.getContext());
        if (adView.getTitleView() != null) {
            mUnifiedNativeAdView.setHeadlineView(adView.getTitleView());
        }

        if (adView.getDescView() != null) {
            mUnifiedNativeAdView.setBodyView(adView.getDescView());
        }

        if (adView.getCallToActionView() != null) {
            mUnifiedNativeAdView.setCallToActionView(adView.getCallToActionView());
        }

        if (mMediaView != null) {
            mMediaView.removeAllViews();
            com.google.android.gms.ads.nativead.MediaView adMobMediaView = new
                    com.google.android.gms.ads.nativead.MediaView(adView.getContext());
            mMediaView.addView(adMobMediaView);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            adMobMediaView.setLayoutParams(layoutParams);
            mUnifiedNativeAdView.setMediaView(adMobMediaView);
        }

        if (mAdIconView != null && mAdMobNativeAd.getIcon() != null && mAdMobNativeAd.getIcon().getDrawable() != null) {
            mAdIconView.removeAllViews();
            ImageView iconImageView = new ImageView(adView.getContext());
            mAdIconView.addView(iconImageView);
            iconImageView.setImageDrawable(mAdMobNativeAd.getIcon().getDrawable());
            iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            mUnifiedNativeAdView.setIconView(mAdIconView);
        }

        TextView textView = new TextView(adView.getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(50, 35);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        textView.setLayoutParams(layoutParams);
        textView.setBackgroundColor(Color.argb(255, 234, 234, 234));
        textView.setGravity(Gravity.CENTER);
        textView.setText("Ad");
        textView.setTextSize(10);
        textView.setTextColor(Color.argb(255, 45, 174, 201));
        relativeLayout.addView(textView);
        mUnifiedNativeAdView.setAdvertiserView(textView);

        int count = adView.getChildCount();
        for (int a = 0; a < count; a++) {
            View v = adView.getChildAt(a);
            if (v == null || v instanceof com.google.android.gms.ads.nativead.NativeAdView) {
                continue;
            }
            adView.removeView(v);
            relativeLayout.addView(v);
        }
        mUnifiedNativeAdView.setNativeAd(mAdMobNativeAd);

        textView.bringToFront();
        if (mUnifiedNativeAdView.getAdChoicesView() != null) {
            mUnifiedNativeAdView.getAdChoicesView().bringToFront();
        }
        adView.addView(mUnifiedNativeAdView);
        int l = adView.getPaddingLeft();
        int t = adView.getPaddingTop();
        int r = adView.getPaddingRight();
        int b = adView.getPaddingBottom();
        relativeLayout.setPadding(l, t, r, b);
        adView.setPadding(0, 0, 0, 0);
        adView.addView(relativeLayout);
    }

    @Override
    public void destroy(Activity activity) {
        if (mAdLoader != null) {
            mAdLoader = null;
        }
        if (mAdMobNativeAd != null) {
            mAdMobNativeAd.destroy();
            mAdMobNativeAd = null;
        }
        if (mUnifiedNativeAdView != null) {
            mUnifiedNativeAdView.removeAllViews();
            mUnifiedNativeAdView.destroy();
            mUnifiedNativeAdView = null;
        }

        isDestroyed = true;
        mMediaView = null;
        mAdIconView = null;
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_2;
    }
}
