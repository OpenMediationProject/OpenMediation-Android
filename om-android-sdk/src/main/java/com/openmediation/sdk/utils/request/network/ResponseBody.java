// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;

/**
 * The interface Response body.
 */
public interface ResponseBody extends Closeable {

    /**
     * Transforms the response data into a string.
     *
     * @return the string
     * @throws IOException the io exception
     */
    String string() throws IOException;

    /**
     * Transforms the response data into a byte array.
     *
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    byte[] byteArray() throws IOException;

    /**
     * Transforms the response data into a stream.
     *
     * @return the buffered input stream
     * @throws IOException the io exception
     */
    BufferedInputStream stream() throws IOException;
}
