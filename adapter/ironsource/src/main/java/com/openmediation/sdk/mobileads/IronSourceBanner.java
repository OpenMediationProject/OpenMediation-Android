package com.openmediation.sdk.mobileads;

import android.app.Activity;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class IronSourceBanner extends CustomBannerEvent implements BannerListener {

    private AtomicBoolean mDidBannerInited = new AtomicBoolean(false);
    private IronSourceBannerLayout mIrBannerLayout;
    private boolean isFailed;

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_15;
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!mDidBannerInited.getAndSet(true)) {
            String appKey = config.get("AppKey");
            IronSource.init(activity, appKey, IronSource.AD_UNIT.BANNER);
        }
        if (mIrBannerLayout == null || isFailed) {
            int[] size = getBannerSize(config);
            ISBannerSize bannerSize = new ISBannerSize(size[0], size[1]);
            mIrBannerLayout = IronSource.createBanner(activity, bannerSize);
            mIrBannerLayout.setBannerListener(this);
            IronSource.loadBanner(mIrBannerLayout);
        } else {
            onInsReady(mIrBannerLayout);
        }
    }

    @Override
    public void destroy(Activity activity) {
        if (mIrBannerLayout != null) {
            IronSource.destroyBanner(mIrBannerLayout);
        }
    }

    @Override
    public void onBannerAdLoaded() {
        if (isDestroyed) {
            return;
        }
        isFailed = false;
        onInsReady(mIrBannerLayout);
    }

    @Override
    public void onBannerAdLoadFailed(IronSourceError ironSourceError) {
        if (isDestroyed) {
            return;
        }
        isFailed = true;
        onInsError(ironSourceError.getErrorMessage());
    }

    @Override
    public void onBannerAdClicked() {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    @Override
    public void onBannerAdScreenPresented() {

    }

    @Override
    public void onBannerAdScreenDismissed() {

    }

    @Override
    public void onBannerAdLeftApplication() {

    }
}
