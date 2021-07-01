// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.mbridge.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mbridge.msdk.interstitialvideo.out.MBBidInterstitialVideoHandler;
import com.mbridge.msdk.interstitialvideo.out.MBInterstitialVideoHandler;
import com.mbridge.msdk.out.MBBidRewardVideoHandler;
import com.mbridge.msdk.out.MBConfiguration;
import com.mbridge.msdk.out.MBRewardVideoHandler;
import com.mbridge.msdk.out.RewardVideoListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.mintegral.BuildConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralAdapter extends CustomAdsAdapter {
    private static final String PAY_LOAD = "pay_load";
    private ConcurrentHashMap<String, MBInterstitialVideoHandler> mInterstitialAds;
    private ConcurrentHashMap<String, MBRewardVideoHandler> mRvAds;
    private ConcurrentHashMap<String, MBBidInterstitialVideoHandler> mInterstitialBidAds;
    private ConcurrentHashMap<String, MBBidRewardVideoHandler> mRvBidAds;
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
        return MBConfiguration.SDK_VERSION;
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
    public boolean isAdNetworkInit() {
        return MintegralSingleTon.getInstance().isInit();
    }

    @Override
    public void initRewardedVideo(final Activity activity, Map<String, Object> dataMap, final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        final String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        initSDK(new MintegralSingleTon.InitCallback() {
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
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        realLoadRvAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    private void realLoadRvAd(final Context context, final String adUnitId, final Map<String, Object> extras, final RewardedVideoCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String payload = "";
                    if (extras != null && extras.containsKey(PAY_LOAD)) {
                        payload = String.valueOf(extras.get(PAY_LOAD));
                    }
                    if (TextUtils.isEmpty(payload)) {
                        mBidAdUnits.remove(adUnitId);
                        loadRvAd(context, adUnitId, callback);
                    } else {
                        mBidAdUnits.put(adUnitId, true);
                        loadRvAdWithBid(context, adUnitId, payload, callback);
                    }
                } catch(Exception e) {
                    if (callback != null) {
                        callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, e.getMessage()));
                    }
                }
            }
        });
    }

    private void loadRvAd(Context context, String adUnitId, RewardedVideoCallback callback) {
        MBRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        if (rewardVideoHandler == null) {
            rewardVideoHandler = new MBRewardVideoHandler(context, "", adUnitId);
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

    private void loadRvAdWithBid(Context context, String adUnitId, String payload, RewardedVideoCallback callback) {
        MBBidRewardVideoHandler rewardVideoHandler = mRvBidAds.get(adUnitId);
        if (rewardVideoHandler == null) {
            rewardVideoHandler = new MBBidRewardVideoHandler(context, "", adUnitId);
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
            MBBidRewardVideoHandler videoHandler = mRvBidAds.get(adUnitId);
            return videoHandler != null && videoHandler.isBidReady();
        }
        MBRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        return rewardVideoHandler != null && rewardVideoHandler.isReady();
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(adUnitId);
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
            MBBidRewardVideoHandler rewardVideoHandler = mRvBidAds.get(adUnitId);
            if (rewardVideoHandler == null || !rewardVideoHandler.isBidReady()) {
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MTGRewardedVideo Ad Not Ready"));
                }
                return;
            }
            rewardVideoHandler.showFromBid("1");
        } else {
            MBRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
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
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        initSDK(new MintegralSingleTon.InitCallback() {
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
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        realLoadIsAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    private void realLoadIsAd(final Context context, final String adUnitId, final Map<String, Object> extras, final InterstitialAdCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String payload = "";
                    if (extras != null && extras.containsKey(PAY_LOAD)) {
                        payload = String.valueOf(extras.get(PAY_LOAD));
                    }
                    if (TextUtils.isEmpty(payload)) {
                        mBidAdUnits.remove(adUnitId);
                        loadIsAd(context, adUnitId, callback);
                    } else {
                        mBidAdUnits.put(adUnitId, true);
                        loadIsAdWithBid(context, adUnitId, payload, callback);
                    }
                } catch(Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                                AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, e.getMessage()));
                    }
                }
            }
        });
    }

    private void loadIsAd(Context context, String adUnitId, InterstitialAdCallback callback) {
        MBInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null) {
            mtgInterstitialVideoHandler = new MBInterstitialVideoHandler(context, "", adUnitId);
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

    private void loadIsAdWithBid(Context context, String adUnitId, String payload, InterstitialAdCallback callback) {
        MBBidInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null) {
            mtgInterstitialVideoHandler = new MBBidInterstitialVideoHandler(context, "", adUnitId);
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
            MBBidInterstitialVideoHandler bidInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
            return bidInterstitialVideoHandler != null && bidInterstitialVideoHandler.isBidReady();
        }
        MBInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        return mtgInterstitialVideoHandler != null && mtgInterstitialVideoHandler.isReady();
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
        showIsAd(adUnitId, callback);
    }

    private void showIsAd(String adUnitId, InterstitialAdCallback callback) {
        if (mBidAdUnits.containsKey(adUnitId)) {
            MBBidInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialBidAds.get(adUnitId);
            if (mtgInterstitialVideoHandler == null || !mtgInterstitialVideoHandler.isBidReady()) {
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "Mintegral Interstitial Ad Not Ready"));
                }
                return;
            }
            mtgInterstitialVideoHandler.showFromBid();
        } else {
            MBInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
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

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> extras, BannerAdCallback callback) {
        super.initBannerAd(activity, extras, callback);
        String error = check();
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        MintegralBannerManager.getInstance().initAd(MediationUtil.getContext(), extras, callback);
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        super.loadBannerAd(activity, adUnitId, extras, callback);
        String error = check(adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            }
            return;
        }
        MintegralBannerManager.getInstance().loadAd(MediationUtil.getContext(), adUnitId, extras, callback);
    }

    @Override
    public boolean isBannerAdAvailable(String adUnitId) {
        return MintegralBannerManager.getInstance().isAdAvailable(adUnitId);
    }

    @Override
    public void destroyBannerAd(String adUnitId) {
        super.destroyBannerAd(adUnitId);
        MintegralBannerManager.getInstance().destroyAd(adUnitId);
    }

    private void initSDK(MintegralSingleTon.InitCallback listener) {
        MintegralSingleTon.getInstance().initSDK(MediationUtil.getContext(), mAppKey, listener);
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
        public void onShowFail(String errorMsg) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, "MintegralAdapter", errorMsg));
            }
        }

        @Override
        public void onVideoAdClicked(String placementId, String unitId) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClicked();
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
