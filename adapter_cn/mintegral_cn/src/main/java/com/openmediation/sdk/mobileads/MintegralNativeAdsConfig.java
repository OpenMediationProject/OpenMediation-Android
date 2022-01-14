package com.openmediation.sdk.mobileads;

import com.mbridge.msdk.nativex.view.MBMediaView;
import com.mbridge.msdk.out.Campaign;
import com.mbridge.msdk.out.MBBidNativeHandler;
import com.mbridge.msdk.out.MBNativeHandler;

public class MintegralNativeAdsConfig {
    private Campaign campaign;
    private MBNativeHandler nativeHandler;
    private MBBidNativeHandler bidNativeHandler;
    private String bidToken;
    private MBMediaView adnMediaView;

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public MBNativeHandler getNativeHandler() {
        return nativeHandler;
    }

    public void setNativeHandler(MBNativeHandler nativeHandler) {
        this.nativeHandler = nativeHandler;
    }

    public MBBidNativeHandler getBidNativeHandler() {
        return bidNativeHandler;
    }

    public void setBidNativeHandler(MBBidNativeHandler bidNativeHandler) {
        this.bidNativeHandler = bidNativeHandler;
    }

    public String getBidToken() {
        return bidToken;
    }

    public void setBidToken(String bidToken) {
        this.bidToken = bidToken;
    }

    public void setMBMediaView(MBMediaView adnMediaView) {
        this.adnMediaView = adnMediaView;
    }

    public MBMediaView getMBMediaView() {
        return adnMediaView;
    }

}
