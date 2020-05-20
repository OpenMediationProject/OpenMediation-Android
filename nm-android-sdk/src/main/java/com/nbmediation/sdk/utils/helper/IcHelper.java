// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.helper;

import android.text.TextUtils;

import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.cache.DataCache;
import com.nbmediation.sdk.utils.constant.KeyConstants;
import com.nbmediation.sdk.utils.crash.CrashUtil;
import com.nbmediation.sdk.utils.model.Configurations;
import com.nbmediation.sdk.utils.request.HeaderUtils;
import com.nbmediation.sdk.utils.request.RequestBuilder;
import com.nbmediation.sdk.utils.request.network.AdRequest;
import com.nbmediation.sdk.utils.request.network.ByteRequestBody;
import com.nbmediation.sdk.utils.request.network.Headers;

/**
 * The type Ic helper.
 */
public class IcHelper {
    /**
     * reporting given extId; called upon mediation Video ads Finish
     *
     * @param placementId the placement id
     * @param mediationId the mediation id
     * @param insId       the ins id
     * @param scene       the scene
     * @param extId       the ext id
     */
    public static void icReport(String placementId, int mediationId, int insId, int scene, String extId) {
        try {

            Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
            if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getIc())) {
                return;
            }
            String vpcUrl = RequestBuilder.buildIcUrl(config.getApi().getIc());

            if (TextUtils.isEmpty(vpcUrl)) {
                return;
            }

            Headers headers = HeaderUtils.getBaseHeaders();
            AdRequest.post()
                    .url(vpcUrl)
                    .headers(headers)
                    .body(new ByteRequestBody(RequestBuilder.buildIcRequestBody(Integer.valueOf(placementId),
                            mediationId, insId, scene, extId)))
                    .connectTimeout(30000)
                    .readTimeout(60000)
                    .instanceFollowRedirects(true)
                    .performRequest(AdtUtil.getApplication());
        } catch (Exception e) {
            DeveloperLog.LogE("icReport error ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }
}
