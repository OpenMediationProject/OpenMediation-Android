// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;

import com.hytt.hyadxopensdk.HyAdXOpenSdk;
import com.hytt.hyadxopensdk.hyadxopenad.HyAdXOpenBannerAd;
import com.hytt.hyadxopensdk.interfoot.HyAdXOpenBannerListener;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.Map;

public class HyAdXOpenBanner extends CustomBannerEvent {
    private static String TAG = "OM-HyAdXOpen: ";
    private View mBannerView;

    private HyAdXOpenBannerAd hyAdXOpenBannerAd;
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
        initSdk(appKey);
        loadBannerAd(instanceKey);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_18;
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
    }

    private void loadBannerAd(String codeId) {
        create(codeId);
        hyAdXOpenBannerAd.load();
    }

    private void initSdk(String appKey) {
        HyAdXOpenSdk.getInstance().init(mActivity, appKey);
    }


    private void create(final String adUnitId) {
        hyAdXOpenBannerAd = new HyAdXOpenBannerAd(mActivity, adUnitId,
                mActivity.getResources().getDisplayMetrics().widthPixels,
                mActivity.getResources().getDisplayMetrics().widthPixels * 100 / 640,
                new OpenBannerListener(this));
    }

    public static class OpenBannerListener implements HyAdXOpenBannerListener {
        private WeakReference<HyAdXOpenBanner> mReference;

        OpenBannerListener(HyAdXOpenBanner banner) {
            mReference = new WeakReference<>(banner);
        }

        @Override
        public void onAdFill(int code, String searchId, View view) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            HyAdXOpenBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            AdLog.getSingleton().LogD(TAG + "onAdFill");
            if (banner.hyAdXOpenBannerAd != null) {
                banner.hyAdXOpenBannerAd.show();
            }
            banner.mBannerView = view;
            banner.onInsReady(view);
        }

        @Override
        public void onAdShow(int code, String searchId) {
            AdLog.getSingleton().LogD(TAG + "onAdShow");
        }

        @Override
        public void onAdClick(int code, String searchId) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            HyAdXOpenBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            banner.onInsClicked();
            AdLog.getSingleton().LogD(TAG + "onAdClicked");
        }

        @Override
        public void onAdFailed(int code, String message) {
            HyAdXOpenBanner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            AdLog.getSingleton().LogD(TAG + "onAdFailed " + message + " code:" + code);
            banner.onInsError(message);
        }

        @Override
        public void onAdClose(int code, String searchId) {
            AdLog.getSingleton().LogD(TAG + "onAdClose");
        }
    }

}
