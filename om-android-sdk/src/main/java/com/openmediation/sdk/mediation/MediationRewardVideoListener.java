// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import com.openmediation.sdk.utils.error.Error;

/**
 * The interface Mediation reward video listener.
 */
public interface MediationRewardVideoListener {
    /**
     * called when rewardedVideo load success
     */
    void onRewardedVideoLoadSuccess();

    /**
     * called when rewardedVideo load failed
     *
     * @param error the error
     */
    void onRewardedVideoLoadFailed(Error error);

    /**
     * called when rewardedVideo shows
     */
    void onRewardedVideoAdShowed();

    /**
     * called when rewardedVideo show failed
     *
     * @param error RewardedVideoAd show error reason
     */
    void onRewardedVideoAdShowFailed(Error error);

    /**
     * called when rewardedVideo is clicked
     */
    void onRewardedVideoAdClicked();

    /**
     * called when rewardedVideo is closed
     */
    void onRewardedVideoAdClosed();

    /**
     * called when rewardedVideo starts to play
     */
    void onRewardedVideoAdStarted();

    /**
     * called when rewardedVideo play ends
     */
    void onRewardedVideoAdEnded();

    /**
     * called when rewardedVideo can be rewarded
     */
    void onRewardedVideoAdRewarded();
}
