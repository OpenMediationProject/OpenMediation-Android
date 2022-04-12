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
import com.openmediation.sdk.mediation.BidCallback;
import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.utils.AdLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    }

    public static HeliumSingleTon getInstance() {
        return CbtHolder.INSTANCE;
    }

    public void init(final Context context, String appKey, final HeliumInitCallback cbtCallback) {
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
            final String appId = tmp[0];
            final String signature = tmp[1];

            MediationUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HeliumSdk.start(context, appId, signature, new HeliumSdk.HeliumSdkListener() {
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
                }
            });
        } catch (Throwable e) {
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

    void loadRewardedVideo(String adUnitId, RewardedVideoCallback adCallback, BidCallback bidCallback) {
        HeliumRewardedAd heliumAd = mRvAds.get(adUnitId);
        if (heliumAd != null) {
            HeliumSdk.clearLoaded(heliumAd);
        }
        InnerRvListener listener = new InnerRvListener(adCallback, bidCallback);
        HeliumRewardedAd rewardedAd = new HeliumRewardedAd(adUnitId, listener);
        listener.setHeliumAd(rewardedAd);
        rewardedAd.load();
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

    void loadInterstitial(String adUnitId, InterstitialAdCallback adCallback, BidCallback bidCallback) {
        HeliumInterstitialAd heliumAd = mIsAds.get(adUnitId);
        if (heliumAd != null) {
            HeliumSdk.clearLoaded(heliumAd);
        }
        InnerIsListener listener = new InnerIsListener(adCallback, bidCallback);
        HeliumInterstitialAd interstitialAd = new HeliumInterstitialAd(adUnitId, listener);
        listener.setHeliumAd(interstitialAd);
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

    private class InnerRvListener implements HeliumRewardedAdListener {

        private final RewardedVideoCallback mAdCallback;
        private final BidCallback mBidCallback;
        private HeliumRewardedAd mHeliumRewardedAd;

        private InnerRvListener(RewardedVideoCallback adCallback, BidCallback bidCallback) {
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        public void setHeliumAd(HeliumRewardedAd rewardedAd) {
            mHeliumRewardedAd = rewardedAd;
        }

        @Override
        public void didReceiveWinningBid(String placementId, HashMap<String, String> hashMap) {
            AdLog.getSingleton().LogD("Helium RewardVideo ad didReceiveWinningBid, placementId : " + placementId + ", " + hashMap);
            mRvAds.put(placementId, mHeliumRewardedAd);
            if (mAdCallback != null) {
                onBidSuccess(hashMap, mAdCallback);
            }
            if (mBidCallback != null) {
                onBidSuccess(hashMap, mBidCallback);
            }
        }

        @Override
        public void didCache(String placementId, HeliumAdError error) {
            if (error != null) {
                AdLog.getSingleton().LogE("Helium RewardedAd didCache ad error : " + placementId + " " + error.toString());
                if (mAdCallback != null) {
                    onBidFailed(error.code + ", " + error.message, mAdCallback);
                    mAdCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "HeliumAdapter", error.code, error.message));
                }
                if (mBidCallback != null) {
                    onBidFailed(error.code + ", " + error.message, mBidCallback);
                }
                return;
            }
            AdLog.getSingleton().LogD("Helium HeliumRewardedAd didCache ad load success " + placementId);
            if (mAdCallback != null) {
                mAdCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void didShow(String placementId, HeliumAdError error) {
            if (mVideoAdCallback == null) {
                return;
            }
            if (error != null) {
                AdLog.getSingleton().LogE("Helium RewardedAd didCache ad error : " + placementId + " " + error.toString());
                mVideoAdCallback.didRewardedShowFailed(placementId, error);
                return;
            }
            AdLog.getSingleton().LogD("Helium RewardVideo ad display");
            mVideoAdCallback.didRewardedShowed(placementId);
        }

        @Override
        public void didClose(String placementId, HeliumAdError error) {
            AdLog.getSingleton().LogD("Helium RewardVideo ad close");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.didRewardedClosed(placementId);
            }
        }

        @Override
        public void didReceiveReward(String placementId, String s1) {
            AdLog.getSingleton().LogD("Helium RewardVideo ad didReceiveReward");
            if (mVideoAdCallback != null) {
                mVideoAdCallback.didRewardedRewarded(placementId);
            }
        }
    }

    private class InnerIsListener implements HeliumInterstitialAdListener {

        private final InterstitialAdCallback mAdCallback;
        private final BidCallback mBidCallback;
        private HeliumInterstitialAd mHeliumInterstitialAd;

        private InnerIsListener(InterstitialAdCallback adCallback, BidCallback bidCallback) {
            mAdCallback = adCallback;
            mBidCallback = bidCallback;
        }

        public void setHeliumAd(HeliumInterstitialAd ad) {
            mHeliumInterstitialAd = ad;
        }

        @Override
        public void didReceiveWinningBid(String placementId, HashMap<String, String> hashMap) {
            AdLog.getSingleton().LogD("Helium Interstitial ad didReceiveWinningBid, placementId : " + placementId + ", " + hashMap);
            mIsAds.put(placementId, mHeliumInterstitialAd);
            if (mAdCallback != null) {
                onBidSuccess(hashMap, mAdCallback);
            }
            if (mBidCallback != null) {
                onBidSuccess(hashMap, mBidCallback);
            }
        }

        @Override
        public void didCache(String placementId, HeliumAdError error) {
            if (error != null) {
                AdLog.getSingleton().LogE("Helium Interstitial ad load failed : " + placementId + ", " + error.toString());
                if (mAdCallback != null) {
                    onBidFailed(error.code + ", " + error.message, mAdCallback);
                    mAdCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "HeliumAdapter", error.code, error.message));
                }
                if (mBidCallback != null) {
                    onBidFailed(error.code + ", " + error.message, mBidCallback);
                }
                return;
            }
            AdLog.getSingleton().LogD("Helium Interstitial ad complete: " + placementId);
            if (mAdCallback != null) {
                mAdCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void didShow(String placementId, HeliumAdError error) {
            if (error != null) {
                AdLog.getSingleton().LogD("Helium Interstitial ad show failed: " + placementId + ", " + error.toString());
                if (mInterstitialAdCallback != null) {
                    mInterstitialAdCallback.didInterstitialShowFailed(placementId, error);
                }
                return;
            }
            AdLog.getSingleton().LogD("Helium Interstitial ad didShow: " + placementId);
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.didInterstitialShowed(placementId);
            }
        }

        @Override
        public void didClose(String placementId, HeliumAdError error) {
            AdLog.getSingleton().LogD("Helium Helium Interstitial ad close: " + placementId);
            if (mInterstitialAdCallback != null) {
                mInterstitialAdCallback.didInterstitialClosed(placementId);
            }
        }
    }

    private void onBidSuccess(Map<String, String> map, BidCallback callback) {
        if (callback == null) {
            return;
        }
        if (map == null || map.isEmpty() || !map.containsKey("price")) {
            callback.onBidFailed("Helium bid failed cause no bid response");
            return;
        }
        BidResponse bidResponse = new BidResponse();
        try {
            if (map.containsKey("price")) {
                String price = map.get("price");
                if (price != null && !TextUtils.isEmpty(price) && !price.equalsIgnoreCase("NaN")) {
                    bidResponse.setPrice(Double.parseDouble(price));
                }
            }
        } catch (Exception ignored) {
        }
        bidResponse.setOriginal(map.toString());
        callback.onBidSuccess(bidResponse);
    }

    private void onBidFailed(String error, BidCallback callback) {
        if (callback == null) {
            return;
        }
        callback.onBidFailed(error);
    }
}
