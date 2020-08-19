// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.error;

public class Error {
    private int code;
    private String message;
    private int internalCode;

    public Error(int code, String message, int internalCode) {
        this.code = code;
        this.message = message;
        this.internalCode = internalCode;
    }

    public int getErrorCode() {
        return code;
    }

    public String getErrorMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (internalCode == -1) {
            return "Error{" +
                    "code:" + code +
                    ", message:" + message +
                    "}";
        }
        return "Error{" +
                "code:" + code +
                ", message:" + message +
                ", internalCode:" + internalCode +
                "}";
    }
}
