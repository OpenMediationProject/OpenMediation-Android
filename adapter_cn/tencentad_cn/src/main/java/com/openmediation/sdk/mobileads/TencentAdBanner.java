// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class TencentAdBanner extends CustomBannerEvent implements UnifiedBannerADListener {
    private static String TAG = "OM-TencentAd: ";
    private UnifiedBannerView mBannerView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        GDTADManager.getInstance().initWith(activity.getApplicationContext(), config.get("AppKey"));
        loadBannerAd(activity, mInstancesKey);
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

    private void loadBannerAd(Activity activity, String codeId) {
        if (mBannerView != null) {
            mBannerView.destroy();
        }
        mBannerView = new UnifiedBannerView(activity, codeId, this);
        mBannerView.loadAD();
    }

    @Override
    public void onNoAD(AdError adError) {
        if (isDestroyed) {
            return;
        }
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, adError.getErrorCode(), adError.getErrorMsg()));
    }

    @Override
    public void onADReceive() {
        if (isDestroyed) {
            return;
        }
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
