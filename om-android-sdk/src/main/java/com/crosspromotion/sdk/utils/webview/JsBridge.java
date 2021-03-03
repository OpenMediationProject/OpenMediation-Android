package com.crosspromotion.sdk.utils.webview;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.device.DeviceUtil;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class JsBridge {

    private BaseWebView mWebView;
    private MessageListener mListener;
    private AtomicBoolean isJsLoaded = new AtomicBoolean(false);
    private AtomicBoolean isReportShowCalled = new AtomicBoolean(false);
    private String placementId;
    private String sceneName;
    private String campaign;
    private int abt;

    public JsBridge() {
    }

    @SuppressLint("JavascriptInterface")
    public void injectJavaScript(WebView webView) {
        if (webView == null) {
            return;
        }
        mWebView = (BaseWebView) webView;
        mWebView.addJavascriptInterface(this, "sdk");
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public void setAbt(int abt) {
        this.abt = abt;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public void setMessageListener(MessageListener listener) {
        mListener = listener;
    }

    public void release() {
        isJsLoaded.set(false);
        isReportShowCalled.set(false);
        mListener = null;
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mWebView != null) {
                    mWebView.removeJavascriptInterface("sdk");
                }
            }
        });
    }

    @JavascriptInterface
    public void postMessage(String param) {
        try {
            if (TextUtils.isEmpty(param)) {
                return;
            }
            JSONObject object = new JSONObject(param);
            String m = object.optString("m");
            JSONObject data = object.optJSONObject("d");
            DeveloperLog.LogD("postMessage : " + " method : " + m + " data : " + (data != null ? data.toString() : ""));
            if (JsBridgeConstants.METHOD_JS_LOAD.equals(m)) {
                jsLoaded();
            } else if (JsBridgeConstants.METHOD_OPEN_BROWSER.equals(m)) {
                openBrowser(data);
            } else if (JsBridgeConstants.METHOD_PUSH_EVENT.equals(m)
                    || JsBridgeConstants.METHOD_CLICK.equals(m)
                    || JsBridgeConstants.METHOD_WV_CLICK.equals(m)
                    || JsBridgeConstants.METHOD_CLOSE.equals(m)
                    || JsBridgeConstants.METHOD_ClOSE_VISIBLE.equals(m)
                    || JsBridgeConstants.METHOD_REFRESH_AD.equals(m)
                    || JsBridgeConstants.METHOD_REPORT_VIDEO_PROGRESS.equals(m)
                    || JsBridgeConstants.METHOD_AD_REWARDED.equals(m)) {
                if (mListener != null) {
                    mListener.onReceiveMessage(m, data);
                }
                if (JsBridgeConstants.METHOD_CLOSE.equals(m)) {
                    release();
                }
            } else {
                //TODO:empty
            }
        } catch (Exception e) {
            DeveloperLog.LogD("postMessage", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    public void reportShowEvent() {
        if (isJsLoaded.get()) {
            isReportShowCalled.set(false);
            reportEvent(JsBridgeConstants.EVENT_SHOW);
        } else {
            isReportShowCalled.set(true);
        }
    }

    public void reportEvent(String event) {
        WebUtil.sendEvent(mWebView, WebUtil.buildScript(JsReportParams.buildEventParams(event)));
    }

    private void jsLoaded() {
        try {
            isJsLoaded.set(true);
            WebUtil.sendEvent(mWebView, WebUtil.buildScript(JsReportParams.buildInitEventParams(
                    placementId, sceneName, campaign, abt)));
            if (isReportShowCalled.get()) {
                reportShowEvent();
            }
        } catch (Exception e) {
            isJsLoaded.set(false);
            DeveloperLog.LogD("jsLoaded", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private void openBrowser(JSONObject data) {
        String url = data.optString("url");
        if (TextUtils.isEmpty(url)) {
            return;
        }
        DeviceUtil.openBrowser(mWebView.getContext(), url);
    }

    public interface MessageListener {
        void onReceiveMessage(String method, JSONObject data);
    }
}
