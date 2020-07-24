package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
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
import com.fyber.inneractive.sdk.external.InneractiveUserConfig;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.utils.AdLog;

import java.util.Map;

public class FyberBanner extends CustomBannerEvent implements InneractiveAdSpot.RequestListener {

    private InneractiveAdSpot mBannerSpot;
    private ViewGroup mBannerView;

    @Override
    public void setGDPRConsent(Context context, boolean consent) {
        super.setGDPRConsent(context, consent);
        InneractiveAdManager.setGdprConsent(consent);
    }

    @Override
    public void setUSPrivacyLimit(Context context, boolean value) {
        super.setUSPrivacyLimit(context, value);
        String ccpaStringVal = value ? "1YY-" : "1YN-";
        InneractiveAdManager.setUSPrivacyString(ccpaStringVal);
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!InneractiveAdManager.wasInitialized()) {
            String appKey = config.get("AppKey");
            InneractiveAdManager.initialize(activity, appKey);
            if (mUserConsent != null) {
                setGDPRConsent(activity, mUserConsent);
            }
            if (mUSPrivacyLimit != null) {
                setUSPrivacyLimit(activity, mUSPrivacyLimit);
            }
        }
        if (mBannerSpot != null) {
            mBannerSpot.destroy();
        }
        if (mBannerView == null) {
            mBannerView = new RelativeLayout(activity);
            int[] adSize = getAdSize(activity, config);
            int width = adSize[0], height = adSize[1];
            if (adSize[0] < 0 || adSize[1] < 0) {
                width = 320;
                height = 50;
            }
            mBannerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    FyberUtil.dpToPixels(activity, width), FyberUtil.dpToPixels(activity, height)));
        } else {
            mBannerView.removeAllViews();
        }
        mBannerSpot = InneractiveAdSpotManager.get().createSpot();
        InneractiveAdViewUnitController controller = new InneractiveAdViewUnitController();
        mBannerSpot.addUnitController(controller);

        InneractiveAdRequest request = new InneractiveAdRequest(mInstancesKey);
        if (mUserGender != null || mUserAge != null) {
            InneractiveUserConfig userConfig = new InneractiveUserConfig();
            if ("male".equals(mUserGender)) {
                userConfig.setGender(InneractiveUserConfig.Gender.MALE);
            } else if ("female".equals(mUserGender)) {
                userConfig.setGender(InneractiveUserConfig.Gender.FEMALE);
            }
            if (mUserAge != null) {
                userConfig.setAge(mUserAge);
            }
            request.setUserParams(userConfig);
        }
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
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Wrong Banner Spot: Received - " + adSpot + ", Actual - " + mBannerSpot));
            return;
        }
        InneractiveAdViewUnitController controller = (InneractiveAdViewUnitController) mBannerSpot.getSelectedUnitController();
        controller.setEventsListener(new InneractiveAdViewEventsListenerWithImpressionData() {
            @Override
            public void onAdImpression(InneractiveAdSpot adSpot, ImpressionData impressionData) {
            }

            @Override
            public void onAdImpression(InneractiveAdSpot adSpot) {
            }

            @Override
            public void onAdClicked(InneractiveAdSpot adSpot) {
                onInsClicked();
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
                onInsError(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error.getMessage()));
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
        onInsReady(mBannerView);
    }

    @Override
    public void onInneractiveFailedAdRequest(InneractiveAdSpot inneractiveAdSpot, InneractiveErrorCode errorCode) {
        onInsError(AdapterErrorBuilder.buildLoadError(
                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, errorCode.toString()));
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

    private int[] getAdSize(Activity activity, Map<String, String> config) {
        String desc = getBannerDesc(config);
        int widthDp = 320;
        int heightDp = 50;

        if (DESC_RECTANGLE.equals(desc)) {
            widthDp = 300;
            heightDp = 250;
        } else if (DESC_SMART.equals(desc) && isLargeScreen(activity)) {
            widthDp = 728;
            heightDp = 90;
        }
        return new int[] {widthDp, heightDp};
    }
}
