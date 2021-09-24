// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;
import android.graphics.Bitmap;
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
                    ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.RGB_565, errorListener);
            Volley.newRequestQueue(context,null).add(request);
        } catch (Throwable e) {
            errorListener.onErrorResponse(new VolleyError(e.getMessage()));
        }
    }

}
