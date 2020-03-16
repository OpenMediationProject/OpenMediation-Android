// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.mediation.MediationInterstitialListener;
import com.openmediation.sdk.mediation.MediationRewardVideoListener;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoListener;

/**
 *
 */
public class ListenerWrapper {
    private RewardedVideoListener mRvListener;
    private InterstitialAdListener mIsListener;
    private MediationRewardVideoListener mMediationRvListener;
    private MediationInterstitialListener mMediationIsListener;

    private String mPlacementId;

    private boolean canSendCallback(Object listener) {
        return listener != null;
    }

    private void sendCallback(Runnable callbackRunnable) {
        if (callbackRunnable != null) {
            HandlerUtil.runOnUiThread(callbackRunnable);
        }
    }

    public void setPlacementId(String placementId) {
        this.mPlacementId = placementId;
    }

    public void setRewardedVideoListener(RewardedVideoListener listener) {
        mRvListener = listener;
    }

    public void setInterstitialAdListener(InterstitialAdListener listener) {
        mIsListener = listener;
    }

    public void setMediationRewardedVideoListener(MediationRewardVideoListener listener) {
        mMediationRvListener = listener;
    }

    public void setMediationInterstitialListener(MediationInterstitialListener listener) {
        mMediationIsListener = listener;
    }


    public void onRewardedVideoAvailabilityChanged(final boolean available) {
        DeveloperLog.LogD("onRewardedVideoAvailabilityChanged : " + available);
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mRvListener.onRewardedVideoAvailabilityChanged(available);
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
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdShowed(scene);
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
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdShowFailed(scene, error);
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
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdClicked(scene);
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
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdClosed(scene);
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
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdStarted(scene);
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
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdEnded(scene);
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
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdRewarded(scene);
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
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mIsListener.onInterstitialAdAvailabilityChanged(available);
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
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mIsListener.onInterstitialAdShowed(scene);
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
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mIsListener.onInterstitialAdShowFailed(scene, error);
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
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mIsListener.onInterstitialAdClosed(scene);
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
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {

                @Override
                public void run() {
                    mIsListener.onInterstitialAdClicked(scene);
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
}
