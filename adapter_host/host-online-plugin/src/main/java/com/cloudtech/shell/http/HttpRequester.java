package com.cloudtech.shell.http;

import com.cloudtech.shell.utils.ThreadPoolProxy;

/**
 * Created by Vincent
 * Email:jingwei.zhang@yeahmobi.com
 */
public class HttpRequester {


    public interface Listener {
        void onSuccess(byte[] data,String url);

        void onFailure(String msg,String url);
    }


    public static void requestByGet(String urlStr, Listener listener) {
        Runnable runnable = new HttpRunnable(urlStr, listener);
        ThreadPoolProxy.getInstance().execute(runnable);
    }
}
