// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.qq.e.comm.managers.GDTAdSdk;

public class TencentAdManagerHolder {

    private static boolean isInit = false;

    public synchronized static boolean init(Context context, String appKey) {
        if (context == null) {
            return false;
        }
        GDTAdSdk.init(context.getApplicationContext(), appKey);
        isInit = true;
        return true;
    }

    static boolean isInit() {
        return isInit;
    }
}
