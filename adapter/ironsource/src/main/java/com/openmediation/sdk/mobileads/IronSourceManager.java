package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.mobileads.ironsource.BuildConfig;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyInterstitialListener;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyRewardedVideoListener;
import com.openmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A centralized {@link ISDemandOnlyRewardedVideoListener} to forward IronSource ad events
 * to all {@link IronSourceAdapter} instances.
 */
class IronSourceManager implements ISDemandOnlyRewardedVideoListener, ISDemandOnlyInterstitialListener {

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

    static IronSourceManager getInstance() {
        return instance;
    }

    private IronSourceManager() {
        availableInstances = new ConcurrentHashMap<>();
        availableStates = new ConcurrentHashMap<>();
        IronSource.setISDemandOnlyRewardedVideoListener(this);
        IronSource.setISDemandOnlyInterstitialListener(this);
    }

    void initIronSourceSDK(Activity activity, String appKey, List<IronSource.AD_UNIT> adUnits) {
        IronSource.setMediationType(MEDIATION_NAME + BuildConfig.VERSION_NAME);
        if (adUnits.size() > 0) {
            IronSource.initISDemandOnly(activity, appKey, adUnits.toArray(new IronSource.AD_UNIT[adUnits.size()]));
        }
    }

    void loadInterstitial(String instanceId, WeakReference<IronSourceAdapter> weakAdapter) {
        if (TextUtils.isEmpty(instanceId) || weakAdapter == null) {
            log("loadInterstitial- instanceId / weakAdapter is null");
            return;
        }

        IronSourceAdapter ironSourceAdapter = weakAdapter.get();
        if (ironSourceAdapter == null) {
            log("loadInterstitial - ironSourceAdapter is null");
            return;
        }

        if (canLoadInstance(instanceId)) {
            changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.LOCKED);
            registerAdapter(instanceId, weakAdapter);
            IronSource.loadISDemandOnlyInterstitial(instanceId);
        } else {
            ironSourceAdapter.onInterstitialAdLoadFailed(instanceId, new IronSourceError(IronSourceError.ERROR_CODE_GENERIC,
                    "interstitial instance already exists, couldn't load another one at the same time!"));
        }
    }

    void loadRewardedVideo(String instanceId, WeakReference<IronSourceAdapter> weakAdapter) {

        if (instanceId == null || weakAdapter == null) {
            log("loadRewardedVideo - instanceId / weakAdapter is null");
            return;
        }

        IronSourceAdapter ironSourceMediationAdapter = weakAdapter.get();
        if (ironSourceMediationAdapter == null) {
            log("loadRewardedVideo - ironSourceMediationAdapter is null");
            return;
        }

        if (canLoadInstance(instanceId)) {
            changeInstanceState(instanceId, IronSourceAdapter.INSTANCE_STATE.LOCKED);
            registerAdapter(instanceId, weakAdapter);
            IronSource.loadISDemandOnlyRewardedVideo(instanceId);
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
        IronSource.showISDemandOnlyRewardedVideo(instanceId);
    }

    void showInterstitial(String instanceId) {
        IronSource.showISDemandOnlyInterstitial(instanceId);
    }

    boolean isRewardedVideoReady(String instanceId) {
        return IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId);
    }

    boolean isInterstitialReady(String instanceId) {
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
}
