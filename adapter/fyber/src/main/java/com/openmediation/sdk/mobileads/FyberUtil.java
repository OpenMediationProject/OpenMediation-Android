package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.util.TypedValue;

public final class FyberUtil {

    static int dpToPixels(Context activity, int dpSize) {
        if (activity == null) {
            return dpSize;
        } else {
            return (int) TypedValue.applyDimension(1, (float) dpSize, activity.getResources().getDisplayMetrics());
        }
    }
}
