// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.banner;

import android.content.Context;
import android.widget.FrameLayout;

import com.crosspromotion.sdk.core.imp.banner.BannerImp;

import java.util.Map;

public class BannerAd extends FrameLayout {

    private BannerImp mBannerImp;

    public BannerAd(Context context, String placementId) {
        super(context);
        mBannerImp = new BannerImp(placementId, this);
    }

    public void setAdListener(BannerAdListener listener) {
        mBannerImp.setListener(listener);
    }

    public void setAdSize(AdSize adSize) {
        mBannerImp.setAdSize(adSize);
    }

    public void loadAdWithPayload(String payload, Map extras) {
        mBannerImp.loadAdsWithPayload(payload, extras);
    }

    public void destroy() {
        mBannerImp.destroy();
    }
}
