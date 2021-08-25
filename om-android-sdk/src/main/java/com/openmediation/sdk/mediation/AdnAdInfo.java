/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mediation;

import com.openmediation.sdk.nativead.AdInfo;

public class AdnAdInfo extends AdInfo {

    /**
     * The NativeAd received from Network
     */
    private Object mAdnNativeAd;

    public Object getAdnNativeAd() {
        return mAdnNativeAd;
    }

    public void setAdnNativeAd(Object adnNativeAd) {
        this.mAdnNativeAd = adnNativeAd;
    }
}
