// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

import android.text.TextUtils;

public class AdapterErrorBuilder {

    public static final String AD_UNIT_REWARDED_VIDEO = "Rewarded Video";
    public static final String AD_UNIT_INTERSTITIAL = "Interstitial";
    public static final String AD_UNIT_BANNER = "Banner";
    public static final String AD_UNIT_NATIVE = "Native";
    public static final String AD_UNIT_SPLASH = "Splash";
    public static final String AD_UNIT_PROMOTION = "Promotion";

    public static AdapterError buildInitError(String adUnit, String adapterName, String errorMsg) {
        String resultingMessage = "" + adUnit + " Init Failed" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        resultingMessage += errorMsg;
        return new AdapterError(resultingMessage);
    }

    public static AdapterError buildInitError(String adUnit, String adapterName, int errorCode, String errorMsg) {
        String resultingMessage = "" + adUnit + " Init Failed" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        resultingMessage += errorMsg;
        return new AdapterError(errorCode, resultingMessage);
    }

    public static AdapterError buildLoadError(String adUnit, String adapterName, String errorMsg) {
        String resultingMessage = "" + adUnit + " Load Failed" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        resultingMessage += errorMsg;
        AdapterError error = new AdapterError(resultingMessage);
        error.setLoadFailFromAdn(true);
        return error;
    }

    public static AdapterError buildLoadCheckError(String adUnit, String adapterName, String errorMsg) {
        String resultingMessage = "" + adUnit + " Load Failed" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        resultingMessage += errorMsg;
        return new AdapterError(resultingMessage);
    }

    public static AdapterError buildLoadError(String adUnit, String adapterName, int errorCode, String errorMsg) {
        String resultingMessage = "" + adUnit + " Load Failed" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        resultingMessage += errorMsg;
        AdapterError error = new AdapterError(errorCode, resultingMessage);
        error.setLoadFailFromAdn(true);
        return error;
    }

    public static AdapterError buildShowError(String adUnit, String adapterName, String errorMsg) {
        String resultingMessage = "" + adUnit + " Show Failed" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        resultingMessage += errorMsg;
        return new AdapterError(resultingMessage);
    }

    public static AdapterError buildShowError(String adUnit, String adapterName, int errorCode, String errorMsg) {
        String resultingMessage = "" + adUnit + " Show Failed" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        resultingMessage += errorMsg;
        return new AdapterError(errorCode, resultingMessage);
    }

}
