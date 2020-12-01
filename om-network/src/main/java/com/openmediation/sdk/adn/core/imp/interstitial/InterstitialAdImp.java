// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.adn.core.imp.interstitial;

import com.openmediation.sdk.adn.AdsActivity;
import com.openmediation.sdk.adn.core.AbstractAdsManager;
import com.openmediation.sdk.adn.core.CallbackBridge;
import com.openmediation.sdk.adn.interstitial.InterstitialAdListener;
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
