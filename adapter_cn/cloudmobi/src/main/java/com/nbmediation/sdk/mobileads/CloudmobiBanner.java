// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;

import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;
import com.suib.base.callback.EmptyAdEventListener;
import com.suib.base.core.SuibSDK;
import com.suib.base.core.ZCNative;
import com.suib.base.enums.AdSize;

import java.lang.ref.WeakReference;
import java.util.Map;

public class CloudmobiBanner extends CustomBannerEvent {

    private static String TAG = "OM-Cloudmobi: ";
    private View mBannerView;

    private Activity mActivity;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        this.mActivity = activity;

        String appKey = config.get("AppKey");
        String instanceKey = config.get("InstanceKey");
        if (null == appKey || !(appKey instanceof String)) {
            return;
        }
        if (null == instanceKey || !(instanceKey instanceof String)) {
            return;
        }
        SuibSDK.initialize(activity, appKey);
        loadBannerAd(activity, instanceKey, config);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_18;
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
    }

    private void loadBannerAd(Activity activity, String codeId, Map<String, String> config) {
        int[] size = getBannerSize(config);
        AdSize mAdSize = null;
        for (AdSize adSize : AdSize.values()) {
            if (size[0] == adSize.getWidth() && adSize.getHeight() == adSize.getHeight()) {
                mAdSize = adSize;
                break;
            }
        }
        if (mAdSize != null) {
            SuibSDK.getBannerAd(activity, codeId, mAdSize, new BannerListener(this));
        } else {
            throw new RuntimeException("Cloudmobi error,不支持的广告大小，width=" + size[0]
                    + ",height=" + size[1]
                    + ",具体原因请参考com.suib.base.enums.AdSize类");
        }

    }


    public static class BannerListener extends EmptyAdEventListener {

        private WeakReference<CloudmobiBanner> mReference;

        BannerListener(CloudmobiBanner banner) {
            mReference = new WeakReference<>(banner);
        }


        @Override
        public void onReceiveAdSucceed(ZCNative zcNative) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            CloudmobiBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            AdLog.getSingleton().LogD(TAG + "onReceiveAdSucceed");
            banner.mBannerView = zcNative;
            banner.onInsReady(zcNative);
        }

        @Override
        public void onReceiveAdFailed(ZCNative zcNative) {
            CloudmobiBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            String message = "";
            if (zcNative != null) {
                message = zcNative.getErrorsMsg();
            }
            AdLog.getSingleton().LogD(TAG + "onReceiveAdFailed " + message);
            banner.onInsError(message);
        }

        @Override
        public void onAdClicked(ZCNative zcNative) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            CloudmobiBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            banner.onInsClicked();
            AdLog.getSingleton().LogD(TAG + "onAdClicked");
        }

        @Override
        public void onAdClosed(ZCNative zcNative) {
            AdLog.getSingleton().LogD(TAG + "onAdClosed");
        }
    }

}
