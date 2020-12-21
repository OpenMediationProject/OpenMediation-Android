// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.SDKInitStatusListener;

import java.util.Map;

public class MintegralSingleTon {
    private static class MintegralHolder {
        private static final MintegralSingleTon INSTANCE = new MintegralSingleTon();
    }

    private boolean mDidInitSdk;

    private MintegralSingleTon() {
    }

    public static MintegralSingleTon getInstance() {
        return MintegralHolder.INSTANCE;
    }

    public void initSDK(final Context context, String appKey, final SDKInitStatusListener listener) {
        try {
            if (mDidInitSdk) {
                if (listener != null) {
                    listener.onInitSuccess();
                }
                return;
            }
            String[] tmp = appKey.split("#");
            String appId = tmp[0];
            String key = tmp[1];
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, String> map = sdk.getMTGConfigurationMap(appId, key);
            sdk.initAsync(map, context.getApplicationContext(), new SDKInitStatusListener() {
                @Override
                public void onInitSuccess() {
                    mDidInitSdk = true;
                    if (listener != null) {
                        listener.onInitSuccess();
                    }
                }

                @Override
                public void onInitFail() {
                    if (listener != null) {
                        listener.onInitFail();
                    }
                }
            });
        } catch (Exception e) {
            if (listener != null) {
                listener.onInitFail();
            }
        }
    }

    public boolean isInit() {
        return mDidInitSdk;
    }
}
