package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.nbmediation.sdk.mobileads.ironsource.BuildConfig;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class IronSourceAdapter extends CustomAdsAdapter
{

    private static final String TAG = "AdTiming-IronSource";

    private static AtomicBoolean mDidInitInterstitial = new AtomicBoolean(false);

    private final static List<IronSource.AD_UNIT> mIsAdUnitsToInit =
            new ArrayList<>(Collections.singletonList(IronSource.AD_UNIT.INTERSTITIAL));

    private static AtomicBoolean mDidInitRewardedVideo = new AtomicBoolean(false);

    private final static List<IronSource.AD_UNIT> mRvAdUnitsToInit =
            new ArrayList<>(Collections.singletonList(IronSource.AD_UNIT.REWARDED_VIDEO));

    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    enum INSTANCE_STATE {
        START, //Initial state when instance wasn't loaded yet
        CAN_LOAD, //If load is called on an instance with this state, pass it forward to IronSource SDK
        LOCKED, //if load is called on an instance with this state, report load fail
    }

    public IronSourceAdapter() {
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return IronSourceUtils.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_15;
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        IronSource.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        IronSource.onPause(activity);
        super.onPause(activity);
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
            return;
        }
        if (!mDidInitRewardedVideo.getAndSet(true)) {
            IronSourceManager.getInstance().initIronSourceSDK(activity, mAppKey, mRvAdUnitsToInit);
        }
        if (callback != null) {
            callback.onRewardedVideoInitSuccess();
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (isRewardedVideoAvailable(adUnitId) && callback != null) {
                callback.onRewardedVideoLoadSuccess();
                return;
            }
            if (callback != null) {
                mRvCallbacks.put(adUnitId, callback);
            }
            IronSourceManager.getInstance().loadRewardedVideo(adUnitId, new WeakReference<>(IronSourceAdapter.this));
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(checkError);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                mRvCallbacks.put(adUnitId, callback);
            }
            IronSourceManager.getInstance().showRewardedVideo(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(checkError);
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return IronSourceManager.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
            return;
        }
        if (!mDidInitInterstitial.getAndSet(true)) {
            IronSourceManager.getInstance().initIronSourceSDK(activity, mAppKey, mIsAdUnitsToInit);
        }
        if (callback != null) {
            callback.onInterstitialAdInitSuccess();
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (isInterstitialAdAvailable(adUnitId) && callback != null) {
                callback.onInterstitialAdLoadSuccess();
                return;
            }
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            IronSourceManager.getInstance().loadInterstitial(adUnitId, new WeakReference<>(IronSourceAdapter.this));
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(checkError);
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            IronSourceManager.getInstance().showInterstitial(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(checkError);
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return IronSourceManager.getInstance().isInterstitialReady(adUnitId);
    }

    //region ISDemandOnlyInterstitialListener implementation.
    void onInterstitialAdReady(String instanceId) {
        AdLog.getSingleton().LogD(TAG, String.format("IronSource Interstitial loaded successfully for instance %s "
                , instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    void onInterstitialAdLoadFailed(String instanceId, IronSourceError ironSourceError) {
        Log.e(TAG, String.format("IronSource Interstitial failed to load for instance %s  with Error: %s"
                , instanceId, ironSourceError.getErrorMessage()));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(ironSourceError.toString());
        }
    }

    void onInterstitialAdOpened(String instanceId) {
        Log.d(TAG, String.format("IronSource Interstitial opened ad for instance %s",
                instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    void onInterstitialAdClosed(String instanceId) {
        Log.d(TAG, String.format("IronSource Interstitial closed ad for instance %s",
                instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    void onInterstitialAdShowFailed(String instanceId, IronSourceError ironSourceError) {
        Log.e(TAG, String.format("IronSource Interstitial failed to show " +
                "for instance %s with Error: %s", instanceId, ironSourceError.getErrorMessage()));
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdShowFailed(ironSourceError.toString());
        }
    }

    void onInterstitialAdClicked(String instanceId) {
        Log.d(TAG, String.format("IronSource Interstitial ad clicked for instance %s",
                instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdClick();
        }
    }

    /**
     * IronSource callbacks for AdMob Mediation.
     */

    void onRewardedVideoAdLoadSuccess(String instanceId) {
        Log.d(TAG, String.format("IronSource load success for instanceId: %s", instanceId));
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    void onRewardedVideoAdLoadFailed(String instanceId, IronSourceError ironSourceError) {
        final String message = String.format("IronSource Rewarded Video failed to load for instance %s with Error: %s",
                instanceId, ironSourceError.getErrorMessage());
        Log.d(TAG, message);
        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(message);
        }
    }

    void onRewardedVideoAdOpened(final String instanceId) {
        Log.d(TAG, String.format("IronSource Rewarded Video opened ad for instance %s",
                instanceId));

        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
            callback.onRewardedVideoAdStarted();
        }
    }

    void onRewardedVideoAdClosed(String instanceId) {
        Log.d(TAG, String.format("IronSource Rewarded Video closed ad for instance %s",
                instanceId));

        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    void onRewardedVideoAdRewarded(String instanceId) {
        Log.d(TAG, String.format("IronSource Rewarded Video received reward for instance %s", instanceId));

        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdRewarded();
        }
    }

    void onRewardedVideoAdShowFailed(final String instanceId, IronSourceError ironsourceError) {
        final String message = String.format("IronSource Rewarded Video failed to show for instance %s with Error: %s",
                instanceId, ironsourceError.getErrorMessage());
        Log.w(TAG, message);

        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(message);
        }
    }

    void onRewardedVideoAdClicked(String instanceId) {
        Log.d(TAG, String.format("IronSource Rewarded Video clicked for instance %s",
                instanceId));

        RewardedVideoCallback callback = mRvCallbacks.get(instanceId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }
}
