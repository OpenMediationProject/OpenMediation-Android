// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.openmediation.sdk.demo.R;
import com.openmediation.sdk.splash.SplashAd;
import com.openmediation.sdk.splash.SplashAdListener;

public class SplashAdActivity extends Activity implements SplashAdListener {

    ViewGroup mSplashContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_splash);
        mSplashContainer = findViewById(R.id.splash_container);
        SplashAd.setSplashAdListener(this);
        mSplashContainer.post(() -> {
            int width = mSplashContainer.getWidth();
            int height = mSplashContainer.getHeight();
            SplashAd.setSize(width, height);
            SplashAd.setLoadTimeout(3000);
            SplashAd.loadAd();
        });
    }

    @Override
    public void onSplashAdLoad() {
        Log.e("SplashAdActivity", "----------- onSplashAdLoad ----------");
        SplashAd.showAd(SplashAdActivity.this, mSplashContainer);
    }

    @Override
    public void onSplashAdFailed(String error) {
        Log.e("SplashAdActivity", "----------- onSplashAdFailed ----------" + error);
    }

    @Override
    public void onSplashAdClicked() {
        Log.e("SplashAdActivity", "----------- onSplashAdClicked ----------");
    }

    @Override
    public void onSplashAdShowed() {
        Log.e("SplashAdActivity", "----------- onSplashAdShowed ----------");
    }

    @Override
    public void onSplashAdShowFailed(String error) {
        Log.e("SplashAdActivity", "----------- onSplashAdShowFailed ----------" + error);
    }

    @Override
    public void onSplashAdTick(long millisUntilFinished) {
        Log.e("SplashAdActivity", "----------- onSplashAdTick ----------" + millisUntilFinished);
    }

    @Override
    public void onSplashAdDismissed() {
        Log.e("SplashAdActivity", "----------- onSplashAdDismissed ----------");
        finish();
    }

    @Override
    protected void onDestroy() {
        SplashAd.setSplashAdListener(null);
        super.onDestroy();
    }
}
