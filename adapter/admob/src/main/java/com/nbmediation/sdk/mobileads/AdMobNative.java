// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdIconView;
import com.nbmediation.sdk.nativead.MediaView;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.Map;

public class AdMobNative extends CustomNativeEvent {

    private AdLoader mAdLoader;
    private UnifiedNativeAd mUnifiedNativeAd;
    private UnifiedNativeAdView mUnifiedNativeAdView;
    private MediaView mMediaView;
    private AdIconView mAdIconView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }

        if (mAdLoader != null) {
            mAdLoader.loadAd(new AdRequest.Builder().build());
            return;
        }
        AdLoader.Builder builder = new AdLoader.Builder(activity.getApplicationContext(), mInstancesKey);
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                if (!isDestroyed) {
                    mUnifiedNativeAd = unifiedNativeAd;
                    mAdInfo.setType(1);
                    mAdInfo.setTitle(unifiedNativeAd.getHeadline());
                    mAdInfo.setDesc(unifiedNativeAd.getBody());
                    mAdInfo.setCallToActionText(unifiedNativeAd.getCallToAction());
                    onInsReady(mAdInfo);
                }
            }
        });
        //
        NativeAdOptions.Builder nativeAdOptionsBuilder = new NativeAdOptions.Builder();
        //single image
        nativeAdOptionsBuilder.setRequestMultipleImages(false);
        mAdLoader = builder.withNativeAdOptions(nativeAdOptionsBuilder.build()).withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                AdLog.getSingleton().LogE("Om-AdMob: AdMob Native ad failed to load, error code is : " + errorCode);
                if (!isDestroyed) {
                    onInsError("onAdFailedToLoad:" + errorCode);
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
        mAdLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        if (isDestroyed) {
            return;
        }

        RelativeLayout relativeLayout = new RelativeLayout(adView.getContext());
        if (mUnifiedNativeAd == null) {
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
        mUnifiedNativeAdView = new UnifiedNativeAdView(adView.getContext());
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
            com.google.android.gms.ads.formats.MediaView admobMediaView = new
                    com.google.android.gms.ads.formats.MediaView(adView.getContext());
            mMediaView.addView(admobMediaView);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            admobMediaView.setLayoutParams(layoutParams);
            mUnifiedNativeAdView.setMediaView(admobMediaView);
        }

        if (mAdIconView != null && mUnifiedNativeAd.getIcon() != null && mUnifiedNativeAd.getIcon().getDrawable() != null) {
            mAdIconView.removeAllViews();
            ImageView iconImageView = new ImageView(adView.getContext());
            mAdIconView.addView(iconImageView);
            iconImageView.setImageDrawable(mUnifiedNativeAd.getIcon().getDrawable());
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
            if (v == null || v instanceof UnifiedNativeAdView) {
                continue;
            }
            adView.removeView(v);
            relativeLayout.addView(v);
        }
        mUnifiedNativeAdView.setNativeAd(mUnifiedNativeAd);

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
        if (mUnifiedNativeAd != null) {
            mUnifiedNativeAd.destroy();
            mUnifiedNativeAd = null;
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
