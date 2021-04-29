// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import com.openmediation.sdk.utils.model.Instance;

import java.util.List;

public interface AuctionCallback {
    void onBidS2SComplete(List<BidResponse> responses);
    void onBidC2SComplete(List<Instance> c2sInstances, List<BidResponse> responses);
}
