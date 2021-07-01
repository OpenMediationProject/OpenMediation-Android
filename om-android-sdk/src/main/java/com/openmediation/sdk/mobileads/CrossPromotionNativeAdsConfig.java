/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import com.crosspromotion.sdk.nativead.Ad;
import com.crosspromotion.sdk.nativead.NativeAd;

public class CrossPromotionNativeAdsConfig {
    private NativeAd mNativeAd;
    private Ad mAd;

    public NativeAd getNativeAd() {
        return mNativeAd;
    }

    public void setNativeAd(NativeAd nativeAd) {
        this.mNativeAd = nativeAd;
    }

    public Ad getContent() {
        return mAd;
    }

    public void setContent(Ad content) {
        this.mAd = content;
    }

}
