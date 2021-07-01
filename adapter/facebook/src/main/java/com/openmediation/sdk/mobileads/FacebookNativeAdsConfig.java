/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;

public class FacebookNativeAdsConfig {
    private NativeAd nativeAd;
    private AdOptionsView adOptionsView;
    private MediaView mediaView;
    private MediaView iconView;

    public NativeAd getNativeAd() {
        return nativeAd;
    }

    public void setNativeAd(NativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }

    public AdOptionsView getAdOptionsView() {
        return adOptionsView;
    }

    public void setAdOptionsView(AdOptionsView adOptionsView) {
        this.adOptionsView = adOptionsView;
    }

    public MediaView getMediaView() {
        return mediaView;
    }

    public void setMediaView(MediaView mediaView) {
        this.mediaView = mediaView;
    }

    public MediaView getIconView() {
        return iconView;
    }

    public void setIconView(MediaView iconView) {
        this.iconView = iconView;
    }
}
