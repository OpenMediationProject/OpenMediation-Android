// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.demo;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.api.unity.NmSdk;
import com.nbmediation.sdk.demo.utils.NewApiUtils;
import com.nbmediation.sdk.utils.error.Error;

public class UnityBannerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewApiUtils.ENABLE_LOG = true;
        setContentView(R.layout.activity_unity_banner_test);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorStatuBar));
        }
        initSDK();
    }


    private void initSDK() {
        NewApiUtils.printLog("start init sdk");
        NmSdk.init(this, NewApiUtils.APPKEY, new InitCallback() {
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

    public void loadBanner1(View view) {
        NmSdk.loadBanner(this, "260");
    }

    public void loadBanner2(View view) {
        NmSdk.loadBanner(this, "259");
    }

    public void showBanner1(View view) {
        NmSdk.showBanner(this, "260");
    }

    public void showBanner2(View view) {
        NmSdk.showBanner(this, "259");
    }

    public void isBannerReady1(View view) {
        boolean isReady = NmSdk.isBannerReady("260");
        Toast.makeText(this, "isReady=" + isReady, Toast.LENGTH_LONG).show();

    }

    public void isBannerReady2(View view) {
        boolean isReady = NmSdk.isBannerReady("259");
        Toast.makeText(this, "isReady=" + isReady, Toast.LENGTH_LONG).show();
    }

    public void hideBanner1(View view) {
        NmSdk.hideBanner(this, "260", false);
    }

    public void hideBanner2(View view) {
        NmSdk.hideBanner(this, "259", false);
    }

    public void destroyAll(View view) {
        NmSdk.destroyAllForBanner(this);
    }
}
