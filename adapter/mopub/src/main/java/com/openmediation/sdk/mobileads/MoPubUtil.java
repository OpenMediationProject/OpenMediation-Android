// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.widget.ImageView;

import com.mopub.volley.Request;
import com.mopub.volley.Response;
import com.mopub.volley.VolleyError;
import com.mopub.volley.toolbox.ImageRequest;
import com.mopub.volley.toolbox.Volley;

final class MoPubUtil {
    static void Request(Context context, String url, Response.Listener listener,
                        Response.ErrorListener errorListener) {
        try {
            Request<Bitmap> request = new ImageRequest(url, listener, 0, 0,
                    ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.ARGB_4444, errorListener);
            Volley.newRequestQueue(context,null).add(request);
        } catch (Exception e) {
            errorListener.onErrorResponse(new VolleyError(e.getMessage()));
        }
    }

    static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (dpValue * (scale / 160) + 0.5f);
    }

    static String check(Activity activity, String adUnitId) {
        if (activity == null) {
            return "activity is null";
        }
        if (isDestroyed(activity)) {
            return "activity is destroyed";
        }
        if (TextUtils.isEmpty(adUnitId)) {
            return "instanceKey is null";
        }
        return "";
    }

    /**
     * Checks if an Activity is available
     *
     * @param activity the given activity
     * @return activity availability
     */
    private static boolean isDestroyed(Activity activity) {
        boolean flage = false;
        if (Build.VERSION.SDK_INT >= 17) {
            if (activity == null || activity.isDestroyed()) {
                flage = true;
            }
        } else {
            if (activity == null || activity.isFinishing()) {
                flage = true;
            }
        }
        return flage;
    }
}
