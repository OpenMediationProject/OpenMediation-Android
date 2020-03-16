// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.NativeAdView;

public abstract class CustomNativeEvent extends CustomAdEvent {
    protected AdInfo mAdInfo = new AdInfo();

    public abstract void registerNativeView(NativeAdView adView);
}
