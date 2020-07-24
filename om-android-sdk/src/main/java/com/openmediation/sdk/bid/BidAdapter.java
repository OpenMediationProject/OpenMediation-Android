// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.bid;

import android.content.Context;

import java.util.Map;

public abstract class BidAdapter implements BidApi {

    @Override
    public void initBid(Context context, Map<String, Object> dataMap, BidCallback callback) {

    }

    @Override
    public String getBiddingToken(Context context) {
        return null;
    }

    @Override
    public void executeBid(Context context, Map<String, Object> dataMap, BidCallback callback) {

    }

    @Override
    public void notifyWin(String placementId, Map<String, Object> dataMap) {

    }

    @Override
    public void notifyLose(String placementId, Map<String, Object> dataMap) {

    }
}
