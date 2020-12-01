// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;

import android.text.TextUtils;

import com.openmediation.sdk.utils.IOUtil;

import java.io.IOException;
import java.io.OutputStream;

public class StringRequestBody implements RequestBody {

    private final String mBody;
    private final String mContentType;

    public StringRequestBody(String body) {
        this(body, Headers.VALUE_APPLICATION_JSON);
    }

    public StringRequestBody(String body, String contentType) {
        this.mBody = body;
        this.mContentType = contentType;
    }

    @Override
    public long length() {
        return TextUtils.isEmpty(mBody) ? 0 : mBody.length();
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
