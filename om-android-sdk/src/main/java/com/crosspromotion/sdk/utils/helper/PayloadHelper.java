package com.crosspromotion.sdk.utils.helper;

import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.Gzip;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.model.PlacementInfo;
import com.openmediation.sdk.utils.request.HeaderUtils;
import com.openmediation.sdk.utils.request.RequestBuilder;
import com.openmediation.sdk.utils.request.network.AdRequest;
import com.openmediation.sdk.utils.request.network.ByteRequestBody;
import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.request.network.Request;

import org.json.JSONObject;

import java.nio.charset.Charset;

public class PayloadHelper {

    public static void payloadRequest(final PlacementInfo info, final String payload, final Request.OnRequestCallback callback) throws Exception {
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
                    if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getCppl())) {
                        callback.onRequestFailed("empty Url");
                        return;
                    }
                    String url = buildCPPlUrl(config.getApi().getCppl());
                    byte[] bytes = buildPlRequestBody(info, payload);

                    ByteRequestBody requestBody = new ByteRequestBody(bytes);
                    Headers headers = HeaderUtils.getBaseHeaders();
                    AdRequest.post().url(url).body(requestBody).headers(headers).connectTimeout(30000).readTimeout(60000)
                            .callback(callback).performRequest(AdtUtil.getApplication());
                } catch (Exception e) {
                    DeveloperLog.LogE("CrossPromotion SDK Payload Error: " + e.getMessage());
                    CrashUtil.getSingleton().saveException(e);
                    callback.onRequestFailed("Load failed: unknown exception occurred");
                }
            }
        });
    }

    /**
     * Build pl url string.
     *
     * @param host the host
     * @return the string
     */
    private static String buildCPPlUrl(String host) {
        String request = new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .format();
        return host + "?" + request;
    }

    private static byte[] buildPlRequestBody(PlacementInfo info, String token) throws Exception {
        JSONObject body = RequestBuilder.getRequestBodyBaseJson();
        body.put("pid", info.getId());
        body.put("token", token);
        if (info.getWidth() != 0) {
            body.put(KeyConstants.RequestBody.KEY_W, info.getWidth());
        }
        if (info.getHeight() != 0) {
            body.put(KeyConstants.RequestBody.KEY_H, info.getHeight());
        }
        return Gzip.inGZip(body.toString().getBytes(Charset.forName("UTF-8")));
    }
}
