// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.adtbid.sdk.bid.BidderTokenProvider;
import com.openmediation.sdk.bid.BidAdapter;

public class AdTimingBidAdapter extends BidAdapter {

    @Override
    public String getBiddingToken(Context context) {
        return BidderTokenProvider.getBidderToken();
    }
}
