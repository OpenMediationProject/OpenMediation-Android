// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import android.view.View;

public interface BannerAdCallback {
    void onBannerAdLoaded(View view);

    void onBannerAdLoadFailed(String error);

    void onBannerAdClicked();
}
