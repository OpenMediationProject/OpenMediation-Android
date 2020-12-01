// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.promotion;

import com.openmediation.sdk.utils.error.Error;

public interface CpManagerListener {

    void onPromotionAdInitSuccess(CpInstance iaInstance);

    void onPromotionAdInitFailed(Error error, CpInstance iaInstance);

    void onPromotionAdLoadSuccess(CpInstance iaInstance);

    void onPromotionAdLoadFailed(Error error, CpInstance iaInstance);

    void onPromotionAdShowSuccess(CpInstance iaInstance);

    void onPromotionAdShowFailed(Error error, CpInstance iaInstance);

    void onPromotionAdVisible(CpInstance iaInstance);

    void onPromotionAdClicked(CpInstance iaInstance);

    void onPromotionAdHidden(CpInstance iaInstance);

}
