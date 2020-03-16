// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The interface Request body.
 */
public interface RequestBody {

    /**
     * Returns the size of the data.
     *
     * @return the long
     */
    long length();

    /**
     * Gets the content type of data.
     *
     * @return the string
     */
    String contentType();

    /**
     * OutData data.
     *
     * @param writer the writer
     * @throws IOException the io exception
     */
    void writeTo(OutputStream writer) throws IOException;
}
