// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.promotion;

import android.app.Activity;

import com.crosspromotion.sdk.core.OmAdNetworkManager;

import java.util.Map;

public final class PromotionAd {

    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady(String placementId) {
        return OmAdNetworkManager.getInstance().isPromotionAdReady(placementId);
    }

    /**
     * Load ad.
     */
    public static void loadAd(String placementId, Map extras) {
        OmAdNetworkManager.getInstance().loadPromotionAd(placementId, extras);
    }

    /**
     * shows ad with default placement and default scene
     */
    public static void showAd(Activity activity, PromotionAdRect rect, String placementId) {
        OmAdNetworkManager.getInstance().showPromotionAd(activity, rect, placementId);
    }

    /**
     * shows ad with default placement and default scene
     */
    public static void hideAd(String placementId) {
        OmAdNetworkManager.getInstance().hidePromotionAd(placementId);
    }

    /**
     * Set the {@link PromotionAdListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     *
     * @param listener the listener
     */
    public static void setAdListener(String placementId, PromotionAdListener listener) {
        OmAdNetworkManager.getInstance().setPromotionAdListener(placementId, listener);
    }
}