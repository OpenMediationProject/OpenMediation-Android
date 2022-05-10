/*
 * Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.demo;

import android.app.Application;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.demo.utils.Constants;
import com.openmediation.sdk.utils.error.Error;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        initSDK();
    }

    private void initSDK() {
        Constants.printLog("start init sdk");
        InitConfiguration configuration = new InitConfiguration.Builder()
                .appKey(Constants.APPKEY)
                .logEnable(true)
                .build();
        OmAds.init(configuration, new InitCallback() {
            @Override
            public void onSuccess() {
                Constants.printLog("init success");
            }

            @Override
            public void onError(Error result) {
                Constants.printLog("init failed " + result.toString());
            }
        });
    }
}
