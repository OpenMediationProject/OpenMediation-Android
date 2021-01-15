// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.mediation.MediationInterstitialListener;
import com.openmediation.sdk.mediation.MediationRewardVideoListener;
import com.openmediation.sdk.promotion.PromotionAdListener;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoListener;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ListenerWrapper {
    private Set<RewardedVideoListener> mRvListeners;
    private Set<InterstitialAdListener> mIsListeners;
    private Set<PromotionAdListener> mCpListeners;
    private MediationRewardVideoListener mMediationRvListener;
    private MediationInterstitialListener mMediationIsListener;

    private String mPlacementId;

    ListenerWrapper() {
        mRvListeners = new HashSet<>();
        mIsListeners = new HashSet<>();
        mCpListeners = new HashSet<>();
    }

    private boolean canSendCallback(Object listener) {
        return listener != null;
    }

    private boolean canSendListCallback(Set listeners) {
        return listeners != null && !listeners.isEmpty();
    }

    private void sendCallback(Runnable callbackRunnable) {
        if (callbackRunnable != null) {
            HandlerUtil.runOnUiThread(callbackRunnable);
        }
    }

    public void addRewardedVideoListener(RewardedVideoListener listener) {
        mRvListeners.add(listener);
    }

    public void removeRewardedVideoListener(RewardedVideoListener listener) {
        mRvListeners.remove(listener);
    }

    public void addInterstitialListener(InterstitialAdListener listener) {
        mIsListeners.add(listener);
    }

    public void removeInterstitialListener(InterstitialAdListener listener) {
        mIsListeners.remove(listener);
    }

    public void addPromotionAdListener(PromotionAdListener listener) {
        mCpListeners.add(listener);
    }

    public void removePromotionAdListener(PromotionAdListener listener) {
        mCpListeners.remove(listener);
    }

    public void setPlacementId(String placementId) {
        this.mPlacementId = placementId;
    }

    public void setMediationRewardedVideoListener(MediationRewardVideoListener listener) {
        mMediationRvListener = listener;
    }

    public void setMediationInterstitialListener(MediationInterstitialListener listener) {
        mMediationIsListener = listener;
    }


    public void onRewardedVideoAvailabilityChanged(final boolean available) {
        DeveloperLog.LogD("onRewardedVideoAvailabilityChanged : " + available);
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAvailabilityChanged(available);
                    }
                    if (available) {
                        AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_SUCCESS, mPlacementId, null, null);
                    } else {
                        AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_ERROR, mPlacementId, null, null);
                    }
                }
            });
        }
    }


    public void onRewardedVideoLoadSuccess() {
        DeveloperLog.LogD("onRewardedVideoLoadSuccess");
        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoLoadSuccess();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_SUCCESS, mPlacementId, null, null);
                }
            });
        }
    }


    public void onRewardedVideoLoadFailed(final Error error) {
        DeveloperLog.LogD("onRewardedVideoLoadFailed : " + error);
        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoLoadFailed(error);
                    AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_ERROR, mPlacementId, null, error);
                }
            });
        }
    }


    public void onRewardedVideoAdShowed(final Scene scene) {
        DeveloperLog.LogD("onRewardedVideoAdShowed");
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAdShowed(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_PRESENT_SCREEN, mPlacementId, scene, null);
                }
            });
        }
        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoAdShowed();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_PRESENT_SCREEN, mPlacementId, scene, null);
                }
            });
        }
    }


    public void onRewardedVideoAdShowFailed(final Scene scene, final Error error) {
        DeveloperLog.LogD("onRewardedVideoAdShowFailed : " + error);
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAdShowFailed(scene, error);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_SHOW_FAILED, mPlacementId, scene, error);
                }
            });
        }
        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoAdShowFailed(error);
                    AdsUtil.callbackActionReport(EventId.CALLBACK_SHOW_FAILED, mPlacementId, scene, error);
                }
            });
        }
    }


    public void onRewardedVideoAdClicked(final Scene scene) {
        DeveloperLog.LogD("onRewardedVideoAdClicked");
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAdClicked(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_CLICK, mPlacementId, scene, null);
                }
            });
        }
        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoAdClicked();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_CLICK, mPlacementId, scene, null);
                }
            });
        }
    }


    public void onRewardedVideoAdClosed(final Scene scene) {
        DeveloperLog.LogD("onRewardedVideoAdClosed");
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAdClosed(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_DISMISS_SCREEN, mPlacementId, scene, null);
                }
            });
        }
        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoAdClosed();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_DISMISS_SCREEN, mPlacementId, scene, null);
                }
            });
        }
    }


    public void onRewardedVideoAdStarted(final Scene scene) {
        DeveloperLog.LogD("onRewardedVideoAdStarted");
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAdStarted(scene);
                    }
                }
            });
        }

        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoAdStarted();
                }
            });
        }
    }


    public void onRewardedVideoAdEnded(final Scene scene) {
        DeveloperLog.LogD("onRewardedVideoAdEnded : ");
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAdEnded(scene);
                    }
                }
            });
        }

        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoAdEnded();
                }
            });
        }
    }


    public void onRewardedVideoAdRewarded(final Scene scene) {
        DeveloperLog.LogD("onRewardedVideoAdRewarded");
        if (canSendListCallback(mRvListeners)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    for (RewardedVideoListener listener : mRvListeners) {
                        listener.onRewardedVideoAdRewarded(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_REWARDED, mPlacementId, scene, null);
                }
            });
        }
        if (canSendCallback(mMediationRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mMediationRvListener.onRewardedVideoAdRewarded();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_REWARDED, mPlacementId, scene, null);
                }
            });
        }
    }


    public void onInterstitialAdAvailabilityChanged(final boolean available) {
        DeveloperLog.LogD("onInterstitialAdAvailabilityChanged : " + available);
        if (canSendListCallback(mIsListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (InterstitialAdListener listener : mIsListeners) {
                        listener.onInterstitialAdAvailabilityChanged(available);
                    }
                    if (available) {
                        AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_SUCCESS, mPlacementId, null, null);
                    } else {
                        AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_ERROR, mPlacementId, null, null);
                    }
                }
            });
        }
    }


    public void onInterstitialAdLoadSuccess() {
        DeveloperLog.LogD("onInterstitialAdLoadSuccess");
        if (canSendCallback(mMediationIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationIsListener.onInterstitialAdLoadSuccess();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_SUCCESS, mPlacementId, null, null);
                }
            });
        }
    }


    public void onInterstitialAdLoadFailed(final Error error) {
        DeveloperLog.LogD("onInterstitialAdLoadFailed: " + error);
        if (canSendCallback(mMediationIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationIsListener.onInterstitialAdLoadFailed(error);
                    AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_ERROR, mPlacementId, null, error);
                }
            });
        }
    }


    public void onInterstitialAdShowed(final Scene scene) {
        DeveloperLog.LogD("onInterstitialAdShowed");
        if (canSendListCallback(mIsListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (InterstitialAdListener listener : mIsListeners) {
                        listener.onInterstitialAdShowed(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_PRESENT_SCREEN, mPlacementId, scene, null);
                }
            });
        }

        if (canSendCallback(mMediationIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationIsListener.onInterstitialAdShowed();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_PRESENT_SCREEN, mPlacementId, scene, null);
                }
            });
        }
    }


    public void onInterstitialAdShowFailed(final Scene scene, final Error error) {
        DeveloperLog.LogD("onInterstitialAdShowFailed");
        if (canSendListCallback(mIsListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (InterstitialAdListener listener : mIsListeners) {
                        listener.onInterstitialAdShowFailed(scene, error);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_SHOW_FAILED, mPlacementId, scene, error);
                }
            });
        }
        if (canSendCallback(mMediationIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationIsListener.onInterstitialAdShowFailed(error);
                    AdsUtil.callbackActionReport(EventId.CALLBACK_SHOW_FAILED, mPlacementId, scene, error);
                }
            });
        }
    }


    public void onInterstitialAdClosed(final Scene scene) {
        DeveloperLog.LogD("onInterstitialAdClosed");
        if (canSendListCallback(mIsListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (InterstitialAdListener listener : mIsListeners) {
                        listener.onInterstitialAdClosed(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_DISMISS_SCREEN, mPlacementId, scene, null);
                }
            });
        }

        if (canSendCallback(mMediationIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationIsListener.onInterstitialAdClosed();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_DISMISS_SCREEN, mPlacementId, scene, null);
                }
            });
        }
    }


    public void onInterstitialAdClicked(final Scene scene) {
        DeveloperLog.LogD("onInterstitialAdClicked");
        if (canSendListCallback(mIsListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (InterstitialAdListener listener : mIsListeners) {
                        listener.onInterstitialAdClicked(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_CLICK, mPlacementId, scene, null);
                }
            });
        }

        if (canSendCallback(mMediationIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mMediationIsListener.onInterstitialAdClicked();
                    AdsUtil.callbackActionReport(EventId.CALLBACK_CLICK, mPlacementId, scene, null);
                }
            });
        }
    }

    public void onPromotionAdAvailabilityChanged(final boolean available) {
        DeveloperLog.LogD("onPromotionAdAvailabilityChanged : " + available);
        if (canSendListCallback(mCpListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (PromotionAdListener listener : mCpListeners) {
                        listener.onPromotionAdAvailabilityChanged(available);
                    }
                    if (available) {
                        AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_SUCCESS, mPlacementId, null, null);
                    } else {
                        AdsUtil.callbackActionReport(EventId.CALLBACK_LOAD_ERROR, mPlacementId, null, null);
                    }
                }
            });
        }
    }

    public void onPromotionAdClicked(final Scene scene) {
        DeveloperLog.LogD("onPromotionAdClicked");
        if (canSendListCallback(mCpListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (PromotionAdListener listener : mCpListeners) {
                        listener.onPromotionAdClicked(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_CLICK, mPlacementId, scene, null);
                }
            });
        }
    }

    public void onPromotionAdShowed(final Scene scene) {
        DeveloperLog.LogD("onPromotionAdShowed");
        if (canSendListCallback(mCpListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (PromotionAdListener listener : mCpListeners) {
                        listener.onPromotionAdShowed(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_PRESENT_SCREEN, mPlacementId, scene, null);
                }
            });
        }
    }

    public void onPromotionAdShowFailed(final Scene scene, final Error error) {
        DeveloperLog.LogD("onPromotionAdShowFailed");
        if (canSendListCallback(mCpListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (PromotionAdListener listener : mCpListeners) {
                        listener.onPromotionAdShowFailed(scene, error);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_SHOW_FAILED, mPlacementId, scene, error);
                }
            });
        }
    }

    public void onPromotionAdHidden(final Scene scene) {
        DeveloperLog.LogD("onPromotionAdHidden");
        if (canSendListCallback(mCpListeners)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    for (PromotionAdListener listener : mCpListeners) {
                        listener.onPromotionAdHidden(scene);
                    }
                    AdsUtil.callbackActionReport(EventId.CALLBACK_DISMISS_SCREEN, mPlacementId, scene, null);
                }
            });
        }
    }
}
