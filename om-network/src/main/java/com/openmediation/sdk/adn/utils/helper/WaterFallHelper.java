// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.adn.utils.helper;

import android.text.TextUtils;

import com.openmediation.sdk.adn.utils.PUtils;
import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.Gzip;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.device.DeviceUtil;
import com.openmediation.sdk.utils.helper.IapHelper;
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


/**
 * The type Water fall helper.
 */
public class WaterFallHelper {

    /**
     * Wf request.
     *
     * @param info     the info
     * @param callback the callback
     * @throws Exception the exception
     */
    public static void wfRequest(PlacementInfo info, int loadType,
                                 Request.OnRequestCallback callback) throws Exception {

        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getCpcl())) {
            callback.onRequestFailed("empty Url");
            return;
        }
        String url = buildCPCLUrl(config.getApi().getCpcl());
        byte[] bytes = buildClRequestBody(info.getId(),
                String.valueOf(info.getWidth()),
                String.valueOf(info.getHeight()),
                String.valueOf(PUtils.getPlacementImprCount(info.getId())),
                String.valueOf(IapHelper.getIap()),
                String.valueOf(loadType));

        ByteRequestBody requestBody = new ByteRequestBody(bytes);
        Headers headers = HeaderUtils.getBaseHeaders();
        AdRequest.post().url(url).body(requestBody).headers(headers).connectTimeout(30000).readTimeout(60000)
                .callback(callback).performRequest(AdtUtil.getApplication());
    }

    /**
     * Build cp/cl url string.
     *
     * @param host the host
     * @return the string
     */
    private static String buildCPCLUrl(String host) {
        String request = new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .format();
        return host + "?" + request;
    }

    /**
     * Build wf request body byte [ ].
     *
     * @param extras the extras
     * @return the byte [ ]
     * @throws Exception the exception
     */
    private static byte[] buildClRequestBody(String... extras) throws Exception {
        JSONObject body = RequestBuilder.getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_PID, extras[0]);
        if (!"0".equals(extras[1])) {
            body.put(KeyConstants.RequestBody.KEY_W, extras[1]);
        }
        if (!"0".equals(extras[2])) {
            body.put(KeyConstants.RequestBody.KEY_H, extras[2]);
        }
        body.put(KeyConstants.RequestBody.KEY_IMPRTIMES, Integer.valueOf(extras[3]));
        body.put(KeyConstants.RequestBody.KEY_IAP, Float.valueOf(extras[4]));
        body.put(KeyConstants.RequestBody.KEY_NG, DeviceUtil.isGpInstall(AdtUtil.getApplication()));
        body.put(KeyConstants.RequestBody.KEY_ACT, extras[5]);

        DeveloperLog.LogD("request wf params : " + body.toString());
        return Gzip.inGZip(body.toString().getBytes(Charset.forName("UTF-8")));
    }
}
