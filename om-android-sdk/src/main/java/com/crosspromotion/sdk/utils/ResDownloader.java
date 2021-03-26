// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils;

import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.request.HeaderUtils;
import com.openmediation.sdk.utils.request.network.AdRequest;
import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.request.network.Response;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ResDownloader {

    static boolean downloadFile(List<String> urls, List<String> necessaryRes) throws Exception {
        int failSize = 0;
        for (String url : urls) {
            if (!Cache.existCache(AdtUtil.getApplication(), url)) {
                File file = downloadFile(url);
                if (file == null && necessaryRes.contains(url)) {
                    failSize++;
                }
            }
        }
        return failSize == 0;
    }

    public static File downloadFile(String url) throws Exception {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Response response = null;
        try {
            response = AdRequest.get().url(url).connectTimeout(30 * 1000).readTimeout(10 * 60 * 1000)
                    .headers(getCacheHeaders(AdtUtil.getApplication(), url)).syncRequest();

            if (response == null) {
                return null;
            }
            int code = response.code();
            if (code == HttpURLConnection.HTTP_OK) {
                boolean success = Cache.saveFile(AdtUtil.getApplication(), url, response);
                if (success) {
                    return Cache.getCacheFile(AdtUtil.getApplication(), url, null);
                } else {
                    deleteFileWhenError(url);
                    return null;
                }
            } else if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {//文件内容无需更新，但是更新一下header
                Cache.saveHeaderFields(AdtUtil.getApplication(), url, response);
                return Cache.getCacheFile(AdtUtil.getApplication(), url, null);
            } else if (code == 301 || code == 302 || code == 303 || code == 307) {
                Cache.saveHeaderFields(AdtUtil.getApplication(), url, response);
                String redirectUrl = response.headers().getLocation();
                URL u = new URL(new URL(url), redirectUrl);
                DeveloperLog.LogD("ResDownLoader", "redirect url is : " + u.toString());
                return downloadFile(u.toString());
            } else {
                deleteFileWhenError(url);
                return null;
            }
        } finally {
            DeveloperLog.LogD("ResDownLoader", "url is : " + url + " finally close response");
            IOUtil.closeQuietly(response);
        }
    }

    private static void deleteFileWhenError(String url) {
        File content = Cache.getCacheFile(AdtUtil.getApplication(), url, null);
        if (content != null && content.exists()) {
            DeveloperLog.LogD("ResDownLoader", "delete content file when error : " + content.delete());
        }
        File header = Cache.getCacheFile(AdtUtil.getApplication(), url, CommonConstants.FILE_HEADER_SUFFIX);
        if (header != null && header.exists()) {
            DeveloperLog.LogD("ResDownLoader", "delete header file when error : " + header.delete());
        }
    }

    private static Headers getCacheHeaders(Context context, String url) throws Exception {
        Headers headers = HeaderUtils.getBaseHeaders();
        File header = Cache.getCacheFile(context, url, CommonConstants.FILE_HEADER_SUFFIX);
        if (header != null && header.exists()) {
            String eTag = Cache.getValueFromFile(header, CommonConstants.KEY_ETAG);
            if (!TextUtils.isEmpty(eTag)) {
                headers.set(CommonConstants.KEY_IF_NONE_MATCH, eTag);
            } else {
                String lastModified = Cache.getValueFromFile(header, CommonConstants.KEY_LAST_MODIFIED);
                if (!TextUtils.isEmpty(lastModified)) {
                    headers.set(CommonConstants.KEY_IF_MODIFIED_SINCE, lastModified);
                }
            }
        }
        return headers;
    }
}
