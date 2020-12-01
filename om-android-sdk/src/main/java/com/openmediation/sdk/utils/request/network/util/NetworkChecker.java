// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.openmediation.sdk.utils.AdtUtil;

/**
 * The type Network checker.
 */
public class NetworkChecker {

    /**
     * The enum Net type.
     */
    public enum NetType {
        /**
         * 0 Unknown
         */
        UNKNOWN(0),
        /**
         * 1 Ethernet
         */
        ETHERNET(1),
        /**
         * 2 WIFI
         */
        WIFI(2),
        /**
         * 3 Cellular Network-Unknown Generation
         */
        MOBILE(3),
        /**
         * mobile2G
         */
        MOBILE_2G(4),
        /**
         * mobile3G
         */
        MOBILE_3G(5),
        /**
         * mobile4G
         */
        MOBILE_4G(6),
        /**
         * mobile4G
         */
        MOBILE_5G(7),
        /**
         * mobile4G
         */
        MOBILE_6G(8);

        private int mValue;

        NetType(int value) {
            this.mValue = value;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public int getValue() {
            return this.mValue;
        }
    }

    /**
     * Check the network is enable.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isAvailable(Context context) {
        if (context == null) {
            context = AdtUtil.getApplication();
        }
        return NetType.UNKNOWN.getValue() != getConnectType(context);
    }

    /**
     * Gets network operator.
     *
     * @param context context
     * @return operator name
     */
    public static String getNetworkOperator(Context context) {
        if (context == null) {
            return "";
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperator() + telephonyManager.getNetworkOperatorName();
    }


    /**
     * 0 Unknown
     * 1 Ethernet
     * 2 WIFI
     * 3 Cellular Network-Unknown Generation
     * 4 Cellular Network-2G
     * 5 Cellular Network-3G
     * 6 Cellular Network-4G
     * 7 Cellular Network-5G
     * 8 Cellular Network-6G
     *
     * @param context context
     * @return the connect type
     */
    public static int getConnectType(Context context) {
        if (context == null) {
            return NetType.UNKNOWN.getValue();
        }
        NetType netType = null;
        NetworkInfo networkInfo = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo != null) {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_ETHERNET:
                    netType = NetType.ETHERNET;
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    netType = NetType.WIFI;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    netType = getMobileNetType(context);
                    break;
                default:
                    netType = NetType.UNKNOWN;
                    break;
            }
        }
        if (netType == null) {
            netType = NetType.UNKNOWN;
        }
        return netType.getValue();
    }

    private static NetType getMobileNetType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager == null) {
            return NetType.MOBILE;
        }
        try {
            int networkType = telephonyManager.getNetworkType();
            switch (networkType) {
                //2G
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NetType.MOBILE_2G;
                //3G
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NetType.MOBILE_3G;
                //4G
                case TelephonyManager.NETWORK_TYPE_LTE:
                case TelephonyManager.NETWORK_TYPE_GSM:
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    return NetType.MOBILE_4G;
                case TelephonyManager.NETWORK_TYPE_NR:
                    return NetType.MOBILE_5G;
                //add 6g here
                default:
                    return NetType.MOBILE;
            }
        } catch (Exception e) {
            return NetType.MOBILE;
        }
    }
}