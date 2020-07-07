package com.nbmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.adsgreat.base.callback.VideoAdLoadListener;
import com.adsgreat.base.config.Const;
import com.adsgreat.base.core.AGError;
import com.adsgreat.base.core.AGVideo;
import com.adsgreat.base.core.AdsgreatSDK;
import com.adsgreat.video.core.AdsGreatVideo;
import com.adsgreat.video.core.RewardedVideoAdListener;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.plugin1.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by jiantao.tu on 2020/5/14.
 */
public class Plugin1Adapter extends CustomAdsAdapter {

    private static String TAG = "OM-Cloudmobi-Plugin1: ";

    private ConcurrentMap<String, AGVideo> mRvAds;

    private AtomicBoolean isPreload = new AtomicBoolean();


    public Plugin1Adapter() {
        mRvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return Const.getVersionNumber();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_32;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        Object appKey = dataMap.get("AppKey");
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            if (appKey instanceof String) {
                AdsgreatSDK.initialize(activity, (String) appKey);
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
                return;
            }
        }
        if (callback != null) {
            callback.onRewardedVideoInitFailed(error);
        }

    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        loadRvAd(activity, adUnitId, callback);

    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
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
        AGVideo agVideo = mRvAds.get(adUnitId);
        if (agVideo != null) {
            AdsGreatVideo.showRewardedVideo(agVideo, new VideoAdListenerImpl(callback));
            isPreload.set(false);
            mRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(TAG + "RewardedVideo is not ready");
            }
        }
    }


    public static class VideoAdListenerImpl extends RewardedVideoAdListener {

        RewardedVideoCallback mCallback;

        VideoAdListenerImpl(RewardedVideoCallback callback) {
            mCallback = callback;
        }

        @Override
        public void videoStart() {
            AdLog.getSingleton().LogD(TAG + "videoStart: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdStarted();
                mCallback.onRewardedVideoAdShowSuccess();
            }
        }

        @Override
        public void videoFinish() {
            AdLog.getSingleton().LogD(TAG + "videoFinish: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void videoError(Exception e) {
            AdLog.getSingleton().LogD(TAG + "onAdFailed: " + e.getMessage());
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed(TAG + " RewardedVideo load failed : " + e.getMessage());
            }
        }

        @Override
        public void videoClosed() {
            AdLog.getSingleton().LogD(TAG + "videoClosed: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void videoClicked() {
            AdLog.getSingleton().LogD(TAG + "videoClicked: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void videoRewarded(String rewardName, String rewardAmount) {
            AdLog.getSingleton().LogD(TAG + "videoRewarded: rewardName=" + rewardName + ",rewardAmount=" + rewardAmount);
            if (mCallback != null) {
                mCallback.onRewardedVideoAdRewarded();
            }

        }
    }

    private void loadRvAd(Context activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!isPreload.compareAndSet(false, true)) {
                callback.onRewardedVideoLoadFailed(TAG + "ad loading, no need to load repeatedly");
                return;
            }
            AGVideo zcVideo = mRvAds.get(adUnitId);
            if (zcVideo == null) {
                realLoadRvAd(activity, adUnitId, callback);
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

    private void realLoadRvAd(Context activity, final String adUnitId, RewardedVideoCallback callback) {
        VideoAdLoadListener videoAdLoadListener = create(adUnitId, callback);
        AdsGreatVideo.preloadRewardedVideo(activity, adUnitId, videoAdLoadListener);
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }

        AGVideo video = mRvAds.get(adUnitId);
        if (video == null) return false;

        return AdsGreatVideo.isRewardedVideoAvailable(video);
    }

    private VideoAdLoadListener create(final String adUnitId, final RewardedVideoCallback callback) {

        return new VideoAdLoadListener() {

            @Override
            public void onVideoAdLoadSucceed(AGVideo zcVideo) {
                AdLog.getSingleton().LogD(TAG + "onVideoAdLoadSucceed: ");
                mRvAds.put(adUnitId, zcVideo);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            @Override
            public void onVideoAdLoadFailed(AGError zcError) {
                isPreload.set(false);
                String message = "";
                if (zcError != null) {
                    message = zcError.getMsg();
                }
                AdLog.getSingleton().LogD(TAG + "onAdFailed: " + message);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed : " + message);
                }
            }
        };
    }

}
