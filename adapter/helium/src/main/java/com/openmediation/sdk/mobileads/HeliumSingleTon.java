// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.chartboost.heliumsdk.HeliumSdk;
import com.chartboost.heliumsdk.ad.HeliumAdError;
import com.chartboost.heliumsdk.ad.HeliumInterstitialAd;
import com.chartboost.heliumsdk.ad.HeliumInterstitialAdListener;
import com.chartboost.heliumsdk.ad.HeliumRewardedAd;
import com.chartboost.heliumsdk.ad.HeliumRewardedAdListener;
import com.openmediation.sdk.utils.AdLog;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HeliumSingleTon {

    enum InitState {
        /**
         *
         */
        NOT_INIT,
        /**
         *
         */
        INIT_PENDING,
        /**
         *
         */
        INIT_SUCCESS,
    }

    private final ConcurrentMap<String, HeliumRewardedAd> mRvAds;
    private final ConcurrentMap<String, HeliumInterstitialAd> mIsAds;

    private final ConcurrentMap<String, HeliumBidCallback> mBidCallbacks;
    private final ConcurrentMap<String, HeliumAdError> mBidError;
    private HeliumInterstitialCallback mInterstitialAdCallback;
    private HeliumVideoCallback mVideoAdCallback;

    private volatile InitState mInitState = InitState.NOT_INIT;

    private final List<HeliumInitCallback> mCallbacks = new CopyOnWriteArrayList<>();

    private static class CbtHolder {
        private static final HeliumSingleTon INSTANCE = new HeliumSingleTon();
    }

    private HeliumSingleTon() {
        mRvAds = new ConcurrentHashMap<>();
        mIsAds = new ConcurrentHashMap<>();
        mBidCallbacks = new ConcurrentHashMap<>();
        mBidError = new ConcurrentHashMap<>();
    }

    public static HeliumSingleTon getInstance() {
        return CbtHolder.INSTANCE;
    }

    public void init(Context context, String appKey, final HeliumInitCallback cbtCallback) {
        try {
            if (TextUtils.isEmpty(appKey)) {
                if (cbtCallback != null) {
                    cbtCallback.initFailed("app key is empty");
                }
                return;
            }

            if (InitState.INIT_SUCCESS == mInitState) {
                if (cbtCallback != null) {
                    cbtCallback.initSuccess();
                }
                return;
            }
            if (cbtCallback != null) {
                mCallbacks.add(cbtCallback);
            }
            if (InitState.INIT_PENDING == mInitState) {
                return;
            }
            mInitState = InitState.INIT_PENDING;
            String[] tmp = appKey.split("#");
            String appId = tmp[0];
            String signature = tmp[1];
            HeliumSdk.start(context.getApplicationContext(), appId, signature, new HeliumSdk.HeliumSdkListener() {
                @Override
                public void didInitialize(Error error) {
                    if (error == null) {
                        mInitState = InitState.INIT_SUCCESS;
                        AdLog.getSingleton().LogD("Helium SDK initialized successfully");
                        for (HeliumInitCallback callback : mCallbacks) {
                            if (callback != null) {
                                callback.initSuccess();
                            }
                        }
                    } else {
                        mInitState = InitState.NOT_INIT;
                        AdLog.getSingleton().LogD("Helium SDK initialized failed");
                        for (HeliumInitCallback callback : mCallbacks) {
                            if (callback != null) {
                                callback.initFailed(error.getMessage());
                            }
                        }
                    }
                    mCallbacks.clear();
                }
            });
        } catch (Exception e) {
            mInitState = InitState.NOT_INIT;
            AdLog.getSingleton().LogE("OM-ChartboostBid", e.getMessage());
            for (HeliumInitCallback callback : mCallbacks) {
                if (callback != null) {
                    callback.initFailed(e.getMessage());
                }
            }
            mCallbacks.clear();
        }
    }

    InitState getInitState() {
        return mInitState;
    }

    void setInterstitialAdCallback(HeliumInterstitialCallback callback) {
        mInterstitialAdCallback = callback;
    }

    void setVideoAdCallback(HeliumVideoCallback callback) {
        mVideoAdCallback = callback;
    }

    void addBidCallback(String placementId, HeliumBidCallback callback) {
        if (!TextUtils.isEmpty(placementId) && callback != null) {
            mBidCallbacks.put(placementId, callback);
        }
    }

    void removeBidCallback(String placementId) {
        if (!TextUtils.isEmpty(placementId)) {
            mBidCallbacks.remove(placementId);
        }
    }

    void loadRewardedVideo(String adUnitId) {
        HeliumRewardedAd heliumAd = mRvAds.get(adUnitId);
        if (heliumAd != null) {
            HeliumSdk.clearLoaded(heliumAd);
        }
        HeliumRewardedAd rewardedAd = new HeliumRewardedAd(adUnitId, new InnerRvListener());
        mRvAds.put(adUnitId, rewardedAd);
        rewardedAd.load();
    }

    HeliumAdError getError(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId)) {
            return mBidError.get(adUnitId);
        }
        return null;
    }

    boolean isRewardedVideoReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mRvAds.containsKey(adUnitId) && mRvAds.get(adUnitId).readyToShow();
    }

    void showRewardedVideo(String adUnitId) {
        HeliumRewardedAd heliumRewardedAd = mRvAds.get(adUnitId);
        if (heliumRewardedAd != null) {
            heliumRewardedAd.show();
            mRvAds.remove(adUnitId);
        }
    }

    void loadInterstitial(String adUnitId) {
        HeliumInterstitialAd heliumAd = mIsAds.get(adUnitId);
        if (heliumAd != null) {
            HeliumSdk.clearLoaded(heliumAd);
        }
        HeliumInterstitialAd interstitialAd = new HeliumInterstitialAd(adUnitId, new InnerIsListener());
        mIsAds.put(adUnitId, interstitialAd);
        interstitialAd.load();
    }

    boolean isInterstitialReady(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mIsAds.containsKey(adUnitId) && mIsAds.get(adUnitId).readyToShow();
    }

    void showInterstitial(String adUnitId) {
        HeliumInterstitialAd heliumInterstitialAd = mIsAds.get(adUnitId);
        if (heliumInterstitialAd != null) {
            heliumInterstitialAd.show();
            mIsAds.remove(adUnitId);
        }
    }

    private static class InnerRvListener implements HeliumRewardedAdListener {

        @Override
        public void didReceiveWinningBid(String placementId, HashMap<String, String> hashMap) {
            AdLog.getSingleton().LogD("Helium RewardVideo ad didReceiveWinningBid, placementId : " + placementId + ", " + hashMap);
            if (getInstance().mBidCallbacks.containsKey(placementId)) {
                getInstance().mBidCallbacks.get(placementId).onBidSuccess(placementId, hashMap);
            }
        }

        @Override
        public void didCache(String placementId, HeliumAdError error) {
            if (error != null) {
                AdLog.getSingleton().LogE("Helium RewardedAd didCache ad error : " + placementId + " " + error.toString());
                if (getInstance().mBidCallbacks.containsKey(placementId)) {
                    getInstance().mBidCallbacks.get(placementId).onBidFailed(placementId, error.message);
                }
                if (!TextUtils.isEmpty(placementId)) {
                    getInstance().mBidError.put(placementId, error);
                }
                return;
            }
            AdLog.getSingleton().LogD("Helium HeliumRewardedAd didCache ad load success " + placementId);
        }

        @Override
        public void didShow(String placementId, HeliumAdError error) {
            if (getInstance().mVideoAdCallback == null) {
                return;
            }
            if (error != null) {
                AdLog.getSingleton().LogE("Helium RewardedAd didCache ad error : " + placementId + " " + error.toString());
                getInstance().mVideoAdCallback.didRewardedShowFailed(placementId, error);
                return;
            }
            AdLog.getSingleton().LogD("Helium RewardVideo ad display");
            getInstance().mVideoAdCallback.didRewardedShowed(placementId);
        }

        @Override
        public void didClose(String placementId, HeliumAdError error) {
            AdLog.getSingleton().LogD("Helium RewardVideo ad close");
            if (getInstance().mVideoAdCallback != null) {
                getInstance().mVideoAdCallback.didRewardedClosed(placementId);
            }
        }

        @Override
        public void didReceiveReward(String placementId, String s1) {
            AdLog.getSingleton().LogD("Helium RewardVideo ad didReceiveReward");
            if (getInstance().mVideoAdCallback != null) {
                getInstance().mVideoAdCallback.didRewardedRewarded(placementId);
            }
        }
    }

    private static class InnerIsListener implements HeliumInterstitialAdListener {

        @Override
        public void didReceiveWinningBid(String placementId, HashMap<String, String> hashMap) {
            AdLog.getSingleton().LogD("Helium Interstitial ad didReceiveWinningBid, placementId : " + placementId + ", " + hashMap);
            if (getInstance().mBidCallbacks.containsKey(placementId)) {
                getInstance().mBidCallbacks.get(placementId).onBidSuccess(placementId, hashMap);
            }
        }

        @Override
        public void didCache(String placementId, HeliumAdError error) {
            if (error != null) {
                AdLog.getSingleton().LogE("Helium Interstitial ad load failed : " + error.toString());
                if (getInstance().mBidCallbacks.containsKey(placementId)) {
                    getInstance().mBidCallbacks.get(placementId).onBidFailed(placementId, error.message);
                }
                if (!TextUtils.isEmpty(placementId)) {
                    getInstance().mBidError.put(placementId, error);
                }
                return;
            }
            AdLog.getSingleton().LogD("Helium Interstitial ad complete");
        }

        @Override
        public void didShow(String placementId, HeliumAdError error) {
            if (error != null) {
                AdLog.getSingleton().LogD("Helium Helium Interstitial ad show failed: " + error.toString());
                if (getInstance().mInterstitialAdCallback != null) {
                    getInstance().mInterstitialAdCallback.didInterstitialShowFailed(placementId, error);
                }
                return;
            }
            AdLog.getSingleton().LogD("Helium Helium Interstitial ad didShow");
            if (getInstance().mInterstitialAdCallback != null) {
                getInstance().mInterstitialAdCallback.didInterstitialShowed(placementId);
            }
        }

        @Override
        public void didClose(String placementId, HeliumAdError error) {
            AdLog.getSingleton().LogD("Helium Helium Interstitial ad close");
            if (getInstance().mInterstitialAdCallback != null) {
                getInstance().mInterstitialAdCallback.didInterstitialClosed(placementId);
            }
        }
    }
}
