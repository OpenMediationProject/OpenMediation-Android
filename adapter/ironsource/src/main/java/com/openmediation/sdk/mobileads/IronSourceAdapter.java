package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.ironsource.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class IronSourceAdapter extends CustomAdsAdapter {

    private static AtomicBoolean mDidInitInterstitial = new AtomicBoolean(false);

    private final static List<IronSource.AD_UNIT> mIsAdUnitsToInit =
            new ArrayList<>(Collections.singletonList(IronSource.AD_UNIT.INTERSTITIAL));

    private static AtomicBoolean mDidInitRewardedVideo = new AtomicBoolean(false);

    private final static List<IronSource.AD_UNIT> mRvAdUnitsToInit =
            new ArrayList<>(Collections.singletonList(IronSource.AD_UNIT.REWARDED_VIDEO));

    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    enum INSTANCE_STATE {
        START, //Initial state when instance wasn't loaded yet
        CAN_LOAD, //If load is called on an instance with this state, pass it forward to IronSource SDK
        LOCKED, //if load is called on an instance with this state, report load fail
    }

    public IronSourceAdapter() {
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return IronSourceUtils.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_15;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        try {
            IronSource.setConsent(consent);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        try {
            String sell = value ? "true" : "false";
            IronSource.setMetaData("do_not_sell", sell);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        try {
            IronSource.onResume(activity);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onPause(Activity activity) {
        try {
            IronSource.onPause(activity);
        } catch (Throwable ignored) {
        }
        super.onPause(activity);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        try {
            if (!mDidInitRewardedVideo.getAndSet(true)) {
                IronSourceManager.getInstance().initIronSourceSDK(activity, mAppKey, mRvAdUnitsToInit);
            }
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (isRewardedVideoAvailable(adUnitId) && callback != null) {
                callback.onRewardedVideoLoadSuccess();
                return;
            }
            try {
                if (callback != null) {
                    mRvCallbacks.put(adUnitId, callback);
                }
                IronSourceManager.getInstance().loadRewardedVideo(activity, adUnitId, new WeakReference<>(IronSourceAdapter.this));
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String checkError = check(adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            try {
                if (callback != null) {
                    mRvCallbacks.put(adUnitId, callback);
                }
                IronSourceManager.getInstance().showRewardedVideo(adUnitId);
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Unknown Error, " + e.getMessage()));
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return IronSourceManager.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        try {
            if (!mDidInitInterstitial.getAndSet(true)) {
                IronSourceManager.getInstance().initIronSourceSDK(activity, mAppKey, mIsAdUnitsToInit);
            }
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (isInterstitialAdAvailable(adUnitId) && callback != null) {
                callback.onInterstitialAdLoadSuccess();
                return;
            }
            try {
                if (callback != null) {
                    mIsCallbacks.put(adUnitId, callback);
                }
                IronSourceManager.getInstance().loadInterstitial(activity, adUnitId, new WeakReference<>(IronSourceAdapter.this));
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String checkError = check(adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            try {
                if (callback != null) {
                    mIsCallbacks.put(adUnitId, callback);
                }
                IronSourceManager.getInstance().showInterstitial(adUnitId);
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return IronSourceManager.getInstance().isInterstitialReady(adUnitId);
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        IronSourceBannerManager.getInstance().initAd(activity, extras, callback);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        try {
            IronSourceBannerManager.getInstance().loadAd(activity, adUnitId, extras, callback);
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return IronSourceBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        try {
            IronSourceBannerManager.getInstance().destroyAd(adUnitId);
        } catch (Throwable ignored) {
        }
    }

    //region ISDemandOnlyInterstitialListener implementation.
    void onInterstitialAdReady(String instanceId) {
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    void onInterstitialAdLoadFailed(String instanceId, IronSourceError error) {
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getErrorCode(), error.getErrorMessage()));
        }
    }

    void onInterstitialAdOpened(String instanceId) {
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    void onInterstitialAdClosed(String instanceId) {
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    void onInterstitialAdShowFailed(String instanceId, IronSourceError error) {
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getErrorCode(), error.getErrorMessage()));
        }
    }

    void onInterstitialAdClicked(String instanceId) {
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdClicked();
        }
    }

    /**
     * IronSource callbacks for AdMob Mediation.
     */

    void onRewardedVideoAdLoadSuccess(String instanceId) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    void onRewardedVideoAdLoadFailed(String instanceId, IronSourceError error) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getErrorCode(), error.getErrorMessage()));
        }
    }

    void onRewardedVideoAdOpened(final String instanceId) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
        }
    }

    void onRewardedVideoAdStarted(String instanceId) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdStarted();
        }
    }

    void onRewardedVideoAdEnded(String instanceId) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdEnded();
        }
    }

    void onRewardedVideoAdClosed(String instanceId) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    void onRewardedVideoAdRewarded(String instanceId) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdRewarded();
        }
    }

    void onRewardedVideoAdShowFailed(final String instanceId, IronSourceError error) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getErrorCode(), error.getErrorMessage()));
        }
    }

    void onRewardedVideoAdClicked(String instanceId) {
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }
}
