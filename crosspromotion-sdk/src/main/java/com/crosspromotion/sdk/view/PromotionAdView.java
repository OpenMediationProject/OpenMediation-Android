// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.crosspromotion.sdk.bean.AdBean;
import com.crosspromotion.sdk.core.OmAdNetworkManager;
import com.crosspromotion.sdk.promotion.PromotionAdRect;
import com.crosspromotion.sdk.report.AdReport;
import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.GpUtil;
import com.crosspromotion.sdk.utils.PUtils;
import com.crosspromotion.sdk.utils.ResUtil;
import com.crosspromotion.sdk.utils.webview.BaseWebView;
import com.crosspromotion.sdk.utils.webview.BaseWebViewClient;
import com.crosspromotion.sdk.utils.webview.JsBridge;
import com.crosspromotion.sdk.utils.webview.JsBridgeConstants;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DensityUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.SdkUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;

import org.json.JSONObject;

import java.io.File;

public final class PromotionAdView implements JsBridge.MessageListener {

    private static final float SCALE = 132f / 153f;

    private BaseWebView mAdView;
    private AdWebClient mWebClient;
    private JsBridge mAdJsBridge;

    private boolean mIsShowing;

    private AdBean mAdBean;
    private String mPlacementId;

    private Context mContext;
    private FrameLayout frameLayout;

    @Override
    public void onReceiveMessage(String method, JSONObject data) {
        if (JsBridgeConstants.METHOD_CLICK.equals(method)) {
            click();
        } else if (JsBridgeConstants.METHOD_WV_CLICK.equals(method)) {
            wvClick();
        } else if (JsBridgeConstants.METHOD_PUSH_EVENT.equals(method)) {
            if (data == null) {
                return;
            }
            String event = data.optString("e");
            if (!TextUtils.isEmpty(event)) {
                addEvent(event);
            }
        }
    }

    private static class PvHolder {
        private static final PromotionAdView INSTANCE = new PromotionAdView();
    }

    private PromotionAdView() {
        mContext = AdtUtil.getApplication();
    }

    public static PromotionAdView getInstance() {
        return PvHolder.INSTANCE;
    }

    public void init() {
        if (mAdView != null) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    createWebView();
                    mAdView.loadUrl("about:blank");
                } catch (Throwable e) {
                    DeveloperLog.LogD("PromotionAdView", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }

    private void createWebView() {
        if (mAdView == null) {
            synchronized (PromotionAdView.class) {
                if (mAdView == null) {
                    mAdView = new BaseWebView(AdtUtil.getApplication());
                    mAdView.setBackgroundColor(0);
                    mWebClient = new AdWebClient(AdtUtil.getApplication(), "");
                    mAdView.setWebViewClient(mWebClient);
                }
            }
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public boolean load(String placementId, final AdBean adBean) throws Exception {
        createWebView();
        String url = adBean.getResources().get(0);
        boolean existCache = Cache.existCache(mAdView.getContext(), url);
        if (existCache) {
            mPlacementId = placementId;
            mAdBean = adBean;
            mWebClient.setPackageName(mAdBean.getPkgName());
            if (mAdJsBridge != null) {
                mAdJsBridge.release();
            }
            mAdJsBridge = new JsBridge();
            mAdJsBridge.injectJavaScript(mAdView);
            mAdJsBridge.setPlacementId(placementId);
            mAdJsBridge.setCampaign(adBean.getAdString());
            mAdJsBridge.setMessageListener(this);
            File file = Cache.getCacheFile(mAdView.getContext(), url, null);
            String data = IOUtil.toString(IOUtil.getFileInputStream(file), CommonConstants.CHARTSET_UTF8);
            mAdView.loadDataWithBaseURL(url, data, "text/html",
                    CommonConstants.CHARTSET_UTF8, null);
        }
        return existCache;
    }

    public void show(final Activity activity, final PromotionAdRect rect, final String placementId, final AdBean adBean, final AdRenderListener listener) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean exist = load(placementId, adBean);
                    if (!exist) {
                        if (listener != null) {
                            listener.onRenderFailed("PromotionAd not ready");
                        }
                        return;
                    }
                    frameLayout = new FrameLayout(activity);
                    frameLayout.setBackgroundColor(0);
                    FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    activity.addContentView(frameLayout, fParams);

                    ViewGroup.LayoutParams params = getViewLayoutParams(rect, activity);
                    if (mAdJsBridge != null) {
                        mAdJsBridge.reportShowEvent();
                    }
                    frameLayout.addView(mAdView, params);
                    DeveloperLog.LogD("PromotionAdView show success");
                    mIsShowing = true;
                    if (listener != null) {
                        listener.onRenderSuccess();
                    }
                } catch (Throwable e) {
                    DeveloperLog.LogD("PromotionAdView", e);
                    CrashUtil.getSingleton().saveException(e);
                    if (listener != null) {
                        listener.onRenderFailed(e.getMessage());
                    }
                }
            }
        });
    }

    public void hide() {
        if (mAdJsBridge != null) {
            mAdJsBridge.release();
            mAdJsBridge = null;
        }
        if (frameLayout != null) {
            frameLayout.removeAllViews();
            frameLayout = null;
        }
        if (mAdView != null) {
            try {
                ViewParent parent = mAdView.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(mAdView);
                }
            } catch (Throwable ignored) {
            }
        }
        mIsShowing = false;
    }

    public void release() {
        if (mAdView == null) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (frameLayout != null) {
                        frameLayout.removeAllViews();
                        frameLayout = null;
                    }
                    if (mAdView.getParent() != null) {
                        ViewGroup parent = (ViewGroup) mAdView.getParent();
                        parent.removeView(mAdView);
                    }
                    mAdView.removeAllViews();
                    mAdView.removeJavascriptInterface("");
                    mAdView.setWebViewClient(null);
                    mAdView.setWebChromeClient(null);
                    mAdView.freeMemory();
                    mAdView.destroy();
                    mAdView = null;
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private void click() {
        DeveloperLog.LogD("PromotionAdView js called click");
        AdReport.CLKReport(mContext, mPlacementId, mAdBean);
        PUtils.doClick(mContext, mPlacementId, mAdBean);
        OmAdNetworkManager.getInstance().getPromotionManager(mPlacementId).onAdsClicked();
    }

    private void addEvent(String event) {
        DeveloperLog.LogD("PromotionAdView js called addEvent");
        OmAdNetworkManager.getInstance().getPromotionManager(mPlacementId).onAddEvents(event);
    }

    private void wvClick() {
        DeveloperLog.LogD("PromotionAdView js called wvClick");
        AdReport.CLKReport(mContext, mPlacementId, mAdBean);
        OmAdNetworkManager.getInstance().getPromotionManager(mPlacementId).onAdsClicked();
    }

    private ViewGroup.LayoutParams getViewLayoutParams(PromotionAdRect rect, Activity activity) {
        int width = rect.getWidth();
        int height = rect.getHeight();
        if (width > 0) {
            height = Math.round(width / SCALE);
        } else {
            if (height > 0) {
                width = Math.round(height * SCALE);
            }
        }
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                DensityUtil.dip2px(activity, width),
                DensityUtil.dip2px(activity, height));
        mAdView.setX(DensityUtil.getPhoneWidth(activity) * rect.getScaleX());
        mAdView.setY(DensityUtil.getPhoneHeight(activity) * rect.getScaleY());
        mAdView.setRotation(rect.getAngle());
        return params;
    }

    protected static class AdWebClient extends BaseWebViewClient {
        private boolean isJumped = false;

        public AdWebClient(Context activity, String pkgName) {
            super(activity, pkgName);
        }

        public void setPackageName(String pkgName) {
            mPkgName = pkgName;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isJumped) {
                isJumped = false;
                return true;
            }
            boolean result = super.shouldOverrideUrlLoading(view, url);
            if (!result) {
                try {
                    if (GpUtil.isGp(url)) {
                        GpUtil.goGp(view.getContext().getApplicationContext(), url);
                    } else {
                        if (SdkUtil.isAcceptedScheme(url)) {
                            view.loadUrl(url);
                        }
                    }
                } catch (Exception e) {
                    DeveloperLog.LogD("shouldOverrideUrlLoading error", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            } else {
                isJumped = true;
                view.stopLoading();
            }
            return true;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse response = ResUtil.shouldInterceptRequest(view, url);
            if (response == null) {
                DeveloperLog.LogD("response null:" + url);
            }
            return response == null ? super.shouldInterceptRequest(view, url) : response;
        }
    }

    public interface AdRenderListener {
        void onRenderSuccess();

        void onRenderFailed(String msg);
    }
}

