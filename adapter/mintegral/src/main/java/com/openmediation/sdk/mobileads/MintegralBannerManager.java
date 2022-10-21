// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.mbridge.msdk.out.BannerAdListener;
import com.mbridge.msdk.out.BannerSize;
import com.mbridge.msdk.out.MBBannerView;
import com.mbridge.msdk.out.MBridgeIds;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralBannerManager {

    private static final String PAY_LOAD = "pay_load";

    private final ConcurrentHashMap<String, MBBannerView> mBannerViews;

    private static class BannerHolder {
        private static final MintegralBannerManager INSTANCE = new MintegralBannerManager();
    }

    private MintegralBannerManager() {
        mBannerViews = new ConcurrentHashMap<>();
    }

    public static MintegralBannerManager getInstance() {
        return BannerHolder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final BannerAdCallback callback, Boolean userConsent, Boolean ageRestricted) {
        String appKey = (String) extras.get("AppKey");
        MintegralSingleTon.getInstance().initSDK(context, appKey, new MintegralSingleTon.InitCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onBannerAdInitSuccess();
                }
            }

            @Override
            public void onFailed(String msg) {
                if (callback != null) {
                    callback.onBannerAdInitFailed(AdapterErrorBuilder.buildInitError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "MintegralAdapter", msg));
                }
            }
        }, userConsent, ageRestricted);
    }

    public void loadAd(Context context, String adUnitId, Map<String, Object> extras, BannerAdCallback callback) {
        try {
            String payload = "";
            if (extras.containsKey(PAY_LOAD) && extras.get(PAY_LOAD) != null) {
                payload = extras.get(PAY_LOAD).toString();
            }
            MBBannerView bannerView = new MBBannerView(context.getApplicationContext());
            BannerSize adSize = getAdSize(context, extras);
            bannerView.init(adSize, "", adUnitId);
            bannerView.setLayoutParams(new RelativeLayout.LayoutParams(dip2px(context, adSize.getWidth()), dip2px(context, adSize.getHeight())));
            bannerView.setRefreshTime(0);
            InnerBannerAdListener listener = new InnerBannerAdListener(bannerView, adUnitId, callback);
            bannerView.setBannerAdListener(listener);
            if (TextUtils.isEmpty(payload)) {
                bannerView.load();
            } else {
                bannerView.loadFromBid(payload);
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "MintegralAdapter", "Unknown Error, " + e.getMessage()));
            }
        }
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerViews.containsKey(adUnitId)) {
            MBBannerView bannerView = mBannerViews.remove(adUnitId);
            bannerView.release();
            bannerView = null;
        }
    }

    private static int dip2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    private class InnerBannerAdListener implements BannerAdListener {

        private MBBannerView mBannerView;
        private String mAdUnitId;
        private BannerAdCallback mAdCallback;

        private InnerBannerAdListener(MBBannerView bannerView, String adUnitId, BannerAdCallback callback) {
            this.mBannerView = bannerView;
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onLoadFailed(MBridgeIds mBridgeIds, String msg) {
            mBannerViews.remove(mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "MintegralAdapter", msg));
            }
        }

        @Override
        public void onLoadSuccessed(MBridgeIds mBridgeIds) {
            if (mBannerView != null) {
                mBannerViews.put(mAdUnitId, mBannerView);
                if (mAdCallback != null) {
                    mAdCallback.onBannerAdLoadSuccess(mBannerView);
                }
            }
        }

        @Override
        public void onLogImpression(MBridgeIds mBridgeIds) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdImpression();
            }
        }

        @Override
        public void onClick(MBridgeIds mBridgeIds) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdAdClicked();
            }
        }

        @Override
        public void onLeaveApp(MBridgeIds mBridgeIds) {

        }

        @Override
        public void showFullScreen(MBridgeIds mBridgeIds) {

        }

        @Override
        public void closeFullScreen(MBridgeIds mBridgeIds) {

        }

        @Override
        public void onCloseBanner(MBridgeIds mBridgeIds) {

        }
    }

    private BannerSize getAdSize(Context context, Map<String, Object> config) {
        String bannerDesc = MediationUtil.getBannerDesc(config);
        switch (bannerDesc) {
            case MediationUtil.DESC_LEADERBOARD:
                return new BannerSize(BannerSize.DEV_SET_TYPE, 728, 90);
            case MediationUtil.DESC_RECTANGLE:
                return new BannerSize(BannerSize.MEDIUM_TYPE, 300, 250);
            case MediationUtil.DESC_SMART:
                if (MediationUtil.isLargeScreen(context)) {
                    return new BannerSize(BannerSize.DEV_SET_TYPE, 728, 90);
                }
            default:
                return new BannerSize(BannerSize.STANDARD_TYPE, 320, 50);
        }
    }

}