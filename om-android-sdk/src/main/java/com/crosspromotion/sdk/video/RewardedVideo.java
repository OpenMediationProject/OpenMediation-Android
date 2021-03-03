package com.crosspromotion.sdk.video;

import com.crosspromotion.sdk.core.OmAdNetworkManager;

import java.util.Map;

public final class RewardedVideo {
    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady(String placementId) {
        return OmAdNetworkManager.getInstance().isRewardedVideoReady(placementId);
    }

    /**
     * Loads ads with default placement
     */
    public static void loadAdWithPayload(String placementId, String payload, Map extras) {
        OmAdNetworkManager.getInstance().loadRewardedVideo(placementId, payload, extras);
    }

    /**
     * showAd ads with default placement and default scene
     */
    public static void showAd(String placementId) {
        OmAdNetworkManager.getInstance().showRewardedVideo(placementId);
    }

    public static void setAdListener(String placementId, RewardedVideoListener listener) {
        OmAdNetworkManager.getInstance().setRewardedVideoListener(placementId, listener);
    }
}
