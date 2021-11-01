// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

public interface BidCallback {

    /**
     * Network C2S Bid Success
     * @param response BidResponse
     */
    void bidSuccess(BidResponse response);

    void bidFailed(String error);
}
