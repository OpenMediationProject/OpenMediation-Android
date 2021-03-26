// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGBidInterstitialVideoHandler;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGBidRewardVideoHandler;
import com.mintegral.msdk.out.MTGConfiguration;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.mintegral.BuildConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralAdapter extends CustomAdsAdapter {
    private static final String PAY_LOAD = "pay_load";
    private ConcurrentHashMap<String, MTGInterstitialVideoHandler> mInterstitialAds;
    private ConcurrentHashMap<String, MTGRewardVideoHandler> mRvAds;
    private ConcurrentHashMap<String, MTGBidInterstitialVideoHandler> mInterstitialBidAds;
    private ConcurrentHashMap<String, MTGBidRewardVideoHandler> mRvBidAds;
    private ConcurrentHashMap<String, Boolean> mBidAdUnits;

    public MintegralAdapter() {
        mInterstitialAds = new ConcurrentHashMap<>();
        mRvAds = new ConcurrentHashMap<>();
        mRvBidAds = new ConcurrentHashMap<>();
        mInterstitialBidAds = new ConcurrentHashMap<>();
        mBidAdUnits = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return MTGConfiguration.SDK_VERSION;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_14;
    }

    @Override
    public void setGDPRConsent(final Context context, final boolean consent) {
        super.setGDPRConsent(context, consent);
        if (context != null) {
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            int consentStatus = consent ? MIntegralConstans.IS_SWITCH_ON : MIntegralConstans.IS_SWITCH_OFF;
            sdk.setConsentStatus(context, consentStatus);
        }
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
        sdk.setDoNotTrackStatus(value);
    }

    @Override
    public void initRewardedVideo(final Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        final String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity.getApplicationContext(), new MintegralSingleTon.InitCallback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onRewardedVideoInitSuccess();
                    }
                    setCustomParam(activity);
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

    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        realLoadRvAd(activity, adUnitId, extras, callback);
    }

    private void realLoadRvAd(final Activity activity, final String adUnitId, final Map<String, Object> extras, final RewardedVideoCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (TextUtils.isEmpty(error)) {
                        String payload = "";
                        if (extras != null && extras.containsKey(PAY_LOAD)) {
                            payload = String.valueOf(extras.get(PAY_LOAD));
                        }
                        if (TextUtils.isEmpty(payload)) {
                            mBidAdUnits.remove(adUnitId);
                            loadRvAd(activity, adUnitId, callback);
                        } else {
                            mBidAdUnits.put(adUnitId, true);
                            loadRvAdWithBid(activity, adUnitId, payload, callback);
                        }
                    } else {
                        if (callback != null) {
                            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, e.getMessage()));
                    }
                }
            }
        });
    }

    private void loadRvAd(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        if (rewardVideoHandler == null) {
            rewardVideoHandler = new MTGRewardVideoHandler(activity, "", adUnitId);
            mRvAds.put(adUnitId, rewardVideoHandler);
            rewardVideoHandler.setRewardVideoListener(new MtgRvAdListener(callback));
        }
        if (rewardVideoHandler.isReady()) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            rewardVideoHandler.load();
        }
    }

    private void loadRvAdWithBid(Activity activity, String adUnitId, String payload, RewardedVideoCallback callback) {
        MTGBidRewardVideoHandler rewardVideoHandler = mRvBidAds.get(adUnitId);
        if (rewardVideoHandler == null) {
            rewardVideoHandler = new MTGBidRewardVideoHandler(activity, "", adUnitId);
            mRvBidAds.put(adUnitId, rewardVideoHandler);
            rewardVideoHandler.setRewardVideoListener(new MtgRvAdListener(callback));
        }
        if (rewardVideoHandler.isBidReady()) {
            if (callback != null) {
                callback.onRewardedVideoLoadSuccess();
            }
        } else {
            rewardVideoHandler.loadFromBid(payload);
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        if (mBidAdUnits.containsKey(adUnitId)) {
            MTGBidRewardVideoHandler videoHandler = mRvBidAds.get(adUnitId);
            return videoHandler != null && videoHandler.isBidReady();
        }
        MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        return rewardVideoHandler != null && rewardVideoHandler.isReady();
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
        showRvAd(adUnitId, callback);
    }

    private void showRvAd(String adUnitId, RewardedVideoCallback callback) {
        if (mBidAdUnits.containsKey(adUnitId)) {
            MTGBidRewardVideoHandler rewardVideoHandler = mRvBidAds.get(adUnitId);
            if (rewardVideoHandler == null || !rewardVideoHandler.isBidReady()) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MTGRewardedVideo Ad Not Ready"));
                }
                return;
            }
            rewardVideoHandler.showFromBid("1");
        } else {
            MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
            if (rewardVideoHandler == null || !rewardVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MTGRewardedVideo Ad Not Ready"));
                }
                return;
            }
            rewardVideoHandler.show("1");
        }
    }

    @Override
    public void initInterstitialAd(final Activity activity, Map<String, Object> dataMap, final InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity.getApplicationContext(), new MintegralSingleTon.InitCallback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onInterstitialAdInitSuccess();
                    }
                    setCustomParam(activity);
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

    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        realLoadIsAd(activity, adUnitId, extras, callback);
    }

    private void realLoadIsAd(final Activity activity, final String adUnitId, final Map<String, Object> extras, final InterstitialAdCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (TextUtils.isEmpty(error)) {
                        String payload = "";
                        if (extras != null && extras.containsKey(PAY_LOAD)) {
                            payload = String.valueOf(extras.get(PAY_LOAD));
                        }
                        if (TextUtils.isEmpty(payload)) {
                            mBidAdUnits.remove(adUnitId);
                            loadIsAd(activity, adUnitId, callback);
                        } else {
                            mBidAdUnits.put(adUnitId, true);
                            loadIsAdWithBid(activity, adUnitId, payload, callback);
                        }
                    } else {
                        if (callback != null) {
                            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, e.getMessage()));
                    }
                }
            }
        });
    }

    private void loadIsAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null) {
            mtgInterstitialVideoHandler = new MTGInterstitialVideoHandler(activity, "", adUnitId);
            mInterstitialAds.put(adUnitId, mtgInterstitialVideoHandler);
            mtgInterstitialVideoHandler.setInterstitialVideoListener(new MtgInterstitialAdListener(callback));
        }
        if (mtgInterstitialVideoHandler.isReady()) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            mtgInterstitialVideoHandler.load();
        }
    }

    private void loadIsAdWithBid(Activity activity, String adUnitId, String payload, InterstitialAdCallback callback) {
        MTGBidInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null) {
            mtgInterstitialVideoHandler = new MTGBidInterstitialVideoHandler(activity, "", adUnitId);
            mInterstitialBidAds.put(adUnitId, mtgInterstitialVideoHandler);
            mtgInterstitialVideoHandler.setInterstitialVideoListener(new MtgInterstitialAdListener(callback));
        }
        if (mtgInterstitialVideoHandler.isBidReady()) {
            if (callback != null) {
                callback.onInterstitialAdLoadSuccess();
            }
        } else {
            mtgInterstitialVideoHandler.loadFromBid(payload);
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        if (mBidAdUnits.containsKey(adUnitId)) {
            MTGBidInterstitialVideoHandler bidInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
            return bidInterstitialVideoHandler != null && bidInterstitialVideoHandler.isBidReady();
        }
        MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        return mtgInterstitialVideoHandler != null && mtgInterstitialVideoHandler.isReady();
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
        showIsAd(adUnitId, callback);
    }

    private void showIsAd(String adUnitId, InterstitialAdCallback callback) {
        if (mBidAdUnits.containsKey(adUnitId)) {
            MTGBidInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
            if (mtgInterstitialVideoHandler == null || !mtgInterstitialVideoHandler.isBidReady()) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Mintegral Interstitial Ad Not Ready"));
                }
                return;
            }
            mtgInterstitialVideoHandler.showFromBid();
        } else {
            MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
            if (mtgInterstitialVideoHandler == null || !mtgInterstitialVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Mintegral Interstitial Ad Not Ready"));
                }
                return;
            }
            mtgInterstitialVideoHandler.show();
        }
    }

    private void initSDK(Context context, MintegralSingleTon.InitCallback listener) {
        MintegralSingleTon.getInstance().initSDK(context, mAppKey, listener);
    }

    private void setCustomParam(Context context) {
        if (mUserAge != null) {
            setUserAge(context, mUserAge);
        }
        if (mUserGender != null) {
            setUserGender(context, mUserGender);
        }
    }

    private static class MtgInterstitialAdListener implements InterstitialVideoListener {

        private InterstitialAdCallback mCallback;

        MtgInterstitialAdListener(InterstitialAdCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onLoadSuccess(String placementId, String unitId) {

        }

        @Override
        public void onVideoLoadSuccess(String placementId, String unitId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onVideoLoadFail(String errorMsg) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "MintegralAdapter", errorMsg));
            }
        }

        @Override
        public void onAdShow() {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onAdClose(boolean b) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onShowFail(String s) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "MintegralAdapter", s));
            }
        }

        @Override
        public void onVideoAdClicked(String placementId, String unitId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClick();
            }
        }

        @Override
        public void onVideoComplete(String placementId, String unitId) {
        }

        @Override
        public void onAdCloseWithIVReward(boolean isComplete, int rewardAlertStatus) {

        }

        @Override
        public void onEndcardShow(String placementId, String unitId) {
        }
    }

    private static class MtgRvAdListener implements RewardVideoListener {

        private RewardedVideoCallback mRvCallback;

        MtgRvAdListener(RewardedVideoCallback callback) {
            mRvCallback = callback;
        }

        @Override
        public void onVideoLoadSuccess(String placementId, String unitId) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onLoadSuccess(String placementId, String unitId) {

        }

        @Override
        public void onVideoLoadFail(String errorMsg) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "MintegralAdapter", errorMsg));
            }
        }

        @Override
        public void onAdShow() {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdShowSuccess();
                mRvCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onAdClose(boolean isCompleteView, String rewardName, float rewardAmount) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onShowFail(String errorMsg) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, "MintegralAdapter", errorMsg));
            }
        }

        @Override
        public void onVideoAdClicked(String placementId, String unitId) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onVideoComplete(String placementId, String unitId) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdEnded();
                mRvCallback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onEndcardShow(String placementId, String unitId) {

        }
    }
}
