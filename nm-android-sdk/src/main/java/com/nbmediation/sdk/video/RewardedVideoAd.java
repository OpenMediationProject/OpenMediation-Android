// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.video;

import android.os.Looper;

import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.core.BaseOmAds;
import com.nbmediation.sdk.utils.HandlerUtil;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.model.Scene;

/**
 * RewardedVideoAd ads API
 */
public final class RewardedVideoAd extends BaseOmAds {

    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady() {
        return NmManager.getInstance().isRewardedVideoReady("");
    }

    /**
     * Returns specific scene cap status
     *
     * @param scene the scene
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return isSceneCapped(CommonConstants.VIDEO, scene);
    }

    /**
     * Returns specific scene's info
     *
     * @param scene the scene
     * @return {@link Scene}
     */
    public static Scene getSceneInfo(String scene) {
        return getSceneInfo(CommonConstants.VIDEO, scene);
    }

    /**
     * Loads ads with default placement
     */
    public static void loadAd() {
        NmManager.getInstance().loadRewardedVideo("");
    }

    /**
     * show ads with default placement and default scene
     */
    public static void showAd() {
        showAd("");
    }

    /**
     * shows ads with default placement and specific scene
     *
     * @param scene optional param ,if null, shows default scene
     */
    public static void showAd(final String scene) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NmManager.getInstance().showRewardedVideo("", scene);
            }
        });
    }

    /**
     * sets up a custom id to receive rewarded callback through S2S
     *
     * @param scene display scene, can be null
     * @param extId custom id
     */
    public static void setExtId(String scene, String extId) {
        NmManager.getInstance().setRewardedExtId("", scene, extId);
    }

    /**
     * Set the {@link RewardedVideoListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     *
     * @param listener the listener
     */
    public static void setAdListener(RewardedVideoListener listener) {
        NmManager.getInstance().setRewardedVideoListener("", listener);
    }
}
