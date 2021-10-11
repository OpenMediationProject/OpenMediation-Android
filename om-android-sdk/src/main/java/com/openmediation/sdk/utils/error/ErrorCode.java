// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.error;

public interface ErrorCode {
    String ERROR_CONTEXT = "Context error";//invalid context
    String ERROR_NOT_INIT = "SDK Not init";//not init error
    String ERROR_NO_FILL = "No Fill, ";//no fill
    String ERROR_PLACEMENT_ID = "Placement id is empty"; //placement id empty
    String ERROR_PLACEMENT_EMPTY = "Config is not contain this placement";//get placement from config result empty
    String ERROR_PLACEMENT_TYPE = "Placement type match error"; //placement type not match the request type
    String ERROR_LOAD_AD_BUT_DESTROYED = "This ad object has been destroyed , please re-init it before load ad"; //this ad object has been destroyed , please re-init it before load ad
    String ERROR_TIMEOUT = "Timeout"; // load ad timeout
    String ERROR_CREATE_MEDIATION_ADAPTER = "Create mediation adapter failed";
    String ERROR_INIT_FAILED = "Om SDK init failed";
    String ERROR_LOAD_CAPPED = "Load Capped, ";

    /**
     *
     */
    int CODE_INIT_INVALID_REQUEST = 111;
    String MSG_INIT_INVALID_REQUEST = "Init Invalid Request, ";
    /**
     *
     */
    int CODE_INIT_NETWORK_ERROR = 121;
    String MSG_INIT_NETWORK_ERROR = "Init Network Error";
    /**
     *
     */
    int CODE_INIT_SERVER_ERROR = 131;
    String MSG_INIT_SERVER_ERROR = "Init Server Error, Response Code: ";
    /**
     *
     */
    int CODE_INIT_UNKNOWN_INTERNAL_ERROR = 151;
    String MSG_INIT_UNKNOWN_INTERNAL_ERROR = "Init Unknown Internal Error, ";
    /**
     *
     */
    int CODE_LOAD_INVALID_REQUEST = 211;
    String MSG_LOAD_INVALID_LOADING = "Load Invalid Request, Placement is loading";
    String MSG_LOAD_INVALID_SHOWING = "Load Invalid Request, Placement is showing";
    /**
     *
     */
    int CODE_LOAD_NETWORK_ERROR = 221;
    String MSG_LOAD_NETWORK_ERROR = "Load Network Error";
    /**
     *
     */
    int CODE_LOAD_SERVER_ERROR = 231;
    String MSG_LOAD_SERVER_ERROR = "Load Server Error, ";
    /**
     *
     */
    int CODE_LOAD_NO_AVAILABLE_AD = 241;
    String MSG_LOAD_NO_AVAILABLE_AD = "Load No Available Ad, ";
    /**
     *
     */
    int CODE_LOAD_SDK_UNINITIALIZED = 242;
    String MSG_LOAD_SDK_UNINITIALIZED = "Load SDK Uninitialized";
    /**
     *
     */
    int CODE_LOAD_FAILED_IN_ADAPTER = 245;
    String MSG_LOAD_FAILED_IN_ADAPTER = "Instance Load Failed In Adapter";
    /**
     *
     */
    int CODE_LOAD_UNKNOWN_ERROR = 251;
    String MSG_LOAD_UNKNOWN_ERROR = "Load Unknown Internal Error";
    /**
     *
     */
    int CODE_SHOW_INVALID_ARGUMENT = 311;
    String MSG_SHOW_INVALID_ARGUMENT = "Show Invalid Argument, ";
    /**
     *
     */
    int CODE_SHOW_NO_AD_READY = 341;
    String MSG_SHOW_NO_AD_READY = "No Ad Ready";
    /**
     *
     */
    int CODE_SHOW_SDK_UNINITIALIZED = 342;
    String MSG_SHOW_SDK_UNINITIALIZED = "SDK Uninitialized";
    /**
     *
     */
    int CODE_SHOW_SCENE_CAPPED = 343;
    String MSG_SHOW_SCENE_CAPPED = "Scene Capped";
    /**
     *
     */
    int CODE_SHOW_FAILED_IN_ADAPTER = 345;
    String MSG_SHOW_FAILED_IN_ADAPTER = "Show Failed In Adapter";
    /**
     *
     */
    int CODE_SHOW_SCENE_NOT_FOUND = 352;
    String MSG_SHOW_SCENE_NOT_FOUND = "Scene not found";

    int CODE_SHOW_IMPRESSION_NOT_ENABLED = 401;
    String MSG_SHOW_IMPRESSION_NOT_ENABLED = "Ad revenue measurement is not enabled";

    /**
     * internal codes 
     **/
    /**
     *
     */
    int CODE_INTERNAL_SERVER_ERROR = 1;
    /**
     *
     */
    int CODE_INTERNAL_SERVER_FAILED = 2;
    /**
     *
     */
    int CODE_INTERNAL_REQUEST_APPKEY = 3;
    /**
     *
     */
    int CODE_INTERNAL_REQUEST_PLACEMENTID = 4;
    /**
     *
     */
    int CODE_INTERNAL_REQUEST_ACTIVITY = 5;
    /**
     *
     */
    int CODE_INTERNAL_REQUEST_WEBVIEW = 6;
    /**
     *
     */
    int CODE_INTERNAL_REQUEST_GDPR = 7;
    /**
     *
     */
    int CODE_INTERNAL_REQUEST_PERMISSION = 8;
    /**
     *
     */
    int CODE_INTERNAL_UNKNOWN_ACTIVITY = 9;
    /**
     *
     */
    int CODE_INTERNAL_UNKNOWN_OTHER = 10;

    /**
     * Init Error
     */
    int CODE_INIT_RESPONSE_CHECK_ERROR = 132;
    String MSG_INIT_RESPONSE_CHECK_ERROR = "Init Error: Response data check error";
    int CODE_INTERNAL_RESPONSE_CHECK_ERROR = 11;
    int CODE_INIT_RESPONSE_PARSE_ERROR = 133;
    String MSG_INIT_RESPONSE_PARSE_ERROR = "Init Error: Response data parse error";
    int CODE_INTERNAL_INIT_RESPONSE_PARSE_ERROR = 12;
    int CODE_INIT_EXCEPTION = 134;
    String MSG_INIT_EXCEPTION = "Init Unknown Error: ";
    int CODE_INTERNAL_INIT_EXCEPTION = 13;
    int CODE_INIT_REQUEST_ERROR = 135;
    String MSG_INIT_REQUEST_ERROR = "Init Request Error: ";
    int CODE_INTERNAL_INIT_REQUEST_ERROR = 14;

}
