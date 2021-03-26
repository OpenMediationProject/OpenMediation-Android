// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedAdListener;
import com.mopub.mobileads.MoPubRewardedAdManager;
import com.mopub.mobileads.MoPubRewardedAds;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomAdsAdapter;
import com.openmediation.sdk.mediation.InterstitialAdCallback;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.RewardedVideoCallback;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MoPubAdapter extends CustomAdsAdapter implements MoPubRewardedAdListener
        , MoPubInterstitial.InterstitialAdListener {
    private static final String TAG = "OM-MoPub";
    private static final String TP_PARAM = "imext";

    private volatile InitState mInitState = InitState.NOT_INIT;
    private final ConcurrentMap<String, RewardedVideoCallback> mRvCallback;
    private final ConcurrentMap<String, MoPubInterstitial> mInterstitialAds;
    private final ConcurrentMap<MoPubInterstitial, InterstitialAdCallback> mIsCallback;


    private final MoPubRewardedAdManager.RequestParameters mRequestParameters;
    private String mShowingId;


    public MoPubAdapter() {
        mRvCallback = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mIsCallback = new ConcurrentHashMap<>();
        // adjustment requested by MoPub to be able to report on this incremental supply
        mRequestParameters = new MoPubRewardedAdManager.RequestParameters(TP_PARAM);
    }

    @Override
    public String getMediationVersion() {
        return MoPub.SDK_VERSION;
    }

    @Override
    public String getAdapterVersion() {
        return com.openmediation.sdk.mobileads.mopub.BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_9;
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        MoPub.onCreate(activity);
        MoPub.onStart(activity);
        MoPub.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        MoPub.onPause(activity);
        MoPub.onStop(activity);
    }

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        if (mInitState == InitState.INIT_SUCCESS) {
            PersonalInfoManager manager = MoPub.getPersonalInformationManager();
            if (manager == null) {
                return;
            }
            if (consent) {
                manager.grantConsent();
            } else {
                manager.revokeConsent();
            }
        }
    }

    private void initSDK(final Activity activity, String pid) {
        mInitState = InitState.INIT_PENDING;
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(pid).build();
        MoPub.initializeSdk(activity, sdkConfiguration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                MoPubRewardedAds.setRewardedAdListener(MoPubAdapter.this);
                for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallback.entrySet()) {
                    if (videoCallbackEntry != null) {
                        videoCallbackEntry.getValue().onRewardedVideoInitSuccess();
                    }
                }

                for (Map.Entry<MoPubInterstitial, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                    if (interstitialAdCallbackEntry != null) {
                        interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
                    }
                }
                mInitState = InitState.INIT_SUCCESS;
                if (mUserConsent != null) {
                    setGDPRConsent(activity, mUserConsent);
                }
            }
        });
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String pid = "";
        if (dataMap.get("pid") != null) {
            pid = (String) dataMap.get("pid");
        }
        String error = MoPubUtil.check(activity, pid);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    mRvCallback.put(pid, callback);
                    initSDK(activity, pid);
                    break;
                case INIT_PENDING:
                    mRvCallback.put(pid, callback);
                    break;
                case INIT_SUCCESS:
                    callback.onRewardedVideoInitSuccess();
                    break;
                case INIT_FAIL:
                    callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MoPub initRewardedVideo failed"));
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        String error = MoPubUtil.check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!mRvCallback.containsKey(adUnitId)) {
                mRvCallback.put(adUnitId, callback);
            }
            if (MoPubRewardedAds.hasRewardedAd(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                MoPubRewardedAds.loadRewardedAd(adUnitId, mRequestParameters);
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = MoPubUtil.check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, error));
            }
            return;
        }
        if (isRewardedVideoAvailable(adUnitId)) {
            if (!mRvCallback.containsKey(adUnitId)) {
                mRvCallback.put(adUnitId, callback);
            }
            MoPubRewardedAds.showRewardedAd(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, "MoPub ad not ready to show"));
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return MoPubRewardedAds.hasRewardedAd(adUnitId);
    }

    @Override
    public void onRewardedAdLoadSuccess(String adUnitId) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    @Override
    public void onRewardedAdLoadFailure(String adUnitId, MoPubErrorCode errorCode) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, errorCode.name()));
        }
    }

    @Override
    public void onRewardedAdStarted(String adUnitId) {
        mShowingId = adUnitId;
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
            callback.onRewardedVideoAdStarted();
        }
    }

    @Override
    public void onRewardedAdShowError(String adUnitId, MoPubErrorCode errorCode) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(AdapterErrorBuilder.buildShowError(
                    AdapterErrorBuilder.AD_UNIT_REWARDED_VIDEO, mAdapterName, errorCode.name()));
        }
    }

    @Override
    public void onRewardedAdClicked(String adUnitId) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    @Override
    public void onRewardedAdClosed(String adUnitId) {
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onRewardedAdCompleted(Set<String> adUnitIds, MoPubReward reward) {
        if (!TextUtils.isEmpty(mShowingId)) {
            RewardedVideoCallback callback = mRvCallback.get(mShowingId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdRewarded();
            }
        }
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String pid = "";
        if (dataMap.get("pid") != null) {
            pid = (String) dataMap.get("pid");
        }
        String error = MoPubUtil.check(activity, pid);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    mIsCallback.put(getInterstitialAd(activity, pid), callback);
                    initSDK(activity, pid);
                    break;
                case INIT_PENDING:
                    mIsCallback.put(getInterstitialAd(activity, pid), callback);
                    break;
                case INIT_SUCCESS:
                    callback.onInterstitialAdInitSuccess();
                    break;
                case INIT_FAIL:
                    callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "MoPub initInterstitialAd failed"));
                    break;
                default:
                    break;
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, Map<String, Object> extras, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        String error = MoPubUtil.check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            MoPubInterstitial interstitial = getInterstitialAd(activity, adUnitId);
            if (!mIsCallback.containsKey(interstitial)) {
                mIsCallback.put(interstitial, callback);
            }
            if (interstitial.isReady()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                interstitial.load();
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadCheckError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = MoPubUtil.check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, error));
            }
            return;
        }
        if (isInterstitialAdAvailable(adUnitId)) {
            MoPubInterstitial interstitial = getInterstitialAd(activity, adUnitId);
            if (!mIsCallback.containsKey(interstitial)) {
                mIsCallback.put(interstitial, callback);
            }
            mShowingId = adUnitId;
            interstitial.show();
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(AdapterErrorBuilder.buildShowError(
                        AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, "MoPub interstitial is not ready"));
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        MoPubInterstitial interstitial = mInterstitialAds.get(adUnitId);
        return interstitial != null && interstitial.isReady();
    }

    private MoPubInterstitial getInterstitialAd(Activity activity, String adUnitId) {
        MoPubInterstitial interstitialAd = mInterstitialAds.get(adUnitId);
        if (interstitialAd == null) {
            interstitialAd = new MoPubInterstitial(activity, adUnitId);
            interstitialAd.setInterstitialAdListener(this);
            mInterstitialAds.put(adUnitId, interstitialAd);
        }
        return interstitialAd;
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_INTERSTITIAL, mAdapterName, errorCode.name()));
        }
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdClick();
        }
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
        if (interstitial != null) {
            mIsCallback.remove(interstitial);
            MoPubInterstitial mi = mInterstitialAds.get(mShowingId);
            if (mi == interstitial) {
                mi.destroy();
                mInterstitialAds.remove(mShowingId);
            }
        }
    }

    /**
     * MoPub sdk init state
     */
    private enum InitState {
        /**
         *
         */
        NOT_INIT,
        /**
         *
         */
        INIT_PENDING,
        /**
         *
         */
        INIT_SUCCESS,
        /**
         *
         */
        INIT_FAIL
    }
}
