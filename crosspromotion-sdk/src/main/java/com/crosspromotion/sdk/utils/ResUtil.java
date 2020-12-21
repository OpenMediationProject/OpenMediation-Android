// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;
import com.openmediation.sdk.utils.crash.CrashUtil;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ResUtil {

    public static boolean loadRes(List<String> resources, List<String> necessaryRes) throws Exception {
        if (resources == null || resources.isEmpty()) {
            return false;
        }

        DeveloperLog.LogD("resSet:" + resources.toString());
        return ResDownloader.downloadFile(resources, necessaryRes);
    }

    public static WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        try {
            DeveloperLog.LogD(url);
            int i = url.indexOf('#');
            if (i != -1) url = url.substring(0, i);

            //Check redirect location
            File headerFile = Cache.getCacheFile(view.getContext(), url, CommonConstants.FILE_HEADER_SUFFIX);
            if (headerFile == null || !headerFile.exists()) {
                return null;
            } else {
                String location = Cache.getValueFromFile(headerFile, CommonConstants.KEY_LOCATION);
                if (!TextUtils.isEmpty(location)) {
                    return null;
                }
            }

            if (Cache.existCache(view.getContext(), url)) {
                DeveloperLog.LogD("exist:" + url);
                try {
//                    File header = Cache.getCacheFile(view.getContext().getApplicationContext(), url, Constants.FILE_HEADER_SUFFIX);
                    String mime_type = Cache.getValueFromFile(headerFile, CommonConstants.KEY_CONTENT_TYPE);

                    if (TextUtils.isEmpty(mime_type)) {//checks the mimeType obtained from the header
                        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                        if (TextUtils.equals("js", extension.toLowerCase())) {
                            mime_type = "application/x-javascript";
                        } else {
                            mime_type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        }
                    }

                    if (!TextUtils.isEmpty(mime_type)) {//re-checks the mimeType in order to not load pure text files
                        InputStream input = IOUtil.getFileInputStream(Cache.getCacheFile(view.getContext().getApplicationContext(),
                                url, null));
                        if (input == null) {
                            return null;
                        }
                        WebResourceResponse response = new WebResourceResponse(mime_type, null, input);
                        Map<String, String> headers = new HashMap<>();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            headers.put("Access-Control-Allow-Origin", "*");
                            response.setResponseHeaders(headers);
                        }
                        return response;
                    }
                } catch (Exception e) {
                    DeveloperLog.LogD("ResUtil", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        } catch (Exception e) {
            DeveloperLog.LogD("ResUtil", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return null;
    }
}
