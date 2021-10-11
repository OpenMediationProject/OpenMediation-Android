// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.nativead;

import com.openmediation.sdk.core.BaseInsBidCallback;
import com.openmediation.sdk.mediation.AdapterError;
import com.openmediation.sdk.nativead.AdInfo;

public interface NaManagerListener extends BaseInsBidCallback {
    void onNativeAdInitSuccess(NaInstance instance);

    void onNativeAdInitFailed(NaInstance instance, AdapterError error);

    void onNativeAdLoadSuccess(NaInstance instance, AdInfo adInfo);

    void onNativeAdLoadFailed(NaInstance instance, AdapterError error);

    void onNativeAdImpression(NaInstance instance);

    void onNativeAdAdClicked(NaInstance instance);
}
