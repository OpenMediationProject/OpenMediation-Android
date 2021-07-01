/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.demo;

import android.app.Application;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.demo.utils.NewApiUtils;
import com.openmediation.sdk.utils.error.Error;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        initSDK();
    }

    private void initSDK() {
        NewApiUtils.printLog("start init sdk");
        InitConfiguration configuration = new InitConfiguration.Builder()
                .appKey(NewApiUtils.APPKEY)
                // TODO
//                .preloadAdTypes(OmAds.AD_TYPE.REWARDED_VIDEO)
                .logEnable(true)
                .build();
        OmAds.init(configuration, new InitCallback() {
            @Override
            public void onSuccess() {
                NewApiUtils.printLog("init success");
            }

            @Override
            public void onError(Error result) {
                NewApiUtils.printLog("init failed " + result.toString());
            }
        });
    }
}
