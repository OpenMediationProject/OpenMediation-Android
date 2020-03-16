// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;
import com.openmediation.sdk.mobileads.mintegral.BuildConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralAdapter extends CustomAdsAdapter {
    private boolean mDidInitSdk;
    private ConcurrentHashMap<String, MTGInterstitialVideoHandler> mInterstitialAds;
    private ConcurrentHashMap<String, MTGRewardVideoHandler> mRvAds;

    public MintegralAdapter() {
        mInterstitialAds = new ConcurrentHashMap<>();
        mRvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return "";
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
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        initSDK(activity.getApplicationContext());
        if (mDidInitSdk) {
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
            if (rewardVideoHandler == null) {
                rewardVideoHandler = new MTGRewardVideoHandler(activity, adUnitId);
                mRvAds.put(adUnitId, rewardVideoHandler);
            }
            if (rewardVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                rewardVideoHandler.setRewardVideoListener(new MtgRvAdListener(callback));
                rewardVideoHandler.load();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        return rewardVideoHandler != null && rewardVideoHandler.isReady();
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        if (rewardVideoHandler == null || !rewardVideoHandler.isReady()) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("MTGRewardedVideo ad not ready");
            }
            return;
        }
        rewardVideoHandler.show("1");
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        initSDK(activity.getApplicationContext());
        if (mDidInitSdk) {
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
            if (mtgInterstitialVideoHandler == null) {
                mtgInterstitialVideoHandler = new MTGInterstitialVideoHandler(activity, adUnitId);
                mInterstitialAds.put(adUnitId, mtgInterstitialVideoHandler);
            }

            if (mtgInterstitialVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                mtgInterstitialVideoHandler.setInterstitialVideoListener(new MtgInterstitialAdListener(callback));
                mtgInterstitialVideoHandler.load();
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        return mtgInterstitialVideoHandler != null && mtgInterstitialVideoHandler.isReady();
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null || !mtgInterstitialVideoHandler.isReady()) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("Mintegral Interstitial Ad Not Ready");
            }
            return;
        }
        mtgInterstitialVideoHandler.show();
    }

    private void initSDK(Context context) {
        if (!mDidInitSdk) {
            mDidInitSdk = true;
            String[] tmp = mAppKey.split("#");
            String appId = tmp[0];
            String key = tmp[1];
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, String> map = sdk.getMTGConfigurationMap(appId, key);
            sdk.init(map, context);
        }
    }

    private static class MtgInterstitialAdListener implements InterstitialVideoListener {

        private InterstitialAdCallback mCallback;

        MtgInterstitialAdListener(InterstitialAdCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onLoadSuccess(String s) {

        }

        @Override
        public void onVideoLoadSuccess(String s) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onVideoLoadFail(String s) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadFailed(s);
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
                mCallback.onInterstitialAdShowFailed(s);
            }
        }

        @Override
        public void onVideoAdClicked(String s) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClick();
            }
        }

        @Override
        public void onVideoComplete(String s) {
        }

        @Override
        public void onEndcardShow(String s) {
        }
    }

    private static class MtgRvAdListener implements RewardVideoListener {

        private RewardedVideoCallback mRvCallback;

        MtgRvAdListener(RewardedVideoCallback callback) {
            mRvCallback = callback;
        }

        @Override
        public void onVideoLoadSuccess(String s) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onLoadSuccess(String s) {

        }

        @Override
        public void onVideoLoadFail(String s) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadFailed(s);
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
        public void onAdClose(boolean b, String s, float v) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onShowFail(String s) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdShowFailed(s);
            }
        }

        @Override
        public void onVideoAdClicked(String s) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onVideoComplete(String s) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onEndcardShow(String s) {

        }
    }
}
