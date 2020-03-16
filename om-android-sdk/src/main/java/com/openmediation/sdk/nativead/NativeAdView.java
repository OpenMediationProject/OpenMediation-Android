// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.nativead;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;


/**
 * Native Ads display wrapper, User must provide a {@link NativeAdView} instance,contains{@link MediaView},
 * {@link AdIconView} and TextView to display Native ads
 *
 * 
 */
public class NativeAdView extends RelativeLayout {
    private MediaView mediaView;
    private AdIconView adIconView;
    private View titleView;
    private View descView;
    private View callToActionView;

    public NativeAdView(Context context) {
        super(context);
    }

    public void setTitleView(View titleView) {
        this.titleView = titleView;
    }

    public void setDescView(View descView) {
        this.descView = descView;
    }

    public void setMediaView(MediaView mediaView) {
        this.mediaView = mediaView;
    }

    public void setCallToActionView(View callToActionView) {
        this.callToActionView = callToActionView;
    }

    public void setAdIconView(AdIconView adIconView) {
        this.adIconView = adIconView;
    }

    public MediaView getMediaView() {
        return mediaView;
    }

    public View getTitleView() {
        return titleView;
    }

    public View getDescView() {
        return descView;
    }

    public AdIconView getAdIconView() {
        return adIconView;
    }

    public View getCallToActionView() {
        return callToActionView;
    }
}
