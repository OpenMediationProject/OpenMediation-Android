// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.core.imp.interstitialad.IsManager;
import com.openmediation.sdk.core.imp.rewardedvideo.RvManager;
import com.openmediation.sdk.core.imp.splash.SplashAdManager;
import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.mediation.MediationInterstitialListener;
import com.openmediation.sdk.mediation.MediationRewardVideoListener;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.helper.IapHelper;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.openmediation.sdk.OmAds.AD_TYPE;

/**
 * The type Om manager.
 */
public final class OmManager implements InitCallback {
    private Map<String, IsManager> mIsManagers;
    private Map<String, RvManager> mRvManagers;
    private ConcurrentMap<String, Set<InterstitialAdListener>> mIsListeners;
    private ConcurrentMap<String, Set<RewardedVideoListener>> mRvListeners;

    private ConcurrentMap<String, MediationInterstitialListener> mMediationIsListeners;
    private ConcurrentMap<String, MediationRewardVideoListener> mMediationRvListeners;

    private ConcurrentLinkedQueue<String> mDelayLoadIs;
    private ConcurrentLinkedQueue<String> mDelayLoadRv;

    private List<AD_TYPE> mPreloadAdTypes;
    private AtomicBoolean mDidRvInit = new AtomicBoolean(false);
    private AtomicBoolean mDidIsInit = new AtomicBoolean(false);
    private boolean mIsInForeground = true;
    private static ConcurrentLinkedQueue<InitCallback> mInitCallbacks = new ConcurrentLinkedQueue<>();

    private static final class OmHolder {
        private static final OmManager INSTANCE = new OmManager();
    }

    /**
     * The enum Load type.
     */
    public enum LOAD_TYPE {
        /**
         * Unknown load type.
         */
        UNKNOWN(0),
        /**
         * Initialization
         */
        INIT(1),
        /**
         * Scheduled task
         */
        INTERVAL(2),
        /**
         * Ads close
         */
        CLOSE(3),
        /**
         * Manual
         */
        MANUAL(4);

        private int mValue;

        LOAD_TYPE(int value) {
            this.mValue = value;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public int getValue() {
            return this.mValue;
        }
    }

    private OmManager() {
        mIsManagers = new HashMap<>();
        mRvManagers = new HashMap<>();
        mIsListeners = new ConcurrentHashMap<>();
        mRvListeners = new ConcurrentHashMap<>();
        mPreloadAdTypes = new ArrayList<>();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static OmManager getInstance() {
        return OmHolder.INSTANCE;
    }

    /**
     * The actual init method
     *
     * @param activity required param
     * @param appKey   required param, current app's identifier with Om
     * @param channel  the Channel of App Store
     * @param callback the callback
     * @param types    Optional param, Ad types for preloading; preloads all if null
     */
    public void init(Activity activity, String appKey, String channel, InitCallback callback, AD_TYPE... types) {
        if (InitImp.isInit()) {
            if (callback != null) {
                callback.onSuccess();
            }
            //checks for preloading and scheduled tasks
            anotherInitCalledAfterInitSuccess(types);
            return;
        } else if (InitImp.isInitRunning()) {
            pendingInit(callback);
        } else {
            pendingInit(callback);
            InitImp.init(activity, appKey, channel, this);
        }

        //adds for use after initialization
        if (types != null && types.length > 0) {
            mPreloadAdTypes.addAll(Arrays.asList(types));
        }
    }

    /**
     * Pending init.
     *
     * @param callback the callback
     */
    void pendingInit(InitCallback callback) {
        if (mInitCallbacks != null && callback != null) {
            mInitCallbacks.add(callback);
        }
    }

    /**
     * On resume.
     *
     * @param activity the activity
     */
    public void onResume(Activity activity) {
        mIsInForeground = true;
        if (!mIsManagers.isEmpty()) {
            Set<Map.Entry<String, IsManager>> isEntrys = mIsManagers.entrySet();
            for (Map.Entry<String, IsManager> isManagerEntry : isEntrys) {
                if (isManagerEntry != null) {
                    isManagerEntry.getValue().onResume(activity);
                }
            }
        }
        if (!mRvManagers.isEmpty()) {
            Set<Map.Entry<String, RvManager>> rvEntrys = mRvManagers.entrySet();
            for (Map.Entry<String, RvManager> rvManagerEntry : rvEntrys) {
                if (rvManagerEntry != null) {
                    rvManagerEntry.getValue().onResume(activity);
                }
            }
        }
    }

    /**
     * On pause.
     *
     * @param activity the activity
     */
    public void onPause(Activity activity) {
        mIsInForeground = false;
        if (!mIsManagers.isEmpty()) {
            Set<Map.Entry<String, IsManager>> isEntrys = mIsManagers.entrySet();
            for (Map.Entry<String, IsManager> isManagerEntry : isEntrys) {
                if (isManagerEntry != null) {
                    isManagerEntry.getValue().onPause(activity);
                }
            }
        }
        if (!mRvManagers.isEmpty()) {
            Set<Map.Entry<String, RvManager>> rvEntrys = mRvManagers.entrySet();
            for (Map.Entry<String, RvManager> rvManagerEntry : rvEntrys) {
                if (rvManagerEntry != null) {
                    rvManagerEntry.getValue().onPause(activity);
                }
            }
        }
    }

    /**
     * Is init running boolean.
     *
     * @return the boolean
     */
    public boolean isInitRunning() {
        return InitImp.isInitRunning();
    }

    /**
     * Is init boolean.
     *
     * @return the boolean
     */
    public boolean isInit() {
        return InitImp.isInit();
    }

    /**
     * Sets iap.
     *
     * @param iapCount the iap count
     * @param currency the currency
     */
    public void setIAP(float iapCount, String currency) {
        IapHelper.setIap(iapCount, currency);
    }

    /**
     * Gets sdk version.
     *
     * @return the sdk version
     */
    public String getSDKVersion() {
        return CommonConstants.SDK_VERSION_NAME;
    }

    public boolean isInForeground() {
        return mIsInForeground;
    }

    /**
     * Is scene capped boolean.
     *
     * @param placementId the placement id
     * @param sceneName   the scene name
     * @return the boolean
     */
    public boolean isSceneCapped(String placementId, String sceneName) {
        return BaseOmAds.isSceneCapped(placementId, sceneName);
    }

    /**
     * only one listener will exist in the whole lifecycle
     *
     * @param placementId the placement id
     * @param listener    InterstitialAd listener
     */
    public void setInterstitialAdListener(String placementId, InterstitialAdListener listener) {
        addInterstitialAdListener(placementId, listener);
    }

    public void addInterstitialAdListener(String placementId, InterstitialAdListener listener) {
        addIsAdListenerImp(placementId, listener, false);
    }

    private void addIsAdListenerImp(String placementId, InterstitialAdListener listener, boolean reAdd) {
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.addInterstitialAdListener(listener);
        } else {
            if (reAdd) {
                return;
            }
            if (mIsListeners == null) {
                mIsListeners = new ConcurrentHashMap<>();
            }
            Set<InterstitialAdListener> isListeners = mIsListeners.get(placementId);
            if (isListeners == null) {
                isListeners = new HashSet<>();
            }
            isListeners.add(listener);
            mIsListeners.put(placementId, isListeners);
        }
    }

    public void removeInterstitialAdListener(String placementId, InterstitialAdListener listener) {
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.removeInterstitialAdListener(listener);
        } else {
            if (mIsListeners == null || mIsListeners.isEmpty()) {
                return;
            }
            Set<InterstitialAdListener> isListeners = mIsListeners.get(placementId);
            if (isListeners == null || isListeners.isEmpty()) {
                return;
            }
            isListeners.remove(listener);
            mIsListeners.put(placementId, isListeners);
        }
    }

    /**
     * Sets mediation interstitial ad listener.
     *
     * @param placementId the placement id
     * @param listener    the listener
     */
    public void setMediationInterstitialAdListener(String placementId, MediationInterstitialListener listener) {
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.setMediationInterstitialAdListener(listener);
        } else {
            if (mMediationIsListeners == null) {
                mMediationIsListeners = new ConcurrentHashMap<>();
            }
            mMediationIsListeners.put(placementId, listener);
        }
    }

    /**
     * Load interstitial ad.
     *
     * @param placementId the placement id
     */
    public void loadInterstitialAd(String placementId) {
        AdsUtil.callActionReport(EventId.CALLED_LOAD, placementId, null, CommonConstants.INTERSTITIAL);
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.loadInterstitialAd();
        } else {
            if (isInitRunning()) {
                if (mDelayLoadIs == null) {
                    mDelayLoadIs = new ConcurrentLinkedQueue<>();
                }
                mDelayLoadIs.add(placementId);
            } else {
                if (mIsListeners != null && mIsListeners.containsKey(placementId)) {
                    Set<InterstitialAdListener> isListeners = mIsListeners.get(placementId);
                    if (isListeners == null || isListeners.isEmpty()) {
                        AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
                    } else {
                        for (InterstitialAdListener listener : isListeners) {
                            listener.onInterstitialAdAvailabilityChanged(false);
                        }
                    }
                } else if (mMediationIsListeners != null && mMediationIsListeners.containsKey(placementId)) {
                    mMediationIsListeners.get(placementId).onInterstitialAdLoadFailed(
                            new Error(ErrorCode.CODE_LOAD_SDK_UNINITIALIZED,
                                    ErrorCode.MSG_LOAD_SDK_UNINITIALIZED, -1));
                } else {
                    AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
                }
            }
        }
    }

    /**
     * Is interstitial ad ready boolean.
     *
     * @param placementId the placement id
     * @return the boolean
     */
    public boolean isInterstitialAdReady(String placementId) {
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            boolean result = isManager.isInterstitialAdReady();
            if (result) {
                AdsUtil.callActionReport(EventId.CALLED_IS_READY_TRUE, placementId, null, CommonConstants.INTERSTITIAL);
            } else {
                AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.INTERSTITIAL);
            }
            return result;
        }
        AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.INTERSTITIAL);
        return false;
    }

    /**
     * Shows the given scene's InterstitialAd, shows default if the scene does not exist
     *
     * @param placementId placementId
     * @param scene       scene name
     */
    public void showInterstitialAd(String placementId, String scene) {
        AdsUtil.callActionReport(EventId.CALLED_SHOW, placementId, scene, CommonConstants.INTERSTITIAL);
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.showInterstitialAd(scene);
        } else {
            if (mIsListeners != null && mIsListeners.containsKey(placementId)) {
                Set<InterstitialAdListener> isListeners = mIsListeners.get(placementId);
                if (isListeners == null || isListeners.isEmpty()) {
                    AdLog.getSingleton().LogE(ErrorCode.MSG_SHOW_SDK_UNINITIALIZED);
                } else {
                    for (InterstitialAdListener listener : isListeners) {
                        listener.onInterstitialAdShowFailed(
                                SceneUtil.getScene(PlacementUtils.getPlacement(placementId), scene),
                                new Error(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                                        ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1));
                    }
                }
            } else if (mMediationIsListeners != null && mMediationIsListeners.containsKey(placementId)) {
                mMediationIsListeners.get(placementId).onInterstitialAdShowFailed(
                        new Error(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                                ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1));
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_SHOW_SDK_UNINITIALIZED);
            }
        }
    }

    /**
     * sets rewarded extId with placementId
     *
     * @param placementId the placement id
     * @param scene       the scene
     * @param extId       extId
     */
    public void setRewardedExtId(String placementId, String scene, String extId) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.setRewardedExtId(scene, extId);
        }
    }

    /**
     * only one listener will exist in the whole lifecycle
     *
     * @param placementId the placement id
     * @param listener    RewardedVideoAd listener
     */
    public void setRewardedVideoListener(String placementId, RewardedVideoListener listener) {
        addRewardedVideoListener(placementId, listener);
    }

    public void addRewardedVideoListener(String placementId, RewardedVideoListener listener) {
        addRvAdListenerImp(placementId, listener, false);
    }

    private void addRvAdListenerImp(String placementId, RewardedVideoListener listener, boolean reAdd) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.addRewardedVideoListener(listener);
        } else {
            if (reAdd) {
                return;
            }
            if (mRvListeners == null) {
                mRvListeners = new ConcurrentHashMap<>();
            }
            Set<RewardedVideoListener> rvListeners = mRvListeners.get(placementId);
            if (rvListeners == null) {
                rvListeners = new HashSet<>();
            }
            rvListeners.add(listener);
            mRvListeners.put(placementId, rvListeners);
        }
    }

    public void removeRewardedVideoListener(String placementId, RewardedVideoListener listener) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.removeRewardedVideoListener(listener);
        } else {
            if (mRvListeners == null || mRvListeners.isEmpty()) {
                return;
            }
            Set<RewardedVideoListener> rvListeners = mRvListeners.get(placementId);
            if (rvListeners == null || rvListeners.isEmpty()) {
                return;
            }
            rvListeners.remove(listener);
            mRvListeners.put(placementId, rvListeners);
        }
    }

    /**
     * Sets mediation rewarded video listener.
     *
     * @param placementId the placement id
     * @param listener    the listener
     */
    public void setMediationRewardedVideoListener(String placementId, MediationRewardVideoListener listener) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.setMediationRewardedVideoListener(listener);
        } else {
            if (mMediationRvListeners == null) {
                mMediationRvListeners = new ConcurrentHashMap<>();
            }
            mMediationRvListeners.put(placementId, listener);
        }
    }

    /**
     * Only developers call this method
     *
     * @param placementId the placement id
     */
    public void loadRewardedVideo(String placementId) {
        AdsUtil.callActionReport(EventId.CALLED_LOAD, placementId, null, CommonConstants.VIDEO);
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.loadRewardedVideo();
        } else {
            if (isInitRunning()) {
                if (mDelayLoadRv == null) {
                    mDelayLoadRv = new ConcurrentLinkedQueue<>();
                }
                mDelayLoadRv.add(placementId);
            } else {
                if (mRvListeners != null && mRvListeners.containsKey(placementId)) {
                    Set<RewardedVideoListener> rvListeners = mRvListeners.get(placementId);
                    if (rvListeners == null || rvListeners.isEmpty()) {
                        AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
                    } else {
                        for (RewardedVideoListener listener : rvListeners) {
                            listener.onRewardedVideoAvailabilityChanged(false);
                        }
                    }
                } else if (mMediationRvListeners != null && mMediationRvListeners.containsKey(placementId)) {
                    mMediationRvListeners.get(placementId).onRewardedVideoLoadFailed(
                            new Error(ErrorCode.CODE_LOAD_SDK_UNINITIALIZED,
                                    ErrorCode.MSG_LOAD_SDK_UNINITIALIZED, -1));
                } else {
                    AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
                }
            }
        }
    }

    /**
     * Is rewarded video ready boolean.
     *
     * @param placementId the placement id
     * @return the boolean
     */
    public boolean isRewardedVideoReady(String placementId) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            boolean result = rvManager.isRewardedVideoReady();
            if (result) {
                AdsUtil.callActionReport(EventId.CALLED_IS_READY_TRUE, placementId, null, CommonConstants.VIDEO);
            } else {
                AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.VIDEO);
            }
            return result;
        }
        AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.VIDEO);
        return false;
    }

    /**
     * Shows the given scene's RewardedVideoAd, shows default if the scene does not exist
     *
     * @param placementId the placement id
     * @param scene       scene name
     */
    public void showRewardedVideo(String placementId, String scene) {
        AdsUtil.callActionReport(EventId.CALLED_SHOW, placementId, scene, CommonConstants.VIDEO);
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.showRewardedVideo(scene);
        } else {
            if (mRvListeners != null && mRvListeners.containsKey(placementId)) {
                Set<RewardedVideoListener> rvListeners = mRvListeners.get(placementId);
                if (rvListeners == null || rvListeners.isEmpty()) {
                    AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
                } else {
                    for (RewardedVideoListener listener : rvListeners) {
                        listener.onRewardedVideoAdShowFailed(
                                SceneUtil.getScene(PlacementUtils.getPlacement(placementId), scene),
                                new Error(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                                        ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1));
                    }
                }
            } else if (mMediationRvListeners != null && mMediationRvListeners.containsKey(placementId)) {
                mMediationRvListeners.get(placementId).onRewardedVideoAdShowFailed(
                        new Error(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                                ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1));
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_SHOW_SDK_UNINITIALIZED);
            }
        }
    }

    @Override
    public void onSuccess() {
        initManagerWithDefaultPlacementId();
        setListeners();
        checkHasLoadWhileInInitProgress();
        preloadAdWithAdType();
        if (mInitCallbacks != null) {
            for (InitCallback callback : mInitCallbacks) {
                if (callback == null) {
                    continue;
                }
                callback.onSuccess();
            }
            mInitCallbacks.clear();
        }
        startScheduleTaskWithPreloadType();
    }

    private void setListeners() {
        if (mIsListeners != null && !mIsListeners.isEmpty()) {
            Set<Map.Entry<String, Set<InterstitialAdListener>>> isListenerEntrys = mIsListeners.entrySet();
            for (Map.Entry<String, Set<InterstitialAdListener>> isListenerEntry : isListenerEntrys) {
                if (isListenerEntry != null) {
                    Set<InterstitialAdListener> isListeners = isListenerEntry.getValue();
                    for (InterstitialAdListener listener : isListeners) {
                        addIsAdListenerImp(isListenerEntry.getKey(), listener, true);
                    }
                }
            }
            mIsListeners.clear();
        }
        if (mRvListeners != null && !mRvListeners.isEmpty()) {
            Set<Map.Entry<String, Set<RewardedVideoListener>>> rvListenerEntrys = mRvListeners.entrySet();
            for (Map.Entry<String, Set<RewardedVideoListener>> rvListenerEntry : rvListenerEntrys) {
                if (rvListenerEntry != null) {
                    Set<RewardedVideoListener> rvListeners = rvListenerEntry.getValue();
                    for (RewardedVideoListener listener : rvListeners) {
                        addRvAdListenerImp(rvListenerEntry.getKey(), listener, true);
                    }
                }
            }
            mRvListeners.clear();
        }

        if (mMediationRvListeners != null && !mMediationRvListeners.isEmpty()) {
            Set<Map.Entry<String, MediationRewardVideoListener>> rvListenerEntrys = mMediationRvListeners.entrySet();
            for (Map.Entry<String, MediationRewardVideoListener> rvListenerEntry : rvListenerEntrys) {
                if (rvListenerEntry != null) {
                    setMediationRewardedVideoListener(rvListenerEntry.getKey(), rvListenerEntry.getValue());
                }
            }
            mMediationRvListeners.clear();
        }

        if (mMediationIsListeners != null && !mMediationIsListeners.isEmpty()) {
            Set<Map.Entry<String, MediationInterstitialListener>> isListenerEntrys = mMediationIsListeners.entrySet();
            for (Map.Entry<String, MediationInterstitialListener> isListenerEntry : isListenerEntrys) {
                if (isListenerEntry != null) {
                    setMediationInterstitialAdListener(isListenerEntry.getKey(), isListenerEntry.getValue());
                }
            }
            mMediationIsListeners.clear();
        }
    }

    @Override
    public void onError(Error result) {
        callbackErrorWhileLoadBeforeInit(result);
        if (mInitCallbacks != null) {
            for (InitCallback callback : mInitCallbacks) {
                if (callback == null) {
                    AdLog.getSingleton().LogE(ErrorCode.ERROR_INIT_FAILED + " " + result);
                    continue;
                }
                callback.onError(result);
            }
            mInitCallbacks.clear();
        }
        clearCacheListeners();
    }

    private void clearCacheListeners() {
        if (mRvListeners != null) {
            mRvListeners.clear();
        }
        if (mIsListeners != null) {
            mIsListeners.clear();
        }
    }

    private IsManager getIsManager(String placementId) {
        if (!isInit()) {
            return null;
        }
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = getPlacement("", CommonConstants.INTERSTITIAL);
            if (placement == null) {
                return null;
            }
            placementId = placement.getId();
        }
        return mIsManagers.get(placementId);
    }

    private RvManager getRvManager(String placementId) {
        if (!isInit()) {
            return null;
        }
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = getPlacement("", CommonConstants.VIDEO);
            if (placement == null) {
                return null;
            }
            placementId = placement.getId();
        }
        return mRvManagers.get(placementId);
    }

    private void initManagerWithDefaultPlacementId() {
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null) {
            return;
        }

        Map<String, Placement> placementMap = config.getPls();
        if (placementMap == null || placementMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Placement> placementEntry : placementMap.entrySet()) {
            if (placementEntry == null) {
                continue;
            }
            Placement placement = placementEntry.getValue();
            if (placement != null) {
                int adType = placement.getT();
                String placementId = placement.getId();
                switch (adType) {
                    case CommonConstants.INTERSTITIAL:
                        if (mIsManagers != null && !mIsManagers.containsKey(placementId)) {
                            IsManager isManager = new IsManager();
                            isManager.setCurrentPlacement(placement);
                            mIsManagers.put(placementId, isManager);
                        }
                        break;
                    case CommonConstants.VIDEO:
                        if (mRvManagers != null && !mRvManagers.containsKey(placementId)) {
                            RvManager rvManager = new RvManager();
                            rvManager.setCurrentPlacement(placement);
                            mRvManagers.put(placementId, rvManager);
                        }
                        break;
                    case CommonConstants.SPLASH:
                        SplashAdManager.getInstance().initSplashAd(placementId);
                    default:
                        break;

                }
            }
        }
    }

    /**
     * Called after init done, with different AD_TYPEs
     *
     * @param adTypes AD_TYPEs to be preloaded
     */
    private void anotherInitCalledAfterInitSuccess(AD_TYPE... adTypes) {
        DeveloperLog.LogD("anotherInitCalledAfterInitSuccess");
        if (adTypes == null) {
            return;
        }
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null) {
            DeveloperLog.LogD("anotherInitCalledAfterInitSuccess failed cause config empty");
            return;
        }
        Map<String, Placement> placementMap = config.getPls();
        if (placementMap == null || placementMap.isEmpty()) {
            DeveloperLog.LogD("anotherInitCalledAfterInitSuccess failed cause placementMap empty");
            return;
        }
        for (AD_TYPE adType : adTypes) {
            if (adType == AD_TYPE.REWARDED_VIDEO) {
                if (mDidRvInit.get()) {
                    return;
                }
                preloadRV(placementMap.entrySet());
                startScheduleRv();
            } else if (adType == AD_TYPE.INTERSTITIAL) {
                if (mDidIsInit.get()) {
                    return;
                }
                preloadIS(placementMap.entrySet());
                startScheduleIs();
            }
        }
    }

    /**
     * Gets the 1st Placement for the asType if PlacementId is empty
     *
     * @param placementId
     */
    private Placement getPlacement(String placementId, int adType) {
        if (TextUtils.isEmpty(placementId)) {
            return PlacementUtils.getPlacement(adType);
        }
        return PlacementUtils.getPlacement(placementId);
    }

    private void preloadAdWithAdType() {
        DeveloperLog.LogD("preloadAdWithAdType");
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null) {
            DeveloperLog.LogD("preloadAdWithAdType failed cause config empty");
            return;
        }
        Map<String, Placement> placementMap = config.getPls();
        if (placementMap == null || placementMap.isEmpty()) {
            DeveloperLog.LogD("preloadAdWithAdType failed cause placementMap empty");
            return;
        }
        Set<Map.Entry<String, Placement>> placements = placementMap.entrySet();
        if (mPreloadAdTypes.isEmpty()) {
            DeveloperLog.LogD("preload all ad");
            preloadIS(placements);
            preloadRV(placements);
        } else {
            for (AD_TYPE adType : mPreloadAdTypes) {
                if (adType == AD_TYPE.INTERSTITIAL) {
                    preloadIS(placements);
                } else if (adType == AD_TYPE.REWARDED_VIDEO) {
                    preloadRV(placements);
                }
            }
        }
    }


    private void preloadIS(Set<Map.Entry<String, Placement>> placements) {
        mDidIsInit.set(true);
        for (Map.Entry<String, Placement> placementEntry : placements) {
            Placement placement = placementEntry.getValue();
            if (placement != null) {
                int type = placement.getT();
                if (type == CommonConstants.INTERSTITIAL) {
                    IsManager isManager = getIsManager(placement.getId());
                    if (isManager != null) {
                        DeveloperLog.LogD("preloadIS for placementId : " + placement.getId());
                        isManager.loadAdWithAction(LOAD_TYPE.INIT);
                    }
                }
            }
        }
    }

    private void preloadRV(Set<Map.Entry<String, Placement>> placements) {
        DeveloperLog.LogD("preloadRV");
        mDidRvInit.set(true);
        for (Map.Entry<String, Placement> placementEntry : placements) {
            Placement placement = placementEntry.getValue();
            if (placement != null) {
                int type = placement.getT();
                if (type == CommonConstants.VIDEO) {
                    RvManager rvManager = getRvManager(placement.getId());
                    if (rvManager != null) {
                        DeveloperLog.LogD("preloadRV for placementId : " + placement.getId());
                        rvManager.loadAdWithAction(LOAD_TYPE.INIT);
                    }
                }
            }
        }
    }

    private void startScheduleTaskWithPreloadType() {
        if (mPreloadAdTypes.isEmpty()) {
            startScheduleRv();
            startScheduleIs();
        } else {
            for (AD_TYPE adType : mPreloadAdTypes) {
                if (adType == AD_TYPE.REWARDED_VIDEO) {
                    startScheduleRv();
                } else if (adType == AD_TYPE.INTERSTITIAL) {
                    startScheduleIs();
                }
            }
        }
    }

    private void startScheduleRv() {
        if (!mRvManagers.isEmpty()) {
            Set<Map.Entry<String, RvManager>> rvEntrys = mRvManagers.entrySet();
            for (Map.Entry<String, RvManager> rvManagerEntry : rvEntrys) {
                if (rvManagerEntry != null) {
                    DeveloperLog.LogD("startScheduleRv for placementId : " + rvManagerEntry.getKey());
                    rvManagerEntry.getValue().initRewardedVideo();
                }
            }
        }
    }

    private void startScheduleIs() {
        if (!mIsManagers.isEmpty()) {
            Set<Map.Entry<String, IsManager>> isEntrys = mIsManagers.entrySet();
            for (Map.Entry<String, IsManager> isManagerEntry : isEntrys) {
                if (isManagerEntry != null) {
                    DeveloperLog.LogD("startScheduleIs for placementId : " + isManagerEntry.getKey());
                    isManagerEntry.getValue().initInterstitialAd();
                }
            }
        }
    }

    private void checkHasLoadWhileInInitProgress() {
        if (mDelayLoadIs != null) {
            for (String delayLoadI : mDelayLoadIs) {
                loadInterstitialAd(delayLoadI);
            }
        }

        if (mDelayLoadRv != null) {
            for (String s : mDelayLoadRv) {
                loadRewardedVideo(s);
            }
        }
    }

    private void callbackErrorWhileLoadBeforeInit(Error error) {
        if (mDelayLoadIs != null) {
            for (String delayLoadI : mDelayLoadIs) {
                if (mIsListeners != null && !mIsListeners.isEmpty()) {
                    Set<InterstitialAdListener> isListeners = mIsListeners.get(delayLoadI);
                    if (isListeners == null || isListeners.isEmpty()) {
                        AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
                    } else {
                        for (InterstitialAdListener listener : isListeners) {
                            listener.onInterstitialAdAvailabilityChanged(false);
                        }
                    }
                }

                if (mMediationIsListeners != null && !mMediationIsListeners.isEmpty()) {
                    mMediationIsListeners.get(delayLoadI).onInterstitialAdLoadFailed(error);
                }
            }
        }

        if (mDelayLoadRv != null) {
            for (String s : mDelayLoadRv) {
                if (mRvListeners != null && !mRvListeners.isEmpty()) {
                    Set<RewardedVideoListener> rvListeners = mRvListeners.get(s);
                    if (rvListeners == null || rvListeners.isEmpty()) {
                        AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
                    } else {
                        for (RewardedVideoListener listener : rvListeners) {
                            listener.onRewardedVideoAvailabilityChanged(false);
                        }
                    }
                }

                if (mMediationRvListeners != null && !mMediationRvListeners.isEmpty()) {
                    mMediationRvListeners.get(s).onRewardedVideoLoadFailed(error);
                }
            }
        }
    }

    private static void callActionReport(int eventId, String placementId, String scene, int adType) {
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = PlacementUtils.getPlacement(adType);
            if (placement != null) {
                placementId = placement.getId();
            }
        }
        Scene s = SceneUtil.getScene(PlacementUtils.getPlacement(placementId), scene);
        AdsUtil.callActionReport(placementId, s != null ? s.getId() : 0, eventId);
    }

    public void setGDPRConsent(boolean consent) {
        AdapterRepository.getInstance().setGDPRConsent(consent);
    }

    public void setAgeRestricted(boolean restricted) {
        AdapterRepository.getInstance().setAgeRestricted(restricted);
    }

    public void setUserAge(int age) {
        AdapterRepository.getInstance().setUserAge(age);
    }

    public void setUserGender(String gender) {
        AdapterRepository.getInstance().setUserGender(gender);
    }

    public void setUSPrivacyLimit(boolean value) {
        AdapterRepository.getInstance().setUSPrivacyLimit(value);
    }

    public Boolean getGDPRConsent() {
        return AdapterRepository.getInstance().getGDPRConsent();
    }

    public Boolean getAgeRestricted() {
        return AdapterRepository.getInstance().getAgeRestricted();
    }

    public Integer getUserAge() {
        return AdapterRepository.getInstance().getUserAge();
    }

    public String getUserGender() {
        return AdapterRepository.getInstance().getUserGender();
    }

    public Boolean getUSPrivacyLimit() {
        return AdapterRepository.getInstance().getUSPrivacyLimit();
    }

    public MetaData getMetaData() {
        return AdapterRepository.getInstance().getMetaData();
    }
}
