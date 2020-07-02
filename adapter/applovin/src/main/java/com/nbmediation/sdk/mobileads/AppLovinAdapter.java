// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

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
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.applovin.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AppLovinAdapter extends CustomAdsAdapter implements AppLovinAdVideoPlaybackListener
        , AppLovinAdDisplayListener, AppLovinAdClickListener {

    private boolean mDidInited = false;
    private AppLovinSdk mAppLovinSDk;

    private ConcurrentMap<String, AppLovinInterstitialAdDialog> mIsAds;
    private ConcurrentMap<String, AppLovinAd> mAppLovinIsAds;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    private ConcurrentMap<String, AppLovinIncentivizedInterstitial> mRvAds;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;

    public AppLovinAdapter() {
        mIsAds = new ConcurrentHashMap<>();
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
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        AdLog.getSingleton().LogD("AppLovinAdapter", "initsdk:" + mAppKey);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity);
            if (mDidInited) {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
    }

    private synchronized void initSDK(Context activity) {
        if (!mDidInited) {
            AdLog.getSingleton().LogD("AppLovinAdapter", "initsdk:" + mAppKey);
            AppLovinSdk lovinSdk = AppLovinSdk.getInstance(mAppKey
                    , new AppLovinSdkSettings(activity.getApplicationContext()),
                    activity.getApplicationContext());
            if (lovinSdk == null) {
                return;
            }
            mAppLovinSDk = lovinSdk;
            lovinSdk.initializeSdk();
            mDidInited = true;
        }
    }

    private AppLovinIncentivizedInterstitial getVideo(Context activity, String adUnitId) {
        AppLovinIncentivizedInterstitial videoAd = mRvAds.get(adUnitId);
        if (videoAd == null) {
            AppLovinSdk lovinSdk = AppLovinSdk.getInstance(mAppKey, new AppLovinSdkSettings()
                    , activity);
            if (lovinSdk == null) {
                return null;
            }
            videoAd = AppLovinIncentivizedInterstitial.create(adUnitId, lovinSdk);
            mRvAds.put(adUnitId, videoAd);
        }
        return videoAd;
    }


    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            AppLovinIncentivizedInterstitial videoAd = getVideo(activity, adUnitId);
            if (videoAd == null) {
                callback.onRewardedVideoLoadFailed("onRewardedVideoLoadFailed");
            } else {
                if (videoAd.isAdReadyToDisplay()) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadSuccess();
                    }
                } else {
                    videoAd.preload(new AppLovinAdLoadListener() {
                        @Override
                        public void adReceived(AppLovinAd appLovinAd) {
                            AdLog.getSingleton().LogD("AppLovinAdapter", "adReceived:" + appLovinAd);
                            if (appLovinAd != null) {
                                if (callback != null) {
                                    callback.onRewardedVideoLoadSuccess();
                                }
                            }
                        }

                        @Override
                        public void failedToReceiveAd(int i) {
                            AdLog.getSingleton().LogE("AppLovinAdapter: failedToReceiveAd:" + i);
                            if (callback != null) {
                                callback.onRewardedVideoLoadFailed("failedToReceiveAd:" + i);
                            }
                        }
                    });
                    mRvCallbacks.put(adUnitId, callback);
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public void showRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(error);
            }
            return;
        }
        AppLovinIncentivizedInterstitial videoAd = getVideo(activity, adUnitId);
        if (videoAd == null) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("applovin video get fail when show");
            }
        } else {
            if (!isRewardedVideoAvailable(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed("applovin video not ready when show");
                }
                return;
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
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity);
            if (mDidInited) {
                AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create(mAppLovinSDk, activity);

                String pid = (String) dataMap.get("pid");
                if (!TextUtils.isEmpty(pid)) {
                    mIsAds.put(pid, interstitialAd);
                    mIsCallbacks.put(pid, callback);
                    interstitialAd.setAdClickListener(this);
                    interstitialAd.setAdDisplayListener(this);
                }
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
        }
    }

    @Override
    public void loadInterstitialAd(Context activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (mAppLovinSDk != null) {
                mAppLovinSDk.getAdService().loadNextAdForZoneId(adUnitId, new AppLovinAdLoadListener() {
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
                        AdLog.getSingleton().LogE("Om-AppLovin: AppLovin interstitial ad load failed : " + i);
                        if (callback != null) {
                            callback.onInterstitialAdLoadFailed("Om-");
                        }
                    }
                });
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    @Override
    public void showInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(error);
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            AppLovinInterstitialAdDialog adDialog = mIsAds.get(adUnitId);
            if (adDialog != null) {
                adDialog.showAndRender(mAppLovinIsAds.get(adUnitId));
                mAppLovinIsAds.remove(adUnitId);
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("AppLovin Interstitial ad is not ready to show");
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        AppLovinInterstitialAdDialog adDialog = mIsAds.get(adUnitId);
        return adDialog != null && mAppLovinIsAds.containsKey(adUnitId);
    }

    @Override
    public void adClicked(AppLovinAd appLovinAd) {
        AdLog.getSingleton().LogD("AppLovinAdapter", "adClicked:" + appLovinAd);
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
        AdLog.getSingleton().LogD("AppLovinAdapter", "adDisplayed:" + appLovinAd);
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
        AdLog.getSingleton().LogD("AppLovinAdapter", "adHidden:" + appLovinAd);
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
        AdLog.getSingleton().LogD("AppLovinAdapter", "videoPlaybackBegan:" + appLovinAd);
        if (appLovinAd != null) {
            RewardedVideoCallback callback = mRvCallbacks.get(appLovinAd.getZoneId());
            if (callback != null) {
                callback.onRewardedVideoAdStarted();
            }
        }
    }

    @Override
    public void videoPlaybackEnded(AppLovinAd appLovinAd, double percentViewed, boolean fullyWatched) {
        AdLog.getSingleton().LogD("AppLovinAdapter", "videoPlaybackEnded:" + appLovinAd
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

}
