// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.view.ViewGroup;

import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.AdnAdInfo;
import com.openmediation.sdk.mediation.MediationInfo;
import com.openmediation.sdk.mediation.NativeAdCallback;
import com.openmediation.sdk.utils.AdLog;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

import java.util.List;
import java.util.Map;

public class TencentNativeManager {

    private static class Holder {
        private static final TencentNativeManager INSTANCE = new TencentNativeManager();
    }

    private TencentNativeManager() {
    }

    public static TencentNativeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initAd(Context context, Map<String, Object> extras, final NativeAdCallback callback) {
        String appKey = (String) extras.get("AppKey");
        boolean result = TencentAdManagerHolder.init(context.getApplicationContext(), appKey);
        if (result) {
            if (callback != null) {
                callback.onNativeAdInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onNativeAdInitFailed(AdapterErrorBuilder.buildInitError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TencentAdAdapter", "Init Failed"));
            }
        }
    }

    public void loadAd(Context context, final String adUnitId, Map<String, Object> extras, final NativeAdCallback callback) {
        try {
            int width = 0;
            int height = 0;
            if (extras != null) {
                try {
                    width = Integer.parseInt(extras.get("width").toString());
                } catch (Exception ignored) {
                }
                try {
                    height = Integer.parseInt(extras.get("height").toString());
                } catch (Exception ignored) {
                }
            }

            if (width <= 0) {
                width = ADSize.FULL_WIDTH;
            }
            if (height <= 0) {
                height = ADSize.AUTO_HEIGHT;
            }
            ADSize size = new ADSize(width, height);
            InnerAdListener listener = new InnerAdListener(adUnitId, callback);
            NativeExpressAD expressAD = new NativeExpressAD(context, size, adUnitId, listener);
            expressAD.loadAD(1);
        } catch (Exception e) {
            if (callback != null) {
                callback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TencentAdAdapter", "Unknown Error"));
            }
        }
    }

    public void destroyAd(String adUnitId, AdnAdInfo adInfo) {
        if (adInfo != null && adInfo.getAdnNativeAd() instanceof NativeExpressADView) {
            NativeExpressADView adView = (NativeExpressADView) adInfo.getAdnNativeAd();
            adView.destroy();
        }
    }

    private class InnerAdListener implements NativeExpressAD.NativeExpressADListener {

        private String mAdUnitId;
        private NativeAdCallback mAdCallback;

        private InnerAdListener(String adUnitId, NativeAdCallback callback) {
            this.mAdUnitId = adUnitId;
            this.mAdCallback = callback;
        }

        @Override
        public void onADLoaded(List<NativeExpressADView> list) {
            if (list == null || list.isEmpty() || list.get(0) == null) {
                if (mAdCallback != null) {
                    mAdCallback.onNativeAdInitFailed(AdapterErrorBuilder.buildLoadError(
                            AdapterErrorBuilder.AD_UNIT_NATIVE, "TencentAdAdapter", "No Fill"));
                }
                return;
            }
            NativeExpressADView ad = list.get(0);
            AdLog.getSingleton().LogE("Native type: " + (ad.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO));
            ad.render();
        }

        @Override
        public void onRenderFail(NativeExpressADView nativeExpressADView) {
            if (mAdCallback != null) {
                mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TencentAdAdapter", "Native Render Failed"));
            }
        }

        @Override
        public void onRenderSuccess(NativeExpressADView adView) {
            AdnAdInfo adInfo = new AdnAdInfo();
            adInfo.setType(MediationInfo.MEDIATION_ID_6);
            adInfo.setTemplateRender(true);
            adInfo.setView(adView);
            adInfo.setAdnNativeAd(adView);
            if (mAdCallback != null) {
                mAdCallback.onNativeAdLoadSuccess(adInfo);
            }
        }

        @Override
        public void onADExposure(NativeExpressADView adView) {
            AdLog.getSingleton().LogD("TencentAd NativeAd onADExposure");
            if (mAdCallback != null) {
                mAdCallback.onNativeAdImpression();
            }
        }

        @Override
        public void onADClicked(NativeExpressADView adView) {
            AdLog.getSingleton().LogD("TencentNative onADClicked: " + mAdUnitId);
            if (mAdCallback != null) {
                mAdCallback.onNativeAdAdClicked();
            }
        }

        @Override
        public void onADClosed(NativeExpressADView adView) {
            AdLog.getSingleton().LogD("TencentNative onADClosed: " + mAdUnitId);
            if (adView != null && adView.getParent() instanceof ViewGroup) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
        }

        @Override
        public void onADLeftApplication(NativeExpressADView adView) {

        }

        @Override
        public void onADOpenOverlay(NativeExpressADView adView) {

        }

        @Override
        public void onADCloseOverlay(NativeExpressADView adView) {
            AdLog.getSingleton().LogD("TencentNative onADCloseOverlay: " + mAdUnitId);
        }

        @Override
        public void onNoAD(AdError adError) {
            if (mAdCallback != null) {
                mAdCallback.onNativeAdLoadFailed(AdapterErrorBuilder.buildLoadError(
                        AdapterErrorBuilder.AD_UNIT_NATIVE, "TencentAdAdapter", adError.getErrorCode(), adError.getErrorMsg()));
            }
        }
    }
}
