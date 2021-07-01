// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core.imp.banner;

import com.openmediation.sdk.mediation.AdapterError;

public interface BnManagerListener {
    void onBannerAdInitSuccess(BnInstance instance);

    void onBannerAdInitFailed(BnInstance instance, AdapterError error);

    void onBannerAdLoadSuccess(BnInstance instance);

    void onBannerAdLoadFailed(BnInstance instance, AdapterError error);

    void onBannerAdShowSuccess(BnInstance instance);

    void onBannerAdShowFailed(BnInstance instance, AdapterError error);

    void onBannerAdAdClicked(BnInstance instance);
}
