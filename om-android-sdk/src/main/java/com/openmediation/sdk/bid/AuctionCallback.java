// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import java.util.List;

public interface AuctionCallback {
    void onBidComplete(List<BidResponse> c2sResponses, List<BidResponse> s2sResponses);
}
