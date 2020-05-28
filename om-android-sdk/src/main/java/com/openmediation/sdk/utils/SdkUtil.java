// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.Manifest;
import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.utils.device.DeviceUtil;
import com.openmediation.sdk.utils.device.GdprUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.request.network.util.NetworkChecker;

/**
 * The type Sdk util.
 */
public class SdkUtil {
    private static String[] ADT_PERMISSIONS = new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};

    /**
     * Ban run error.
     *
     * @param activity the activity
     * @param appKey   the app key
     * @return the error
     */
    public static Error banRun(Activity activity, String appKey) {
        Error error;
        //
        if (!DeviceUtil.isActivityAvailable(activity)) {
            error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            //init error activity is not available
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            return error;
        }
        //
        if (TextUtils.isEmpty(appKey)) {
            error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_APPKEY);
            //init error appKey is empty
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            return error;
        }
        //
        if (GdprUtil.isGdprSubjected(activity)) {
            error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_GDPR);
            //init error gdpr is rejected
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            return error;
        }
        //
        if (!PermissionUtil.isGranted(activity, ADT_PERMISSIONS)) {
            error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PERMISSION);
            //init error permission is not granted
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            return error;
        }
        //
        if (!NetworkChecker.isAvailable(activity)) {
            error = new Error(ErrorCode.CODE_INIT_NETWORK_ERROR
                    , ErrorCode.MSG_INIT_NETWORK_ERROR, -1);
            //init error network is not available
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            return error;
        }
        return null;
    }
}
