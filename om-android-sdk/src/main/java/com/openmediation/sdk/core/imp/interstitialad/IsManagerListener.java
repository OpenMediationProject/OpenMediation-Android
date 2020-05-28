// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.interstitialad;

import com.openmediation.sdk.utils.error.Error;

public interface IsManagerListener {

    void onInterstitialAdInitSuccess(IsInstance isInstance);

    void onInterstitialAdInitFailed(Error error, IsInstance isInstance);

    void onInterstitialAdShowFailed(Error error, IsInstance isInstance);

    void onInterstitialAdShowSuccess(IsInstance isInstance);

    void onInterstitialAdClick(IsInstance isInstance);

    void onInterstitialAdClosed(IsInstance isInstance);

    void onInterstitialAdLoadSuccess(IsInstance isInstance);

    void onInterstitialAdLoadFailed(Error error, IsInstance isInstance);
}
