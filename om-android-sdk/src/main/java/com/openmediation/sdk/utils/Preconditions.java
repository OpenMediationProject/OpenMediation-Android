// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils;

import java.util.IllegalFormatException;

public final class Preconditions {
    public static final String EMPTY_ARGUMENTS = "";

    public static boolean checkNotNull(Object object) {
        return object != null;
    }

    /**
     * Ensures that an object reference is not null.
     */
    public static void checkNotNull(Object reference, boolean allowThrow) {
        checkNotNullInternal(reference, allowThrow, "Object can not be null.", EMPTY_ARGUMENTS);
    }

    private static boolean checkNotNullInternal(Object reference, boolean allowThrow,
                                                String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference != null) {
            return true;
        }
        String errorMessage = format(errorMessageTemplate, errorMessageArgs);
        if (allowThrow) {
            throw new NullPointerException(errorMessage);
        }
        return false;
    }

    /**
     * Substitutes each {@code %s} in {@code template} with an argument. These are matched by
     * position - the first {@code %s} gets {@code args[0]}, etc.
     */
    private static String format(String template, Object... args) {
        template = String.valueOf(template);  // null -> "null"
        try {
            return String.format(template, args);
        } catch (IllegalFormatException exception) {
            return template;
        }
    }
}
