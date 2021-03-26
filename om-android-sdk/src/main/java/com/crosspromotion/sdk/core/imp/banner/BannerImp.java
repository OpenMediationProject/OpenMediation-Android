// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.core.imp.banner;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.crosspromotion.sdk.banner.AdSize;
import com.crosspromotion.sdk.banner.BannerAdListener;
import com.crosspromotion.sdk.bean.AdBean;
import com.crosspromotion.sdk.core.AbstractAdsManager;
import com.crosspromotion.sdk.report.AdReport;
import com.crosspromotion.sdk.utils.Cache;
import com.crosspromotion.sdk.utils.GpUtil;
import com.crosspromotion.sdk.utils.PUtils;
import com.crosspromotion.sdk.utils.Visibility;
import com.crosspromotion.sdk.utils.error.Error;
import com.crosspromotion.sdk.utils.error.ErrorBuilder;
import com.crosspromotion.sdk.utils.error.ErrorCode;
import com.crosspromotion.sdk.utils.webview.BaseWebView;
import com.crosspromotion.sdk.utils.webview.BaseWebViewClient;
import com.crosspromotion.sdk.utils.webview.JsBridge;
import com.crosspromotion.sdk.utils.webview.JsBridgeConstants;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.utils.DensityUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.SdkUtil;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.PlacementInfo;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BannerImp extends AbstractAdsManager implements JsBridge.MessageListener,
        View.OnAttachStateChangeListener {

    private FrameLayout mLytBanner;
    private BaseWebView mBannerView;
    private JsBridge mJsBridge;
    private BannerWebClient mBannerWebClient;
    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private HandlerUtil.HandlerHolder mHandler;
    private RefreshTask mRefreshTask;
    private AdSize mAdSize;
//    private VolumeReceiver mReceiver;
    private boolean mCurrentAutoRefreshStatus = false;

    public BannerImp(String placementId, FrameLayout lytBanner) {
        super(placementId);

        mLytBanner = lytBanner;
        FrameLayout.LayoutParams lytParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mLytBanner.setLayoutParams(lytParams);
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
    }

    public void setAdSize(AdSize adSize) {
        mAdSize = adSize;
    }

    public void setListener(BannerAdListener adListener) {
        mListenerWrapper.setBannerListener(adListener);
    }

    @Override
    protected int getAdType() {
        return CommonConstants.BANNER;
    }

    @Override
    protected boolean isIntervalLoadType() {
        return isRefreshing.get();
    }

    @Override
    public void destroy() {
        super.destroy();
        mAdBean = null;

//        if (mReceiver != null) {
//            mReceiver.setMuteModeListener(null);
//            mReceiver.unRegister(mContext);
//            mReceiver = null;
//        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mRefreshTask);
            mRefreshTask = null;
            mHandler = null;
        }

        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mJsBridge != null) {
                        mJsBridge.release();
                        mJsBridge = null;
                    }
                    mLytBanner.removeAllViews();
                    if (mBannerView != null) {
                        mBannerView.removeJavascriptInterface("sdk");
                        mBannerView.removeAllViews();
                        mBannerView.setWebViewClient(null);
                        mBannerView.stopLoading();
                        mBannerView.clearHistory();
                        mBannerView.freeMemory();
                        mBannerView.destroy();
                        mBannerWebClient = null;
                        mBannerView = null;
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    @Override
    protected void onAdsLoadSuccess(AdBean bean) {
        super.onAdsLoadSuccess(bean);
        updateRefreshStatus(false);
        drawBanner();
    }

    @Override
    protected void onAdsLoadFailed(Error error) {
        super.onAdsLoadFailed(error);
        if (!isRefreshing.compareAndSet(true, false)) {
            callbackAdsFailed(error);
        }
    }

    @Override
    protected void callbackAdsReady() {
        super.callbackAdsReady();
        mListenerWrapper.onBannerAdsReady(mPlacementId, mLytBanner);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        try {
            if (mJsBridge != null) {
                mJsBridge.reportShowEvent();
            }
            onAdsShowed();
        } catch (Exception e) {
            DeveloperLog.LogE("adt-banner onViewAttachedToWindow ", e);
            CrashUtil.getSingleton().saveException(e);
            onAdsShowFailed(ErrorBuilder.build(ErrorCode.CODE_SHOW_UNKNOWN_EXCEPTION));
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
    }

    private void click() {
        AdReport.CLKReport(mContext, mPlacementId, mAdBean);
        PUtils.doClick(mContext, mPlacementId, mAdBean);
        onAdsClicked();
    }

    private void addEvent(final String event) {
        onAddEvents(event);
    }

    private void wvClick() {
        AdReport.CLKReport(mContext, mPlacementId, mAdBean);
        onAdsClicked();
    }

    private void loadBannerUrl() throws Exception {
        String url = mAdBean.getResources().get(0);
        boolean existCache = Cache.existCache(mContext, url);
        if (existCache) {
            File file = Cache.getCacheFile(mContext, url, null);
            String data = IOUtil.toString(IOUtil.getFileInputStream(file), CommonConstants.CHARTSET_UTF8);
            mBannerView.loadDataWithBaseURL(url, data, "text/html",
                    CommonConstants.CHARTSET_UTF8, null);
        } else {
            mBannerView.loadUrl(url);
        }
    }

    private void refreshAd(long delay) {
        try {
            if (isRefreshing.get() || mHandler == null) {
                return;
            }
            if (mRefreshTask == null) {
                mRefreshTask = new RefreshTask();
            }
            mHandler.postDelayed(mRefreshTask, delay);
        } catch (Exception e) {
            updateRefreshStatus(false);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private void drawBanner() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mAdBean == null) {
                    onAdsLoadFailed(ErrorBuilder.build(ErrorCode.CODE_LOAD_DESTROYED));
                    return;
                }
                try {
                    if (mBannerView == null) {
                        mBannerView = new BaseWebView(mContext);
                        int[] size = getSize(mContext);
                        FrameLayout.LayoutParams bannerViewParams = new FrameLayout.LayoutParams(
                                DensityUtil.dip2px(mContext, size[0]), DensityUtil.dip2px(mContext, size[1]));
                        bannerViewParams.gravity = Gravity.CENTER;
                        mLytBanner.addView(mBannerView, bannerViewParams);
                    }
                    setUpJsInterface();
                    setUpWebClient();
//                    registerMuteReceiver();
                    loadBannerUrl();
                    mLytBanner.removeOnAttachStateChangeListener(BannerImp.this);
                    mLytBanner.addOnAttachStateChangeListener(BannerImp.this);

                    callbackAdsReady();
                } catch (Exception e) {
                    onAdsLoadFailed(ErrorBuilder.build(ErrorCode.CODE_LOAD_BANNER_UNKNOWN_EXCEPTION));
                    DeveloperLog.LogE("BannerImp drawBanner error : " + e.getMessage());
                }
            }
        };
        HandlerUtil.runOnUiThread(runnable);
    }

    private void setUpJsInterface() {
        if (mJsBridge == null) {
            mJsBridge = new JsBridge();
        }
        mJsBridge.injectJavaScript(mBannerView);
        mJsBridge.setMessageListener(this);
        mJsBridge.setPlacementId(mPlacementId);
        mJsBridge.setAbt(DataCache.getInstance().getFromMem(mPlacementId + KeyConstants.KEY_DISPLAY_ABT, Integer.class));
        mJsBridge.setCampaign(mAdBean.getAdString());
    }

    private void setUpWebClient() {
        if (mBannerWebClient == null) {
            mBannerWebClient = new BannerWebClient(mContext, mAdBean.getPkgName());
        }
        mBannerView.setWebViewClient(mBannerWebClient);
    }

//    private void registerMuteReceiver() {
//        if (mReceiver == null) {
//            mReceiver = new VolumeReceiver();
//        }
//        mReceiver.setMuteModeListener(this);
//        mReceiver.register(mContext);
//    }

    private void updateRefreshStatus(boolean isRefresh) {
        isRefreshing.set(isRefresh);
    }

    private int[] getSize(Context context) {
        AdSize adSize = mAdSize;
        if (adSize == null) {
            adSize = AdSize.BANNER;
        } else if (mAdSize == AdSize.SMART) {
            if (CustomBannerEvent.isLargeScreen(context)) {
                adSize = AdSize.LEADERBOARD;
            } else {
                adSize = AdSize.BANNER;
            }
        }
        return new int[]{adSize.getWidth(), adSize.getHeight()};
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        int[] size = getSize(mContext);
        return new PlacementInfo(mPlacementId).getBannerPlacementInfo(size[0], size[1]);
    }

    @Override
    public void onReceiveMessage(String method, JSONObject data) {
        if (JsBridgeConstants.METHOD_CLICK.equals(method)) {
            click();
        } else if (JsBridgeConstants.METHOD_WV_CLICK.equals(method)) {
            wvClick();
        } else if (JsBridgeConstants.METHOD_REFRESH_AD.equals(method)) {
            if (data == null) {
                return;
            }
            long delay = data.optLong("delay");
            refreshAd(delay);
        } else if (JsBridgeConstants.METHOD_PUSH_EVENT.equals(method)) {
            if (data == null) {
                return;
            }
            String event = data.optString("e");
            if (!TextUtils.isEmpty(event)) {
                addEvent(event);
            }
        }
    }

//    @Override
//    public void onMuteMode(boolean mute) {
//        if (mJsBridge == null) {
//            return;
//        }
//        mJsBridge.reportEvent(mute ? JsBridgeConstants.EVENT_MUTE : JsBridgeConstants.EVENT_UNMUTE);
//    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            try {
                // not in foreground, stop load AD
                if (!mCurrentAutoRefreshStatus) {
                    return;
                }
                if (mLytBanner.getVisibility() != View.VISIBLE) {
                    return;
                }
                updateRefreshStatus(true);
                loadAds(mLoadParams);
            } catch (Exception e) {
                updateRefreshStatus(false);
                CrashUtil.getSingleton().saveException(e);
            }
        }
    }

    private class BannerWebClient extends BaseWebViewClient {
        private boolean isJumped = false;

        BannerWebClient(Context context, String pkgName) {
            super(context, pkgName);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isJumped) {
                isJumped = false;
                return true;
            }
            boolean result = super.shouldOverrideUrlLoading(view, url);
            if (!result) {
                try {
                    if (GpUtil.isGp(url)) {
                        GpUtil.goGp(view.getContext().getApplicationContext(), url);
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
                isJumped = true;
                view.stopLoading();
            }
            return true;
        }
    }

    public void setAdVisibility(final int visibility) {
        if (Visibility.isScreenVisible(visibility)) {
            resumeRefresh();
        } else {
            pauseRefresh();
        }
    }

    private void resumeRefresh() {
        mCurrentAutoRefreshStatus = true;
        try {
            setUpJsInterface();
            setUpWebClient();
            loadBannerUrl();
        } catch (Exception e) {
            DeveloperLog.LogE("Banner load url exception: ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private void pauseRefresh() {
        mCurrentAutoRefreshStatus = false;
    }
}
