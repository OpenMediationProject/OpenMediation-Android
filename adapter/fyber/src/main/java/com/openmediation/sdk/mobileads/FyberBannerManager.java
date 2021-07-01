/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fyber.inneractive.sdk.external.ImpressionData;
import com.fyber.inneractive.sdk.external.InneractiveAdRequest;
import com.fyber.inneractive.sdk.external.InneractiveAdSpot;
import com.fyber.inneractive.sdk.external.InneractiveAdSpotManager;
import com.fyber.inneractive.sdk.external.InneractiveAdViewEventsListenerWithImpressionData;
import com.fyber.inneractive.sdk.external.InneractiveAdViewUnitController;
import com.fyber.inneractive.sdk.external.InneractiveErrorCode;
import com.fyber.inneractive.sdk.external.InneractiveUnitController.AdDisplayError;
import com.fyber.inneractive.sdk.external.InneractiveUserConfig;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.BannerAdCallback;
import com.openmediation.sdk.mediation.MediationUtil;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FyberBannerManager {

    private ConcurrentHashMap<String, InneractiveAdSpot> mBannerSpots;
    private ConcurrentHashMap<String, ViewGroup> mBannerViews;

    private static class BannerHolder {
        private static final FyberBannerManager INSTANCE = new FyberBannerManager();
    }

    private FyberBannerManager() {
        mBannerSpots = new ConcurrentHashMap<>();
        mBannerViews = new ConcurrentHashMap<>();
    }

    public static FyberBannerManager getInstance() {
        return BannerHolder.INSTANCE;
    }

    public void loadAd(Context context, String adUnitId, Map<String, Object> extras,
                       InneractiveAdRequest request,
                       BannerAdCallback callback) {
        RelativeLayout bannerView = new RelativeLayout(context);
        int[] adSize = getAdSize(context, extras);
        int width = adSize[0], height = adSize[1];
        if (adSize[0] < 0 || adSize[1] < 0) {
            width = 320;
            height = 50;
        }
        bannerView.setLayoutParams(new RelativeLayout.LayoutParams(
                FyberUtil.dpToPixels(context, width), FyberUtil.dpToPixels(context, height)));
        InneractiveAdSpot bannerAdSpot = InneractiveAdSpotManager.get().createSpot();
        InneractiveAdViewUnitController controller = new InneractiveAdViewUnitController();
        bannerAdSpot.addUnitController(controller);
        bannerAdSpot.setRequestListener(new InnerBannerAdListener(bannerView, adUnitId, callback));
        bannerAdSpot.requestAd(request);
        mBannerViews.put(adUnitId, bannerView);
        mBannerSpots.put(adUnitId, bannerAdSpot);
    }

    public boolean isAdAvailable(String adUnitId) {
        return !TextUtils.isEmpty(adUnitId) && mBannerSpots.containsKey(adUnitId);
    }

    public void destroyAd(String adUnitId) {
        if (!TextUtils.isEmpty(adUnitId) && mBannerSpots.containsKey(adUnitId)) {
            ViewGroup viewGroup = mBannerViews.remove(adUnitId);
            if (viewGroup != null) {
                viewGroup.removeAllViews();
                viewGroup = null;
            }
            InneractiveAdSpot adSpot = mBannerSpots.remove(adUnitId);
            adSpot.destroy();
        }
    }

    private class InnerBannerAdListener implements InneractiveAdSpot.RequestListener {
        private String mAdUnitId;
        private BannerAdCallback mAdCallback;
        private ViewGroup mBannerView;

        private InnerBannerAdListener(ViewGroup bannerLayout, String adUnitId, BannerAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
            this.mBannerView = bannerLayout;
        }

        @Override
        public void onInneractiveSuccessfulAdRequest(InneractiveAdSpot adSpot) {
            if (adSpot == null) {
                if (mAdCallback != null) {
                    mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_BANNER, "FyberAdapter", "Wrong Banner Spot: No Fill"));
                }
                return;
            }
            InneractiveAdViewUnitController controller = (InneractiveAdViewUnitController) adSpot.getSelectedUnitController();
            controller.setEventsListener(new InneractiveAdViewEventsListenerWithImpressionData() {
                @Override
                public void onAdImpression(InneractiveAdSpot adSpot, ImpressionData impressionData) {
                }

                @Override
                public void onAdImpression(InneractiveAdSpot adSpot) {
                    if (mAdCallback != null) {
                        mAdCallback.onBannerAdImpression();
                    }
                }

                @Override
                public void onAdClicked(InneractiveAdSpot adSpot) {
                    if (mAdCallback != null) {
                        mAdCallback.onBannerAdAdClicked();
                    }
                }

                @Override
                public void onAdWillCloseInternalBrowser(InneractiveAdSpot adSpot) {
                }

                @Override
                public void onAdWillOpenExternalApp(InneractiveAdSpot adSpot) {
                }

                @Override
                public void onAdEnteredErrorState(InneractiveAdSpot inneractiveAdSpot, AdDisplayError error) {
                    AdLog.getSingleton().LogE("OM-Fyber: onAdEnteredErrorState - " + error.getMessage());
                    if (mAdCallback != null) {
                        mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, "FyberAdapter", error.getMessage()));
                    }
                }

                @Override
                public void onAdExpanded(InneractiveAdSpot adSpot) {
                }

                @Override
                public void onAdResized(InneractiveAdSpot adSpot) {
                }

                @Override
                public void onAdCollapsed(InneractiveAdSpot adSpot) {
                }
            });
            controller.bindView(mBannerView);
            //banner加载成功后返回view
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadSuccess(mBannerView);
            }
        }

        @Override
        public void onInneractiveFailedAdRequest(InneractiveAdSpot inneractiveAdSpot, InneractiveErrorCode errorCode) {
            if (mAdCallback != null) {
                mAdCallback.onBannerAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, "FyberAdapter", errorCode.toString()));
            }
        }
    }

    private int[] getAdSize(Context activity, Map<String, Object> config) {
        String desc = MediationUtil.getBannerDesc(config);
        int widthDp = 320;
        int heightDp = 50;

        if (MediationUtil.DESC_RECTANGLE.equals(desc)) {
            widthDp = 300;
            heightDp = 250;
        } else if (MediationUtil.DESC_SMART.equals(desc) && MediationUtil.isLargeScreen(activity)) {
            widthDp = 728;
            heightDp = 90;
        }
        return new int[]{widthDp, heightDp};
    }
}
