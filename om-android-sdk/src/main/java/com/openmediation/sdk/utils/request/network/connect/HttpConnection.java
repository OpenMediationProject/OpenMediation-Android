// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network.connect;

import android.os.Build;

import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.request.network.Request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static com.openmediation.sdk.utils.request.network.Headers.KEY_CONNECTION;

public class HttpConnection extends AbstractUrlConnection {
    private HttpURLConnection mConnection;

    @Override
    public void cancel() throws Exception {
        if (mConnection != null) {
            IOUtil.closeQuietly(mConnection.getInputStream());
            mConnection.disconnect();
        }
    }

    @Override
    int getResponseCode() throws IOException {
        return mConnection.getResponseCode();
    }

    @Override
    public URLConnection connect(Request request) throws Exception {
        String u = request.getUrl();
        DeveloperLog.LogD("HttpConnection", "url is : " + u);
        URL url = new URL(u);
        mConnection = (HttpURLConnection) url.openConnection();
        mConnection.setConnectTimeout(request.getConnectTimeout());
        mConnection.setReadTimeout(request.getReadTimeout());
        mConnection.setInstanceFollowRedirects(request.isInstanceFollowRedirects());

        Request.Method method = request.getRequestMethod();
        mConnection.setRequestMethod(method.toString());
        mConnection.setDoInput(true);
        boolean isAllowBody = isAllowBody(method);
        mConnection.setDoOutput(isAllowBody);

        Headers headers = request.getHeaders();

        if (headers != null) {
            List<String> values = headers.get(KEY_CONNECTION);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && values != null && !values.isEmpty()) {
                headers.set(KEY_CONNECTION, values.get(0));
            }
            Map<String, String> requestHeaders = Headers.getRequestHeaders(headers);
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                mConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        mConnection.connect();
        return mConnection;
    }


}
