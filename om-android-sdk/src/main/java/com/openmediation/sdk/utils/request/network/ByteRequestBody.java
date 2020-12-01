// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;

import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class ByteRequestBody implements RequestBody {

    private final byte[] mBody;
    private final Charset mCharset;
    private final String mContentType;

    public ByteRequestBody(byte[] body) {
        this(body, Charset.forName(CommonConstants.CHARTSET_UTF8));
    }

    public ByteRequestBody(byte[] body, Charset charset) {
        this(body, charset, Headers.VALUE_APPLICATION_JSON);
    }

    public ByteRequestBody(byte[] body, String contentType) {
        this(body, Charset.forName(CommonConstants.CHARTSET_UTF8), contentType);
    }

    public ByteRequestBody(byte[] body, Charset charset, String contentType) {
        this.mBody = body;
        this.mCharset = charset;
        this.mContentType = contentType;
    }

    @Override
    public long length() {
        return mBody.length;
    }

    @Override
    public String contentType() {
        return mContentType;
    }

    @Override
    public void writeTo(OutputStream writer) throws IOException {
        IOUtil.write(writer, mBody);
    }
}
