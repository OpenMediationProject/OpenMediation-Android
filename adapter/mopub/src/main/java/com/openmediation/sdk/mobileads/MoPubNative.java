// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.ViewBinder;
import com.mopub.volley.Response;
import com.mopub.volley.VolleyError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomNativeEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MoPubNative extends CustomNativeEvent implements com.mopub.nativeads.MoPubNative.MoPubNativeNetworkListener,
        NativeAd.MoPubNativeEventListener {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private static final int VID = generateViewId();
    private static final int PID = generateViewId();
    private static final int CID = generateViewId();
    private WeakReference<Activity> mActRef;
    private NativeAd mNativeAd;
    private MediaView mediaView;
    private AdIconView adIconView;

    private Bitmap mIcon;
    private Bitmap mContent;

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (!MoPub.isSdkInitialized()) {
            return;
        }
        PersonalInfoManager manager = MoPub.getPersonalInformationManager();
        if (manager == null) {
            return;
        }
        if (consent) {
            manager.grantConsent();
        } else {
            manager.revokeConsent();
        }
    }

    @Override
    public void registerNativeView(final NativeAdView nativeAdView) {
        if (isDestroyed) {
            return;
        }
        List<View> views = new ArrayList<>();
        if (nativeAdView.getMediaView() != null) {
            mediaView = nativeAdView.getMediaView();
            views.add(mediaView);
        }

        if (nativeAdView.getAdIconView() != null) {
            adIconView = nativeAdView.getAdIconView();
            views.add(adIconView);
        }

        if (nativeAdView.getTitleView() != null) {
            views.add(nativeAdView.getTitleView());
        }

        if (nativeAdView.getDescView() != null) {
            views.add(nativeAdView.getDescView());
        }

        if (nativeAdView.getCallToActionView() != null) {
            views.add(nativeAdView.getCallToActionView());
        }

        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nativeAdView.findViewById(CID).callOnClick();
                    }
                });
            }
        }

        if (mContent != null && mediaView != null) {
            mediaView.removeAllViews();
            ImageView imageView = new ImageView(nativeAdView.getContext());
            mediaView.addView(imageView);
            imageView.setImageBitmap(mContent);
            imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            imageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            mediaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nativeAdView.findViewById(CID).callOnClick();
                }
            });
        }

        if (mIcon != null && adIconView != null) {
            adIconView.removeAllViews();
            ImageView iconImageView = new ImageView(nativeAdView.getContext());
            adIconView.addView(iconImageView);
            iconImageView.setImageBitmap(mIcon);
            iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

            adIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nativeAdView.findViewById(CID).callOnClick();
                }
            });
        }

        addAndShowAdLogo(nativeAdView);
    }

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (mActRef == null || mActRef.get() == null) {
            mActRef = new WeakReference<>(activity);
        }

        if (!MoPub.isSdkInitialized()) {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(mInstancesKey).build();
            MoPub.initializeSdk(activity, sdkConfiguration, new SdkInitializationListener() {
                @Override
                public void onInitializationFinished() {
                    if (mUserConsent != null) {
                        setGDPRConsent(activity, mUserConsent);
                    }
                    loadNativeAd(mActRef.get());
                }
            });
        } else {
            loadNativeAd(mActRef.get());
        }
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_9;
    }

    @Override
    public void destroy(Activity activity) {
        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
        if (mActRef != null) {
            mActRef.clear();
        }
        isDestroyed = true;
    }

    private static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1;
            }
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    @Override
    public void onNativeLoad(NativeAd nativeAd) {
        if (mActRef.get() == null || isDestroyed) {
            return;
        }
        mNativeAd = nativeAd;
        mNativeAd.setMoPubNativeEventListener(this);
        StaticNativeAd staticNativeAd = (StaticNativeAd) nativeAd.getBaseNativeAd();
        mAdInfo.setDesc(staticNativeAd.getText());
        mAdInfo.setType(getMediation());
        mAdInfo.setCallToActionText(staticNativeAd.getCallToAction());
        mAdInfo.setTitle(staticNativeAd.getTitle());
        MoPubUtil.Request(mActRef.get(), staticNativeAd.getIconImageUrl(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                mIcon = bitmap;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        MoPubUtil.Request(mActRef.get(), staticNativeAd.getMainImageUrl(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                mContent = bitmap;
                onInsReady(mAdInfo);
                AdLog.getSingleton().LogD("OM-Mopub", "Mopub Native ad load success ");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (isDestroyed) {
                    return;
                }
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, volleyError.getMessage()));
            }
        });
    }

    @Override
    public void onNativeFail(NativeErrorCode errorCode) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, errorCode.name()));
    }

    @Override
    public void onImpression(View view) {

    }

    @Override
    public void onClick(View view) {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    private void loadNativeAd(Activity activity) {
        ViewBinder viewBinder = new ViewBinder.Builder(VID)
                .privacyInformationIconImageId(PID)
                .callToActionId(CID)
                .build();
        MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer = new MoPubStaticNativeAdRenderer(viewBinder);

        com.mopub.nativeads.MoPubNative moPubNative = new com.mopub.nativeads.MoPubNative(activity, mInstancesKey, this);
        moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
        moPubNative.makeRequest();
    }

    private void addAndShowAdLogo(RelativeLayout parent) {
        //
        try {
            parent.setId(VID);
            Button actView = new Button(parent.getContext());
            actView.setId(CID);
            actView.setVisibility(View.GONE);
            parent.addView(actView);

            ImageView privacy_img = new ImageView(parent.getContext());
            privacy_img.setId(PID);
            parent.addView(privacy_img);

            int size = MoPubUtil.dip2px(parent.getContext(), 15);
            privacy_img.getLayoutParams().width = size;
            privacy_img.getLayoutParams().height = size;

            ((RelativeLayout.LayoutParams) privacy_img.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ((RelativeLayout.LayoutParams) privacy_img.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);

            mNativeAd.prepare(parent);
            mNativeAd.renderAdView(parent);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE("Om-Mopub: addAndShowAdLogo error : " + e.getMessage());
        }
    }
}
