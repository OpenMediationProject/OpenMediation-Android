// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.zyt.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;
import com.zyt.mediation.RewardAdLoadListener;
import com.zyt.mediation.RewardAdResponse;
import com.zyt.mediation.RewardAdShowListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import mobi.android.RewardAd;
import mobi.android.ZYTMediationSDK;

public class ZYTAdapter extends CustomAdsAdapter {

    private static String TAG = "OM-ZYT: ";
    private ConcurrentMap<String, RewardAdResponse> mTTRvAds;

    public ZYTAdapter() {
        mTTRvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_21;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            String[] split = mAppKey.split("\\|");
            String appId = split[0];
            String pubKey = null;
            if (split.length > 1) {
                pubKey = split[1];
            }
            initSdk(activity, appId, pubKey);
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    private void loadRvAd(Context activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            RewardAdResponse rewardedVideoAd = mTTRvAds.get(adUnitId);
            if (rewardedVideoAd == null) {
                realLoadRvAd(adUnitId, callback);
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
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
        RewardAdResponse rewardedVideoAd = mTTRvAds.get(adUnitId);
        if (rewardedVideoAd != null) {
            RewardAd.show(adUnitId, new InnerRvAdShowListener(callback));
            mTTRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(TAG + "RewardedVideo is not ready");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mTTRvAds.get(adUnitId) != null && RewardAd.isReady(adUnitId);
    }


    private void initSdk(final Context activity, String appId, String pubKey) {
        ZYTMediationSDK.initSdk(activity, appId, pubKey);
    }


    private void realLoadRvAd(final String adUnitId, final RewardedVideoCallback rvCallback) {
        RewardAd.loadAd(adUnitId, new InnerLoadRvAdListener(rvCallback, adUnitId, mTTRvAds));
    }

    private static class InnerLoadRvAdListener implements RewardAdLoadListener {

        private RewardedVideoCallback mCallback;
        private String mCodeId;
        private ConcurrentMap<String, RewardAdResponse> mTTRvAds;

        private InnerLoadRvAdListener(RewardedVideoCallback callback, String codeId, ConcurrentMap<String, RewardAdResponse> tTRvAds) {
            this.mCallback = callback;
            this.mCodeId = codeId;
            this.mTTRvAds = tTRvAds;
        }

        @Override
        public void onLoaded(String s, RewardAdResponse rewardAdResponse) {
            if (rewardAdResponse == null) {
                if (mCallback != null) {
                    mCallback.onRewardedVideoLoadFailed(TAG +"RewardedVideo load failed");
                }
                return;
            }
            mTTRvAds.put(mCodeId, rewardAdResponse);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadSuccess();
            }
            AdLog.getSingleton().LogD(TAG + "rewardedVideo  onLoaded");
        }

        @Override
        public void onError(String adUnitId, String message) {
            AdLog.getSingleton().LogD(TAG + "RewardedVideo  onError: " + adUnitId + ", " + message);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed(TAG+"FRewardedVideo load failed : " + adUnitId + ", " + message);
            }
        }
    }

    private static class InnerRvAdShowListener implements RewardAdShowListener {

        private RewardedVideoCallback callback;

        private InnerRvAdShowListener(RewardedVideoCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onADShow(String s) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd show");
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onADClick(String s) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd bar click");
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onADFinish(String adUnitId, boolean isReward) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd complete");
            if (callback != null) {
                if (isReward) {
                    callback.onRewardedVideoAdRewarded();
                }
                callback.onRewardedVideoAdClosed();
                callback.onRewardedVideoAdEnded();
            }
        }
    }

}
