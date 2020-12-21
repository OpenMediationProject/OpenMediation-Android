package com.crosspromotion.sdk;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.core.BaseActivity;
import com.crosspromotion.sdk.core.imp.video.VideoAdImp;
import com.crosspromotion.sdk.report.AdReport;
import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.PUtils;
import com.crosspromotion.sdk.utils.error.ErrorBuilder;
import com.crosspromotion.sdk.utils.error.ErrorCode;
import com.crosspromotion.sdk.utils.webview.ActWebView;
import com.crosspromotion.sdk.utils.webview.AdsWebView;
import com.crosspromotion.sdk.utils.webview.JsBridge;
import com.crosspromotion.sdk.utils.webview.JsBridgeConstants;
import com.crosspromotion.sdk.view.DrawCrossMarkView;
import com.openmediation.sdk.utils.DensityUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;

import org.json.JSONObject;

import java.io.File;

public class AdsActivity extends BaseActivity implements JsBridge.MessageListener {

    private DrawCrossMarkView mDrawCrossMarkView;
    private boolean isBackEnable = true;
    private JsBridge mJsBridge;
//    private VolumeReceiver mReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        if (mJsBridge != null) {
            mJsBridge.reportEvent(JsBridgeConstants.EVENT_RESUME);
        }
    }

    @Override
    protected void onPause() {
        if (mJsBridge != null) {
            mJsBridge.reportEvent(JsBridgeConstants.EVENT_PAUSE);
        }
        super.onPause();
    }

    @Override
    protected void initViewAndLoad(String impUrl) {
        try {
            super.initViewAndLoad(impUrl);
            if (mJsBridge == null) {
                mJsBridge = new JsBridge();

            }
            mJsBridge.setMessageListener(this);
            mJsBridge.injectJavaScript(mAdView);
            mJsBridge.setPlacementId(mPlacementId);
            mJsBridge.setSceneName(mSceneName);
            mJsBridge.setAbt(mAbt);
            mJsBridge.setCampaign(mAdBean.getAdString());

//            if (mReceiver == null) {
//                mReceiver = new VolumeReceiver();
//            }
//            mReceiver.setMuteModeListener(this);
//            mReceiver.register(this);

            //back button
            mDrawCrossMarkView = new DrawCrossMarkView(this, Color.GRAY);
            mLytAd.addView(mDrawCrossMarkView);
            mDrawCrossMarkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            mDrawCrossMarkView.setVisibility(View.GONE);
            updateCloseBtnStatus();

            int size = DensityUtil.dip2px(this, 20);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(30, 30, 30, 30);
            mDrawCrossMarkView.setLayoutParams(params);

            mAdView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

            loadAdUrl(impUrl);
            if (mAdsManager != null) {
                mAdsManager.onAdsShowed();
            }
        } catch (Exception e) {
            DeveloperLog.LogE(e.getMessage());
            callbackAdShowFailedOnUIThread(ErrorBuilder.build(ErrorCode.CODE_SHOW_UNKNOWN_EXCEPTION));
        }
    }

    @Override
    public void onBackPressed() {
        if (isBackEnable) {
            callbackAdCloseOnUIThread();
            super.onBackPressed();
        }
    }

//    @Override
//    public void onMuteMode(boolean mute) {
//        if (mJsBridge == null) {
//            return;
//        }
//        mJsBridge.reportEvent(mute ? JsBridgeConstants.EVENT_MUTE : JsBridgeConstants.EVENT_UNMUTE);
//    }

    @Override
    protected void onDestroy() {
        callbackAdCloseOnUIThread();
//        if (mReceiver != null) {
//            mReceiver.setMuteModeListener(null);
//            mReceiver.unRegister(this);
//            mReceiver = null;
//        }
        if (mLytAd != null) {
            mLytAd.removeAllViews();
        }
        if (mJsBridge != null) {
            mJsBridge.release();
            mJsBridge = null;
        }

        ActWebView.getInstance().destroy("sdk");

        if (mAdView != null) {
            mAdView.stopLoading();
            mAdView.removeJavascriptInterface("playin");
            AdsWebView.getInstance().destroy(mAdView, "sdk");
        }

        mAdBean = null;
        mAdsManager = null;
        super.onDestroy();
    }

    @Override
    public void onReceiveMessage(String method, JSONObject data) {
        if (JsBridgeConstants.METHOD_CLOSE.equals(method)) {
            close();
        } else if (JsBridgeConstants.METHOD_CLICK.equals(method)) {
            click();
        } else if (JsBridgeConstants.METHOD_WV_CLICK.equals(method)) {
            wvClick();
        } else if (JsBridgeConstants.METHOD_ClOSE_VISIBLE.equals(method)) {
            if (data == null) {
                return;
            }
            isBackEnable = data.optBoolean("visible");
            updateCloseBtnStatus();
        } else if (JsBridgeConstants.METHOD_AD_REWARDED.equals(method)) {
            addRewarded();
        } else if (JsBridgeConstants.METHOD_PUSH_EVENT.equals(method)) {
            if (data == null) {
                return;
            }
            String event = data.optString("e");
            if (!TextUtils.isEmpty(event)) {
                addEvent(event);
            }
        } else if (JsBridgeConstants.METHOD_REPORT_VIDEO_PROGRESS.equals(method)) {
            if (data == null) {
                return;
            }
            int progress = data.optInt("progress");
            videoProgress(progress);
        }
    }

    private void close() {
        callbackAdCloseOnUIThread();
        finish();
    }

    private void click() {
        AdReport.CLKReport(this, mPlacementId, mAdBean);
        PUtils.doClick(this, mPlacementId, mAdBean);
        callbackAdClickOnUIThread();
    }

    private void addEvent(String event) {
        if (mAdsManager != null) {
            mAdsManager.onAddEvents(event);
        }
    }

    private void wvClick() {
        AdReport.CLKReport(this, mPlacementId, mAdBean);
        callbackAdClickOnUIThread();
    }

    private void videoProgress(int progress) {
        if (progress == 0) {
            if (mAdsManager != null && mAdsManager instanceof VideoAdImp) {
                ((VideoAdImp) mAdsManager).onRewardedVideoStarted();
            }
        } else if (progress == 100) {
            if (mAdsManager != null && mAdsManager instanceof VideoAdImp) {
                ((VideoAdImp) mAdsManager).onRewardedVideoEnded();
            }
        }
    }

    private void addRewarded() {
        if (mAdsManager != null && mAdsManager instanceof VideoAdImp) {
            ((VideoAdImp) mAdsManager).onRewardedRewarded();
        }
    }

    private void loadAdUrl(String url) throws Exception {
        boolean existCache = Cache.existCache(this, url);
        if (existCache) {
            File file = Cache.getCacheFile(this, url, null);
            String data = IOUtil.toString(IOUtil.getFileInputStream(file), CommonConstants.CHARTSET_UTF8);
            mAdView.loadDataWithBaseURL(url, data, "text/html", CommonConstants.CHARTSET_UTF8, null);
        } else {
            mAdView.loadUrl(url);
        }
        if (mJsBridge != null) {
            mJsBridge.reportShowEvent();
        }
    }

    private void updateCloseBtnStatus() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isBackEnable) {
                    if (mDrawCrossMarkView != null) {
                        mDrawCrossMarkView.setVisibility(View.VISIBLE);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(mDrawCrossMarkView, "alpha", 0f, 1f);
                        animator.setDuration(500);
                        animator.start();
                    }
                } else {
                    if (mDrawCrossMarkView != null) {
                        mDrawCrossMarkView.setVisibility(View.GONE);
                    }
                }
            }
        };

        if (mLytAd != null) {
            mLytAd.postDelayed(runnable, 3000);
        }
    }
}
