// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;

import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class TencentAdBanner extends CustomBannerEvent implements UnifiedBannerADListener {
    private static String TAG = "OM-TencentAd: ";
    private UnifiedBannerView mBannerView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        loadBannerAd(activity, config.get("AppKey"), mInstancesKey);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_6;
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        if (mBannerView != null) {
            mBannerView.destroy();
        }
    }

    private void loadBannerAd(Activity activity, String appId, String codeId) {
        if (mBannerView != null) {
            mBannerView.destroy();
        }
        mBannerView = new UnifiedBannerView(activity, appId, codeId, this);
        mBannerView.loadAD();
    }

    @Override
    public void onNoAD(AdError adError) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Banner ad load failed: code " + adError.getErrorCode() + " " + adError.getErrorMsg());
        onInsError(adError.getErrorMsg());
    }

    @Override
    public void onADReceive() {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "Banner ad onADReceive");
        onInsReady(mBannerView);
    }

    @Override
    public void onADExposure() {

    }

    @Override
    public void onADClosed() {

    }

    @Override
    public void onADClicked() {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
        AdLog.getSingleton().LogD(TAG + "onAdClicked");
    }

    @Override
    public void onADLeftApplication() {

    }

    @Override
    public void onADOpenOverlay() {

    }

    @Override
    public void onADCloseOverlay() {

    }

}
