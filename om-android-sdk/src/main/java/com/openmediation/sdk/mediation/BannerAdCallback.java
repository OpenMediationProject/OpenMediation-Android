// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.view.View;

public interface BannerAdCallback extends BidCallback {

    /**
     * called when third-party ad network ads init success
     */
    void onBannerAdInitSuccess();

    /**
     * called when third-party ad network ads init failed
     *
     * @param error init failed reason
     */
    void onBannerAdInitFailed(AdapterError error);

    /**
     * called when third-party ad network ads load success
     */
    void onBannerAdLoadSuccess(View view);

    /**
     * called when third-party ad network load failed
     *
     * @param error load failed reason
     */
    void onBannerAdLoadFailed(AdapterError error);

    void onBannerAdImpression();

    /**
     * called when third-party ad network ads are clicked
     */
    void onBannerAdAdClicked();
}
