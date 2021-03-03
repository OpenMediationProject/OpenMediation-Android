// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.helper;

import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.WorkExecutor;
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

    public static void report(String reqId, int ruleId, String placementId, int loadType, int abt, int reportType, int bid) {
        report(reqId, ruleId, null, placementId, -1, loadType, -1, -1, abt, reportType, bid);
    }

    public static void report(BaseInstance instance, int loadType, int abt, int reportType, int bid) {
        report(instance, -1, loadType, abt, reportType, bid);
    }

    public static void report(BaseInstance instance, int sceneId, int loadType, int abt, int reportType, int bid) {
        if (instance == null) {
            return;
        }
        report("", -1, instance, instance.getPlacementId(), sceneId, loadType, instance.getId(), instance.getMediationId(), abt, reportType, bid);
    }

    private static void report(final String reqId, final int ruleId, final BaseInstance instance, final String placementId, final int sceneId, final int loadType,
                               final int instanceId, final int mediationId, final int abt, final int reportType, final int bid) {
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
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
                            .body(new ByteRequestBody(RequestBuilder.buildLrRequestBody(
                                    reqId,
                                    ruleId,
                                    instance,
                                    Integer.parseInt(placementId),
                                    sceneId,
                                    loadType,
                                    mediationId,
                                    instanceId,
                                    abt,
                                    reportType,
                                    bid)
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
        });
    }
}
