package com.openmediation.sdk.adn.utils.error;

public class ErrorBuilder {

    public static Error build(int code) {
        return new Error(code, ErrorCode.getErrorMessage(code));
    }
}
