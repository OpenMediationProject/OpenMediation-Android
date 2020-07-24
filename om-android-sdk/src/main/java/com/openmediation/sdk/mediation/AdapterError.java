// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mediation;

public class AdapterError {
    private Integer code;
    private String message = "";
    private boolean loadFailFromAdn = false;

    public AdapterError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public AdapterError(Integer code) {
        this.code = code;
    }

    public AdapterError(String message) {
        this.message = message;
    }

    public String getCode() {
        if (code == null) {
            return "";
        }
        return String.valueOf(code);
    }

    public String getMessage() {
        return message;
    }

    public void setLoadFailFromAdn(boolean loadFailFromAdn) {
        this.loadFailFromAdn = loadFailFromAdn;
    }

    public boolean isLoadFailFromAdn() {
        return loadFailFromAdn;
    }

    @Override
    public String toString() {
        return "{" + code +
                ", " + message +
                "}";
    }
}
