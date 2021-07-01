// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.nativead;

import com.openmediation.sdk.mediation.AdapterError;

public interface NaManagerListener {
    void onNativeAdInitSuccess(NaInstance instance);

    void onNativeAdInitFailed(NaInstance instance, AdapterError error);

    void onNativeAdLoadSuccess(NaInstance instance);

    void onNativeAdLoadFailed(NaInstance instance, AdapterError error);

    void onNativeAdAdClicked(NaInstance instance);
}
