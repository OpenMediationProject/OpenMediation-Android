// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.promotion;

import android.app.Activity;

import com.openmediation.sdk.core.BaseOmAds;
import com.openmediation.sdk.core.OmManager;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.model.Scene;

/**
 * AdTiming PromotionAd API
 */
public class PromotionAd extends BaseOmAds {

    /**
     * Returns default placement's availability
     *
     * @return true or false
     */
    public static boolean isReady() {
        return OmManager.getInstance().isPromotionAdReady("");
    }

    /**
     * Returns specific scene's cap status
     *
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return isSceneCapped(CommonConstants.PROMOTION, scene);
    }

    /**
     * Returns specific scene's info
     *
     * @return {@link Scene}
     */
    public static Scene getSceneInfo(String scene) {
        return getSceneInfo(CommonConstants.PROMOTION, scene);
    }

    /**
     * Loads ad for default Placement
     */
    public static void loadAd() {
        OmManager.getInstance().loadPromotionAd("");
    }

    /**
     * shows ad with default placement and default scene
     */
    public static void showAd(Activity activity, PromotionAdRect rect) {
        showAd(activity, rect, "");
    }

    /**
     * shows ad with default placement and specific scene
     *
     * @param scene optional param ,if null, shows default scene
     */
    public static void showAd(Activity activity, PromotionAdRect rect, String scene) {
        OmManager.getInstance().showPromotionAd(activity, "", rect, scene);
    }

    /**
     * hide ad with default placement and specific scene
     */
    public static void hideAd() {
        OmManager.getInstance().hidePromotionAd("");
    }

    /**
     * Set the {@link PromotionAdListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     */
    public static void setAdListener(PromotionAdListener listener) {
        OmManager.getInstance().setPromotionAdListener("", listener);
    }

    public static void addAdListener(PromotionAdListener listener) {
        OmManager.getInstance().addPromotionAdListener("", listener);
    }

    public static void removeAdListener(PromotionAdListener listener) {
        OmManager.getInstance().removePromotionAdListener("", listener);
    }
}
