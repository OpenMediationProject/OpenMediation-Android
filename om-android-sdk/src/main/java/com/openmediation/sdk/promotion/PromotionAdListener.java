// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.promotion;

import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;

/**
 * Listener for promotion events. Implementers of this interface will receive events for promotion ads
 */
public interface PromotionAdListener {

    /**
     * called when Ad availability changed
     *
     * @param available represent Ad available status
     */
    void onPromotionAdAvailabilityChanged(boolean available);

    /**
     * called when promotionAd is shown
     */
    void onPromotionAdShowed(Scene scene);

    /**
     * called when promotionAd show failed
     *
     * @param error Promotion ads show failed reason
     */
    void onPromotionAdShowFailed(Scene scene, Error error);

    /**
     * called when promotionAd closes
     */
    void onPromotionAdHidden(Scene scene);

    /**
     * called when promotionAd is clicked
     */
    void onPromotionAdClicked(Scene scene);

}
