package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.hytt.hyadxopensdk.HyAdXOpenSdk;
import com.hytt.hyadxopensdk.hyadxopenad.HyAdXOpenMotivateVideoAd;
import com.hytt.hyadxopensdk.interfoot.HyAdXOpenListener;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.hyadxopen.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by jiantao.tu on 2020/5/14.
 */
public class HyAdXOpenAdapter extends CustomAdsAdapter {

    private static String TAG = "OM-HyAdXOpen: ";
    private ConcurrentMap<String, HyAdXOpenMotivateVideoAd> mRvAds;


    public HyAdXOpenAdapter() {
        mRvAds = new ConcurrentHashMap<>();
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
        return MediationInfo.MEDIATION_ID_18;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        Object appKey = dataMap.get("AppKey");
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            if (appKey instanceof String) {
                HyAdXOpenSdk.getInstance().init(activity, (String) appKey);
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
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        loadRvAd(activity, adUnitId, callback);

    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(error);
            }
            return;
        }
        HyAdXOpenMotivateVideoAd rewardedVideoAd = mRvAds.get(adUnitId);
        if (rewardedVideoAd != null) {
            rewardedVideoAd.show();
            mRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("HyAdXOpen RewardedVideo is not ready");
            }
        }
    }


    private void loadRvAd(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            HyAdXOpenMotivateVideoAd rewardedVideoAd = mRvAds.get(adUnitId);
            if (rewardedVideoAd == null) {
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

    private void realLoadRvAd(Activity activity, final String adUnitId, RewardedVideoCallback callback) {
        HyAdXOpenMotivateVideoAd hyAdXOpenMotivateVideoAd = create(activity, adUnitId, callback);
        hyAdXOpenMotivateVideoAd.load();
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mRvAds.get(adUnitId) != null;
    }

    private HyAdXOpenMotivateVideoAd create(final Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        final HyAdXOpenMotivateVideoAd[] hyAdXOpenMotivateVideoAds = new HyAdXOpenMotivateVideoAd[1];
        hyAdXOpenMotivateVideoAds[0] = new HyAdXOpenMotivateVideoAd(activity,
                adUnitId,
                new HyAdXOpenListener() {
                    @Override
                    public void onAdFill(int code, final String searchId, View view) {
                        AdLog.getSingleton().LogD(TAG + "onAdFill: " + searchId);
                        mRvAds.put(adUnitId, hyAdXOpenMotivateVideoAds[0]);
                        if (callback != null) {
                            callback.onRewardedVideoLoadSuccess();
                        }
                    }

                    @Override
                    public void onAdShow(int code, String searchId) {
                        AdLog.getSingleton().LogD(TAG + "onAdShow: ");
                        if (callback != null) {
                            callback.onRewardedVideoAdShowSuccess();
                        }
                    }

                    @Override
                    public void onAdClick(int code, String searchId) {
                        AdLog.getSingleton().LogD(TAG + "onAdClick: ");
                        if (callback != null) {
                            callback.onRewardedVideoAdClicked();
                        }
                    }

                    @Override
                    public void onAdClose(int code, String searchId) {
                        AdLog.getSingleton().LogD(TAG + "onAdClose: ");
                        if (callback != null) {
                            callback.onRewardedVideoAdClosed();
                        }
                    }

                    @Override
                    public void onAdFailed(int code, String message) {
                        AdLog.getSingleton().LogD(TAG + "onAdFailed: " + code + " " + message);
                        if (callback != null) {
                            callback.onRewardedVideoLoadFailed("HyAdXOpen RewardedVideo load failed : " + code + ", " + message);
                        }
                    }

                    @Override
                    public void onVideoDownloadSuccess(int code, String searchId) {
                        AdLog.getSingleton().LogD(TAG + "onVideoDownloadSuccess: ");
                    }

                    @Override
                    public void onVideoDownloadFailed(int code, String searchId) {
                        AdLog.getSingleton().LogD(TAG + "onVideoDownloadFailed");
                        if (callback != null) {
                            callback.onRewardedVideoAdShowFailed(" onVideoDownloadFailed");
                        }
                    }

                    @Override
                    public void onVideoPlayStart(int code, String searchId) {
                        AdLog.getSingleton().LogD(TAG + "onVideoPlayStart: ");
                        if (callback != null) {
                            callback.onRewardedVideoAdStarted();
                        }
                    }

                    @Override
                    public void onVideoPlayEnd(int code, String searchId) {
                        AdLog.getSingleton().LogD(TAG + "onVideoPlayEnd: ");
                        if (callback != null) {
                            callback.onRewardedVideoAdRewarded();
                            callback.onRewardedVideoAdEnded();
                        }
                    }
                });
        return hyAdXOpenMotivateVideoAds[0];
    }

}
