package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fyber.inneractive.sdk.external.ImpressionData;
import com.fyber.inneractive.sdk.external.InneractiveAdManager;
import com.fyber.inneractive.sdk.external.InneractiveAdRequest;
import com.fyber.inneractive.sdk.external.InneractiveAdSpot;
import com.fyber.inneractive.sdk.external.InneractiveAdSpotManager;
import com.fyber.inneractive.sdk.external.InneractiveAdViewEventsListenerWithImpressionData;
import com.fyber.inneractive.sdk.external.InneractiveAdViewUnitController;
import com.fyber.inneractive.sdk.external.InneractiveErrorCode;
import com.fyber.inneractive.sdk.external.InneractiveUnitController.AdDisplayError;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;

public class FyberBanner extends CustomBannerEvent implements InneractiveAdSpot.RequestListener {

    private InneractiveAdSpot mBannerSpot;
    private ViewGroup mBannerView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!InneractiveAdManager.wasInitialized()) {
            String appKey = config.get("AppKey");
            InneractiveAdManager.initialize(activity, appKey);
        }
        if (mBannerSpot != null) {
            mBannerSpot.destroy();
        }
        if (mBannerView == null) {
            mBannerView = new RelativeLayout(activity);
            int[] adSize = getBannerSize(config);
            mBannerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    FyberUtil.dpToPixels(activity, adSize[0]), FyberUtil.dpToPixels(activity, adSize[1])));
        } else {
            mBannerView.removeAllViews();
        }
        mBannerSpot = InneractiveAdSpotManager.get().createSpot();
        InneractiveAdViewUnitController controller = new InneractiveAdViewUnitController();
        mBannerSpot.addUnitController(controller);

        InneractiveAdRequest request = new InneractiveAdRequest(mInstancesKey);
        mBannerSpot.setRequestListener(this);
        mBannerSpot.requestAd(request);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_30;
    }

    @Override
    public void onInneractiveSuccessfulAdRequest(InneractiveAdSpot adSpot) {
        if (adSpot != mBannerSpot) {
            AdLog.getSingleton().LogE("Wrong Banner Spot: Received - " + adSpot + ", Actual - " + mBannerSpot);
            onInsError("Wrong Banner Spot: Received - " + adSpot + ", Actual - " + mBannerSpot);
            return;
        }
        InneractiveAdViewUnitController controller = (InneractiveAdViewUnitController) mBannerSpot.getSelectedUnitController();
        controller.setEventsListener(new InneractiveAdViewEventsListenerWithImpressionData() {
            @Override
            public void onAdImpression(InneractiveAdSpot adSpot, ImpressionData impressionData) {
                AdLog.getSingleton().LogE("ImpressionData" + impressionData.toString());
            }

            @Override
            public void onAdImpression(InneractiveAdSpot adSpot) {
                AdLog.getSingleton().LogE("onAdImpression");
            }

            @Override
            public void onAdClicked(InneractiveAdSpot adSpot) {
                AdLog.getSingleton().LogE("onAdClicked");
                onInsClicked();
            }

            @Override
            public void onAdWillCloseInternalBrowser(InneractiveAdSpot adSpot) {
                AdLog.getSingleton().LogE("onAdWillCloseInternalBrowser");
            }

            @Override
            public void onAdWillOpenExternalApp(InneractiveAdSpot adSpot) {
                AdLog.getSingleton().LogE("onAdWillOpenExternalApp");
            }

            @Override
            public void onAdEnteredErrorState(InneractiveAdSpot inneractiveAdSpot, AdDisplayError error) {
                AdLog.getSingleton().LogE("onAdEnteredErrorState - " + error.getMessage());
                onInsError("onAdEnteredErrorState - " + error.getMessage());
            }

            @Override
            public void onAdExpanded(InneractiveAdSpot adSpot) {
                AdLog.getSingleton().LogE("onAdExpanded");
            }

            @Override
            public void onAdResized(InneractiveAdSpot adSpot) {
                AdLog.getSingleton().LogE("onAdResized");
            }

            @Override
            public void onAdCollapsed(InneractiveAdSpot adSpot) {
                AdLog.getSingleton().LogE("onAdCollapsed");
            }
        });
        controller.bindView(mBannerView);
        //banner加载成功后返回view
        onInsReady(mBannerView);
    }

    @Override
    public void onInneractiveFailedAdRequest(InneractiveAdSpot inneractiveAdSpot, InneractiveErrorCode errorCode) {
        AdLog.getSingleton().LogE("Failed loading banner! with error: " + errorCode);
        onInsError("Failed loading banner! with error: " + errorCode);
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerSpot != null) {
            mBannerSpot.destroy();
        }
        if (mBannerView != null) {
            mBannerView.removeAllViews();
            mBannerView = null;
        }
    }
}
