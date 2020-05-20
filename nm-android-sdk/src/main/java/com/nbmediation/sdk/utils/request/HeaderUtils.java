// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.request;

import com.nbmediation.sdk.utils.request.network.Headers;
import com.nbmediation.sdk.utils.cache.DataCache;

public final class HeaderUtils {

    public static Headers getBaseHeaders() {
        Headers headers = new Headers();
        headers.set("User-Agent", DataCache.getInstance().get("UserAgent", String.class));
        headers.set("Content-Type", Headers.VALUE_APPLICATION_STREAM);
        return headers;
    }
}
