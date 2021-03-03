package com.openmediation.sdk.utils.helper;

import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.request.HeaderUtils;
import com.openmediation.sdk.utils.request.RequestBuilder;
import com.openmediation.sdk.utils.request.network.AdRequest;
import com.openmediation.sdk.utils.request.network.ByteRequestBody;
import com.openmediation.sdk.utils.request.network.Headers;

public class AfHelper {

    public static void sendAfRequest(final int type, final Object data) {
        if (data == null) {
            return;
        }
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
                    if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getCd())) {
                        return;
                    }
                    String url = RequestBuilder.buildCdUrl(config.getApi().getCd());

                    Headers headers = HeaderUtils.getBaseHeaders();

                    byte[] bytes = RequestBuilder.buildCdRequestBody(type, data);

                    ByteRequestBody requestBody = new ByteRequestBody(bytes);
                    AdRequest.post()
                            .url(url)
                            .headers(headers)
                            .body(requestBody)
                            .connectTimeout(30000)
                            .readTimeout(60000)
                            .performRequest(AdtUtil.getApplication());
                } catch (Exception e) {
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }
}
