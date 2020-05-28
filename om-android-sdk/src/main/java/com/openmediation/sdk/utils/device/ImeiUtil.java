// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.device;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.KeyConstants;

import java.lang.reflect.Method;

public class ImeiUtil {

    public static String getIMEI(Context context) {
        String imei;
        if (DataCache.getInstance().containsKey(KeyConstants.RequestBody.KEY_DID)) {
            imei = DataCache.getInstance().get(KeyConstants.RequestBody.KEY_DID, String.class);
        } else {
            imei = getDeviceId(context);
            if (!TextUtils.isEmpty(imei)) {
                DataCache.getInstance().set(KeyConstants.RequestBody.KEY_DID, imei);
            }
        }
        return imei;
    }

    private static String getDeviceId(Context context) {
        String imei = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) {
                return imei;
            }
            if (Build.VERSION.SDK_INT < 21) {
                return tm.getDeviceId();
            }
            try {
                Method method = tm.getClass().getMethod("getImei");
                imei = (String) method.invoke(tm);
            } catch (Exception e) {
            }
        } catch (Throwable e) {
            AdLog.getSingleton().LogD(e.getLocalizedMessage());
        }
        return imei;
    }
}
