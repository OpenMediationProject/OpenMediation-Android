// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.video;

import com.crosspromotion.sdk.core.BaseAdListener;
import com.crosspromotion.sdk.utils.error.Error;

public interface RewardedVideoListener extends BaseAdListener {
    void onRewardedVideoAdLoadSuccess(String placementId);

    void onRewardedVideoAdLoadFailed(String placementId, Error error);

    void onRewardedVideoAdShowed(String placementId);

    void onRewardedVideoAdShowFailed(String placementId, Error error);

    void onRewardedVideoAdClicked(String placementId);

    void onRewardedVideoAdRewarded(String placementId);

    void onRewardedVideoAdStarted(String placementId);

    void onRewardedVideoAdEnded(String placementId);

    void onRewardedVideoAdClosed(String placementId);

    void onVideoAdEvent(String placementId, String event);
}
