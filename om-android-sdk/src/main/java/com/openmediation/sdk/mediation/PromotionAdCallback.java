// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

/**
 * This interface is used to notify adapter's lifecycle to AdTiming's SDK
 */
public interface PromotionAdCallback {
    /**
     * called t third-party ad network ads init success
     */
    void onPromotionAdInitSuccess();

    /**
     * called when third-party ad networks ads init failed
     *
     * @param error init failed reason
     */
    void onPromotionAdInitFailed(AdapterError error);

    /**
     * called at third-party ad networks ads load success
     */
    void onPromotionAdLoadSuccess();

    /**
     * called when third-party ad networks ads load failed
     *
     * @param error load failure reason
     */
    void onPromotionAdLoadFailed(AdapterError error);

    /**
     * called when third-party ad networks ads show success
     */
    void onPromotionAdShowSuccess();

    /**
     * called when third-party ad networks ads show failed
     *
     * @param error show failure reason
     */
    void onPromotionAdShowFailed(AdapterError error);

    /**
     * called when third-party ad network ads become visible to user
     */
    void onPromotionAdVisible();

    /**
     * called when third-party ad networks ads are clicked
     */
    void onPromotionAdClicked();

    void onPromotionAdHidden();
}
