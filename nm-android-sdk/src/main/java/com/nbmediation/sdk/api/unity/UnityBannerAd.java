package com.nbmediation.sdk.api.unity;

import android.view.View;

import com.nbmediation.sdk.banner.BannerAd;

/**
 * Created by jiantao.tu on 2020/5/25.
 */
public class UnityBannerAd {

    private BannerAd mBannerAd;

    private View mReadyView;


    UnityBannerAd(BannerAd bannerAd) {
        this.mBannerAd = bannerAd;
    }

    private boolean isReady = false;

    private boolean isDestroy = false;

    synchronized boolean isReady() {
        return isReady;
    }

    synchronized void setReady(boolean ready) {
        isReady = ready;
    }

    public synchronized boolean isDestroy() {
        return isDestroy;
    }

    public synchronized void setDestroy(boolean destroy) {
        isDestroy = destroy;
    }

    synchronized View getReadyView() {
        return mReadyView;
    }

    synchronized void setReadyView(View readyView) {
        this.mReadyView = readyView;
    }

    public void destroy() {
        isDestroy = true;
        isReady = false;
        mBannerAd.destroy();
        mReadyView = null;
    }
}
