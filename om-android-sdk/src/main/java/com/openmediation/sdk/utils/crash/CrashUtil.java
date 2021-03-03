// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.crash;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.request.HeaderUtils;
import com.openmediation.sdk.utils.request.RequestBuilder;
import com.openmediation.sdk.utils.request.network.AdRequest;
import com.openmediation.sdk.utils.request.network.ByteRequestBody;
import com.openmediation.sdk.utils.request.network.Headers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 * The type Crash util.
 */
public class CrashUtil implements Thread.UncaughtExceptionHandler {
    private static final String SP_NAME = "AdTimingCrashSP";
    private SharedPreferences mCrashSp;
    private Thread.UncaughtExceptionHandler mDefaultEh;
    private boolean isNe = true;

    private static class CrashUtilHolder {
        private static final CrashUtil INSTANCE = new CrashUtil();
    }

    private CrashUtil() {
    }

    /**
     * Gets singleton.
     *
     * @return the singleton
     */
    public static CrashUtil getSingleton() {
        return CrashUtilHolder.INSTANCE;
    }

    /**
     * Init.
     */
    public void init() {
        try {
            //inits SP
            mCrashSp = AdtUtil.getApplication().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            //gets exception handler if set by app
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashUtil)) {
                mDefaultEh = Thread.getDefaultUncaughtExceptionHandler();
            }
            Thread.setDefaultUncaughtExceptionHandler(this);
        } catch (Exception e) {
            DeveloperLog.LogD("CrashUtil", e);
        }
    }

    /**
     * called for uncaught exceptions
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            if (throwable == null || throwable instanceof UndeclaredThrowableException) {
                return;
            }
            saveException(throwable);
            //passes to exception handler if set by app developer
            if (mDefaultEh != null && mDefaultEh != this && !(mDefaultEh instanceof CrashUtil)) {
                mDefaultEh.uncaughtException(thread, throwable);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("CrashUtil", e);
        }
    }

    /**
     * Sets ne.
     *
     * @param ne the ne
     */
    public void setNe(int ne) {
        isNe = ne != 1;
    }

    /**
     * saves exceptions
     *
     * @param throwable the throwable
     */
    public void saveException(final Throwable throwable) {
        if (!isNe) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (throwable == null || mCrashSp == null) {
                    return;
                }
                if (mCrashSp.getAll() != null && mCrashSp.getAll().size() >= 10) {
                    return;
                }
                try {
                    String errorInfo = CommonConstants.SDK_VERSION_NAME.concat(":").concat(getStackTraceString(throwable));
                    if (TextUtils.isEmpty(errorInfo)) {
                        return;
                    }
                    SharedPreferences.Editor editor = mCrashSp.edit();
                    editor.putString(Long.toString(System.currentTimeMillis()), errorInfo.trim());
                    editor.apply();
                } catch (Exception e) {
                    DeveloperLog.LogD("CrashUtil", e);
                }
            }
        };
        WorkExecutor.execute(runnable);
    }

    /**
     * uploads Exceptions in separate thread
     *
     * @param config the config
     * @param appKey the app key
     */
    public void uploadException(final Configurations config, final String appKey) {
        if (mCrashSp == null || config == null || config.getApi() == null) {
            return;
        }
        if (TextUtils.isEmpty(config.getApi().getEr())) {
            return;
        }
        if (TextUtils.isEmpty(appKey)) {
            return;
        }
        final Map<String, ?> errorMap = mCrashSp.getAll();
        if (errorMap.size() == 0) {
            return;
        }

        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //clears SP after successful uploads
                    mCrashSp.edit().clear().apply();
                    String xrUrl = config.getApi().getEr().concat("?").concat(new RequestBuilder()
                            .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                            .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                            .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                            .p(KeyConstants.Request.KEY_APP_KEY, appKey)
                            .format());
                    for (Map.Entry<String, ?> nv : errorMap.entrySet()) {
                        String errorInfo = (String) nv.getValue();
                        if (TextUtils.isEmpty(errorInfo) || !errorInfo.contains(CommonConstants.PKG_SDK)) {
                            continue;
                        }

                        String errorType = getErrorType(errorInfo);
                        if (TextUtils.isEmpty(errorType)) {
                            errorType = "UnknownError";
                        }
                        errorInfo = errorInfo.replaceAll("\u0001", " ");
                        byte[] body = RequestBuilder.buildErrorRequestBody(errorType, errorInfo);
                        Headers headers = HeaderUtils.getBaseHeaders();
                        AdRequest.post()
                                .body(new ByteRequestBody(body))
                                .headers(headers)
                                .url(xrUrl)
                                .connectTimeout(30000)
                                .readTimeout(60000)
                                .performRequest(AdtUtil.getApplication());
                    }
                } catch (Throwable e) {
                    DeveloperLog.LogD("CrashUtil", e);
                }
            }
        });
    }


    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        Throwable t = new Throwable(CommonConstants.SDK_VERSION_NAME, tr);
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * matches error type with regex
     */
    private static String getErrorType(String exception) {
        String type = "";
        try {
            Pattern pattern = compile(".*?(Exception|Error|Death)", CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(exception);
            if (matcher.find()) {
                type = matcher.group(0);
            }
            if (!TextUtils.isEmpty(type)) {
                type = type.replaceAll("Caused by:", "").replaceAll(" ", "");
            }
        } catch (Exception e) {
            DeveloperLog.LogD("CrashUtil", e);
        }
        return type;
    }
}
