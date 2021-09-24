package com.openmediation.sdk.core;

import com.openmediation.sdk.utils.model.BaseInstance;

public interface BaseInsExpiredCallback {
    /**
     * Ad Expired
     * @param instance BaseInstance
     */
    void onAdExpired(BaseInstance instance);
}
