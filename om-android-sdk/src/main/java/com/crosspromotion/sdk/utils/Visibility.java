package com.crosspromotion.sdk.utils;

import android.view.View;

public class Visibility {
    private Visibility() {}

    public static boolean isScreenVisible(final int visibility) {
        return visibility == View.VISIBLE;
    }

    public static boolean hasScreenVisibilityChanged(final int oldVisibility,
            final int newVisibility) {
        return (isScreenVisible(oldVisibility) != isScreenVisible(newVisibility));
    }
}
