// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.request.network;

import com.nbmediation.sdk.utils.request.network.connect.AbstractUrlConnection;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.IOUtil;

import java.io.Closeable;
import java.io.IOException;

/**
 * The type Response.
 */
public final class Response implements Closeable {

    /**
     * New builder builder.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private int mCode;
    private Headers mHeaders;
    private ResponseBody mBody;
    private AbstractUrlConnection mConnection;

    private Response(Builder builder) {
        this.mCode = builder.mCode;
        this.mHeaders = builder.mHeaders;
        this.mBody = builder.mBody;
        this.mConnection = builder.mConnection;
    }

    /**
     * Get the mCode of response.
     *
     * @return the int
     */
    public int code() {
        return mCode;
    }

    /**
     * Get http headers.
     *
     * @return the headers
     */
    public Headers headers() {
        return mHeaders;
    }

    /**
     * Get http body.
     *
     * @return the response body
     */
    public ResponseBody body() {
        return mBody;
    }

    @Override
    public void close() throws IOException {
        try {
            if (mConnection != null) {
                mConnection.cancel();
                mConnection = null;
            }
            if (mHeaders != null) {
                mHeaders.clear();
                mHeaders = null;
            }
            IOUtil.closeQuietly(mBody.stream());
        } catch (Exception e) {
            DeveloperLog.LogD("Response close", e);
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                "mCode=" + mCode +
                ", mHeaders=" + mHeaders +
                ", mBody=" + mBody +
                '}';
    }

    /**
     * The type Builder.
     */
    public static final class Builder {
        private int mCode;
        private Headers mHeaders;
        private ResponseBody mBody;
        private AbstractUrlConnection mConnection;

        /**
         * Instantiates a new Builder.
         */
        Builder() {
        }

        /**
         * Code builder.
         *
         * @param code the code
         * @return the builder
         */
        public Builder code(int code) {
            this.mCode = code;
            return this;
        }

        /**
         * Headers builder.
         *
         * @param headers the headers
         * @return the builder
         */
        public Builder headers(Headers headers) {
            this.mHeaders = headers;
            return this;
        }

        /**
         * Body builder.
         *
         * @param body the body
         * @return the builder
         */
        public Builder body(ResponseBody body) {
            this.mBody = body;
            return this;
        }

        /**
         * Connection builder.
         *
         * @param connection the connection
         * @return the builder
         */
        public Builder connection(AbstractUrlConnection connection) {
            this.mConnection = connection;
            return this;
        }

        /**
         * Build response.
         *
         * @return the response
         */
        public Response build() {
            return new Response(this);
        }
    }
}
