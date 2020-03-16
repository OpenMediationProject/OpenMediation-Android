// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.helper;

import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.BaseInstance;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.request.HeaderUtils;
import com.openmediation.sdk.utils.request.RequestBuilder;
import com.openmediation.sdk.utils.request.network.AdRequest;
import com.openmediation.sdk.utils.request.network.ByteRequestBody;
import com.openmediation.sdk.utils.request.network.Headers;

/**
 *
 */
public final class LrReportHelper {


    public static void report(String placementId, int loadType, int abt, int reportType) {
        report(placementId, -1, loadType, -1, -1, abt, reportType);
    }

    public static void report(BaseInstance instance, int loadType, int abt, int reportType) {
        report(instance, -1, loadType, abt, reportType);
    }

    public static void report(BaseInstance instance, int sceneId, int loadType, int abt, int reportType) {
        if (instance == null) {
            return;
        }
        report(instance.getPlacementId(), sceneId, loadType, instance.getId(), instance.getMediationId(), abt, reportType);
    }

    private static void report(String placementId, int sceneId, int loadType,
                               int instanceId, int mediationId, int abt, int reportType) {
        try {
            Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
            if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getLr())) {
                return;
            }
            String lrUrl = RequestBuilder.buildLrUrl(config.getApi().getLr());

            if (TextUtils.isEmpty(lrUrl)) {
                return;
            }

            Headers headers = HeaderUtils.getBaseHeaders();
            AdRequest.post()
                    .url(lrUrl)
                    .headers(headers)
                    .body(new ByteRequestBody(RequestBuilder.buildLrRequestBody(Integer.parseInt(placementId),
                            sceneId,
                            loadType,
                            mediationId,
                            instanceId,
                            abt,
                            reportType)
                    ))
                    .connectTimeout(30000)
                    .readTimeout(60000)
                    .instanceFollowRedirects(true)
                    .performRequest(AdtUtil.getApplication());
        } catch (Exception e) {
            DeveloperLog.LogE("httpLr error ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }
}
