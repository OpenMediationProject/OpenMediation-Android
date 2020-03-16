// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;


import android.content.Context;
import android.text.TextUtils;

import com.openmediation.sdk.utils.error.ErrorCode;
import com.openmediation.sdk.utils.IOUtil;

/**
 * The type Request.
 */
public class Request {

    private final Method mMethod;
    private final Headers mHeaders;
    private final int mConnectTimeout;
    private final int mReadTimeout;
    private final String mUrl;
    private final RequestBody mRequestBody;
    private final boolean isInstanceFollowRedirects;
    private final boolean isCheckChain;
    private final OnRequestCallback mCallback;
    private final Object mTag;
    private boolean mShouldCallbackResponse;

    private Context mContext;


    /**
     * The enum Method.
     */
    public enum Method {
        /**
         * Get method.
         */
        GET("GET"),
        /**
         * Post method.
         */
        POST("POST");

        private final String value;

        Method(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }


    /**
     * The interface On request callback.
     */
    public interface OnRequestCallback {
        /**
         * On request success.
         *
         * @param response the response
         */
        void onRequestSuccess(Response response);

        /**
         * On request failed.
         *
         * @param error the error
         */
        void onRequestFailed(String error);
    }

    /**
     * New builder request builder.
     *
     * @return the request builder
     */
    static RequestBuilder newBuilder() {
        return new RequestBuilder();
    }

    private Request(RequestBuilder builder) {
        mMethod = builder.mMethod;
        mHeaders = builder.mHeaders;
        mConnectTimeout = builder.mConnectTimeout;
        mReadTimeout = builder.mReadTimeout;
        mUrl = builder.mUrl;
        mRequestBody = builder.mRequestBody;
        isInstanceFollowRedirects = builder.isInstanceFollowRedirects;
        isCheckChain = builder.isCheckChain;
        mCallback = builder.mCallback;
        mTag = builder.mTag;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Gets method.
     *
     * @return the request method
     */
    public Method getRequestMethod() {
        return mMethod;
    }

    /**
     * Gets headers.
     *
     * @return the headers
     */
    public Headers getHeaders() {
        return mHeaders;
    }

    /**
     * Gets request body.
     *
     * @return the request body
     */
    public RequestBody getRequestBody() {
        return mRequestBody;
    }

    /**
     * Gets the connection timeout time, Unit is a millisecond.
     *
     * @return the connect timeout
     */
    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    /**
     * Gets the readResponse timeout time, Unit is a millisecond.
     *
     * @return the read timeout
     */
    public int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * Gets the connection can follow redirects
     *
     * @return the boolean
     */
    public boolean isInstanceFollowRedirects() {
        return isInstanceFollowRedirects;
    }

    /**
     * Is check chain boolean.
     *
     * @return the boolean
     */
    public boolean isCheckChain() {
        return isCheckChain;
    }

    /**
     * Gets context.
     *
     * @return the context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Should callback response boolean.
     *
     * @return the boolean
     */
    public boolean shouldCallbackResponse() {
        return mShouldCallbackResponse || mCallback != null;
    }

    /**
     * Gets tag.
     *
     * @return the tag
     */
    public Object getTag() {
        return mTag;
    }

    private void performRequest(Context context) {
        if (context == null) {
            callbackError(mCallback, ErrorCode.ERROR_CONTEXT);
            return;
        }

        if (TextUtils.isEmpty(mUrl)) {
            callbackError(mCallback, "request need a valid url, current is empty");
            return;
        }

        mContext = context;

        AsyncReq asyncReq = new AsyncReq(this);
        asyncReq.setCallback(new AsyncReq.OnTaskCallback() {
            @Override
            public void onSuccess(Response response) {
                if (mCallback != null) {
                    mCallback.onRequestSuccess(response);
                } else {
                    IOUtil.closeQuietly(response);
                }
            }

            @Override
            public void onError(String error) {
                if (mCallback != null) {
                    mCallback.onRequestFailed(error);
                }
            }
        });
        ReqExecutor.execute(asyncReq);
    }


    private Response syncRequest() {
        mShouldCallbackResponse = true;
        return new SyncReq(this).start();
    }

    private void callbackError(OnRequestCallback callback, String error) {
        if (callback == null) {
            throw new IllegalArgumentException(error);
        } else {
            callback.onRequestFailed(error);
        }
    }

    /**
     * The type Request builder.
     */
    public static class RequestBuilder {
        private Method mMethod;
        private Headers mHeaders;
        private int mConnectTimeout;
        private int mReadTimeout;
        private String mUrl;
        private RequestBody mRequestBody;
        private OnRequestCallback mCallback;
        private boolean isInstanceFollowRedirects;
        private boolean isCheckChain;
        private Object mTag;

        /**
         * Method request builder.
         *
         * @param method the method
         * @return the request builder
         */
        public RequestBuilder method(Method method) {
            mMethod = method;
            return this;
        }

        /**
         * Headers request builder.
         *
         * @param headers the headers
         * @return the request builder
         */
        public RequestBuilder headers(Headers headers) {
            mHeaders = headers;
            return this;
        }

        /**
         * Connect timeout request builder.
         *
         * @param timeout the timeout
         * @return the request builder
         */
        public RequestBuilder connectTimeout(int timeout) {
            mConnectTimeout = timeout;
            return this;
        }

        /**
         * Read timeout request builder.
         *
         * @param readTimeout the read timeout
         * @return the request builder
         */
        public RequestBuilder readTimeout(int readTimeout) {
            mReadTimeout = readTimeout;
            return this;
        }

        /**
         * Url request builder.
         *
         * @param url the url
         * @return the request builder
         */
        public RequestBuilder url(String url) {
            mUrl = url;
            return this;
        }

        /**
         * Body request builder.
         *
         * @param body the body
         * @return the request builder
         */
        public RequestBuilder body(RequestBody body) {
            mRequestBody = body;
            return this;
        }

        /**
         * Instance follow redirects request builder.
         *
         * @param redirects the redirects
         * @return the request builder
         */
        public RequestBuilder instanceFollowRedirects(boolean redirects) {
            isInstanceFollowRedirects = redirects;
            return this;
        }

        /**
         * Is check chain request builder.
         *
         * @param isCheckChain the is check chain
         * @return the request builder
         */
        public RequestBuilder isCheckChain(boolean isCheckChain) {
            this.isCheckChain = isCheckChain;
            return this;
        }

        /**
         * Callback request builder.
         *
         * @param callback the callback
         * @return the request builder
         */
        public RequestBuilder callback(OnRequestCallback callback) {
            mCallback = callback;
            return this;
        }

        /**
         * Tag request builder.
         *
         * @param tag the tag
         * @return the request builder
         */
        public RequestBuilder tag(Object tag) {
            mTag = tag;
            return this;
        }

        /**
         * Sync request response.
         *
         * @return the response
         */
        public Response syncRequest() {
            return new Request(this).syncRequest();
        }

        /**
         * Perform request.
         *
         * @param context the context
         */
        public void performRequest(Context context) {
            new Request(this).performRequest(context);
        }
    }
}
