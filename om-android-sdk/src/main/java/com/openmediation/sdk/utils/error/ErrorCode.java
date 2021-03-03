// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.error;

public interface ErrorCode {
    String ERROR_CONTEXT = "Context error";//invalid context
    String ERROR_NOT_INIT = "not init";//not init error
    String ERROR_GDPR = "gdpr rejected";//subject to GDPR
    String ERROR_APPKEY = "Empty AppKey";//empty appKey
    String ERROR_NETWORK_NOT_AVAILABLE = "Network is not available,please check network";//network unavailable
    String ERROR_MISS_PERMISSION = "The required permission is not register on Manifest";// miss required permission
    String ERROR_MAIN_THREAD_REQUIRED = "Require on main thread"; //require a main thread
    String ERROR_NOT_SURPPORT_WEBVIEW = "WebView is not supported";//not support webView
    String ERROR_NO_FILL = "No Fill";//no fill
    String ERROR_AD_RESOURCE_EMPTY = "Ad resource is empty";// ad resource empty
    String ERROR_PLACEMENT_ID = "Placement id is empty"; //placement id empty
    String ERROR_ACTIVITY = "Activity is null or destroyed"; //activity is destroyed, not an available activity
    String ERROR_CONFIG_EMPTY = "Config is empty,please check the config on server"; //Config is empty
    String ERROR_PLACEMENT_EMPTY = "Config is not contain this placement";//get placement from config result empty
    String ERROR_PLACEMENT_TYPE = "Placement type match error"; //placement type not match the request type
    String ERROR_LOAD_AD_BUT_DESTROYED = "This ad object has been destroyed , please re-init it before load ad"; //this ad object has been destroyed , please re-init it before load ad
    String ERROR_CREATE_AD_FAILED = "Create ad object failed, please check init params";//create ad object failed, please check init params
    String ERROR_TIMEOUT = "Timeout"; // load ad timeout
    String ERROR_UNSPECIFIED = "UnSpecified Error";//unSpecified error,includes many reason but can not describe the details to users
    String ERROR_DISPLAY_AD = "Display ad error, the ad is not ready";// display ad error, the ad is not ready or occurs something error
    String ERROR_INSTANCE_EMPTY = "Instances is empty, no instance to be load";
    String ERROR_CL = "Placement config error";//request cl error
    String ERROR_CREATE_MEDATION_ADAPTER = "create mediation adapter failed";
    String ERROR_ACTIVITY_EMPTY = "Activity is empty";
    String ERROR_INIT_IS_RUNNING = "Init is running";
    String ERROR_UI_THREAD = "Should be called on the main UI thread.";
    String ERROR_EMPTY_INSTANCE_KEY = "instance key is empty";
    String ERROR_AD_OBJECT_DESTROYED = "ad object is destroyed";
    String ERROR_AD_IS_SHOWING = "ad is showing";
    String ERROR_INIT_FAILED = "Om SDK init failed";
    String ERROR_LOAD_AD_FAILED_CAUSE_NOT_INIT_WITH_EMPTY_APPKEY = "Load ads failed cause sdk hasn't been init with empty appKey";

    /**
     *
     */
    int CODE_INIT_INVALID_REQUEST = 111;
    String MSG_INIT_INVALID_REQUEST = "Init Invalid Request";
    /**
     *
     */
    int CODE_INIT_NETWORK_ERROR = 121;
    String MSG_INIT_NETWORK_ERROR = "Init Network Error";
    /**
     *
     */
    int CODE_INIT_SERVER_ERROR = 131;
    String MSG_INIT_SERVER_ERROR = "Init Server Error";
    /**
     *
     */
    int CODE_INIT_UNKNOWN_INTERNAL_ERROR = 151;
    String MSG_INIT_UNKNOWN_INTERNAL_ERROR = "Init Unknown Internal Error";
    /**
     *
     */
    int CODE_LOAD_INVALID_REQUEST = 211;
    String MSG_LOAD_INVALID_REQUEST = "Load Invalid Request";
    /**
     *
     */
    int CODE_LOAD_NETWORK_ERROR = 221;
    String MSG_LOAD_NETWORK_ERROR = "Load Network Error";
    /**
     *
     */
    int CODE_LOAD_SERVER_ERROR = 231;
    String MSG_LOAD_SERVER_ERROR = "Load Server Error";
    /**
     *
     */
    int CODE_LOAD_NO_AVAILABLE_AD = 241;
    String MSG_LOAD_NO_AVAILABLE_AD = "Load No Available Ad";
    /**
     *
     */
    int CODE_LOAD_SDK_UNINITIALIZED = 242;
    String MSG_LOAD_SDK_UNINITIALIZED = "Load SDK Uninitialized";
    /**
     *
     */
    int CODE_LOAD_CAPPED = 243;
    String MSG_LOAD_CAPPED = "Load Capped";
    /**
     *
     */
    int CODE_LOAD_MISSING_ADAPTER = 244;
    String MSG_LOAD_MISSING_ADAPTER = "Instance Load Failed for missing adn or adapter";
    /**
     *
     */
    int CODE_LOAD_FAILED_IN_ADAPTER = 245;
    String MSG_LOAD_FAILED_IN_ADAPTER = "Instance Load Failed In Adapter";
    /**
     *
     */
    int CODE_LOAD_UNKNOWN_INTERNAL_ERROR = 251;
    String MSG_LOAD_UNKNOWN_INTERNAL_ERROR = "Load Unknown Internal Error";
    /**
     *
     */
    int CODE_SHOW_INVALID_ARGUMENT = 311;
    String MSG_SHOW_INVALID_ARGUMENT = "Show Invalid Argument";
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
    int CODE_SHOW_UNKNOWN_INTERNAL_ERROR = 351;
    String MSG_SHOW_UNKNOWN_INTERNAL_ERROR = "Show Unknown Internal Error";
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
    int CODE_INTERNAL_SERVER_UNRESPOND = 0;
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

}
