// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.openmediation.sdk.demo.utils.NewApiUtils;
import com.openmediation.sdk.splash.SplashAd;
import com.openmediation.sdk.splash.SplashAdListener;

public class SplashAdActivity extends Activity implements SplashAdListener {

    ViewGroup mSplashContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_splash);
        mSplashContainer = findViewById(R.id.splash_container);
        SplashAd.setSplashAdListener(NewApiUtils.P_SPLASH, this);
        mSplashContainer.post(() -> {
            int width = mSplashContainer.getWidth();
            int height = mSplashContainer.getHeight();
            SplashAd.setSize(NewApiUtils.P_SPLASH, width, height);
            SplashAd.setLoadTimeout(NewApiUtils.P_SPLASH, 3000);
            SplashAd.loadAd(NewApiUtils.P_SPLASH);
        });
    }

    @Override
    public void onSplashAdLoad(String placementId) {
        Log.e("SplashAdActivity", "----------- onSplashAdLoad ----------");
        SplashAd.showAd(SplashAdActivity.this, NewApiUtils.P_SPLASH, mSplashContainer);
    }

    @Override
    public void onSplashAdFailed(String placementId, String error) {
        Log.e("SplashAdActivity", "----------- onSplashAdFailed ----------" + error);
    }

    @Override
    public void onSplashAdClicked(String placementId) {
        Log.e("SplashAdActivity", "----------- onSplashAdClicked ----------");
    }

    @Override
    public void onSplashAdShowed(String placementId) {
        Log.e("SplashAdActivity", "----------- onSplashAdShowed ----------");
    }

    @Override
    public void onSplashAdShowFailed(String placementId, String error) {
        Log.e("SplashAdActivity", "----------- onSplashAdShowFailed ----------" + error);
    }

    @Override
    public void onSplashAdTick(String placementId, long millisUntilFinished) {
        Log.e("SplashAdActivity", "----------- onSplashAdTick ----------" + millisUntilFinished);
    }

    @Override
    public void onSplashAdDismissed(String placementId) {
        Log.e("SplashAdActivity", "----------- onSplashAdDismissed ----------");
        finish();
    }

    @Override
    protected void onDestroy() {
        SplashAd.setSplashAdListener(NewApiUtils.P_SPLASH, null);
        super.onDestroy();
    }
}
