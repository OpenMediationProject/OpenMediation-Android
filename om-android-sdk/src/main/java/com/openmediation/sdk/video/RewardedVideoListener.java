// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.video;

import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;

/**
 * Listener for rewarded video events. Implementers of this interface will receive events for rewarded video ads
 */
public interface RewardedVideoListener {

    /**
     * called when rewardedVideo availability changed
     *
     * @param available if RewardedVideoAd is available
     */
    void onRewardedVideoAvailabilityChanged(boolean available);

    /**
     * called when rewardedVideo shows
     *
     * @param scene the scene
     */
    void onRewardedVideoAdShowed(Scene scene);

    /**
     * called when rewardedVideo show failed
     *
     * @param scene the scene
     * @param error RewardedVideoAd show error reason
     */
    void onRewardedVideoAdShowFailed(Scene scene, Error error);

    /**
     * called when rewardedVideo is clicked
     *
     * @param scene the scene
     */
    void onRewardedVideoAdClicked(Scene scene);

    /**
     * called when rewardedVideo is closed
     *
     * @param scene the scene
     */
    void onRewardedVideoAdClosed(Scene scene);

    /**
     * called when rewardedVideo starts to play
     *
     * @param scene the scene
     */
    void onRewardedVideoAdStarted(Scene scene);

    /**
     * called when rewardedVideo play ends
     *
     * @param scene the scene
     */
    void onRewardedVideoAdEnded(Scene scene);

    /**
     * called when rewardedVideo can be rewarded
     *
     * @param scene the scene
     */
    void onRewardedVideoAdRewarded(Scene scene);
}
