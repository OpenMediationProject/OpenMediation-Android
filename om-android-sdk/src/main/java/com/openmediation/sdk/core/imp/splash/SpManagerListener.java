// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.splash;

import com.openmediation.sdk.core.BaseInsBidCallback;
import com.openmediation.sdk.mediation.AdapterError;

public interface SpManagerListener extends BaseInsBidCallback {
    void onSplashAdInitSuccess(SpInstance instance);

    void onSplashAdInitFailed(SpInstance instance, AdapterError error);

    void onSplashAdLoadSuccess(SpInstance instance);

    void onSplashAdLoadFailed(SpInstance instance, AdapterError error);

    void onSplashAdShowSuccess(SpInstance instance);

    void onSplashAdShowFailed(SpInstance instance, AdapterError error);

    void onSplashAdTick(SpInstance instance, long millisUntilFinished);

    void onSplashAdAdClicked(SpInstance instance);

    void onSplashAdDismissed(SpInstance instance);
}
