// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.rewardedvideo;

import com.openmediation.sdk.core.BaseInsBidCallback;
import com.openmediation.sdk.core.BaseInsExpiredCallback;
import com.openmediation.sdk.mediation.AdapterError;

public interface RvManagerListener extends BaseInsBidCallback, BaseInsExpiredCallback {
    void onRewardedVideoInitSuccess(RvInstance rvInstance);

    void onRewardedVideoInitFailed(RvInstance rvInstance, AdapterError error);

    void onRewardedVideoAdShowFailed(RvInstance rvInstance, AdapterError error);

    void onRewardedVideoAdShowSuccess(RvInstance rvInstance);

    void onRewardedVideoAdClosed(RvInstance rvInstance);

    void onRewardedVideoLoadSuccess(RvInstance rvInstance);

    void onRewardedVideoLoadFailed(RvInstance rvInstance, AdapterError error);

    void onRewardedVideoAdStarted(RvInstance rvInstance);

    void onRewardedVideoAdEnded(RvInstance rvInstance);

    void onRewardedVideoAdRewarded(RvInstance rvInstance);

    void onRewardedVideoAdClicked(RvInstance rvInstance);
}
