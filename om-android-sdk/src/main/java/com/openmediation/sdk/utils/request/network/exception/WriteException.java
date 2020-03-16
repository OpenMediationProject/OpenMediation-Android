// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network.exception;

import java.io.IOException;

public class WriteException extends IOException {

    public WriteException(String message) {
        super(message);
    }

    public WriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public WriteException(Throwable cause) {
        super(cause);
    }
}
