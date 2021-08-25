// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.core;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.bid.BidAuctionManager;
import com.openmediation.sdk.core.runnable.InitScheduleTask;
import com.openmediation.sdk.utils.AFManager;
import com.openmediation.sdk.utils.lifecycle.ActLifecycle;
import com.openmediation.sdk.utils.AdLog;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.JsonUtil;
import com.openmediation.sdk.utils.OaidHelper;
import com.openmediation.sdk.utils.SdkUtil;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.device.AdvertisingIdClient;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.error.ErrorBuilder;
import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.event.EventId;
import com.openmediation.sdk.utils.event.EventUploadManager;
import com.openmediation.sdk.utils.helper.ConfigurationHelper;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.request.network.Request;
import com.openmediation.sdk.utils.request.network.Response;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Init imp.
 */
public final class InitImp {
    private static AtomicBoolean hasInit = new AtomicBoolean(false);
    private static AtomicBoolean isInitRunning = new AtomicBoolean(false);
    private static InitCallback mCallback;
    private static long sInitStart;
    // Re Init Delay
    private static AtomicBoolean reInitRunning = new AtomicBoolean(false);

    /**
     * init method
     *
     * @param callback the callback
     * @param channel  the channel
     */
    public static void init(Activity activity, InitConfiguration configuration, final InitCallback callback) {
        //
//        if (hasInit.get()) {
//            return;
//        }

        if (isInitRunning.get()) {
            return;
        }


        isInitRunning.set(true);
        sInitStart = System.currentTimeMillis();
        mCallback = callback;
//        SensorManager.getSingleton();
        DeveloperLog.enableDebug(AdtUtil.getInstance().getApplicationContext(), false);
        CrashUtil.getSingleton().init();
        ActLifecycle.getInstance().init();
        if (activity != null) {
            ActLifecycle.getInstance().setActivity(activity);
        }
        EventUploadManager.getInstance().init(AdtUtil.getInstance().getApplicationContext());
        EventUploadManager.getInstance().uploadEvent(reInitRunning.get() ? EventId.RE_INIT_START : EventId.INIT_START);
        WorkExecutor.execute(new InitAsyncRunnable(configuration));
    }

    /**
     * Re init sdk.
     *
     * @param callback the callback
     */
    static void reInitSDK(final InitCallback callback) {
        if (DataCache.getInstance().containsKey(KeyConstants.KEY_APP_KEY)) {
            String appKey = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_KEY, String.class);
            String appChannel = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_CHANNEL, String.class);
            String initHost = DataCache.getInstance().getFromMem(KeyConstants.KEY_INIT_HOST, String.class);
            InitConfiguration configuration = new InitConfiguration.Builder().appKey(appKey)
                    .channel(appChannel).initHost(initHost).build();
            init(ActLifecycle.getInstance().getActivity(), configuration, new InitCallback() {
                @Override
                public void onSuccess() {
                    DeveloperLog.LogD("reInitSDK success");
                    callback.onSuccess();
                }

                @Override
                public void onError(Error error) {
                    callback.onError(error);
                }
            });
        } else {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_NOT_INIT, ErrorCode.CODE_INTERNAL_REQUEST_APPKEY);
            callback.onError(error);
        }
    }

    /**
     * Re init sdk at delay time.
     *
     * @param callback the callback
     */
    public static void startReInitSDK(final InitCallback callback) {
        reInitRunning.set(true);
        reInitSDK(callback);
    }

    /**
     * init success?
     *
     * @return the boolean
     */
    public static boolean isInit() {
        return hasInit.get();
    }

    /**
     * Is init running boolean.
     *
     * @return the boolean
     */
    static boolean isInitRunning() {
        return isInitRunning.get();
    }

    private static void requestConfig(InitConfiguration configuration) {
        try {
            DeveloperLog.LogD("Om init request config");
            //requests Config
            ConfigurationHelper.getConfiguration(configuration.getAppKey(), configuration.getInitHost(),
                    new InitRequestCallback(configuration.getAppKey()));
        } catch(Exception e) {
            DeveloperLog.LogD("requestConfig  exception : ", e);
            CrashUtil.getSingleton().saveException(e);
            Error error = new Error(ErrorCode.CODE_INIT_UNKNOWN_INTERNAL_ERROR
                    , ErrorCode.MSG_INIT_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            DeveloperLog.LogE(error.toString() + ", requestConfig");
            callbackInitErrorOnUIThread(error);
        }
    }

    /**
     * Inits global utils
     */
    private static void initUtil() {
        DataCache.getInstance().init(AdtUtil.getInstance().getApplicationContext());
    }

    private static void doAfterGetConfig(String appKey, Configurations config) {
        try {
            DeveloperLog.enableDebug(AdtUtil.getInstance().getApplicationContext(), config.getD() == 1);
            AFManager.checkAfDataStatus();
            EventUploadManager.getInstance().updateReportSettings(config);
            //reports error logs
            CrashUtil.getSingleton().uploadException(config, appKey);

            InitScheduleTask.startTask(config);
        } catch(Exception e) {
            DeveloperLog.LogD("doAfterGetConfig  exception : ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private static void callbackInitErrorOnUIThread(final Error result) {
        AdLog.getSingleton().LogE("Init Failed: " + result);
        HandlerUtil.runOnUiThread(new InitFailRunnable(result));
    }

    private static void callbackInitSuccessOnUIThread() {
        AdLog.getSingleton().LogD("Init Success");
        HandlerUtil.runOnUiThread(new InitSuccessRunnable());
    }

    private static void initCompleteReport(int eventId, Error error) {
        JSONObject jsonObject = new JSONObject();
        if (error != null) {
            JsonUtil.put(jsonObject, "msg", error);
        }
        if (sInitStart != 0) {
            int dur = (int) (System.currentTimeMillis() - sInitStart) / 1000;
            JsonUtil.put(jsonObject, "duration", dur);
        }
        EventUploadManager.getInstance().uploadEvent(eventId, jsonObject);
    }

    private static class InitSuccessRunnable implements Runnable {

        @Override
        public void run() {
            DeveloperLog.LogD("Om init Success ");
            hasInit.set(true);
            isInitRunning.set(false);
            if (mCallback != null) {
                mCallback.onSuccess();
            }
            initCompleteReport(reInitRunning.get() ? EventId.RE_INIT_COMPLETE : EventId.INIT_COMPLETE, null);
            reInitRunning.set(false);
        }
    }

    private static class InitAsyncRunnable implements Runnable {
        private InitConfiguration initConfiguration;

        private InitAsyncRunnable(InitConfiguration configuration) {
            this.initConfiguration = configuration;
        }

        @Override
        public void run() {
            try {
                Context context = AdtUtil.getInstance().getApplicationContext();
                Error error = SdkUtil.banRun(context, initConfiguration.getAppKey());
                if (error != null) {
                    callbackInitErrorOnUIThread(error);
                    return;
                }
                initUtil();
                DataCache.getInstance().setMEM(KeyConstants.KEY_APP_KEY, initConfiguration.getAppKey());
                DataCache.getInstance().setMEM(KeyConstants.KEY_AF_ID, AFManager.getAfId(context));
                String appChannel = initConfiguration.getChannel();
                if (TextUtils.isEmpty(appChannel)) {
                    appChannel = "";
                }
                DataCache.getInstance().setMEM(KeyConstants.KEY_APP_CHANNEL, appChannel);
                if (!TextUtils.isEmpty(initConfiguration.getInitHost())) {
                    DataCache.getInstance().setMEM(KeyConstants.KEY_INIT_HOST, initConfiguration.getInitHost());
                }
                AdvertisingIdClient.getGaid(AdtUtil.getInstance().getApplicationContext(), new AdvertisingIdClient.OnGetGaidListener() {
                    @Override
                    public void onGetGaid(String gaid) {
                        if (!TextUtils.isEmpty(gaid)) {
                            DataCache.getInstance().set(KeyConstants.RequestBody.KEY_GAID, gaid);
                            requestConfig(initConfiguration);
                        } else {
                            OaidHelper.getOaid(AdtUtil.getInstance().getApplicationContext(), new OaidHelper.oaidListener() {
                                @Override
                                public void onGetOaid(String oaid) {
                                    requestConfig(initConfiguration);
                                }
                            });
                        }
                    }
                });
            } catch(Exception e) {
                DeveloperLog.LogD("initOnAsyncThread  exception : ", e);
                CrashUtil.getSingleton().saveException(e);
                Error error = new Error(ErrorCode.CODE_INIT_UNKNOWN_INTERNAL_ERROR
                        , ErrorCode.MSG_INIT_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
                DeveloperLog.LogE(error.toString() + ", initOnAsyncThread");
                callbackInitErrorOnUIThread(error);
            }
        }
    }

    private static class InitFailRunnable implements Runnable {
        private Error mError;

        /**
         * Instantiates a new Init fail runnable.
         *
         * @param result the result
         */
        InitFailRunnable(Error result) {
            mError = result;
        }

        @Override
        public void run() {
            DeveloperLog.LogD("Om init error  " + mError);
            isInitRunning.set(false);
            if (reInitRunning.get()) {
                initCompleteReport(EventId.RE_INIT_FAILED, mError);
            } else {
                hasInit.set(false);
                initCompleteReport(EventId.INIT_FAILED, mError);
            }
            if (mCallback != null) {
                mCallback.onError(mError);
            }
            reInitRunning.set(false);
        }
    }

    private static class InitRequestCallback implements Request.OnRequestCallback {

        private String appKey;

        /**
         * Instantiates a new Init request callback.
         *
         * @param appKey the app key
         */
        InitRequestCallback(String appKey) {
            this.appKey = appKey;
        }

        @Override
        public void onRequestSuccess(Response response) {
            try {
                int code = response.code();
                if (code != HttpURLConnection.HTTP_OK) {
                    Error error = new Error(ErrorCode.CODE_INIT_SERVER_ERROR
                            , ErrorCode.MSG_INIT_SERVER_ERROR + code, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString() + "Om init request config response code not 200 : " + response.code());
                    callbackInitErrorOnUIThread(error);
                    return;
                }

                String requestData = new String(ConfigurationHelper.checkResponse(response), Charset.forName(CommonConstants.CHARTSET_UTF8));
                if (TextUtils.isEmpty(requestData)) {
                    Error error = new Error(ErrorCode.CODE_INIT_RESPONSE_CHECK_ERROR
                            , ErrorCode.MSG_INIT_RESPONSE_CHECK_ERROR, ErrorCode.CODE_INTERNAL_RESPONSE_CHECK_ERROR);
                    DeveloperLog.LogE(error.toString() + ", Om init response data is null: " + requestData);
                    callbackInitErrorOnUIThread(error);
                    return;
                }
                //adds global data to memory
                Configurations config = ConfigurationHelper.parseFormServerResponse(requestData);
                if (config != null) {
                    DeveloperLog.LogD("Om init request config success");
                    DataCache.getInstance().setMEM(KeyConstants.KEY_CONFIGURATION, config);
                    try {
                        BidAuctionManager.getInstance().initBid(AdtUtil.getInstance().getApplicationContext(), config);
                    } catch(Exception e) {
                        DeveloperLog.LogD("initBid  exception : ", e);
                        CrashUtil.getSingleton().saveException(e);
                    }
                    callbackInitSuccessOnUIThread();
                    doAfterGetConfig(appKey, config);
                } else {
                    Error error = new Error(ErrorCode.CODE_INIT_RESPONSE_PARSE_ERROR
                            , ErrorCode.MSG_INIT_RESPONSE_PARSE_ERROR, ErrorCode.CODE_INTERNAL_INIT_RESPONSE_PARSE_ERROR);
                    DeveloperLog.LogE(error.toString() + ", Om init format config is null");
                    callbackInitErrorOnUIThread(error);
                }
            } catch (Exception e) {
                CrashUtil.getSingleton().saveException(e);
                Error error = new Error(ErrorCode.CODE_INIT_EXCEPTION
                        , ErrorCode.MSG_INIT_EXCEPTION + e.getMessage(), ErrorCode.CODE_INTERNAL_INIT_EXCEPTION);
                DeveloperLog.LogE(error.toString() + ", request config exception:" + e);
                callbackInitErrorOnUIThread(error);
            } finally {
                IOUtil.closeQuietly(response);
            }
        }

        @Override
        public void onRequestFailed(String error) {
            Error result = new Error(ErrorCode.CODE_INIT_REQUEST_ERROR
                    , ErrorCode.MSG_INIT_REQUEST_ERROR + error, ErrorCode.CODE_INTERNAL_INIT_REQUEST_ERROR);
            DeveloperLog.LogE("request config failed : " + result + ", error:" + error);
            AdLog.getSingleton().LogE("Init Failed: " + error);
            callbackInitErrorOnUIThread(result);
        }
    }
}
