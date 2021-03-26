// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.crosspromotion.sdk.bean.AdBean;
import com.crosspromotion.sdk.utils.GpUtil;
import com.crosspromotion.sdk.utils.webview.ActWebView;
import com.crosspromotion.sdk.utils.webview.BaseWebView;
import com.crosspromotion.sdk.utils.webview.BaseWebViewClient;
import com.crosspromotion.sdk.utils.webview.JsBridge;
import com.crosspromotion.sdk.view.DrawCrossMarkView;
import com.openmediation.sdk.utils.DensityUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.SdkUtil;
import com.openmediation.sdk.utils.crash.CrashUtil;

import java.util.HashMap;
import java.util.Map;


public class ActionActivity extends Activity {

    private RelativeLayout mLytWeb;
    private JsBridge mJsBridge;
    private HandlerUtil.HandlerHolder mHandler;
    private TimeoutRunnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mLytWeb = new RelativeLayout(this);
            setContentView(mLytWeb);
            String placementId = getIntent().getStringExtra("placementId");
            if (getIntent().getExtras() != null) {
                getIntent().getExtras().setClassLoader(AdBean.class.getClassLoader());
            }
            String adBeanString = getIntent().getStringExtra("adBean");
            AdBean adBean = AdBean.toAdBean(adBeanString);
            initAndLoad(placementId, adBean);
        } catch (Throwable e) {
            DeveloperLog.LogD("ActionActivity", e);
            CrashUtil.getSingleton().saveException(e);
            finish();
        }
    }


    private void initAndLoad(String placementId, AdBean adBean) {
        if (TextUtils.isEmpty(adBean.getAdUrl())) {
            finish();
            return;
        }

        BaseWebView mAdView = ActWebView.getInstance().getActView();

        if (mJsBridge == null) {
            mJsBridge = new JsBridge();
        }
        mJsBridge.injectJavaScript(mAdView);
        mJsBridge.setPlacementId(placementId);
        mJsBridge.setCampaign(adBean.getAdString());
        if (mAdView.getParent() != null) {
            ViewGroup vg = (ViewGroup) mAdView.getParent();
            vg.removeView(mAdView);
        }
        mLytWeb.addView(mAdView);
        mAdView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
        mAdView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

        if (adBean.isWebView()) {
            mAdView.setVisibility(View.VISIBLE);
            //Exit button
            DrawCrossMarkView drawCrossMarkView = new DrawCrossMarkView(this, Color.GRAY);
            mLytWeb.addView(drawCrossMarkView);
            drawCrossMarkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            int size = DensityUtil.dip2px(this, 20);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(30, 30, 30, 30);
            drawCrossMarkView.setLayoutParams(params);
        } else {
            mAdView.setVisibility(View.GONE);
            ProgressBar progressBar = new ProgressBar(this);
            mLytWeb.addView(progressBar);
            int size = DensityUtil.dip2px(this, 40);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            progressBar.setLayoutParams(params);

            sendTimeoutMsg();
        }
        String adUrl = adBean.getAdUrl();
        if (adUrl.contains("{scene}")) {
            adUrl = adUrl.replace("{scene}", "");
        }
        mAdView.setWebViewClient(new AdWebClient(this, adBean.getPkgName()));
        final Map<String, String> additionalHttpHeaders = new HashMap<>();
        additionalHttpHeaders.put("Cache-Control", "no-cache");
        mAdView.loadUrl(adUrl, additionalHttpHeaders);
    }

    @Override
    protected void onDestroy() {
        if (mLytWeb != null) {
            mLytWeb.removeAllViews();
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
            mHandler = null;
        }
        if (mJsBridge != null) {
            mJsBridge.release();
            mJsBridge = null;
        }
        ActWebView.getInstance().destroy("sdk");
        super.onDestroy();
    }

    private void sendTimeoutMsg() {
        if (mHandler == null) {
            mHandler = new HandlerUtil.HandlerHolder(null);
        }

        if (mRunnable == null) {
            mRunnable = new TimeoutRunnable();
        }
        mHandler.postDelayed(mRunnable, 8000);
    }

    private class AdWebClient extends BaseWebViewClient {

        AdWebClient(Activity activity, String pkgName) {
            super(activity, pkgName);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean result = super.shouldOverrideUrlLoading(view, url);
            if (!result) {
                try {
                    if (GpUtil.isGp(url)) {
                        GpUtil.goGp(view.getContext().getApplicationContext(), url);
                        finish();
                    } else {
                        if (SdkUtil.isAcceptedScheme(url)) {
                            view.loadUrl(url);
                        }
                    }
                } catch (Exception e) {
                    DeveloperLog.LogD("shouldOverrideUrlLoading error", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            } else {
                finish();
            }
            return true;
        }
    }

    private class TimeoutRunnable implements Runnable {

        @Override
        public void run() {
            finish();
        }
    }
}
