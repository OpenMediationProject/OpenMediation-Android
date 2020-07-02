package com.cloudtech.shell.http;

import com.cloudtech.shell.SdkShell;

import java.net.HttpURLConnection;

/**
 * Created by Vincent
 * Email:jingwei.zhang@yeahmobi.com
 */
public class HttpRunnable implements Runnable {

    private static final String TAG = "HttpRunnable";

    private String urlStr;

    private HttpRequester.Listener listener;


    public HttpRunnable(String urlStr, HttpRequester.Listener listener) {
        this.urlStr = urlStr;
        this.listener = listener;
    }


    @Override
    public void run() {
        try {
            HttpURLConnection connection = HttpUtils.handleConnection(urlStr, 15000);
            final byte[] bytes = HttpUtils.handleSuccess(connection);
            SdkShell.handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(bytes,urlStr);
                }
            });
        } catch (final Exception e) {
//            YeLog.e(e);
            SdkShell.handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(e.getMessage(),urlStr);
                }
            });
        }
    }


}
