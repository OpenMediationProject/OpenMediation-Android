// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.adn.nativead;

import com.openmediation.sdk.adn.core.BaseAdListener;
import com.openmediation.sdk.adn.utils.error.Error;

public interface NativeAdListener extends BaseAdListener {

    void onNativeAdReady(String placementId, Ad ad);

    void onNativeAdFailed(String placementId, Error error);

    void onNativeAdClicked(String placementId);

    void onNativeAdShowFailed(String placementId, Error error);
}
