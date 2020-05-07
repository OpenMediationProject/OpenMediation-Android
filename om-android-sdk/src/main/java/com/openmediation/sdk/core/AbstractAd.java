// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.bid.AdTimingAuctionManager;
import com.openmediation.sdk.bid.AdTimingBidResponse;
import com.openmediation.sdk.bid.AuctionCallback;
import com.openmediation.sdk.utils.ActLifecycle;
import com.openmediation.sdk.mediation.Callback;
import com.openmediation.sdk.utils.AdRateUtil;
import com.openmediation.sdk.utils.AdsUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.helper.HbHelper;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.helper.LrReportHelper;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.Preconditions;
import com.openmediation.sdk.utils.helper.WaterFallHelper;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.device.DeviceUtil;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorBuilder;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Placement;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.utils.request.network.Request;
import com.openmediation.sdk.utils.request.network.Response;
import com.openmediation.sdk.utils.request.network.util.NetworkChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * The type Abstract ad.
 */
public abstract class AbstractAd extends Callback implements Request.OnRequestCallback,
        AuctionCallback, InitCallback, HbHelper.OnHbCallback {

    /**
     * The M placement.
     */
    protected Placement mPlacement;
    /**
     * The M placement id.
     */
    protected String mPlacementId;
    /**
     * The Is destroyed.
     */
    protected boolean isDestroyed;
    /**
     * The M act ref.
     */
    protected WeakReference<Activity> mActRef;
    /**
     * The M current ins.
     */
    protected BaseInstance mCurrentIns;
    /**
     * The M bid responses.
     */
    protected List<AdTimingBidResponse> mBidResponses;
    /**
     * The Is manual triggered.
     */
    protected boolean isManualTriggered;
    private OmManager.LOAD_TYPE mLoadType;

    /**
     * The M total ins.
     */
    BaseInstance[] mTotalIns;
    /**
     * The M bs.
     */
    int mBs;
    /**
     * The Is fo.
     */
    boolean isFo;
    /**
     * The M pt.
     */
    int mPt;

    /**
     * The M load ts.
     */
    protected long mLoadTs;
    /**
     * The M callback ts.
     */
    protected long mCallbackTs;

    /**
     * Gets ad type.
     *
     * @return the ad type
     */
    protected abstract int getAdType();

    /**
     * Gets placement info.
     *
     * @return the placement info
     */
    protected abstract PlacementInfo getPlacementInfo();

    /**
     * Dispatch ad request.
     */
    protected abstract void dispatchAdRequest();

    /**
     * On ad error callback.
     *
     * @param error the error
     */
    protected abstract void onAdErrorCallback(String error);

    /**
     * On ad ready callback.
     */
    protected abstract void onAdReadyCallback();

    /**
     * On ad click callback.
     */
    protected abstract void onAdClickCallback();

    /**
     * Instantiates a new Abstract ad.
     *
     * @param activity    the activity
     * @param placementId the placement id
     */
    AbstractAd(Activity activity, String placementId) {
        isDestroyed = false;
        mActRef = new WeakReference<>(activity);
        mPlacementId = placementId;
        mBidResponses = new ArrayList<>();
    }

    /**
     * Load ad.
     *
     * @param type the type
     */
    public void loadAd(OmManager.LOAD_TYPE type) {
        //load returns if in the middle of initialization
        if (InitImp.isInitRunning()) {
            OmManager.getInstance().pendingInit(this);
            return;
        }

        //checks if initialization was successful
        if (!InitImp.isInit()) {
            InitImp.reInitSDK(mActRef.get(), this);
            return;
        }
        delayLoad(type);
    }

    /**
     * Destroy.
     */
    public void destroy() {
        mPlacement = null;
        if (mActRef != null) {
            mActRef.clear();
            mActRef = null;
        }
        isDestroyed = true;
    }


    @Override
    public void onSuccess() {
        //initialization successful. starts loading
        delayLoad(OmManager.LOAD_TYPE.INIT);
    }

    @Override
    public void onError(Error error) {
        callbackAdErrorOnUiThread(error != null ? error.toString() : "");
    }

    @Override
    public void onHbSuccess(int abt, BaseInstance[] instances) {
        mPlacement.setHbAbt(abt);
        AdTimingAuctionManager.getInstance().bid(mActRef.get(), mPlacement.getId(), instances, abt,
                mPlacement.getT(), this);
    }

    @Override
    public void onHbFailed(String error) {
        try {
            WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, null, this);
        } catch (Exception e) {
            callbackAdErrorOnUiThread(error);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    @Override
    public void onBidComplete(List<AdTimingBidResponse> responses) {
        try {
            if (responses != null) {
                mBidResponses.addAll(responses);
            }
            WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, responses, this);
        } catch (Exception e) {
            callbackAdErrorOnUiThread(e.getMessage());
            CrashUtil.getSingleton().saveException(e);
        }
    }

    @Override
    public void onRequestSuccess(Response response) {
        //parses the response. gets ins array, bs size, pt size，fo switch, campaign info
        try {
            if (response == null || response.code() != HttpsURLConnection.HTTP_OK) {
                callbackAdErrorOnUiThread(ErrorCode.ERROR_NO_FILL);
                return;
            }

            JSONObject clInfo = new JSONObject(response.body().string());

            mPlacement.setWfAbt(clInfo.optInt("abt"));
            BaseInstance[] tmp = WaterFallHelper.getArrayInstances(clInfo, mPlacement, mBs);
            if (tmp == null || tmp.length == 0) {
                DeveloperLog.LogD("Ad", "request cl success, but ins[] is empty" + mPlacement);
                callbackAdErrorOnUiThread(ErrorCode.ERROR_NO_FILL);
            } else {
                mTotalIns = tmp;
                doLoadOnUiThread();
            }
        } catch (IOException | JSONException e) {
            CrashUtil.getSingleton().saveException(e);
            callbackAdErrorOnUiThread(ErrorCode.ERROR_NO_FILL);
        } finally {
            IOUtil.closeQuietly(response);
        }
    }

    @Override
    public void onRequestFailed(String error) {//if failed to request cl, calls ad error callback
        callbackAdErrorOnUiThread(ErrorCode.ERROR_NO_FILL);
    }

    /**
     * Sets manual triggered.
     *
     * @param isManualTriggered the is manual triggered
     */
    public void setManualTriggered(boolean isManualTriggered) {
        this.isManualTriggered = isManualTriggered;
    }

    /**
     * Clean after close or failed.
     */
    protected void cleanAfterCloseOrFailed() {
        mTotalIns = null;
        mBs = 0;
        mPt = 0;
        isFo = false;
        if (mCurrentIns != null) {
            mCurrentIns.setObject(null);
            mCurrentIns.setStart(0);
            mCurrentIns = null;
        }
    }

    /**
     * Ad loading error callback
     *
     * @param error error reason
     */
    void callbackAdErrorOnUiThread(final String error) {
        if (isDestroyed) {
            return;
        }
        if (mLoadTs > mCallbackTs) {
            mCallbackTs = System.currentTimeMillis();
        }
//        cleanAfterCloseOrFailed();
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onAdErrorCallback(error);
            }
        });
    }

    /**
     * Ad loading success callback
     */
    void callbackAdReadyOnUiThread() {
        if (isDestroyed) {
            return;
        }
        if (mLoadTs > mCallbackTs) {
            mCallbackTs = System.currentTimeMillis();
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onAdReadyCallback();
            }
        });
    }

    /**
     * Ad click callback
     */
    void callbackAdClickOnUiThread() {
        if (isDestroyed) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_CLICK,
                        PlacementUtils.placementEventParams(mPlacementId));
                onAdClickCallback();
            }
        });
    }

    /**
     * All-is-ready reporting
     */
    void aReadyReport() {
        if (isDestroyed) {
            return;
        }
        if (isManualTriggered) {
            LrReportHelper.report(mPlacementId, OmManager.LOAD_TYPE.MANUAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.WATERFALL_READY);
        } else {
            LrReportHelper.report(mPlacementId, OmManager.LOAD_TYPE.INTERVAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.WATERFALL_READY);
        }
    }

    /**
     * Instance-level load reporting
     *
     * @param instances the instances
     */
    protected void iLoadReport(BaseInstance instances) {
        if (isDestroyed) {
            return;
        }
        if (isManualTriggered) {
            LrReportHelper.report(instances, OmManager.LOAD_TYPE.MANUAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.INSTANCE_LOAD);
        } else {
            LrReportHelper.report(instances, OmManager.LOAD_TYPE.INTERVAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.INSTANCE_LOAD);
        }
    }

    /**
     * Instance-level ready reporting
     *
     * @param instances the instances
     */
    void iReadyReport(BaseInstance instances) {
        if (isDestroyed) {
            return;
        }
        if (isManualTriggered) {
            LrReportHelper.report(instances, OmManager.LOAD_TYPE.MANUAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.INSTANCE_READY);
        } else {
            LrReportHelper.report(instances, OmManager.LOAD_TYPE.INTERVAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.INSTANCE_READY);
        }
    }

    /**
     * Instance-level impression reporting
     *
     * @param instances the instances
     */
    protected void insImpReport(BaseInstance instances) {
        try {
            if (isDestroyed) {
                return;
            }
            mCurrentIns.onInsShow(null);
            if (isManualTriggered) {
                LrReportHelper.report(instances, OmManager.LOAD_TYPE.MANUAL.getValue(), mPlacement.getWfAbt(),
                        CommonConstants.INSTANCE_IMPR);
            } else {
                LrReportHelper.report(instances, OmManager.LOAD_TYPE.INTERVAL.getValue(), mPlacement.getWfAbt(),
                        CommonConstants.INSTANCE_IMPR);
            }
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * Instance-level click reporting
     *
     * @param instances the instances
     */
    void insClickReport(BaseInstance instances) {
        if (isDestroyed) {
            return;
        }
        instances.onInsClick(null);
        if (isManualTriggered) {
            LrReportHelper.report(instances, OmManager.LOAD_TYPE.MANUAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.INSTANCE_CLICK);
        } else {
            LrReportHelper.report(instances, OmManager.LOAD_TYPE.INTERVAL.getValue(), mPlacement.getWfAbt(),
                    CommonConstants.INSTANCE_CLICK);
        }
    }

    /**
     * Checks if callback has been trigged
     *
     * @return the boolean
     */
    boolean hasCallbackToUser() {
        return mLoadTs <= mCallbackTs;
    }

    /**
     * Check act ref boolean.
     *
     * @return the boolean
     */
    protected boolean checkActRef() {
        if (mActRef == null || mActRef.get() == null || !DeviceUtil.isActivityAvailable(mActRef.get())) {
            Activity activity = ActLifecycle.getInstance().getActivity();
            if (activity == null) {
                return false;
            }
            mActRef = new WeakReference<>(activity);
        }
        return true;
    }

    /**
     * Loads ads on UI thread
     */
    private void doLoadOnUiThread() {
        if (isDestroyed) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetFields();
                dispatchAdRequest();
            }
        });
    }

    private void delayLoad(OmManager.LOAD_TYPE type) {
        try {
            //returns if load can't start
            Error error = checkLoadAvailable();
            if (error != null) {
                AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
                return;
            }

            //load start timestamp
            mLoadTs = System.currentTimeMillis();
            mLoadType = type;
            mBs = mPlacement.getBs();
            mPt = mPlacement.getPt();
            isFo = mPlacement.getFo() == 1;
            if (mBs == 0) {
                mBs = 3;
            }
            mBidResponses.clear();
            if (mPlacement.hasHb()) {
                HbHelper.executeHb(mPlacement, type, this);
            } else {
                WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, null, this);
            }
        } catch (Exception e) {
            callbackAdErrorOnUiThread(e.getMessage());
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * Various checks before load can start
     */
    private Error checkLoadAvailable() {
        //empty placementId?
        if (TextUtils.isEmpty(mPlacementId)) {
            callbackAdErrorOnUiThread(ErrorCode.ERROR_PLACEMENT_ID);
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
        }
        //activity effective?
        if (!checkActRef()) {
            callbackAdErrorOnUiThread(ErrorCode.ERROR_ACTIVITY);
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_ACTIVITY);
        }

        //network available?
        if (!NetworkChecker.isAvailable(mActRef.get())) {
            callbackAdErrorOnUiThread(ErrorCode.ERROR_NETWORK_NOT_AVAILABLE);
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_NETWORK_ERROR
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }

        if (isDestroyed) {
            callbackAdErrorOnUiThread(ErrorCode.ERROR_LOAD_AD_BUT_DESTROYED);
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }
        if (mLoadTs > mCallbackTs) {//loading in progress
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }

        if (mPlacement == null) {
            mPlacement = PlacementUtils.getPlacement(mPlacementId);
            if (mPlacement == null) {
                callbackAdErrorOnUiThread(ErrorCode.ERROR_PLACEMENT_EMPTY);
                return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                        , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            }
            if (mPlacement.getT() != getAdType()) {
                callbackAdErrorOnUiThread(ErrorCode.ERROR_PLACEMENT_TYPE);
                return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                        , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            }
        }

        if (AdRateUtil.shouldBlockPlacement(mPlacement) || AdRateUtil.isPlacementCapped(mPlacement)) {
            callbackAdErrorOnUiThread(ErrorCode.ERROR_NO_FILL);
            return ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
        }
        return null;
    }

    private void resetFields() {
        if (mCurrentIns != null) {
            mCurrentIns = null;
        }
        isDestroyed = false;
    }
}
