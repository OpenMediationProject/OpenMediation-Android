// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;

import android.text.TextUtils;

import com.openmediation.sdk.utils.IOUtil;
import com.openmediation.sdk.utils.constant.CommonConstants;

import java.io.BufferedInputStream;
import java.io.IOException;


public class StreamBody implements ResponseBody {

    private String mContentType;
    private BufferedInputStream mStream;

    public StreamBody(String contentType, BufferedInputStream stream) {
        this.mContentType = contentType;
        this.mStream = stream;
    }

    @Override
    public String string() throws IOException {
        String charset = Headers.parseSubValue(mContentType, "charset", CommonConstants.CHARTSET_UTF8);
        return TextUtils.isEmpty(charset) ? IOUtil.toString(mStream) : IOUtil.toString(mStream, charset);
    }

    @Override
    public byte[] byteArray() throws IOException {
        return IOUtil.toByteArray(mStream);
    }

    @Override
    public BufferedInputStream stream() throws IOException {
        return mStream;
    }

    @Override
    public void close() throws IOException {
        mStream.close();
    }
}