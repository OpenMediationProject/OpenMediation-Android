// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.app.Activity;

import java.util.Map;

/**
 * This interface is the base api for rewardedVideo ads
 */
public interface RewardedVideoApi {

    /**
     * Third-party ad networks can be inited in this method
     *
     * @param activity must be an activity
     * @param dataMap  some configs for third-party ad networks on dashboard
     * @param callback {@link RewardedVideoCallback}
     */
    void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback);

    /**
     * Calls third-party ad networks to get ads
     *
     * @param activity must be an activity
     * @param adUnitId ad unit id on third-party ad networks
     * @param extras   the extras
     * @param callback {@link RewardedVideoCallback}
     */
    void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback);

    /**
     * Calls third-party ad networks to show ads
     *
     * @param activity must be an activity
     * @param adUnitId ad unit id on third-party ad networks
     * @param callback {@link RewardedVideoCallback}
     */
    void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback);

    /**
     * Checks if third-party ad networks have available ads
     *
     * @param adUnitId ad unit id on third-party ad networks
     * @return third -party ad networks have available ads or not
     */
    boolean isRewardedVideoAvailable(String adUnitId);
}
