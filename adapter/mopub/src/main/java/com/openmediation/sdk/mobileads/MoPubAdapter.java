// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedAdListener;
import com.mopub.mobileads.MoPubRewardedAdManager;
import com.mopub.mobileads.MoPubRewardedAds;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.ViewBinder;
import com.mopub.volley.Response;
import com.mopub.volley.VolleyError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.AdLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MoPubAdapter extends CustomAdsAdapter implements MoPubRewardedAdListener
        , MoPubInterstitial.InterstitialAdListener, MoPubView.BannerAdListener {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static final String TAG = "OM-MoPub";
    private static final String TP_PARAM = "imext";

    private volatile InitState mInitState = InitState.NOT_INIT;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallback;
    private final ConcurrentMap<String, MoPubInterstitial> mInterstitialAds;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsCallback;
    private final ConcurrentMap<String, BannerAdCallback> mBnCallback;
    private final ConcurrentMap<String, MoPubView> mBannerAds;
    private final ConcurrentMap<String, NativeAdCallback> mNaCallback;

    private static final int VID = generateViewId();
    private static final int PID = generateViewId();
    private static final int CID = generateViewId();

    private final MoPubRewardedAdManager.RequestParameters mRequestParameters;
    private String mShowingId;


    public MoPubAdapter() {
        mRvCallback = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mIsCallback = new ConcurrentHashMap<>();
        mBnCallback = new ConcurrentHashMap<>();
        mBannerAds = new ConcurrentHashMap<>();
        mNaCallback = new ConcurrentHashMap<>();
        // adjustment requested by MoPub to be able to report on this incremental supply
        mRequestParameters = new MoPubRewardedAdManager.RequestParameters(TP_PARAM);
    }

    @Override
    public String getMediationVersion() {
        return MoPub.SDK_VERSION;
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.mopub.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_9;
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        MoPub.onCreate(activity);
        MoPub.onStart(activity);
        MoPub.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        MoPub.onPause(activity);
        MoPub.onStop(activity);
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (mInitState == InitState.INIT_SUCCESS) {
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
    }

    private void initSDK(final Activity activity, String pid) {
        mInitState = InitState.INIT_PENDING;
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(pid).build();
        MoPub.initializeSdk(activity, sdkConfiguration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                MoPubRewardedAds.setRewardedAdListener(MoPubAdapter.this);
                mInitState = InitState.INIT_SUCCESS;
                if (mUserConsent != null) {
                    setGDPRConsent(activity, mUserConsent);
                }
                for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallback.entrySet()) {
                    if (videoCallbackEntry != null) {
                        videoCallbackEntry.getValue().onRewardedVideoInitSuccess();
                    }
                }

                for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                    if (interstitialAdCallbackEntry != null) {
                        interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
                    }
                }

                for (BannerAdCallback callback : mBnCallback.values()) {
                    if (callback != null) {
                        callback.onBannerAdInitSuccess();
                    }
                }

                for (NativeAdCallback callback : mNaCallback.values()) {
                    if (callback != null) {
                        callback.onNativeAdInitSuccess();
                    }
                }
            }
        });
    }

    @Override
    public void initRewardedVideo(final Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String pid = "";
        if (dataMap.get("pid") != null) {
            pid = (String) dataMap.get("pid");
        }
        String error = check(activity, pid);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    mRvCallback.put(pid, callback);
                    initSDK(activity, pid);
                    break;
                case INIT_PENDING:
                    mRvCallback.put(pid, callback);
                    break;
                case INIT_SUCCESS:
                    callback.onRewardedVideoInitSuccess();
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
    }

    @Override
    public void loadRewardedVideo(final Activity activity, final String adUnitId, Map<String, Object> extras, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        try {
            String error = check(adUnitId);
            if (TextUtils.isEmpty(error)) {
                if (callback != null) {
                    mRvCallback.put(adUnitId, callback);
                }
                if (MoPubRewardedAds.hasRewardedAd(adUnitId)) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadSuccess();
                    }
                } else {
                    MoPubRewardedAds.setRewardedAdListener(MoPubAdapter.this);
                    MoPubRewardedAds.loadRewardedAd(adUnitId, mRequestParameters);
                }
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        try {
            String error = check(adUnitId);
            if (!TextUtils.isEmpty(error)) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                }
                return;
            }
            if (isRewardedVideoAvailable(adUnitId)) {
                if (callback != null) {
                    mRvCallback.put(adUnitId, callback);
                }
                MoPubRewardedAds.showRewardedAd(adUnitId);
            } else {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MoPub ad not ready to show"));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return MoPubRewardedAds.hasRewardedAd(adUnitId);
    }

    @Override
    public void onRewardedAdLoadSuccess(String adUnitId) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    @Override
    public void onRewardedAdLoadFailure(String adUnitId, MoPubErrorCode errorCode) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, errorCode.name()));
        }
    }

    @Override
    public void onRewardedAdStarted(String adUnitId) {
        mShowingId = adUnitId;
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
            callback.onRewardedVideoAdStarted();
        }
    }

    @Override
    public void onRewardedAdShowError(String adUnitId, MoPubErrorCode errorCode) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, errorCode.name()));
        }
    }

    @Override
    public void onRewardedAdClicked(String adUnitId) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    @Override
    public void onRewardedAdClosed(String adUnitId) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onRewardedAdCompleted(Set<String> adUnitIds, MoPubReward reward) {
        if (!TextUtils.isEmpty(mShowingId)) {
            RewardedVideoCallback callback = mRvCallback.get(mShowingId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdRewarded();
            }
        }
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String pid = "";
        if (dataMap.get("pid") != null) {
            pid = (String) dataMap.get("pid");
        }
        String error = check(activity, pid);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    if (callback != null) {
                        mIsCallback.put(pid, callback);
                    }
                    initSDK(activity, pid);
                    break;
                case INIT_PENDING:
                    if (callback != null) {
                        mIsCallback.put(pid, callback);
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
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        try {
            String error = check(activity, adUnitId);
            if (TextUtils.isEmpty(error)) {
                MoPubInterstitial interstitial = getInterstitialAd(activity, adUnitId);
                if (callback != null) {
                    mIsCallback.put(adUnitId, callback);
                }
                if (interstitial.isReady()) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadSuccess();
                    }
                } else {
                    interstitial.load();
                }
            } else {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            MoPubInterstitial interstitial = mInterstitialAds.get(adUnitId);
            if (callback != null) {
                mIsCallback.put(adUnitId, callback);
            }
            mShowingId = adUnitId;
            interstitial.show();
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "MoPub interstitial is not ready"));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        MoPubInterstitial interstitial = mInterstitialAds.get(adUnitId);
        return interstitial != null && interstitial.isReady();
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String pid = "";
        if (extras.get("pid") != null) {
            pid = (String) extras.get("pid");
        }
        String error = check(activity, pid);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    if (callback != null) {
                        mBnCallback.put(pid, callback);
                    }
                    initSDK(activity, pid);
                    break;
                case INIT_PENDING:
                    if (callback != null) {
                        mBnCallback.put(pid, callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onBannerAdInitSuccess();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        try {
            String error = check(adUnitId);
            if (TextUtils.isEmpty(error)) {
                MoPubView adView = new MoPubView(MediationUtil.getContext());
                MoPubView.MoPubAdSize adSize = getAdSize(extras, adView.getAdSize());
                adView.setAdSize(adSize);
                adView.setAdUnitId(adUnitId);
                adView.setBannerAdListener(this);
                mBannerAds.put(adUnitId, adView);
                if (callback != null) {
                    mBnCallback.put(adUnitId, callback);
                }
                adView.loadAd();
            } else {
                if (callback != null) {
                    callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return mBannerAds.containsKey(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        if (!mBannerAds.containsKey(adUnitId) || mBannerAds.get(adUnitId) == null) {
            return;
        }
        mBannerAds.get(adUnitId).destroy();
        mBannerAds.remove(adUnitId);
    }

    @Override
    public void initNativeAd(Activity activity, Map<String, Object> extras, NativeAdCallback callback) {
        super.initNativeAd(activity, extras, callback);
        String pid = "";
        if (extras.get("pid") != null) {
            pid = (String) extras.get("pid");
        }
        String error = check(activity, pid);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    if (callback != null) {
                        mNaCallback.put(pid, callback);
                    }
                    initSDK(activity, pid);
                    break;
                case INIT_PENDING:
                    if (callback != null) {
                        mNaCallback.put(pid, callback);
                    }
                    break;
                case INIT_SUCCESS:
                    if (callback != null) {
                        callback.onNativeAdInitSuccess();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadNativeAd(Activity activity, String adUnitId, Map<String, Object> extras, NativeAdCallback callback) {
        super.loadNativeAd(activity, adUnitId, extras, callback);
        try {
            String error = check(adUnitId);
            if (TextUtils.isEmpty(error)) {
                ViewBinder viewBinder = new ViewBinder.Builder(VID)
                        .privacyInformationIconImageId(PID)
                        .callToActionId(CID)
                        .build();
                MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer = new MoPubStaticNativeAdRenderer(viewBinder);
                Context context = MediationUtil.getContext();
                MoPubNative moPubNative = new MoPubNative(context, adUnitId,
                        new MpNaLoadListener(context, adUnitId, callback));
                moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
                moPubNative.makeRequest();
            } else {
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, error));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void registerNativeAdView(String adUnitId, final NativeAdView adView, AdnAdInfo adInfo, NativeAdCallback callback) {
        super.registerNativeAdView(adUnitId, adView, adInfo, callback);
        try {
            if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof MoPubNativeAdsConfig)) {
                AdLog.getSingleton().LogE("MoPubAdapter NativeAd not ready");
                return;
            }

            List<View> views = new ArrayList<>();
            if (adView.getMediaView() != null) {
                views.add(adView.getMediaView());
            }

            if (adView.getAdIconView() != null) {
                views.add(adView.getAdIconView());
            }

            if (adView.getTitleView() != null) {
                views.add(adView.getTitleView());
            }

            if (adView.getDescView() != null) {
                views.add(adView.getDescView());
            }

            if (adView.getCallToActionView() != null) {
                views.add(adView.getCallToActionView());
            }

            for (View view : views) {
                if (view != null) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adView.findViewById(CID).callOnClick();
                        }
                    });
                }
            }

            MoPubNativeAdsConfig config = (MoPubNativeAdsConfig) adInfo.getAdnNativeAd();
            if (config.getContent() != null && adView.getMediaView() != null) {
                adView.getMediaView().removeAllViews();
                ImageView imageView = new ImageView(adView.getContext());
                adView.getMediaView().addView(imageView);
                imageView.setImageBitmap(config.getContent());
                imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                imageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                adView.getMediaView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adView.findViewById(CID).callOnClick();
                    }
                });
            }

            if (config.getIcon() != null && adView.getAdIconView() != null) {
                adView.getAdIconView().removeAllViews();
                ImageView iconImageView = new ImageView(adView.getContext());
                adView.getAdIconView().addView(iconImageView);
                iconImageView.setImageBitmap(config.getIcon());
                iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

                adView.getAdIconView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adView.findViewById(CID).callOnClick();
                    }
                });
            }
            addAndShowAdLogo(config.getNativeAd(), adView);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void destroyNativeAd(String adUnitId, AdnAdInfo adInfo) {
        super.destroyNativeAd(adUnitId, adInfo);
        if (adInfo == null || !(adInfo.getAdnNativeAd() instanceof MoPubNativeAdsConfig)) {
            AdLog.getSingleton().LogE("MoPubAdapter destroyNativeAd failed: AdnAdInfo is null");
            return;
        }
        MoPubNativeAdsConfig config = (MoPubNativeAdsConfig) adInfo.getAdnNativeAd();
        if (config.getNativeAd() != null) {
            config.getNativeAd().destroy();
        }
    }

    private MoPubInterstitial getInterstitialAd(Activity activity, String adUnitId) {
        MoPubInterstitial interstitialAd = mInterstitialAds.get(adUnitId);
        if (interstitialAd == null) {
            interstitialAd = new MoPubInterstitial(activity, adUnitId);
            interstitialAd.setInterstitialAdListener(this);
            mInterstitialAds.put(adUnitId, interstitialAd);
        }
        return interstitialAd;
    }

    private MoPubView.MoPubAdSize getAdSize(Map<String, Object> config, MoPubView.MoPubAdSize defaultSize) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_BANNER:
                return MoPubView.MoPubAdSize.HEIGHT_50;
            case MediationUtil.DESC_LEADERBOARD:
                return MoPubView.MoPubAdSize.HEIGHT_90;
            case MediationUtil.DESC_RECTANGLE:
                return MoPubView.MoPubAdSize.HEIGHT_250;
            default:
                return defaultSize;
        }
    }

    private void addAndShowAdLogo(NativeAd nativeAd, RelativeLayout parent) {
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

            int size = MediationUtil.dip2px(parent.getContext(), 15);
            privacy_img.getLayoutParams().width = size;
            privacy_img.getLayoutParams().height = size;

            ((RelativeLayout.LayoutParams) privacy_img.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ((RelativeLayout.LayoutParams) privacy_img.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);

            nativeAd.prepare(parent);
            nativeAd.renderAdView(parent);
        } catch (Throwable e) {
            AdLog.getSingleton().LogE(TAG, "addAndShowAdLogo error : " + e.getMessage());
        }
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
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        if (interstitial == null || TextUtils.isEmpty(interstitial.getAdUnitId())) {
            return;
        }
        String adUnitId = interstitial.getAdUnitId();
        InterstitialAdCallback callback = mIsCallback.get(adUnitId);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        if (interstitial == null || TextUtils.isEmpty(interstitial.getAdUnitId())) {
            return;
        }
        String adUnitId = interstitial.getAdUnitId();
        InterstitialAdCallback callback = mIsCallback.get(adUnitId);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, errorCode.name()));
        }
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        if (interstitial == null || TextUtils.isEmpty(interstitial.getAdUnitId())) {
            return;
        }
        String adUnitId = interstitial.getAdUnitId();
        InterstitialAdCallback callback = mIsCallback.get(adUnitId);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        if (interstitial == null || TextUtils.isEmpty(interstitial.getAdUnitId())) {
            return;
        }
        String adUnitId = interstitial.getAdUnitId();
        InterstitialAdCallback callback = mIsCallback.get(adUnitId);
        if (callback != null) {
            callback.onInterstitialAdClicked();
        }
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        if (interstitial == null || TextUtils.isEmpty(interstitial.getAdUnitId())) {
            return;
        }
        String adUnitId = interstitial.getAdUnitId();
        InterstitialAdCallback callback = mIsCallback.get(adUnitId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
        if (interstitial != null) {
            mIsCallback.remove(adUnitId);
            MoPubInterstitial mi = mInterstitialAds.get(mShowingId);
            if (mi == interstitial) {
                mi.destroy();
                mInterstitialAds.remove(mShowingId);
            }
        }
    }

    @Override
    public void onBannerLoaded(MoPubView moPubView) {
        String adUnitId = moPubView.getAdViewController().getAdUnitId();
        BannerAdCallback callback = mBnCallback.get(adUnitId);
        if (callback != null) {
            callback.onBannerAdLoadSuccess(moPubView);
        }
    }

    @Override
    public void onBannerFailed(MoPubView moPubView, MoPubErrorCode moPubErrorCode) {
        String adUnitId = moPubView.getAdViewController().getAdUnitId();
        BannerAdCallback callback = mBnCallback.get(adUnitId);
        if (callback != null) {
            callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, moPubErrorCode.name()));
        }
    }

    @Override
    public void onBannerClicked(MoPubView moPubView) {
        String adUnitId = moPubView.getAdViewController().getAdUnitId();
        BannerAdCallback callback = mBnCallback.get(adUnitId);
        if (callback != null) {
            callback.onBannerAdAdClicked();
        }
    }

    @Override
    public void onBannerExpanded(MoPubView moPubView) {

    }

    @Override
    public void onBannerCollapsed(MoPubView moPubView) {

    }

    private class MpNaLoadListener implements MoPubNative.MoPubNativeNetworkListener {

        private String adUnitId;
        private Context context;
        private NativeAdCallback callback;

        private MpNaLoadListener(Context context, String adUnitId, NativeAdCallback callback) {
            this.context = context;
            this.adUnitId = adUnitId;
            this.callback = callback;
        }

        @Override
        public void onNativeLoad(NativeAd nativeAd) {
            try {
                nativeAd.setMoPubNativeEventListener(new MpNaImpressionListener(callback));
                final MoPubNativeAdsConfig config = new MoPubNativeAdsConfig();
                config.setNativeAd(nativeAd);
                final StaticNativeAd staticNativeAd = (StaticNativeAd) nativeAd.getBaseNativeAd();
                MoPubUtil.Request(context, staticNativeAd.getIconImageUrl(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        config.setIcon(bitmap);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
                MoPubUtil.Request(context, staticNativeAd.getMainImageUrl(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        config.setContent(bitmap);
                        if (callback != null) {
                            final AdnAdInfo adInfo = new AdnAdInfo();
                            adInfo.setAdnNativeAd(config);
                            adInfo.setDesc(staticNativeAd.getText());
                            adInfo.setType(getAdNetworkId());
                            adInfo.setCallToActionText(staticNativeAd.getCallToAction());
                            adInfo.setTitle(staticNativeAd.getTitle());
                            callback.onNativeAdLoadSuccess(adInfo);
                        }
                        AdLog.getSingleton().LogD(TAG, "MoPub Native ad load success ");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (callback != null) {
                            callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_NATIVE, "MoPubAdapter", volleyError.getMessage()));
                        }
                    }
                });
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "MoPubAdapter", e.getMessage()));
                }
            }
        }

        @Override
        public void onNativeFail(NativeErrorCode nativeErrorCode) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "MoPubAdapter", nativeErrorCode.name()));
            }
        }
    }

    private static class MpNaImpressionListener implements NativeAd.MoPubNativeEventListener {

        private NativeAdCallback callback;

        private MpNaImpressionListener(NativeAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onImpression(View view) {
            AdLog.getSingleton().LogD("MoPubAdapter NativeAd onImpression");
            if (callback != null) {
                callback.onNativeAdImpression();
            }
        }

        @Override
        public void onClick(View view) {
            if (callback != null) {
                callback.onNativeAdAdClicked();
            }
        }
    }

    /**
     * MoPub sdk init state
     */
    private enum InitState {
        NOT_INIT,
        INIT_PENDING,
        INIT_SUCCESS
    }
}
