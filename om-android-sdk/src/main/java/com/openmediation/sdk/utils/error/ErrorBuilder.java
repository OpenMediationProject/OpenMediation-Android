// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.error;

public class ErrorBuilder {

    public static Error build(int code, String message, int internalCode) {
        return new Error(code, message, internalCode);
    }
}
