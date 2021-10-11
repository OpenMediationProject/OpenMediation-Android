/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.core;

import com.openmediation.sdk.bid.BidResponse;
import com.openmediation.sdk.utils.model.BaseInstance;

public interface BaseInsBidCallback {

    /**
     * Instance bid success
     * @param instance bid instance
     * @param response BidResponse
     */
    void onBidSuccess(BaseInstance instance, BidResponse response);

    /**
     * Instance bid failed
     * @param instance bid instance
     * @param error error msg
     */
    void onBidFailed(BaseInstance instance, String error);
}
