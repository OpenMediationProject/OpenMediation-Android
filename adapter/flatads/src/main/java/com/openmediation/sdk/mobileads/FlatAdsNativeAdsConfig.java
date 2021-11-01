// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import com.flatads.sdk.builder.NativeAd;
import com.flatads.sdk.response.AdContent;

public class FlatAdsNativeAdsConfig {
    private NativeAd mNativeAd;
    private AdContent mAdContent;

    public NativeAd getNativeAd() {
        return mNativeAd;
    }

    public void setNativeAd(NativeAd nativeAd) {
        this.mNativeAd = nativeAd;
    }

    public AdContent getAdContent() {
        return mAdContent;
    }

    public void setAdContent(AdContent mAdContent) {
        this.mAdContent = mAdContent;
    }
}
