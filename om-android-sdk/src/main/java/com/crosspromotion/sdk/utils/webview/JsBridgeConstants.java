package com.crosspromotion.sdk.utils.webview;

public interface JsBridgeConstants {
    String METHOD_JS_LOAD = "jsLoaded";
    String METHOD_PUSH_EVENT = "pushEvent";
    String METHOD_ClOSE_VISIBLE = "setCloseVisible";
    String METHOD_CLOSE = "close";
    String METHOD_CLICK = "click";
    String METHOD_WV_CLICK = "wvClick";
    String METHOD_OPEN_BROWSER = "openBrowser";
    String METHOD_REPORT_VIDEO_PROGRESS = "reportVideoProgress";
    String METHOD_AD_REWARDED = "addRewarded";
    String METHOD_REFRESH_AD = "refreshAd";

    String EVENT_INIT = "wv.init";
    String EVENT_SHOW = "wv.show";
    String EVENT_PAUSE = "wv.pause";
    String EVENT_RESUME = "wv.resume";
    String EVENT_MUTE = "wv.muted";
    String EVENT_UNMUTE = "wv.unmute";
}
