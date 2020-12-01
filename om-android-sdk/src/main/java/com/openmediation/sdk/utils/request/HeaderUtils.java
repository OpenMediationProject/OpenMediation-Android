// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request;

import com.openmediation.sdk.utils.request.network.Headers;
import com.openmediation.sdk.utils.cache.DataCache;

public final class HeaderUtils {

    public static Headers getBaseHeaders() {
        Headers headers = new Headers();
        headers.set("User-Agent", DataCache.getInstance().get("UserAgent", String.class));
        headers.set("Content-Type", Headers.VALUE_APPLICATION_JSON);
        headers.set("Content-Encoding", "gzip");
        return headers;
    }
}
