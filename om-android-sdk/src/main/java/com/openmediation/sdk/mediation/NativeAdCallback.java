// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import com.openmediation.sdk.nativead.AdInfo;

public interface NativeAdCallback {
    /**
     * called when third-party ad network ads init success
     */
    void onNativeAdInitSuccess();

    /**
     * called when third-party ad network ads init failed
     *
     * @param error init failed reason
     */
    void onNativeAdInitFailed(AdapterError error);

    /**
     * called when third-party ad network ads load success
     */
    void onNativeAdLoadSuccess(AdInfo info);

    /**
     * called when third-party ad network load failed
     *
     * @param error load failed reason
     */
    void onNativeAdLoadFailed(AdapterError error);

    /**
     * called when third-party ad network ads are clicked
     */
    void onNativeAdAdClicked();
}
