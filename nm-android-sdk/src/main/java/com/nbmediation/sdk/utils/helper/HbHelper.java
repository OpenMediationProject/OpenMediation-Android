// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.helper;

import android.text.TextUtils;
import android.util.SparseArray;

import com.nbmediation.sdk.core.OmManager;
import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.Preconditions;
import com.nbmediation.sdk.utils.cache.DataCache;
import com.nbmediation.sdk.utils.constant.KeyConstants;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.error.ErrorBuilder;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.Configurations;
import com.nbmediation.sdk.utils.model.Placement;
import com.nbmediation.sdk.utils.request.HeaderUtils;
import com.nbmediation.sdk.utils.request.RequestBuilder;
import com.nbmediation.sdk.utils.request.network.AdRequest;
import com.nbmediation.sdk.utils.request.network.ByteRequestBody;
import com.nbmediation.sdk.utils.request.network.Headers;
import com.nbmediation.sdk.utils.request.network.Request;
import com.nbmediation.sdk.utils.request.network.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class HbHelper {

    public static void executeHb(final Placement placement, OmManager.LOAD_TYPE loadType, final OnHbCallback callback)
            throws Exception {
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getHb())) {
            callback.onHbFailed("empty Url");
            return;
        }
        String url = RequestBuilder.buildHbUrl(config.getApi().getHb());
        byte[] body = RequestBuilder.buildHbRequestBody(placement.getId(), IapHelper.getIap(),
                PlacementUtils.getPlacementImprCount(placement.getId()), loadType.getValue());
        ByteRequestBody requestBody = new ByteRequestBody(body);
        Headers headers = HeaderUtils.getBaseHeaders();
        AdRequest.post().url(url).body(requestBody).headers(headers).connectTimeout(30000).readTimeout(60000)
                .callback(new Request.OnRequestCallback() {
                    @Override
                    public void onRequestSuccess(Response response) {
                        try {
                            if (!Preconditions.checkNotNull(response) || response.code() != HttpURLConnection.HTTP_OK) {
                                Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                                        , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                                callback.onHbFailed(error.toString());
                                return;
                            }
                            JSONObject hbResponse = new JSONObject(response.body().string());
                            JSONArray insArray = hbResponse.optJSONArray("ins");
                            if (insArray == null || insArray.length() <= 0) {
                                callback.onHbFailed("hb result empty");
                                return;
                            }

                            SparseArray<BaseInstance> insMap = placement.getInsMap();
                            if (insMap == null || insMap.size() <= 0) {
                                callback.onHbFailed("hb result empty");
                                return;
                            }

                            List<BaseInstance> instancesList = new ArrayList<>();
                            for (int i = 0; i < insArray.length(); i++) {
                                BaseInstance ins = insMap.get(insArray.optInt(i));
                                if (ins != null) {
                                    instancesList.add(ins);
                                }
                            }
                            //
                            if (instancesList.size() == 0) {
                                callback.onHbFailed("hb result empty");
                                return;
                            }

                            callback.onHbSuccess(hbResponse.optInt("abt"),
                                    instancesList.toArray(new BaseInstance[instancesList.size()]));
                        } catch (Exception e) {
                            callback.onHbFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onRequestFailed(String error) {
                        callback.onHbFailed(error);
                    }
                }).performRequest(AdtUtil.getApplication());
    }

    public interface OnHbCallback {
        void onHbSuccess(int abt, BaseInstance[] instances);

        void onHbFailed(String error);
    }
}
