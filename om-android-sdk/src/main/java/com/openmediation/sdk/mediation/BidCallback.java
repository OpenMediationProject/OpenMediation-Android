/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.mediation;

import com.openmediation.sdk.bid.BidResponse;

public interface BidCallback {

    /**
     * Network C2S Bid Success
     * @param response BidResponse
     */
    void onBidSuccess(BidResponse response);

    void onBidFailed(String error);
}
