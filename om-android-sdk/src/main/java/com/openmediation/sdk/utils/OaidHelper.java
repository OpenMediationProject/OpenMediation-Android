package com.openmediation.sdk.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class OaidHelper {
    public static final String TAG = "OaidHelper";
    public static final int HELPER_VERSION_CODE = 20211018;
    private static boolean isCertInit = false;
    private static final AtomicBoolean isCallback = new AtomicBoolean(false);
    private static String sOaid = "";

    public static String getOaid() {
        return sOaid;
    }

    public static void getOaid(Context context, OnGetOaidCallback callback) {
        try {
            if (isSafeSDKAccess()) {
                internalGetOaid(context, callback);
            } else if (isHMSAccess()) {
                sOaid = getOaidFromHMS(context);
                if (callback != null) {
                    callback.result();
                }
            } else {
                if (callback != null) {
                    callback.result();
                }
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.result();
            }
        }
    }

    private static void internalGetOaid(Context context, final OnGetOaidCallback callback) {
        isCallback.set(false);
        try {
            // 加固版本在调用前必须载入SDK安全库
            System.loadLibrary("msaoaidsec");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        // DemoHelper版本建议与SDK版本一致
        if (com.bun.miitmdid.core.MdidSdkHelper.SDK_VERSION_CODE != HELPER_VERSION_CODE) {
            Log.w(TAG, "SDK version not match.");
        }
        if (!isCertInit) { // 证书只需初始化一次
            String certFileName = context.getPackageName() + ".cert.pem";
            isCertInit = com.bun.miitmdid.core.MdidSdkHelper.InitCert(context, loadPemFromAssetFile(context, certFileName));
            if (!isCertInit) {
                Log.w(TAG, "getDeviceIds: cert init failed");
            }
        }

        com.bun.miitmdid.interfaces.IIdentifierListener identifierListener = new com.bun.miitmdid.interfaces.IIdentifierListener() {
            @Override
            public void onSupport(com.bun.miitmdid.interfaces.IdSupplier supplier) {
                if (supplier != null) {
                    sOaid = supplier.getOAID();
                }
                if (isCallback.get()) {
                    return;
                }
                isCallback.set(true);
                if (callback != null) {
                    callback.result();
                }
            }
        };
        int code = com.bun.miitmdid.core.MdidSdkHelper.InitSdk(context, true, identifierListener);
        if (code == com.bun.miitmdid.core.InfoCode.INIT_ERROR_CERT_ERROR) {                         // 证书未初始化或证书无效，SDK内部不会回调onSupport
            Log.w(TAG, "cert not init or check not pass");
            identifierListener.onSupport(null);
        } else if (code == com.bun.miitmdid.core.InfoCode.INIT_ERROR_DEVICE_NOSUPPORT) {             // 不支持的设备, SDK内部不会回调onSupport
            Log.w(TAG, "device not supported");
            identifierListener.onSupport(null);
        } else if (code == com.bun.miitmdid.core.InfoCode.INIT_ERROR_LOAD_CONFIGFILE) {            // 加载配置文件出错, SDK内部不会回调onSupport
            Log.w(TAG, "failed to load config file");
            identifierListener.onSupport(null);
        } else if (code == com.bun.miitmdid.core.InfoCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) {      // 不支持的设备厂商, SDK内部不会回调onSupport
            Log.w(TAG, "manufacturer not supported");
            identifierListener.onSupport(null);
        } else if (code == com.bun.miitmdid.core.InfoCode.INIT_ERROR_SDK_CALL_ERROR) {             // sdk调用出错, SSDK内部不会回调onSupport
            Log.w(TAG, "sdk call error");
            identifierListener.onSupport(null);
        } else if (code == com.bun.miitmdid.core.InfoCode.INIT_INFO_RESULT_DELAY) {             // 获取接口是异步的，SDK内部会回调onSupport
            Log.i(TAG, "result delay (async)");
            new Handler().postDelayed(new Timeout(identifierListener), 1000);
        } else if (code == com.bun.miitmdid.core.InfoCode.INIT_INFO_RESULT_OK) {                  // 获取接口是同步的，SDK内部会回调onSupport
            Log.i(TAG, "result ok (sync)");
        } else {
            // sdk版本高于DemoHelper代码版本可能出现的情况，无法确定是否调用onSupport
            // 不影响成功的OAID获取
            Log.w(TAG, "getDeviceIds: unknown code: " + code);
            identifierListener.onSupport(null);
        }
    }

    private static boolean isSafeSDKAccess() {
        try {
            Class.forName("com.bun.miitmdid.core.InfoCode");
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean isHMSAccess() {
        try {
            Class.forName("com.huawei.hms.ads.identifier.AdvertisingIdClient");
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static String getOaidFromHMS(Context context) throws Exception {
        if (context == null) {
            return "";
        }
        com.huawei.hms.ads.identifier.AdvertisingIdClient.Info info =
                com.huawei.hms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context);
        return info.getId();
    }

    /**
     * 从asset文件读取证书内容
     *
     * @param context
     * @param assetFileName
     * @return 证书字符串
     */
    public static String loadPemFromAssetFile(Context context, String assetFileName) {
        try {
            InputStream is = context.getAssets().open(assetFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "loadPemFromAssetFile failed");
            return "";
        }
    }

    private static class Timeout implements Runnable {

        private com.bun.miitmdid.interfaces.IIdentifierListener mListener;

        public Timeout(com.bun.miitmdid.interfaces.IIdentifierListener listener) {
            mListener = listener;
        }

        @Override
        public void run() {
            Log.w(TAG, "get oaid result delay timeout");
            if (mListener != null && !isCallback.get()) {
                mListener.onSupport(null);
            }
        }
    }

    public interface OnGetOaidCallback {
        void result();
    }
}
