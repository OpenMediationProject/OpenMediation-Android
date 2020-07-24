// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;

public class TTAdManagerHolder {

    private static boolean sInit;

    public static TTAdManager get() {
        if (!sInit) {
            throw new RuntimeException("TTAdSdk is not init, please check.");
        }
        return TTAdSdk.getAdManager();
    }

    public static void init(Context context, String appId, Boolean consent, Boolean ageRestricted) {
        if (context == null) {
            return;
        }
        if (!sInit) {
            TTAdSdk.init(context, buildConfig(context, appId, consent, ageRestricted));
            sInit = true;
        }
    }

    private static TTAdConfig buildConfig(Context context, String appId, Boolean consent, Boolean ageRestricted) {
        TTAdConfig.Builder builder = new TTAdConfig.Builder()
                .appId(appId)
                .useTextureView(true)
                .appName(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString())
                .allowShowPageWhenScreenLock(false)
                .supportMultiProcess(false);
        if (consent != null) {
            // 0 close GDRP Privacy protection ，1: open GDRP Privacy protection
            builder.setGDPR(consent ? 0 : 1);
        }
        if (ageRestricted != null) {
            // 0:adult ，1:child
            builder.coppa(ageRestricted ? 1 : 0);
        }
        return builder.build();
    }
}
