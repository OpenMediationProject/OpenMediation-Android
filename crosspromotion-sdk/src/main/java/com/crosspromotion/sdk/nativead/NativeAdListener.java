// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.nativead;

import com.crosspromotion.sdk.core.BaseAdListener;
import com.crosspromotion.sdk.utils.error.Error;

public interface NativeAdListener extends BaseAdListener {

    void onNativeAdReady(String placementId, Ad ad);

    void onNativeAdFailed(String placementId, Error error);

    void onNativeAdClicked(String placementId);

    void onNativeAdShowFailed(String placementId, Error error);
}
