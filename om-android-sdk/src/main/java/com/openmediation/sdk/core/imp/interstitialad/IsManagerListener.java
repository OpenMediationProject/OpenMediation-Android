// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.interstitialad;

import com.openmediation.sdk.core.BaseInsBidCallback;
import com.openmediation.sdk.mediation.AdapterError;

public interface IsManagerListener extends BaseInsBidCallback {

    void onInterstitialAdInitSuccess(IsInstance isInstance);

    void onInterstitialAdInitFailed(IsInstance isInstance, AdapterError error);

    void onInterstitialAdShowFailed(IsInstance isInstance, AdapterError error);

    void onInterstitialAdShowSuccess(IsInstance isInstance);

    void onInterstitialAdClicked(IsInstance isInstance);

    void onInterstitialAdClosed(IsInstance isInstance);

    void onInterstitialAdLoadSuccess(IsInstance isInstance);

    void onInterstitialAdLoadFailed(IsInstance isInstance, AdapterError error);
}
