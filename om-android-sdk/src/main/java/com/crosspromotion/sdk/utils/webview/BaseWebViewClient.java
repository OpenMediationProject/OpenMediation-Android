package com.crosspromotion.sdk.utils.webview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crosspromotion.sdk.utils.GpUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.SdkUtil;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.device.DeviceUtil;

import java.lang.ref.WeakReference;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BaseWebViewClient extends WebViewClient {

    private WeakReference<Context> mActRef;
    protected String mPkgName;

    public BaseWebViewClient(Context context, String pkgName) {
        mActRef = new WeakReference<>(context);
        mPkgName = pkgName;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            if (TextUtils.isEmpty(mPkgName)) {
                return super.shouldOverrideUrlLoading(view, url);
            }
            if (GpUtil.isGp(url)) {
                return super.shouldOverrideUrlLoading(view, url);
            } else if (url.startsWith("file")) {
                return super.shouldOverrideUrlLoading(view, url);
            } else if (!url.startsWith("http")) {
                if (mActRef == null || mActRef.get() == null) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
                if (handleOverrideUrlLoading(url)) {
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("shouldOverrideUrlLoading error", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    private boolean handleOverrideUrlLoading(String url) throws Exception {
        if (DeviceUtil.isPkgInstalled(mPkgName)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent intent;
                if (url.startsWith("android-app://")) {
                    intent = Intent.parseUri(url, Intent.URI_ANDROID_APP_SCHEME);
                } else {
                    intent = GpUtil.parseIntent(url);
                }
                if (intent == null) {
                    return false;
                }
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mActRef.get().startActivity(intent);
                return true;
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setData(Uri.parse(url));
                mActRef.get().startActivity(intent);
                return true;
            }
        } else {
            SdkUtil.copy(url);
            String landingUrl = "market://details?id=" + mPkgName;
            GpUtil.goGp(mActRef.get(), landingUrl);
            return true;
        }
    }
}
