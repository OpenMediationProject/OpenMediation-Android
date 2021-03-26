// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.applovin.BuildConfig;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AppLovinAdapter extends CustomAdsAdapter implements AppLovinAdVideoPlaybackListener
        , AppLovinAdDisplayListener, AppLovinAdClickListener {

    private final ConcurrentMap<String, AppLovinAd> mAppLovinIsAds;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    private final ConcurrentMap<String, AppLovinIncentivizedInterstitial> mRvAds;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;

    static final int AGE_RESTRICTION = 16;

    public AppLovinAdapter() {
        mAppLovinIsAds = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();

        mRvAds = new ConcurrentHashMap<>();
        mRvCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return AppLovinSdk.VERSION;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_8;
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (context != null) {
            AppLovinPrivacySettings.setHasUserConsent(consent, context);
        }
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        setAgeRestricted(context, age < AGE_RESTRICTION);
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        if (context != null) {
            AppLovinPrivacySettings.setIsAgeRestrictedUser(restricted, context);
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        if (context != null) {
            AppLovinPrivacySettings.setDoNotSell(value, context);
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity, new AppLovinSingleTon.InitCallback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                }

                @Override
                public void onFailed(String msg) {
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, msg));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    private synchronized void initSDK(Activity activity, AppLovinSingleTon.InitCallback callback) {
        AppLovinSingleTon.getInstance().init(activity, mAppKey, callback);
    }

    private AppLovinIncentivizedInterstitial getVideo(Activity activity, String adUnitId) {
        AppLovinIncentivizedInterstitial videoAd = mRvAds.get(adUnitId);
        if (videoAd == null) {
            AppLovinSdk sdk = AppLovinSingleTon.getInstance().getAppLovinSdk();
            if (sdk != null) {
                videoAd = AppLovinIncentivizedInterstitial.create(adUnitId, sdk);
                mRvAds.put(adUnitId, videoAd);
            }
        }
        return videoAd;
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            AppLovinIncentivizedInterstitial videoAd = getVideo(activity, adUnitId);
            if (videoAd == null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Ad LoadFailed"));
            } else {
                if (videoAd.isAdReadyToDisplay()) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadSuccess();
                    }
                } else {
                    videoAd.preload(new AppLovinAdLoadListener() {
                        @Override
                        public void adReceived(AppLovinAd appLovinAd) {
                            if (appLovinAd != null) {
                                if (callback != null) {
                                    callback.onRewardedVideoLoadSuccess();
                                }
                            }
                        }

                        @Override
                        public void failedToReceiveAd(int i) {
                            if (callback != null) {
                                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, i, getErrorString(i)));
                            }
                        }
                    });
                    mRvCallbacks.put(adUnitId, callback);
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        AppLovinIncentivizedInterstitial videoAd = getVideo(activity, adUnitId);
        if (videoAd == null) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "AppLovin video get fail when show"));
            }
        } else {
            if (!isRewardedVideoAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "AppLovin video not ready when show"));
                }
                return;
            }
            if (callback != null) {
                mRvCallbacks.put(adUnitId, callback);
            }
            videoAd.show(activity, null, this,
                    this, this);
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        AppLovinIncentivizedInterstitial videoAd = mRvAds.get(adUnitId);
        return videoAd != null && videoAd.isAdReadyToDisplay();
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        final String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity, new AppLovinSingleTon.InitCallback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                }

                @Override
                public void onFailed(String msg) {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, msg));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, final String adUnitId, Map<String, Object> extras, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (isInterstitialAdAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
                return;
            }
            AppLovinSdk sdk = AppLovinSingleTon.getInstance().getAppLovinSdk();
            if (sdk != null) {
                sdk.getAdService().loadNextAdForZoneId(adUnitId, new AppLovinAdLoadListener() {
                    @Override
                    public void adReceived(AppLovinAd appLovinAd) {
                        if (appLovinAd != null) {
                            mAppLovinIsAds.put(adUnitId, appLovinAd);
                            if (callback != null) {
                                callback.onInterstitialAdLoadSuccess();
                            }
                        }
                    }

                    @Override
                    public void failedToReceiveAd(int i) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, i, getErrorString(i)));
                        }
                    }
                });
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            AppLovinSdk sdk = AppLovinSingleTon.getInstance().getAppLovinSdk();
            if (sdk != null) {
                AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create(sdk, activity);
                if (interstitialAd != null) {
                    interstitialAd.setAdClickListener(this);
                    interstitialAd.setAdDisplayListener(this);
                    if (callback != null) {
                        mIsCallbacks.put(adUnitId, callback);
                    }
                    interstitialAd.showAndRender(mAppLovinIsAds.get(adUnitId));
                    mAppLovinIsAds.remove(adUnitId);
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "not ready"));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return mAppLovinIsAds.containsKey(adUnitId);
    }

    @Override
    public void adClicked(AppLovinAd appLovinAd) {
        AdLog.getSingleton().LogD("OM-AppLovin", "adClicked:" + appLovinAd);
        if (appLovinAd != null) {
            RewardedVideoCallback callback = mRvCallbacks.get(appLovinAd.getZoneId());
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            } else {
                InterstitialAdCallback interstitialAdCallback = mIsCallbacks.get(appLovinAd.getZoneId());
                if (interstitialAdCallback != null) {
                    interstitialAdCallback.onInterstitialAdClick();
                }
            }
        }
    }

    @Override
    public void adDisplayed(AppLovinAd appLovinAd) {
        AdLog.getSingleton().LogD("OM-AppLovin", "adDisplayed:" + appLovinAd);
        if (appLovinAd != null) {
            RewardedVideoCallback callback = mRvCallbacks.get(appLovinAd.getZoneId());
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
            } else {
                InterstitialAdCallback interstitialAdCallback = mIsCallbacks.get(appLovinAd.getZoneId());
                if (interstitialAdCallback != null) {
                    interstitialAdCallback.onInterstitialAdShowSuccess();
                }
            }
        }
    }

    @Override
    public void adHidden(AppLovinAd appLovinAd) {
        AdLog.getSingleton().LogD("OM-AppLovin", "adHidden:" + appLovinAd);
        if (appLovinAd != null) {
            RewardedVideoCallback callback = mRvCallbacks.get(appLovinAd.getZoneId());
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            } else {
                InterstitialAdCallback interstitialAdCallback = mIsCallbacks.get(appLovinAd.getZoneId());
                if (interstitialAdCallback != null) {
                    interstitialAdCallback.onInterstitialAdClosed();
                }
            }
        }
    }

    @Override
    public void videoPlaybackBegan(AppLovinAd appLovinAd) {
        AdLog.getSingleton().LogD("OM-AppLovin", "videoPlaybackBegan:" + appLovinAd);
        if (appLovinAd != null) {
            RewardedVideoCallback callback = mRvCallbacks.get(appLovinAd.getZoneId());
            if (callback != null) {
                callback.onRewardedVideoAdStarted();
            }
        }
    }

    @Override
    public void videoPlaybackEnded(AppLovinAd appLovinAd, double percentViewed, boolean fullyWatched) {
        AdLog.getSingleton().LogD("OM-AppLovin", "videoPlaybackEnded:" + appLovinAd
                + ", percentViewed:" + percentViewed + ", fullyWatched:" + fullyWatched);
        if (appLovinAd != null) {
            RewardedVideoCallback callback = mRvCallbacks.get(appLovinAd.getZoneId());
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                if (fullyWatched) {
                    callback.onRewardedVideoAdRewarded();
                }
            }
        }
    }

    static String getErrorString(int errorCode) {
        switch (errorCode) {
            case AppLovinErrorCodes.FETCH_AD_TIMEOUT:
                return "Ad fetch timeout";
            case AppLovinErrorCodes.INCENTIVIZED_NO_AD_PRELOADED:
                return "No ad pre-loaded";
            case AppLovinErrorCodes.INCENTIVIZED_SERVER_TIMEOUT:
                return "Server timeout";
            case AppLovinErrorCodes.INCENTIVIZED_UNKNOWN_SERVER_ERROR:
                return "Unknown server error";
            case AppLovinErrorCodes.INCENTIVIZED_USER_CLOSED_VIDEO:
                return "User closed video before reward";
            case AppLovinErrorCodes.NO_FILL:
                return "No fill";
            case AppLovinErrorCodes.NO_NETWORK:
                return "No network available";
            case AppLovinErrorCodes.UNABLE_TO_RENDER_AD:
                return "Unable to render ad";
            case AppLovinErrorCodes.UNSPECIFIED_ERROR:
                return "Unspecified error";
            default:
                return "Unknown error";
        }
    }

}
