// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.content.Context;

import com.facebook.ads.BidderTokenProvider;
import com.openmediation.sdk.bid.BidAdapter;
import com.openmediation.sdk.bid.BidCallback;

import java.util.Map;

public class FacebookBidAdapter extends BidAdapter {

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {
        super.initBid(context, dataMap, callback);
    }

    @Override
    public String getBiddingToken(Context context) {
        return BidderTokenProvider.getBidderToken(context);
    }

}
