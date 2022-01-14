// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3
package com.openmediation.sdk.core;

import android.text.TextUtils;

import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.constant.KeyConstants;
import com.openmediation.sdk.utils.model.Configurations;
import com.openmediation.sdk.utils.request.RequestBuilder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Cache for init and waterfall config
 */
public class OmCacheManager {
    private static final String TAG = "OmCacheManager: ";
    private static final String UTF = "UTF-8";

    public static OmCacheManager getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final OmCacheManager INSTANCE = new OmCacheManager();
    }

    /**
     * save init data
     */
    public void saveInitData(InitConfiguration configuration, String responseData) {
        try {
            boolean needCache = needCacheInitData();
            if (!needCache) {
                return;
            }
            String value = URLEncoder.encode(responseData, UTF);
            String key = getInitKey(configuration);
            DataCache.getInstance().set(key, value);
        } catch (Throwable e) {
            DeveloperLog.LogE("save init cache error: " + e.getMessage());
        }
    }

    /**
     * read local data
     *
     * @return local init data
     */
    public String getInitData(InitConfiguration configuration) {
        try {
            boolean needCache = needCacheInitData();
            if (!needCache) {
                return "";
            }
            String key = getInitKey(configuration);
            String value = DataCache.getInstance().get(key, String.class);
            if (TextUtils.isEmpty(value)) {
                return "";
            }
            return URLDecoder.decode(value, UTF);
        } catch (Throwable e) {
            DeveloperLog.LogW(TAG + "get init cache error: " + e.getMessage());
        }
        return "";
    }

    /**
     * save waterfall data
     */
    public void saveWaterfallData(String pid, int type, String responseData) {
        try {
            boolean needCacheWfData = needCacheWfData(type);
            if (!needCacheWfData) {
                return;
            }
            DeveloperLog.LogD(TAG + "save wf data to local");
            String key = getWaterfallKey() + "_" + pid;
            String value = URLEncoder.encode(responseData, UTF);
            DataCache.getInstance().set(key, value);
        } catch (Throwable e) {
            DeveloperLog.LogE("save waterfall cache error: " + e.getMessage());
        }
    }

    public String getWaterfallData(String pid, int type) {
        try {
            boolean needCacheWfData = needCacheWfData(type);
            if (!needCacheWfData) {
                return "";
            }
            String key = getWaterfallKey() + "_" + pid;
            String value = DataCache.getInstance().get(key, String.class);
            if (TextUtils.isEmpty(value)) {
                return "";
            }
            return URLDecoder.decode(value, UTF);
        } catch (Throwable e) {
            DeveloperLog.LogE(TAG + "get waterfall cache error: " + e.getMessage());
        }
        return "";
    }

    public boolean needCacheWfData(int type) {
        List list = DataCache.getInstance().getFromMem(KeyConstants.KEY_CACHE_AD_TYPE, List.class);
        if (list == null || list.isEmpty()) {
            return false;
        }
        List<OmAds.CACHE_TYPE> typeList = list;
        for (OmAds.CACHE_TYPE adType : typeList) {
            if (adType != null && adType.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public boolean needCacheInitData() {
        List list = DataCache.getInstance().getFromMem(KeyConstants.KEY_CACHE_AD_TYPE, List.class);
        if (list == null || list.isEmpty()) {
            return false;
        }
        return true;
    }

    private String getInitKey(InitConfiguration configuration) throws Exception {
        String host = "";
        if (TextUtils.isEmpty(configuration.getInitHost())) {
            host = CommonConstants.INIT_URL;
        }
        String initUrl = RequestBuilder.buildInitUrl(host, configuration.getAppKey());
        String encode = URLEncoder.encode(initUrl, UTF);
        return KeyConstants.KEY_CACHE_INIT_CONFIG + "_" + encode;
    }

    private String getWaterfallKey() throws Exception {
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null || config.getApi() == null || TextUtils.isEmpty(config.getApi().getWf())) {
            return KeyConstants.KEY_CACHE_INIT_CONFIG;
        }
        String url = RequestBuilder.buildWfUrl(config.getApi().getWf());
        String encode = URLEncoder.encode(url, UTF);
        return KeyConstants.KEY_CACHE_WATERFALL_CONFIG + "_" + encode;
    }
}
