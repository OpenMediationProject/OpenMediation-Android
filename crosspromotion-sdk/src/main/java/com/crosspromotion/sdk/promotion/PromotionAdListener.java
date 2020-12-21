// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.promotion;

import com.crosspromotion.sdk.core.BaseAdListener;
import com.crosspromotion.sdk.utils.error.Error;

public interface PromotionAdListener extends BaseAdListener {
    void onPromotionAdLoadSuccess(String placementId);

    void onPromotionAdLoadFailed(String placementId, Error error);

    void onPromotionAdShowed(String placementId);

    void onPromotionAdHidden(String placementId);

    void onPromotionAdShowFailed(String placementId, Error error);

    void onPromotionAdClicked(String placementId);
}
