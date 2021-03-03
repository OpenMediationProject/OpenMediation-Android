// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils.error;

import android.util.SparseArray;

public class ErrorCode {
    private static SparseArray<String> mErrorMap = new SparseArray<>();

    public static final int CODE_LOAD_NOT_INITIALIZED = 201;
    private static final String MSG_LOAD_NOT_INITIALIZED = "Load failed: SDK not initialized";

    public static final int CODE_LOAD_GDPR_REFUSE = 202;
    private static final String MSG_LOAD_GDPR_REFUSE = "Load failed: GDPR refuse";

    public static final int CODE_LOAD_PLACEMENT_EMPTY = 203;
    private static final String MSG_LOAD_PLACEMENT_EMPTY = "Load failed: placement is empty";

    public static final int CODE_LOAD_PLACEMENT_NOT_FOUND = 204;
    private static final String MSG_LOAD_PLACEMENT_NOT_FOUND = "Load failed: Placement not found in app";

    public static final int CODE_LOAD_PLACEMENT_AD_TYPE_INCORRECT = 205;
    private static final String MSG_LOAD_PLACEMENT_AD_TYPE_INCORRECT = "Load failed: adtype wrong";

    public static final int CODE_LOAD_FREQUENCY_ERROR = 206;
    private static final String MSG_LOAD_FREQUENCY_ERROR = "Load failed: no ad fill";

    public static final int CODE_LOAD_NETWORK_ERROR = 207;
    private static final String MSG_LOAD_NETWORK_ERROR = "Load failed: network is currently unavailable";

    public static final int CODE_LOAD_SERVER_ERROR = 208;
    private static final String MSG_LOAD_SERVER_ERROR = "Load failed: server error";

    public static final int CODE_LOAD_NO_FILL = 209;
    private static final String MSG_LOAD_NO_FILL = "Load failed: no ad fill";

    public static final int CODE_LOAD_TIMEOUT = 210;
    private static final String MSG_LOAD_TIMEOUT = "Load failed: cache resource timeout";

    public static final int CODE_LOAD_PARSE_FAILED = 211;
    private static final String MSG_LOAD_PARSE_FAILED = "Load failed: data parsing failed";

    /**
     * CL 请求Exception
     */
    public static final int CODE_LOAD_UNKNOWN_EXCEPTION = 212;
    private static final String MSG_LOAD_UNKNOWN_EXCEPTION = "Load failed: unknown exception occurred";

    /**
     * 资源下载失败
     */
    public static final int CODE_LOAD_DOWNLOAD_FAILED = 213;
    private static final String MSG_LOAD_DOWNLOAD_FAILED = "Load failed: assets download failed";

    /**
     * 下载资源catch
     */
    public static final int CODE_LOAD_DOWNLOAD_EXCEPTION = 214;
    private static final String MSG_LOAD_DOWNLOAD_EXCEPTION = "Load failed: unknown exception occurred";

    public static final int CODE_LOAD_PLACEMENT_IS_SHOWING = 215;
    private static final String MSG_LOAD_PLACEMENT_IS_SHOWING = "Load failed: placement is showing";

    public static final int CODE_LOAD_RESOURCE_ERROR = 216;
    private static final String MSG_LOAD_RESOURCE_ERROR = "Load failed: resource error";

    public static final int CODE_LOAD_BEFORE_UNKNOWN_ERROR = 217;
    private static final String MSG_LOAD_BEFORE_UNKNOWN_ERROR = "Load failed: unknown exception occurred";

    public static final int CODE_LOAD_UNKNOWN_ACTIVITY = 219;
    private static final String MSG_LOAD_UNKNOWN_ACTIVITY = "Load failed: activity is unknown";

    public static final int CODE_LOAD_DESTROYED = 220;
    private static final String MSG_LOAD_DESTROYED = "Load failed: ad has bean destroyed";

    public static final int CODE_LOAD_BANNER_UNKNOWN_EXCEPTION = 221;
    private static final String MSG_LOAD_BANNER_UNKNOWN_EXCEPTION = "Load failed: unknown exception occurred";

    public static final int CODE_SHOW_PLACEMENT_EMPTY = 301;
    private static final String MSG_SHOW_PLACEMENT_EMPTY = "Show failed: placement empty";

    public static final int CODE_SHOW_PLACEMENT_NOT_FOUND = 302;
    private static final String MSG_SHOW_PLACEMENT_NOT_FOUND = "Show failed: placement not found in app";

    public static final int CODE_SHOW_PLACEMENT_AD_TYPE_INCORRECT = 303;
    private static final String MSG_SHOW_PLACEMENT_AD_TYPE_INCORRECT = "Show failed: adtype wrong";

    public static final int CODE_SHOW_NOT_INITIALIZED = 304;
    private static final String MSG_SHOW_NOT_INITIALIZED = "Show failed: SDK not initialized";

    public static final int CODE_SHOW_FAIL_NOT_READY = 305;
    private static final String MSG_SHOW_FAIL_NOT_READY = "Show failed: ad not ready";

    public static final int CODE_SHOW_RESOURCE_ERROR = 306;
    private static final String MSG_SHOW_RESOURCE_ERROR = "Show failed: resource not ready";

    public static final int CODE_SHOW_UNKNOWN_EXCEPTION = 307;
    private static final String MSG_SHOW_UNKNOWN_EXCEPTION = "Show failed: unknown exception occurred";

    public static final int CODE_SHOW_INVALID_ARGUMENT = 308;
    public static final String MSG_SHOW_INVALID_ARGUMENT = "Show failed: Invalid Argument, ";

    public static final int CODE_BID_SDK_NOT_INIT = 401;
    private static final String MSG_BID_SDK_NOT_INIT = "SDK Uninitialized";

    public static final int CODE_BID_NETWORK_ERROR = 402;
    private static final String MSG_BID_SDK_NETWORK = "Network is currently unavailable";

    public static final int CODE_BID_TIMEOUT = 403;
    private static final String MSG_BID_TIMEOUT = "Bid Timeout";

    public static final int CODE_BID_SERVER_ERROR = 404;
    private static final String MSG_BID_SERVER_ERROR = "Bid Server Error";

    public static final int CODE_BID_NO_BID = 405;
    private static final String MSG_BID_NO_BID = "NoBid";

    public static final int CODE_BID_INTERNAL_ERROR = 406;
    private static final String MSG_BID_INTERNAL_ERROR = "Internal Error";

    static {
        mErrorMap.put(CODE_LOAD_NOT_INITIALIZED, MSG_LOAD_NOT_INITIALIZED);
        mErrorMap.put(CODE_LOAD_GDPR_REFUSE, MSG_LOAD_GDPR_REFUSE);
        mErrorMap.put(CODE_LOAD_PLACEMENT_EMPTY, MSG_LOAD_PLACEMENT_EMPTY);
        mErrorMap.put(CODE_LOAD_PLACEMENT_NOT_FOUND, MSG_LOAD_PLACEMENT_NOT_FOUND);
        mErrorMap.put(CODE_LOAD_PLACEMENT_AD_TYPE_INCORRECT, MSG_LOAD_PLACEMENT_AD_TYPE_INCORRECT);
        mErrorMap.put(CODE_LOAD_FREQUENCY_ERROR, MSG_LOAD_FREQUENCY_ERROR);
        mErrorMap.put(CODE_LOAD_NETWORK_ERROR, MSG_LOAD_NETWORK_ERROR);
        mErrorMap.put(CODE_LOAD_SERVER_ERROR, MSG_LOAD_SERVER_ERROR);
        mErrorMap.put(CODE_LOAD_NO_FILL, MSG_LOAD_NO_FILL);
        mErrorMap.put(CODE_LOAD_TIMEOUT, MSG_LOAD_TIMEOUT);
        mErrorMap.put(CODE_LOAD_PARSE_FAILED, MSG_LOAD_PARSE_FAILED);
        mErrorMap.put(CODE_LOAD_UNKNOWN_EXCEPTION, MSG_LOAD_UNKNOWN_EXCEPTION);
        mErrorMap.put(CODE_LOAD_DOWNLOAD_FAILED, MSG_LOAD_DOWNLOAD_FAILED);
        mErrorMap.put(CODE_LOAD_DOWNLOAD_EXCEPTION, MSG_LOAD_DOWNLOAD_EXCEPTION);
        mErrorMap.put(CODE_LOAD_PLACEMENT_IS_SHOWING, MSG_LOAD_PLACEMENT_IS_SHOWING);
        mErrorMap.put(CODE_LOAD_RESOURCE_ERROR, MSG_LOAD_RESOURCE_ERROR);
        mErrorMap.put(CODE_LOAD_BEFORE_UNKNOWN_ERROR, MSG_LOAD_BEFORE_UNKNOWN_ERROR);
        mErrorMap.put(CODE_LOAD_UNKNOWN_ACTIVITY, MSG_LOAD_UNKNOWN_ACTIVITY);
        mErrorMap.put(CODE_LOAD_DESTROYED, MSG_LOAD_DESTROYED);
        mErrorMap.put(CODE_LOAD_BANNER_UNKNOWN_EXCEPTION, MSG_LOAD_BANNER_UNKNOWN_EXCEPTION);
        mErrorMap.put(CODE_SHOW_PLACEMENT_EMPTY, MSG_SHOW_PLACEMENT_EMPTY);
        mErrorMap.put(CODE_SHOW_PLACEMENT_NOT_FOUND, MSG_SHOW_PLACEMENT_NOT_FOUND);
        mErrorMap.put(CODE_SHOW_PLACEMENT_AD_TYPE_INCORRECT, MSG_SHOW_PLACEMENT_AD_TYPE_INCORRECT);
        mErrorMap.put(CODE_SHOW_NOT_INITIALIZED, MSG_SHOW_NOT_INITIALIZED);
        mErrorMap.put(CODE_SHOW_FAIL_NOT_READY, MSG_SHOW_FAIL_NOT_READY);
        mErrorMap.put(CODE_SHOW_RESOURCE_ERROR, MSG_SHOW_RESOURCE_ERROR);
        mErrorMap.put(CODE_SHOW_UNKNOWN_EXCEPTION, MSG_SHOW_UNKNOWN_EXCEPTION);
        mErrorMap.put(CODE_SHOW_INVALID_ARGUMENT, MSG_SHOW_INVALID_ARGUMENT);
        mErrorMap.put(CODE_BID_SDK_NOT_INIT, MSG_BID_SDK_NOT_INIT);
        mErrorMap.put(CODE_BID_NETWORK_ERROR, MSG_BID_SDK_NETWORK);
        mErrorMap.put(CODE_BID_TIMEOUT, MSG_BID_TIMEOUT);
        mErrorMap.put(CODE_BID_SERVER_ERROR, MSG_BID_SERVER_ERROR);
        mErrorMap.put(CODE_BID_NO_BID, MSG_BID_NO_BID);
        mErrorMap.put(CODE_BID_INTERNAL_ERROR, MSG_BID_INTERNAL_ERROR);
    }

    static String getErrorMessage(int code) {
        return mErrorMap.get(code);
    }
}
