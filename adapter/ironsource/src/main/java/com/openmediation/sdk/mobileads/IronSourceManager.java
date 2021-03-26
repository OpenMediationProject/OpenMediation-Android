package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyInterstitialListener;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyRewardedVideoListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.openmediation.sdk.mobileads.ironsource.BuildConfig;
import com.openmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A centralized {@link ISDemandOnlyRewardedVideoListener} to forward IronSource ad events
 * to all {@link IronSourceAdapter} instances.
 */
class IronSourceManager implements ISDemandOnlyRewardedVideoListener, ISDemandOnlyInterstitialListener,
        RewardedVideoListener, InterstitialListener {

    /**
     * Constant used for IronSource internal reporting.
     */
    private static final String MEDIATION_NAME = "AdTiming";

    private static final IronSourceManager instance = new IronSourceManager();

    private ConcurrentHashMap<String, WeakReference<IronSourceAdapter>> availableInstances;
    /**
     * state of placementId
     */
    private ConcurrentHashMap<String, IronSourceAdapter.INSTANCE_STATE> availableStates;

    /**
     * user mediation API only one InterstitialAd and RewardedVideoAd
     */
    private String mLastMediationIsInsId, mLastMediationVideoInsId;
    private WeakReference<IronSourceAdapter> mLastIsAdapter, mLastVideoAdapter;

    static IronSourceManager getInstance() {
        return instance;
    }

    private IronSourceManager() {
        availableInstances = new ConcurrentHashMap<>();
        availableStates = new ConcurrentHashMap<>();
        IronSource.setISDemandOnlyRewardedVideoListener(this);
        IronSource.setISDemandOnlyInterstitialListener(this);

        // MediationAPI
        IronSource.setRewardedVideoListener(this);
        IronSource.setInterstitialListener(this);
    }

    void initIronSourceSDK(Activity activity, String appKey, List<IronSource.AD_UNIT> adUnits) {
        IronSource.setMediationType(MEDIATION_NAME + BuildConfig.VERSION_NAME);
        if (adUnits.size() > 0) {
            if (IronSourceSetting.isMediationMode()) {
                IronSource.init(activity, appKey, adUnits.toArray(new IronSource.AD_UNIT[adUnits.size()]));
            } else {
                IronSource.initISDemandOnly(activity.getApplicationContext(), appKey, adUnits.toArray(new IronSource.AD_UNIT[adUnits.size()]));
            }
        }
    }

    void loadInterstitial(Activity activity, String instanceId, WeakReference<IronSourceAdapter> weakAdapter) {
        if (TextUtils.isEmpty(instanceId) || weakAdapter == null) {
            log("loadInterstitial- instanceId / weakAdapter is null");
            return;
        }

        IronSourceAdapter ironSourceAdapter = weakAdapter.get();
        if (ironSourceAdapter == null) {
            log("loadInterstitial - ironSourceAdapter is null");
            return;
        }

        if (IronSourceSetting.isMediationMode()) {
            mLastMediationIsInsId = instanceId;
            mLastIsAdapter = weakAdapter;
            IronSource.loadInterstitial();
            return;
        }

        if (canLoadInstance(instanceId)) {
            changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.LOCKED);
            registerAdapter(instanceId, weakAdapter);
            IronSource.loadISDemandOnlyInterstitial(activity, instanceId);
        } else {
            ironSourceAdapter.onInterstitialAdLoadFailed(instanceId, new IronSourceError(IronSourceError.ERROR_CODE_GENERIC,
                    "interstitial instance already exists, couldn't load another one at the same time!"));
        }
    }

    void loadRewardedVideo(Activity activity, String instanceId, WeakReference<IronSourceAdapter> weakAdapter) {

        if (instanceId == null || weakAdapter == null) {
            log("loadRewardedVideo - instanceId / weakAdapter is null");
            return;
        }

        IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
        if (ironSourceMediationAdapter == null) {
            log("loadRewardedVideo - ironSourceMediationAdapter is null");
            return;
        }

        if (IronSourceSetting.isMediationMode()) {
            mLastMediationVideoInsId = instanceId;
            mLastVideoAdapter = weakAdapter;
            return;
        }

        if (canLoadInstance(instanceId)) {
            changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.LOCKED);
            registerAdapter(instanceId, weakAdapter);
            IronSource.loadISDemandOnlyRewardedVideo(activity, instanceId);
        } else {
            ironSourceMediationAdapter.onRewardedVideoAdLoadFailed(instanceId, new IronSourceError(IronSourceError.ERROR_CODE_GENERIC,
                    "instance already exists, couldn't load another one in the same time!"));
        }
    }

    private boolean canLoadInstance(String instanceId) {
        if (!isAdapterRegistered(instanceId)) {
            return true;
        }

        return isRegisteredAdapterCanLoad(instanceId);
    }

    private boolean isRegisteredAdapterCanLoad(String instanceId) {
        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);
        if (weakAdapter == null) {
            return true;
        }
        IronSourceAdapter ironSourceAdapter = weakAdapter.get();
        if (ironSourceAdapter == null) {
            return true;
        }

        IronSourceAdapter.INSTANCE_STATE state = availableStates.get(instanceId);
        return state == null || state.equals(IronSourceAdapter.INSTANCE_STATE.CAN_LOAD);
    }

    void showRewardedVideo(String instanceId) {
        if (IronSourceSetting.isMediationMode()) {
            IronSource.showRewardedVideo(instanceId);
        } else {
            IronSource.showISDemandOnlyRewardedVideo(instanceId);
        }
    }

    void showInterstitial(String instanceId) {
        if (IronSourceSetting.isMediationMode()) {
            IronSource.showInterstitial(instanceId);
        } else {
            IronSource.showISDemandOnlyInterstitial(instanceId);
        }
    }

    boolean isRewardedVideoReady(String instanceId) {
        if (IronSourceSetting.isMediationMode()) {
            return IronSource.isRewardedVideoAvailable();
        }
        return IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId);
    }

    boolean isInterstitialReady(String instanceId) {
        if (IronSourceSetting.isMediationMode()) {
            return IronSource.isInterstitialReady();
        }
        return IronSource.isISDemandOnlyInterstitialReady(instanceId);
    }

    private void registerAdapter(String instanceId,
                                 WeakReference<IronSourceAdapter> weakAdapter) {
        if (weakAdapter == null) {
            log("registerAdapter - weakAdapter is null");
            return;
        }
        IronSourceAdapter ironSourceAdapter = weakAdapter.get();
        if (ironSourceAdapter == null) {
            log("registerAdapter - ironSourceMediationAdapter is null");
            return;
        }
        availableInstances.put(instanceId, weakAdapter);
    }

    private boolean isAdapterRegistered(String instanceId) {
        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);
        if (weakAdapter != null) {
            IronSourceAdapter ironSourceAdapter = weakAdapter.get();
            return ironSourceAdapter != null;
        }
        return false;
    }

    @Override
    public void onRewardedVideoAdLoadSuccess(String instanceId) {
        log(String.format("IronSourceManager got RV Load success for instance %s", instanceId));

        changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.CAN_LOAD);
        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
            if (ironSourceMediationAdapter != null) {
                ironSourceMediationAdapter.onRewardedVideoAdLoadSuccess(instanceId);
            }
        }
    }

    @Override
    public void onRewardedVideoAdLoadFailed(String instanceId, IronSourceError ironSourceError) {
        log(String.format("IronSourceManager got RV Load failed for instance %s", instanceId));

        changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.CAN_LOAD);
        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
            if (ironSourceMediationAdapter != null) {
                ironSourceMediationAdapter.onRewardedVideoAdLoadFailed(instanceId, ironSourceError);
            }
        }
    }

    private void changeInstanceState(String instanceId,
                                     IronSourceAdapter.INSTANCE_STATE newState) {
        log(String.format("IronSourceManager change state to %s : %s", newState, instanceId));

        availableStates.put(instanceId, newState);
    }

    @Override
    public void onRewardedVideoAdOpened(String instanceId) {
        log(String.format("IronSourceManager got RV ad opened for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
            if (ironSourceMediationAdapter != null) {
                ironSourceMediationAdapter.onRewardedVideoAdOpened(instanceId);
                ironSourceMediationAdapter.onRewardedVideoAdStarted(instanceId);
            }
        }
    }

    private void log(String stringToLoad) {
        AdLog.getSingleton().LogD("OM-IronSource", stringToLoad);
    }

    @Override
    public void onRewardedVideoAdClosed(String instanceId) {
        log(String.format("IronSourceManager got RV ad closed for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
            if (ironSourceMediationAdapter != null) {
                ironSourceMediationAdapter.onRewardedVideoAdEnded(instanceId);
                ironSourceMediationAdapter.onRewardedVideoAdClosed(instanceId);
            }
        }
    }

    @Override
    public void onRewardedVideoAdShowFailed(String instanceId, IronSourceError ironSourceError) {
        log(String.format("IronSourceManager got RV show failed for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
            if (ironSourceMediationAdapter != null) {
                ironSourceMediationAdapter.onRewardedVideoAdShowFailed(instanceId, ironSourceError);
            }
        }
    }

    @Override
    public void onRewardedVideoAdClicked(String instanceId) {
        log(String.format("IronSourceManager got RV ad clicked for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
            if (ironSourceMediationAdapter != null) {
                ironSourceMediationAdapter.onRewardedVideoAdClicked(instanceId);
            }
        }
    }

    @Override
    public void onRewardedVideoAdRewarded(String instanceId) {
        log(String.format("IronSourceManager got RV ad rewarded for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
            if (ironSourceMediationAdapter != null) {
                ironSourceMediationAdapter.onRewardedVideoAdRewarded(instanceId);
            }
        }
    }

    @Override
    public void onInterstitialAdReady(String instanceId) {
        log(String.format("IronSourceManager got interstitial Load success for instance %s", instanceId));
        changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.CAN_LOAD);
        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceAdapter = weakAdapter.get();
            if (ironSourceAdapter != null) {
                ironSourceAdapter.onInterstitialAdReady(instanceId);
            }
        }
    }

    @Override
    public void onInterstitialAdLoadFailed(String instanceId, IronSourceError ironSourceError) {
        log(String.format("IronSourceManager got interstitial Load failed for instance %s", instanceId));
        changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.CAN_LOAD);
        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceAdapter = weakAdapter.get();
            if (ironSourceAdapter != null) {
                ironSourceAdapter.onInterstitialAdLoadFailed(instanceId, ironSourceError);
            }
        }

    }

    @Override
    public void onInterstitialAdOpened(String instanceId) {
        log(String.format("IronSourceManager got interstitial ad opened for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceAdapter = weakAdapter.get();
            if (ironSourceAdapter != null) {
                ironSourceAdapter.onInterstitialAdOpened(instanceId);
            }
        }

    }

    @Override
    public void onInterstitialAdClosed(String instanceId) {
        log(String.format("IronSourceManager got interstitial ad closed for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceAdapter = weakAdapter.get();
            if (ironSourceAdapter != null) {
                ironSourceAdapter.onInterstitialAdClosed(instanceId);
            }
        }

    }

    @Override
    public void onInterstitialAdShowFailed(String instanceId, IronSourceError ironSourceError) {
        log(String.format("IronSourceManager got interstitial show failed for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceAdapter = weakAdapter.get();
            if (ironSourceAdapter != null) {
                ironSourceAdapter.onInterstitialAdShowFailed(instanceId, ironSourceError);
            }
        }

    }

    @Override
    public void onInterstitialAdClicked(String instanceId) {
        log(String.format("IronSourceManager got interstitial ad clicked for instance %s", instanceId));

        WeakReference<IronSourceAdapter> weakAdapter = availableInstances.get(instanceId);

        if (weakAdapter != null) {
            IronSourceAdapter ironSourceAdapter = weakAdapter.get();
            if (ironSourceAdapter != null) {
                ironSourceAdapter.onInterstitialAdClicked(instanceId);
            }
        }
    }


    /**
     * Mediation API
     **/
    @Override
    public void onInterstitialAdReady() {
        log("IronSourceManager got onInterstitialAdReady ");
        if (!TextUtils.isEmpty(mLastMediationIsInsId) && mLastIsAdapter != null && mLastIsAdapter.get() != null) {
            mLastIsAdapter.get().onInterstitialAdReady(mLastMediationIsInsId);
        }
    }

    @Override
    public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
        log("IronSourceManager got onInterstitialAdLoadFailed ");
        if (!TextUtils.isEmpty(mLastMediationIsInsId) && mLastIsAdapter != null && mLastIsAdapter.get() != null) {
            mLastIsAdapter.get().onInterstitialAdLoadFailed(mLastMediationIsInsId, ironSourceError);
        }
    }

    @Override
    public void onInterstitialAdOpened() {
        log("IronSourceManager got onInterstitialAdOpened ");
        if (!TextUtils.isEmpty(mLastMediationIsInsId) && mLastIsAdapter != null && mLastIsAdapter.get() != null) {
            mLastIsAdapter.get().onInterstitialAdOpened(mLastMediationIsInsId);
        }
    }

    @Override
    public void onInterstitialAdClosed() {
        log("IronSourceManager got onInterstitialAdClosed ");
        if (!TextUtils.isEmpty(mLastMediationIsInsId) && mLastIsAdapter != null && mLastIsAdapter.get() != null) {
            mLastIsAdapter.get().onInterstitialAdClosed(mLastMediationIsInsId);
        }
    }

    @Override
    public void onInterstitialAdShowSucceeded() {
        log("IronSourceManager got onInterstitialAdShowSucceeded ");
    }

    @Override
    public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
        log("IronSourceManager got onInterstitialAdShowFailed ");
        if (!TextUtils.isEmpty(mLastMediationIsInsId) && mLastIsAdapter != null && mLastIsAdapter.get() != null) {
            mLastIsAdapter.get().onInterstitialAdShowFailed(mLastMediationIsInsId, ironSourceError);
        }
    }

    @Override
    public void onInterstitialAdClicked() {
        log("IronSourceManager got onInterstitialAdClicked ");
        if (!TextUtils.isEmpty(mLastMediationIsInsId) && mLastIsAdapter != null && mLastIsAdapter.get() != null) {
            mLastIsAdapter.get().onInterstitialAdClicked(mLastMediationIsInsId);
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
        log("IronSourceManager got IronSourceManager got onRewardedVideoAdOpened ");
        if (!TextUtils.isEmpty(mLastMediationVideoInsId) && mLastVideoAdapter != null && mLastVideoAdapter.get() != null) {
            mLastVideoAdapter.get().onRewardedVideoAdStarted(mLastMediationVideoInsId);
            mLastVideoAdapter.get().onRewardedVideoAdOpened(mLastMediationVideoInsId);
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        log("IronSourceManager got onRewardedVideoAdClosed ");
        if (!TextUtils.isEmpty(mLastMediationVideoInsId) && mLastVideoAdapter != null && mLastVideoAdapter.get() != null) {
            mLastVideoAdapter.get().onRewardedVideoAdEnded(mLastMediationVideoInsId);
            mLastVideoAdapter.get().onRewardedVideoAdClosed(mLastMediationVideoInsId);
        }
    }

    @Override
    public void onRewardedVideoAvailabilityChanged(boolean b) {
        log("IronSourceManager got onRewardedVideoAvailabilityChanged " + b);
        if (!b) {
            return;
        }
        if (!TextUtils.isEmpty(mLastMediationVideoInsId) && mLastVideoAdapter != null && mLastVideoAdapter.get() != null) {
            mLastVideoAdapter.get().onRewardedVideoAdLoadSuccess(mLastMediationVideoInsId);
        }
    }

    @Override
    public void onRewardedVideoAdStarted() {
        log("IronSourceManager got onRewardedVideoAdStarted ");
    }

    @Override
    public void onRewardedVideoAdEnded() {
        log("IronSourceManager got onRewardedVideoAdEnded ");
    }

    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        log("IronSourceManager got onRewardedVideoAdRewarded ");
        if (!TextUtils.isEmpty(mLastMediationVideoInsId) && mLastVideoAdapter != null && mLastVideoAdapter.get() != null) {
            mLastVideoAdapter.get().onRewardedVideoAdRewarded(mLastMediationVideoInsId);
        }
    }

    @Override
    public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
        log("IronSourceManager got onRewardedVideoAdShowFailed " + ironSourceError);
        if (!TextUtils.isEmpty(mLastMediationVideoInsId) && mLastVideoAdapter != null && mLastVideoAdapter.get() != null) {
            mLastVideoAdapter.get().onRewardedVideoAdShowFailed(mLastMediationVideoInsId, ironSourceError);
        }
    }

    @Override
    public void onRewardedVideoAdClicked(Placement placement) {
        log("IronSourceManager got onRewardedVideoAdClicked " + placement);
        if (!TextUtils.isEmpty(mLastMediationVideoInsId) && mLastVideoAdapter != null && mLastVideoAdapter.get() != null) {
            mLastVideoAdapter.get().onRewardedVideoAdClicked(mLastMediationVideoInsId);
        }
    }
}
