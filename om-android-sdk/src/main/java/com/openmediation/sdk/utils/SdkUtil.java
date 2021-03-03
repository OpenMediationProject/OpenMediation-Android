// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.utils.device.DeviceUtil;
import com.openmediation.sdk.utils.device.GdprUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.request.network.util.NetworkChecker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Sdk util.
 */
public class SdkUtil {
    private static String[] ADT_PERMISSIONS = new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
    private static final Pattern ACCEPTED_URI_SCHEME = Pattern.compile("(?i)"
            + // switch on case insensitive matching
            '('
            + // begin group for scheme
            "(?:http|https|ftp|file)://" + "|(?:inline|data|about|javascript):" + "|(?:.*:.*@)"
            + ')' + "(.*)");

    /**
     * Ban run error.
     *
     * @param activity the activity
     * @param appKey   the app key
     * @return the error
     */
    public static Error banRun(Activity activity, String appKey) {
        Error error;
        if (!DeviceUtil.isActivityAvailable(activity)) {
            error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            //init error activity is not available
            DeveloperLog.LogE(error.toString());
            return error;
        }
        if (TextUtils.isEmpty(appKey)) {
            error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_APPKEY);
            //init error appKey is empty
            DeveloperLog.LogE(error.toString());
            return error;
        }
        if (!PermissionUtil.isGranted(activity, ADT_PERMISSIONS)) {
            error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PERMISSION);
            //init error permission is not granted
            DeveloperLog.LogE(error.toString());
            return error;
        }
//        if (!NetworkChecker.isAvailable(activity)) {
//            error = new Error(ErrorCode.CODE_INIT_NETWORK_ERROR
//                    , ErrorCode.MSG_INIT_NETWORK_ERROR, -1);
//            //init error network is not available
//            DeveloperLog.LogE(error.toString());
//            return error;
//        }
        return null;
    }

    public static boolean isAcceptedScheme(String url) {
        String lowerCaseUrl = url.toLowerCase();
        Matcher acceptedUrlSchemeMatcher = ACCEPTED_URI_SCHEME.matcher(lowerCaseUrl);
        return acceptedUrlSchemeMatcher.matches();
    }

    /**
     * @param copyStr
     * @return
     */
    public static boolean copy(String copyStr) {
        try {
            ClipboardManager cm = (ClipboardManager) AdtUtil.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", copyStr);
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
