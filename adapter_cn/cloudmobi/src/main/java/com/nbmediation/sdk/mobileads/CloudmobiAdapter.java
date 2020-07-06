package com.nbmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.cloudmobi.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;
import com.suib.base.callback.VideoAdLoadListener;
import com.suib.base.config.Const;
import com.suib.base.core.SuibSDK;
import com.suib.base.core.ZCError;
import com.suib.base.core.ZCVideo;
import com.suib.video.core.RewardedVideoAdListener;
import com.suib.video.core.SuibVideo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by jiantao.tu on 2020/5/14.
 */
public class CloudmobiAdapter extends CustomAdsAdapter {

    private static String TAG = "OM-Cloudmobi: ";

    private ConcurrentMap<String, ZCVideo> mRvAds;


    public CloudmobiAdapter() {
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
        return MediationInfo.MEDIATION_ID_17;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        Object appKey = dataMap.get("AppKey");
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            if (appKey instanceof String) {
                SuibSDK.initialize(activity, (String) appKey);
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
        ZCVideo zcVideo = mRvAds.get(adUnitId);
        if (zcVideo != null) {
            SuibVideo.showRewardedVideo(zcVideo, new VideoAdListenerImpl(callback));
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
            ZCVideo zcVideo = mRvAds.get(adUnitId);
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
        SuibVideo.preloadRewardedVideo(activity, adUnitId, videoAdLoadListener);
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }

        ZCVideo video = mRvAds.get(adUnitId);
        if (video == null) return false;

        return SuibVideo.isRewardedVideoAvailable(video);
    }

    private VideoAdLoadListener create(final String adUnitId, final RewardedVideoCallback callback) {

        return new VideoAdLoadListener() {

            @Override
            public void onVideoAdLoadSucceed(ZCVideo zcVideo) {
                AdLog.getSingleton().LogD(TAG + "onVideoAdLoadSucceed: ");
                mRvAds.put(adUnitId, zcVideo);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            @Override
            public void onVideoAdLoadFailed(ZCError zcError) {
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
