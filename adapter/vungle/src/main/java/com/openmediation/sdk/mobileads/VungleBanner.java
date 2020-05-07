package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;
import com.vungle.warren.AdConfig;
import com.vungle.warren.Banners;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleBanner extends CustomBannerEvent implements LoadAdCallback, PlayAdCallback {

    private com.vungle.warren.VungleBanner mBanner;
    private AdConfig.AdSize mAdSize = AdConfig.AdSize.BANNER;

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_5;
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        mAdSize = getAdSize(config);
        checkInitAndLoad(activity, config);
    }

    @Override
    public void destroy(Activity activity) {
        if (mBanner != null) {
            mBanner.destroyAd();
        }
    }

    @Override
    public void onAdLoad(String id) {
        if (isDestroyed) {
            return;
        }
        if (mBanner != null) {
            mBanner.destroyAd();
        }
        mBanner = Banners.getBanner(id, mAdSize, this);
        if (mBanner != null) {
            mBanner.disableLifeCycleManagement(true);
            onInsReady(mBanner);
            mBanner.renderAd();
        } else {
            onInsError("Load Vungle banner error");
        }
    }

    @Override
    public void onAdStart(String id) {

    }

    @Override
    public void onAdEnd(String id, boolean completed, boolean isCTAClicked) {
        if (isDestroyed) {
            return;
        }
        if (isCTAClicked) {
            onInsClicked();
        }
    }

    @Override
    public void onError(String id, VungleException exception) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogE("Load Vungle Banner error : " + exception.getExceptionCode() + " msg : " + exception.getLocalizedMessage());
        onInsError("Load Vungle Banner error : " + exception.getExceptionCode() + " msg : " + exception.getLocalizedMessage());
    }

    private void loadBanner() {
        if (mBanner != null) {
            mBanner.finishAd();
        }
        Banners.loadBanner(mInstancesKey, mAdSize, this);
    }

    private void checkInitAndLoad(Activity activity, Map<String, String> config) {
        if (Vungle.isInitialized()) {
            loadBanner();
            return;
        }
        String appKey = config.get("AppKey");
        if (TextUtils.isEmpty(appKey)) {
            AdLog.getSingleton().LogE("Load Vungle error When init Vungle SDK with empty appKey");
            onInsError("Load Vungle error When init Vungle SDK with empty appKey");
            return;
        }
        Vungle.init(appKey, activity.getApplicationContext(), new InitCallback() {
            @Override
            public void onSuccess() {
                AdLog.getSingleton().LogD("OM-Vungle", "Vungle init success ");
                loadBanner();
            }

            @Override
            public void onError(VungleException exception) {
                AdLog.getSingleton().LogE("OM-Vungle: Vungle init failed " + exception.getLocalizedMessage());
                onInsError("Load Vungle error When init Vungle SDK : " + exception.getLocalizedMessage());
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {
            }
        });
    }

    private AdConfig.AdSize getAdSize(Map<String, String> config) {
        int[] size = getBannerSize(config);
        int width = size[0];
        int height = size[1];
        if (width == AdConfig.AdSize.BANNER_LEADERBOARD.getWidth()
                && height == AdConfig.AdSize.BANNER_LEADERBOARD.getHeight()) {
            return AdConfig.AdSize.BANNER_LEADERBOARD;
        } else if (width == AdConfig.AdSize.BANNER_SHORT.getWidth()
                && height == AdConfig.AdSize.BANNER_SHORT.getHeight()) {
            return AdConfig.AdSize.BANNER_SHORT;
        }
        return AdConfig.AdSize.BANNER;
    }
}
