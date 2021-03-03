package com.crosspromotion.sdk.utils.webview;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.openmediation.sdk.utils.DeveloperLog;

import org.json.JSONException;
import org.json.JSONObject;

public class WebUtil {
    private static final String JS_TEMPLATE = "window.postMessage(%1s, '*')";

    public static void sendEvent(final WebView webView, final String script) {
        if (webView == null || TextUtils.isEmpty(script)) {
            return;
        }
        DeveloperLog.LogD("WebViewUtil:sendEvent", script);
        webView.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    webView.loadUrl(script);
                } else {
                    webView.evaluateJavascript(script, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            DeveloperLog.LogD("WebViewUtil:sendEvent", "evaluateJavascript : " + script + " result: " + value);
                        }
                    });
                }
            }
        });
    }

    public static String buildScript(JSONObject data) {
        return String.format(JS_TEMPLATE, data.toString());
    }

    public static JSONObject buildEventData(String event) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", event);
        return jsonObject;
    }

    public static JSONObject appendEventData(JSONObject object, String key, Object value) throws JSONException {
        return object.put(key, value);
    }
}
