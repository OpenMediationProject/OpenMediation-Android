// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.core.imp.interstitial;

import com.crosspromotion.sdk.AdsActivity;
import com.crosspromotion.sdk.core.AbstractAdsManager;
import com.crosspromotion.sdk.core.CallbackBridge;
import com.crosspromotion.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.utils.constant.CommonConstants;

public final class InterstitialAdImp extends AbstractAdsManager {

    public InterstitialAdImp(String placementId) {
        super(placementId);
    }

    @Override
    protected int getAdType() {
        return CommonConstants.INTERSTITIAL;
    }

    public void setListener(InterstitialAdListener adListener) {
        mListenerWrapper.setInterstitialListener(adListener);
    }

    @Override
    public void showAds() {
        super.showAds();
        CallbackBridge.addListenerToMap(mPlacementId, this);
        show(AdsActivity.class);
    }

    @Override
    public void destroy() {
        super.destroy();
        CallbackBridge.removeListenerFromMap(mPlacementId);
    }
}
