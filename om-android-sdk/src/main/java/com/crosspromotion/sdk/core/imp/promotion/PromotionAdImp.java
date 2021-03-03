// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.core.imp.promotion;

import android.app.Activity;

import com.crosspromotion.sdk.bean.AdBean;
import com.crosspromotion.sdk.core.AbstractAdsManager;
import com.crosspromotion.sdk.core.CallbackBridge;
import com.crosspromotion.sdk.promotion.PromotionAdListener;
import com.crosspromotion.sdk.promotion.PromotionAdRect;
import com.crosspromotion.sdk.utils.PUtils;
import com.crosspromotion.sdk.utils.ResponseUtil;
import com.crosspromotion.sdk.utils.error.Error;
import com.crosspromotion.sdk.utils.error.ErrorBuilder;
import com.crosspromotion.sdk.utils.error.ErrorCode;
import com.crosspromotion.sdk.view.PromotionAdView;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.model.Placement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PromotionAdImp extends AbstractAdsManager {

    private int mStock = 1;

    private final AtomicBoolean mShouldCallback = new AtomicBoolean(true);

    private final ConcurrentLinkedQueue<AdBean> mAdBeanQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<AdBean> mStockQueue = new ConcurrentLinkedQueue<>();

    private final AtomicInteger mPendingCount = new AtomicInteger(0);
    private final AtomicInteger mSuccessCount = new AtomicInteger(0);
    private final AtomicInteger mFailedCount = new AtomicInteger(0);

    public PromotionAdImp(String placementId) {
        super(placementId);
        Placement placement = PUtils.getPlacement(mPlacementId);
        if (placement != null) {
            setStock(placement.getCs());
        }
    }

    @Override
    protected int getAdType() {
        return CommonConstants.PROMOTION;
    }

    public void setListener(PromotionAdListener adListener) {
        mListenerWrapper.setPromotionListener(adListener);
    }

    private void setStock(int stock) {
        if (stock <= 0) {
            return;
        }
        mStock = stock;
    }

    @Override
    public boolean isReady() {
        boolean ready = internalReady();
        updateStock(mLoadParams);
        return ready;
    }

    @Override
    public void loadAds(Map extras) {
        boolean ready = internalReady();
        if (ready) {
            callbackAdsReady();
        }
        updateStock(extras);
    }

    @Override
    protected void preLoadRes(final List<AdBean> adBeanList) {
        for (AdBean adBean : adBeanList) {
            if (adBean != null && adBean.getResources() != null && !adBean.getResources().isEmpty()) {
                mAdBeanQueue.offer(adBean);
            }
        }
        if (mAdBeanQueue.isEmpty()) {
            onAdsLoadFailed(ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR));
            return;
        }
        internalPreLoad();
    }

    public void showAds(Activity activity, PromotionAdRect rect) {
        if (activity == null || activity.isFinishing()) {
            onAdsShowFailed(new Error(ErrorCode.CODE_SHOW_INVALID_ARGUMENT, ErrorCode.MSG_SHOW_INVALID_ARGUMENT + "activity is isDestroyed"));
            return;
        }
        if (rect == null || (rect.getWidth() <= 0 && rect.getHeight() <= 0)) {
            onAdsShowFailed(new Error(ErrorCode.CODE_SHOW_INVALID_ARGUMENT, ErrorCode.MSG_SHOW_INVALID_ARGUMENT + "PromotionAd width or height must be positive"));
            return;
        }
        CallbackBridge.addListenerToMap(mPlacementId, this);
        if (mStockQueue.isEmpty()) {
            onAdsShowFailed(ErrorBuilder.build(ErrorCode.CODE_SHOW_FAIL_NOT_READY));
            return;
        }
        showPromotionAd(activity, mStockQueue.poll(), rect);
    }

    public void hideAds() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!PromotionAdView.getInstance().isShowing()) {
                    DeveloperLog.LogD("PromotionAd not showing, placementId: " + mPlacementId);
                    return;
                }
                PromotionAdView.getInstance().hide();
                DeveloperLog.LogD("PromotionAd hide placementId: " + mPlacementId);
                onAdsClosed();
            }
        });
    }

    @Override
    protected void onAdsLoadSuccess(AdBean adBean) {
        // setInstanceRevenue
        ResponseUtil.setInstanceRevenue(mPlacementId, mLoadParams, adBean);

        PromotionAdView.getInstance().init();
        mSuccessCount.incrementAndGet();
        mStockQueue.offer(adBean);
        if (mShouldCallback.get()) {
            super.onAdsLoadSuccess(adBean);
            mShouldCallback.set(false);
        }
        DeveloperLog.LogD("PromotionAd onAdsLoadSuccess: PlacementId: " + mPlacementId + ", Stock size is " + mStockQueue.size() + ", AdBeanQueue size is " + mAdBeanQueue.size() + ",\n"
                + "SuccessCount is " + mSuccessCount.get() + ", FailedCount is " + mFailedCount.get() + ", PendingCount is " + mPendingCount.get());
    }

    @Override
    protected void onAdsLoadFailed(Error error) {
        if (error != null && error.getCode() != ErrorCode.CODE_LOAD_DOWNLOAD_FAILED) {
            DeveloperLog.LogE("PromotionAd onAdsLoadFailed: " + mPlacementId + ", " + error);
            if (mShouldCallback.get()) {
                super.onAdsLoadFailed(error);
                mShouldCallback.set(false);
            }
            return;
        }
        mFailedCount.incrementAndGet();
        DeveloperLog.LogD("PromotionAd onAdsLoadFailed: " + mPlacementId + ", " + error);
        DeveloperLog.LogD("PromotionAd onAdsLoadFailed: Stock size is " + mStockQueue.size() + ", AdBeanQueue size is " + mAdBeanQueue.size() + ",\n"
                + "SuccessCount is " + mSuccessCount.get() + ", FailedCount is " + mFailedCount.get() + ", PendingCount is " + mPendingCount.get());
        // download finish
        if (mAdBeanQueue.isEmpty()) {
            // all failed
            if (mFailedCount.get() >= mPendingCount.get()) {
                DeveloperLog.LogD("PromotionAd called super.onAdsLoadFailed(error)");
                if (mShouldCallback.get()) {
                    super.onAdsLoadFailed(error);
                    mShouldCallback.set(false);
                }
            }
        } else {
            afterCallback();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        CallbackBridge.removeListenerFromMap(mPlacementId);
    }

    private boolean internalReady() {
        if (mStockQueue.isEmpty()) {
            return false;
        }
        boolean ready = false;
        for (AdBean adBean : mStockQueue) {
            if (!adBean.isExpired()) {
                ready = true;
                break;
            } else {
                mStockQueue.remove(adBean);
            }
        }
        return ready;
    }

    private void internalPreLoad() {
        DeveloperLog.LogD("PromotionAd download resource internalPreLoad");
        int size = mStockQueue.size();
        mPendingCount.set(0);
        mSuccessCount.set(0);
        mFailedCount.set(0);
        while (!mAdBeanQueue.isEmpty()) {
            AdBean adBean = mAdBeanQueue.poll();
            mPendingCount.incrementAndGet();
            DeveloperLog.LogD("PromotionAd download resource mPendingCount.get()  = " + mPendingCount.get());
            preLoadResImpl(adBean);
            if (mPendingCount.get() >= mStock - size) {
                break;
            }
        }
    }

    private void afterCallback() {
        if (canPreload() && !isStocking()) {
            DeveloperLog.LogD("PromotionAd afterCallback: called internalPreLoad()");
            internalPreLoad();
        }
    }

    private void showPromotionAd(final Activity activity, final AdBean adBean, final PromotionAdRect rect) {
        // Set Show Instance Revenue
        ResponseUtil.setInstanceRevenue(mPlacementId, mLoadParams, adBean);

        mAdBean = adBean;
        PromotionAdView.AdRenderListener listener = new PromotionAdView.AdRenderListener() {
            @Override
            public void onRenderSuccess() {
                onAdsShowed();
            }

            @Override
            public void onRenderFailed(String msg) {
                DeveloperLog.LogE("PromotionAd show failed: " + msg);
                PromotionAdView.getInstance().hide();
                onAdsShowFailed(ErrorBuilder.build(ErrorCode.CODE_SHOW_UNKNOWN_EXCEPTION));
                onAdsClosed();
            }
        };
        PromotionAdView.getInstance().show(activity, rect, mPlacementId, adBean, listener);
    }

    private void updateStock(Map extras) {
        int size = mStockQueue.size();
        if (size >= mStock) {
            return;
        }
        DeveloperLog.LogD("PromotionAd updateStock: Stock size is " + size + ", AdBeanQueue size is " + mAdBeanQueue.size() + ",\n"
                + "SuccessCount is " + mSuccessCount.get() + ", FailedCount is " + mFailedCount.get() + ", PendingCount is " + mPendingCount.get());
        if (isStocking()) {
            DeveloperLog.LogD("PromotionAd updateStock resource downloading, return");
            return;
        }
        if (canPreload()) {
            internalPreLoad();
        } else {
            mShouldCallback.set(true);
            super.loadAds(extras);
        }
    }

    private boolean isStocking() {
        return mSuccessCount.get() + mFailedCount.get() < mPendingCount.get();
    }

    private boolean canPreload() {
        int size = mStockQueue.size();
        if (size >= mStock) {
            DeveloperLog.LogD("PromotionAd updateStock, StockQueue is full");
            return false;
        }
        boolean result = !mAdBeanQueue.isEmpty();
        DeveloperLog.LogD("PromotionAd updateStock, canPreload: " + result);
        return result;
    }
}
