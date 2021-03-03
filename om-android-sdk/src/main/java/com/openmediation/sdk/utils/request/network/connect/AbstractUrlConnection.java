// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network.connect;

import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.request.network.Request;
import com.openmediation.sdk.utils.request.network.RequestBody;
import com.openmediation.sdk.utils.request.network.Response;
import com.openmediation.sdk.utils.request.network.ResponseBody;
import com.openmediation.sdk.utils.request.network.StreamBody;
import com.openmediation.sdk.utils.request.network.exception.ReadException;
import com.openmediation.sdk.utils.request.network.exception.WriteException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * The type Abstract url connection.
 */
public abstract class AbstractUrlConnection {

    private URLConnection mConnection;

    /**
     * Cancel.
     *
     * @throws Exception the exception
     */
    public abstract void cancel() throws Exception;

    /**
     * Gets response code.
     *
     * @return response code
     * @throws IOException the io exception
     */
    abstract int getResponseCode() throws IOException;

    /**
     * Connect url connection.
     *
     * @param request the request
     * @return url connection
     * @throws Exception the exception
     */
    public abstract URLConnection connect(Request request) throws Exception;

    /**
     * constructor
     */
    AbstractUrlConnection() {
    }

    /**
     * Intercept response.
     *
     * @param request the request
     * @return the response
     * @throws Exception the exception
     */
    public Response intercept(Request request) throws Exception {
//        if (!NetworkChecker.isAvailable(request.getContext())) {
//            throw new ConnectException(ErrorCode.ERROR_NETWORK_NOT_AVAILABLE);
//        }

        Request.Method method = request.getRequestMethod();
        if (isAllowBody(method)) {
            Headers headers = request.getHeaders();
            RequestBody body = request.getRequestBody();
            if (body != null && headers != null) {
                headers.set(Headers.KEY_CONTENT_LENGTH, Long.toString(body.length()));
                headers.set(Headers.KEY_CONTENT_TYPE, body.contentType());
            }
            mConnection = connect(request);
            writeBody(body);
        } else {
            mConnection = connect(request);
        }
        return readResponse(request);
    }

    private void writeBody(RequestBody body) throws WriteException {
        try {
            if (body == null) {
                return;
            }
            OutputStream stream = mConnection.getOutputStream();
            body.writeTo(IOUtil.toBufferedOutputStream(stream));
            IOUtil.closeQuietly(stream);
        } catch (Exception e) {
            throw new WriteException(e);
        }
    }

    private Response readResponse(Request request) throws ReadException {
        try {
            int code = getResponseCode();
            if (code >= 400) {
                throw new ReadException(String.format("%s RequestCode:%d", mConnection.getURL().toString(), code));
            }
            BufferedInputStream inputStream = IOUtil.toBufferedInputStream(mConnection.getInputStream());
            if (!request.shouldCallbackResponse()) {
                IOUtil.closeQuietly(inputStream);
                inputStream.close();
                cancel();
                return null;
            }

            Headers headers = parseResponseHeaders(mConnection.getHeaderFields());
            String contentType = headers.getContentType();
            ResponseBody body = new StreamBody(contentType, inputStream);
            return Response.newBuilder().code(code).headers(headers).body(body).connection(this).build();
        } catch (SocketTimeoutException e) {
            throw new ReadException(String.format("Read data time out: %1$s.", mConnection.getURL().toString()), e);
        } catch (Exception e) {
            if (e instanceof ReadException) {
                throw new ReadException(e);
            } else {
                Exception exception = new Exception(request.getUrl(), e);
                CrashUtil.getSingleton().saveException(exception);
                throw new ReadException(exception);
            }
        }
    }

    private Headers parseResponseHeaders(Map<String, List<String>> headersMap) {
        Headers headers = new Headers();
        for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * Is allow body boolean.
     *
     * @param method the method
     * @return the boolean
     */
    boolean isAllowBody(Request.Method method) {
        return method.equals(Request.Method.POST);
    }
}
