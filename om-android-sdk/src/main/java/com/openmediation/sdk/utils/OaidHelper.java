package com.openmediation.sdk.utils;

import android.content.Context;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.supplier.IdSupplier;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class OaidHelper {
    private static String sOaid = "";
    private static AtomicBoolean isCallback = new AtomicBoolean(false);

    public static String getOaid() {
        return sOaid;
    }

    public static void getOaid(Context context, final oaidListener listener) {
        try {
            Class.forName("com.bun.miitmdid.core.MdidSdkHelper");
        } catch (ClassNotFoundException e) {
            if (listener != null) {
                listener.onGetOaid("");
                isCallback.set(true);
            }
            return;
        }
        try {
            com.bun.miitmdid.core.JLibrary.InitEntry(context);
            int error = com.bun.miitmdid.core.MdidSdkHelper.InitSdk(context, true, new IIdentifierCallback(listener));

            switch (error) {
                case ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                case ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT:
                case ErrorCode.INIT_ERROR_LOAD_CONFIGFILE:
                case ErrorCode.INIT_HELPER_CALL_ERROR:
                    DeveloperLog.LogD("get oaid error : " + error);
                    if (listener != null) {
                        listener.onGetOaid("");
                        isCallback.set(true);
                    }
                    break;
                case ErrorCode.INIT_ERROR_RESULT_DELAY:
                    WorkExecutor.execute(new Timeout(listener), 1, TimeUnit.SECONDS);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onGetOaid("");
                isCallback.set(true);
            }
        }
    }

    private static class IIdentifierCallback implements IIdentifierListener {

        private oaidListener mListener;

        public IIdentifierCallback(oaidListener listener) {
            mListener = listener;
        }

        @Override
        public void OnSupport(boolean b, IdSupplier idSupplier) {
            if (idSupplier != null && idSupplier.isSupported()) {
                sOaid = idSupplier.getOAID();
                DeveloperLog.LogD("oaid : " + sOaid);
                if (mListener != null && !isCallback.get()) {
                    mListener.onGetOaid(sOaid);
                    isCallback.set(true);
                }
            } else {
                if (mListener != null && !isCallback.get()) {
                    mListener.onGetOaid(sOaid);
                    isCallback.set(true);
                }
            }
        }
    }

    private static class Timeout implements Runnable {

        private oaidListener mListener;

        public Timeout(oaidListener listener) {
            mListener = listener;
        }

        @Override
        public void run() {
            if (mListener != null && !isCallback.get()) {
                mListener.onGetOaid("");
                isCallback.set(true);
            }
        }
    }

    public interface oaidListener {
        void onGetOaid(String oaid);
    }
}
