package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.ogury.core.OguryError;
import com.ogury.ed.OguryInterstitialAd;
import com.ogury.ed.OguryInterstitialAdListener;
import com.ogury.ed.OguryOptinVideoAd;
import com.ogury.ed.OguryOptinVideoAdListener;
import com.ogury.ed.OguryReward;
import com.ogury.sdk.Ogury;
import com.ogury.sdk.OguryConfiguration;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;
import com.openmediation.sdk.mobileads.ougry.BuildConfig;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class OguryAdapter extends CustomAdsAdapter {

    private static final String TAG = "OguryAdapter ";

    private final ConcurrentMap<String, OguryOptinVideoAd> mRewardedAds;
    private final ConcurrentMap<String, OguryInterstitialAd> mInterstitialAds;
    private final AtomicBoolean hasInit = new AtomicBoolean(false);

    public OguryAdapter() {
        mRewardedAds = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return Ogury.getSdkVersion();
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
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        OguryInterstitialAd interstitialAd = mInterstitialAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isLoaded();
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            AdLog.getSingleton().LogE(TAG + "插屏广告初始化成功...");
            initSdk(activity);
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        } else {
            AdLog.getSingleton().LogE(TAG + "插屏广告初始化失败...");
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(final Activity activity, final String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        AdLog.getSingleton().LogE(TAG + "插屏广告开始加载... ");
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        OguryInterstitialAd interstitialPlacement = new OguryInterstitialAd(activity.getApplicationContext(), adUnitId);
        mInterstitialAds.put(adUnitId, interstitialPlacement);
        interstitialPlacement.setListener(createInterstitialListener(adUnitId, callback));
        interstitialPlacement.load();
    }

    private OguryInterstitialAdListener createInterstitialListener(final String adUnitId, final InterstitialAdCallback callback) {
        return new OguryInterstitialAdListener() {

            @Override
            public void onAdLoaded() {
                AdLog.getSingleton().LogE(TAG + "插屏广告已加载好");
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            }

            @Override
            public void onAdDisplayed() {
                AdLog.getSingleton().LogE(TAG + "插屏广告展示");
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            public void onAdClicked() {
                if (callback != null) {
                    callback.onInterstitialAdClick();
                }
            }

            @Override
            public void onAdClosed() {
                AdLog.getSingleton().LogE(TAG + "插屏广告关闭");
                mInterstitialAds.remove(adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }

            @Override
            public void onAdError(OguryError oguryError) {
                AdLog.getSingleton().LogE(TAG + "插屏广告加载出错");
                mInterstitialAds.remove(adUnitId);
                if (callback != null) {
                    AdapterError adapterError = AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, oguryError.getErrorCode(), oguryError.getMessage());
                    callback.onInterstitialAdLoadFailed(adapterError);
                }
            }

        };
    }

    @Override
    public void showInterstitialAd(final Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (!isInterstitialAdAvailable(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad not ready"));
            }
            return;
        }
        OguryInterstitialAd ad = mInterstitialAds.get(adUnitId);
        if (ad != null) {
            ad.show();
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "ad not ready"));
            }
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSdk(activity);
            if (callback != null) {
                AdLog.getSingleton().LogE(TAG + "激励广告初始化成功...");
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            if (callback != null) {
                AdLog.getSingleton().LogE(TAG + "激励广告初始化失败...");
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        OguryOptinVideoAd videoAd = mRewardedAds.get(adUnitId);
        return videoAd != null && videoAd.isLoaded();
    }

    @Override
    public void loadRewardedVideo(final Activity activity, final String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        AdLog.getSingleton().LogE(TAG + "激励广告开始加载...");
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        OguryOptinVideoAd rewardedPlacement = new OguryOptinVideoAd(activity.getApplicationContext(), adUnitId);
        rewardedPlacement.setListener(createRvLoadListener(adUnitId, callback));
        mRewardedAds.put(adUnitId, rewardedPlacement);
        rewardedPlacement.load();
    }

    private void initSdk(Activity activity) {
        if (!hasInit.get()) {
            OguryConfiguration.Builder configurationBuilder = new OguryConfiguration.Builder(activity.getApplicationContext(), mAppKey);
            Ogury.start(configurationBuilder.build());
            hasInit.set(true);
        }
    }

    @Override
    public void showRewardedVideo(final Activity activity, final String adUnitId, final RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (!isRewardedVideoAvailable(adUnitId)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
            }
            return;
        }
        OguryOptinVideoAd ad = mRewardedAds.get(adUnitId);
        if (ad != null) {
            ad.show();
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "Not Ready"));
            }
        }
    }

    private OguryOptinVideoAdListener createRvLoadListener(final String adUnitId, final RewardedVideoCallback callback) {
        return new OguryOptinVideoAdListener() {

            @Override
            public void onAdLoaded() {
                AdLog.getSingleton().LogE(TAG + "激励广告加载成功...");
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            @Override
            public void onAdDisplayed() {
                AdLog.getSingleton().LogE(TAG + "激励广告显示...");
                if (callback != null) {
                    callback.onRewardedVideoAdShowSuccess();
                    callback.onRewardedVideoAdStarted();
                }
            }

            @Override
            public void onAdClicked() {
                if (callback != null) {
                    callback.onRewardedVideoAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
                AdLog.getSingleton().LogE(TAG + "激励广告关闭...");
                mRewardedAds.remove(adUnitId);
                if (callback != null) {
                    callback.onRewardedVideoAdEnded();
                    callback.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdError(OguryError oguryError) {
                AdLog.getSingleton().LogE(TAG + "激励广告加载错误...错误码：" + oguryError);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, oguryError.getErrorCode(), oguryError.getMessage()));
                }
            }

            @Override
            public void onAdRewarded(OguryReward oguryReward) {
                AdLog.getSingleton().LogE(TAG + "获取奖励...");
                if (callback != null) {
                    callback.onRewardedVideoAdRewarded();
                }
            }
        };
    }
}
