// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.chartboost.heliumsdk.HeliumSdk;
import com.chartboost.heliumsdk.ad.HeliumAdError;
import com.openmediation.sdk.bid.BidConstance;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BidCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HeliumAdapter extends CustomAdsAdapter implements HeliumInterstitialCallback, HeliumVideoCallback {

    private final static String APP_KEY = "AppKey";
    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private final ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    public HeliumAdapter() {
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return HeliumSdk.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.helium.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_17;
    }

    @Override
    public void initBid(Context context, Map<String, Object> dataMap) {
        super.initBid(context, dataMap);
        if (MediationUtil.getContext() == null) {
            return;
        }
        HeliumSingleTon.InitState initState = HeliumSingleTon.getInstance().getInitState();
        if (initState == HeliumSingleTon.InitState.NOT_INIT) {
            HeliumSingleTon.getInstance().init(MediationUtil.getContext(), String.valueOf(dataMap.get(BidConstance.BID_APP_KEY)), null);
        }
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (HeliumSingleTon.InitState.INIT_SUCCESS == HeliumSingleTon.getInstance().getInitState()) {
            HeliumSdk.setSubjectToGDPR(true);
            HeliumSdk.setUserHasGivenConsent(consent);
        }
    }

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        if (HeliumSingleTon.InitState.INIT_SUCCESS == HeliumSingleTon.getInstance().getInitState()) {
            HeliumSdk.setSubjectToCoppa(restricted);
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        if (HeliumSingleTon.InitState.INIT_SUCCESS == HeliumSingleTon.getInstance().getInitState()) {
            HeliumSdk.setCCPAConsent(!value);
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String checkError = check();
        if (TextUtils.isEmpty(checkError)) {
            if (dataMap.get("pid") != null && callback != null) {
                mRvCallbacks.put((String) dataMap.get("pid"), callback);
            }
            HeliumSingleTon.getInstance().init(MediationUtil.getContext(), String.valueOf(dataMap.get(APP_KEY)), new HeliumInitCallback() {
                @Override
                public void initSuccess() {
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                    setCustomParam();
                }

                @Override
                public void initFailed(String error) {
                    if (callback != null) {
                        callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        realLoadRvAd(adUnitId, callback);
    }

    private void realLoadRvAd(String adUnitId, RewardedVideoCallback callback) {
        try {
            String checkError = check(adUnitId);
            if (TextUtils.isEmpty(checkError)) {
                if (isRewardedVideoAvailable(adUnitId)) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadSuccess();
                    }
                } else {
                    HeliumSingleTon.getInstance().loadRewardedVideo(adUnitId, callback, null);
                }
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
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
            String checkError = check(adUnitId);
            if (TextUtils.isEmpty(checkError)) {
                if (isRewardedVideoAvailable(adUnitId)) {
                    if (callback != null) {
                        mRvCallbacks.put(adUnitId, callback);
                    }
                    HeliumSingleTon.getInstance().setVideoAdCallback(this);
                    HeliumSingleTon.getInstance().showRewardedVideo(adUnitId);
                } else {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Ad Not Ready"));
                }
            } else {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, checkError));
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
        return HeliumSingleTon.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String checkError = check();
        if (TextUtils.isEmpty(checkError)) {
            if (dataMap.get("pid") != null && callback != null) {
                mIsCallbacks.put((String) dataMap.get("pid"), callback);
            }
            HeliumSingleTon.getInstance().init(MediationUtil.getContext(), String.valueOf(dataMap.get(APP_KEY)), new HeliumInitCallback() {
                @Override
                public void initSuccess() {
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                    setCustomParam();
                }

                @Override
                public void initFailed(String error) {
                    if (callback != null) {
                        callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        realLoadIsAd(adUnitId, callback);
    }

    private void realLoadIsAd(String adUnitId, InterstitialAdCallback callback) {
        try {
            String checkError = check(adUnitId);
            if (TextUtils.isEmpty(checkError)) {
                if (isInterstitialAdAvailable(adUnitId)) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadSuccess();
                    }
                } else {
                    HeliumSingleTon.getInstance().loadInterstitial(adUnitId, callback, null);
                }
            } else {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
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
        try {
            String checkError = check(adUnitId);
            if (TextUtils.isEmpty(checkError)) {
                if (isInterstitialAdAvailable(adUnitId)) {
                    if (callback != null) {
                        mIsCallbacks.put(adUnitId, callback);
                    }
                    HeliumSingleTon.getInstance().setInterstitialAdCallback(this);
                    HeliumSingleTon.getInstance().showInterstitial(adUnitId);
                } else {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Ad Not Ready"));
                }
            } else {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, checkError));
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Unknown Error, " + e.getMessage()));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return HeliumSingleTon.getInstance().isInterstitialReady(adUnitId);
    }

    @Override
    public void didRewardedShowed(String placementId) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener == null) {
            return;
        }

        listener.onRewardedVideoAdShowSuccess();
        listener.onRewardedVideoAdStarted();
    }

    @Override
    public void didRewardedShowFailed(String placementId, HeliumAdError error) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener == null) {
            return;
        }
        listener.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error.getCode(), error.getMessage()));
    }

    @Override
    public void didRewardedClosed(String placementId) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener != null) {
            listener.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void didRewardedRewarded(String placementId) {
        RewardedVideoCallback listener = mRvCallbacks.get(placementId);
        if (listener != null) {
            listener.onRewardedVideoAdRewarded();
            listener.onRewardedVideoAdEnded();
        }
    }

    @Override
    public void didInterstitialShowed(String placementId) {
        InterstitialAdCallback listener = mIsCallbacks.get(placementId);
        if (listener == null) {
            return;
        }
        listener.onInterstitialAdShowSuccess();
    }

    @Override
    public void didInterstitialShowFailed(String placementId, HeliumAdError error) {
        InterstitialAdCallback listener = mIsCallbacks.get(placementId);
        if (listener == null) {
            return;
        }
        listener.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error.getCode(), error.getMessage()));
    }

    @Override
    public void didInterstitialClosed(String placementId) {
        InterstitialAdCallback listener = mIsCallbacks.get(placementId);
        if (listener != null) {
            listener.onInterstitialAdClosed();
        }
    }

    private void setCustomParam() {
        if (mUserConsent != null) {
            setGDPRConsent(null, mUserConsent);
        }
        if (mAgeRestricted != null) {
            setAgeRestricted(null, mAgeRestricted);
        }
        if (mUSPrivacyLimit != null) {
            setUSPrivacyLimit(null, mUSPrivacyLimit);
        }
    }

    @Override
    public void getBidResponse(Context context, final Map<String, Object> dataMap, final BidCallback callback) {
        super.getBidResponse(context, dataMap, callback);
        String appKey = "";
        if (dataMap.get(BidConstance.BID_APP_KEY) != null) {
            appKey = String.valueOf(dataMap.get(BidConstance.BID_APP_KEY));
        }
        HeliumSingleTon.getInstance().init(MediationUtil.getContext(), appKey, new HeliumInitCallback() {
            @Override
            public void initSuccess() {
                executeBid(dataMap, callback);
            }

            @Override
            public void initFailed(String error) {
                if (callback != null) {
                    callback.onBidFailed("Helium SDK init error: " + error);
                }
            }
        });
    }

    private void executeBid(Map<String, Object> dataMap, BidCallback callback) {
        try {
            int adType = (int) dataMap.get(BidConstance.BID_AD_TYPE);
            String adUnitId = (String) dataMap.get(BidConstance.BID_PLACEMENT_ID);
            if (adType == BidConstance.INTERSTITIAL) {
                HeliumSingleTon.getInstance().loadInterstitial(adUnitId, null, callback);
            } else if (adType == BidConstance.VIDEO) {
                HeliumSingleTon.getInstance().loadRewardedVideo(adUnitId, null, callback);
            } else {
                if (callback != null) {
                    callback.onBidFailed("unSupport bid type");
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBidFailed("Bid Failed: " + e.getMessage());
            }
        }
    }
}
