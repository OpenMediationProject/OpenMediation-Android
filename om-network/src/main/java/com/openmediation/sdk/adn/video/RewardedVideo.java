package com.openmediation.sdk.adn.video;

import com.openmediation.sdk.adn.core.OmAdNetworkManager;

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
    public static void loadAd(String placementId) {
        OmAdNetworkManager.getInstance().loadRewardedVideo(placementId);
    }

    public static void loadAdWithPayload(String placementId, String payload) {
        OmAdNetworkManager.getInstance().loadRewardedVideo(placementId, payload);
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
