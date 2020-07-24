// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3
// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import com.google.android.gms.ads.AdRequest;

public class AdMobErrorUtil {
    static String getErrorString(int errorCode) {
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                return "Internal error";
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                return "The ad request was invalid";
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                return "The ad request was unsuccessful due to network connectivity";
            case AdRequest.ERROR_CODE_NO_FILL:
                return "The ad request was successful, but no ad was returned due to lack of ad inventory";
            case AdRequest.ERROR_CODE_APP_ID_MISSING:
                return "The ad request was not made due to a missing app ID";
            default:
                return "Unknown error";
        }
    }
}
