// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.ImageUtils;
import com.crosspromotion.sdk.utils.ResDownloader;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomNativeEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.WorkExecutor;

import net.pubnative.lite.sdk.HyBid;
import net.pubnative.lite.sdk.models.NativeAd;

import java.io.File;
import java.util.Map;

public class PubNativeNative extends CustomNativeEvent implements NativeAd.Listener {
    private NativeAd mNativeAd;

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        HyBid.setCoppaEnabled(restricted);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        HyBid.setAge(String.valueOf(age));
    }

    @Override
    public void setUserGender(Context context, String gender) {
        super.setUserGender(context, gender);
        HyBid.setGender(gender);
    }

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        mNativeAd = PubNativeSingleTon.getInstance().getNativeAd(mInstancesKey);
        if (mNativeAd == null) {
            String error = PubNativeSingleTon.getInstance().getError(mInstancesKey);
            if (TextUtils.isEmpty(error)) {
                error = "No Fill";
            }
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            return;
        }
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                downloadRes(mNativeAd);
            }
        });
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        try {
            if (isDestroyed || mNativeAd == null) {
                return;
            }

            if (!TextUtils.isEmpty(mNativeAd.getBannerUrl()) && adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();
                mediaView.removeAllViews();

                ImageView adnMediaView = new ImageView(adView.getContext());
                mediaView.addView(adnMediaView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                adnMediaView.setLayoutParams(layoutParams);
                Bitmap content = ImageUtils.getBitmap(IOUtil.getFileInputStream(Cache.getCacheFile(adView.getContext(),
                        mNativeAd.getBannerUrl(), null)));
                adnMediaView.setImageBitmap(content);
            }

            if (!TextUtils.isEmpty(mNativeAd.getIconUrl()) && adView.getAdIconView() != null) {
                AdIconView iconView = adView.getAdIconView();
                iconView.removeAllViews();
                ImageView adnIconView = new ImageView(adView.getContext());
                iconView.addView(adnIconView);
                adnIconView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                adnIconView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                Bitmap content = ImageUtils.getBitmap(IOUtil.getFileInputStream(Cache.getCacheFile(adView.getContext(),
                        mNativeAd.getIconUrl(), null)));
                adnIconView.setImageBitmap(content);
            }
            if (adView.getCallToActionView() != null) {
                mNativeAd.startTracking(adView.getCallToActionView(), this);
            } else {
                mNativeAd.startTracking(adView, this);
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_23;
    }

    @Override
    public void destroy(Activity activity) {
        if (mNativeAd != null) {
            mNativeAd.stopTracking();
            mNativeAd = null;
        }
        isDestroyed = true;
    }


    @Override
    public void onAdImpression(NativeAd nativeAd, View view) {

    }

    @Override
    public void onAdClick(NativeAd nativeAd, View view) {
        if (!isDestroyed) {
            onInsClicked();
        }
    }

    private void downloadRes(NativeAd ad) {
        try {
            if (!TextUtils.isEmpty(ad.getBannerUrl())) {
                File file = ResDownloader.downloadFile(ad.getBannerUrl());
                if (file == null || !file.exists()) {
                    onInsError(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "NativeAd Load Failed"));
                    return;
                }
                AdLog.getSingleton().LogD("PubNativeNative", "Content File = " + file);
            }
            if (!TextUtils.isEmpty(ad.getIconUrl())) {
                File file = ResDownloader.downloadFile(ad.getIconUrl());
                if (file == null || !file.exists()) {
                    onInsError(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "NativeAd Load Failed"));
                    return;
                }
                AdLog.getSingleton().LogD("PubNativeNative", "Icon File = " + file);
            }
            AdInfo adInfo = new AdInfo();
            adInfo.setDesc(ad.getDescription());
            adInfo.setType(getMediation());
            adInfo.setTitle(ad.getTitle());
            adInfo.setCallToActionText(ad.getCallToActionText());
            adInfo.setStarRating(ad.getRating());
            onInsReady(adInfo);
        } catch (Exception e) {
            onInsError(AdapterErrorBuilder.buildLoadCheckError(
                    AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "NativeAd Load Failed"));
        }
    }

}
