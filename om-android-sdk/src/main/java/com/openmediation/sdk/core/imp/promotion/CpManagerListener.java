// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.promotion;

import com.openmediation.sdk.core.BaseInsBidCallback;
import com.openmediation.sdk.mediation.AdapterError;

public interface CpManagerListener extends BaseInsBidCallback {

    void onPromotionAdInitSuccess(CpInstance cpInstance);

    void onPromotionAdInitFailed(CpInstance cpInstance, AdapterError error);

    void onPromotionAdLoadSuccess(CpInstance cpInstance);

    void onPromotionAdLoadFailed(CpInstance cpInstance, AdapterError error);

    void onPromotionAdShowSuccess(CpInstance cpInstance);

    void onPromotionAdShowFailed(CpInstance cpInstance, AdapterError error);

    void onPromotionAdVisible(CpInstance cpInstance);

    void onPromotionAdClicked(CpInstance cpInstance);

    void onPromotionAdHidden(CpInstance cpInstance);

}
