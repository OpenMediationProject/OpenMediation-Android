// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.app.Activity;

import java.util.Map;

/**
 * This interface is the base API for PromotionAds
 */
public interface PromotionAdApi {

    /**
     * Third-party ad networks can be inited in this method
     *
     * @param activity must be an activity
     * @param dataMap  some configs for third-party ad networks on AdTiming dashboard
     * @param callback {@link PromotionAdCallback}
     */
    void initPromotionAd(Activity activity, Map<String, Object> dataMap, PromotionAdCallback callback);

    /**
     * Calls third-party ad networks to get ads
     *
     * @param activity must be an activity
     * @param adUnitId ad unit id on third-party ad networks
     * @param callback {@link PromotionAdCallback}
     */
    void loadPromotionAd(Activity activity, String adUnitId, Map<String, Object> extras, PromotionAdCallback callback);

    /**
     * Calls third-party ad networks to show ads
     *
     * @param activity must be an activity
     * @param adUnitId ad unit id on third-party ad networks
     * @param callback {@link PromotionAdCallback}
     */
    void showPromotionAd(Activity activity, String adUnitId, Map<String, Object> extras, PromotionAdCallback callback);

    /**
     * Calls third-party ad networks to show ads
     *
     * @param adUnitId ad unit id on third-party ad networks
     * @param callback {@link PromotionAdCallback}
     */
    void hidePromotionAd(String adUnitId, PromotionAdCallback callback);

    /**
     * Checks if third-party ad networks have available ads
     *
     * @param adUnitId ad unit id on third-party ad networks
     * @return third-party ad networks have available ads or not
     */
    boolean isPromotionAdAvailable(String adUnitId);
}
