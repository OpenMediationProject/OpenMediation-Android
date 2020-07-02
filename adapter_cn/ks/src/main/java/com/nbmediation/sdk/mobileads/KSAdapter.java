// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.kwad.sdk.KsAdSDK;
import com.kwad.sdk.SdkConfig;
import com.kwad.sdk.export.i.IAdRequestManager;
import com.kwad.sdk.export.i.KsRewardVideoAd;
import com.kwad.sdk.protocol.model.AdScene;
import com.kwad.sdk.video.VideoPlayConfig;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.ks.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KSAdapter extends CustomAdsAdapter {
    private static String TAG = "OM-KS: ";
    private ConcurrentMap<String, KsRewardVideoAd> mTTRvAds;

    public KSAdapter() {
        mTTRvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return KsAdSDK.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_22;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            String[] split = mAppKey.split("\\|");
            String appId = split[0];
            String appName = null;
            boolean isDebug = false;
            if (split.length > 1) {
                appName = split[1];
            }
            if (split.length > 2) {
                try {
                    isDebug = Boolean.parseBoolean(split[2]);
                } catch (Exception ignored) {
                }
            }
            initSdk(activity, appId, appName, isDebug);
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
            KsRewardVideoAd rewardedVideoAd = mTTRvAds.get(adUnitId);
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
        KsRewardVideoAd rewardedVideoAd = mTTRvAds.get(adUnitId);
        if (rewardedVideoAd != null) {
            VideoPlayConfig videoPlayConfig = new VideoPlayConfig.Builder().showLandscape(true) // 横屏播放
                    .build();
            rewardedVideoAd.setRewardAdInteractionListener(new InnerRvAdShowListener(callback));
            rewardedVideoAd.showRewardVideoAd((Activity) activity, videoPlayConfig);
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
        KsRewardVideoAd rewardedVideoAd = mTTRvAds.get(adUnitId);
        return rewardedVideoAd != null && rewardedVideoAd.isAdEnable();
    }


    private void initSdk(final Context activity, String appId, String appName, boolean isDebug) {
        KsAdSDK.init(activity, new SdkConfig.Builder()
                .appId(appId) // 测试aapId，请联系快⼿手平台申请正式AppId，必填
                .appName(appName) // 测试appName，请填写您应⽤用的名称，⾮非必填
                .showNotification(true) // 是否展示下载通知栏 .debug(true)
                .debug(isDebug) // 是否开启sdk 调试⽇日志 可选
                .build());

    }


    private void realLoadRvAd(final String adUnitId, final RewardedVideoCallback rvCallback) {
        long postId = 0;
        try {
            postId = Long.parseLong(adUnitId);
        } catch (NumberFormatException e) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoLoadFailed(TAG + "adUnitId format error, adUnitId is " + adUnitId);
            }
        }

        AdScene scene = new AdScene(postId); // 此为测试posId， 请联系快⼿手平台申请正式posId
        IAdRequestManager adRequestManager = KsAdSDK.getAdManager();
        if (adRequestManager != null) {
            KsAdSDK.getAdManager().loadRewardVideoAd(scene, new InnerLoadRvAdListener(rvCallback, adUnitId, mTTRvAds));
        } else {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoLoadFailed(TAG + "init error,getAdManager is null");
            }
        }
    }


    private static class InnerLoadRvAdListener implements IAdRequestManager.RewardVideoAdListener {

        private RewardedVideoCallback mCallback;
        private String mCodeId;
        private ConcurrentMap<String, KsRewardVideoAd> mTTRvAds;

        private InnerLoadRvAdListener(RewardedVideoCallback callback, String codeId, ConcurrentMap<String, KsRewardVideoAd> tTRvAds) {
            this.mCallback = callback;
            this.mCodeId = codeId;
            this.mTTRvAds = tTRvAds;
        }

        @Override
        public void onError(int code, String message) {
            AdLog.getSingleton().LogD(TAG + "RewardedVideo  onError: " + code + ", " + message);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed : " + code + ", " + message);
            }
        }

        @Override
        public void onRewardVideoAdLoad(List<KsRewardVideoAd> list) {
            if (list == null || list.size() == 0) {
                if (mCallback != null) {
                    mCallback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed");
                }
                return;
            }
            mTTRvAds.put(mCodeId, list.get(0));
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadSuccess();
            }
            AdLog.getSingleton().LogD(TAG + "rewardedVideo  onRewardVideoAdLoad");
        }

    }

    private static class InnerRvAdShowListener implements KsRewardVideoAd.RewardAdInteractionListener {

        private RewardedVideoCallback callback;

        private InnerRvAdShowListener(RewardedVideoCallback callback) {
            this.callback = callback;
        }


        @Override
        public void onAdClicked() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd click");
            if (callback != null) {
                callback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onPageDismiss() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd close");
            if (callback != null) {
                callback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onVideoPlayError(int code, int extra) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd error, code=" + code + ",extra=" + extra);
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(TAG + "rewardedVideo play failed, code=" + code + ",extra=" + extra);
            }
        }

        @Override
        public void onVideoPlayEnd() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoPlayEnd");
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onVideoPlayStart() {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoPlayStart");
            if (callback != null) {
                callback.onRewardedVideoAdShowSuccess();
                callback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onRewardVerify() {
            if (callback != null) {
                callback.onRewardedVideoAdRewarded();
            }
        }
    }

}
