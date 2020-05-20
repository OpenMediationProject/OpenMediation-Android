// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.interstitial;

import com.nbmediation.sdk.core.OmManager;
import com.nbmediation.sdk.core.BaseOmAds;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.model.Scene;

/**
 * InterstitialAd API
 */
public class InterstitialAd extends BaseOmAds {

    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady() {
        return OmManager.getInstance().isInterstitialAdReady("");
    }

    /**
     * Returns specific scene's cap status
     *
     * @param scene the scene
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return isSceneCapped(CommonConstants.INTERSTITIAL, scene);
    }

    /**
     * Returns specific scene's info
     *
     * @param scene the scene
     * @return {@link Scene}
     */
    public static Scene getSceneInfo(String scene) {
        return getSceneInfo(CommonConstants.INTERSTITIAL, scene);
    }

    /**
     * Load ad.
     */
    public static void loadAd() {
        OmManager.getInstance().loadInterstitialAd("");
    }

    /**
     * shows ad with default placement and default scene
     */
    public static void showAd() {
        showAd("");
    }

    /**
     * shows ad with default placement and specific scene
     *
     * @param scene optional param ,if null, show default scene
     */
    public static void showAd(String scene) {
        OmManager.getInstance().showInterstitialAd("", scene);
    }

    /**
     * Set the {@link InterstitialAdListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     *
     * @param listener the listener
     */
    public static void setAdListener(InterstitialAdListener listener) {
        OmManager.getInstance().setInterstitialAdListener("", listener);
    }
}
